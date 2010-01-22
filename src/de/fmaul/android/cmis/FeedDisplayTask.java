package de.fmaul.android.cmis;

import android.app.ListActivity;
import android.os.AsyncTask;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class FeedDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private final ListActivity activity;
	private final CmisRepository repository;

	public FeedDisplayTask(ListActivity activity, CmisRepository repository) {
		super();
		this.activity = activity;
		this.repository = repository;
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
		activity.getWindow().setTitle(itemCollection.getTitle());
		activity.setProgressBarIndeterminateVisibility(false);

	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}