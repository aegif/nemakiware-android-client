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
package de.fmaul.android.cmis;

import java.util.ArrayList;

import de.fmaul.android.cmis.model.Server;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ServerAdapter extends ArrayAdapter<Server> {

	private final Context context;
	private ArrayList<Server> items;

	public ServerAdapter(Context context, int textViewResourceId, ArrayList<Server> servers) {
		super(context, textViewResourceId,servers);
		this.items = servers;
		this.context = context;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.server_row, null);
		}
		Server s = items.get(position);
		if (s != null) {
			TextView tv = (TextView) v.findViewById(R.id.rowserver);
			ImageView iv = (ImageView) v.findViewById(R.id.iconserver); 
			if (tv != null) {             
				tv.setText(s.getName());                             
			}
			if(iv != null){
				iv.setImageResource(R.drawable.repository);
			}
		}
		return v;
	}
}
