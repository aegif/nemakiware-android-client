package de.fmaul.android.cmis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import de.fmaul.android.cmis.repo.CmisItem;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileChooserActivity extends ListActivity {

    private final String t = "File Chooser";

    protected ArrayList<File> mFileList;
    protected File mRoot;
    protected File parent;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filelister);
        parent = new File("/");
        initialize("toto", "/sdcard");
        
    }


    public void initialize(String title, String path) {
        setTitle(getString(R.string.app_name) + " > " + title);
        mFileList = new ArrayList<File>();
        if (getDirectory(path)) {
            getFiles(mRoot);
            displayFiles();
        }
    }

    private boolean getDirectory(String path) {

        TextView tv = (TextView) findViewById(R.id.filelister_message);
        
        // check to see if there's an sd card.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            tv.setText("Error");
            return false;
        }
        
        // if storage directory does not exist, create it.
        mRoot = new File(path);
        if (!mRoot.exists()) {
        	 return false;
        }
        
        return true;
    }

    private void getFiles(File f) {
    	mFileList.clear();
    	mFileList.add(parent);
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
            	mFileList.add(child);
            }
        }
    }

    private void displayFiles() {
        ArrayAdapter<File> fileAdapter;

        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileAdapter = new FileAdapter(this, R.layout.file_list_row, mFileList, parent);
        setListAdapter(fileAdapter);
    }
    
    
    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = (File) l.getItemAtPosition(position);
		
		if (file != null){
			if (file.isDirectory()) {
				setListAdapter(null);
				if (file.getParent() != null){
					parent = new File(file.getParent());
				} else {
					parent = null;
				}
				initialize(file.getName(), file.getPath());
			} else {
				Intent i = new Intent();
				i.putExtra("path", file.getPath());
				setResult(RESULT_OK, i);
				finish();
			}
		}
    }
}