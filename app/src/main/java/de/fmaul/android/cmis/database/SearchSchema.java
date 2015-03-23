/*
 * Copyright (C) 2010  Jean Marie PASCAL
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

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SearchSchema {

	public static final String TABLENAME = "SavedSearch";
	
	public static final String COLUMN_ID = "id";
	public static final int COLUMN_ID_ID = 0;
	
    public static final String COLUMN_NAME = "name";
    public static final int COLUMN_NAME_ID = 1;
    
    public static final String COLUMN_URL = "url";
    public static final int COLUMN_URL_ID = 2;
    
    public static final String COLUMN_SERVER = "serverid";
    public static final int COLUMN_SERVER_ID = 3;	
    
    private static final String QUERY_TABLE_CREATE =
    	  "create table " + TABLENAME + " (" 
      	+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      	+ COLUMN_NAME + " TEXT NOT NULL,"
      	+ COLUMN_URL + " TEXT NOT NULL,"
      	+ COLUMN_SERVER + " INTEGER NOT NULL" 
      	+ ");";

	private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS "
			+ TABLENAME;
    
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SearchSchema.QUERY_TABLE_CREATE);

		SearchDAO searchDao = new SearchDAO(db); 
		searchDao.insert("Query Document Test", "SELECT+*+FROM+cmis%3Adocument+WHERE+cmis%3Aname+LIKE+%27%25test%25%27", 1);
		searchDao.insert("Query Document Test", "SELECT+*+FROM+cmis%3Adocument+WHERE+cmis%3Aname+LIKE+%27%25test%25%27", 2);
		searchDao.insert("Query Document Test", "SELECT+*+FROM+cmis%3Adocument+WHERE+cmis%3Aname+LIKE+%27%25test%25%27", 3);
		searchDao.insert("Query Document Test", "SELECT+*+FROM+cmis%3Adocument+WHERE+cmis%3Aname+LIKE+%27%25test%25%27", 4);
		//searchDao.insert();
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("ServersSchema: ", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL(QUERY_TABLE_DROP);
		onCreate(db);
	}
	
}
