package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.Server;
import de.fmaul.android.cmis.utils.FeedLoadException;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.ListUtils;

public class ServerInfoLoadingTask extends AsyncTask<String, Void, Map<String, ArrayList<CmisProperty>>> {

	private final Activity activity;
	private Server server;
	private ProgressDialog pg;

	public ServerInfoLoadingTask(Activity activity, Server server) {
		super();
		this.activity = activity;
		this.server = server;
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true);
	}

	@Override
	protected Map<String, ArrayList<CmisProperty>> doInBackground(String... params) {
		try {
			Element workspace;
			Map<String, ArrayList<CmisProperty>> properties = null;
			try {
				workspace = FeedUtils.getWorkspace(server.getWorkspace(), server.getUrl(), server.getUsername(), server.getPassword());
				properties = FeedUtils.getCmisRepositoryProperties(workspace);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return properties;
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Map<String, ArrayList<CmisProperty>> properties) {
		//Init View
		Intent intent = new Intent(activity, ServerInfoActivity.class);

		intent.putExtra("title", "Info " + server.getName());
		
		intent.putParcelableArrayListExtra(Server.INFO_GENERAL, properties.get(Server.INFO_GENERAL));
		intent.putParcelableArrayListExtra(Server.INFO_ACL_CAPABILITIES, properties.get(Server.INFO_ACL_CAPABILITIES));
		intent.putParcelableArrayListExtra(Server.INFO_CAPABILITIES, properties.get(Server.INFO_CAPABILITIES));
		activity.startActivity(intent);
		pg.dismiss();
	}

	@Override
	protected void onCancelled() {
		pg.dismiss();
	}
}