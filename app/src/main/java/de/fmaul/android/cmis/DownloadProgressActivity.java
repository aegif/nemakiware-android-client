/*
 * Copyright (C) 2010 Jean Marie PASCAL
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
package de.fmaul.android.cmis;

import java.util.List;

import android.app.ListActivity;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import de.fmaul.android.cmis.asynctask.AbstractDownloadTask;
import de.fmaul.android.cmis.repo.DownloadItem;
import de.fmaul.android.cmis.utils.ActionUtils;

public class DownloadProgressActivity extends ListActivity {

	private DownloadAdapter downloadAdapter;
	private DownloadItem downloadItem;
	private Handler handler;
	private Runnable runnable;
	private List<DownloadItem> downloadedFiles;
	private int finishTaks;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.server);
		setTitle(R.string.menu_item_download_manager);

		createDownloadProgress();
		
		handler = new Handler();
		runnable = new Runnable() {
		 
		public void run() {
		    finishTaks = 0;
			for (DownloadItem downloadedFile : downloadedFiles) {
				if (Status.FINISHED.equals(downloadedFile.getTask().getStatus())){
					finishTaks++;
				}
			}
			if (finishTaks == downloadedFiles.size()){
				handler.removeCallbacks(runnable);
			} else {
				handler.postDelayed(this, 1000);
			}
			createDownloadProgress();
		 }
		};
		
		registerForContextMenu(getListView());
		runnable.run();
	}
	
	public void createDownloadProgress(){
		downloadedFiles = ((CmisApp) getApplication()).getDownloadedFiles();

		downloadAdapter = new DownloadAdapter(this, R.layout.download_list_row, downloadedFiles);
		setListAdapter(downloadAdapter);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		downloadItem = (DownloadItem) getListView().getItemAtPosition(info.position);
		
		menu.setHeaderTitle(downloadItem.getItem().getTitle());
		
		if (isFinish(downloadItem)){ 
			menu.add(0, 2, Menu.NONE, getString(R.string.open));
			menu.add(0, 3, Menu.NONE, getString(R.string.delete_list));
		} else {
			if (isCancellable(downloadItem)){
				menu.add(0, 1, Menu.NONE, getString(R.string.cancel_download));
			} else {
				menu.add(0, 3, Menu.NONE, getString(R.string.delete_list));	
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		downloadItem = (DownloadItem) getListView().getItemAtPosition(menuInfo.position);

		switch (menuItem.getItemId()) {
		case 1:
			downloadItem.getTask().setState(AbstractDownloadTask.CANCELLED);
			return true;
		case 2:
			ActionUtils.openDocument(DownloadProgressActivity.this, downloadItem.getItem());
			return true;
		case 3:
			downloadedFiles.remove(menuInfo.position);
			createDownloadProgress();
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		downloadItem = (DownloadItem) getListView().getItemAtPosition(position);
		
		if (isFinish(downloadItem)){
			ActionUtils.openDocument(DownloadProgressActivity.this, downloadItem.getItem());
		}
		
	}
	
	private boolean isFinish(DownloadItem downloadItem){
		if (Status.FINISHED.equals(downloadItem.getTask().getStatus()) && downloadItem.getTask().getPercent() == 100){
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isCancellable(DownloadItem downloadItem){
		if (Status.RUNNING.equals(downloadItem.getTask().getStatus()) && downloadItem.getTask().getPercent() != 100){
			return true;
		} else {
			return false;
		}
	}
	
	
}
