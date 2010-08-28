/*
 * Copyright (C) 2010 Florian Maul & Jean Marie PASCAL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fmaul.android.cmis.utils;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.widget.Toast;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.DocumentDetailsActivity;
import de.fmaul.android.cmis.ListCmisFeedActivity;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.asynctask.AbstractDownloadTask;
import de.fmaul.android.cmis.asynctask.ItemPropertiesDisplayTask;
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.FavoriteDAO;
import de.fmaul.android.cmis.database.SearchDAO;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisPropertyFilter;
import de.fmaul.android.cmis.repo.CmisRepository;

public class ActionUtils {

	public static void openDocument(final Activity contextActivity, final CmisItemLazy item) {
		try {
			File content = getItemFile(contextActivity,  contextActivity.getIntent().getStringExtra("workspace"), item);
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())){
				viewFileInAssociatedApp(contextActivity, content, item.getMimeType());
			} else {
				confirmDownload(contextActivity, item, true);
			}
		} catch (Exception e) {
			displayMessage(contextActivity, e.getMessage());
		}
	}
	
	public static void openWithDocument(final Activity contextActivity, final CmisItemLazy item) {
		try {
			File content = getItemFile(contextActivity,  contextActivity.getIntent().getStringExtra("workspace"), item);
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())){
				openWith(contextActivity, content);
			} else {
				confirmDownload(contextActivity, item, false);
			}
		} catch (Exception e) {
			displayMessage(contextActivity, e.getMessage());
		}
	}
	
	
	private static void openWith(final Activity contextActivity, final File tempFile){
		CharSequence[] cs = MimetypeUtils.getOpenWithRowsLabel(contextActivity); 
		AlertDialog.Builder builder = new AlertDialog.Builder(contextActivity);
		builder.setTitle(R.string.open_with_title);
		builder.setSingleChoiceItems(cs, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				 viewFileInAssociatedApp(contextActivity, tempFile, MimetypeUtils.getDefaultMimeType().get(which));
				 dialog.dismiss();
			}
		});
		builder.setNegativeButton(contextActivity.getText(R.string.cancel), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.cancel();
	           }
	       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	private static void startDownload(final Activity contextActivity, final CmisItemLazy item, final boolean openAutomatic){
		new AbstractDownloadTask(getRepository(contextActivity), contextActivity) {
			@Override
			public void onDownloadFinished(File contentFile) {
				if (contentFile != null && contentFile.exists()) {
					if (openAutomatic){
						viewFileInAssociatedApp(contextActivity, contentFile, item.getMimeType());
					} else {
						openWith(contextActivity, contentFile);
					}
				} else {
					displayMessage(contextActivity, R.string.error_file_does_not_exists);
				}
			}
		}.execute(item);
	}
	
	
	private static void confirmDownloadBackground(final Activity contextActivity, final CmisItemLazy item) {
		if (getPrefs(contextActivity).isConfirmDownload() && Integer.parseInt(item.getSize()) > convertSizeToKb(getPrefs(contextActivity).getDownloadFileSize())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(contextActivity);
			builder.setMessage(
					contextActivity.getText(R.string.confirm_donwload) + " " + 
					convertAndFormatSize(contextActivity, item.getSize()) + " " + 
					contextActivity.getText(R.string.confirm_donwload2)
					)
			       .setCancelable(false)
			       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   NotificationUtils.downloadNotification(contextActivity);
			        	   startDownloadBackground(contextActivity, item);
			           }
			       })
			       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			startDownloadBackground(contextActivity, item);
		}
	}
	
	private static void startDownloadBackground(final Activity contextActivity, final CmisItemLazy item){
		new AbstractDownloadTask(getRepository(contextActivity), contextActivity, true) {
			@Override
			public void onDownloadFinished(File contentFile) {
				if (contentFile != null && contentFile.exists()) {
					NotificationUtils.downloadNotification(contextActivity, contentFile, item.getMimeType());	
				} else {
					NotificationUtils.cancelDownloadNotification(contextActivity);	
					//displayMessage(contextActivity, R.string.error_file_does_not_exists);
				}
			}
		}.execute(item);
	}
	
	private static void confirmDownload(final Activity contextActivity, final CmisItemLazy item, final boolean notification) {
		if (getPrefs(contextActivity).isConfirmDownload() && Integer.parseInt(item.getSize()) > convertSizeToKb(getPrefs(contextActivity).getDownloadFileSize())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(contextActivity);
			builder.setMessage(
					contextActivity.getText(R.string.confirm_donwload) + " " + 
					convertAndFormatSize(contextActivity, item.getSize()) + " " + 
					contextActivity.getText(R.string.confirm_donwload2)
					)
			       .setCancelable(false)
			       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   startDownload(contextActivity, item, true);
			           }
			       })
			       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			startDownload(contextActivity, item, true);
		}
	}
	
	
	public static void displayMessage(Activity contextActivity, int messageId) {
		Toast.makeText(contextActivity, messageId, Toast.LENGTH_LONG).show();
	}
	
	public static void displayMessage(Activity contextActivity, String messageId) {
		Toast.makeText(contextActivity, messageId, Toast.LENGTH_LONG).show();
	}
	
	public static void viewFileInAssociatedApp(final Activity contextActivity, final File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		//viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			contextActivity.startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			//Toast.makeText(contextActivity, R.string.application_not_available, Toast.LENGTH_SHORT).show();
			openWith(contextActivity, tempFile);
		}
	}
	
	public static void saveAs(final Activity contextActivity, final String workspace, final CmisItemLazy item){
		try {
			File content = item.getContentDownload(contextActivity.getApplication(), ((CmisApp) contextActivity.getApplication()).getPrefs().getDownloadFolder());
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())){
				viewFileInAssociatedApp(contextActivity, content, item.getMimeType());
			} else {
				File cacheContent = item.getContent(contextActivity.getApplication(), workspace);
				if (cacheContent != null && cacheContent.length() > 0 && cacheContent.length() == Long.parseLong(item.getSize())){
					//TODO AsyncTask
					ProgressDialog pg = ProgressDialog.show(contextActivity, "", contextActivity.getText(R.string.loading), true, true);
					StorageUtils.copy(cacheContent, content);
					pg.dismiss();
					viewFileInAssociatedApp(contextActivity, cacheContent, item.getMimeType());
				} else {
					confirmDownloadBackground(contextActivity, item);
				}
			}
		} catch (Exception e) {
			displayMessage(contextActivity, e.getMessage());
		}
	}
	
	public static void shareDocument(final Activity activity, final String workspace, final CmisItemLazy item) {
		
		try {
			File content = getItemFile(activity, workspace, item);
			if (item.getMimeType().length() == 0){
				shareFileInAssociatedApp(activity, content, item);
			//} else if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())) {
			//	shareFileInAssociatedApp(contextActivity, content, item);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(activity.getText(R.string.share_question)).setCancelable(true)
						.setPositiveButton(activity.getText(R.string.share_link), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								shareFileInAssociatedApp(activity, null, item);
							}
						}).setNegativeButton(activity.getText(R.string.share_content), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									File content = getItemFile(activity, workspace, item);
									if (content != null) {
										shareFileInAssociatedApp(activity, content, item);
									} else {
										new AbstractDownloadTask(getRepository(activity), activity) {
											@Override
											public void onDownloadFinished(File contentFile) {
												shareFileInAssociatedApp(activity, contentFile, item);
											}
										}.execute(item);
									}
								} catch (StorageException e) {
									displayMessage(activity, R.string.generic_error);
								}
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} catch (Exception e) {
			displayMessage(activity, R.string.generic_error);
		}
		
	}
	
	private static void shareFileInAssociatedApp(Activity contextActivity, File contentFile, CmisItemLazy item) {
		shareFileInAssociatedApp(contextActivity, contentFile, item, item.getMimeType());
	}
	
	private static void shareFileInAssociatedApp(Activity contextActivity, File contentFile, CmisItemLazy item, String mimetype) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
		if (contentFile != null && contentFile.exists()){
			i.putExtra(Intent.EXTRA_TEXT, item.getContentUrl());
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
			i.setType(mimetype);
		} else {
			i.putExtra(Intent.EXTRA_TEXT, item.getSelfUrl());
			i.setType("text/plain");
		}
		contextActivity.startActivity(Intent.createChooser(i, contextActivity.getText(R.string.share)));
	}
	
	private static File getItemFile(final Activity contextActivity, final String workspace, final CmisItemLazy item) throws StorageException{
			File content = item.getContent(contextActivity.getApplication(), workspace);
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())) {
				return content; 
			} 
			
			content = item.getContentDownload(contextActivity.getApplication(), ((CmisApp) contextActivity.getApplication()).getPrefs().getDownloadFolder());
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())) {
				return content; 
			} 
			
			return null;
	}
	
	public static void createFavorite(Activity activity, Server server, CmisItemLazy item){
		try {
			Database database = Database.create(activity);
			FavoriteDAO favDao = new FavoriteDAO(database.open());
			long result = 1L;
			
			if (favDao.isPresentByURL(item.getSelfUrl()) == false){
				
				String mimetype = "";
				if (item.getMimeType() == null || item.getMimeType().length() == 0){
					mimetype = item.getBaseType();
				} else {
					mimetype = item.getMimeType();
				}
				
				result = favDao.insert(item.getTitle(), item.getSelfUrl(), server.getId(), mimetype);
				if (result == -1){
					Toast.makeText(activity, R.string.favorite_create_error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(activity, R.string.favorite_create, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(activity, R.string.favorite_present, Toast.LENGTH_LONG).show();
			}
			
			database.close();
			
		} catch (Exception e) {
			displayMessage(activity, R.string.generic_error);
		}
	}
	
	public static void createSaveSearch(Activity activity, Server server, String name, String url){
		try {
			Database database = Database.create(activity);
			SearchDAO searchDao = new SearchDAO(database.open());
			long result = 1L;
			
			if (searchDao.isPresentByURL(url) == false){
				result = searchDao.insert(name, url, server.getId());
				if (result == -1){
					Toast.makeText(activity, R.string.saved_search_create_error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(activity, R.string.saved_search_create, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(activity, R.string.saved_search_present, Toast.LENGTH_LONG).show();
			}
			
			database.close();
			
		} catch (Exception e) {
			displayMessage(activity, R.string.generic_error);
		}
	}
	
	public static void displayDocumentDetails(Activity activity, CmisItem doc) {
		displayDocumentDetails(activity, getRepository(activity).getServer(), doc);
	}
	
	public static void displayDocumentDetails(Activity activity, Server server, CmisItem doc) {
		try {
			((CmisApp) activity.getApplication()).setCmisPropertyFilter(null);
			Intent intent =getDocumentDetailsIntent(activity, server,  doc);
			activity.startActivity(intent);
		} catch (Exception e) {
			displayMessage(activity, R.string.generic_error);
		}
	}
	
	public static Intent getDocumentDetailsIntent(Activity activity, Server server, CmisItem doc) {
		try {
			Intent intent = new Intent(activity, DocumentDetailsActivity.class);
	
			ArrayList<CmisProperty> propList = new ArrayList<CmisProperty>(doc.getProperties().values());
			
			intent.putParcelableArrayListExtra("properties", propList);
			
			intent.putExtra("workspace", server.getWorkspace());
			intent.putExtra("objectTypeId", doc.getProperties().get("cmis:objectTypeId").getValue());
			intent.putExtra("baseTypeId", doc.getProperties().get("cmis:baseTypeId").getValue());
			intent.putExtra("item", new CmisItemLazy(doc));
			
			return intent;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	private static int convertSizeToKb(String size){
		return Integer.parseInt(size) * 1024;
	}
	
	public static String convertAndFormatSize(Activity activity, String size) {
		int sizeInByte = Integer.parseInt(size);
	    return convertAndFormatSize(activity, sizeInByte);
	}
	
	
	public static String convertAndFormatSize(Activity activity, int sizeInByte) {
		if (sizeInByte < 1024) {
			return String.valueOf(sizeInByte) + " " + activity.getText(R.string.file_size_bytes);
		} else {
			int sizeInKB = sizeInByte / 1024;
			if (sizeInKB < 1024) {
				return String.valueOf(sizeInKB) + " " + activity.getText(R.string.file_size_kilobytes);
			} else {
				int sizeInMB = sizeInKB / 1024;
				if (sizeInMB < 1024) {
					return String.valueOf(sizeInMB) + " " + activity.getText(R.string.file_size_megabytes);
				} else {
					return String.valueOf(sizeInMB / 1024) + " " + activity.getText(R.string.file_size_gigabytes);
				}
			}
		}
	}
	
	public static void openNewListViewActivity(Activity activity, CmisItem item) {
		openNewListViewActivity(activity, new CmisItemLazy(item));
	}
	
	public static void openNewListViewActivity(Activity activity, CmisItemLazy item) {
		Intent intent = new Intent(activity, ListCmisFeedActivity.class);
		if (item != null){
			intent.putExtra("item", item);
		} else {
			intent.putExtra("title", getRepository(activity).getServer().getName());
		}
		
		activity.startActivity(intent);
	}
	
	
	public static void openDocument(final Activity contextActivity, final File content) {
		try {
			if (content != null && content.length() > 0 ){
				viewFileInAssociatedApp(contextActivity, content, MimetypeUtils.getMimetype(contextActivity, content));
			}
		} catch (Exception e) {
			displayMessage(contextActivity, e.getMessage());
		}
	}
	
	public static void openWithDocument(final Activity contextActivity, final File content) {
		try {
			if (content != null && content.length() > 0){
				openWith(contextActivity, content);
			}
		} catch (Exception e) {
			displayMessage(contextActivity, e.getMessage());
		}
	}
	
	public static void shareFileInAssociatedApp(Activity contextActivity, File contentFile) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
		i.setType(MimetypeUtils.getMimetype(contextActivity, contentFile));
		contextActivity.startActivity(Intent.createChooser(i, contextActivity.getText(R.string.share)));
	}
	
	
	private static CmisRepository getRepository(Activity activity) {
		return ((CmisApp) activity.getApplication()).getRepository();
	}
	
	private static Prefs getPrefs(Activity activity) {
		return ((CmisApp) activity.getApplication()).getPrefs();
	}

}
