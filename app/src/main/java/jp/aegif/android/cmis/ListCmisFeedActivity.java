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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import jp.aegif.android.cmis.asynctask.FeedDisplayTask;
import jp.aegif.android.cmis.asynctask.FeedItemDisplayTask;
import jp.aegif.android.cmis.asynctask.ServerInitTask;
import jp.aegif.android.cmis.model.Server;
import jp.aegif.android.cmis.repo.CmisItem;
import jp.aegif.android.cmis.repo.CmisItemCollection;
import jp.aegif.android.cmis.repo.CmisItemLazy;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.repo.QueryType;
import jp.aegif.android.cmis.utils.ActionUtils;
import jp.aegif.android.cmis.utils.FeedLoadException;
import jp.aegif.android.cmis.utils.FeedUtils;
import jp.aegif.android.cmis.utils.IntentIntegrator;
import jp.aegif.android.cmis.utils.IntentResult;
import jp.aegif.android.cmis.utils.StorageUtils;
import jp.aegif.android.cmis.utils.UIUtils;

public class ListCmisFeedActivity extends ListActivity {

	private static final String TAG = "ListCmisFeedActivity";
	
	private List<String> workspaces;
	private CharSequence[] cs;
	private Context context = this;
	private ListActivity activity = this;
	private OnSharedPreferenceChangeListener listener;
	private CmisItemLazy item;
	private CmisItemLazy itemParent;
	private CmisItemCollection items;
	private GridView gridview;
	private ListView listView;
	private Prefs prefs;
	private ArrayList<CmisItemLazy> currentStack =  new ArrayList<CmisItemLazy>();

	private ListCmisFeedActivitySave save;
	private boolean firstStart = false;
	
	protected static final int REQUEST_CODE_FAVORITE = 1;
	
	/**
	 * Contains the current connection information and methods to access the
	 * CMIS repository.
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (activityIsCalledWithSearchAction()){
			Intent intent = new Intent(this, SearchActivity.class);
			
			intent.putExtras(getIntent());
			
			Server s = getRepository().getServer();
			intent.putExtra("server", s);
			intent.putExtra("title", s.getName());
			this.finish();
			startActivity(intent);
		} else {
			initWindow();
			initActionIcon();
			
			Bundle extra = this.getIntent().getExtras();
			if (extra != null && extra.getBoolean("isFirstStart")) {
				firstStart = true;
			}
			
			
			//Restart
			if (getLastNonConfigurationInstance() != null ){
				save = (ListCmisFeedActivitySave) getLastNonConfigurationInstance();
				this.item = save.getItem();
				this.itemParent = save.getItemParent();
				this.items = save.getItems();
				this.currentStack = save.getCurrentStack();
				firstStart = false;
				new FeedDisplayTask(this, getRepository(), null, item, items).execute();
			}
			
			if (initRepository() == false){
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
	
			//Set Default Breadcrumbs
			((TextView) activity.findViewById(R.id.path)).setText("/");
		
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
		if (item == null){
			item = getRepository().getRootItem();
			itemParent = item;
			currentStack.add(item);
		}
		return new ListCmisFeedActivitySave(item, itemParent, getItems(), currentStack);
	}
	
	@Override
	protected void onDestroy() {
		if (activityIsCalledWithSearchAction() == false){
			setSaveContext(null);
		}
		super.onDestroy();
	}
	
	private void initActionIcon() {
		Button home = (Button) findViewById(R.id.home);
		Button up = (Button) findViewById(R.id.up);
		Button back = (Button) findViewById(R.id.back);
		Button next = (Button) findViewById(R.id.next);
		Button refresh = (Button) findViewById(R.id.refresh);
		Button filter = (Button) findViewById(R.id.preference);
		
		
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
				goUP(false);
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getRepository().generateParams(activity, false);
				new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), item).execute(item.getDownLink());
			}
		});
		
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (item == null) {
					item = getRepository().getRootItem();
				}
				getRepository().generateParams(activity, true);
				new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), item).execute(item.getDownLink());
			}
		});
		
		filter.setOnClickListener(new OnClickListener() {
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
	
	public void processSearchOrDisplayIntent() {
		if (getRepository() != null) {
			//if (activityIsCalledWithSearchAction()) {
			//	doSearchWithIntent(getIntent());
			//} else {
				// Start this activity from favorite
				Bundle extras = getIntent().getExtras();
				if (extras != null) {
					if (extras.get("item") != null) {
						item = (CmisItemLazy) extras.get("item");
					}
				}
				new FeedDisplayTask(this, getRepository(), item).execute(item.getDownLink());
			//}
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
	/*private QueryType getQueryTypeFromIntent(Intent intent) {
		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			String queryType = appData.getString(QueryType.class.getName());
			return QueryType.valueOf(queryType);
		}
		return QueryType.FULLTEXT;
	}*/

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
		
