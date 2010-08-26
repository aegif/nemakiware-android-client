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
	private int numItems;
	private String title;


	private CmisItemCollection() {
	}

	public CmisItemCollection(List<CmisItem> items, int numItems, String title) {
		super();
		this.items = items;
		this.numItems = numItems;
		this.title = title;
	}

	public List<CmisItem> getItems() {
		return items;
	}
	
	public String getTitle() {
		return title;
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
		return cmi;
	}
	
	public int getNumItems() {
		return numItems;
	}
	

}
