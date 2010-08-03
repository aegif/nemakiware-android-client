package de.fmaul.android.cmis;

import de.fmaul.android.cmis.repo.CmisDBAdapter;
import de.fmaul.android.cmis.repo.Server;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ServerEditActivity extends Activity {
	
	private CmisDBAdapter cmisDbAdapter;
	
	private Context context = this;
	
	private boolean isEdit = false;
	private Server currentServer;
	
	private EditText serverNameEditText;
	private EditText serverUrlEditText; 
	private EditText userEditText; 
	private EditText passwordEditText;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_edit);
		
		initServerData();
		
		cmisDbAdapter = new CmisDBAdapter(this); 
		
		Button button = (Button)findViewById(R.id.validation_button);

		button.setOnClickListener( new Button.OnClickListener(){
			public void onClick(View view){	  	

				if(serverNameEditText.getText().toString().equals("") || serverUrlEditText.getText().toString().equals("") || userEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("")){
					Toast.makeText(ServerEditActivity.this, "EMPTY FIELDS", Toast.LENGTH_LONG).show();
				} else if (isEdit == false){
					cmisDbAdapter.open();
					
					cmisDbAdapter.insert(
							serverNameEditText.getText().toString(), 
							serverUrlEditText.getText().toString(), 
							userEditText.getText().toString(), 
							passwordEditText.getText().toString());
					
					cmisDbAdapter.close();
					
				    Intent intent = new Intent(context, ServerActivity.class);
				    finish();
				   	startActivity(intent);
				} else if (isEdit) {
					cmisDbAdapter.open();
					
					cmisDbAdapter.update(
							currentServer.getId(),
							serverNameEditText.getText().toString(), 
							serverUrlEditText.getText().toString(), 
							userEditText.getText().toString(), 
							passwordEditText.getText().toString());
					
					cmisDbAdapter.close();
					
				    Intent intent = new Intent(context, ServerActivity.class);
				    finish();
				   	startActivity(intent);
				}
			}
		});
	}

	private void initServerData() {
		currentServer = (Server) getIntent().getExtras().getSerializable("server");
		
		if (currentServer != null){
			serverNameEditText = (EditText) findViewById(R.id.cmis_repo_server_name);
			serverUrlEditText = (EditText) findViewById(R.id.cmis_repo_url_id);
			userEditText = (EditText) findViewById(R.id.cmis_repo_user_id);
			passwordEditText = (EditText) findViewById(R.id.cmis_repo_password_id);
			
			serverNameEditText.setText(currentServer.getName());
			serverUrlEditText.setText(currentServer.getUrl());
			userEditText.setText(currentServer.getUsername());
			passwordEditText.setText(currentServer.getPassword());
			
			isEdit = true;
		}
	}
}
