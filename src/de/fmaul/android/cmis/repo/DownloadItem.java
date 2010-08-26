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

import de.fmaul.android.cmis.asynctask.AbstractDownloadTask;


public class DownloadItem {
	
	private CmisItemLazy item;
	private AbstractDownloadTask task;
	
	public DownloadItem(CmisItemLazy item, AbstractDownloadTask task) {
		super();
		this.item = item;
		this.task = task;
	}
	public CmisItemLazy getItem() {
		return item;
	}
	public void setItem(CmisItem item) {
		this.item = item;
	}
	public AbstractDownloadTask getTask() {
		return task;
	}
	public void setTask(AbstractDownloadTask task) {
		this.task = task;
	}
	



}
