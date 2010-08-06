package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.ListUtils;

public class ServerInfoDisplayTask extends AsyncTask<String, Void, SimpleAdapter> {

	private final Activity activity;

	public ServerInfoDisplayTask(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected SimpleAdapter doInBackground(String... params) {
		try {
			ArrayList<CmisProperty> propList = activity.getIntent().getParcelableArrayListExtra(activity.getIntent().getStringExtra("context"));
			
			List<Map<String, ?>> list = ListUtils.buildListOfNameValueMaps(propList);
			SimpleAdapter props = new SimpleAdapter(activity, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
					R.id.propertyName, R.id.propertyValue });
			return props;
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(SimpleAdapter props) {
		//Init View
		activity.setContentView(R.layout.server_info_general);
		ListView listInfo = (ListView) activity.findViewById(R.id.server_info_general);
		listInfo.setAdapter(props);
		activity.setProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onCancelled() {
		activity.setProgressBarIndeterminateVisibility(false);
	}
}