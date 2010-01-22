package de.fmaul.android.cmis.repo;

public class CmisProperty {

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
	
}
