package de.fmaul.android.cmis.utils;

import java.io.File;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import de.fmaul.android.cmis.DownloadProgressActivity;
import de.fmaul.android.cmis.HomeActivity;
import de.fmaul.android.cmis.R;

public class NotificationUtils {
	
	public static final int DOWNLOAD_ID = 3313;
	
	public static void downloadNotification(Activity activity, File contentFile, String mimetype){
		
		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.cmisexplorer, activity.getText(R.string.notif_download_finish), System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(contentFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimetype.toLowerCase());
		
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, viewIntent, 0);
		String titreNotification = activity.getText(R.string.notif_download_title).toString();
		String texteNotification = activity.getText(R.string.notif_download_texte) + contentFile.getName();
		notification.setLatestEventInfo(activity, titreNotification, texteNotification, pendingIntent);
		notificationManager.notify(DOWNLOAD_ID, notification);
	}
	
	public static void downloadNotification(Activity activity){
		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.cmisexplorer, activity.getText(R.string.download_progress), System.currentTimeMillis());
		Intent viewIntent = new Intent(activity, DownloadProgressActivity.class);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, viewIntent, 0);
		String titreNotification =  activity.getText(R.string.download_progress).toString();
		String texteNotification =  activity.getText(R.string.notif_open).toString();
		notification.setLatestEventInfo(activity, titreNotification, texteNotification, pendingIntent);
		notificationManager.notify(DOWNLOAD_ID, notification);
	}
	
	public static void cancelDownloadNotification(Activity contextActivity){
		NotificationManager notificationManager = (NotificationManager) contextActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(DOWNLOAD_ID);
	}
	
	public static void updateDownloadNotification(Activity activity, String message){
		
		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.cmisexplorer, activity.getText(R.string.download_progress), System.currentTimeMillis());
		Intent viewIntent = new Intent(activity, DownloadProgressActivity.class);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, viewIntent, 0);
		String titreNotification =  activity.getText(R.string.download_progress).toString();
		String texteNotification =  message;
		notification.setLatestEventInfo(activity, titreNotification, texteNotification, pendingIntent);
		notificationManager.notify(DOWNLOAD_ID, notification);
	}
	
}