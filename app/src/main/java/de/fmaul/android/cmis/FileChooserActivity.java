package de.fmaul.android.cmis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.fmaul.android.cmis.asynctask.CopyFilesTask;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FileSystemUtils;
import de.fmaul.android.cmis.utils.StorageException;
import de.fmaul.android.cmis.utils.StorageUtils;

public class FileChooserActivity extends ListActivity {

    private static final int EDIT_ACTION = 0;

	private final String TAG = "File Chooser";

    protected ArrayList<File> mFileList;
    protected File mRoot;
    protected File parent;

	private File file;

	private ListView listView;

	private EditText input;
	private Boolean multipleMode = false;
	
	private static final int MENU_NEW_FOLDER = Menu.FIRST + 1;
	
	private static final int DIALOG_NEW_FOLDER = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_RENAME = 3;
	private static final int DIALOG_ABOUT = 4;
	private static final int DIALOG_ORDER = 5;
	
	private static final String SDCARD = "sdcard";
	
	private int sort = SORT_ALPHA;

	private ArrayList<String> sortingValueLabel = new ArrayList<String>(3);
	private ArrayList<Integer> sorting = new ArrayList<Integer>(3);

	private static final int SORT_ALPHA = R.string.action_sorting_name;
	private static final int SORT_SIZE = R.string.action_sorting_size;
	private static final int SORT_DATE = R.string.action_sorting_date;

	private ArrayList<File> copiedFiles = new ArrayList<File>();
	private ArrayList<File> cutFiles = new ArrayList<File>();
	private ArrayList<File> selectionFiles = new ArrayList<File>();

