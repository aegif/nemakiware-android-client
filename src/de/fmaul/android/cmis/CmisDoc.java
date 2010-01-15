package de.fmaul.android.cmis;

import java.util.Date;


public class CmisDoc {

	private final String title;
	private final String linkChildren;
	private final String author;
	private final String contentUrl;
	private final String id;
	private final String mimeType;
	private final Date modificationDate;

	public CmisDoc(String id, String title, String linkChildren, String author,
			String contentUrl, String mimeType, Date modificationDate) {
		super();
		this.id = id;
		this.title = title;
		this.linkChildren = linkChildren;
		this.author = author;
		this.contentUrl = contentUrl;
		this.mimeType = mimeType;
		this.modificationDate = modificationDate;
	}

	public String getLinkChildren() {
		return linkChildren;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public String getAuthor() {
		return author;
	}

	boolean hasChildren() {
		return linkChildren != null && linkChildren.length() > 0;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public String getId() {
		return id;
	}

	public String getMimeType() {
		return mimeType;
	}
	
	public Date getModificationDate() {
		return modificationDate;
	}

}
