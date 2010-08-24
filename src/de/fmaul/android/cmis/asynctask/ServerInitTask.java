package de.fmaul.android.cmis.asynctask;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.ListCmisFeedActivity;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.StorageException;

public class ServerInitTask extends AsyncTask<String, Void, CmisRepository> {

	private final ListCmisFeedActivity activity;
	private ProgressDialog pg;
	private Server server;
	private Application app;

	public ServerInitTask(ListCmisFeedActivity activity, Application app, final Server server) {
		super();
		this.activity = activity;
		this.app = app;
		this.server = server;
		
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true, true, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ServerInitTask.this.cancel(true);
				activity.finish();
				dialog.dismiss();
			}
		});
	}

	@Override
	protected CmisRepository doInBackground(String... params) {
		try {
			return CmisRepository.create(app, server);
		} catch (FeedLoadException fle) {
			return null;
		} catch (Exception fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CmisRepository repo) {
		try {
			repo.generateParams(activity);
			((CmisApp) activity.getApplication()).setRepository(repo);
			activity.setItem(repo.getRootItem());
			repo.clearCache(repo.getServer().getWorkspace());
			new FeedDisplayTask(activity, repo, getTitleFromIntent()).execute(getFeedFromIntent());
			pg.dismiss();
		} catch (StorageException e) {
			ActionUtils.displayError(activity, R.string.generic_error);
			pg.dismiss();
		} catch (Exception e) {
			ActionUtils.displayError(activity, R.string.generic_error);
			activity.finish();
			pg.dismiss();
		}
	}

	@Override
	protected void onCancelled() {
		activity.finish();
		pg.dismiss();
	}
	
	private String getFeedFromIntent() {
		Bundle extras = activity.getIntent().getExtras();
		if (extras != null) {
			if (extras.get("feed") != null) {
				return extras.get("feed").toString();
			}
		}
		return null;
	}
	
	private String getTitleFromIntent() {
		Bundle extras = activity.getIntent().getExtras();
		if (extras != null) {
			if (extras.get("title") != null) {
				return extras.get("title").toString();
			}
		}
		return null;
	}
}