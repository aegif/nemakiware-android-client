package de.fmaul.android.cmis.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisRepository;

public class UIUtils {

	public static void createSearchMenu(Menu menu) {
		SubMenu searchMenu = menu.addSubMenu(R.string.menu_item_search);
		searchMenu.setIcon(R.drawable.search);
		searchMenu.getItem().setAlphabeticShortcut(SearchManager.MENU_KEY);
		searchMenu.setHeaderIcon(R.drawable.search);

		searchMenu.add(Menu.NONE, 20, 0, R.string.menu_item_search_title);
		searchMenu.add(Menu.NONE, 21, 0, R.string.menu_item_search_folder_title);
		searchMenu.add(Menu.NONE, 22, 0, R.string.menu_item_search_fulltext);
		searchMenu.add(Menu.NONE, 23, 0, R.string.menu_item_search_cmis);
		searchMenu.add(Menu.NONE, 24, 0, R.string.menu_item_search_saved_search);
	}
	
	public static void createContextMenu(ListActivity activity, ContextMenu menu, ContextMenuInfo menuInfo) {
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(activity.getString(R.string.feed_menu_title));
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		CmisItem doc = (CmisItem) activity.getListView().getItemAtPosition(info.position);
		
		menu.add(0, 2, Menu.NONE, activity.getString(R.string.menu_item_details));
		menu.add(0, 3, Menu.NONE, activity.getString(R.string.menu_item_share));

		if (doc != null && doc.getProperties().get("cmis:contentStreamLength") != null){
			menu.add(0, 1, Menu.NONE, activity.getString(R.string.download));
		}
		
		menu.add(0, 4, Menu.NONE, activity.getString(R.string.menu_item_favorites));
	}
	
	public static boolean onContextItemSelected(Activity activity, MenuItem menuItem, Prefs prefs) {

		AdapterView.AdapterContextMenuInfo menuInfo;
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		GridView gridview = (GridView) activity.findViewById(R.id.gridview);
		ListView listView = ((ListActivity) activity).getListView();
		CmisItem item = null;
		if(prefs != null && prefs.getDataView() == Prefs.GRIDVIEW){
			item = (CmisItem) gridview.getItemAtPosition(menuInfo.position);
		} else {
			item = (CmisItem) listView.getItemAtPosition(menuInfo.position);
		}
		
		CmisRepository repository = ((CmisApp) activity.getApplication()).getRepository();
		
		switch (menuItem.getItemId()) {
		case 1:
			if (item != null && item.hasChildren() == false) {
				ActionUtils.openDocument(activity, item);
			}
			return true;
		case 2:
			if (item != null) {
				ActionUtils.displayDocumentDetails(activity, item);
			}
			return true;
		case 3:
			if (item != null) {
				ActionUtils.shareDocument(activity, repository.getServer().getWorkspace(), item);
			}
			return true;
		case 4:
			if (item != null) {
				ActionUtils.createFavorite(activity, repository.getServer(), item);
			}
			return true;
		default:
			return false;
		}
	}
	
	public static AlertDialog createDialog(Activity activity, int title, int message, String defaultValue, DialogInterface.OnClickListener positiveClickListener){
    	final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle(title);
		alert.setMessage(message);
		EditText input = new EditText(activity);
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
}
