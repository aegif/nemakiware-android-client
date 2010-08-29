/*
 * Copyright (C) 2010 Florian Maul & Jean Marie PASCAL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			SearchSchema.onCreate(db);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			ServerSchema.onUpgrade(db, oldVersion, newVersion);
			FavoriteSchema.onUpgrade(db, oldVersion, newVersion);
			SearchSchema.onUpgrade(db, oldVersion, newVersion);
		}
	}
    
}
