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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

public class CmisItemCollection {

	private List<CmisItem> items = new ArrayList<CmisItem>();
	private String upLink;
	private String title;



	private CmisItemCollection() {
	}

	public List<CmisItem> getItems() {
		return items;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getUpLink() {
		return upLink;
	}

	public static CmisItemCollection createFromFeed(Document doc) {
		CmisItemCollection cic = new CmisItemCollection();
		cic.parseEntries(doc);
		return cic;
	}

	@SuppressWarnings("unchecked")
	private void parseEntries(Document doc) {
		List<Element> entries = doc.getRootElement().elements("entry");
		for (Element entry : entries) {
			items.add(CmisItem.createFromFeed(entry));
		}

	}

	public static CmisItemCollection emptyCollection() {
		CmisItemCollection cmi = new CmisItemCollection();
		cmi.title = "";
		cmi.upLink = "";
		return cmi;
	}

}
