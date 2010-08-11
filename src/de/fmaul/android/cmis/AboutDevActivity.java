/*
 * Copyright (C) 2010 Jean Marie PASCAL
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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AboutDevActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_dev);
		
		ListView lv1 = (ListView) findViewById(R.id.Listdev);
		String[] devs = getResources().getStringArray(R.array.dev);
		lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devs));
		
		
		((Button) findViewById(R.id.open_icon)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent viewIntent = new Intent(Intent.ACTION_VIEW);
				viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				viewIntent.setData(Uri.parse("http://code.google.com/p/android-cmis-browser/"));

				try {
					startActivity(viewIntent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(AboutDevActivity.this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
}
