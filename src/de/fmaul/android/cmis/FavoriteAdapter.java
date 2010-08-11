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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fmaul.android.cmis.model.Favorite;


public class FavoriteAdapter extends ArrayAdapter<Favorite> {

	static private class ViewHolder {
		TextView topText;
		TextView bottomText;
		ImageView icon;
	}

	public FavoriteAdapter(Context context, int textViewResourceId, ArrayList<Favorite> favorites) {
		super(context, textViewResourceId,favorites);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		Favorite item = getItem(position);
		updateControls(vh, item);
		return v;
	}
	
	private View recycleOrCreateView(View v) {
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.feed_list_row, null);

			ViewHolder vh = new ViewHolder();
			vh.icon = (ImageView) v.findViewById(R.id.icon);
			vh.topText = (TextView) v.findViewById(R.id.toptext);
			vh.bottomText = (TextView) v.findViewById(R.id.bottomtext);

			v.setTag(vh);
		}
		return v;
	}
	
	
	private void updateControls(ViewHolder v, Favorite item) {
		if (item != null) {
			v.topText.setText(item.getName());
			v.bottomText.setText(item.getUrl());
			updateControlIcon(v, item);
		}
	}
	
	private void updateControlIcon(ViewHolder vh, Favorite item) {

		String mimetype = item.getMimetype();
		if (mimetype == null || mimetype.length() == 0) {
			vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.folderopen));
		} else {
			if(mimetype.contains("image")){
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image));
			} else if(mimetype.contains("pdf")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.pdf));
			} else if(mimetype.contains("msword")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msword));
			} else if(mimetype.contains("excel")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msexcel));
			} else if(mimetype.contains("point")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mspowerpoint));
			} else if(mimetype.contains("html")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.html));
			} else if(mimetype.contains("video")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.video));
			} else {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.text));
			}
		}
	}
	
}
