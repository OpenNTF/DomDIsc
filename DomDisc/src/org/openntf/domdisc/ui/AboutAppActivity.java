package org.openntf.domdisc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.webkit.WebView;
import org.openntf.domdisc.R;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutAppActivity extends SherlockActivity {
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app_activity);  
        WebView webView;  
        webView = (WebView) findViewById(R.id.webview);  
        webView.loadUrl("file:///android_asset/aboutapp.html");   
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }  
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this, org.openntf.domdisc.ui.StartActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

