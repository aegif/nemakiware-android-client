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

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;

import android.app.Application;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.HttpUtils;
import de.fmaul.android.cmis.utils.StorageUtils;

/**
 * @author Florian Maul
 */
public class CmisRepository {

	private final String feedRootCollection;
	private final String feedTypesCollection;
	private final String uriTemplateQuery;
	private final String uriTemplateTypeById;
	private final String repositoryUser;
	private final String repositoryPassword;
	private final String repositoryWorkspace;
	private final Application application;
	

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
	private CmisRepository(Application application, String repositoryUrl, String user, String password, String workspace) {
		this.application = application;
		this.repositoryUser = user;
		this.repositoryPassword = password;
		this.repositoryWorkspace = workspace;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser, repositoryPassword);
		feedRootCollection = FeedUtils.getCollectionUrlFromRepoFeed(doc, "root", workspace);
		feedTypesCollection = FeedUtils.getCollectionUrlFromRepoFeed(doc, "types", workspace);

		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed(doc, "query", workspace);
		uriTemplateTypeById = FeedUtils.getUriTemplateFromRepoFeed(doc, "typebyid", workspace);
	}

	/**
	 * Creates a repository connection from the application preferences.
	 * 
	 * @param prefs
	 * @return
	 */
	public static CmisRepository create(Application app, final Prefs prefs) {
		return new CmisRepository(app, prefs.getUrl(), prefs.getUser(), prefs.getPassword(), prefs.getWorkspace());
	}
	
	public static CmisRepository create(Application app, final Server server) {
		return new CmisRepository(app, server.getUrl(), server.getUsername(), server.getPassword(), server.getWorkspace());
	}

	/**
	 * Returns the root collection with documents and folders.
	 * 
	 * @return
	 */
	public CmisItemCollection getRootCollection() {
		return getCollectionFromFeed(feedRootCollection);
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
	 */
	public CmisItemCollection getCollectionFromFeed(final String feedUrl) {
		Document doc;

		if (StorageUtils.isFeedInCache(application, feedUrl)) {
			doc = StorageUtils.getFeedFromCache(application, feedUrl);
		} else {
			doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser, repositoryPassword);
			if (doc != null) {
				StorageUtils.storeFeedInCache(application, feedUrl, doc);
			}
		}
		return CmisItemCollection.createFromFeed(doc);
	}

	public CmisTypeDefinition getTypeDefinition(String documentTypeId) {
		String url = uriTemplateTypeById.replace("{id}", documentTypeId);
		Document doc = FeedUtils.readAtomFeed(url, repositoryUser, repositoryPassword);
		return CmisTypeDefinition.createFromFeed(doc);
	}

	public File retreiveContent(CmisItem item) {
		File contentFile = StorageUtils.getStorageFile(application, StorageUtils.DUMMYREPO, StorageUtils.TYPE_CONTENT, item.getId(), item.getTitle());

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
	private void downloadContent(CmisItem item, OutputStream os) throws ClientProtocolException, IOException {
		HttpUtils.getWebRessource(item.getContentUrl(), repositoryUser, repositoryPassword).getEntity().writeTo(os);
	}

	public void clearCache() {
		StorageUtils.deleteRepositoryFiles(application, StorageUtils.DUMMYREPO);
	}

}
