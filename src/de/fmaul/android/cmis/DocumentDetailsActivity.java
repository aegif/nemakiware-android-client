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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.IntentIntegrator;
import de.fmaul.android.cmis.utils.ListUtils;

public class DocumentDetailsActivity extends ListActivity {

	private CmisItemLazy item;
	private Button download, share, edit, delete, qrcode;
	private String objectTypeId;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		activity = this;
		
		item = (CmisItemLazy) getIntent().getExtras().getSerializable("item");
		
		setTitleFromIntent();
		displayPropertiesFromIntent();
		displayActionIcons();
	}
	
	private void displayActionIcons(){
		
		download = (Button) findViewById(R.id.download);
		share = (Button) findViewById(R.id.share);
		edit = (Button) findViewById(R.id.editmetadata);
		delete = (Button) findViewById(R.id.delete);
		qrcode = (Button) findViewById(R.id.qrcode);
		
		//File
		if (item != null && item.getSize() != null){
			download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActionUtils.openDocument(activity, item);
				}
			});
			
			edit.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			
		} else {
			//FOLDER
			download.setVisibility(View.GONE);
			edit.setVisibility(View.GONE);
			//share.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
		}
		
		
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActionUtils.shareDocument(activity, activity.getIntent().getStringExtra("workspace"), item);
			}
		});
		
		qrcode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator.shareText(activity, item.getSelfUrl());
			}
		});
	}

	private void setTitleFromIntent() {
		setTitle(getString(R.string.title_details) + " '" + item.getTitle() + "'");
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
}
