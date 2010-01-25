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
		View v = getListRowView(convertView);
		CmisItem item = getItem(position);
		updateControls(v, item);
		return v;
	}

	private View getListRowView(View convertView) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.feed_list_row, null);
		}
		return v;
	}

	private void updateControls(View v, CmisItem item) {
		if (item != null) {
			updateControlTitle(v, item);
			updateControlDescriptionText(v, item);
			updateControlIcon(v, item);
		}
	}

	private void updateControlIcon(View v, CmisItem item) {
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

	private void updateControlDescriptionText(View v, CmisItem item) {
		TextView bt = (TextView) v.findViewById(R.id.bottomtext);
		if (bt != null) {
			bt.setText(buildBottomText(item));
		}
	}

	private void updateControlTitle(View v, CmisItem item) {
		TextView tt = (TextView) v.findViewById(R.id.toptext);
		if (tt != null) {
			tt.setText(item.getTitle());
		}
	}

	private CharSequence buildBottomText(CmisItem doc) {
		List<String> infos = new LinkedList<String>(); 
		appendInfoAuthor(doc, infos);
		appendInfoModificationDate(doc, infos);
		appendInfoDocumentSize(doc, infos);
		return TextUtils.join(" | ", infos);
	}

	private void appendInfoDocumentSize(CmisItem doc, List<String> infos) {
		CmisProperty fileSize = doc.getProperties().get("cmis:contentStreamLength");
		if (fileSize != null) {
			infos.add(fileSize.getValue() + " Bytes");
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