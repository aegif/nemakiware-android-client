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

import android.app.ListActivity;
import android.os.AsyncTask;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class FeedDisplayTask extends AsyncTask<String, Void, CmisItemCollection> {

	private final ListActivity activity;
	private final CmisRepository repository;
	private final String title;

	public FeedDisplayTask(ListActivity activity, CmisRepository repository) {
		this(activity, repository, null);
	}

	public FeedDisplayTask(ListActivity activity, CmisRepository repository, String title) {
		super();
		this.activity = activity;
		this.repository = repository;
		this.title = title;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected CmisItemCollection doInBackground(String... params) {
		try {
			String feed = params[0];
			if (feed == null) {
				return repository.getRootCollection();
			}
			else {
				return repository.getCollectionFromFeed(feed);
			}
		} catch (FeedLoadException fle) {
			return CmisItemCollection.emptyCollection();
			//FIXME eror handling
		}
	}

	@Override
	protected void onPostExecute(CmisItemCollection itemCollection) {
		activity.setListAdapter(new CmisItemCollectionAdapter(activity, R.layout.feed_list_row,
				itemCollection));
		if (title == null) {
			activity.getWindow().setTitle(itemCollection.getTitle());
		}
		else {
			activity.getWindow().setTitle(title);
		}
		activity.setProgressBarIndeterminateVisibility(false);

	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}