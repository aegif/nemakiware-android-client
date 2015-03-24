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
package jp.aegif.android.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Application;
import jp.aegif.android.cmis.repo.CmisItemCollection;
import jp.aegif.android.cmis.repo.CmisPropertyFilter;
import jp.aegif.android.cmis.repo.CmisRepository;
import jp.aegif.android.cmis.repo.DownloadItem;
import jp.aegif.android.cmis.utils.MimetypeUtils;

public class CmisApp extends Application {

	private static final String TAG = "CmisApp";
	
	private CmisRepository repository;
	private Prefs prefs;
	private CmisItemCollection items;
	private ListCmisFeedActivitySave savedContextItems;
	private CmisPropertyFilter cmisPropertyFilter;
	private Map<String,Integer> mimetypesMap;
	private List<DownloadItem> downloadedFiles = new ArrayList<DownloadItem>(5);
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		mimetypesMap = MimetypeUtils.createIconMap();
	}
	
	public CmisRepository getRepository() {
		return repository;
	}
	
	public void setRepository(CmisRepository repository) {
		this.repository = repository;
	}
	
	public Prefs getPrefs() {
		return prefs;
	}

	public void setPrefs(Prefs prefs) {
		this.prefs = prefs;
	}

	public void setItems(CmisItemCollection items) {
		this.items = items;
	}

	public CmisItemCollection getItems() {
		return items;
	}

	public void setCmisPropertyFilter(CmisPropertyFilter cmisPropertyFilter) {
		this.cmisPropertyFilter = cmisPropertyFilter;
	}

	public CmisPropertyFilter getCmisPropertyFilter() {
		return cmisPropertyFilter;
	}

	public void setMimetypesMap(Map<String,Integer> mimetypesMap) {
		this.mimetypesMap = mimetypesMap;
	}

	public Map<String,Integer> getMimetypesMap() {
		return mimetypesMap;
	}

	public void setSavedContextItems(ListCmisFeedActivitySave savedContextItems) {
		this.savedContextItems = savedContextItems;
	}

	public ListCmisFeedActivitySave getSavedContextItems() {
		return savedContextItems;
	}

	public void setDownloadedFiles(List<DownloadItem> downloadedFiles) {
		this.downloadedFiles = downloadedFiles;
	}

	public List<DownloadItem> getDownloadedFiles() {
		return downloadedFiles;
	}
	
}
