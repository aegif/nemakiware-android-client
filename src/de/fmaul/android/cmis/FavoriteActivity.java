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
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.FavoriteDAO;
import de.fmaul.android.cmis.model.Favorite;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.utils.ActionUtils;

public class FavoriteActivity extends ListActivity {

	private ArrayList<Favorite> listFavorite;
	private Server currentServer;
	private Activity activity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			currentServer = (Server) getIntent().getExtras().getSerializable("server");
		}
		
		setContentView(R.layout.server);
		setTitle("Favorites for " + currentServer.getName());

		createFavoriteList();
		registerForContextMenu(getListView());
	}
	
	public void createFavoriteList(){
		Database db = Database.create(this);
		FavoriteDAO favoriteDao = new FavoriteDAO(db.open());
		listFavorite = new ArrayList<Favorite>(favoriteDao.findAll(currentServer.getId()));
		db.close();
		setListAdapter(new FavoriteAdapter(this, R.layout.feed_list_row, listFavorite));
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {

		final Favorite f = listFavorite.get(position);
		if (f != null){
			if (f.getMimetype() != null && f.getMimetype().length() != 0){
				new FeedItemDisplayTask(activity, currentServer, f.getUrl()).execute();
			} else {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("Choose your view...").setCancelable(true)
						.setPositiveButton("Details", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new FeedItemDisplayTask(activity, currentServer, f.getUrl()).execute();
							}

						}).setNegativeButton("Folder", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new FeedItemDisplayTask(activity, currentServer, f.getUrl(), 1).execute();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} else {
			ActionUtils.displayError(this, R.string.favorite_error);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.context_menu_title));
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

		Favorite favorite = (Favorite) getListView().getItemAtPosition(menuInfo.position);

		switch (menuItem.getItemId()) {
		case 1:
			if (favorite != null) {
				delete(favorite.getId());
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
	
	public void delete(long id){
		Database db = Database.create(this);
		FavoriteDAO favoriteDao = new FavoriteDAO(db.open());

		if (favoriteDao.delete(id)) {
			Toast.makeText(this, this.getString(R.string.favorite_delete), Toast.LENGTH_LONG).show();
			createFavoriteList();
		} else {
			Toast.makeText(this, this.getString(R.string.favorite_delete_error), Toast.LENGTH_LONG).show();
		}
		db.close();
	}
	
}
