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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import jp.aegif.android.cmis.asynctask.ServerInfoLoadingTask;
import jp.aegif.android.cmis.database.Database;
import jp.aegif.android.cmis.database.ServerDAO;
import jp.aegif.android.cmis.model.Server;
import jp.aegif.android.cmis.repo.QueryType;
import jp.aegif.android.cmis.utils.UIUtils;

public class ServerActivity extends ListActivity {

	private ServerAdapter cmisSAdapter;
	
	
	private ArrayList<Server> listServer;


	private Server server;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (activityIsCalledWithSearchAction()){
			Intent intent = new Intent(this, SearchActivity.class);
			intent.putExtras(getIntent());
			this.finish();
			startActivity(intent);
		} else {
			setContentView(R.layout.server);

			createServerList();
			
			registerForContextMenu(getListView());
		}
	}
	
	private boolean activityIsCalledWithSearchAction() {
		final String queryAction = getIntent().getAction();
		return Intent.ACTION_SEARCH.equals(queryAction);
	}
	
	public void createServerList(){
		Database db = Database.create(this);
		ServerDAO serverDao = new ServerDAO(db.open());
		listServer = new ArrayList<Server>(serverDao.findAll());
		db.close();

		cmisSAdapter = new ServerAdapter(this, R.layout.server_row, listServer);
		setListAdapter(cmisSAdapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		MenuItem menuItem = menu.add(Menu.NONE, 1, 0, R.string.menu_item_server_add);
		menuItem.setIcon(R.drawable.add);
		
		menuItem = menu.add(Menu.NONE, 2, 0, R.string.menu_item_filter);
		menuItem.setIcon(R.drawable.filter);
		
		menuItem = menu.add(Menu.NONE, 3, 0, R.string.quit);
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
			startActivity( new Intent(this, CmisFilterActivity.class));
			return true;
		case 3:
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
			intent.putExtra("server", s);
			intent.putExtra("title", s.getName());
			
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.generic_error, Toast.LENGTH_LONG);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
		menu.add(0, 1, Menu.NONE, getString(R.string.server_info));
		menu.add(0, 2, Menu.NONE, getString(R.string.edit));
		menu.add(0, 3, Menu.NONE, getString(R.string.delete));
		menu.add(0, 4, Menu.NONE, getString(R.string.menu_item_favorites));
		UIUtils.createSearchMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		if (menuInfo != null){
			server = (Server) getListView().getItemAtPosition(menuInfo.position);
		}

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
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.delete);
				builder.setMessage(ServerActivity.this.getText(R.string.action_delete_desc) + " " + server.getName() + " ? ")
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						deleteServer(server.getId());
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).create();
				builder.show();
			}
			return true;
		case 4:
			if (server != null) {
				Intent intent = new Intent(this, FavoriteActivity.class);
				intent.putExtra("server", server);
				intent.putExtra("isFirstStart", true);
				startActivity(intent);
			}
			return true;
		
		case 20:
			onSearchRequested(QueryType.TITLE);
			return true;
		case 21:
			onSearchRequested(QueryType.FOLDER);
			return true;
		case 22:
			onSearchRequested(QueryType.FULLTEXT);
			return true;
		case 23:
			onSearchRequested(QueryType.CMISQUERY);
			return true;
		case 24:
			Intent intent = new Intent(this, SavedSearchActivity.class);
			intent.putExtra("server", server);
			intent.putExtra("isFirstStart", true);
			startActivity(intent);
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	public boolean onSearchRequested(QueryType queryType) {
		Bundle appData = new Bundle();
		appData.putString(QueryType.class.getName(), queryType.name());
		appData.putSerializable("server", server);
		startSearch("", false, appData, false);
		return true;
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
	
	private static ArrayList<String> getSearchItems(Activity activity) {
		ArrayList<String> filters = new ArrayList<String>(5);
		filters.add(activity.getText(R.string.menu_item_search_title).toString());
		filters.add(activity.getText(R.string.menu_item_search_folder_title).toString());
		filters.add(activity.getText(R.string.menu_item_search_fulltext).toString());
		filters.add(activity.getText(R.string.menu_item_search_cmis).toString());
		filters.add(activity.getText(R.string.menu_item_search_saved_search).toString());
		return filters;
	}
	
	private ArrayList<QueryType> getQueryType() {
		ArrayList<QueryType> filters = new ArrayList<QueryType>(5);
		filters.add(QueryType.TITLE);
		filters.add(QueryType.FOLDER);
		filters.add(QueryType.FULLTEXT);
		filters.add(QueryType.CMISQUERY);
		filters.add(null);
		return filters;
	}
	
	private CharSequence[] getSearchItemsLabel() {
		ArrayList<String> filters = getSearchItems(this);
		return filters.toArray(new CharSequence[filters.size()]);
	}
	
	void startSearch(){
		CharSequence[] cs = getSearchItemsLabel(); 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.search);
		builder.setTitle(R.string.menu_item_search);
		builder.setSingleChoiceItems(cs, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (getQueryType().get(which) != null){
					onSearchRequested(getQueryType().get(which));
				} else {
					Intent intent = new Intent(ServerActivity.this, SavedSearchActivity.class);
					intent.putExtra("server", server);
					intent.putExtra("isFirstStart", true);
					startActivity(intent);
				}
				 dialog.dismiss();
			}
		});
		builder.setNegativeButton(this.getText(R.string.cancel), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.cancel();
	           }
	       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
}
