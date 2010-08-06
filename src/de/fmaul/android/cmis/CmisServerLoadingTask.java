package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import android.app.Activity;
import android.app.Application;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.Server;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.ListUtils;

public class CmisServerLoadingTask extends AsyncTask<String, Void, CmisRepository> {

	private final ListCmisFeedActivity activity;
	private ProgressDialog pg;
	private Prefs prefs;
	private Application app;

	public CmisServerLoadingTask(ListCmisFeedActivity activity, Application app, final Prefs prefs) {
		super();
		this.activity = activity;
		this.app = app;
		this.prefs = prefs;
		
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected CmisRepository doInBackground(String... params) {
		try {
			return CmisRepository.create(app, prefs);
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CmisRepository repo) {
		activity.setRepository(repo);
		activity.getRepository().clearCache(repo.getRepositoryWorkspace());
		activity.processSearchOrDisplayIntent();
	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}