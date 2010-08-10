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
