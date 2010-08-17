package de.fmaul.android.cmis.utils;

import java.io.File;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import de.fmaul.android.cmis.HomeActivity;
import de.fmaul.android.cmis.R;

public class NotificationUtils {
	
	public static final int DOWNLOAD_ID = 3313;
	
	public static void downloadNotification(Activity contextActivity, File contentFile, String mimetype){
		
		NotificationManager notificationManager = (NotificationManager) contextActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.cmisexplorer, "Download finished", System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(contentFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimetype.toLowerCase());
		
		/*Intent i = new Intent(contextActivity, OpenFileActivity.class);
		i.putExtra("path", contentFile.getPath());
		i.putExtra("mimeType", item.getMimeType());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
		
		PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, viewIntent, 0);
		String titreNotification = "Download file finished.";
		String texteNotification = "Open file " + contentFile.getName();
		notification.setLatestEventInfo(contextActivity, titreNotification, texteNotification, pendingIntent);
		notificationManager.notify(DOWNLOAD_ID, notification);
	}
	
	public static void downloadNotification(Activity contextActivity){
		
		NotificationManager notificationManager = (NotificationManager) contextActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.cmisexplorer, "Downloading..", System.currentTimeMillis());
		Intent viewIntent = new Intent(contextActivity, HomeActivity.class);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, viewIntent, 0);
		String titreNotification = "Downloading files.";
		String texteNotification = "Open Cmis Browser";
		notification.setLatestEventInfo(contextActivity, titreNotification, texteNotification, pendingIntent);
		notificationManager.notify(DOWNLOAD_ID, notification);
	}
	
	public static void cancelDownloadNotification(Activity contextActivity){
		NotificationManager notificationManager = (NotificationManager) contextActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(DOWNLOAD_ID);
	}
	
}