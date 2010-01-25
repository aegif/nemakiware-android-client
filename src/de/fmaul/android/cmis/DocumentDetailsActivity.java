package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fmaul.android.cmis.repo.CmisProperty;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SimpleAdapter;

public class DocumentDetailsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		displayPropertiesFromIntent();
	}

	private void displayPropertiesFromIntent() {
		List<CmisProperty> propList = getPropertiesFromIntent(); 
		List<Map<String, ?>> list = buildListOfNameValueMaps(propList);
		initListAdapter(list);
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
			List<CmisProperty> propList) {
		List<Map<String,?>> list = new ArrayList<Map<String,?>>();
		for (CmisProperty cmisProperty : propList) {
			list.add(createPair(getDisplayNameFromProperty(cmisProperty), cmisProperty.getValue()));
		}
		return list;
	}

	private String getDisplayNameFromProperty(CmisProperty cmisProperty) {
		String name = cmisProperty.getDisplayName();
		if (TextUtils.isEmpty(name)) {
			name = cmisProperty.getDefinitionId().replaceAll("cmis:", "");
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
}
