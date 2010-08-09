package de.fmaul.android.cmis.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.fmaul.android.cmis.model.Server;

public class ServerDAO implements DAO<Server> {

	private final SQLiteDatabase db;

	public ServerDAO(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long insert(String name, String url, String username, String pass, String workspace) {
		ContentValues insertValues = createContentValues(name, url, username,
				pass, workspace);
		
		return db.insert(ServerSchema.TABLENAME, null, insertValues);
	}
	
	public boolean update(long id, String name, String url, String username, String pass, String workspace) {
		ContentValues updateValues = createContentValues(name, url, username,
				pass, workspace);
		
		return db.update(ServerSchema.TABLENAME, updateValues, ServerSchema.COLUMN_ID + "=" + id, null) > 0;
	}

	public boolean delete(long id) {
		return db.delete(ServerSchema.TABLENAME, ServerSchema.COLUMN_ID + "=" + id, null) > 0;
	}
	
	public List<Server> findAll() {
		Cursor c = db.query(
				ServerSchema.TABLENAME, new String[] { ServerSchema.COLUMN_ID, ServerSchema.COLUMN_NAME, ServerSchema.COLUMN_URL, ServerSchema.COLUMN_USER, ServerSchema.COLUMN_PASS, ServerSchema.COLUMN_WS },
				null, null, null, null, null);
		return cursorToServers(c);
	}
	
	public Server findById(long id) {
		Cursor c = db.query(ServerSchema.TABLENAME, null, ServerSchema.COLUMN_ID + " like " + id, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}		
		return cursorToServer(c);
	}

	private ContentValues createContentValues(String name, String url,
			String username, String pass, String workspace) {
		ContentValues updateValues = new ContentValues();
		
		updateValues.put(ServerSchema.COLUMN_NAME, name);
		updateValues.put(ServerSchema.COLUMN_URL, url);
		updateValues.put(ServerSchema.COLUMN_USER, username);
		updateValues.put(ServerSchema.COLUMN_PASS, pass);
		updateValues.put(ServerSchema.COLUMN_WS, workspace);
		return updateValues;
	}
	
	private ArrayList<Server> cursorToServers(Cursor c){
		if (c.getCount() == 0){
			return new ArrayList<Server>();
		}
		
		ArrayList<Server> servers = new ArrayList<Server>(c.getCount());
		c.moveToFirst();
		
		do {
			Server server = createServerFromCursor(c);
			servers.add(server);
		} while (c.moveToNext());
		c.close();
		return servers;
	}

	private Server createServerFromCursor(Cursor c) {
		Server server = new Server(
				c.getInt(ServerSchema.COLUMN_ID_ID), 
				c.getString(ServerSchema.COLUMN_NAME_ID), 
				c.getString(ServerSchema.COLUMN_URL_ID), 
				c.getString(ServerSchema.COLUMN_USER_ID), 
				c.getString(ServerSchema.COLUMN_PASS_ID),
				c.getString(ServerSchema.COLUMN_WS_ID)
				);
		return server;
	}
	
	private Server cursorToServer(Cursor c){
		if (c.getCount() == 0){
			return null;
		}
		Server server = createServerFromCursor(c);
		c.close();
		return server;
	}

}
