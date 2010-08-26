/*
 * Copyright (C) 2010 Jean Marie PASCAL
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.text.TextUtils;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.utils.ListUtils;

public class CmisPropertyFilter {
	
	
	private Map<String, CmisProperty> extraProps= new LinkedHashMap<String, CmisProperty>();
	private Map<String, CmisProperty> objectProps= new LinkedHashMap<String, CmisProperty>(); 
	private Map<String, CmisProperty> docProps= new LinkedHashMap<String, CmisProperty>(); 
	private Map<String, CmisProperty> contentProps= new LinkedHashMap<String, CmisProperty>(); 
	private Map<String, CmisProperty> folderProps= new LinkedHashMap<String, CmisProperty>();
	private Map<String, CmisProperty> allProps = new LinkedHashMap<String, CmisProperty>(); 
	
	private Map<String, Map<String, CmisProperty>> filters = new HashMap<String, Map<String,CmisProperty>>(5);
	private Map<String[], Map<String, CmisProperty>> filterToProps = new HashMap<String[], Map<String,CmisProperty>>(5);
	
	private CmisTypeDefinition typeDefinition;
	private String[] filterSelected;
	
	
	public CmisPropertyFilter(List<CmisProperty> propList, CmisTypeDefinition typeDefinition){
		this.typeDefinition = typeDefinition;
		initFilters();
		dispatch(propList);
	}
	
	public List<Map<String, ?>> render(){
		return render(filterSelected);
	}
	
	
	public List<Map<String, ?>> render(String[] filterSelected){
		this.filterSelected = filterSelected;
		Map<String, CmisProperty> currentProps = allProps;
		if (filterToProps.containsKey(filterSelected)){
			currentProps = filterToProps.get(filterSelected);
		}
		
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for(Entry<String, CmisProperty> prop : currentProps.entrySet()) {
			if (prop.getValue() != null){
				list.add(ListUtils.createPair(getDisplayNameFromProperty(prop.getValue(), typeDefinition), prop.getValue().getValue()));
			}
		}
		return list;
	}
	
	private String getDisplayNameFromProperty(CmisProperty property, CmisTypeDefinition typeDefinition) {
		String name = property.getDisplayName();
		if (TextUtils.isEmpty(name)) {
		}
		name = typeDefinition.getDisplayNameForProperty(property);
		if (TextUtils.isEmpty(name)) {
			name = property.getDefinitionId();
		}
		return name.replaceAll("cmis:", "");
	}
	
	private void dispatch(List<CmisProperty> propList) {
		for (CmisProperty cmisProperty : propList) {
			String definitionId = cmisProperty.getDefinitionId();
			if (definitionId != null) {
				if (filters.get(definitionId) != null){
					filters.get(definitionId).put(definitionId, cmisProperty);
					allProps.put(definitionId, cmisProperty);
				} else {
					extraProps.put(definitionId, cmisProperty);
				}
			}
		}
		allProps.putAll(extraProps);
	}

	private void initFilters(){
		initPropsFilter(LIST_OBJECT, objectProps);
		initPropsFilter(LIST_FOLDER, folderProps);
		initPropsFilter(LIST_DOC, docProps);
		initPropsFilter(LIST_CONTENT, contentProps);
		initPropsFilter(LIST_EXTRA, extraProps);
		initAllPropsFilter();
		
	}
	
	private void initPropsFilter(String[] props, Map<String, CmisProperty> listProps){
		//LinkedHashMap<String, CmisProperty> hashMap = new LinkedHashMap<String, CmisProperty>(props.length);
		for (int i = 0; i < props.length; i++) {
			listProps.put(props[i], null);
			filters.put(props[i], listProps);
		}
		//listProps.putAll(hashMap;
		filterToProps.put(props, listProps);
	}
	
	private void initAllPropsFilter(){
		allProps.putAll(objectProps);
		allProps.putAll(folderProps);
		allProps.putAll(docProps);
		allProps.putAll(contentProps);
	}
	
	public static final String[] LIST_EXTRA = {
	};
	
	public static final String[] LIST_ALL = {
	};
	
	public static final String[] LIST_OBJECT = {
		CmisProperty.OBJECT_NAME,
		CmisProperty.OBJECT_ID,
		CmisProperty.OBJECT_TYPEID,
		CmisProperty.OBJECT_BASETYPEID,
		CmisProperty.OBJECT_CREATEDBY,
		CmisProperty.OBJECT_CREATIONDATE,
		CmisProperty.OBJECT_LASTMODIFIEDBY,
		CmisProperty.OBJECT_LASTMODIFICATION,
		CmisProperty.OBJECT_CHANGETOKEN
	};
	
	public static final String[] LIST_DOC = {
		CmisProperty.DOC_VERSIONLABEL,
		CmisProperty.DOC_ISLATESTEVERSION,
		CmisProperty.DOC_ISLATESTMAJORVERSION,
		CmisProperty.DOC_VERSIONSERIESID,
		CmisProperty.DOC_ISMAJORVERSION,
		CmisProperty.DOC_ISVERSIONCHECKEDOUT,
		CmisProperty.DOC_ISVERSIONCHECKEDOUTBY,
		CmisProperty.DOC_ISVERSIONCHECKEDOUTID,
		CmisProperty.DOC_ISIMMUTABLE,
		CmisProperty.DOC_CHECINCOMMENT
	};
	
	public static final String[] LIST_CONTENT = {
		CmisProperty.CONTENT_STREAMID,
		CmisProperty.CONTENT_STREAMLENGTH,
		CmisProperty.CONTENT_STREAMMIMETYPE,
		CmisProperty.CONTENT_STREAMFILENAME
	};
	
	public static final String[] LIST_FOLDER = {
		CmisProperty.FOLDER_PARENTID,
		CmisProperty.FOLDER_PATH,
		CmisProperty.FOLDER_ALLOWCHILDREN,
	};
	
	public static String[] concat(String[] A, String[] B) {
		String[] C = new String[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}
	
	public static String[] generateFilter(ArrayList<String[]> workingFilters){
		String[] C = {};
		for (String[] strings : workingFilters) {
			C = concat(C, strings);
		}
		return C;
	}
	
	public static String[] getFilter(CmisItemLazy item){
		if (CmisModel.CMIS_TYPE_FOLDER.equals(item.getBaseType())){
			return concat(LIST_OBJECT, LIST_FOLDER);
		} else if (CmisModel.CMIS_TYPE_DOCUMENTS.equals(item.getBaseType())){
			if (item.getSize() != null && item.getSize().equals("0") == false){
				return concat(LIST_OBJECT,concat(LIST_DOC, LIST_CONTENT));
			} else {
				return concat(LIST_OBJECT, LIST_DOC);
			}
			
		} else {
			return LIST_OBJECT;
		}
	}
	
	public static ArrayList<String[]> getFilters(CmisItemLazy item) {
		ArrayList<String[]> filters = new ArrayList<String[]>(5);
		filters.add(LIST_OBJECT);
		if (CmisModel.CMIS_TYPE_FOLDER.equals(item.getBaseType())){
			filters.add(LIST_FOLDER);
		} else if (CmisModel.CMIS_TYPE_DOCUMENTS.equals(item.getBaseType())){
			filters.add(LIST_DOC);
			filters.add(LIST_CONTENT);
		} 
		filters.add(LIST_EXTRA);
		filters.add(LIST_ALL);
		return filters;
	}
	
	private static ArrayList<String> getFilters(Activity activity, CmisItemLazy item) {
		ArrayList<String> filters = new ArrayList<String>(5);
		filters.add(activity.getText(R.string.item_filter_object).toString());
		if (CmisModel.CMIS_TYPE_FOLDER.equals(item.getBaseType())){
			filters.add(activity.getText(R.string.item_filter_folder).toString());
		} else if (CmisModel.CMIS_TYPE_DOCUMENTS.equals(item.getBaseType())){
			filters.add(activity.getText(R.string.item_filter_document).toString());
			filters.add(activity.getText(R.string.item_filter_content).toString());
		} 
		filters.add(activity.getText(R.string.item_filter_extra).toString());
		filters.add(activity.getText(R.string.item_filter_all).toString());
		return filters;
	}
	
	public static CharSequence[] getFiltersLabel(Activity activity, CmisItemLazy item) {
		ArrayList<String> filters = getFilters(activity, item);
		return filters.toArray(new CharSequence[filters.size()]);
	}
	
	
	
	public static ArrayList<String[]> createFilters() {
		ArrayList<String[]> filters = new ArrayList<String[]>(5); 
		filters.add(LIST_OBJECT);
		filters.add(LIST_FOLDER);
		filters.add(LIST_DOC);
		filters.add(LIST_CONTENT);
		filters.add(LIST_EXTRA);
		filters.add(LIST_ALL);
		return filters;
	}
	
}
