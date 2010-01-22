package de.fmaul.android.cmis.repo;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;


public class CmisItemCollection {

	List<CmisItem> items = new ArrayList<CmisItem>(); 
	
	String upLink;
	String title;
	
	private CmisItemCollection() {
	}
	
	public List<CmisItem> getItems() {
		return items;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUpLink() {
		return upLink;
	}
	
	
	public static CmisItemCollection createFromFeed(Document doc) {
		CmisItemCollection cic = new CmisItemCollection();
		cic.parseFeed(doc);
		cic.parseEntries(doc);
		return cic;
	}

	private void parseEntries(Document doc) {
		List<Element> entries = doc.getRootElement().elements("entry");
		for (Element entry : entries) {
			items.add(CmisItem.createFromFeed(entry));
		}
		
	}

	private void parseFeed(Document doc) {
		List<Element> feedLinks = doc.getRootElement().elements("link");
		for (Element link : feedLinks) {
			if ("up".equalsIgnoreCase(link.attributeValue("rel"))) {
				upLink = link.attributeValue("href");
				items.add(CmisItem.create("..", upLink));
			}
		}
		title = doc.getRootElement().elementText("title");
	}

	public static CmisItemCollection emptyCollection() {
		CmisItemCollection cmi = new CmisItemCollection();
		cmi.title = "";
		cmi.upLink  = "";
		return cmi;
	}

}
