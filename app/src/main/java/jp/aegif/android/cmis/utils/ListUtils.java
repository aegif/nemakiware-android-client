/*
 * Copyright (C) 2010 Florian Maul & Jean Marie PASCAL
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
package jp.aegif.android.cmis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.aegif.android.cmis.repo.CmisProperty;

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
