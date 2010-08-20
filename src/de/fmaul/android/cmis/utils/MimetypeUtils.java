package de.fmaul.android.cmis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisModel;
import de.fmaul.android.cmis.repo.CmisPropertyFilter;

public class MimetypeUtils {

	private static ArrayList<String> getOpenWithRows(Activity activity) {
		ArrayList<String> filters = new ArrayList<String>(5);
		filters.add(activity.getText(R.string.open_with_text).toString());
		filters.add(activity.getText(R.string.open_with_image).toString());
		filters.add(activity.getText(R.string.open_with_audio).toString());
		filters.add(activity.getText(R.string.open_with_video).toString());
		return filters;
	}
	
	public static ArrayList<String> getDefaultMimeType() {
		ArrayList<String> filters = new ArrayList<String>(5);
		filters.add("text/plain");
		filters.add("image/jpeg");
		filters.add("audio/mpeg3");
		filters.add("video/avi");
		return filters;
	}
	
	public static CharSequence[] getOpenWithRowsLabel(Activity activity) {
		ArrayList<String> filters = getOpenWithRows(activity);
		return filters.toArray(new CharSequence[filters.size()]);
	}
	
	public static Integer getIcon(Activity activity, CmisItem item) {
		if (item.hasChildren()) {
			return R.drawable.mt_folderopen;
		} else {
			String mimetype = item.getMimeType();
			if (((CmisApp) activity.getApplication()).getMimetypesMap().containsKey(mimetype)){
				return ((CmisApp) activity.getApplication()).getMimetypesMap().get(mimetype);
			} else {
				return R.drawable.mt_file;
			}
		}
	}
	
	public static Integer getIcon(Activity activity, String mimetype) {
		if (mimetype != null && mimetype.length() != 0 && mimetype.equals("cmis:folder") == false) {
			if (((CmisApp) activity.getApplication()).getMimetypesMap().containsKey(mimetype)){
				return ((CmisApp) activity.getApplication()).getMimetypesMap().get(mimetype);
			} else {
				return R.drawable.mt_file;
			}
		} else {
			return R.drawable.mt_folderopen;
		}
	}
	
	
	public static Map<String,Integer> createIconMap(){
		Map<String,Integer> iconMap = new HashMap<String, Integer>();
		iconMap.put("application/atom+xml",R.drawable.mt_xml);
		iconMap.put("application/javascript",R.drawable.mt_script);
		iconMap.put("application/mp4",R.drawable.mt_video);
		iconMap.put("application/octet-stream",R.drawable.mt_file);
		iconMap.put("application/msword",R.drawable.mt_msword);
		iconMap.put("application/pdf",R.drawable.mt_pdf);
		iconMap.put("application/postscript",R.drawable.mt_script);
		iconMap.put("application/rtf",R.drawable.mt_text);
		iconMap.put("application/sgml",R.drawable.mt_xml);
		iconMap.put("application/vnd.ms-excel",R.drawable.mt_msexcel);
		iconMap.put("application/vnd.ms-powerpoint",R.drawable.mt_mspowerpoint);
		iconMap.put("application/xml",R.drawable.mt_xml);
		iconMap.put("application/x-tar",R.drawable.mt_package);
		iconMap.put("application/zip",R.drawable.mt_package);
		iconMap.put("audio/basic",R.drawable.mt_audio);
		iconMap.put("audio/mpeg",R.drawable.mt_audio);
		iconMap.put("audio/mp4",R.drawable.mt_audio);
		iconMap.put("audio/x-aiff",R.drawable.mt_audio);
		iconMap.put("audio/x-wav",R.drawable.mt_audio);
		iconMap.put("image/gif",R.drawable.mt_image);
		iconMap.put("image/jpeg",R.drawable.mt_image);
		iconMap.put("image/png",R.drawable.mt_image);
		iconMap.put("image/tiff",R.drawable.mt_image);
		iconMap.put("image/x-portable-bitmap",R.drawable.mt_image);
		iconMap.put("image/x-portable-graymap",R.drawable.mt_image);
		iconMap.put("image/x-portable-pixmap",R.drawable.mt_image);
		iconMap.put("multipart/x-zip",R.drawable.mt_package);
		iconMap.put("multipart/x-gzip",R.drawable.mt_package);
		iconMap.put("text/css",R.drawable.mt_css);
		iconMap.put("text/csv",R.drawable.mt_sql);
		iconMap.put("text/html",R.drawable.mt_html);
		iconMap.put("text/plain",R.drawable.mt_text);
		iconMap.put("text/richtext",R.drawable.mt_text);
		iconMap.put("text/rtf",R.drawable.mt_text);
		iconMap.put("text/tab-separated-value",R.drawable.mt_sql);
		iconMap.put("text/xml",R.drawable.mt_xml);
		iconMap.put("video/h264",R.drawable.mt_video);
		iconMap.put("video/dv",R.drawable.mt_video);
		iconMap.put("video/mpeg",R.drawable.mt_video);
		iconMap.put("video/quicktime",R.drawable.mt_video);
		iconMap.put("video/msvideo",R.drawable.mt_video);
		return iconMap;
	}
	
	
}
