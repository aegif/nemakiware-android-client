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
package jp.aegif.android.cmis.asynctask;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import jp.aegif.android.cmis.CmisApp;
import jp.aegif.android.cmis.CmisItemCollectionAdapter;
import jp.aegif.android.cmis.GridAdapter;
import jp.aegif.android.cmis.Prefs;
import jp.aegif.android.cmis.R;
import jp.aegif.android.cmis.repo.CmisItemCollection;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.utils.ActionUtils;
import jp.aegif.android.cmis.utils.FeedLoadException;
import jp.aegif.android.cmis.utils.StorageException;

public class SearchDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private static final String TAG = "SearchDisplayTask";
	
	private final ListActivity activity;
	private final CmisRepository repository;
	private final String queryString;
	private View layout;
	private CmisItemCollection items;
	private ListView layoutListing;
	private GridView layoutGrid;
	private Boolean errorOnExit = false;

	public SearchDisplayTask(ListActivity activity, CmisRepository repository, String queryString) {
		this(activity, repository, queryString, null);
	}
	
	public SearchDisplayTask(ListActivity activity, CmisRepository repository, String queryString,  CmisItemCollection items) {
		super();
		this.activity = activity;
		this.repository = repository;
		this.queryString = queryString;
		this.items = items;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
		
		//Hide Data during Animation
		layoutListing = activity.getListView();
		
		layoutListing.setVisibility(View.GONE);
		activity.findViewById(R.id.empty).setVisibility(View.GONE);
		
		//Setting TITLE
		activity.getWindow().setTitle(repository.getServer().getName() + " > " + activity.getString(R.string.search_progress));
		
		//Setting Breadcrumb
		((TextView) activity.findViewById(R.id.path)).setText(">");
		
		//Loading Animation
		layout = activity.findViewById(R.id.animation);
		layout.setVisibility(View.VISIBLE);
		View objet = activity.findViewById(R.id.transfert);
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.download);
        objet.startAnimation(animation);
		
	}

	@Override
	protected CmisItemCollection doInBackground(String... params) {
		try {
			if (items != null){
				return items;
			} else {
				String feed = params[0];
				return repository.getCollectionFromFeed(feed);
			}
		} catch (FeedLoadException fle) {
			Log.d(TAG, fle.getMessage());
			errorOnExit = true;
			return CmisItemCollection.emptyCollection();
		} catch (StorageException e) {
			Log.d(TAG, e.getMessage());
			errorOnExit = true;
			return CmisItemCollection.emptyCollection();
		}
	}

	@Override
	protected void onPostExecute(CmisItemCollection itemCollection) {
		itemCollection.setTitle(queryString);
		
		if (errorOnExit){
			ActionUtils.displayMessage(activity, R.string.generic_error);
		}
		
		((CmisApp) activity.getApplication()).setItems(itemCollection);
		
		Prefs prefs = ((CmisApp) activity.getApplication()).getPrefs();
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			GridView gridview = (GridView) activity.findViewById(R.id.gridview);
		    gridview.setAdapter(new GridAdapter(activity, R.layout.feed_grid_row, itemCollection));
		} else {
			layoutListing.setAdapter(new CmisItemCollectionAdapter(activity, R.layout.feed_list_row, itemCollection));
		}
		
		//Setting TITLE
		activity.getWindow().setTitle(repository.getServer().getName() + " > " + activity.getString(R.string.menu_item_search));
		
		//Setting BreadCrumb
		((TextView) activity.findViewById(R.id.path)).setText(
				itemCollection.getItems().size() + " " 
				+ activity.getString(R.string.search_results_for) + " "
				+ queryString);
		
		//Show Data & Hide  Animation
		prefs = ((CmisApp) activity.getApplication()).getPrefs();
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			layoutGrid.setVisibility(View.VISIBLE);
		} else {
			layoutListing.setVisibility(View.VISIBLE);
		}
		layout.setVisibility(View.GONE);
		activity.setProgressBarIndeterminateVisibility(false);
		
		
		if (itemCollection.getItems().size() == 0 ){
			activity.findViewById(R.id.empty).setVisibility(View.VISIBLE);
		} else {
			activity.findViewById(R.id.empty).setVisibility(View.GONE);
		}
		
		//Allow screen rotation
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
	
}