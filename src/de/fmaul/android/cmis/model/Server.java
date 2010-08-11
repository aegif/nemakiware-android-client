package de.fmaul.android.cmis.model;

import java.io.Serializable;

public class Server implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String INFO_GENERAL = "serverInfoGeneral";
	public static final String INFO_CAPABILITIES = "serverInfoCapabilites";
	public static final String INFO_ACL_CAPABILITIES = "serverInfoACL";
	
	private long id;
    private String name;
    private String url;
	private String username;
	private String password;
	private String workspace;
	
	public Server(long id, String name, String url, String username, String password, String workspace) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.username = username;
		this.password = password;
		this.workspace = workspace;
	}
	
	
	public Server() {
	}


	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	public String getWorkspace() {
		return workspace;
	}
}
