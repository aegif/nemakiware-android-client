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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.utils.ActionItem;
import de.fmaul.android.cmis.utils.QuickAction;


public class ServerAdapter extends ArrayAdapter<Server> {

	private final Context context;
	private ArrayList<Server> items;
	private Server server;
	private ServerActivity serverActivity;
	
	private static final Integer ACTION_SERVER_OPEN = 0;
	private static final Integer ACTION_SERVER_INFO = 1;
	private static final Integer ACTION_SERVER_EDIT = 2;
	private static final Integer ACTION_SERVER_FAVORITE = 3;
	private static final Integer ACTION_SERVER_SEARCH = 4;
	private static final Integer ACTION_SERVER_DELETE = 5;
	
	
	public ServerAdapter(Context context, int textViewResourceId, ArrayList<Server> servers) {
		super(context, textViewResourceId,servers);
		this.items = servers;
		this.context = context;
		this.serverActivity = (ServerActivity) context;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.server_row, null);
		}
		server = items.get(position);
		if (server != null) {
			TextView tv = (TextView) v.findViewById(R.id.rowserver);
			ImageView iv = (ImageView) v.findViewById(R.id.iconserver);
			iv.setTag(position);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Integer position = (Integer)v.getTag();
					server = items.get(position);
					showQuickAction(v);
				}
			});
			if (tv != null) {             
				tv.setText(server.getName());                             
			}
			if(iv != null){
				iv.setImageResource(R.drawable.repository);
			}
		}
		return v;
	}
	
	private void showQuickAction(View v){

		final QuickAction qa = new QuickAction(v);
		v.getParent().getParent();
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_OPEN)){
			final ActionItem actionItem = new ActionItem();
			actionItem.setTitle(context.getText(R.string.open).toString());
			actionItem.setIcon(context.getResources().getDrawable(R.drawable.open_remote));
			actionItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, ListCmisFeedActivity.class);
					intent.putExtra("isFirstStart", true);
					intent.putExtra("server", server);
					intent.putExtra("title", server.getName());
					context.startActivity(intent);
					qa.dismiss();
				}
			});
			qa.addActionItem(actionItem);
		}
		
		
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_INFO)){
			final ActionItem info = new ActionItem();
			info.setTitle(context.getText(R.string.server_info).toString());
			info.setIcon(context.getResources().getDrawable(R.drawable.info));
			info.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					serverActivity.getInfoServer(server);
					qa.dismiss();
				}
			});
			qa.addActionItem(info);
		}
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_EDIT)){
			final ActionItem edit = new ActionItem();
			edit.setTitle(context.getText(R.string.edit).toString());
			edit.setIcon(context.getResources().getDrawable(R.drawable.editmetada));
			edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					serverActivity.editServer(server);
					qa.dismiss();
				}
			});
			qa.addActionItem(edit);
		}
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_FAVORITE)){
			final ActionItem favorite = new ActionItem();
			favorite.setTitle(context.getText(R.string.menu_item_favorites).toString());
			favorite.setIcon(context.getResources().getDrawable(R.drawable.favorite));
			favorite.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, FavoriteActivity.class);
					intent.putExtra("server", server);
					intent.putExtra("isFirstStart", true);
					context.startActivity(intent);
					qa.dismiss();
				}
			});
			qa.addActionItem(favorite);
		}
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_SEARCH)){
			final ActionItem search = new ActionItem();
			search.setTitle(context.getText(R.string.menu_item_search).toString());
			search.setIcon(context.getResources().getDrawable(R.drawable.search));
			search.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					serverActivity.startSearch();
					qa.dismiss();
				}
			});
			qa.addActionItem(search);
		}
		
		if (getCmisPrefs().getQuickActionServer().get(ACTION_SERVER_DELETE)){
			final ActionItem delete = new ActionItem();
			delete.setTitle(context.getText(R.string.delete).toString());
			delete.setIcon(context.getResources().getDrawable(R.drawable.delete));
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(R.string.delete);
					builder.setMessage(context.getText(R.string.action_delete_desc) + " " + server.getName() + " ? ")
					.setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							serverActivity.deleteServer(server.getId());
							qa.dismiss();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							qa.dismiss();
						}
					}).create();
					builder.show();
				}
			});
			qa.addActionItem(delete);
		}
		
		qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
		
		qa.show();
	}
	
	Prefs getCmisPrefs() {
		return ((CmisApp) serverActivity.getApplication()).getPrefs();
	}
}
