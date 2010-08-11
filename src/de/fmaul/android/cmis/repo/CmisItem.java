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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import de.fmaul.android.cmis.utils.FeedUtils;

public class CmisItem extends CmisItemLazy {

	public CmisItem(CmisItem item) {
		super(item);
	}
	
	private CmisItem() {
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private Map<String, CmisProperty> properties;

	public Map<String, CmisProperty> getProperties() {
		return properties;
	}

	public static CmisItem createFromFeed(Element entry) {
		CmisItem cmi = new CmisItem();
		cmi.parseEntry(entry);
		return cmi;
	}

	private void parseEntry(Element entry) {
		title = entry.element("title").getText();
		id = entry.element("id").getText();
		downLink = "";
		contentUrl = "";
		mimeType = "";
		author = entry.element("author").element("name").getText();
		modificationDate = parseXmlDate(entry.element("updated").getText());

		Element contentElement = entry.element("content");
		if (contentElement != null) {
			contentUrl = contentElement.attributeValue("src");
			mimeType = contentElement.attributeValue("type");
		}

		for (Element link : (List<Element>) entry.elements("link")) {
			if ("down".equals(link.attribute("rel").getText())) {

				if (link.attribute("type").getText().startsWith("application/atom+xml")) {
					downLink = link.attribute("href").getText();
				}
			} else if ("self".equals(link.attribute("rel").getText())) {
				selfUrl = link.attribute("href").getText();
			}
		}
		
		properties = FeedUtils.getCmisPropertiesForEntry(entry);
		
		if (properties.get("cmis:contentStreamLength") != null){
			size = properties.get("cmis:contentStreamLength").getValue();
		} else {
			size = null;
		}
	}

	private Date parseXmlDate(String date) {
		// 2009-11-03T11:55:39.495Z
		Date modfiedDate = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		try {
			modfiedDate = df.parse(date);
		} catch (ParseException e) {
			// meh
		}
		return modfiedDate;
	}

	public static CmisItem create(String title, String upLink) {
		CmisItem cmisItem = new CmisItem();
		cmisItem.title = title;
		cmisItem.downLink = upLink;
		cmisItem.id = "";
		cmisItem.author = "";
		cmisItem.contentUrl = null;
		cmisItem.properties = new HashMap<String, CmisProperty>();
		return cmisItem;
	}
	
	public static CmisItem create(String title, String id,  String mimeType, String contentUrl, String selfUrl) {
		CmisItem cmisItem = new CmisItem();
		cmisItem.title = title;
		cmisItem.id = id;
		cmisItem.mimeType = mimeType;
		cmisItem.contentUrl = contentUrl;
		cmisItem.selfUrl = selfUrl;
		cmisItem.properties = new HashMap<String, CmisProperty>();
		return cmisItem;
	}

}
