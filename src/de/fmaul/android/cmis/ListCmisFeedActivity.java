package de.fmaul.android.cmis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.QueryType;

public class ListCmisFeedActivity extends ListActivity {

	CmisRepository repository;
	
	QueryType queryType = QueryType.FULLTEXT;
		

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWindow();

		if (repository == null) {
			Prefs prefs = new Prefs(this);
			repository = CmisRepository.create(prefs); 
		}
		
		if (activityIsCalledWithSearchAction()) {
			doSearchWithIntent(getIntent());
		} else {
			String feed = getFeedFromIntent();
			displayFeedInListView(feed);
		}
	}

	private QueryType getQueryTypeFromIntent(Intent intent) {
		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			String queryType = appData.getString(QueryType.class.getName());
			return QueryType.valueOf(queryType);
		 }
		return QueryType.FULLTEXT;
	}

	private boolean activityIsCalledWithSearchAction() {
		final String queryAction = getIntent().getAction();
		return Intent.ACTION_SEARCH.equals(queryAction);
	}

	private void initWindow() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		getListView().setTextFilterEnabled(true);
		getListView().setItemsCanFocus(true);
		getListView().setClickable(true);
		getListView().setOnItemClickListener(new CmisDocSelectedListener());
	}

	private void doSearchWithIntent(final Intent queryIntent) {
		final String queryString = queryIntent
				.getStringExtra(SearchManager.QUERY);
		
		QueryType queryType = getQueryTypeFromIntent(queryIntent);
		String searchFeed = repository.getSearchFeed(queryType, queryString);
		displayFeedInListView(searchFeed);
	}

	private String getFeedFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.get("feed") != null) {
				return extras.get("feed").toString();
			}
		}
		return null;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		displayFeedInListView(null);
	}

	private void displayFeedInListView(final String feed) {
		setTitle("loading...");
		new FeedDisplayTask(this, repository).execute(feed);
	}

	private void downloadContent(OutputStream os, String contentUrl) {
		try {
			repository.fetchContent(contentUrl, os);
			os.close();
		} catch (Exception e) {
			Toast.makeText(this, R.string.error_downloading_content,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void openDocument(CmisItem doc) {
		try {
			OutputStream os = openFileOutput(doc.getTitle(),
					MODE_WORLD_READABLE);

			downloadContent(os, doc.getContentUrl());
		} catch (FileNotFoundException fnfe) {
			Toast.makeText(this, R.string.error_file_does_not_exists,
					Toast.LENGTH_SHORT).show();
		}

		File tempFile = new File(getFilesDir(), doc.getTitle());
		if (!tempFile.exists()) {
			Toast.makeText(this, R.string.error_file_does_not_exists,
					Toast.LENGTH_SHORT).show();
			return;
		}

		viewFileInAssociatedApp(tempFile, doc.getMimeType());
	}

	private void viewFileInAssociatedApp(File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.application_not_available,
					Toast.LENGTH_SHORT).show();
		}
	}

	private class CmisDocSelectedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CmisItem doc = (CmisItem) parent.getItemAtPosition(position);

			if (doc.hasChildren()) {
				openNewListViewActivity(doc);
			} else {
				openDocument(doc);
			}
		}
	}

	private void openNewListViewActivity(CmisItem item) {
		Intent intent = new Intent(this, ListCmisFeedActivity.class);
		intent.putExtra("feed", item.getDownLink());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem settingsItem = menu.add(Menu.NONE, 1, 0, "Settings");
		settingsItem.setIcon(android.R.drawable.ic_menu_edit);
		MenuItem aboutItem = menu.add(Menu.NONE, 2, 0, "About");
		aboutItem.setIcon(android.R.drawable.ic_menu_info_details);
		//MenuItem searchItem = menu.add(Menu.NONE, 3, 0, "Search");

		SubMenu searchMenu = menu.addSubMenu("Search");
		searchMenu.setIcon(android.R.drawable.ic_menu_search);
		searchMenu.getItem().setAlphabeticShortcut(SearchManager.MENU_KEY);

		searchMenu.add(Menu.NONE, 4, 0, "Search by title");
		searchMenu.add(Menu.NONE, 5, 0, "Fulltext search");
		searchMenu.add(Menu.NONE, 6, 0, "CMIS query");

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(this, CmisPreferences.class));
			return true;
		case 2:
			Toast.makeText(this, "CMIS Browser by Florian Maul (2010)", 5)
					.show();
			return true;
		case 4:
			queryType = QueryType.TITLE;
			onSearchRequested();
			return true;
		case 5:
			queryType = QueryType.FULLTEXT;
			onSearchRequested();
			return true;
		case 6:
			queryType = QueryType.CMISQUERY;
			onSearchRequested();
			return true;
		default:
			Toast.makeText(this, "unknown menu item.", 5).show();
		}

		return false;
	}
	
	 @Override
	 public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     appData.putString(QueryType.class.getName(), queryType.name());
	     startSearch("", false, appData, false);
	     return true;
	 }

}