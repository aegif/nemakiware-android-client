package de.fmaul.android.cmis.asynctask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.DownloadItem;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.HttpUtils;
import de.fmaul.android.cmis.utils.NotificationUtils;
import de.fmaul.android.cmis.utils.StorageException;
import de.fmaul.android.cmis.utils.StorageUtils;

public abstract class AbstractDownloadTask extends AsyncTask<CmisItemLazy, Integer, File> {

	private final CmisRepository repository;
	private final Activity activity;
	private ProgressDialog progressDialog;
	private Boolean isDownload;
	private CmisItemLazy item;
	
	private static final int MAX_BUFFER_SIZE = 1024;
	private static final int NB_NOTIF = 10;
	
	public static final int DOWNLOADING = 0;
    private static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    private static final int ERROR = 4;
    
    public int state;
	private int downloaded;
	private int size;
	private int notifCount = 0;
	private int percent;
	
	

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
			
			state = DOWNLOADING;
			downloaded = 0;
			
			progressDialog = new ProgressDialog(activity);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage(this.activity.getText(R.string.download));
			progressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							AbstractDownloadTask.this.cancel(true);
							state = CANCELLED;
							//NotificationUtils.cancelDownloadNotification(activity);
							dialog.dismiss();
						}
					});
			progressDialog.setCancelable(true);
			progressDialog.setTitle(this.activity.getText(R.string.download));
			progressDialog.setMessage(this.activity.getText(R.string.download_progress));
			progressDialog.setProgress(0);
			progressDialog.setMax(100);
			progressDialog.show();
			
		} else {
			ActionUtils.displayMessage(activity, R.string.download_progress);
		}
	}

	@Override
	protected File doInBackground(CmisItemLazy... params) {
		item = params[0];
		
		List<DownloadItem> dl = ((CmisApp) activity.getApplication()).getDownloadedFiles();
		if (dl == null) {
			dl = new ArrayList<DownloadItem>();
		}
		dl.add(new DownloadItem(item, this));
		
		if (item != null) {
			//progressDialog.setMax(Integer.parseInt(item.getSize()));
			size = Integer.parseInt(item.getSize());
			try {
				if (isDownload){
					return retreiveContent(item, ((CmisApp) activity.getApplication()).getPrefs().getDownloadFolder());
				} else {
					return retreiveContent(item);
				}
			} catch (Exception e) {
				ActionUtils.displayMessage(activity, R.string.generic_error);
				return null;
			}
		}
		return null;
	}

	protected void onPostExecute(File result) {
		if (state == CANCELLED){
			result.delete();
			result = null;
		}
		
		if (progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		
		onDownloadFinished(result);
	}

	@Override
	protected void onCancelled() {
		state = CANCELLED;
		if (progressDialog.isShowing()){
			progressDialog.dismiss();
		}
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		percent = Math.round(((float) values[0] / Float.parseFloat(item.getSize())) * 100);
		if (isDownload == false){
			progressDialog.setProgress(percent);
		} else {
			if (notifCount == NB_NOTIF){
				String message = activity.getText(R.string.progress) + " : " + percent + " %";
				NotificationUtils.updateDownloadNotification(activity, message);
				notifCount = 0;
			} else {
				notifCount++;
			}
		}
	}
	

	public abstract void onDownloadFinished(File result);
	
	
	private File retreiveContent(CmisItemLazy item) throws StorageException {
		File contentFile = StorageUtils.getStorageFile(activity.getApplication(), repository.getServer().getWorkspace(), StorageUtils.TYPE_CONTENT, item.getId(), item.getTitle());
		return retreiveContent(item, contentFile);
	}
	
	private File retreiveContent(CmisItemLazy item, String downloadFolder) throws StorageException {
		File contentFile = item.getContentDownload(activity.getApplication(), downloadFolder);
		return retreiveContent(item, contentFile);
	}
	
	private File retreiveContent(CmisItemLazy item, File contentFile) throws StorageException {
		OutputStream os = null;
		InputStream in = null;
		
		try {
			contentFile.getParentFile().mkdirs();
			contentFile.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(contentFile));

			in = HttpUtils.getWebRessourceAsStream(item.getContentUrl(), repository.getServer().getUsername(), repository.getServer().getPassword());
			byte[] buffer = new byte[MAX_BUFFER_SIZE];
			
			 while (state == DOWNLOADING) {
                if (size - downloaded < MAX_BUFFER_SIZE) {
                	buffer = new byte[size - downloaded];
                }
                
                int read = in.read(buffer);
                if (read == -1)
                    break;
                
                os.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }
			 
			if (state == DOWNLOADING) {
                state = COMPLETE;
                stateChanged();
            }
			
			return contentFile;
		} catch (Exception e) {
            
        } finally {
            // Close file.
            if (os != null) {
                try {
                	os.close();
                } catch (Exception e) {}
            }
            
            // Close connection to server.
            if (in != null) {
                try {
                	in.close();
                } catch (Exception e) {}
            }
        }
		return null;
	}
	
	private void stateChanged() {
		publishProgress(downloaded);
    }

	public int getPercent() {
		return percent;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getState(){
		return state;
	}

	
}
