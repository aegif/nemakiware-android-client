package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.Server;
import de.fmaul.android.cmis.utils.ListUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ServerInfoGeneralActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		new ServerInfoDisplayTask(this).execute();
		
		/*//Init View
		setContentView(R.layout.server_info_general);
		
		//
		ArrayList<CmisProperty> propList = getIntent().getParcelableArrayListExtra(getIntent().getStringExtra("context"));
		
		List<Map<String, ?>> list = ListUtils.buildListOfNameValueMaps(propList);
		SimpleAdapter props = new SimpleAdapter(this, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
				R.id.propertyName, R.id.propertyValue });
		
		ListView listInfo = (ListView) findViewById(R.id.server_info_general);
		listInfo.setAdapter(props);*/
	}
	
}
