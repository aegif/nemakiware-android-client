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
package de.fmaul.android.cmis.model;

public class Favorite {
	public final long id; 
	public final String name;
	public final String url;
	public final long serverId;
	public final String mimetype;
	
	public Favorite(long id, String name, String url, long serverId, String mimetype) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.serverId = serverId;
		this.mimetype = mimetype;
	} 
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public long getServerId() {
		return serverId;
	}
	
	public String getMimetype() {
		return mimetype;
	}
	
}
