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
package jp.aegif.android.cmis;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import jp.aegif.android.cmis.asynctask.SearchDisplayTask;
import jp.aegif.android.cmis.asynctask.ServerSearchInitTask;
import jp.aegif.android.cmis.model.Search;
import jp.aegif.android.cmis.model.Server;
import jp.aegif.android.cmis.repo.CmisItem;
import jp.aegif.android.cmis.repo.CmisItemCollection;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.repo.QueryType;
import jp.aegif.android.cmis.utils.ActionUtils;
import jp.aegif.android.cmis.utils.FeedLoadException;
import jp.aegif.android.cmis.utils.StorageUtils;
import jp.aegif.android.cmis.utils.UIUtils;

public class SearchActivity extends ListActivity {

	private static final String TAG = "SearchActivity";
	
	private ListActivity activity = this;
	private ListView listView;
	private Server server;
	private String searchFeed;
	private String queryString;
	private Prefs prefs;
	private Search savedSearch = null;
	private EditText input;
	
	
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
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			savedSearch = (Search) bundle.getSerializable("savedSearch");
		}
		
		//Restart
		if (getLastNonConfigurationInstance() != null ){
			
		}
		
		if (initRepository() == false){
			doSearchWithIntent();
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	    	this.finish();
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return null;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void initActionIcon() {
		Button home = (Button) findViewById(R.id.home);
		Button refresh = (Button) findViewById(R.id.refresh);
		Button save = (Button) findViewById(R.id.save);
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
		
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (StorageUtils.deleteFeedFile(getApplication(), getRepository().getServer().getWorkspace(), searchFeed)){
						Log.d(TAG, "SearchFeed : " + searchFeed);
						if (savedSearch != null){
							queryString = savedSearch.getName();
						}
						if (new SearchPrefs(activity).getExactSearch()){
							searchFeed = searchFeed.replaceAll("%25", "");
						}
						new SearchDisplayTask(SearchActivity.this, getRepository(), queryString).execute(searchFeed);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogInterface.OnClickListener saveSearch = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (server == null){
							server = getRepository().getServer();
						}
						Log.d(TAG, "SearchFeed : " + server + " - " + searchFeed.substring( searchFeed.indexOf("=")+1, searchFeed.indexOf("&")) + " - " + input.getText().toString());
						ActionUtils.createSaveSearch(SearchActivity.this, server, input.getText().toString(), searchFeed.substring( searchFeed.indexOf("=")+1, searchFeed.indexOf("&")));
					}
				};
				createDialog(R.string.search_create_title, R.string.search_create_desc, "", saveSearch).show();
			}
		});
		
		pref.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(activity, SearchPreferencesActivity.class));
			}
		});
		
	}
	
	private boolean initRepository() {
		if (savedSearch != null) {
			return false;
		}
		boolean init = true;
		try {
			if (getRepository() == null) {
				new ServerSearchInitTask(this, getApplication(), (Server) getIntent().getBundleExtra(SearchManager.APP_DATA).getSerializable("server"), getIntent()).execute();
			} else {
				// Case if we change repository.
				server = (Server) getIntent().getBundleExtra(SearchManager.APP_DATA).getSerializable("server");
				if (getRepository().getServer() != null && server!= null) {
					if (getRepository().getServer().getName().equals(server.getName())) {
						init = false;
					} else {
						new ServerSearchInitTask(this, getApplication(), (Server) getIntent().getBundleExtra(SearchManager.APP_DATA).getSerializable("server"), getIntent()).execute();
					}
				} else {
					init = false;
				}
			}
		} catch (FeedLoadException fle) {
			ActionUtils.displayMessage(activity, R.string.generic_error);
		}
		return init;
	}
	

	/**
	 * Initialize the window and the activity.
	 */
	private void initWindow() {
		setRequestedOrientation(getResources().getConfiguration().orientation);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.search_list_main);
		
		prefs = ((CmisApp) activity.getApplication()).getPrefs();
		
		listView = activity.getListView();
		
		listView.setTextFilterEnabled(true);
		listView.setItemsCanFocus(true);
		listView.setClickable(true);
		listView.setOnItemClickListener(new CmisDocSelectedListener());
		listView.setOnCreateContextMenuListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		UIUtils.createContextMenu(activity, menu, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		return UIUtils.onContextItemSelected(this, menuItem, prefs);
	}

	/**
	 * Process the current intent as search intent, build a query url and
	 * display the feed.
	 * 
	 * @param queryIntent
	 */
	private void doSearchWithIntent() {
		if (savedSearch != null){
			searchFeed = getRepository().getSearchFeed(this, QueryType.SAVEDSEARCH, savedSearch.getUrl());
			Log.d(TAG, "SearchFeed : " + searchFeed);
			new SearchDisplayTask(this, getRepository(), savedSearch.getName()).execute(searchFeed);
		} else {
			queryString = getIntent().getStringExtra(SearchManager.QUERY);
			QueryType queryType = getQueryTypeFromIntent(getIntent());
			searchFeed = getRepository().getSearchFeed(this, queryType, queryString);
			Log.d(TAG, "SearchFeed : " + searchFeed);
			new SearchDisplayTask(this, getRepository(), queryString).execute(searchFeed);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		UIUtils.createSearchMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
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
			if (server == null){
				server = getRepository().getServer();
			}
			intent.putExtra("server", server);
			startActivity(intent);
			this.finish();
			return true;
		default:
			return false;
		}
	}
	
	public boolean onSearchRequested(QueryType queryType) {
		Bundle appData = new Bundle();
		appData.putString(QueryType.class.getName(), queryType.name());
		startSearch("", false, appData, false);
		return true;
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
				ActionUtils.openNewListViewActivity(activity, doc);
			} else {
				ActionUtils.displayDocumentDetails(activity, getRepository().getServer(), doc);
			}
		}
	}
	
	private AlertDialog createDialog(int title, int message, String defaultValue, DialogInterface.OnClickListener positiveClickListener){
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(message);
		input = new EditText(this);
		input.setText(defaultValue);
		alert.setView(input);
		alert.setPositiveButton(R.string.validate, positiveClickListener);

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		return alert.create();
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
	
	Prefs getCmisPrefs() {
		return ((CmisApp) getApplication()).getPrefs();
	}
	
	public ListCmisFeedActivitySave getSaveContext(){
		return ((CmisApp) getApplication()).getSavedContextItems();
	}
	
	public void setSaveContext(ListCmisFeedActivitySave save){
		((CmisApp) getApplication()).setSavedContextItems(save);
	}
	

}