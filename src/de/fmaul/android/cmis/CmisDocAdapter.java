/**
 * 
 */
package de.fmaul.android.cmis;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class CmisDocAdapter extends ArrayAdapter<CmisDoc> {

	private List<CmisDoc> items;
	private final Context context;

	public CmisDocAdapter(Context context, int textViewResourceId,
			List<CmisDoc> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
		CmisDoc doc = items.get(position);
		if (doc != null) {
			
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			if (tt != null) {
				tt.setText(doc.getTitle());
			}

			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			if (bt != null) {
				bt.setText(buildBottomText(doc));
			}

			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			if (icon != null) {
				if (doc.hasChildren()) {
					icon.setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.folder));
				} else {
					icon.setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.file));
				}
			}
		}
		return v;
	}

	private CharSequence buildBottomText(CmisDoc doc) {
		List<String> infos = new LinkedList<String>(); 

		if (!TextUtils.isEmpty(doc.getAuthor())) {
			infos.add(doc.getAuthor());
		}
		
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
		
		return TextUtils.join(" | ", infos);
	}
}