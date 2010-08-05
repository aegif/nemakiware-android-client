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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.QueryType;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;

public class ListCmisFeedActivity extends ListActivity {

	private Prefs prefs;
	private List<String> workspaces;
	private CharSequence[] cs;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private Context context = this;

	/**
	 * Contains the current connection information and methods to access the
	 * CMIS repository.
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWindow();
		initRepository();
		processSearchOrDisplayIntent();
	}

	private void initRepository() {
		try {
			if (getRepository() == null) {
				prefs = new Prefs(this);
				setRepository(CmisRepository.create(getApplication(), prefs));
				getRepository().clearCache();
			} else {
				Bundle extra = this.getIntent().getExtras();
				if (extra != null && extra.getBoolean("isFirstStart")){
					prefs = new Prefs(this);
					setRepository(CmisRepository.create(getApplication(), prefs));
					getRepository().clearCache();
				}
			}
		} catch (FeedLoadException fle) {
		}
	}
	
	private void processSearchOrDisplayIntent() {
		if (getRepository() != null) {
			if (activityIsCalledWithSearchAction()) {
				doSearchWithIntent(getIntent());
			} else {
				// display the feed that is passed in through the intent
				String feed = getFeedFromIntent();
				String title = getTitleFromIntent();
				displayFeedInListView(feed, title);
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
	 * Initialze the window and the activity.
	 */
	private void initWindow() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getListView().setTextFilterEnabled(true);
		getListView().setItemsCanFocus(true);
		getListView().setClickable(true);
		getListView().setOnItemClickListener(new CmisDocSelectedListener());
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		CmisItem doc = (CmisItem) getListView().getItemAtPosition(menuInfo.position);

		switch (menuItem.getItemId()) {
		case 1:
			if (doc != null && doc.hasChildren() == false) {
				openDocument(doc);
			}
			return true;
		case 2:
			if (doc != null) {
				displayDocumentDetails(doc);
			}
			return true;
		case 3:
			if (doc != null) {
				emailDocument(doc);
			}
			return true;
		default:
			return super.onContextItemSelected(menuItem);
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
		
		if (doc != null && doc.hasChildren() == false){
			menu.add(0, 1, Menu.NONE, getString(R.string.download));
			menu.add(0, 3, Menu.NONE, getString(R.string.menu_item_share));
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
		displayFeedInListView(searchFeed, getString(R.string.search_results_for) + " '" + queryString + "'");
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

	/*
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		if (getRepository() == null) {
			initRepository();
			displayFeedInListViewWithTitleFromFeed(null);
		}
	}

	/**
	 * Displays the cmis feed given as in the list asynchronously
	 * 
	 * @param feed
	 */
	private void displayFeedInListView(final String feed, String title) {
		setTitle(R.string.loading);
		new FeedDisplayTask(this, getRepository(), title).execute(feed);
	}

	private void displayFeedInListViewWithTitleFromFeed(final String feed) {
		setTitle(R.string.loading);
		new FeedDisplayTask(this, getRepository()).execute(feed);
	}

	private void displayError(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Opens a file by downloading it and starting the associated app.
	 * 
	 * @param item
	 */
	private void openDocument(final CmisItem item) {

		new AbstractDownloadTask(getRepository(), this) {
			@Override
			public void onDownloadFinished(File contentFile) {
				if (contentFile != null && contentFile.exists()) {
					viewFileInAssociatedApp(contentFile, item.getMimeType());
				} else {
					displayError(R.string.error_file_does_not_exists);
				}
			}
		}.execute(item);

	}

	/**
	 * Displays a file on the local system with the associated app by calling
	 * the ACTION_VIEW intent.
	 * 
	 * @param tempFile
	 * @param mimeType
	 */
	private void viewFileInAssociatedApp(File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
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
				openNewListViewActivity(doc);
			} else {
				//openDocument(doc);
				displayDocumentDetails(doc);
			}
		}
	}

	private void displayDocumentDetails(CmisItem doc) {
		Intent intent = new Intent(ListCmisFeedActivity.this, DocumentDetailsActivity.class);

		ArrayList<CmisProperty> propList = new ArrayList<CmisProperty>(doc.getProperties().values());
		
		intent.putParcelableArrayListExtra("properties", propList);
		intent.putExtra("title", doc.getTitle());
		intent.putExtra("mimetype", doc.getMimeType());
		intent.putExtra("objectTypeId", doc.getProperties().get("cmis:objectTypeId").getValue());
		intent.putExtra("baseTypeId", doc.getProperties().get("cmis:baseTypeId").getValue());
		intent.putExtra("contentUrl", doc.getContentUrl());
		startActivity(intent);
	}

	/**
	 * Opens a feed url in a new listview. This enables the user to use the
	 * backbutton to get back to the previous list (usually the parent folder).
	 * 
	 * @param item
	 */
	private void openNewListViewActivity(CmisItem item) {
		Intent intent = new Intent(this, ListCmisFeedActivity.class);
		if (CmisItemCollection.FEED_UP.equals(item.getDownLink())){
			finish();
		} else {
			intent.putExtra("feed", item.getDownLink());
			intent.putExtra("title", item.getTitle());
			startActivity(intent);
		}
	}

	private void emailDocument(final CmisItem item) {

		new AbstractDownloadTask(getRepository(), this) {
			@Override
			public void onDownloadFinished(File contentFile) {
				if (contentFile != null && contentFile.exists()) {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
					i.putExtra(Intent.EXTRA_TEXT, item.getContentUrl());
					i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
					i.setType(item.getMimeType());
					startActivity(Intent.createChooser(i, "Email file"));
				} else {
					displayError(R.string.error_file_does_not_exists);
				}
			}
		}.execute(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		createRepoMenu(menu);
		
		MenuItem aboutItem = menu.add(Menu.NONE, 2, 0, R.string.menu_item_about);
		aboutItem.setIcon(R.drawable.cmisexplorer);

		createSearchMenu(menu);
		return true;

	}

	private void createRepoMenu(Menu menu) {
		SubMenu settingsMenu = menu.addSubMenu(Menu.NONE, 1, 0, R.string.menu_item_settings);
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
		case 2:
			//Toast.makeText(this, R.string.about_message, 5).show();
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
			setRepository(null);
			onRestart();
			return true;
		case 8:
			startActivity(new Intent(this, ServerActivity.class));
			return true;
		case 9:
			chooseWorkspace();
			return true;
		}

		return false;
	}
	
	private void chooseWorkspace(){
		try {
			workspaces = FeedUtils.getRootFeedsFromRepo(prefs.getUrl(), prefs.getUser(), prefs.getPassword());
			cs = workspaces.toArray(new CharSequence[workspaces.size()]);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.cmis_repo_choose_workspace);
			builder.setSingleChoiceItems(cs, -1 ,new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
					preferences = PreferenceManager.getDefaultSharedPreferences(context);
					editor = preferences.edit();
			    	editor.putString("workspace", cs[item].toString());
					editor.commit();
			        dialog.dismiss();
			        setRepository(null);
					onRestart();
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		} catch (Exception e) {
			Toast.makeText(ListCmisFeedActivity.this, R.string.error_repo_connexion, Toast.LENGTH_LONG).show();
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

}