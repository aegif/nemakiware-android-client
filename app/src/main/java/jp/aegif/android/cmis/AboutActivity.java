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
package jp.aegif.android.cmis;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class AboutActivity extends TabActivity {
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);

	    Resources res = getResources(); 
	    TabHost tabHost = getTabHost();  
	    TabHost.TabSpec spec;  
	    Intent intent;  

	    intent = new Intent().setClass(this, AboutDevActivity.class);

	    spec = tabHost.newTabSpec("dev").setIndicator(this.getText(R.string.about_dev),res.getDrawable(R.drawable.dev))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, AboutResourcesActivity.class);
	    spec = tabHost.newTabSpec("res").setIndicator(this.getText(R.string.about_resources), res.getDrawable(R.drawable.resources))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem settingsItem = menu.add(Menu.NONE, 1, 0, "Home");
		settingsItem.setIcon(R.drawable.cmisexplorer);
		
		return true;

	}
	
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(this, HomeActivity.class));
			return true;
		}

		return false;
	}
    
    
}