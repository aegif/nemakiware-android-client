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
package jp.aegif.android.cmis.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import jp.aegif.android.cmis.model.Search;

public class SearchDAO implements DAO<Search> {

	private final SQLiteDatabase db;

	public SearchDAO(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long insert(String name, String url, long serverId) {
		ContentValues insertValues = createContentValues(name, url, serverId);
		
		return db.insert(SearchSchema.TABLENAME, null, insertValues);
	}
	
	public boolean update(long id, String name, String url, long serverId) {
		ContentValues updateValues = createContentValues(name, url, serverId);
		
		return db.update(SearchSchema.TABLENAME, updateValues, SearchSchema.COLUMN_ID + "=" + id, null) > 0;
	}

	public boolean delete(long id) {
		return db.delete(SearchSchema.TABLENAME, SearchSchema.COLUMN_ID + "=" + id, null) > 0;
	}
	
	public List<Search> findAll() {
		Cursor c = db.query(
				SearchSchema.TABLENAME, new String[] { SearchSchema.COLUMN_ID, SearchSchema.COLUMN_NAME, SearchSchema.COLUMN_URL, SearchSchema.COLUMN_SERVER },
				null, null, null, null, null);
		return cursorToSearches(c);
	}
	
	public List<Search> findAll(long id) {
		Cursor c = db.query(
				SearchSchema.TABLENAME, new String[] { SearchSchema.COLUMN_ID, SearchSchema.COLUMN_NAME, SearchSchema.COLUMN_URL, SearchSchema.COLUMN_SERVER },
				SearchSchema.COLUMN_SERVER + "=" + id, null, null, null, null);
		return cursorToSearches(c);
	}
	
	public Search findById(long id) {
		Cursor c = db.query(SearchSchema.TABLENAME, null, SearchSchema.COLUMN_ID + " like " + id, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}		
		return cursorToSearch(c);
	}
	
	public boolean isPresentByURL(String url) {
		Cursor c = db.query(SearchSchema.TABLENAME, null, SearchSchema.COLUMN_URL + " = '" + url + "'", null, null, null, null);
		if (c != null) {
			if (c.getCount() == 1){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private ContentValues createContentValues(String name, String url, long serverId) {
		ContentValues updateValues = new ContentValues();
		
		updateValues.put(SearchSchema.COLUMN_NAME, name);
		updateValues.put(SearchSchema.COLUMN_URL, url);
		updateValues.put(SearchSchema.COLUMN_SERVER, serverId);
		return updateValues;
	}
	
	private ArrayList<Search> cursorToSearches(Cursor c){
		if (c.getCount() == 0){
			return new ArrayList<Search>();
		}
		
		ArrayList<Search> searches = new ArrayList<Search>(c.getCount());
		c.moveToFirst();
		
		do {
			Search search = createSearchFromCursor(c);
			searches.add(search);
		} while (c.moveToNext());
		c.close();
		return searches;
	}

	private Search createSearchFromCursor(Cursor c) {
		Search search = new Search(
				c.getInt(SearchSchema.COLUMN_ID_ID), 
				c.getString(SearchSchema.COLUMN_NAME_ID), 
				c.getString(SearchSchema.COLUMN_URL_ID), 
				c.getInt(SearchSchema.COLUMN_SERVER_ID)
				);
		return search;
	}
	
	private Search cursorToSearch(Cursor c){
		if (c.getCount() == 0){
			return null;
		}
		Search search = createSearchFromCursor(c);
		c.close();
		return search;
	}

}
