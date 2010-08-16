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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.DocumentDetailsActivity;
import de.fmaul.android.cmis.ListCmisFeedActivity;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.asynctask.AbstractDownloadTask;
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.FavoriteDAO;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;

public class ActionUtils {

	public static void openDocument(final Activity contextActivity, final CmisItemLazy item) {
		try {
			File content = item.getContent(contextActivity.getApplication(), contextActivity.getIntent().getStringExtra("workspace"));
			if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())){
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
		} catch (Exception e) {
			displayError(contextActivity, e.getMessage());
		}
	}
	
	
	/*public static void confirmDownload(final Activity contextActivity, final CmisItemLazy item) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(contextActivity);
		builder.setMessage("").setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

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
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
	}*/
	
	
	public static void displayError(Activity contextActivity, int messageId) {
		Toast.makeText(contextActivity, messageId, Toast.LENGTH_LONG).show();
	}
	
	public static void displayError(Activity contextActivity, String messageId) {
		Toast.makeText(contextActivity, messageId, Toast.LENGTH_LONG).show();
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
	
	public static void shareDocument(final Activity contextActivity, final String workspace, final CmisItemLazy item) {
		
		try {
			File content = item.getContent(contextActivity.getApplication(), workspace);
			if (item.getMimeType().length() == 0){
				shareFileInAssociatedApp(contextActivity, content, item);
			} else if (content != null && content.length() > 0 && content.length() == Long.parseLong(item.getSize())) {
				shareFileInAssociatedApp(contextActivity, content, item);
			} else {
				new AbstractDownloadTask(getRepository(contextActivity), contextActivity) {
					@Override
					public void onDownloadFinished(File contentFile) {
							shareFileInAssociatedApp(contextActivity, contentFile, item);
					}
				}.execute(item);
			}
		} catch (Exception e) {
			displayError(contextActivity, R.string.generic_error);
		}
		
	}
	
	private static void shareFileInAssociatedApp(Activity contextActivity, File contentFile, CmisItemLazy item) {
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
		contextActivity.startActivity(Intent.createChooser(i, contextActivity.getText(R.string.share)));
	}
	
	private static CmisRepository getRepository(Activity activity) {
		return ((CmisApp) activity.getApplication()).getRepository();
	}
	
	public static void createFavorite(Activity activity, Server server, CmisItemLazy item){
		try {
			Database database = Database.create(activity);
			FavoriteDAO favDao = new FavoriteDAO(database.open());
			long result = 1L;
			
			if (favDao.isPresentByURL(item.getSelfUrl()) == false){
				result = favDao.insert(item.getTitle(), item.getSelfUrl(), server.getId(), item.getMimeType());
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
			displayError(activity, R.string.generic_error);
		}
	}
	
	public static void displayDocumentDetails(Activity activity, CmisItem doc) {
		displayDocumentDetails(activity, getRepository(activity).getServer(), doc);
	}
	
	public static void displayDocumentDetails(Activity activity, Server server, CmisItem doc) {
		try {
			Intent intent = new Intent(activity, DocumentDetailsActivity.class);
	
			ArrayList<CmisProperty> propList = new ArrayList<CmisProperty>(doc.getProperties().values());
			
			intent.putParcelableArrayListExtra("properties", propList);
			
			intent.putExtra("workspace", server.getWorkspace());
			intent.putExtra("objectTypeId", doc.getProperties().get("cmis:objectTypeId").getValue());
			intent.putExtra("baseTypeId", doc.getProperties().get("cmis:baseTypeId").getValue());
			intent.putExtra("item", new CmisItemLazy(doc));
			
			activity.startActivity(intent);
		} catch (Exception e) {
			displayError(activity, R.string.generic_error);
		}
	}
	
	public static void openNewListViewActivity(Activity activity, CmisItem item) {
		openNewListViewActivity(activity, new CmisItemLazy(item));
	}
	
	public static void openNewListViewActivity(Activity activity, CmisItemLazy item) {
		Intent intent = new Intent(activity, ListCmisFeedActivity.class);
		intent.putExtra("item", item);
		activity.startActivity(intent);
	}
	
}
