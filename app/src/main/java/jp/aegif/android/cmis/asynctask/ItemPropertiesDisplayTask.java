package jp.aegif.android.cmis.asynctask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.view.View;
import android.widget.SimpleAdapter;
import jp.aegif.android.cmis.CmisApp;
import jp.aegif.android.cmis.DocumentDetailsActivity;
import jp.aegif.android.cmis.R;
import jp.aegif.android.cmis.repo.CmisProperty;
import jp.aegif.android.cmis.repo.CmisPropertyFilter;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.repo.CmisTypeDefinition;
import jp.aegif.android.cmis.utils.FeedLoadException;

public class ItemPropertiesDisplayTask extends AsyncTask<String, Void, List<Map<String, ?>>> {

	private final DocumentDetailsActivity activity;
	private ProgressDialog pg;
	private String[] filterProperties;
	private CmisPropertyFilter propertiesFilters;
	private boolean screenRotation = false;


	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity) {
		super();
		this.activity = activity;
	}
	
	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity, boolean screenRotation) {
		super();
		this.activity = activity;
		this.screenRotation = true;
	}
	
	public ItemPropertiesDisplayTask(DocumentDetailsActivity activity, String[] filterProperties) {
		super();
		this.activity = activity;
		this.filterProperties = filterProperties;
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true, true, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ItemPropertiesDisplayTask.this.cancel(true);
				activity.finish();
				dialog.dismiss();
			}
		});
		propertiesFilters = ((CmisApp) activity.getApplication()).getCmisPropertyFilter();
	}

	@Override
	protected List<Map<String, ?>> doInBackground(String... params) {
		try {
			if (propertiesFilters != null){
				 if (screenRotation) {
					return propertiesFilters.render();
				 } else {
					 return propertiesFilters.render(filterProperties);
				 }
			} else {
				List<CmisProperty> propList = getPropertiesFromIntent();
				CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(getObjectTypeIdFromIntent());
				
				if (propertiesFilters == null){
					propertiesFilters = new CmisPropertyFilter(propList, typeDefinition);	
				}
				
				return propertiesFilters.render(filterProperties);
			}
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(List<Map<String, ?>> list) {
		((CmisApp) activity.getApplication()).setCmisPropertyFilter(propertiesFilters);
		
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

	private ArrayList<CmisProperty> getPropertiesFromIntent() {
		ArrayList<CmisProperty> propList = activity.getIntent().getParcelableArrayListExtra("properties");
		return propList;
	}

	CmisRepository getRepository() {
		return ((CmisApp) activity.getApplication()).getRepository();
	}
	
}