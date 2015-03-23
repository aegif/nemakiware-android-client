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
import de.fmaul.android.cmis.asynctask.FeedItemDisplayTask;
import de.fmaul.android.cmis.asynctask.ServerInitTask;
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.FavoriteDAO;
import de.fmaul.android.cmis.database.SearchDAO;
import de.fmaul.android.cmis.model.Favorite;
import de.fmaul.android.cmis.model.Search;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class SavedSearchActivity extends ListActivity {

	private ArrayList<Search> listSearch;
	private Server currentServer;
	private Activity activity;
	private boolean firstStart = true;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			currentServer = (Server) bundle.getSerializable("server");
			firstStart = bundle.getBoolean("isFirstStart");
		}
		
		setContentView(R.layout.server);
		setTitle(this.getText(R.string.saved_search_title) + " : " +  currentServer.getName());

		createSearchList();
		registerForContextMenu(getListView());
		initRepository();
	}
	
	public void createSearchList(){
		try {
			Database db = Database.create(this);
			SearchDAO searchDao = new SearchDAO(db.open());
			listSearch = new ArrayList<Search>(searchDao.findAll(currentServer.getId()));
			db.close();
			setListAdapter(new SavedSearchAdapter(this, R.layout.feed_list_row, listSearch));
		} catch (Exception e) {
			ActionUtils.displayMessage(this, e.getMessage());
		}
		
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {

		final Search s = listSearch.get(position);
		if (s != null){
			Intent intents = new Intent(this, SearchActivity.class);
			intents.putExtra("savedSearch", s);
			startActivity(intents);
			this.finish();
		} else {
			ActionUtils.displayMessage(this, R.string.favorite_error);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.saved_search_option));
		menu.add(0, 1, Menu.NONE, getString(R.string.delete));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		Search search = (Search) getListView().getItemAtPosition(menuInfo.position);

		switch (menuItem.getItemId()) {
		case 1:
			if (search != null) {
				delete(search.getId());
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	public void delete(long id){
		Database db = Database.create(this);
		SearchDAO searchDao = new SearchDAO(db.open());

		if (searchDao.delete(id)) {
			Toast.makeText(this, this.getString(R.string.favorite_delete), Toast.LENGTH_LONG).show();
			createSearchList();
		} else {
			Toast.makeText(this, this.getString(R.string.favorite_delete_error), Toast.LENGTH_LONG).show();
		}
		db.close();
	}
	
	private boolean initRepository() {
		boolean init = true;
		try {
			if (getRepository() == null) {
				new ServerInitTask(this, getApplication(), (Server) getIntent().getExtras().getSerializable("server")).execute();
			} else {
				// Case if we change repository.
				if (firstStart) {
					new ServerInitTask(this, getApplication(), (Server) getIntent().getExtras().getSerializable("server")).execute();
				} else {
					init = false;
				}
			}
		} catch (FeedLoadException fle) {
			ActionUtils.displayMessage(activity, R.string.generic_error);
		}
		return init;
	}
	
	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}
	
}
