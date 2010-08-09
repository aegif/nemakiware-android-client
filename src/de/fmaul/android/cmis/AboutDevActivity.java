package de.fmaul.android.cmis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AboutDevActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_dev);
		
		ListView lv1 = (ListView) findViewById(R.id.Listdev);
		String[] devs = getResources().getStringArray(R.array.dev);
		lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devs));
		
	}
}
