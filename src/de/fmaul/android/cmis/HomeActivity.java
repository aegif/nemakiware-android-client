package de.fmaul.android.cmis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class HomeActivity extends Activity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((Button) findViewById(R.id.about)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, AboutActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.repository)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, ServerActivity.class));
			}
		});
        
        
    }
    
    
    
}