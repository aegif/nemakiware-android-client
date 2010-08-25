package de.fmaul.android.cmis.asynctask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import de.fmaul.android.cmis.FileChooserActivity;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FileSystemUtils;
import de.fmaul.android.cmis.utils.StorageException;

public class CopyFilesTask extends AsyncTask<String, Integer, Boolean> {

	private final Activity activity;
	private ProgressDialog progressDialog;
	private Application app;
	
	private static final int MAX_BUFFER_SIZE = 1024;
	
    public static final String STATUSES[] = {"Downloading",
    "Paused", "Complete", "Cancelled", "Error"};
    
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private int status;
    private int downloaded;
	private float sizeTotal = 0f;
	private File folder;
	private List<File> listingFiles;
	private List<File> moveFiles;

	public CopyFilesTask(Activity activity, final List<File> listingFiles, final List<File> moveFiles, final File folder) {
		super();
		this.activity = activity;
		this.folder = folder;
		this.listingFiles = listingFiles;
		this.moveFiles = moveFiles;
	}

	@Override
	protected void onPreExecute() {
		status = DOWNLOADING;
		downloaded = 0;
		
		progressDialog = new ProgressDialog(activity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		//progressDialog.setMessage(this.activity.getText(R.string.copy));
		progressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						CopyFilesTask.this.cancel(true);
						status = CANCELLED;
						dialog.dismiss();
					}
				});
		progressDialog.setCancelable(true);
		progressDialog.setTitle(this.activity.getText(R.string.copy));
		//progressDialog.setMessage(this.activity.getText(R.string.copy));
		progressDialog.setProgress(0);
		progressDialog.setMax(100);
		progressDialog.show();
		
		for (File file : listingFiles) {
			sizeTotal += file.length();
		}
		
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			
			for (File file : listingFiles) {
				copyFile(file);
			}
			
			for (File fileToMove : moveFiles) {
				FileSystemUtils.rename(folder, fileToMove);
			}
			
			return true;
			
		} catch (Exception fle) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean statut) {
		try {
			if (statut){
				listingFiles.clear();
				moveFiles.clear();
				activity.findViewById(R.id.paste).setVisibility(View.GONE);
				activity.findViewById(R.id.clear).setVisibility(View.GONE);
				
				progressDialog.dismiss();
				ActionUtils.displayMessage(activity, R.string.action_paste_success);
				((FileChooserActivity) activity).initialize(folder.getName(), folder);
			} else {
				ActionUtils.displayMessage(activity, R.string.generic_error);
				progressDialog.dismiss();
			}
		} catch (Exception e) {
			ActionUtils.displayMessage(activity, R.string.generic_error);
			activity.finish();
			progressDialog.dismiss();
		}
	}

	@Override
	protected void onCancelled() {
		activity.finish();
		progressDialog.dismiss();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		int percent = Math.round((float) values[0] / sizeTotal * 100);
		progressDialog.setProgress(percent);
	}
	
	private void copyFile(File src) throws StorageException {
		OutputStream os = null;
		InputStream in = null;
		int size = (int) src.length();
		
		try {
			in = new FileInputStream(src);
			os = new FileOutputStream(createUniqueCopyName(src.getName()));

			byte[] buffer = new byte[MAX_BUFFER_SIZE];
			
			 while (status == DOWNLOADING) {
                if (size - downloaded < MAX_BUFFER_SIZE) {
                	buffer = new byte[size - downloaded];
                }
                
                int read = in.read(buffer);
                if (read == -1 || size == downloaded)
                    break;
                
                os.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }
			 
			if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
			
		} catch (Exception e) {
            
        } finally {
            if (os != null) {
                try {
                	os.close();
                } catch (Exception e) {}
            }
            if (in != null) {
                try {
                	in.close();
                } catch (Exception e) {}
            }
        }
	}
	
	private File createUniqueCopyName(String fileName) {
		File file = new File(folder, fileName);
		
		if (!file.exists()) {
			return file;
		}
		
		file = new File(folder, activity.getString(R.string.copied_file_name) + fileName);
		
		if (!file.exists()) {
			return file;
		}
		
		int copyIndex = 2;
		
		while (copyIndex < 500) {
			file = new File(folder, activity.getString(R.string.copied_file_name) + copyIndex + "_" + fileName);
			if (!file.exists()) {
				return file;
			}
			copyIndex++;
		}
		return null;
	}
	
	
	private void stateChanged() {
		publishProgress(downloaded);
    }
}