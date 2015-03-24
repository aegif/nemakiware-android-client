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
package jp.aegif.android.cmis.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import jp.aegif.android.cmis.model.Favorite;

public class FavoriteDAO implements DAO<Favorite> {

	private final SQLiteDatabase db;

	public FavoriteDAO(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long insert(String name, String url, long serverId, String mimetype) {
		ContentValues insertValues = createContentValues(name, url, serverId, mimetype);
		
		return db.insert(FavoriteSchema.TABLENAME, null, insertValues);
	}
	
	public boolean delete(long id) {
		return db.delete(FavoriteSchema.TABLENAME, FavoriteSchema.COLUMN_ID + "=" + id, null) > 0;
	}
	
	public List<Favorite> findAll() {
		Cursor c = db.query(
				FavoriteSchema.TABLENAME, new String[] { FavoriteSchema.COLUMN_ID, FavoriteSchema.COLUMN_NAME, FavoriteSchema.COLUMN_URL, FavoriteSchema.COLUMN_SERVERID, FavoriteSchema.COLUMN_MIMETYPE },
				null, null, null, null, null);
		return cursorToFavorites(c);
	}
	
	public List<Favorite> findAll(Long serverId) {
		Cursor c = db.query(
				FavoriteSchema.TABLENAME, new String[] { FavoriteSchema.COLUMN_ID, FavoriteSchema.COLUMN_NAME, FavoriteSchema.COLUMN_URL, FavoriteSchema.COLUMN_SERVERID, FavoriteSchema.COLUMN_MIMETYPE },
				FavoriteSchema.COLUMN_SERVERID + " = " + serverId, null, null, null, null);
		return cursorToFavorites(c);
	}
	
	public Favorite findById(long id) {
		Cursor c = db.query(FavoriteSchema.TABLENAME, null, FavoriteSchema.COLUMN_ID + " like " + id, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}		
		return cursorToFavorite(c);
	}
	
	public boolean isPresentByURL(String url) {
		Cursor c = db.query(FavoriteSchema.TABLENAME, null, FavoriteSchema.COLUMN_URL + " = '" + url + "'", null, null, null, null);
		if (c != null) {
			if (c.getCount() == 1){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private ContentValues createContentValues(String name, String url, long repoId, String mimetype) {
		ContentValues updateValues = new ContentValues();
		
		updateValues.put(FavoriteSchema.COLUMN_NAME, name);
		updateValues.put(FavoriteSchema.COLUMN_URL, url);
		updateValues.put(FavoriteSchema.COLUMN_SERVERID, repoId);
		updateValues.put(FavoriteSchema.COLUMN_MIMETYPE, mimetype);
		return updateValues;
	}
	
	private ArrayList<Favorite> cursorToFavorites(Cursor c){
		if (c.getCount() == 0){
			return new ArrayList<Favorite>();
		}
		
		ArrayList<Favorite> servers = new ArrayList<Favorite>(c.getCount());
		c.moveToFirst();
		
		do {
			Favorite favs = createFromCursor(c);
			servers.add(favs);
		} while (c.moveToNext());
		c.close();
		return servers;
	}

	private Favorite createFromCursor(Cursor c) {
		Favorite fav = new Favorite(
				c.getInt(FavoriteSchema.COLUMN_ID_ID), 
				c.getString(FavoriteSchema.COLUMN_NAME_ID), 
				c.getString(FavoriteSchema.COLUMN_URL_ID), 
				c.getInt(FavoriteSchema.COLUMN_SERVERID_ID),
				c.getString(FavoriteSchema.COLUMN_MIMETYPE_ID) 
				);
		return fav;
	}
	
	private Favorite cursorToFavorite(Cursor c){
		if (c.getCount() == 0){
			return null;
		}
		Favorite fav = createFromCursor(c);
		c.close();
		return fav;
	}

}
