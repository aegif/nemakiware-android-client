package de.fmaul.android.cmis;

import java.io.File;

import android.app.Activity;
import android.os.AsyncTask;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisRepository;

public abstract class AbstractDownloadTask extends AsyncTask<CmisItem, Void, File> {

	private final CmisRepository repository;
	private final Activity activity;

	public AbstractDownloadTask(CmisRepository repository, Activity activity) {
		this.repository = repository;
		this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		activity.setProgressBarIndeterminateVisibility(true);
		
	}
	
	@Override
	protected File doInBackground(CmisItem... params) {
		CmisItem item = params[0];
		if (item != null){
			return repository.retreiveContent(item);
		}
		return null;
	}
	
	protected void onPostExecute(File result) {
		activity.setProgressBarIndeterminateVisibility(false);	
		onDownloadFinished(result);
	}
	
	public abstract void onDownloadFinished(File result);
	
}
