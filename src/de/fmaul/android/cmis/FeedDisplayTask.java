package de.fmaul.android.cmis;

import android.app.ListActivity;
import android.os.AsyncTask;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class FeedDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private final ListActivity activity;
	private final CmisRepository repository;
	private final String title;

	public FeedDisplayTask(ListActivity activity, CmisRepository repository) {
		this(activity, repository, null);
	}

	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title) {
		super();
		this.activity = activity;
		this.repository = repository;
		this.title = title;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected CmisItemCollection doInBackground(String... params) {
		try {
			String feed = params[0];
			if (feed == null) {
				return repository.getRootCollection();
			}
			else {
				return repository.getCollectionFromFeed(feed);
			}
		} catch (FeedLoadException fle) {
			return CmisItemCollection.emptyCollection();
			//FIXME eror handling
		}
	}

	@Override
	protected void onPostExecute(CmisItemCollection itemCollection) {
		activity.setListAdapter(new CmisItemCollectionAdapter(activity, R.layout.row,
				itemCollection));
		if (title == null) {
			activity.getWindow().setTitle(itemCollection.getTitle());
		}
		else {
			activity.getWindow().setTitle(title);
		}
		activity.setProgressBarIndeterminateVisibility(false);

	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}