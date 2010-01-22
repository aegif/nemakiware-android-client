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
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisProperty;

public class CmisItemCollectionAdapter extends ArrayAdapter<CmisItem> {

	private final Context context;

	public CmisItemCollectionAdapter(Context context, int textViewResourceId,
			CmisItemCollection itemCollection) {
		super(context, textViewResourceId, itemCollection.getItems() );
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
		
		CmisItem item = getItem(position);
		if (item != null) {
			
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			if (tt != null) {
				tt.setText(item.getTitle());
			}

			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			if (bt != null) {
				bt.setText(buildBottomText(item));
			}

			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			if (icon != null) {
				if (item.hasChildren()) {
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

	private CharSequence buildBottomText(CmisItem doc) {
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
		
		CmisProperty fileSize = doc.getProperties().get("cmis:contentStreamLength");
		if (fileSize != null) {
			infos.add(fileSize.getValue() + " Bytes");
		}
		
		
		return TextUtils.join(" | ", infos);
	}
}