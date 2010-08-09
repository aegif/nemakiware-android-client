package de.fmaul.android.cmis.model;

public class Favorite {
	public final long id; 
	public final String name;
	public final String url;
	public final long serverId;
	
	public Favorite(long id, String name, String url, long serverId) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.serverId = serverId;
	} 
}
