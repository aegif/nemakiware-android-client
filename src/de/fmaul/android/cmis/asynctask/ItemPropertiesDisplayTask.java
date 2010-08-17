package de.fmaul.android.cmis.asynctask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.DocumentDetailsActivity;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.ListUtils;

public class ItemPropertiesDisplayTask extends AsyncTask<String, Void, List<Map<String, ?>>> {

	private final DocumentDetailsActivity activity;
	private ProgressDialog pg;

	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true);
	}

	@Override
	protected List<Map<String, ?>> doInBackground(String... params) {
		try {
			CmisItemLazy item = (CmisItemLazy) activity.getIntent().getExtras().getSerializable("item");
			List<CmisProperty> propList = getPropertiesFromIntent();
			CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(getObjectTypeIdFromIntent());
			
			return buildListOfNameValueMaps(propList, typeDefinition);
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(List<Map<String, ?>> list) {
		SimpleAdapter props = new SimpleAdapter(activity, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
			R.id.propertyName, R.id.propertyValue });
		activity.setListAdapter(props);
		pg.dismiss();
	}

	@Override
	protected void onCancelled() {
		pg.dismiss();
	}
	

	private String getObjectTypeIdFromIntent() {
		return activity.getIntent().getStringExtra("objectTypeId");
	}

	private List<Map<String, ?>> buildListOfNameValueMaps(List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for (CmisProperty cmisProperty : propList) {
			if (cmisProperty.getDefinitionId() != null) {
				list.add(ListUtils.createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
			}
		}
		return list;
	}

	private String getDisplayNameFromProperty(CmisProperty property, CmisTypeDefinition typeDefinition) {
		String name = property.getDisplayName();

		if (TextUtils.isEmpty(name)) {
		}
		name = typeDefinition.getDisplayNameForProperty(property);

		if (TextUtils.isEmpty(name)) {
			name = property.getDefinitionId();
		}
		
		return name.replaceAll("cmis:", "");
	}

	private ArrayList<CmisProperty> getPropertiesFromIntent() {
		ArrayList<CmisProperty> propList = activity.getIntent().getParcelableArrayListExtra("properties");
		return propList;
	}

	CmisRepository getRepository() {
		return ((CmisApp) activity.getApplication()).getRepository();
	}
	
}