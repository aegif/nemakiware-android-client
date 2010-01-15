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
		return getPrefs().getString(activity.getString(R.string.cmis_repo_server_id), "http://cmis.alfresco.com/service/cmis");
	}

	public String getUser() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_user_id), "admin");
	}

	public String getPassword() {
		return getPrefs().getString(activity.getString(R.string.cmis_repo_password_id), "admin");
	}

	private SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(activity);
	}
	
}
