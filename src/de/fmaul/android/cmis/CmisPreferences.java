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

import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.StorageException;
import de.fmaul.android.cmis.utils.StorageUtils;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class CmisPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		//setContentView(R.layout.feed_list_main);
		
		( (CheckBoxPreference) (getPreferenceManager().findPreference("cache"))).setChecked(false);
		getPreferenceManager().findPreference("cache").setEnabled(true);
		
		getPreferenceManager().findPreference("cache").setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference){
		    	try {
					StorageUtils.deleteCacheFolder(CmisPreferences.this.getApplication());
				} catch (StorageException e) {
					ActionUtils.displayError(CmisPreferences.this, R.string.generic_error);
				}
		    	preference.setEnabled(false);
		    	return true;
		    }
		});
		
		
		getPreferenceManager().findPreference("default_view").setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference){
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setDataView(Integer.parseInt(((ListPreference) preference).getValue()));
		    	return true;
		    }
		});
		
		getPreferenceManager().findPreference("download_folder").setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference){
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setDownloadFolder(((EditTextPreference) preference).getText());
		    	return true;
		    }
		});
		
	}
	
}