/*
 * Copyright (C) 2010 Jean Marie PASCAL
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
package de.fmaul.android.cmis.repo;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import de.fmaul.android.cmis.utils.StorageUtils;

public class CmisItemLazy implements Serializable {

	private static final long serialVersionUID = -8274543604325636130L;
	
	protected String title;
	protected String downLink;
	protected String author;
	protected String contentUrl;
	protected String selfUrl;
	protected String parentUrl;
	protected String id;
	protected String mimeType;
	protected String size;
	protected String path;

	protected Date modificationDate;
	
	public CmisItemLazy(){
	}
	
	public CmisItemLazy(CmisItem item) {
		super();
		this.title = item.getTitle();
		this.downLink =  item.getDownLink();
		this.parentUrl =  item.getParentUrl();
		this.author =  item.getAuthor();
		this.contentUrl =  item.getContentUrl();
		this.selfUrl =  item.getSelfUrl();
		this.id =  item.getId();
		this.mimeType =  item.getMimeType();
		this.size =  item.getSize();
		this.modificationDate =  item.getModificationDate();
		this.path = item.getPath();
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return getTitle();
	}
	
	public String getSelfUrl() {
		return selfUrl;
	}

	public String getAuthor() {
		return author;
	}

	public boolean hasChildren() {
		return downLink != null && downLink.length() > 0;
	}

	public String getDownLink() {
		return downLink;
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
	
	public String getSize() {
		return size;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getParentUrl() {
		return parentUrl;
	}
	
	public File getContent(String repositoryWorkspace){
		return StorageUtils.getStorageFile(repositoryWorkspace, StorageUtils.TYPE_CONTENT, getId(), getTitle());
	}
	
}
