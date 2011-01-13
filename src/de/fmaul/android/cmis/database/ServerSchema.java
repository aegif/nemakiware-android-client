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

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ServerSchema {

	public static final String TABLENAME = "Servers";
	
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
    
    public static final String COLUMN_WS = "workspace";
    public static final int COLUMN_WS_ID = 5;	
    
    private static final String QUERY_TABLE_CREATE =
    	  "create table " + TABLENAME + " (" 
      	+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      	+ COLUMN_NAME + " TEXT NOT NULL,"
      	+ COLUMN_URL + " TEXT NOT NULL,"
      	+ COLUMN_USER + " TEXT NOT NULL,"
      	+ COLUMN_PASS + " TEXT NOT NULL," 
      	+ COLUMN_WS + " TEXT NOT NULL" 
      	+ ");";

	private static final String QUERY_TABLE_DROP = "DROP TABLE IF EXISTS "
			+ TABLENAME;
    
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(ServerSchema.QUERY_TABLE_CREATE);

		ServerDAO serverDao = new ServerDAO(db); 
		serverDao.insert("CMIS Nuxeo", "http://cmis.demo.nuxeo.org/nuxeo/atom/cmis", "Administrator", "Administrator", "Nuxeo Repository default");
		serverDao.insert("CMIS Alfresco", "http://cmis.alfresco.com/service/cmis", "admin", "admin", "Main Repository");
		serverDao.insert("CMIS eXo", "http://xcmis.org/xcmis1/rest/cmisatom", "", "", "cmis1");
		serverDao.insert("CMIS Day CRX", "http://cmis.day.com/cmis/repository", "", "", "CRX");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("ServersSchema: ", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL(QUERY_TABLE_DROP);
		onCreate(db);
	}
	
}