	private Button home;
	private Button up;
	private Button filter;
	private Button paste;
	private Button clear;
	private Button order;
	private Button copy;
	private Button cut;
	private Button delete;
	private Button cancel;
	private Button multiple;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list_main);
        
        //Filter Init
        initSorting();
        initSortingLabel();
        
        
        listView = this.getListView();
        listView.setOnCreateContextMenuListener(this);
        try {
			file = StorageUtils.getDownloadRoot(this.getApplication());
		} catch (StorageException e) {
		}
		setTitle(getString(R.string.app_name));
		initActionIcon();
		if (getLastNonConfigurationInstance() != null ){
			file = (File) getLastNonConfigurationInstance();
		}
		
		parent = new File(file.getParent());
		
		initialize("Download", file);
    }
    private AlertDialog createDialog(int title, int message, String defaultValue, DialogInterface.OnClickListener positiveClickListener){
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(message);
		input = new EditText(this);
		input.setText(defaultValue);
		alert.setView(input);
		alert.setPositiveButton(R.string.validate, positiveClickListener);

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		return alert.create();
    }
    
    private void initActionIcon() {
		 home = (Button) findViewById(R.id.home);
		 up = (Button) findViewById(R.id.up);
		 filter = (Button) findViewById(R.id.preference);
		 order = (Button) findViewById(R.id.order);
		 paste = (Button) findViewById(R.id.paste);
		 clear = (Button) findViewById(R.id.clear);
		 copy = (Button) findViewById(R.id.copy);
		 cut = (Button) findViewById(R.id.cut);
		 delete = (Button) findViewById(R.id.delete);
		 cancel = (Button) findViewById(R.id.cancel);
		 multiple = (Button) findViewById(R.id.multiple);
		
		multiple.setVisibility(View.GONE);
		filter.setVisibility(View.GONE);
		paste.setVisibility(View.GONE);
		clear.setVisibility(View.GONE);
		copy.setVisibility(View.GONE);
		cut.setVisibility(View.GONE);
		delete.setVisibility(View.GONE);
		cancel.setVisibility(View.GONE);
		
		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(FileChooserActivity.this, HomeActivity.class);
				intent.putExtra("EXIT", false);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		order.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ORDER);
			}
		});
		
		up.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goUp(false);
			}
		});
		
		paste.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (File fileToMove : cutFiles) {
					FileSystemUtils.rename(mRoot, fileToMove);
				}
				new CopyFilesTask(FileChooserActivity.this, copiedFiles, cutFiles, mRoot).execute();
			}
		});
		
		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				copiedFiles.clear();
				cutFiles.clear();
				paste.setVisibility(View.GONE);
				clear.setVisibility(View.GONE);
				ActionUtils.displayMessage(FileChooserActivity.this, R.string.action_clear_success);
			}
		});
		
		copy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				copy.setVisibility(View.GONE);
				cut.setVisibility(View.GONE);
				paste.setVisibility(View.GONE);
				clear.setVisibility(View.GONE);
				delete.setVisibility(View.GONE);
				multipleMode = false;
				copiedFiles.addAll(selectionFiles);
				selectionFiles.clear();
			}
		});
		
		cut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				copy.setVisibility(View.GONE);
				cut.setVisibility(View.GONE);
				paste.setVisibility(View.GONE);
				clear.setVisibility(View.GONE);
				delete.setVisibility(View.GONE);
				multipleMode = false;
				cutFiles.addAll(selectionFiles);
				selectionFiles.clear();
			}
		});
		
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				copy.setVisibility(View.GONE);
				cut.setVisibility(View.GONE);
				paste.setVisibility(View.GONE);
				clear.setVisibility(View.GONE);
				delete.setVisibility(View.GONE);
				multipleMode = false;
				for (File file : selectionFiles) {
					FileSystemUtils.delete(file);
				}
			}
		});
		
		multiple.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (multipleMode){
					copy.setVisibility(View.GONE);
					cut.setVisibility(View.GONE);
					paste.setVisibility(View.GONE);
					delete.setVisibility(View.GONE);
					multipleMode = false;
					initialize(mRoot.getName(), mRoot);
				} else {
					copy.setVisibility(View.VISIBLE);
					cut.setVisibility(View.VISIBLE);
					paste.setVisibility(View.VISIBLE);
					clear.setVisibility(View.GONE);
					delete.setVisibility(View.VISIBLE);
					multipleMode = true;
				}
			}
		});
		
	}
    

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	//goUp(true);
        	this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
	public Object onRetainNonConfigurationInstance() {
		return file;
	}
    
    public void initialize(String title, File file) {
        ((TextView) this.findViewById(R.id.path)).setText(file.getPath());
        mFileList = new ArrayList<File>();
        if (getDirectory(file)) {
            getFiles(mRoot);
            displayFiles();
        }
    }
    
    private boolean getDirectory(File file) {
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
    	
    	mRoot = file;
        if (!mRoot.exists()) {
        	 return false;
        }
        return true;
    }


    private void getFiles(File f) {
    	mFileList.clear();
    	//mFileList.add(parent);
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
            	mFileList.add(child);
            }
        }
    }

    private void displayFiles() {
        ArrayAdapter<File> fileAdapter;
        
        switch (sort) {
		case SORT_ALPHA:
			Collections.sort(mFileList, new DirAlphaComparator()); 
			break;
		case SORT_DATE:
			Collections.sort(mFileList, new DirDateComparator()); 
			break;
		case SORT_SIZE:
			Collections.sort(mFileList, new DirSizeComparator()); 
			break;
		default:
			break;
		}
        

        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        fileAdapter = new FileAdapter(this, R.layout.file_list_row, mFileList, parent);
        setListAdapter(fileAdapter);
    }
    
    
    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    	file = (File) l.getItemAtPosition(position);
    	
    	if (SDCARD.equals(file.getName())){
    		up.setVisibility(View.GONE);
    	} else {
    		up.setVisibility(View.VISIBLE);
    	}
    	
    	int c;
    	if (multipleMode){
        	if (selectionFiles.contains(file)){
        		selectionFiles.remove(file);
        		c = Color.BLUE;
        	} else {
        		selectionFiles.add(file);
        		c = Color.DKGRAY;
        	}
        	v.setBackgroundColor(c);
        	l.getChildAt(position).refreshDrawableState();
    	} else {
			if (file != null){
				if (file.isDirectory()) {
					setListAdapter(null);
					if (file.getParent() != null){
						parent = new File(file.getParent());
					} else {
						parent = null;
					}
					initialize(file.getName(), file);
				} else {
					ActionUtils.openDocument(this, file);
				}
			}
    	}
    }
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(this.getString(R.string.feed_menu_title));
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		File file = (File) getListView().getItemAtPosition(info.position);
		
		if (file.isFile()){
			menu.add(0, 0, Menu.NONE, getString(R.string.share));
			menu.add(0, 1, Menu.NONE, getString(R.string.open));
			menu.add(0, 2, Menu.NONE, getString(R.string.open_with));
			menu.add(0, 5, Menu.NONE, getString(R.string.copy));
			menu.add(0, 6, Menu.NONE, getString(R.string.cut));
		} else {
			menu.add(0, 6, Menu.NONE, getString(R.string.move));
		}
		menu.add(0, 3, Menu.NONE, getString(R.string.rename));
		menu.add(0, 4, Menu.NONE, getString(R.string.delete));
	}
    
    @Override
	public boolean onContextItemSelected(MenuItem menuItem) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		file = (File) getListView().getItemAtPosition(menuInfo.position);
		
		switch (menuItem.getItemId()) {
		
		case 0:
			ActionUtils.shareFileInAssociatedApp(this, file);
			return true;
		case 1:
			ActionUtils.openDocument(this, file);
			return true;
		case 2:
			ActionUtils.openWithDocument(this, file);
			return true;
		case 3:
			showDialog(DIALOG_RENAME);
			return true;
		case 4:
			showDialog(DIALOG_DELETE);
			return true;
		case 5:
			copiedFiles.add(file);
			ActionUtils.displayMessage(this, R.string.action_clipboard_add);
			this.findViewById(R.id.paste).setVisibility(View.VISIBLE);
			this.findViewById(R.id.clear).setVisibility(View.VISIBLE);
			return true;
		case 6:
			cutFiles.add(file);
			ActionUtils.displayMessage(this, R.string.action_clipboard_add);
			this.findViewById(R.id.paste).setVisibility(View.VISIBLE);
			this.findViewById(R.id.clear).setVisibility(View.VISIBLE);
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_ACTION:
                try {
                    String value = data.getStringExtra("value");
                    if (value != null && value.length() > 0) {
                        //do something with value
                    }
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
    }
    
    
    private void goUp(Boolean exit){
    	if (SDCARD.equals(parent.getName())){
    		up.setVisibility(View.GONE);
    	}
    	
    	if (file.getParent() != null && SDCARD.equals(file.getName()) == false){
			file = new File(file.getParent());
			if (file.getParent() != null){
				parent = new File(file.getParent());
			}
			initialize(file.getName(), file);
		} else if (exit) {
			Intent intent = new Intent(FileChooserActivity.this, HomeActivity.class);
			intent.putExtra("EXIT", false);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
    }
    
    
    @Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {

		case DIALOG_NEW_FOLDER:
			DialogInterface.OnClickListener createFolder = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					FileSystemUtils.createNewFolder(mRoot, input.getText().toString().trim());
					initialize(mRoot.getName(), mRoot);
				}
			};

			return createDialog(R.string.create_folder, R.string.action_create_folder_des, "", createFolder);

		case DIALOG_DELETE:
			return new AlertDialog.Builder(this).setTitle(R.string.delete)
					.setMessage(FileChooserActivity.this.getText(R.string.action_delete_desc) + " " + file.getName() + " ? ").setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FileSystemUtils.delete(file);
							initialize(mRoot.getName(), mRoot);
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).create();

		case DIALOG_RENAME:
			DialogInterface.OnClickListener rename = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					FileSystemUtils.rename(file, input.getText().toString().trim());
					initialize(mRoot.getName(), mRoot);
				}
			};

			return createDialog(R.string.rename, R.string.action_rename_desc, file.getName(), rename);

		case DIALOG_ORDER:
			
			return new AlertDialog.Builder(this).setTitle(R.string.action_sorting_title)
				.setSingleChoiceItems(getFiltersLabel(), sorting.indexOf(sort), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 dialog.dismiss();
			        	 sort = sorting.get(which); 
			        	 initialize(mRoot.getName(), mRoot);
					}
				})
				.setNegativeButton(this.getText(R.string.cancel), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			           }
			    }).create();
			
		default:
			return null;
		}
	}
    
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);

 		menu.add(0, MENU_NEW_FOLDER, 0, R.string.create_folder).setIcon(
 				android.R.drawable.ic_menu_add).setShortcut('0', 'f');
 		return true;
 	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Intent intent;
		switch (item.getItemId()) {
		case MENU_NEW_FOLDER:
			showDialog(DIALOG_NEW_FOLDER);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}
    
    class DirAlphaComparator implements Comparator<File> {

        public int compare(File filea, File fileb) {
            if (filea.isDirectory() && !fileb.isDirectory()) {
                return -1;

            } else if (!filea.isDirectory() && fileb.isDirectory()) {
                return 1;

            } else {
                return filea.getName().compareToIgnoreCase(fileb.getName());
            }
        }
    }
    
    class DirSizeComparator implements Comparator<File> {

        public int compare(File filea, File fileb) {
            if (filea.isDirectory() && !fileb.isDirectory()) {
                return -1;

            } else if (!filea.isDirectory() && fileb.isDirectory()) {
                return 1;

            } else {
            	if (filea.length() > fileb.length()){
            		return 1;
            	} else if  (filea.length() < fileb.length()){
            		return -1;
            	} else {
            		return 0;
            	}
            }
        }
    }
    
    class DirDateComparator implements Comparator<File> {

        public int compare(File filea, File fileb) {
        	if (filea.lastModified() > fileb.lastModified()){
        		return 1;
        	} else if  (filea.lastModified() < fileb.lastModified()){
        		return -1;
        	} else {
        		return 0;
        	}
        }
    }
    
    private void initSortingLabel() {
		sortingValueLabel.add(this.getText(R.string.action_sorting_name).toString());
		sortingValueLabel.add(this.getText(R.string.action_sorting_size).toString());
		sortingValueLabel.add(this.getText(R.string.action_sorting_date).toString());
	}
    
    private void initSorting() {
		sorting.add(R.string.action_sorting_name);
		sorting.add(R.string.action_sorting_size);
		sorting.add(R.string.action_sorting_date);
	}
	
    private CharSequence[] getFiltersLabel() {
		return sortingValueLabel.toArray(new CharSequence[sorting.size()]);
	}

}