package de.fmaul.android.cmis.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
	
	private static final String DATABASE_NAME = "DatabaseCMIS";
    
    private static final int DATABASE_VERSION = 1;
    
    private final SQLiteOpenHelper cmisDbHelper;
    private SQLiteDatabase sqliteDb;

    protected Database(Context ctx){
		this.cmisDbHelper = new CMISDBAdapterHelper(ctx);
	}
    
    public static Database create(Context ctx) {
    	return new Database(ctx);
	}
    
    public SQLiteDatabase open() {
    	if (sqliteDb == null || !sqliteDb.isOpen()) {
    		sqliteDb = cmisDbHelper.getWritableDatabase();
    	}
		return sqliteDb;
	}

	public void close() {
		sqliteDb.close();
	}
	
	
    private static class CMISDBAdapterHelper extends SQLiteOpenHelper {
		CMISDBAdapterHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			ServerSchema.onCreate(db);
			FavoriteSchema.onCreate(db);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			ServerSchema.onUpgrade(db, oldVersion, newVersion);
			FavoriteSchema.onUpgrade(db, oldVersion, newVersion);
		}
	}
    
}
