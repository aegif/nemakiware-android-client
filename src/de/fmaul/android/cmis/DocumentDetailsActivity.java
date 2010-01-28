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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisPropertyTypeDefinition;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SimpleAdapter;

public class DocumentDetailsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		setTitleFromIntent();
		displayPropertiesFromIntent();
	}

	private void setTitleFromIntent() {
		String title = getIntent().getStringExtra("title");
		setTitle("Details for '"+title+"'");
	}

	private void displayPropertiesFromIntent() {
		List<CmisProperty> propList = getPropertiesFromIntent();
		String objectTypeId  = getObjectTypeIdFromIntent();
		CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(objectTypeId);
		List<Map<String, ?>> list = buildListOfNameValueMaps(propList, typeDefinition);
		initListAdapter(list);
	}

	private String getObjectTypeIdFromIntent() {
		return getIntent().getStringExtra("objectTypeId");
	}

	private void initListAdapter(List<Map<String, ?>> list) {
		SimpleAdapter props = new SimpleAdapter(
				this,
				list,
				R.layout.document_details_row,
				new String[] { "name","value" },
				new int[] { R.id.propertyName, R.id.propertyValue } );

		setListAdapter(props);
	}

	private List<Map<String, ?>> buildListOfNameValueMaps(
			List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
		List<Map<String,?>> list = new ArrayList<Map<String,?>>();
		for (CmisProperty cmisProperty : propList) {
			list.add(createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
		}
		return list;
	}

	private String getDisplayNameFromProperty(CmisProperty property,
			CmisTypeDefinition typeDefinition) {
		String name = property.getDisplayName();
		
		if (TextUtils.isEmpty(name)) {
		}	name = typeDefinition.getDisplayNameForProperty(property);
		
		if (TextUtils.isEmpty(name)) {
			name = property.getDefinitionId().replaceAll("cmis:", ""); 
		}
		return name;
	}

	private ArrayList<CmisProperty> getPropertiesFromIntent() {
		ArrayList<CmisProperty> propList = getIntent().getParcelableArrayListExtra("properties");
		return propList;
	}
	
	private Map<String, ?> createPair(String name, String value) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("name", name);
		hashMap.put("value", value);
		return hashMap;
	}
	
	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}
	
}
