package de.fmaul.android.cmis;

import java.util.ArrayList;

import org.dom4j.Document;

import android.app.Activity;
import android.app.ListActivity;
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
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedUtils;

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

		Favorite f = listFavorite.get(position);
		if (f != null){
			Intent intent;
			if (f.getMimetype() != null && f.getMimetype().length() != 0){
				
				//TODO ASYNCTASK
				new FeedItemDisplayTask(activity, currentServer, f.getUrl()).execute();
				
				/*intent = new Intent(this, DocumentDetailsActivity.class);
				Document doc = FeedUtils.readAtomFeed(f.getUrl(), currentServer.getUsername(), currentServer.getPassword());
				CmisItem item = CmisItem.createFromFeed(doc.getRootElement());
				if (item != null){
					ActionUtils.displayDocumentDetails(activity, currentServer, item);
				} else {
					ActionUtils.displayError(activity, "ERROR during favorite");
				}*/
			} else {
				intent = new Intent(this, ListCmisFeedActivity.class);
				intent.putExtra("title", f.getName());
				intent.putExtra("feed", f.getUrl());
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, "ERROR", 3);
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
			Toast.makeText(this, this.getString(R.string.server_delete), Toast.LENGTH_LONG).show();
			createFavoriteList();
		} else {
			Toast.makeText(this, this.getString(R.string.server_delete_error), Toast.LENGTH_LONG).show();
		}
		db.close();
	}
}
