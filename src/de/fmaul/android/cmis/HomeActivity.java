package de.fmaul.android.cmis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.IntentIntegrator;
import de.fmaul.android.cmis.utils.IntentResult;

public class HomeActivity extends Activity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getBooleanExtra("EXIT", false)) {
        	finish();
        }
        
        setContentView(R.layout.main);
        
        ((Button) findViewById(R.id.about)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, AboutActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.preferences)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					
					IntentIntegrator.initiateScan(HomeActivity.this);
					
					/*Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			        startActivityForResult(intent, 0);*/
				} catch (Exception e) {
					ActionUtils.displayError(HomeActivity.this, "Aucune Application installé");
				}
		        
		    }
		});
        
        ((Button) findViewById(R.id.repository)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, ServerActivity.class));
			}
		});
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	    if (scanResult != null) {
	    	ActionUtils.displayError(HomeActivity.this,scanResult.getContents());
	    	ActionUtils.displayError(HomeActivity.this,scanResult.getFormatName());
	      // handle scan result
	    }
    	    // else continue with any other code you need in the method

	}
    
    
    
}