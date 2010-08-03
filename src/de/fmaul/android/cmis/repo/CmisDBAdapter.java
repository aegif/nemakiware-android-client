package de.fmaul.android.cmis.repo;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CmisDBAdapter {
	
	
	private static final String DATABASE_NAME = "DatabaseCMIS";
	private static final String TABLE_SERVERS = "Servers";
	
	public static final String COLUMN_ID = "id";
	public static final int COLUMN_ID_ID = 0;
	
    public static final String COLUMN_NAME = "name";
    public static final int COLUMN_NAME_ID = 1;
    
    public static final String COLUMN_URL = "url";
    public static final int COLUMN_URL_ID = 2;
    
    public static final String COLUMN_USER = "username";
    public static final int COLUMN_USER_ID = 3;
    
    public static final String COLUMN_PASS = "password";
    public static final int COLUMN_PASS_ID = 4;
    
    
    
    private static final int DATABASE_VERSION = 1;
    
    private static final String TAG = "DatabaseAdapter";
    
    private SQLiteOpenHelper cmisDbHelper;
    private SQLiteDatabase cmisDB;
    
    private static final String QUERY_DATABASE_CREATE =
  	  "create table " + TABLE_SERVERS + " (" 
    	+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    	+ COLUMN_NAME + " TEXT NOT NULL,"
    	+ COLUMN_URL + " TEXT NOT NULL,"
    	+ COLUMN_USER + " TEXT NOT NULL,"
    	+ COLUMN_PASS + " TEXT NOT NULL" 
    	+ ");";
    
    public CmisDBAdapter(Context ctx){
		this.cmisDbHelper = new CMISDBAdapterHelper(ctx);
	}
    
    public SQLiteDatabase open() {
    	cmisDB = cmisDbHelper.getWritableDatabase();
		return cmisDB;
	}

	public void close() {
		cmisDB.close();
	}
	
	public long insert(String name, String url, String username, String pass) {
		ContentValues insertValues = new ContentValues();
		
		insertValues.put(COLUMN_NAME, name);
		insertValues.put(COLUMN_URL, url);
		insertValues.put(COLUMN_USER, username);
		insertValues.put(COLUMN_PASS, pass);
		
		return cmisDB.insert(TABLE_SERVERS, null, insertValues);
	}
	
	public boolean update(long id, String name, String url, String username, String pass) {
		ContentValues updateValues = new ContentValues();
		
		updateValues.put(COLUMN_NAME, name);
		updateValues.put(COLUMN_URL, url);
		updateValues.put(COLUMN_USER, username);
		updateValues.put(COLUMN_PASS, pass);
		
		return cmisDB.update(TABLE_SERVERS, updateValues, COLUMN_ID + "=" + id, null) > 0;
	}
	
	public boolean deleteServer(long id) {
		return cmisDB.delete(TABLE_SERVERS, COLUMN_ID + "=" + id, null) > 0;
	}
	
	public ArrayList<Server> getAllServers() {
		Cursor c = cmisDB.query(
				TABLE_SERVERS, new String[] { COLUMN_ID, COLUMN_NAME, COLUMN_URL, COLUMN_USER, COLUMN_PASS },
				null, null, null, null, null);
		return cursorToServers(c);
	}
	
	public Server getServer(long rowHost) {
		Cursor c = cmisDB.query(TABLE_SERVERS, null, COLUMN_ID + " like " + rowHost, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return cursorToServer(c);
	}
	
	private ArrayList<Server> cursorToServers(Cursor c){
		if (c.getCount() == 0){
			return new ArrayList<Server>();
		}
		
		ArrayList<Server> servers = new ArrayList<Server>(c.getCount());
		c.moveToFirst();
		
		do {
			Server server = new Server(c.getInt(COLUMN_ID_ID), c.getString(COLUMN_NAME_ID), c.getString(COLUMN_URL_ID), c.getString(COLUMN_USER_ID), c.getString(COLUMN_PASS_ID));
			servers.add(server);
		} while (c.moveToNext());
		c.close();
		return servers;
	}
	
	private Server cursorToServer(Cursor c){
		if (c.getCount() == 0){
			return null;
		}
		Server server = new Server(c.getInt(COLUMN_ID_ID), c.getString(COLUMN_NAME_ID), c.getString(COLUMN_URL_ID), c.getString(COLUMN_USER_ID), c.getString(COLUMN_PASS_ID));
		c.close();
		return server;
	}
	
    private static class CMISDBAdapterHelper extends SQLiteOpenHelper {
		CMISDBAdapterHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(QUERY_DATABASE_CREATE);

			ContentValues initialValues = new ContentValues();

			initialValues.put(COLUMN_NAME, "CMIS Nuxeo");
			initialValues.put(COLUMN_USER, "Administrator");
			initialValues.put(COLUMN_PASS, "Administrator");
			initialValues.put(COLUMN_URL, "http://cmis.demo.nuxeo.org/nuxeo/site/cmis/repository");
			
			db.insert(TABLE_SERVERS, null, initialValues);
			
			initialValues.put(COLUMN_NAME, "CMIS Alfresco");
			initialValues.put(COLUMN_USER, "admin");
			initialValues.put(COLUMN_PASS, "admin");
			initialValues.put(COLUMN_URL, "http://cmis.alfresco.com/service/cmis");
			
			db.insert(TABLE_SERVERS, null, initialValues);
			
			initialValues.put(COLUMN_NAME, "CMIS eXo");
			initialValues.put(COLUMN_USER, "");
			initialValues.put(COLUMN_PASS, "");
			initialValues.put(COLUMN_URL, "http://cmis.exoplatform.org/xcmis1/rest/cmisatom");
			
			db.insert(TABLE_SERVERS, null, initialValues);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS titles");
			onCreate(db);
		}
	}
    
}
