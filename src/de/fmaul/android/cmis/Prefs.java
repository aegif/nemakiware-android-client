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

public class Prefs {

	private final Activity activity;

	public Prefs(Activity activity) {
		this.activity = activity;
	}

	public String getUrl() {
		return getPrefs().getString("serverURL", "vide");
	}

	public String getUser() {
		return getPrefs().getString("username", "vide");
	}

	public String getPassword() {
		return getPrefs().getString("password", "vide");
	}

	private SharedPreferences getPrefs() {
		PreferenceManager.setDefaultValues(activity, R.xml.preferences, false); 
		return PreferenceManager.getDefaultSharedPreferences(activity);
	}
}
