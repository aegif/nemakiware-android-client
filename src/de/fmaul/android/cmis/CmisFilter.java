package de.fmaul.android.cmis;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CmisFilter extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.filter);
	}
	
}