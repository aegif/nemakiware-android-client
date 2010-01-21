package de.fmaul.android.cmis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

	public static List<CmisDoc> readDocsFromFeed(Document document) {
		List<CmisDoc> docs = new ArrayList<CmisDoc>();
		if (document != null) {
			parseFeed(document, docs);
			parseEntries(document, docs);
		}
		return docs;
	}

	private static void parseFeed(Document document, List<CmisDoc> docs) {
		List<Element> feedLinks = document.getRootElement().elements("link");
		for (Element link : feedLinks) {
			if ("up".equalsIgnoreCase(link.attributeValue("rel"))) {
				docs.add(new CmisDoc("", "..", link.attributeValue("href"), "",
						null, null, null));
			}
		}
	}

	private static void parseEntries(Document document, List<CmisDoc> docs) {
		List<Element> entries = document.getRootElement().elements("entry");
		for (Element entry : entries) {
			parseEntry(docs, entry);
		}
	}

	private static void parseEntry(List<CmisDoc> docs, Element entry) {
		String title = entry.element("title").getText();
		String id = entry.element("id").getText();
		String linkChildrenFeed = "";
		String contentUrl = "";
		String mimeType = "";
		String author = entry.element("author").element("name").getText();

		// 2009-11-03T11:55:39.495Z
		Date modfiedDate = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		try {
			modfiedDate = df.parse(entry.element("updated").getText());
		} catch (ParseException e) {
			// meh
		}

		Element contentElement = entry.element("content");
		if (contentElement != null) {
			contentUrl = contentElement.attributeValue("src");
			mimeType = contentElement.attributeValue("type");
		}

		for (Element link : (List<Element>) entry.elements("link")) {
			if ("down".equals(link.attribute("rel").getText())) {

				if (link.attribute("type").getText().startsWith(
						"application/atom+xml")) {
					linkChildrenFeed = link.attribute("href").getText();
				}
			}
		}

		docs.add(new CmisDoc(id, title, linkChildrenFeed, author, contentUrl,
				mimeType, modfiedDate));
	}

	public static String getRootFeedFromRepo(String url, String user,
			String password) {

		Document doc = readAtomFeed(url, user, password);
		if (doc != null) {
			Element workspace = doc.getRootElement().element("workspace");

			List<Element> collections = workspace.elements("collection");

			for (Element collection : collections) {
				String type = collection.elementText(CMISRA_COLLECTION_TYPE);
				if ("root".equals(type.toLowerCase())) {
					return collection.attributeValue("href");
				}
			}
		}
		return "";
	}

	public static String getSearchQueryFeed(QueryType queryType,
			String baseUrl, String query) {
		switch (queryType) {
		case TITLE:
			return getSearchQueryFeedTitle(baseUrl, query);
		case CMISQUERY:
			return getSearchQueryFeedCmisQuery(baseUrl, query);
		case FULLTEXT:
		default:
			return getSearchQueryFeedFullText(baseUrl, query);
		}
	}

	private static String getSearchQueryFeedTitle(String queryBaseUrl, String query) {

		return getSearchQueryFeedCmisQuery(queryBaseUrl, "SELECT * FROM cmis:document WHERE cmis:name LIKE '%"+query+"%'");
	}

	private static String getSearchQueryFeedFullText(String baseUrl,
			String query) {
		String[] words = TextUtils.split(query.trim(), "\\s+");

		for (int i = 0; i < words.length; i++) {
			words[i] = "contains ('" + words[i] + "')";
		}

		String condition = TextUtils.join(" AND ", words);

		return getSearchQueryFeedCmisQuery(baseUrl, "SELECT * FROM cmis:document WHERE " + condition);
	}

	private static String getSearchQueryFeedCmisQuery(String baseUrl, String cmisQuery) {
		final String encodedCmisQuery = URLEncoder
				.encode(cmisQuery);
		final String url = baseUrl + "/query?q=" + encodedCmisQuery + "&maxItems=50";
		return url;
	}

}