 
package jp.aegif.android.cmis;
 
import java.util.ArrayList;

import jp.aegif.android.cmis.database.Database;
import jp.aegif.android.cmis.database.ServerDAO;
import jp.aegif.android.cmis.model.Server;
import android.app.ListActivity;
import android.os.Bundle;
 
public class CustomDialogActivity extends ListActivity {
 
    @Override
 
        protected void onCreate(Bundle savedInstanceState) {
 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

		createServerList();
    }
    
    public void createServerList(){
		Database db = Database.create(this);
		ServerDAO serverDao = new ServerDAO(db.open());
		ArrayList<Server> listServer = new ArrayList<Server>(serverDao.findAll());
		db.close();

		ServerAdapter cmisSAdapter = new ServerAdapter(this, R.layout.server_row, listServer);
		setListAdapter(cmisSAdapter);
	}
}
