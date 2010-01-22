package de.fmaul.android.cmis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import android.text.TextUtils;

public class FeedUtils {

	private static final Namespace CMISRA = Namespace
			.get("http://docs.oasis-open.org/ns/cmis/restatom/200908/");
	private static final QName CMISRA_COLLECTION_TYPE = QName.get(
			"collectionType", CMISRA);

	public static Document readAtomFeed(final String feed, final String user,
			final String password) throws FeedLoadException {
		Document document = null;
		try {
			InputStream is = HttpUtils.getWebRessourceAsStream(feed, user,
					password);
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


	public static String getRootFeedFromRepo(String url, String user,
			String password) {

		Document doc = readAtomFeed(url, user, password);
		return getCollectionUrlFromRepoFeed(doc, "root");
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

	public static String getSearchQueryFeedTitle(String queryBaseUrl, String query) {

		return getSearchQueryFeedCmisQuery(queryBaseUrl, "SELECT * FROM cmis:document WHERE cmis:name LIKE '%"+query+"%'");
	}

	public static String getSearchQueryFeedFullText(String baseUrl,
			String query) {
		String[] words = TextUtils.split(query.trim(), "\\s+");

		for (int i = 0; i < words.length; i++) {
			words[i] = "contains ('" + words[i] + "')";
		}

		String condition = TextUtils.join(" AND ", words);

		return getSearchQueryFeedCmisQuery(baseUrl, "SELECT * FROM cmis:document WHERE " + condition);
	}

	public static String getSearchQueryFeedCmisQuery(String baseUrl, String cmisQuery) {
		final String encodedCmisQuery = URLEncoder
				.encode(cmisQuery);
		final String url = baseUrl + "/query?q=" + encodedCmisQuery + "&maxItems=50";
		return url;
	}


}