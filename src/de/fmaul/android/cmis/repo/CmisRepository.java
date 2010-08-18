/*
 * Copyright (C) 2010 Florian Maul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fmaul.android.cmis.repo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.Element;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import de.fmaul.android.cmis.FilterPrefs;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.HttpUtils;
import de.fmaul.android.cmis.utils.StorageException;
import de.fmaul.android.cmis.utils.StorageUtils;

/**
 * @author Florian Maul
 */
public class CmisRepository {

	private static final String TAG = "CmisRepository";
	
	private final String feedRootCollection;
	private final String feedTypesCollection;
	private final String uriTemplateQuery;
	private final String uriTemplateTypeById;
	private final String repositoryUser;
	private final String repositoryPassword;
	private final String repositoryWorkspace;
	private String feedParams;
	private Boolean useFeedParams;
	private final Application application;
	private final String repositoryName;
	private String repositoryUrl;
	private final Server server;
	private int skipCount = 0;
	private int maxItems = 0;
	private Boolean paging;
	private int numItems;
	
	/**
	 * Connects to a CMIS Repository with the given connection information FIXME
	 * References to Application should be removed with DI
	 * 
	 * @param repositoryUrl
	 *            The base ATOM feed URL of the CMIS repository
	 * @param user
	 *            The user name to login to the repository
	 * @param password
	 *            The password to login to the repository
	 */
	private CmisRepository(Application application, Server server) {
		this.application = application;
		this.repositoryUser = server.getUsername();
		this.repositoryPassword = server.getPassword();
		this.repositoryWorkspace = server.getWorkspace();
		this.repositoryName = server.getName();
		this.repositoryUrl = server.getUrl();
		this.server = server;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser, repositoryPassword);
		
		Element wsElement = FeedUtils.getWorkspace(doc, repositoryWorkspace);
		
