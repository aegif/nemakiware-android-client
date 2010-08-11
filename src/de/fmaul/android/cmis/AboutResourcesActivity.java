package de.fmaul.android.cmis;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutResourcesActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_res);

		((Button) findViewById(R.id.open_icon)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent viewIntent = new Intent(Intent.ACTION_VIEW);
				viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				viewIntent.setData(Uri.parse("http://openiconlibrary.sourceforge.net/"));

				try {
					startActivity(viewIntent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(AboutResourcesActivity.this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
				}
			}
		});

	}
}
