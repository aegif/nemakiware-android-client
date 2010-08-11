package de.fmaul.android.cmis.asynctask;

import org.dom4j.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;

public class FeedItemDisplayTask extends AsyncTask<String, Void, CmisItem> {

	private final Activity activity;
	private ProgressDialog pg;
	private Server server;
	private String url;

	public FeedItemDisplayTask(Activity activity, final Server server, String url) {
		super();
		this.activity = activity;
		this.url = url;
		this.server = server;
		
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true);
	}

	@Override
	protected CmisItem doInBackground(String... params) {
		try {
			Document doc = FeedUtils.readAtomFeed(url, server.getUsername(), server.getPassword());
			return CmisItem.createFromFeed(doc.getRootElement());
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CmisItem item) {
		if (item != null){
			ActionUtils.displayDocumentDetails(activity, server, item);
		} else {
			ActionUtils.displayError(activity, "ERROR during favorite");
		}
		pg.dismiss();
	}

	@Override
	protected void onCancelled() {
		pg.dismiss();
	}
}