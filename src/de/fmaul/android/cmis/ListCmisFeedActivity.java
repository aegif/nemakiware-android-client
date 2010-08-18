/*
 * Copyright (C) 2010 Florian Maul
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

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.fmaul.android.cmis.asynctask.FeedDisplayTask;
import de.fmaul.android.cmis.asynctask.FeedItemDisplayTask;
import de.fmaul.android.cmis.asynctask.ServerInitTask;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.QueryType;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.IntentIntegrator;
import de.fmaul.android.cmis.utils.IntentResult;
import de.fmaul.android.cmis.utils.StorageUtils;

public class ListCmisFeedActivity extends ListActivity {

	private static final String TAG = "ListCmisFeedActivity";
	
	private List<String> workspaces;
	private CharSequence[] cs;
	private Context context = this;
	private ListActivity activity = this;
	private OnSharedPreferenceChangeListener listener;
	private CmisItemLazy item;
	private CmisItemCollection items;

	private GridView gridview;

	private ListView listView;

	private Prefs prefs;

	/**
	 * Contains the current connection information and methods to access the
	 * CMIS repository.
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initWindow();
		initActionIcon();
		
		//Restart
		items = (CmisItemCollection) getLastNonConfigurationInstance();
		
		if (initRepository() == false){
			item = (CmisItemLazy) getIntent().getExtras().getSerializable("item");
			processSearchOrDisplayIntent();
		}
		//Filter Management
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				  if (prefs.getBoolean(activity.getString(R.string.cmis_repo_params), false)){
					  getRepository().generateParams(activity);
				  }
			  }
			};
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);

		//Set Breadcrumbs
		if (item != null){
			((TextView) activity.findViewById(R.id.path)).setText(item.getPath());
		} else {
			((TextView) activity.findViewById(R.id.path)).setText("/");
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && getRepository().isPaging()) {
	    	getRepository().generateParams(activity, false);
	    	this.finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final CmisItemCollection data = getItems();
	    return data;
	}
	
	private void initActionIcon() {
		Button home = (Button) findViewById(R.id.home);
		Button up = (Button) findViewById(R.id.up);
		Button back = (Button) findViewById(R.id.back);
		Button next = (Button) findViewById(R.id.next);
		Button refresh = (Button) findViewById(R.id.refresh);
		Button pref = (Button) findViewById(R.id.preference);
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(activity, HomeActivity.class);
				intent.putExtra("EXIT", false);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		up.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (item != null && item.getPath().equals("/") == false){
					new FeedItemDisplayTask(activity, getRepository().getServer(), item.getParentUrl(), 1).execute();
				} else {
					Intent intent = new Intent(activity, ServerActivity.class);
					intent.putExtra("EXIT", false);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
			}
		});
		
		//if (getRepository().isPaging()){
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getRepository().generateParams(activity, false);
					//ActionUtils.openNewListViewActivity(ListCmisFeedActivity.this, item);
					ListCmisFeedActivity.this.finish();
					//ListCmisFeedActivity.this.finish();
				}
			});
			
			next.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getRepository().generateParams(activity, true);
					ActionUtils.openNewListViewActivity(ListCmisFeedActivity.this, item);
				}
			});
		//}
		
		pref.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(activity, CmisFilterActivity.class));
			}
		});
		
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
	}
	
	private boolean initRepository() {
		boolean init = true;
		try {
			if (getRepository() == null) {
				new ServerInitTask(this, getApplication(), (Server) getIntent().getExtras().getSerializable("server")).execute();
			} else {
				// Case if we change repository.
				Bundle extra = this.getIntent().getExtras();
				if (extra != null && extra.getBoolean("isFirstStart")) {
					new ServerInitTask(this, getApplication(), (Server) getIntent().getExtras().getSerializable("server")).execute();
				} else {
					init = false;
				}
			}
		} catch (FeedLoadException fle) {
			ActionUtils.displayError(activity, R.string.generic_error);
		}
		return init;
	}
	
	public void processSearchOrDisplayIntent() {
		if (getRepository() != null) {
			if (activityIsCalledWithSearchAction()) {
				doSearchWithIntent(getIntent());
			} else {
				// display the feed that is passed in through the intent
				displayFeedInListView();
			}
		} else {
			Toast.makeText(this, getText(R.string.error_repo_connexion), 5);
		}
	}

	/**
	 * The type of the query is passed by the SearchManager through a bundle in
	 * the search intent.
	 * 
	 * @param intent
	 * @return
	 */
	private QueryType getQueryTypeFromIntent(Intent intent) {
		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			String queryType = appData.getString(QueryType.class.getName());
			return QueryType.valueOf(queryType);
		}
		return QueryType.FULLTEXT;
	}

	/**
	 * Tests if this activity is called with a Search intent.
	 * 
	 * @return
	 */
	private boolean activityIsCalledWithSearchAction() {
		final String queryAction = getIntent().getAction();
		return Intent.ACTION_SEARCH.equals(queryAction);
	}

	/**
	 * Initialize the window and the activity.
	 */
	private void initWindow() {
		setRequestedOrientation(getResources().getConfiguration().orientation);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.feed_list_main);
		
		gridview = (GridView) activity.findViewById(R.id.gridview);
		listView = activity.getListView();
		
		prefs = ((CmisApp) activity.getApplication()).getPrefs();
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			gridview.setOnItemClickListener(new CmisDocSelectedListener());
			gridview.setTextFilterEnabled(true);
			gridview.setClickable(true);
			gridview.setOnCreateContextMenuListener(this);
		} else {
			listView.setTextFilterEnabled(true);
			listView.setItemsCanFocus(true);
			listView.setClickable(true);
			listView.setOnItemClickListener(new CmisDocSelectedListener());
			listView.setOnCreateContextMenuListener(this);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.feed_menu_title));
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		CmisItem doc = (CmisItem) getListView().getItemAtPosition(info.position);
		
		menu.add(0, 2, Menu.NONE, getString(R.string.menu_item_details));
		menu.add(0, 3, Menu.NONE, getString(R.string.menu_item_share));

		if (doc != null && doc.getProperties().get("cmis:contentStreamLength") != null){
			menu.add(0, 1, Menu.NONE, getString(R.string.download));
		}
		
		menu.add(0, 4, Menu.NONE, getString(R.string.menu_item_favorites));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		CmisItem item = (CmisItem) getListView().getItemAtPosition(menuInfo.position);
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			item = (CmisItem) gridview.getItemAtPosition(menuInfo.position);
		} else {
			item = (CmisItem) listView.getItemAtPosition(menuInfo.position);
		}
		
		switch (menuItem.getItemId()) {
		case 1:
			if (item != null && item.hasChildren() == false) {
				ActionUtils.openDocument(activity, item);
			}
			return true;
		case 2:
			if (item != null) {
				ActionUtils.displayDocumentDetails(activity, item);
			}
			return true;
		case 3:
			if (item != null) {
				ActionUtils.shareDocument(activity, getRepository().getServer().getWorkspace(), item);
			}
			return true;
		case 4:
			if (item != null) {
				ActionUtils.createFavorite(activity, getRepository().getServer(), item);
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}

	/**
	 * Process the current intent as search intent, build a query url and
	 * display the feed.
	 * 
	 * @param queryIntent
	 */
	private void doSearchWithIntent(final Intent queryIntent) {
		final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);

		QueryType queryType = getQueryTypeFromIntent(queryIntent);
		String searchFeed = getRepository().getSearchFeed(queryType, queryString);
		new FeedDisplayTask(this, getRepository(), getString(R.string.search_results_for) + " '" + queryString + "'").execute(searchFeed);
	}

	/**
	 * Retrieves the feed to display from a regular intent. This is passed by
	 * the previous activity when a user selects a folder.
	 * 
	 * @return
	 */
	private String getFeedFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.get("feed") != null) {
				return extras.get("feed").toString();
			}
		}
		return null;
	}
	
	private String getTitleFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.get("title") != null) {
				return extras.get("title").toString();
			}
		}
		return null;
	}

	protected void reload(String workspace) {
			Intent intent = new Intent(this, ListCmisFeedActivity.class);
			
			intent.putExtra("EXIT", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			intent.putExtra("isFirstStart", true);
			
			Server s = getRepository().getServer();
			if (workspace != null){
				s.setWorkspace(workspace);
			} 
			intent.putExtra("server", s);
			intent.putExtra("title",s.getName());
			
			this.finish();
			startActivity(intent);
	}
	
	private void refresh() {
		
		String feed = "";
		if (item != null){
			feed = item.getDownLink();
		} else {
			feed = getRepository().getFeedRootCollection();
		}
		
		try {
			if (StorageUtils.deleteFeedFile(getApplication(), getRepository().getServer().getWorkspace(), feed)){
				activity.finish();
				if (item != null){
					ActionUtils.openNewListViewActivity(activity, item);
				} else {
					Intent intent = new Intent(activity, ListCmisFeedActivity.class);
					intent.putExtra("feed", feed);
					intent.putExtra("title", getTitleFromIntent());
					startActivity(intent);
				}
			} else {
				displayError(R.string.application_not_available);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void restart(String workspace) {
		reload(workspace);
	}
	
	protected void restart() {
		reload(null);
	}

	/**
	 * Displays the cmis feed given as in the list asynchronously
	 * 
	 * @param feed
	 */
	private void displayFeedInListView() {
		setTitle(R.string.loading);
		if (items != null){
			Log.d(TAG, "Start FeedDisplayTask : Items");
			new FeedDisplayTask(this, getRepository(), null, item, items).execute();
		} else if (item != null){
			Log.d(TAG, "Start FeedDisplayTask : Item");
			new FeedDisplayTask(this, getRepository(), item).execute(item.getDownLink());
		}  else {
			Log.d(TAG, "Start FeedDisplayTask : title");
			new FeedDisplayTask(this, getRepository(), getTitleFromIntent()).execute(getFeedFromIntent());
		}
	}

	private void displayError(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Listener that is called whenever a user clicks on a file or folder in the
	 * list.
	 */
	private class CmisDocSelectedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CmisItem doc = (CmisItem) parent.getItemAtPosition(position);

			if (doc.hasChildren()) {
				//new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), doc).execute(doc.getDownLink());
				ActionUtils.openNewListViewActivity(ListCmisFeedActivity.this, doc);
			} else {
				ActionUtils.displayDocumentDetails(ListCmisFeedActivity.this, doc);
			}
		}
	}
	

	/**
	 * Opens a feed url in a new listview. This enables the user to use the
	 * backbutton to get back to the previous list (usually the parent folder).
	 * 
	 * @param item
	 */
	/*private void openNewListViewActivity(CmisItem item) {
		Intent intent = new Intent(this, ListCmisFeedActivity.class);
		intent.putExtra("item", new CmisItemLazy(item));
		startActivity(intent);
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(Menu.NONE, 1, 0, R.string.menu_item_favorites);
		item.setIcon(R.drawable.favorites);
		createRepoMenu(menu);
		createSearchMenu(menu);
		item = menu.add(Menu.NONE, 3, 0, R.string.menu_item_about);
		item.setIcon(R.drawable.cmisexplorer);
		
		/* BETA : comment to desactivate Scan Process.*/
		item = menu.add(Menu.NONE, 10, 0, R.string.menu_item_scanner);
		item.setIcon(R.drawable.scanner);
		
		item = menu.add(Menu.NONE, 11, 0, R.string.menu_item_view);
		
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			item.setIcon(R.drawable.viewlisting);
		} else {
			item.setIcon(R.drawable.viewicons);
		}
		return true;
	}

	private void createRepoMenu(Menu menu) {
		SubMenu settingsMenu = menu.addSubMenu(R.string.menu_item_settings);
		settingsMenu.setIcon(R.drawable.repository);
		settingsMenu.setHeaderIcon(android.R.drawable.ic_menu_info_details);

		settingsMenu.add(Menu.NONE, 7, 0, R.string.menu_item_settings_reload);
		settingsMenu.add(Menu.NONE, 8, 0, R.string.menu_item_settings_repo);
		settingsMenu.add(Menu.NONE, 9, 0, R.string.menu_item_settings_ws);
	}
	
	private void createSearchMenu(Menu menu) {
		SubMenu searchMenu = menu.addSubMenu(R.string.menu_item_search);
		searchMenu.setIcon(R.drawable.search);
		searchMenu.getItem().setAlphabeticShortcut(SearchManager.MENU_KEY);
		searchMenu.setHeaderIcon(android.R.drawable.ic_menu_info_details);

		searchMenu.add(Menu.NONE, 4, 0, R.string.menu_item_search_title);
		searchMenu.add(Menu.NONE, 5, 0, R.string.menu_item_search_fulltext);
		searchMenu.add(Menu.NONE, 6, 0, R.string.menu_item_search_cmis);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			Intent intent = new Intent(this, FavoriteActivity.class);
			intent.putExtra("server", getRepository().getServer());
			startActivity(intent);
			return true;
		case 3:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case 4:
			onSearchRequested(QueryType.TITLE);
			return true;
		case 5:
			onSearchRequested(QueryType.FULLTEXT);
			return true;
		case 6:
			onSearchRequested(QueryType.CMISQUERY);
			return true;
		case 7:
			restart();
			return true;
		case 8:
			startActivity(new Intent(this, ServerActivity.class));
			return true;
		case 9:
			chooseWorkspace();
			return true;
		case 10:
			IntentIntegrator.initiateScan(this);
			return true;
		case 11:
			if(prefs.getDataView() == Prefs.GRIDVIEW){
				prefs.setDataView(Prefs.LISTVIEW);
			} else {
				prefs.setDataView(Prefs.GRIDVIEW);
			}
			refresh();
			return true;
		}
		return false;
	}
	
	private void chooseWorkspace(){
		try {
			Server server = getRepository().getServer();
			workspaces = FeedUtils.getRootFeedsFromRepo(server.getUrl(), server.getUsername(), server.getPassword());
			cs = workspaces.toArray(new CharSequence[workspaces.size()]);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.cmis_repo_choose_workspace);
			builder.setSingleChoiceItems(cs, workspaces.indexOf(server.getWorkspace()) ,new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        dialog.dismiss();
			        restart(cs[item].toString());
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		} catch (Exception e) {
			Toast.makeText(ListCmisFeedActivity.this, R.string.error_repo_connexion, Toast.LENGTH_LONG).show();
		}
	}
	
	 public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    	
	    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		    if (scanResult != null) {
		    	new FeedItemDisplayTask(activity, getRepository().getServer(), scanResult.getContents()).execute();
		    }
		}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSearchRequested()
	 */
	public boolean onSearchRequested(QueryType queryType) {
		Bundle appData = new Bundle();
		appData.putString(QueryType.class.getName(), queryType.name());
		startSearch("", false, appData, false);
		return true;
	}

	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}

	void setRepository(CmisRepository repo) {
		((CmisApp) getApplication()).setRepository(repo);
	}
	
	CmisItemCollection getItems() {
		return ((CmisApp) getApplication()).getItems();
	}

	void setItems(CmisItemCollection items) {
		((CmisApp) getApplication()).setItems(items);
	}

}