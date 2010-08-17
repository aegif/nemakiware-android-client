package de.fmaul.android.cmis.asynctask;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.NotificationUtils;

public abstract class AbstractDownloadTask extends AsyncTask<CmisItemLazy, Void, File> {

	private final CmisRepository repository;
	private final Activity activity;
	private ProgressDialog progressDialog;
	private Boolean isDownload;

	public AbstractDownloadTask(CmisRepository repository, Activity activity) {
		 this(repository, activity, false);
	}
	
	public AbstractDownloadTask(CmisRepository repository, Activity activity, Boolean isDownload) {
		this.repository = repository;
		this.activity = activity;
		this.isDownload = isDownload;
	}
	

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (isDownload == false){
			progressDialog = ProgressDialog.show(
					activity, 
					this.activity.getText(R.string.download), 
					this.activity.getText(R.string.download_progress),
					true, 
					true, 
					new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							AbstractDownloadTask.this.cancel(true);
							NotificationUtils.cancelDownloadNotification(activity);
							dialog.dismiss();
						}
					});
		} else {
			ActionUtils.displayError(activity, "Downloading in progress...");
		}
	}

	@Override
	protected File doInBackground(CmisItemLazy... params) {
		CmisItemLazy item = params[0];
		if (item != null) {
			try {
				if (isDownload){
					return repository.retreiveContent(item, ((CmisApp) activity.getApplication()).getPrefs().getDownloadFolder());
				} else {
					return repository.retreiveContent(item);
				}
			} catch (Exception e) {
				ActionUtils.displayError(activity, R.string.generic_error);
				return null;
			}
		}
		return null;
	}

	protected void onPostExecute(File result) {
		if (progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		onDownloadFinished(result);
	}

	@Override
	protected void onCancelled() {
		if (progressDialog.isShowing()){
			progressDialog.dismiss();
		}
	}
	

	public abstract void onDownloadFinished(File result);

}
