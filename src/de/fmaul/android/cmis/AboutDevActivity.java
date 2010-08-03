package de.fmaul.android.cmis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class AboutDevActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview.setText("This is the DEV tab");
		setContentView(textview);
	}
}
