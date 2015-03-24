/*
 * Copyright (C) 2010 Florian Maul
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
package jp.aegif.android.cmis;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import jp.aegif.android.cmis.repo.DownloadItem;
import jp.aegif.android.cmis.utils.ActionUtils;
import jp.aegif.android.cmis.utils.MimetypeUtils;

public class DownloadAdapter extends ArrayAdapter<DownloadItem> {

	private final Context context;

	static private class ViewHolder {
		TextView topText;
		TextView bottomText;
		ImageView icon;
	}

	public DownloadAdapter(Context context, int textViewResourceId, List<DownloadItem> itemCollection) {
		super(context, textViewResourceId, itemCollection);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		DownloadItem item = getItem(position);
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

	private void updateControls(ViewHolder v, DownloadItem item) {
		if (item != null) {
			updateControlTitle(v, item);
			updateControlDescriptionText(v, item);
			updateControlIcon(v, item);
		}
	}

	private void updateControlIcon(ViewHolder vh, DownloadItem item) {
		vh.icon.setImageDrawable(getContext().getResources().getDrawable(MimetypeUtils.getIcon((Activity)context, item.getItem())));
	}

	private void updateControlDescriptionText(ViewHolder vh, DownloadItem item) {
		vh.bottomText.setText(buildBottomText(item));
	}

	private void updateControlTitle(ViewHolder vh, DownloadItem item) {
		vh.topText.setText(item.getItem().getTitle());
	}

	private CharSequence buildBottomText(DownloadItem doc) {
		List<String> infos = new LinkedList<String>();
		appendInfoDocumentSize(doc, infos);
		appendState(doc, infos);
		//appendStatus(doc, infos);
		return TextUtils.join(" | ", infos);
	}

	private void appendInfoDocumentSize(DownloadItem doc, List<String> infos) {
		if (doc.getItem().getSize() != null) {
			infos.add(ActionUtils.convertAndFormatSize((Activity) context, doc.getItem().getSize()));
		}
	}
	
	/*private void appendStatus(DownloadItem doc, List<String> infos) {
		if (doc.getTask().getStatus() != null) {
			infos.add("Status : " + doc.getTask().getStatus());
		}
	}*/
	
	private void appendState(DownloadItem doc, List<String> infos) {
		if (doc.getTask().getStatus() != null) {
			infos.add(doc.getStatut((Activity) context));
		}
	}

}