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

		ArrayList<CmisProperty> propList = getIntent().getParcelableArrayListExtra("properties"); 
		
		List<Map<String,?>> list = new ArrayList<Map<String,?>>();
		for (CmisProperty cmisProperty : propList) {
			String name = cmisProperty.getDisplayName();
			if (TextUtils.isEmpty(name)) {
				name = cmisProperty.getDefinitionId();
			}
			
			list.add(createPair(name, cmisProperty.getValue()));
		}
		
		list.add(createPair("testName", "testValue"));
		
		SimpleAdapter props = new SimpleAdapter(
				this,
				list,
				R.layout.document_details_row,
				new String[] { "name","value" },
				new int[] { R.id.propertyName, R.id.propertyValue } );

		setListAdapter(props);
	}
	

	private Map<String, ?> createPair(String name, String value) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("name", name);
		hashMap.put("value", value);
		return hashMap;
	}

	
}
