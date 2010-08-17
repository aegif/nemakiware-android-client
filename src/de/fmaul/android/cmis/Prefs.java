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
package de.fmaul.android.cmis;

public class Prefs {

	public static  final int LISTVIEW = 1;
	public static final int GRIDVIEW = 2;
	
	private int dataView;
	private String downloadFolder;
	
	
	public Prefs(int dataView) {
		super();
		this.dataView = dataView;
	}

	
	public Prefs(int dataView, String downloadFolder) {
		super();
		this.dataView = dataView;
		this.setDownloadFolder(downloadFolder);
	}


	public int getDataView() {
		return dataView;
	}
	
	public void setDataView(int dataView) {
		this.dataView = dataView;
	}


	public void setDownloadFolder(String downloadFolder) {
		this.downloadFolder = downloadFolder;
	}


	public String getDownloadFolder() {
		return downloadFolder;
	}
	
	
	
	
}
