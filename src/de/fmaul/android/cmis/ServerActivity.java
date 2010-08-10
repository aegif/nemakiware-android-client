package de.fmaul.android.cmis;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import de.fmaul.android.cmis.asynctask.ServerInfoLoadingTask;
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.ServerDAO;
import de.fmaul.android.cmis.model.Server;

public class ServerActivity extends ListActivity {

	private CmisServersAdapter cmisSAdapter;
	
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	
	private ArrayList<Server> listServer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();
		
		setContentView(R.layout.server);

		createServerList();
		
		registerForContextMenu(getListView());
	}
	
	public void createServerList(){
		Database db = Database.create(this);
		ServerDAO serverDao = new ServerDAO(db.open());
		listServer = new ArrayList<Server>(serverDao.findAll());
		db.close();

		cmisSAdapter = new CmisServersAdapter(this, R.layout.server_row, listServer);
		setListAdapter(cmisSAdapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		MenuItem menuItem = menu.add(Menu.NONE, 1, 0, R.string.menu_item_server_add);
		menuItem.setIcon(R.drawable.add);
		
		menuItem = menu.add(Menu.NONE, 2, 0, R.string.quit);
		menuItem.setIcon(R.drawable.quit);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case 1:
			startActivity(new Intent(this,ServerEditActivity.class));
			return true;
		case 2:
			Intent intent = new Intent(this, HomeActivity.class);
			intent.putExtra("EXIT", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {

		Server s = listServer.get(position);
		if (s != null){
			
			Intent intent = new Intent(this, ListCmisFeedActivity.class);
			
			intent.putExtra("isFirstStart", true);
			intent.putExtra("title", s.getName());
			
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
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		menu.add(0, 1, Menu.NONE, getString(R.string.server_info));
		menu.add(0, 2, Menu.NONE, getString(R.string.edit));
		menu.add(0, 3, Menu.NONE, getString(R.string.delete));
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
				getInfoServer(server);
			}
			return true;
		case 2:
			if (server != null) {
				editServer(server);
				
			}
			return true;
		case 3:
			if (server != null) {
				deleteServer(server.getId());
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	public void deleteServer(long id){
		Database db = Database.create(this);
		ServerDAO serverDao = new ServerDAO(db.open());

		if (serverDao.delete(id)) {
			Toast.makeText(this, this.getString(R.string.server_delete),
					Toast.LENGTH_LONG).show();
			createServerList();

		} else {
			Toast.makeText(this, this.getString(R.string.server_delete_error),
					Toast.LENGTH_LONG).show();
		}
		db.close();
	}
	
	public void editServer(Server server){
		Intent intent = new Intent(this, ServerEditActivity.class);
		intent.putExtra("server", server);
		startActivity(intent);
	}
	
	public void getInfoServer(Server server){
		new ServerInfoLoadingTask(this, server).execute();
	}
	
}
