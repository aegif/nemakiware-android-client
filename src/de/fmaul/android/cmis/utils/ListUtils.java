package de.fmaul.android.cmis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fmaul.android.cmis.repo.CmisProperty;

public class ListUtils {

	public static List<Map<String, ?>> buildListOfNameValueMaps(List<CmisProperty> propList) {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for (CmisProperty cmisProperty : propList) {
			list.add(createPair(cmisProperty.getDisplayName(), cmisProperty.getValue()));
		}
		return list;
	}
	
	public static Map<String, ?> createPair(String name, String value) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("name", name);
		hashMap.put("value", value);
		return hashMap;
	}
	
	
}
