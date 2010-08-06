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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class FeedDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private final ListActivity activity;
	private final CmisRepository repository;
	private final String title;
	private Prefs pref;
	private String feedParams = "";

	public FeedDisplayTask(ListActivity activity, CmisRepository repository) {
		this(activity, repository, null, null);
	}

	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title, Prefs pref) {
		super();
		this.activity = activity;
		this.repository = repository;
		this.title = title;
		this.pref = pref;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
		if (pref != null && pref.getParams()){
			feedParams = createParams();
		}
	}
	
	//TODO better
	private String createParams(){
		String params = "?";
		
		if (pref != null){
			if (pref.getTypes() != null && pref.getTypes().length() > 0){
				params += "types" + "=" +  pref.getTypes();
				params += "&";
			}
			/*
			if (pref.getFilter() != null){
				paramsList.put("filter", pref.getFilter());
			}*/
			if (pref.getMaxItems() != null && pref.getMaxItems().equals("-1") == false){
				params += "maxItems" + "=" + pref.getMaxItems();
				params += "&";
			}
			if (pref.getOrder() != null){
				params += "orderBy" + "=" + pref.getOrder();
			}
			
			if (params.length() == 1){
				params = "";
			}
			
		}
		
		String param = "";
		try {
			param = new URI(null, params, null).toASCIIString();
		} catch (URISyntaxException e) {
		}
		
		return param;
	}

	@Override
	protected CmisItemCollection doInBackground(String... params) {
		try {
			String feed = params[0];
			if (feed == null) {
				return repository.getRootCollection(feedParams);
			} else {
				return repository.getCollectionFromFeed(feed + feedParams);
			}
		} catch (FeedLoadException fle) {
			return CmisItemCollection.emptyCollection();
		}
	}

	@Override
	protected void onPostExecute(CmisItemCollection itemCollection) {
		activity.setListAdapter(new CmisItemCollectionAdapter(activity, R.layout.feed_list_row, itemCollection));
		if (title == null) {
			activity.getWindow().setTitle(itemCollection.getTitle());
		} else {
			activity.getWindow().setTitle(title);
		}
		activity.setProgressBarIndeterminateVisibility(false);

	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}