package de.fmaul.android.cmis.repo;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;

import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.HttpUtils;

/**
 * @author Florian Maul
 */
public class CmisRepository {

	private final String feedRootCollection;
	private final String uriTemplateQuery;
	private final String repositoryUser;
	private final String repostoryPassword;

	/**
	 * Connects to a CMIS Repository with the given connection information
	 * @param repositoryUrl
	 * @param user
	 * @param password
	 */
	private CmisRepository(String repositoryUrl, String user, String password) {
		this.repositoryUser = user;
		this.repostoryPassword = password;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser,
				repostoryPassword);
		feedRootCollection = FeedUtils
				.getCollectionUrlFromRepoFeed(doc, "root");
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed(doc,
				"query");
	}

	public static CmisRepository create(final Prefs prefs) {
		return new CmisRepository(prefs.getUrl(), prefs.getUser(), prefs
				.getPassword());
	}

	public CmisItemCollection getRootCollection() {
		return getCollectionFromFeed(feedRootCollection);
	}

	public CmisItemCollection getChildren(CmisItem item) {
		return getCollectionFromFeed(item.getDownLink());
	}

	public String getSearchFeed(QueryType queryType, String query) {
		switch (queryType) {
		case TITLE:
			return FeedUtils
					.getSearchQueryFeedTitle(uriTemplateQuery, query);
		case CMISQUERY:
			return FeedUtils.getSearchQueryFeedCmisQuery(uriTemplateQuery,
					query);
		case FULLTEXT:
		default:
			return FeedUtils.getSearchQueryFeedFullText(uriTemplateQuery,
					query);
		}
	}

	public CmisItemCollection getCollectionFromFeed(final String feedUrl) {
		Document doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser,
				repostoryPassword);
		return CmisItemCollection.createFromFeed(doc);
	}

	public void fetchContent(String contentUrl, OutputStream os)
			throws ClientProtocolException, IOException {
		HttpUtils
				.getWebRessource(contentUrl, repositoryUser, repostoryPassword)
				.getEntity().writeTo(os);
	}

}
