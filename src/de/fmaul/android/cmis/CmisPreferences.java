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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.StorageException;
import de.fmaul.android.cmis.utils.StorageUtils;

public class CmisPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		( (CheckBoxPreference) (getPreferenceManager().findPreference(this.getText(R.string.cmis_cache)))).setChecked(false);
		getPreferenceManager().findPreference(this.getText(R.string.cmis_cache)).setEnabled(true);
		
		getPreferenceManager().findPreference(this.getText(R.string.cmis_cache)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference){
		    	try {
					StorageUtils.deleteCacheFolder(CmisPreferences.this.getApplication());
				} catch (StorageException e) {
					ActionUtils.displayMessage(CmisPreferences.this, R.string.generic_error);
				}
		    	preference.setEnabled(false);
		    	return true;
		    }
		});
		
		
		getPreferenceManager().findPreference("cmis_defaultview").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setDataView(Integer.parseInt((String) newValue));
		    	((ListPreference) preference).setValue((String) newValue);
				return false;
			}
		});
		
		getPreferenceManager().findPreference(this.getText(R.string.cmis_dlfolder)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setDownloadFolder((String) newValue);
		    	return true;
		    }
		});
		
		getPreferenceManager().findPreference(this.getText(R.string.cmis_scan)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setEnableScan((Boolean) newValue);
		    	return true;
		    }
		});
		
		getPreferenceManager().findPreference(this.getText(R.string.cmis_download)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setConfirmDownload((Boolean) newValue);
		    	return true;
		    }
		});
		
		getPreferenceManager().findPreference(this.getText(R.string.cmis_download_size)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		    	((CmisApp) CmisPreferences.this.getApplication()).getPrefs().setDownloadFileSize((String) newValue);
		    	return true;
		    }
		});
		
	}
	
}