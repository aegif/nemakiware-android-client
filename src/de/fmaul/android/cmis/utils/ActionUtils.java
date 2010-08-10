package de.fmaul.android.cmis.utils;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.asynctask.AbstractDownloadTask;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisRepository;

public class ActionUtils {

	public static void openDocument(final Activity contextActivity, final CmisItem item) {

		File content = item.getContent(contextActivity.getIntent().getStringExtra("workspace"));
		if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent(contextActivity))){
			viewFileInAssociatedApp(contextActivity, content, item.getMimeType());
		} else {
			new AbstractDownloadTask(getRepository(contextActivity), contextActivity) {
				@Override
				public void onDownloadFinished(File contentFile) {
					if (contentFile != null && contentFile.exists()) {
						viewFileInAssociatedApp(contextActivity, contentFile, item.getMimeType());
					} else {
						displayError(contextActivity, R.string.error_file_does_not_exists);
					}
				}
			}.execute(item);
		}
	}
	
	
	public static void displayError(Activity contextActivity, int messageId) {
		Toast.makeText(contextActivity, messageId, Toast.LENGTH_SHORT).show();
	}
	
	private static void viewFileInAssociatedApp(Activity contextActivity, File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			contextActivity.startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(contextActivity, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void shareDocument(final Activity contextActivity, final String workspace, final CmisItem item) {
		File content = item.getContent(workspace);
		if (item.getMimeType().length() == 0){
			shareFileInAssociatedApp(contextActivity, content, item);
		} else if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent(contextActivity))) {
			shareFileInAssociatedApp(contextActivity, content, item);
		} else {
			new AbstractDownloadTask(getRepository(contextActivity), contextActivity) {
				@Override
				public void onDownloadFinished(File contentFile) {
						shareFileInAssociatedApp(contextActivity, contentFile, item);
				}
			}.execute(item);
		}
	}
	
	private static void shareFileInAssociatedApp(Activity contextActivity, File contentFile, CmisItem item) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
		if (contentFile != null && contentFile.exists()){
			i.putExtra(Intent.EXTRA_TEXT, item.getContentUrl());
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
			i.setType(item.getMimeType());
		} else {
			i.putExtra(Intent.EXTRA_TEXT, item.getSelfUrl());
			i.setType("text/plain");
		}
		contextActivity.startActivity(Intent.createChooser(i, "Share..."));
	}
	
	private static CmisRepository getRepository(Activity activity) {
		return ((CmisApp) activity.getApplication()).getRepository();
	}
	
	private static String getContentFromIntent(Activity activity) {
		return activity.getIntent().getStringExtra("contentStream");
	}
}
