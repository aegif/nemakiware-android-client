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