		feedRootCollection = FeedUtils.getCollectionUrlFromRepoFeed("root", wsElement);
		feedTypesCollection = FeedUtils.getCollectionUrlFromRepoFeed("types", wsElement);
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed("query", wsElement);
		uriTemplateTypeById = FeedUtils.getUriTemplateFromRepoFeed("typebyid", wsElement);
	}

	public String getFeedRootCollection() {
		return feedRootCollection;
	}

	public void setFeedParams(String feedParams) {
		this.feedParams = feedParams;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * Creates a repository connection from the application preferences.
	 * 
	 * @param prefs
	 * @return
	 */
	public static CmisRepository create(Application app, final Server server) {
		return new CmisRepository(app, server);
	}
	
	/**
	 * Returns the root collection with documents and folders.
	 * 
	 * @return
	 * @throws Exception 
	 * @throws FeedLoadException 
	 */
	public CmisItemCollection getRootCollection() throws FeedLoadException, Exception {
		return getCollectionFromFeed(feedRootCollection);
	}
	
	public CmisItemCollection getRootCollection(String params) throws FeedLoadException, StorageException {
		return getCollectionFromFeed(feedRootCollection + params);
	}

	/**
	 * Returns the ATOM feed that can be used to perform a search for the
	 * specified search terms.
	 * 
	 * @param queryType
	 *            A {@link QueryType} that specifies the type of search.
	 * @param query
	 *            A query that will be run against the repository.
	 * @return
	 */
	public String getSearchFeed(QueryType queryType, String query) {
		switch (queryType) {
		case TITLE:
			return FeedUtils.getSearchQueryFeedTitle(uriTemplateQuery, query);
		case CMISQUERY:
			return FeedUtils.getSearchQueryFeedCmisQuery(uriTemplateQuery, query);
		case FULLTEXT:
		default:
			return FeedUtils.getSearchQueryFeedFullText(uriTemplateQuery, query);
		}
	}

	/**
	 * Returns the collection of {@link CmisItem}s for a given feed url from the
	 * CMIS repository.
	 * 
	 * @param feedUrl
	 * @return
	 * @throws Exception 
	 * @throws FeedLoadException 
	 */
	public CmisItemCollection getCollectionFromFeed(final String feedUrl) throws FeedLoadException, StorageException {
		Document doc;
		Log.d(TAG, "feedUrl : " + feedUrl);
		if (StorageUtils.isFeedInCache(application, feedUrl, repositoryWorkspace)) {
			doc = StorageUtils.getFeedFromCache(application, feedUrl, repositoryWorkspace);
		} else {
			doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser, repositoryPassword);
			if (doc != null) {
				StorageUtils.storeFeedInCache(application, feedUrl, doc, repositoryWorkspace);
			}
		}
		
		numItems = FeedUtils.getNumItemsFeed(doc);
		Log.d(TAG, "NumItems : " + numItems);
		
		return CmisItemCollection.createFromFeed(doc);
	}

	public CmisTypeDefinition getTypeDefinition(String documentTypeId) {
		String url = uriTemplateTypeById.replace("{id}", documentTypeId);
		Document doc = FeedUtils.readAtomFeed(url, repositoryUser, repositoryPassword);
		return CmisTypeDefinition.createFromFeed(doc);
	}

	public File retreiveContent(CmisItemLazy item) throws StorageException {
		File contentFile = StorageUtils.getStorageFile(application, repositoryWorkspace, StorageUtils.TYPE_CONTENT, item.getId(), item.getTitle());
		return retreiveContent(item, contentFile);
	}
	
	public File retreiveContent(CmisItemLazy item, String downloadFolder) throws StorageException {
		File contentFile = item.getContentDownload(application, downloadFolder);
		return retreiveContent(item, contentFile);
	}
	
	private File retreiveContent(CmisItemLazy item, File contentFile) throws StorageException {
		try {
			contentFile.getParentFile().mkdirs();
			contentFile.createNewFile();
			OutputStream os = new BufferedOutputStream(new FileOutputStream(contentFile));

			downloadContent(item, os);
			os.close();
			return contentFile;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Fetches the contents from the CMIS repository for the given
	 * {@link CmisItem}.
	 */
	private void downloadContent(CmisItemLazy item, OutputStream os) throws ClientProtocolException, IOException {
		HttpUtils.getWebRessource(item.getContentUrl(), repositoryUser, repositoryPassword).getEntity().writeTo(os);
	}

	public void clearCache(String workspace) throws StorageException {
		StorageUtils.deleteRepositoryFiles(application, workspace);
	}
	
	public void generateParams(Activity activity){
		FilterPrefs pref = new FilterPrefs(activity);
		if (pref.getParams()){
			setUseFeedParams(true);
			if (pref.getPaging()){
				setPaging(true);
			} else {
				setPaging(false);
			}
			setFeedParams(createParams(pref));
		} else {
			setUseFeedParams(false);
		}
	}
	
	public void generateParams(Activity activity, Boolean isAdd){
		FilterPrefs pref = new FilterPrefs(activity);
		if (pref.getParams()){
			setUseFeedParams(true);
			if (pref.getPaging()){
				setPaging(true);
			} else {
				setPaging(false);
			}
			setFeedParams(createParams(pref, isAdd, false));
		} else {
			setUseFeedParams(false);
		}
	}
	
	private String createParams(FilterPrefs pref, Boolean isAdd, Boolean isFirst){
		String params = "";
		String value = "";
		ArrayList<String> listParams = new ArrayList<String>(4);
		
		if (pref != null && pref.getParams()){
			
			value = pref.getTypes();
			if (value != null && value.length() > 0){
				listParams.add("types" + "=" +  pref.getTypes());
			}
			
			/*
			if (pref.getFilter() != null){
				paramsList.put("filter", pref.getFilter());
			}*/
			
			if (pref.getPaging()){
				if (isFirst){
					listParams.add("skipCount" + "=0");
					setSkipCount(0);
				} else {
					value = pref.getMaxItems();
					if (value != null) {
						if (value.length() == 0 ){
							value = "0";
							setMaxItems(Integer.parseInt(value));
						}
						int skipCountValue = 0;
						if (isAdd){
							skipCountValue = getSkipCount() + getMaxItems() ;
						} else {
							skipCountValue = getSkipCount() - getMaxItems();
						}
						if (skipCountValue < 0){
							skipCountValue = 0;
						}
						listParams.add("skipCount" + "=" + skipCountValue);
						setSkipCount(skipCountValue);
					}
				}
			}
			
			value = pref.getMaxItems();
			if (value != null && value.length() > 0 && Integer.parseInt(value) > 0){
				listParams.add("maxItems" + "=" + pref.getMaxItems());
				setMaxItems(Integer.parseInt(value));
			}
			
			value = pref.getOrder() ;
			if (pref.getOrder() != null && pref.getOrder().length() > 0){
				listParams.add("orderBy" + "=" + pref.getOrder());
			}
			
			params = "?" + TextUtils.join("&", listParams);
		}
		
		try {
			params = new URI(null, params, null).toASCIIString();
		} catch (URISyntaxException e) {
		}
		Log.d(TAG, "Params : " + params);
		return params;
	}
	
	
	private String createParams(FilterPrefs pref){
		return  createParams(pref, true, true);
	}
	
	public String getFeedParams() {
		return feedParams;
	}

	public Boolean getUseFeedParams() {
		if (useFeedParams != null){
			return useFeedParams;
		} else {
			return false;
		}
	}
	
	public void setUseFeedParams(Boolean useFeedParams) {
		this.useFeedParams = useFeedParams;
	}
	
	public Server getServer() {
		return server;
	}

	public void setSkipCount(int skipCount) {
		Log.d(TAG, "skipCount :" + skipCount);
		this.skipCount = skipCount;
	}

	public int getSkipCount() {
		return skipCount;
	}

	public void setPaging(Boolean paging) {
		Log.d(TAG, "Paging :" + paging);
		this.paging = paging;
	}

	public Boolean isPaging() {
		if (paging == null){
			paging = false;
		}
		return paging;
	}

	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

	public int getNumItems() {
		return numItems;
	}

	public void setMaxItems(int maxItems) {
		Log.d(TAG, "MaxItems :" + maxItems);
		this.maxItems = maxItems;
	}

	public int getMaxItems() {
		return maxItems;
	}
}
