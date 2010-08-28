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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fmaul.android.cmis.model.Favorite;
import de.fmaul.android.cmis.model.Search;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.utils.MimetypeUtils;


public class SavedSearchAdapter extends ArrayAdapter<Search> {

	static private class ViewHolder {
		TextView topText;
		TextView bottomText;
		ImageView icon;
	}


	private Context context;

	public SavedSearchAdapter(Context context, int textViewResourceId, ArrayList<Search> favorites) {
		super(context, textViewResourceId,favorites);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		Search item = getItem(position);
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
	
	
	private void updateControls(ViewHolder v, Search item) {
		if (item != null) {
			v.topText.setText(item.getName());
			v.bottomText.setText(item.getUrl());
			updateControlIcon(v, item);
		}
	}
	

	private void updateControlIcon(ViewHolder vh, Search item) {
		vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.search));
	}
	
}
