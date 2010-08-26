/*
 * Copyright (C) 2010 Florian Maul
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
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.fmaul.android.cmis.asynctask.ItemPropertiesDisplayTask;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisPropertyFilter;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.IntentIntegrator;

public class DocumentDetailsActivity extends ListActivity {

	private CmisItemLazy item;
	private Button view, download, share, edit, delete, qrcode, filter, openwith;
	private Activity activity;
	private CmisPropertyFilter propertyFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		
		propertyFilter = (CmisPropertyFilter) getLastNonConfigurationInstance();
		
		activity = this;
		
		item = (CmisItemLazy) getIntent().getExtras().getSerializable("item");
		
		setTitleFromIntent();
		displayActionIcons();
		displayPropertiesFromIntent();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final CmisPropertyFilter data = getCmisPropertyFilter();
	    return data;
	}
	
	private void displayActionIcons(){
		
		download = (Button) findViewById(R.id.download);
		view = (Button) findViewById(R.id.view);
		share = (Button) findViewById(R.id.share);
		edit = (Button) findViewById(R.id.editmetadata);
		delete = (Button) findViewById(R.id.delete);
		qrcode = (Button) findViewById(R.id.qrcode);
		filter = (Button) findViewById(R.id.filter);
		openwith = (Button) findViewById(R.id.openwith);
		
		//File
		if (item != null && item.getSize() != null){
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActionUtils.openDocument(activity, item);
				}
			});
			
			download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActionUtils.saveAs(activity, activity.getIntent().getStringExtra("workspace"), item);
				}
			});
			
			openwith.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActionUtils.openWithDocument(activity, item);
				}
			});
			
			edit.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			
		} else {
			//FOLDER
			view.setVisibility(View.GONE);
			download.setVisibility(View.GONE);
			edit.setVisibility(View.GONE);
			//share.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			openwith.setVisibility(View.GONE);
		}
		
		
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActionUtils.shareDocument(activity, activity.getIntent().getStringExtra("workspace"), item);
			}
		});
		
		if (getCmisPrefs().isEnableScan()){
			qrcode.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					IntentIntegrator.shareText(activity, item.getSelfUrl());
				}
			});
		} else {
			qrcode.setVisibility(View.GONE);
		}
		
		
		
		filter.setOnClickListener(new OnClickListener() {
			private CharSequence[] cs;

			@Override
			public void onClick(View v) {
				
				cs = CmisPropertyFilter.getFiltersLabel(DocumentDetailsActivity.this, item); 
				
				AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetailsActivity.this);
				builder.setTitle(R.string.item_filter_title);
				builder.setSingleChoiceItems(cs, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 dialog.dismiss();
			        	 new ItemPropertiesDisplayTask(DocumentDetailsActivity.this, CmisPropertyFilter.getFilters(item).get(which)).execute();  
					}
				});
				builder.setNegativeButton(DocumentDetailsActivity.this.getText(R.string.cancel), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			           }
			       });
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
		
	}
	
	private void setTitleFromIntent() {
		setTitle(getString(R.string.title_details) + " '" + item.getTitle() + "'");
	}

	private void displayPropertiesFromIntent() {
		if (propertyFilter != null){
			new ItemPropertiesDisplayTask(this, true).execute("");
		} else {
			new ItemPropertiesDisplayTask(this).execute();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem settingsItem = menu.add(Menu.NONE, 1, 0, R.string.menu_item_home);
		settingsItem.setIcon(R.drawable.home);
		
		settingsItem = menu.add(Menu.NONE, 2, 0, R.string.menu_item_download_manager);
		settingsItem.setIcon(R.drawable.download_manager);
		
		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(this, HomeActivity.class));
			return true;
		case 2:
			startActivity(new Intent(this, DownloadProgressActivity.class));
			return true;
		}

		return false;
	}
	

	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}
	
	CmisPropertyFilter getCmisPropertyFilter() {
		return ((CmisApp) getApplication()).getCmisPropertyFilter();
	}
	
	Prefs getCmisPrefs() {
		return ((CmisApp) getApplication()).getPrefs();
	}
	
}
