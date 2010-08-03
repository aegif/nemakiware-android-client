package de.fmaul.android.cmis;


import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import de.fmaul.android.cmis.repo.CmisDBAdapter;
import de.fmaul.android.cmis.repo.Server;

public class ServerActivity extends ListActivity {

	private CmisDBAdapter cmisDbAdapter;
	private CmisServersAdapter cmisSAdapter;
	
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	
	private ArrayList<Server> listServer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false); 
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();
		
		setContentView(R.layout.server);

		createServerList();
		
		registerForContextMenu(this.getListView());
	}
	
	public void createServerList(){
		cmisDbAdapter = new CmisDBAdapter(this); 
		cmisDbAdapter.open();
		listServer = cmisDbAdapter.getAllServers();
		cmisSAdapter = new CmisServersAdapter(this, R.layout.server_row, listServer);
		setListAdapter(cmisSAdapter);
		cmisDbAdapter.close();
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.server_menu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		switch(item.getItemId()){
		case R.id.menu_login:
			this.finish();
			startActivity(new Intent(this,ServerEditActivity.class));
			return true;
		case R.id.menu_quit:
			this.finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {

		Server s = listServer.get(position);
		if (s != null){
			
			Intent intent = new Intent(this, ListCmisFeedActivity.class);
			
			intent.putExtra("isFirstStart", true);
			
			editor.putLong("serverID", s.getId());
			editor.putString("serverName", s.getName());
			editor.putString("serverURL", s.getUrl());
			editor.putString("username", s.getUsername());
			editor.putString("password", s.getPassword());
			editor.putString("workspace", s.getWorkspace());
			editor.commit();
			
			startActivity(intent);
		} else {
			Toast.makeText(this, "ERROR", 3);
		}
		
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.server_menu_title));
		menu.add(0, 1, Menu.NONE, getString(R.string.edit));
		menu.add(0, 2, Menu.NONE, getString(R.string.delete));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		Server server = (Server) getListView().getItemAtPosition(menuInfo.position);

		switch (menuItem.getItemId()) {
		case 1:
			if (server != null) {
				editServer(server);
			}
			return true;
		case 2:
			if (server != null) {
				deleteServer(server.getId());
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	public void deleteServer(long id){
		cmisDbAdapter = new CmisDBAdapter(this);
		cmisDbAdapter.open();
		
			if (cmisDbAdapter.deleteServer(id)){
				Toast.makeText(this, this.getString(R.string.server_delete), 
						Toast.LENGTH_LONG).show();
				createServerList();

			}else{
				Toast.makeText(this, this.getString(R.string.server_delete_error), 
						Toast.LENGTH_LONG).show();
			}
		cmisDbAdapter.close();
	}
	
	public void editServer(Server server){
		Intent intent = new Intent(this, ServerEditActivity.class);
		intent.putExtra("server", server);
		this.finish();
		startActivity(intent);
	}
	
}