		gridview.setOnItemClickListener(new CmisDocSelectedListener());
		gridview.setTextFilterEnabled(true);
		gridview.setClickable(true);
		gridview.setOnCreateContextMenuListener(this);

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
		
		if (item == null){
			item = getRepository().getRootItem();
		}
		getRepository().generateParams(activity, false);
		try {
			if (StorageUtils.deleteFeedFile(getApplication(), getRepository().getServer().getWorkspace(), item.getDownLink())){
				new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), item).execute(item.getDownLink());
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
			
			if (activityIsCalledWithSearchAction()){
				if (doc.hasChildren()) {
					ActionUtils.openNewListViewActivity(activity, doc);
				} else {
					ActionUtils.displayDocumentDetails(activity,  getRepository().getServer(), doc);
				}
			} else {
				if (doc.hasChildren()) {
					if (getRepository().isPaging()) {
				    	getRepository().setSkipCount(0);
				    	getRepository().generateParams(activity);
					}
					
					if (item == null || currentStack.size() == 0) {
						item = getRepository().getRootItem();
						currentStack.add(item);
					}
					
					currentStack.add(doc);
					
					new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), doc).execute(doc.getDownLink());
					
					itemParent = item;
					item = doc;
				} else {
					ActionUtils.displayDocumentDetails(ListCmisFeedActivity.this, doc);
				}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(Menu.NONE, 1, 0, R.string.menu_item_add_favorite);
		item.setIcon(R.drawable.favorite);
		createRepoMenu(menu);
		UIUtils.createSearchMenu(menu);
		createToolsMenu(menu);
		item = menu.add(Menu.NONE, 3, 0, R.string.menu_item_about);
		item.setIcon(R.drawable.cmisexplorer);
		
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
	
	private void createToolsMenu(Menu menu) {
		SubMenu toolsMenu = menu.addSubMenu(R.string.menu_item_tools);
		toolsMenu.setIcon(R.drawable.tools);
		toolsMenu.setHeaderIcon(android.R.drawable.ic_menu_info_details);
		
		if (getCmisPrefs().isEnableScan()){
			toolsMenu.add(Menu.NONE, 10, 0, R.string.menu_item_scanner);
		}
		toolsMenu.add(Menu.NONE, 12, 0, R.string.menu_item_download_manager);
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
			Intent intents = new Intent(this, SavedSearchActivity.class);
			intents.putExtra("server", getRepository().getServer());
			startActivity(intents);
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
		case 12:
			startActivity(new Intent(this, DownloadProgressActivity.class));
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
	    	
		/* super.onActivityResult(requestCode, resultCode, intent);

			switch (requestCode) {
			case REQUEST_CODE_FAVORITE:
				if (resultCode == RESULT_OK && intent != null) {
						
				}
				break;
			}*/
		 
		 
	    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		    if (scanResult != null && scanResult.getContents() != null)
		    	if (scanResult.getContents().length() > 0 && scanResult.getContents().contains("http://")) {
		    		if ( scanResult.getContents().contains(getRepository().getHostname())){
			    		new FeedItemDisplayTask(activity, getRepository().getServer(), scanResult.getContents()).execute();
			    	} else {
			    		ActionUtils.displayMessage(this, R.string.scan_error_repo);	
			    	}
		    	}else {
		    		ActionUtils.displayMessage(this, R.string.scan_error_url);
		    	}
		    }

	 public void goUP(boolean isBack){
		 
		 if (activityIsCalledWithSearchAction()){
				itemParent = null;
				currentStack.clear();
			}
		 
		 if (item != null && item.getPath() != null && item.getPath().equals("/") == false){
				if (getRepository().isPaging()) {
			    	getRepository().setSkipCount(0);
			    	getRepository().generateParams(activity);
				}
				if (itemParent != null) {
					currentStack.remove(item);
					item = currentStack.get(currentStack.size()-1);
					new FeedDisplayTask(ListCmisFeedActivity.this, getRepository(), itemParent).execute(itemParent.getDownLink());
					if (currentStack.size()-2 > 0){
						itemParent = currentStack.get(currentStack.size()-2);
					} else {
						itemParent = currentStack.get(0);
					}
				} else {
					new FeedItemDisplayTask(activity, getRepository().getServer(), item.getParentUrl(), FeedItemDisplayTask.DISPLAY_FOLDER).execute();
				}
			} else if (isBack) {
				Intent intent = new Intent(activity, ServerActivity.class);
				intent.putExtra("EXIT", false);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
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
	
	Prefs getCmisPrefs() {
		return ((CmisApp) getApplication()).getPrefs();
	}
	
	public CmisItemLazy getItem() {
		return item;
	}

	public void setItem(CmisItemLazy item) {
		this.item = item;
	}
	
	public ListCmisFeedActivitySave getSaveContext(){
		return ((CmisApp) getApplication()).getSavedContextItems();
	}
	
	public void setSaveContext(ListCmisFeedActivitySave save){
		((CmisApp) getApplication()).setSavedContextItems(save);
	}
	

}