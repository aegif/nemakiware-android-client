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
package jp.aegif.android.cmis.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	Map<String, CmisPropertyTypeDefinition> propertyDefinition = new HashMap<String, CmisPropertyTypeDefinition>();

	public static CmisTypeDefinition createFromFeed(Document doc) {
		CmisTypeDefinition td = new CmisTypeDefinition();

		final Namespace CMISRA = Namespace.get("http://docs.oasis-open.org/ns/cmis/restatom/200908/");

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
				CmisPropertyTypeDefinition propTypeDef = CmisPropertyTypeDefinition.createFromElement(element);
				td.propertyDefinition.put(propTypeDef.getId(), propTypeDef);
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

	public Map<String, CmisPropertyTypeDefinition> getPropertyDefinition() {
		return propertyDefinition;
	}

	private CmisPropertyTypeDefinition getTypeDefinitionForProperty(CmisProperty property) {
		return getPropertyDefinition().get(property.getDefinitionId());
	}

	public String getDisplayNameForProperty(CmisProperty property) {
		CmisPropertyTypeDefinition propTypeDef = getTypeDefinitionForProperty(property);

		if (propTypeDef != null) {
			return propTypeDef.getDisplayName();
		}
		return "";
	}

}
