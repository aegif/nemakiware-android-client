package de.fmaul.android.cmis;

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

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, AboutDevActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("dev").setIndicator("dev",
	                      res.getDrawable(R.drawable.dev))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, AboutResourcesActivity.class);
	    spec = tabHost.newTabSpec("resource").setIndicator("Resources",
	                      res.getDrawable(R.drawable.resources))
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