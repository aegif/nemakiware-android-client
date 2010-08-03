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
package de.fmaul.android.cmis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import android.text.TextUtils;
import de.fmaul.android.cmis.repo.CmisProperty;

public class FeedUtils {

	private static final Namespace CMISRA = Namespace.get("http://docs.oasis-open.org/ns/cmis/restatom/200908/");
	private static final Namespace CMIS = Namespace.get("http://docs.oasis-open.org/ns/cmis/core/200908/");

	private static final QName CMISRA_REPO_INFO = QName.get("repositoryInfo", CMISRA);
	private static final QName CMIS_REPO_ID = QName.get("repositoryId", CMIS);
	private static final QName CMIS_REPO_NAME = QName.get("repositoryName", CMIS);
	private static final QName CMISRA_COLLECTION_TYPE = QName.get("collectionType", CMISRA);
	private static final QName CMISRA_URI_TEMPLATE = QName.get("uritemplate", CMISRA);
	private static final QName CMISRA_TYPE = QName.get("type", CMISRA);
	private static final QName CMISRA_TEMPLATE = QName.get("template", CMISRA);
	private static final QName CMISRA_OBJECT = QName.get("object", CMISRA);
	private static final QName CMIS_PROPERTIES = QName.get("properties", CMIS);
	private static final QName CMIS_VALUE = QName.get("value", CMIS);

	public static Document readAtomFeed(final String feed, final String user, final String password) throws FeedLoadException {
		Document document = null;
		try {
			InputStream is = HttpUtils.getWebRessourceAsStream(feed, user, password);
			SAXReader reader = new SAXReader(); // dom4j SAXReader
			document = reader.read(is); // dom4j Document

		} catch (ClientProtocolException e) {
			throw new FeedLoadException(e);
		} catch (IOException e) {
			throw new FeedLoadException(e);
		} catch (DocumentException e) {
			throw new FeedLoadException(e);
		}
		return document;
	}

	public static String getRootFeedFromRepo(String url, String user, String password) throws Exception {
		try {
			Document doc = readAtomFeed(url, user, password);
			return getCollectionUrlFromRepoFeed(doc, "root");
		} catch (Exception e) {
	        throw new Exception("Wrong Parameters");
	    }
	}
	
	public static List<String> getRootFeedsFromRepo(String url, String user, String password) throws Exception {
		try {
			Document doc = readAtomFeed(url, user, password);
			return getWorkspaceFromRepoFeed(doc, "root");
		} catch (Exception e) {
	        throw new Exception("Wrong Parameters");
	    }
	}
	
	public static List<String> getWorkspaceFromRepoFeed(Document doc, String type) {
		List<String> listWorkspace = new ArrayList<String>(2);
		if (doc != null) {
			List<Element> workspaces = doc.getRootElement().elements("workspace");
			if (workspaces.size() > 0){
				for (Element workspace : workspaces) {
					Element repoInfo = workspace.element(CMISRA_REPO_INFO);
					Element repoId = repoInfo.element(CMIS_REPO_NAME);
					listWorkspace.add(repoId.getText());
				}
			}
		}
		return listWorkspace;
	}
	
	public static String getCollectionUrlFromRepoFeed(Document doc, String type, String workspaceName) {
		if (doc != null) {
			
			List<Element> collections = getWorkspace(doc, workspaceName).elements("collection");

			for (Element collection : collections) {
				String currentType = collection.elementText(CMISRA_COLLECTION_TYPE);
				if (type.equals(currentType.toLowerCase())) {
					return collection.attributeValue("href");
				}
			}
		}
		return "";
	}
	
	public static Element getWorkspace(Document doc, String workspaceName){
		List<Element> workspaces = doc.getRootElement().elements("workspace");
		Element workspace = null;
		if (workspaces.size() > 0){
			for (Element wSpace : workspaces) {
				Element repoInfo = wSpace.element(CMISRA_REPO_INFO);
				Element repoId = repoInfo.element(CMIS_REPO_NAME);
				if (workspaceName.equals(repoId.getData())){
					return workspace = wSpace;
				}
			}
		} else {
			workspace = null;
		}
		return workspace;
	}
	

	public static String getCollectionUrlFromRepoFeed(Document doc, String type) {
		if (doc != null) {
			Element workspace = doc.getRootElement().element("workspace");

			List<Element> collections = workspace.elements("collection");

			for (Element collection : collections) {
				String currentType = collection.elementText(CMISRA_COLLECTION_TYPE);
				if (type.equals(currentType.toLowerCase())) {
					return collection.attributeValue("href");
				}
			}
		}
		return "";
	}

	public static String getSearchQueryFeedTitle(String urlTemplate, String query) {

		return getSearchQueryFeedCmisQuery(urlTemplate, "SELECT * FROM cmis:document WHERE cmis:name LIKE '%" + query + "%'");
	}

	public static String getSearchQueryFeedFullText(String urlTemplate, String query) {
		String[] words = TextUtils.split(query.trim(), "\\s+");

		for (int i = 0; i < words.length; i++) {
			words[i] = "contains ('" + words[i] + "')";
		}

		String condition = TextUtils.join(" AND ", words);

		return getSearchQueryFeedCmisQuery(urlTemplate, "SELECT * FROM cmis:document WHERE " + condition);
	}

	public static String getSearchQueryFeedCmisQuery(String urlTemplate, String cmisQuery) {
		final String encodedCmisQuery = URLEncoder.encode(cmisQuery);

		final CharSequence feedUrl = TextUtils.replace(urlTemplate, new String[] { "{q}", "{searchAllVersions}", "{maxItems}", "{skipCount}",
				"{includeAllowableActions}", "{includeRelationships}" }, new String[] { encodedCmisQuery, "false", "50", "0", "false", "false" });

		return feedUrl.toString();
	}
	
	
	public static String getUriTemplateFromRepoFeed(Document doc, String type,  String workspaceName) {

		List<Element> templates = getWorkspace(doc, workspaceName).elements(CMISRA_URI_TEMPLATE);

		for (Element template : templates) {
			String currentType = template.elementText(CMISRA_TYPE);
			if (type.equals(currentType.toLowerCase())) {
				return template.elementText(CMISRA_TEMPLATE);
			}
		}
		return null;
	}
	
	public static String getUriTemplateFromRepoFeed(Document doc, String type) {

		Element workspace = doc.getRootElement().element("workspace");

		List<Element> templates = workspace.elements(CMISRA_URI_TEMPLATE);

		for (Element template : templates) {
			String currentType = template.elementText(CMISRA_TYPE);
			if (type.equals(currentType.toLowerCase())) {
				return template.elementText(CMISRA_TEMPLATE);
			}
		}
		return null;
	}

	public static Map<String, CmisProperty> getCmisPropertiesForEntry(Element feedEntry) {
		Map<String, CmisProperty> props = new HashMap<String, CmisProperty>();

		Element objectElement = feedEntry.element(CMISRA_OBJECT);
		if (objectElement != null) {
			Element properitesElement = objectElement.element(CMIS_PROPERTIES);
			if (properitesElement != null) {
				List<Element> properties = properitesElement.elements();

				for (Element property : properties) {
					final String id = property.attributeValue("propertyDefinitionId");

					props.put(id,
							new CmisProperty(property.getName(), id, property.attributeValue("localName"), property.attributeValue("displayName"),
									property.elementText(CMIS_VALUE)));
				}
			}
		}
		return props;
	}

}