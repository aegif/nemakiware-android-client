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

import android.app.ListActivity;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import de.fmaul.android.cmis.repo.CmisItemLazy;

public class ListCmisFeedActivitySave extends ListActivity {

	public CmisItemLazy getItem() {
		return item;
	}
	public CmisItemLazy getItemParent() {
		return itemParent;
	}
	public CmisItemCollection getItems() {
		return items;
	}
	public ArrayList<CmisItemLazy> getCurrentStack() {
		return currentStack;
	}
	public ListCmisFeedActivitySave(CmisItemLazy item, CmisItemLazy itemParent, CmisItemCollection items, ArrayList<CmisItemLazy> currentStack) {
		super();
		this.item = item;
		this.itemParent = itemParent;
		this.items = items;
		this.currentStack = currentStack;
	}
	private CmisItemLazy item;
	private CmisItemLazy itemParent;
	private CmisItemCollection items;
	private ArrayList<CmisItemLazy> currentStack =  new ArrayList<CmisItemLazy>();
	
	
}