package de.fmaul.android.cmis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutResourcesActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview.setText("This is the RESOURCES tab");
		setContentView(textview);
	}
}