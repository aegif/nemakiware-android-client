/*
 * Copyright (C) 2010 Jean Marie PASCAL
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

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fmaul.android.cmis.model.Favorite;


public class FileAdapter extends ArrayAdapter<File> {

	static private class ViewHolder {
		TextView topText;
		ImageView icon;
	}

	private File parent;

	public FileAdapter(Context context, int textViewResourceId, ArrayList<File> files, File parent) {
		super(context, textViewResourceId, files);
		this.parent = parent;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		File item = getItem(position);
		updateControls(vh, item);
		return v;
	}
	
	private View recycleOrCreateView(View v) {
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.file_list_row, null);

			ViewHolder vh = new ViewHolder();
			vh.icon = (ImageView) v.findViewById(R.id.icon);
			vh.topText = (TextView) v.findViewById(R.id.toptext);

			v.setTag(vh);
		}
		return v;
	}
	
	
	private void updateControls(ViewHolder v, File item) {
		if (item != null) {
			v.topText.setText(item.getName());
			updateControlIcon(v, item);
		}
	}
	
	public static String getExtension(String uri) {
        if (uri == null) {
                return null;
        }
        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
                return uri.substring(dot+1);
        } else {
                return "";
        }
}
	
	private void updateControlIcon(ViewHolder vh, File item) {

		if (item.isDirectory()){
			if (item.equals(parent)){
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.up));
			} else {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mt_folderopen));
			}
		} else {
			String mimetype = getExtension(item.getName());
			if (mimetype == null || mimetype.length() == 0) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mt_text));
			} else if (fileExtensions.get(mimetype) != null){
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(fileExtensions.get(mimetype)));
			} else {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mt_text));
			}
		}
	}
	
	
	private static HashMap<String, Integer> fileExtensions =  new HashMap<String, Integer>();
	
	 static {
		 	fileExtensions.put("jpg", R.drawable.mt_image);
		 	fileExtensions.put("jpeg", R.drawable.mt_image);
		 	fileExtensions.put("gif", R.drawable.mt_image);
		 	fileExtensions.put("png", R.drawable.mt_image);
		 	
		 	fileExtensions.put("pdf", R.drawable.mt_pdf);
		 	
		 	fileExtensions.put("doc", R.drawable.mt_msword);
		 	fileExtensions.put("docx", R.drawable.mt_msword);
		 	
		 	fileExtensions.put("xls", R.drawable.mt_msexcel);
		 	fileExtensions.put("xlsx", R.drawable.mt_msexcel);
		 	
		 	fileExtensions.put("ppt", R.drawable.mt_mspowerpoint);
		 	fileExtensions.put("pptx", R.drawable.mt_mspowerpoint);
		 	
		 	fileExtensions.put("html", R.drawable.mt_html);
		 	fileExtensions.put("htm", R.drawable.mt_html);
		 	
		 	fileExtensions.put("mov", R.drawable.mt_video);
		 	fileExtensions.put("avi", R.drawable.mt_video);
		 	
		  }
	
	
}
