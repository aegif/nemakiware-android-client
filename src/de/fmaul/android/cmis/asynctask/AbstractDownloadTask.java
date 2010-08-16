package de.fmaul.android.cmis.asynctask;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;

public abstract class AbstractDownloadTask extends AsyncTask<CmisItemLazy, Void, File> {

	private final CmisRepository repository;
	private final Activity activity;
	private ProgressDialog progressDialog;

	public AbstractDownloadTask(CmisRepository repository, Activity activity) {
		this.repository = repository;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(activity, this.activity.getText(R.string.download), this.activity.getText(R.string.download_progress),
				true);
	}

	@Override
	protected File doInBackground(CmisItemLazy... params) {
		CmisItemLazy item = params[0];
		if (item != null) {
			try {
				return repository.retreiveContent(item);
			} catch (Exception e) {
				ActionUtils.displayError(activity, R.string.generic_error);
				return null;
			}
		}
		return null;
	}

	protected void onPostExecute(File result) {
		progressDialog.dismiss();
		onDownloadFinished(result);
	}

	@Override
	protected void onCancelled() {
		progressDialog.cancel();
	}

	public abstract void onDownloadFinished(File result);

}
