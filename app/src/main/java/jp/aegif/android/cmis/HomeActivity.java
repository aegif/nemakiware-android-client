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
package jp.aegif.android.cmis;

import jp.aegif.android.cmis.utils.ActionUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends ActionBarActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getBooleanExtra("EXIT", false)) {
        	finish();
        }
        
        setContentView(R.layout.main);
        ActionUtils.initPrefs(this);
        

        ((Button) findViewById(R.id.filesystem)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, FileChooserActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.repository)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, ServerActivity.class));
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.preferences) {
            Intent intent = new Intent(this, CmisPreferences.class);
            startActivity(intent);
        }else if(id == R.id.about){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
       if (requestCode == 0) {
    	   if (resultCode == RESULT_OK) {
    		   Toast.makeText(this, "Hello ! ", Toast.LENGTH_LONG);
    	   }
       }
    }
}