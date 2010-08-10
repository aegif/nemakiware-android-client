package de.fmaul.android.cmis.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FavoriteSchema {

	public static final String TABLENAME = "Favorites";
	
	public static final String COLUMN_ID = "id";
	public static final int COLUMN_ID_ID = 0;
	
    public static final String COLUMN_NAME = "name";
    public static final int COLUMN_NAME_ID = 1;
    
    public static final String COLUMN_URL = "url";
    public static final int COLUMN_URL_ID = 2;
    
    public static final String COLUMN_SERVERID = "serverid";
    public static final int COLUMN_SERVERID_ID = 3;
    
    public static final String COLUMN_MIMETYPE = "mimetype";
    public static final int COLUMN_MIMETYPE_ID = 4;
    
    private static final String QUERY_TABLE_CREATE =
    	  "create table " + TABLENAME + " (" 
      	+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      	+ COLUMN_NAME + " TEXT NOT NULL,"
      	+ COLUMN_URL + " TEXT NOT NULL,"
      	+ COLUMN_SERVERID + " INTEGER NOT NULL,"
      	+ COLUMN_MIMETYPE + " TEXT NOT NULL"
      	+ ");";

	private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS "
			+ TABLENAME;
    
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(FavoriteSchema.QUERY_TABLE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("FavoriteSchema: ", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL(QUERY_TABLE_DROP);
		onCreate(db);
	}
	
}
