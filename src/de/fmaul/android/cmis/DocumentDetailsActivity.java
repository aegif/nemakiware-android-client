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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;
import de.fmaul.android.cmis.utils.ListUtils;

public class DocumentDetailsActivity extends ListActivity {

	private CmisItem item;
	private Button download, share, edit, delete;
	private String objectTypeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		setTitleFromIntent();
		displayPropertiesFromIntent();
		displayActionIcons();
	}
	
	private void displayActionIcons(){
		
		item = CmisItem.create(getIntent().getStringExtra("title"), null, getIntent().getStringExtra("mimetype"), getIntent().getStringExtra("contentUrl"));
		
		download = (Button) findViewById(R.id.download);
		share = (Button) findViewById(R.id.share);
		edit = (Button) findViewById(R.id.editmetadata);
		delete = (Button) findViewById(R.id.delete);
		
		//File
		if (item != null && getContentFromIntent() != null){
			download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openDocument();
				}
			});
			
			share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					emailDocument();
				}
			});
			
			edit.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			
		} else {
			//FOLDER
			download.setVisibility(View.GONE);
			edit.setVisibility(View.GONE);
			share.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
		}
	}

	private void setTitleFromIntent() {
		String title = getIntent().getStringExtra("title");
		setTitle(getString(R.string.title_details) + " '" + title + "'");
	}

	private void displayPropertiesFromIntent() {
		List<CmisProperty> propList = getPropertiesFromIntent();
		objectTypeId = getObjectTypeIdFromIntent();
		CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(objectTypeId);
		List<Map<String, ?>> list = buildListOfNameValueMaps(propList, typeDefinition);
		initListAdapter(list);
	}

	private String getObjectTypeIdFromIntent() {
		return getIntent().getStringExtra("objectTypeId");
	}
	
	private String getBaseTypeIdFromIntent() {
		return getIntent().getStringExtra("baseTypeId");
	}
	
	private String getContentFromIntent() {
		return getIntent().getStringExtra("contentStream");
	}

	private void initListAdapter(List<Map<String, ?>> list) {
		SimpleAdapter props = new SimpleAdapter(this, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
				R.id.propertyName, R.id.propertyValue });

		setListAdapter(props);
	}

	private List<Map<String, ?>> buildListOfNameValueMaps(List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for (CmisProperty cmisProperty : propList) {
			if (cmisProperty.getDefinitionId() != null) {
				list.add(ListUtils.createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
			}
		}
		return list;
	}

	private String getDisplayNameFromProperty(CmisProperty property, CmisTypeDefinition typeDefinition) {
		String name = property.getDisplayName();

		if (TextUtils.isEmpty(name)) {
		}
		name = typeDefinition.getDisplayNameForProperty(property);

		if (TextUtils.isEmpty(name)) {
			name = property.getDefinitionId();
		}
		
		return name.replaceAll("cmis:", "");
	}

	private ArrayList<CmisProperty> getPropertiesFromIntent() {
		ArrayList<CmisProperty> propList = getIntent().getParcelableArrayListExtra("properties");
		return propList;
	}

	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}
	
	/**
	 * Opens a file by downloading it and starting the associated app.
	 * 
	 * @param item
	 */
	private void openDocument() {

		File content = item.getContent(getIntent().getStringExtra("workspace"));
		if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent())){
			viewFileInAssociatedApp(content, item.getMimeType());
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
	}

	private void displayError(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Displays a file on the local system with the associated app by calling
	 * the ACTION_VIEW intent.
	 * 
	 * @param tempFile
	 * @param mimeType
	 */
	private void viewFileInAssociatedApp(File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void emailDocument() {
		
		new AbstractDownloadTask(getRepository(), this) {
			@Override
			public void onDownloadFinished(File contentFile) {
				if (contentFile != null && contentFile.exists()) {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
					i.putExtra(Intent.EXTRA_TEXT, item.getContentUrl());
					i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
					i.setType(item.getMimeType());
					startActivity(Intent.createChooser(i, "Email file"));
				} else {
					displayError(R.string.error_file_does_not_exists);
				}
			}
		}.execute(item);
	}

}
