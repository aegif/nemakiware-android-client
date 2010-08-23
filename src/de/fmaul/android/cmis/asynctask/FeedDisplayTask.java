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
package de.fmaul.android.cmis.asynctask;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.CmisItemCollectionAdapter;
import de.fmaul.android.cmis.GridAdapter;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.StorageException;

public class FeedDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private static final String TAG = "FeedDisplayTask";
	
	private final ListActivity activity;
	private final CmisRepository repository;
	private final String title;
	private String feedParams = "";
	private View layout;
	private CmisItemLazy item;
	private CmisItemCollection items;
	private ListView layoutListing;
	private Boolean isSearch;

	public FeedDisplayTask(ListActivity activity, CmisRepository repository) {
		this(activity, repository, null, null, null);
	}
	
	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title) {
		this(activity, repository, title, null, null);
	}
	
	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title, Boolean isSearch) {
		this(activity, repository, title, null, null);
		this.isSearch = isSearch;
	}
	
	public FeedDisplayTask(ListActivity activity, CmisRepository repository, CmisItemLazy item) {
		this(activity, repository, item.getTitle(), item, null);
	}
	
	public FeedDisplayTask(ListActivity activity, CmisRepository repository, CmisItemLazy item, CmisItemCollection items) {
		this(activity, repository, item.getTitle(), item, items);
	}

	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title, CmisItemLazy item,  CmisItemCollection items) {
		super();
		this.activity = activity;
		this.repository = repository;
		this.title = title;
		this.item = item;
		this.items = items;
		this.isSearch = false;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
		
		if (items == null && repository != null && repository.getUseFeedParams()){
				feedParams = repository.getFeedParams();
		}
		
		//Loading Animation
		layoutListing = activity.getListView();
		layoutListing.setVisibility(View.GONE);
		
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
				if (feed == null) {
					return repository.getRootCollection(feedParams);
				} else if (isSearch){
					return repository.getCollectionFromFeed(feed);
				} else {
					return repository.getCollectionFromFeed(feed + feedParams);
				}
			}
		} catch (FeedLoadException fle) {
			Log.d(TAG, fle.getMessage());
			ActionUtils.displayError(activity, R.string.generic_error);
			return CmisItemCollection.emptyCollection();
		} catch (StorageException e) {
			Log.d(TAG, e.getMessage());
			ActionUtils.displayError(activity, R.string.generic_error);
			return CmisItemCollection.emptyCollection();
		}
	}

	@Override
	protected void onPostExecute(CmisItemCollection itemCollection) {
		if (items == null){
			if (title != null){
				itemCollection.setTitle(title);
			} else {
				itemCollection.setTitle(item.getTitle());
			}
		}
		
		((CmisApp) activity.getApplication()).setItems(itemCollection);
		
		Prefs prefs = ((CmisApp) activity.getApplication()).getPrefs();
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			GridView gridview = (GridView) activity.findViewById(R.id.gridview);
		    gridview.setAdapter(new GridAdapter(activity, R.layout.feed_grid_row, itemCollection));
		} else {
			layoutListing.setAdapter(new CmisItemCollectionAdapter(activity, R.layout.feed_list_row, itemCollection));
		}
		
		
		//No Case
		Button back = ((Button)  activity.findViewById(R.id.back));
		Button next = ((Button)  activity.findViewById(R.id.next));
		
		String title_paging = "";
		
		if (repository.isPaging() == false){
			Log.d(TAG, "PAGING : NO");
			back.setVisibility(View.GONE);
			next.setVisibility(View.GONE);
		} else {
			//First Case
			if (repository.getSkipCount() == 0 && (repository.getSkipCount() + repository.getMaxItems()) >= repository.getNumItems()){
				Log.d(TAG, "PAGING : UNIQUE");
				back.setVisibility(View.GONE);
				next.setVisibility(View.GONE);
			} else if (repository.getSkipCount() == 0 && (repository.getSkipCount() + repository.getMaxItems()) < repository.getNumItems()){
				Log.d(TAG, "PAGING : FIRST");
				int nb_page = repository.getNumItems() > 0 ? ((int) Math.ceil((double) repository.getNumItems()/ (double) repository.getMaxItems())) : 0;
				title_paging =  " [1/" + nb_page + "]";  
				next.setVisibility(View.VISIBLE);
			} else if (repository.getSkipCount() != 0 && (repository.getSkipCount() + repository.getMaxItems())  >= repository.getNumItems()){
				int nb_page = repository.getNumItems() > 0 ? ((int) Math.ceil((double) repository.getNumItems()/ (double) repository.getMaxItems())) : 0;
				title_paging =  " [" + nb_page + "/" + nb_page + "]";
				Log.d(TAG, "PAGING : END");
				((Button) activity.findViewById(R.id.back)).setVisibility(View.VISIBLE);
			} else {
				int nb_page = repository.getNumItems() > 0 ? ((int) Math.ceil((double) repository.getNumItems()/ (double) repository.getMaxItems())) : 0;
				int currentPage = repository.getSkipCount() > 0 ? ((int) Math.floor((double) repository.getSkipCount()/ (double) repository.getMaxItems())) + 1 : 0;
				
				title_paging =  " [" + currentPage + "/" + nb_page + "]";  
				
				Log.d(TAG, "PAGING : MIDDLE");
				back.setVisibility(View.VISIBLE);
				next.setVisibility(View.VISIBLE);
			}
		}
		
		//TITLE
		//if (title == null) {
			activity.getWindow().setTitle(itemCollection.getTitle() + title_paging);
		//} else {
		//	activity.getWindow().setTitle(title + title_paging);
		//}
		
		activity.setProgressBarIndeterminateVisibility(false);
		
		layout.setVisibility(View.GONE);
		layoutListing.setVisibility(View.VISIBLE);
		
		if (itemCollection.getItems().size() == 0 ){
			activity.findViewById(R.id.empty).setVisibility(View.VISIBLE);
		}
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		
		if (item != null){
			((TextView) activity.findViewById(R.id.path)).setText(item.getPath());
		} else {
			((TextView) activity.findViewById(R.id.path)).setText("/");
		}
	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
	
}