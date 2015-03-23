package de.fmaul.android.cmis;

import java.io.File;
import java.util.ArrayList;

import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.utils.ActionUtils;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

public class OpenFileActivity extends Activity {


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       
    	try{
    		File file = new File(getIntent().getStringExtra("path"));
    		String mimeType = getIntent().getStringExtra("mimeType");
    		ActionUtils.viewFileInAssociatedApp(this, file, mimeType);
    	} catch (Exception e) {
    		this.finish();
    	}
    	//this.finish();
    }
}