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
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed(doc, "query");
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
	public CmisItemCollection getCollectionFromFeed(final String feedUrl) {
		Document doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser,
				repostoryPassword);
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

}
