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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import de.fmaul.android.cmis.model.Server;

public class ServerInfoActivity extends TabActivity {
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.about);
	    
	    Resources res = getResources(); 
	    TabHost tabHost = getTabHost();  
	    TabHost.TabSpec spec;  
	    Intent intent;  

	    // TAB GENERAL INFO
	    intent = new Intent().setClass(this, ServerInfoGeneralActivity.class);
		intent.putParcelableArrayListExtra(Server.INFO_GENERAL, getIntent().getParcelableArrayListExtra(Server.INFO_GENERAL));
	    intent.putExtra("context", Server.INFO_GENERAL);
		spec = tabHost.newTabSpec(this.getText(R.string.server_info_general).toString()).setIndicator(this.getText(R.string.server_info_general), res.getDrawable(R.drawable.resources)).setContent(intent);
	    tabHost.addTab(spec);

	    // TAB CAPABILITIES
	    intent = new Intent().setClass(this, ServerInfoGeneralActivity.class);
	    intent.putExtra("context", Server.INFO_CAPABILITIES);
		intent.putParcelableArrayListExtra(Server.INFO_CAPABILITIES, getIntent().getParcelableArrayListExtra(Server.INFO_CAPABILITIES));
	    spec = tabHost.newTabSpec(this.getText(R.string.server_info_capabilites).toString()).setIndicator(this.getText(R.string.server_info_capabilites), res.getDrawable(R.drawable.capabilities)).setContent(intent);
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