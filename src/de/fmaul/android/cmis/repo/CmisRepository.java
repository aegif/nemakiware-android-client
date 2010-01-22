package de.fmaul.android.cmis.repo;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;

import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.HttpUtils;

/**
 * NOT USED YET... Should contain all information about the repo
 * 
 * @author Florian
 * 
 */
public class CmisRepository {

	private final String feedRootCollection;
	private final String feedQueryCollection;
	private final String repositoryUser;
	private final String repostoryPassword;

	private CmisRepository(String repositoryUrl, String user, String password) {
		this.repositoryUser = user;
		this.repostoryPassword = password;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser,
				repostoryPassword);
		feedRootCollection = FeedUtils
				.getCollectionUrlFromRepoFeed(doc, "root");
		feedQueryCollection = FeedUtils.getCollectionUrlFromRepoFeed(doc,
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
					.getSearchQueryFeedTitle(feedQueryCollection, query);
		case CMISQUERY:
			return FeedUtils.getSearchQueryFeedCmisQuery(feedQueryCollection,
					query);
		case FULLTEXT:
		default:
			return FeedUtils.getSearchQueryFeedFullText(feedQueryCollection,
					query);
		}

	}

	public CmisItemCollection getCollectionFromFeed(final String feedUrl) {
		Document doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser,
				repostoryPassword);
		return CmisItemCollection.createFromFeed(doc);
	}

	public String getFeedRootCollection() {
		return feedRootCollection;
	}

	public String getFeedQueryCollection() {
		return feedQueryCollection;
	}

	public void fetchContent(String contentUrl, OutputStream os)
			throws ClientProtocolException, IOException {
		HttpUtils
				.getWebRessource(contentUrl, repositoryUser, repostoryPassword)
				.getEntity().writeTo(os);
	}

}
