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
package de.fmaul.android.cmis;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.utils.MimetypeUtils;

public class CmisItemCollectionAdapter extends ArrayAdapter<CmisItem> {

	private final Context context;

	static private class ViewHolder {
		TextView topText;
		TextView bottomText;
		ImageView icon;
	}

	public CmisItemCollectionAdapter(Context context, int textViewResourceId, CmisItemCollection itemCollection) {
		super(context, textViewResourceId, itemCollection.getItems());
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		CmisItem item = getItem(position);
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

	private void updateControls(ViewHolder v, CmisItem item) {
		if (item != null) {
			updateControlTitle(v, item);
			updateControlDescriptionText(v, item);
			updateControlIcon(v, item);
		}
	}

	private void updateControlIcon(ViewHolder vh, CmisItem item) {
		vh.icon.setImageDrawable(getContext().getResources().getDrawable(MimetypeUtils.getIcon((Activity)context, item)));
	}

	private void updateControlDescriptionText(ViewHolder vh, CmisItem item) {
		vh.bottomText.setText(buildBottomText(item));
	}

	private void updateControlTitle(ViewHolder vh, CmisItem item) {
		vh.topText.setText(item.getTitle());
	}

	private CharSequence buildBottomText(CmisItem doc) {
		List<String> infos = new LinkedList<String>();
		appendInfoAuthor(doc, infos);
		appendInfoModificationDate(doc, infos);
		appendInfoDocumentSize(doc, infos);
		return TextUtils.join(" | ", infos);
	}

	private void appendInfoDocumentSize(CmisItem doc, List<String> infos) {
		if (doc.getSize() != null) {
			infos.add(convertAndFormatSize(doc.getSize()));
		}
	}

	private String convertAndFormatSize(String size) {
		int sizeInByte = Integer.parseInt(size);

		if (sizeInByte < 1024) {
			return String.valueOf(sizeInByte) + " bytes";
		} else {
			int sizeInKB = sizeInByte / 1024;
			if (sizeInKB < 1024) {
				return String.valueOf(sizeInKB) + " KB";
			} else {
				int sizeInMB = sizeInKB / 1024;
				if (sizeInMB < 1024) {
					return String.valueOf(sizeInMB) + " MB";
				} else {
					return String.valueOf(sizeInMB / 1024) + " GB";
				}
			}
		}
	}

	private void appendInfoAuthor(CmisItem doc, List<String> infos) {
		if (!TextUtils.isEmpty(doc.getAuthor())) {
			infos.add(doc.getAuthor());
		}
	}

	private void appendInfoModificationDate(CmisItem doc, List<String> infos) {
		Date modificationDate = doc.getModificationDate();
		String modDate = "";
		String modTime = "";
		if (modificationDate != null) {
			modDate = DateFormat.getDateFormat(context).format(modificationDate);
			modTime = DateFormat.getTimeFormat(context).format(modificationDate);
			if (!TextUtils.isEmpty(modDate)) {
				infos.add(modDate);
			}
			if (!TextUtils.isEmpty(modTime)) {
				infos.add(modTime);
			}
		}
	}
}