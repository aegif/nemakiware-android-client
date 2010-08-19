package de.fmaul.android.cmis.asynctask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.DocumentDetailsActivity;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemLazy;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisPropertyFilter;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.ListUtils;

public class ItemPropertiesDisplayTask extends AsyncTask<String, Void, List<Map<String, ?>>> {

	private final DocumentDetailsActivity activity;
	private ProgressDialog pg;
	private List<Map<String, ?>> itemProperties;
	private String[] filterProperties;

	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity) {
		this(activity, null);
	}
	
	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity, List<Map<String, ?>> itemProperties) {
		super();
		this.activity = activity;
		this.itemProperties = itemProperties;
	}
	
	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity, List<Map<String, ?>> itemProperties, String[] filterProperties) {
		super();
		this.activity = activity;
		this.itemProperties = itemProperties;
		this.filterProperties = filterProperties;
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true);
	}

	@Override
	protected List<Map<String, ?>> doInBackground(String... params) {
		try {
			if (itemProperties != null){
				return itemProperties;
			} else {
				CmisItemLazy item = (CmisItemLazy) activity.getIntent().getExtras().getSerializable("item");
				List<CmisProperty> propList = getPropertiesFromIntent();
				CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(getObjectTypeIdFromIntent());
				return buildListOfNameValueMaps(filterProperties, propList, typeDefinition);
			}
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(List<Map<String, ?>> list) {
		((CmisApp) activity.getApplication()).setItemProperties(list);
		
		SimpleAdapter props = new SimpleAdapter(activity, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
			R.id.propertyName, R.id.propertyValue });
		activity.setListAdapter(props);
		
		if (list.size() == 0 ){
			activity.findViewById(R.id.empty).setVisibility(View.VISIBLE);
		} else {
			activity.findViewById(R.id.empty).setVisibility(View.GONE);
		}
		
		pg.dismiss();
	}

	@Override
	protected void onCancelled() {
		pg.dismiss();
	}
	

	private String getObjectTypeIdFromIntent() {
		return activity.getIntent().getStringExtra("objectTypeId");
	}

	//TODO BETTER!!!!
	private List<Map<String, ?>> buildListOfNameValueMaps(String[] filter, List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		if (filter != null){
			if (filter.equals(CmisPropertyFilter.LIST_EXTRA)){
				for (CmisProperty cmisProperty : propList){
					if (cmisProperty.getDefinitionId() != null && getDisplayNameFromProperty(cmisProperty, typeDefinition).contains(":")) {
						list.add(ListUtils.createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
					}
				}
			} else if (filter.equals(CmisPropertyFilter.LIST_ALL)) {
				return buildListOfNameValueMaps(propList, typeDefinition);
			} else {
				List<String> filterList =  new ArrayList<String>(Arrays.asList(filter));
				for (String prop : filterList) {
					for (CmisProperty cmisProperty : propList){
						if (cmisProperty.getDefinitionId() != null && prop.equals(cmisProperty.getDefinitionId())) {
							list.add(ListUtils.createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
						}
					}
				}
			}
			return list;
		} else {
			return buildListOfNameValueMaps(propList, typeDefinition);
		}
	}
	
	private List<Map<String, ?>> buildListOfNameValueMaps (List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
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