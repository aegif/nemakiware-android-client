package jp.aegif.android.cmis.asynctask;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import jp.aegif.android.cmis.CmisApp;
import jp.aegif.android.cmis.R;
import jp.aegif.android.cmis.SearchActivity;
import jp.aegif.android.cmis.model.Server;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.utils.ActionUtils;
import jp.aegif.android.cmis.utils.FeedLoadException;
import jp.aegif.android.cmis.utils.StorageException;

public class ServerSearchInitTask extends AsyncTask<String, Void, CmisRepository> {

	private final Activity currentActivity;
	private ProgressDialog pg;
	private Server server;
	private Application app;
	private Intent intent;

	public ServerSearchInitTask(Activity activity, Application app, final Server server, Intent intent) {
		super();
		this.currentActivity = activity;
		this.app = app;
		this.server = server;
		this.intent = intent;
		
	}


	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(currentActivity, "", currentActivity.getText(R.string.loading), true, true, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ServerSearchInitTask.this.cancel(true);
				currentActivity.finish();
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
			repo.generateParams(currentActivity);
			((CmisApp) currentActivity.getApplication()).setRepository(repo);
			repo.clearCache(repo.getServer().getWorkspace());
			Intent i = new Intent(currentActivity, SearchActivity.class);
			i.putExtras(intent);
			currentActivity.startActivity(i);
			currentActivity.finish();
			pg.dismiss();
		} catch (StorageException e) {
			ActionUtils.displayMessage(currentActivity, R.string.generic_error);
			pg.dismiss();
		} catch (Exception e) {
			ActionUtils.displayMessage(currentActivity, R.string.generic_error);
			currentActivity.finish();
			pg.dismiss();
		}
	}

	@Override
	protected void onCancelled() {
		currentActivity.finish();
		pg.dismiss();
	}
	
	private String getFeedFromIntent() {
		Bundle extras = currentActivity.getIntent().getExtras();
		if (extras != null) {
			if (extras.get("feed") != null) {
				return extras.get("feed").toString();
			}
		}
		return null;
	}
	
	private String getTitleFromIntent() {
		Bundle extras = currentActivity.getIntent().getExtras();
		if (extras != null) {
			if (extras.get("title") != null) {
				return extras.get("title").toString();
			}
		}
		return null;
	}
}