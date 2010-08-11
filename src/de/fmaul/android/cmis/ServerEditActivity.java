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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.fmaul.android.cmis.database.Database;
import de.fmaul.android.cmis.database.ServerDAO;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.FeedUtils;

public class ServerEditActivity extends Activity {
	
	private Database database;
	
	private Context context = this;
	
	private boolean isEdit = false;
	private Server currentServer;
	
	private EditText serverNameEditText;
	private EditText serverUrlEditText; 
	private EditText userEditText; 
	private EditText passwordEditText;
	private Button workspaceEditText;
	
	private List<String> workspaces;

	private CharSequence[] cs;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_edit);
		
		initServerData();
		
		database = Database.create(this);
		
		Button button = (Button)findViewById(R.id.validation_button);
		button.setOnClickListener( new Button.OnClickListener(){
			public void onClick(View view){
				try{
					if(serverNameEditText.getText().toString().equals("") || serverUrlEditText.getText().toString().equals("") || workspaceEditText.getText().toString().equals("")){
						Toast.makeText(ServerEditActivity.this, R.string.cmis_repo_fields, Toast.LENGTH_LONG).show();
					} else if (isEdit == false){
						ServerDAO serverDao = new ServerDAO(database.open());
						
						serverDao.insert(
								serverNameEditText.getText().toString(), 
								serverUrlEditText.getText().toString(), 
								userEditText.getText().toString(), 
								passwordEditText.getText().toString(),
								workspaceEditText.getText().toString());
						
						database.close();
						
					    Intent intent = new Intent(context, ServerActivity.class);
					    finish();
					   	startActivity(intent);
					} else if (isEdit) {
						ServerDAO serverDao = new ServerDAO(database.open());
						
						serverDao.update(
								currentServer.getId(),
								serverNameEditText.getText().toString(), 
								serverUrlEditText.getText().toString(), 
								userEditText.getText().toString(), 
								passwordEditText.getText().toString(),
								workspaceEditText.getText().toString()
								);
						
						database.close();
						
					    Intent intent = new Intent(context, ServerActivity.class);
					    finish();
					   	startActivity(intent);
					}
				} catch (Exception e) {
					ActionUtils.displayError(ServerEditActivity.this, R.string.generic_error);
				}
			}

		});
		workspaceEditText.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseWorkspace();
			}
		});
	}
	
	private void chooseWorkspace(){
		try {
			workspaces = FeedUtils.getRootFeedsFromRepo(getEditTextValue(serverUrlEditText), getEditTextValue(userEditText), getEditTextValue(passwordEditText));
			cs = workspaces.toArray(new CharSequence[workspaces.size()]);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.cmis_repo_choose_workspace);
			builder.setSingleChoiceItems(cs, workspaces.indexOf(workspaceEditText.getText()) ,new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        workspaceEditText.setText(cs[item]);
			        dialog.dismiss();
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		} catch (Exception e) {
			Toast.makeText(ServerEditActivity.this, R.string.error_repo_connexion, Toast.LENGTH_LONG).show();
			workspaceEditText.setText("");
		}
	}
	
	private String getEditTextValue(EditText editText){
		if (editText != null && editText.getText() != null && editText.getText().length() > 0 ){
			return editText.getText().toString();
		} else {
			return null;
		}
	}

	private void initServerData() {
		
		workspaces = null;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			currentServer = (Server) getIntent().getExtras().getSerializable("server");
		}
		
		serverNameEditText = (EditText) findViewById(R.id.cmis_repo_server_name);
		serverUrlEditText = (EditText) findViewById(R.id.cmis_repo_url_id);
		userEditText = (EditText) findViewById(R.id.cmis_repo_user_id);
		passwordEditText = (EditText) findViewById(R.id.cmis_repo_password_id);
		workspaceEditText = (Button) findViewById(R.id.cmis_repo_workspace_id);
		
		if (currentServer != null){
			serverNameEditText.setText(currentServer.getName());
			serverUrlEditText.setText(currentServer.getUrl());
			userEditText.setText(currentServer.getUsername());
			passwordEditText.setText(currentServer.getPassword());
			workspaceEditText.setText(currentServer.getWorkspace());
			isEdit = true;
		}
	}
}
