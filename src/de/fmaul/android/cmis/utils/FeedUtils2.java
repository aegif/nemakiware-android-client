package de.fmaul.android.cmis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLEngineResult.Status;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisModel;
import de.fmaul.android.cmis.repo.CmisProperty;

public class FeedUtils2 {
	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public static final String CMISRA_NAMESPACE = "http://docs.oasis-open.org/ns/cmis/restatom/200908/";
	public static final String CMIS_NAMESPACE = "http://docs.oasis-open.org/ns/cmis/core/200908/";

	public static class HandlerFactory {
		CmisItem item = null;
		CollectionStats stats;

		public ContentHandler newContentHandler(final List<CmisItem> items, CollectionStats theStats) {
			this.stats = theStats;
			RootElement root = new RootElement(ATOM_NAMESPACE, "feed");

			root.getChild(CMISRA_NAMESPACE, "numItems").setEndTextElementListener(new EndTextElementListener() {
				@Override
				public void end(String text) {
					if (text != null){
						stats.numItems = Integer.parseInt(text);
					}
				}
			});
			
			root.getChild(ATOM_NAMESPACE, "title").setEndTextElementListener(
					new EndTextElementListener() {
						public void end(String text) {
							stats.title = text;
						}
					});
			
			Element entry = root.getChild(ATOM_NAMESPACE, "entry");
			entry.setElementListener(new ElementListener() {
				@Override
				public void end() {
					if (item.getProperties().get(CmisProperty.CONTENT_STREAMLENGTH) != null) {
						item.setSize(item.getProperties().get(
								CmisProperty.CONTENT_STREAMLENGTH).getValue());
					} else {
						item.setSize(null);
					}
					if (item.getProperties().get(CmisProperty.FOLDER_PATH) != null) {
						item.setPath(item.getProperties().get(CmisProperty.FOLDER_PATH)
								.getValue());
					}

					if (item.getProperties().get(CmisProperty.OBJECT_BASETYPEID) != null) {
						item.setBaseType(item.getProperties().get(
								CmisProperty.OBJECT_BASETYPEID).getValue());
					}


					items.add(item);
					item = null;
				}

				@Override
				public void start(Attributes attributes) {
					item = new CmisItem();
				}
			});

			entry.getChild(ATOM_NAMESPACE, "title").setEndTextElementListener(
					new EndTextElementListener() {
						public void end(String text) {
							item.setTitle(text);
						}
					});

			entry.getChild(ATOM_NAMESPACE, "id").setEndTextElementListener(
					new EndTextElementListener() {
						public void end(String text) {
							item.setId(text);
						}
					});

			entry.getChild(ATOM_NAMESPACE, "author").getChild(ATOM_NAMESPACE,
					"name").setEndTextElementListener(
					new EndTextElementListener() {
						public void end(String text) {
							item.setAuthor(text);
						}
					});

			entry.getChild(ATOM_NAMESPACE, "updated")
					.setEndTextElementListener(new EndTextElementListener() {
						public void end(String text) {
							item.setModificationDate(parseXmlDate(text));
						}
					});

			Element content = entry.getChild(ATOM_NAMESPACE, "content");
			content.setStartElementListener(new StartElementListener() {
				@Override
				public void start(Attributes attributes) {
					item.setContentUrl(attributes.getValue("src"));
					item.setMimeType(attributes.getValue("type"));
				}
			});

			Element link = entry.getChild(ATOM_NAMESPACE, "link");
			link.setStartElementListener(new StartElementListener() {
				@Override
				public void start(Attributes attributes) {

					final String rel = attributes.getValue("rel");
					final String href = attributes.getValue("href");
					final String type = attributes.getValue("type");

					if (CmisModel.ITEM_LINK_DOWN.equals(rel)) {
						if (type != null
								&& type.startsWith("application/atom+xml")) {
							item.setDownLink(href);
						}
					} else if (CmisModel.ITEM_LINK_SELF.equals(rel)) {
						item.setSelfUrl(href);
					} else if (CmisModel.ITEM_LINK_UP.equals(rel)) {
						item.setParentUrl(href);
					}
				}
			});

			Element cmisProperties = entry.getChild(CMISRA_NAMESPACE, "object")
					.getChild(CMIS_NAMESPACE, "properties");

			registerPropertyListener(cmisProperties, "propertyString");
			registerPropertyListener(cmisProperties, "propertyId");
			registerPropertyListener(cmisProperties, "propertyDateTime");
			registerPropertyListener(cmisProperties, "propertyInteger");
			
			return root.getContentHandler();
		}

		private void registerPropertyListener(Element cmisProperties,
				String propertyType) {
			Element propertyString = cmisProperties.getChild(CMIS_NAMESPACE,
					propertyType);
			PropertyListener propertyListener = new PropertyListener(
					propertyType);
			propertyString.setElementListener(propertyListener);
			propertyString.getChild(CMIS_NAMESPACE, "value")
					.setEndTextElementListener(propertyListener);
		}

		private class PropertyListener implements EndTextElementListener,
				ElementListener {
			String propertyDefinitionId;
			String localName;
			String displayName;
			String textValue;
			private final String type;

			public PropertyListener(String type) {
				this.type = type;
			}

			public void start(Attributes attributes) {
				propertyDefinitionId = attributes.getValue("propertyDefinitionId");
				localName = attributes.getValue("localName");
				displayName = attributes
						.getValue("displayName");
			}

			public void end() {
				CmisProperty cmisProperty = new CmisProperty(type,
						propertyDefinitionId, localName, displayName, textValue);
				Map<String, CmisProperty> properties = item.getProperties();
				properties.put(propertyDefinitionId, cmisProperty);
			}

			@Override
			public void end(String text) {
				textValue = text;
			}
		}

		private Date parseXmlDate(String date) {
			// 2009-11-03T11:55:39.495Z
			Date modfiedDate = null;
			SimpleDateFormat df = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.S'Z'");
			try {
				modfiedDate = df.parse(date);
			} catch (ParseException e) {
				// meh
			}
			return modfiedDate;
		}
	}

	public static CmisItemCollection getCollectionFromFeed(InputStream feedInputStream) {
		List<CmisItem> cil = new ArrayList<CmisItem>();
		CollectionStats stats = new CollectionStats();
		
		try {
			Xml.parse(feedInputStream, Xml.Encoding.UTF_8, newContentHandler(cil, stats));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new CmisItemCollection(cil, stats.numItems, stats.title);
		
	}
		
	public static CmisItemCollection getCollectionFromFeed(final String feedUrl, String user, String password) {

			InputStream is;
			try {
				is = HttpUtils.getWebRessourceAsStream(feedUrl,
						user, password);
				return getCollectionFromFeed(is);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}

	private static ContentHandler newContentHandler(List<CmisItem> items, CollectionStats stats) {
		return new HandlerFactory().newContentHandler(items, stats);
	}
	
	static class CollectionStats {
		int numItems;
		String title;
	}


}
