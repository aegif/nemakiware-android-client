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

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	private final String repostoryPassword;

	/**
	 * Connects to a CMIS Repository with the given connection information
	 * 
	 * @param repositoryUrl
	 *            The base ATOM feed URL of the CMIS repository
	 * @param user
	 *            The user name to login to the repository
	 * @param password
	 *            The password to login to the repository
	 */
	private CmisRepository(String repositoryUrl, String user, String password) {
		this.repositoryUser = user;
		this.repostoryPassword = password;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser,
				repostoryPassword);
		feedRootCollection = FeedUtils
				.getCollectionUrlFromRepoFeed(doc, "root");
		feedTypesCollection = FeedUtils
		.getCollectionUrlFromRepoFeed(doc, "types");
		
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed(doc, "query");
		uriTemplateTypeById = FeedUtils.getUriTemplateFromRepoFeed(doc, "typebyid");
	}

	/**
	 * Creates a repository connection from the application preferences.
	 * 
	 * @param prefs
	 * @return
	 */
	public static CmisRepository create(final Prefs prefs) {
		return new CmisRepository(prefs.getUrl(), prefs.getUser(), prefs
				.getPassword());
	}

	/**
	 * Returns the root collection with documents and folders.
	 * 
	 * @return
	 */
	public CmisItemCollection getRootCollection(Application app) {
		return getCollectionFromFeed(app, feedRootCollection);
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
			return FeedUtils.getSearchQueryFeedCmisQuery(uriTemplateQuery,
					query);
		case FULLTEXT:
		default:
			return FeedUtils
					.getSearchQueryFeedFullText(uriTemplateQuery, query);
		}
	}

	/**
	 * Returns the collection of {@link CmisItem}s for a given feed url from the
	 * CMIS repository.
	 * 
	 * @param feedUrl
	 * @return
	 */
	public CmisItemCollection getCollectionFromFeed(Application app, final String feedUrl) {
		Document doc;
		
		if (StorageUtils.isFeedInCache(app, feedUrl)) {
			doc = StorageUtils.getFeedFromCache(app, feedUrl);
		}
		else {
			doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser,
				repostoryPassword);
			if (doc != null) {
				StorageUtils.storeFeedInCache(app, feedUrl, doc);
			}
		}
		return CmisItemCollection.createFromFeed(doc);
	}


	
	/**
	 * Fetches the contents from the CMIS repository for the given {@link CmisItem}.

	 */
	public void fetchContent(CmisItem item, OutputStream os)
			throws ClientProtocolException, IOException {
		HttpUtils
				.getWebRessource(item.getContentUrl(), repositoryUser, repostoryPassword)
				.getEntity().writeTo(os);
	}

	public CmisTypeDefinition getTypeDefinition(String documentTypeId) {
		String url = uriTemplateTypeById.replace("{id}", documentTypeId);
		Document doc = FeedUtils.readAtomFeed(url, repositoryUser,
				repostoryPassword);
		return CmisTypeDefinition.createFromFeed(doc);
	}
	
}
