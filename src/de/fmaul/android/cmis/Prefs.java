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

import java.util.ArrayList;

public class Prefs {

	public static  final int LISTVIEW = 1;
	public static final int GRIDVIEW = 2;
	
	private int dataView;
	private String downloadFolder;
	private boolean enableScan;
	private boolean confirmDownload;
	private String downloadFileSize;
	private ArrayList<Boolean> quickActionServer;
	
	public Prefs(int dataView) {
		super();
		this.dataView = dataView;
	}

	
	public Prefs(
			int dataView, 
			String downloadFolder, 
			boolean enableScan, 
			boolean confirmDownload, 
			String downloadFileSize,
			ArrayList<Boolean> quickActionServer) {
		super();
		this.dataView = dataView;
		this.downloadFolder = downloadFolder;
		this.enableScan = enableScan;
		this.confirmDownload = confirmDownload;
		this.downloadFileSize = downloadFileSize;
		this.quickActionServer = quickActionServer;
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


	public void setEnableScan(boolean enableScan) {
		this.enableScan = enableScan;
	}

	public boolean isEnableScan() {
		return enableScan;
	}


	public void setConfirmDownload(boolean confirmDownload) {
		this.confirmDownload = confirmDownload;
	}


	public boolean isConfirmDownload() {
		return confirmDownload;
	}


	public void setDownloadFileSize(String downloadFileSize) {
		this.downloadFileSize = downloadFileSize;
	}


	public String getDownloadFileSize() {
		return downloadFileSize;
	}


	public void setQuickActionServer(ArrayList<Boolean> quickActionServer) {
		this.quickActionServer = quickActionServer;
	}


	public ArrayList<Boolean> getQuickActionServer() {
		return quickActionServer;
	}
}
