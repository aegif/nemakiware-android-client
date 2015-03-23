/*
 * Copyright (C) 2010 Florian Maul
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

import android.os.Parcel;
import android.os.Parcelable;

public class CmisProperty implements Parcelable {

	
	public static final String OBJECT_ID = "cmis:objectId";
	public static final String OBJECT_TYPEID = "cmis:objectTypeId";
	public static final String OBJECT_BASETYPEID = "cmis:baseTypeId";
	public static final String OBJECT_NAME = "cmis:name";
	public static final String OBJECT_CREATIONDATE = "cmis:creationDate";
	public static final String OBJECT_LASTMODIFIEDBY = "cmis:lastModifiedBy";
	public static final String OBJECT_CREATEDBY = "cmis:createdBy";
	public static final String OBJECT_LASTMODIFICATION = "cmis:lastModificationDate";
	public static final String OBJECT_CHANGETOKEN = "cmis:changeToken";
	
	public static final String DOC_ISLATESTEVERSION= "cmis:isLatestVersion";
	public static final String DOC_ISLATESTMAJORVERSION = "cmis:isLatestMajorVersion";
	public static final String DOC_VERSIONSERIESID = "cmis:versionSeriesId";
	public static final String DOC_VERSIONLABEL = "cmis:versionLabel";
	public static final String DOC_ISMAJORVERSION = "cmis:isMajorVersion";
	public static final String DOC_ISVERSIONCHECKEDOUT = "cmis:isVersionSeriesCheckedOut";
	public static final String DOC_ISVERSIONCHECKEDOUTBY = "cmis:versionSeriesCheckedOutBy";
	public static final String DOC_ISVERSIONCHECKEDOUTID = "cmis:versionSeriesCheckedOutId";
	public static final String DOC_ISIMMUTABLE = "cmis:isImmutable";
	public static final String DOC_CHECINCOMMENT = "cmis:checkinComment";
	
	public static final String CONTENT_STREAMID = "cmis:contentStreamId";
	public static final String CONTENT_STREAMLENGTH = "cmis:contentStreamLength";
	public static final String CONTENT_STREAMMIMETYPE = "cmis:contentStreamMimeType";
	public static final String CONTENT_STREAMFILENAME = "cmis:contentStreamFileName";
	
	public static final String FOLDER_PARENTID = "cmis:parentId";
	public static final String FOLDER_PATH = "cmis:path";
	public static final String FOLDER_ALLOWCHILDREN = "cmis:allowedChildObjectTypeIds";
	
	
	private final String type;
	private final String definitionId;
	private final String localName;
	private final String displayName;
	private final String value;

	public CmisProperty(String type, String definitionId, String localName, String displayName, String value) {
		this.type = type;
		this.definitionId = definitionId;
		this.localName = localName;
		this.displayName = displayName;
		this.value = value;
	}

	public CmisProperty(Parcel in) {
		type = in.readString();
		definitionId = in.readString();
		localName = in.readString();
		displayName = in.readString();
		value = in.readString();
	}

	public String getDefinitionId() {
		return definitionId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getLocalName() {
		return localName;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(definitionId);
		dest.writeString(localName);
		dest.writeString(displayName);
		dest.writeString(value);

	}

	public static final Parcelable.Creator<CmisProperty> CREATOR = new Parcelable.Creator<CmisProperty>() {
		public CmisProperty createFromParcel(Parcel in) {
			return new CmisProperty(in);
		}

		public CmisProperty[] newArray(int size) {
			return new CmisProperty[size];
		}
	};

}
