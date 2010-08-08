package de.fmaul.android.cmis.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import de.fmaul.android.cmis.AbstractDownloadTask;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisProperty;

public class ActionUtils {

	private static void viewFileInAssociatedApp(Activity activity, File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			activity.startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(activity, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
	}
	
	/*private void openDocument(Activity activity, String workspace, CmisItem item) {

		File content = item.getContent(workspace);
		if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent())){
			viewFileInAssociatedApp(acontent, item.getMimeType());
		} else {
			new AbstractDownloadTask(getRepository(), this) {
				@Override
				public void onDownloadFinished(File contentFile) {
					if (contentFile != null && contentFile.exists()) {
						viewFileInAssociatedApp(contentFile, item.getMimeType());
					} else {
						displayError(R.string.error_file_does_not_exists);
					}
				}
			}.execute(item);
		}
	}*/

	private void displayError(Activity activity, int messageId) {
		Toast.makeText(activity, messageId, Toast.LENGTH_SHORT).show();
	}
	
}
