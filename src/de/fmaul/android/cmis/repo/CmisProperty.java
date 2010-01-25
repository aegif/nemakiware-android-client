package de.fmaul.android.cmis.repo;

import android.os.Parcel;
import android.os.Parcelable;

public class CmisProperty implements Parcelable {

	private final String type;
	private final String definitionId;
	private final String localName;
	private final String displayName;
	private final String value;

	public CmisProperty(String type, String definitionId, String localName,
			String displayName, String value) {
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
