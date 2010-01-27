package de.fmaul.android.cmis.repo;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class CmisTypeDefinition {

	private String id;
	private String localName;
	private String localNamespace;
	private String displayName;
	private String queryName;
	private String description;
	private String baseId;
	private boolean creatable;
	private boolean fileable;
	private boolean queryable;
	private boolean fulltextIndexed;
	private boolean includedInSupertypeQuery;
	private boolean controllablePolicy;
	private boolean controllableACL;
	private boolean versionable;
	private String contentStreamAllowed;

	List<CmisPropertyTypeDefinition> propertyDefinition = new ArrayList<CmisPropertyTypeDefinition>();

	public static CmisTypeDefinition createFromFeed(Document doc) {
		CmisTypeDefinition td = new CmisTypeDefinition();
		
		final  Namespace CMISRA = Namespace
		.get("http://docs.oasis-open.org/ns/cmis/restatom/200908/");

  		final QName CMISRA_TYPE = QName.get("type", CMISRA);
		Element type = doc.getRootElement().element(CMISRA_TYPE);
		
		td.id = type.elementTextTrim("id");
		td.localName = type.elementTextTrim("localName");
		td.localNamespace = type.elementTextTrim("localNamespace");
		td.displayName = type.elementTextTrim("displayName");
		td.queryName = type.elementTextTrim("queryName");
		td.description = type.elementTextTrim("description");
		td.baseId = type.elementTextTrim("baseId");
		td.creatable = Boolean.valueOf(type.elementTextTrim("creatable"));
		td.fileable = Boolean.valueOf(type.elementTextTrim("fileable"));
		td.queryable = Boolean.valueOf(type.elementTextTrim("queryable"));
		td.fulltextIndexed = Boolean.valueOf(type.elementTextTrim("fulltextIndexed"));
		td.includedInSupertypeQuery = Boolean.valueOf(type.elementTextTrim("includedInSupertypeQuery"));
		td.controllablePolicy = Boolean.valueOf(type.elementTextTrim("controllablePolicy"));
		td.controllableACL = Boolean.valueOf(type.elementTextTrim("controllableACL"));
		td.versionable = Boolean.valueOf(type.elementTextTrim("versionable"));
		td.contentStreamAllowed = type.elementTextTrim("contentStreamAllowed");
		
		List<Element> allElements = doc.getRootElement().element(CMISRA_TYPE).elements();
		for (Element element : allElements) {
			if (element.getName().startsWith("property")) {
				td.propertyDefinition.add(CmisPropertyTypeDefinition.createFromElement(element));
			}
		}
		
		return td;
	}

	public String getId() {
		return id;
	}

	public String getLocalName() {
		return localName;
	}

	public String getLocalNamespace() {
		return localNamespace;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getDescription() {
		return description;
	}

	public String getBaseId() {
		return baseId;
	}

	public boolean isCreatable() {
		return creatable;
	}

	public boolean isFileable() {
		return fileable;
	}

	public boolean isQueryable() {
		return queryable;
	}

	public boolean isFulltextIndexed() {
		return fulltextIndexed;
	}

	public boolean isIncludedInSupertypeQuery() {
		return includedInSupertypeQuery;
	}

	public boolean isControllablePolicy() {
		return controllablePolicy;
	}

	public boolean isControllableACL() {
		return controllableACL;
	}

	public boolean isVersionable() {
		return versionable;
	}

	public String getContentStreamAllowed() {
		return contentStreamAllowed;
	}

	public List<CmisPropertyTypeDefinition> getPropertyDefinition() {
		return propertyDefinition;
	}
	
	
}
