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

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FilterPrefs {

	private final Activity activity;

	public FilterPrefs(Activity activity) {
		this.activity = activity;
	}
	
	public String getMaxItems() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_maxitems), "0");
	}

	public String getFilter() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_filter), "");
	}

	public String getTypes() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_types), "");
	}

	public String getOrder() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_orderby), "");	
	}
	
	public Boolean getPaging () {
		return getPrefs().getBoolean(activity.getString(R.string.cmis_repo_paging), true);	
	}
	
	public int getSkipCount () {
		return getPrefs().getInt(activity.getString(R.string.cmis_repo_skipcount), 0);	
	}
	
	public Boolean getParams() {
		return getPrefs().getBoolean(activity.getString(R.string.cmis_repo_params), false);	
	}

	private SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(activity);
	}
}
