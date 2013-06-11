package org.openntf.domdisc.ui;

import java.util.List;

import org.openntf.domdisc.controllers.DiscussionDatabaseController;
import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.Constants;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ViewGroup;
//import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.openntf.domdisc.R;


public class AddDiscussionDatabaseActivity extends SherlockActivity {
	private EditText edit;
	private DiscussionDatabase discussionDatabase;
	private EditText hostNameEdit;
	private EditText dbPathEdit;
	private EditText userNameEdit;
	private EditText passwordEdit;
	private CheckBox useSSLEdit;
	private EditText httpPortEdit;
	private CheckBox disableComputeWithFormEdit;
	
	private String hostName = "";
	private String dbPath = "";
	private String userName = "";
	private String password = "";
	private boolean useSSL = false;
	private String httpPort = "";
	private boolean disableComputeWithForm = false;
	private DiscussionDatabaseController dbController ;
	private Context context;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.add_discussion_database, null);
        edit = (EditText) contentView.findViewById(R.id.edit);
        hostNameEdit = (EditText) contentView.findViewById(R.id.hostName);
        dbPathEdit = (EditText) contentView.findViewById(R.id.dbPath);
        userNameEdit = (EditText) contentView.findViewById(R.id.userName);
        passwordEdit = (EditText) contentView.findViewById(R.id.password);
        useSSLEdit = (CheckBox) contentView.findViewById(R.id.useSSL);
        httpPortEdit = (EditText) contentView.findViewById(R.id.httpPort);
        disableComputeWithFormEdit = (CheckBox) contentView.findViewById(R.id.disableComputeWithForm);
//        Button btn = (Button) contentView.findViewById(R.id.button_save);
//        setupButton(btn);
        
        setupDiscussionDatabase();
        setContentView(contentView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.activity_add_discussion_database, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	final Activity activity = this;
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this,
                        new Intent(this, DatabaseConfigurationsActivity.class));
                return true;
            case R.id.menu_discard: 
            	new AlertDialog.Builder(activity)
				.setMessage(
						"Are you sure you would like to delete discussion database '"
								+ discussionDatabase.getName() + "'?")
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								deleteDiscussionDatabase();
							}
						}).create().show();
            	
            	
            	
            	return true;
            case R.id.menu_save:
            			String name = edit.getText().toString();
        				hostName = hostNameEdit.getText().toString();
        				dbPath = dbPathEdit.getText().toString();
        				userName = userNameEdit.getText().toString();
        				password = passwordEdit.getText().toString();
        				useSSL = useSSLEdit.isChecked();
        				httpPort = httpPortEdit.getText().toString();
        				disableComputeWithForm = disableComputeWithFormEdit.isChecked();
        				
        				if (null!=name && name.length()>0) {
        					if (null!=discussionDatabase) {
        						updateDiscussionDatabase(name);
        					} else {
        						createNewDiscussionDatabase(name);
        					}
        					finish();
        				} else {
        					new AlertDialog.Builder(activity)
        					.setTitle("Error")
        					.setMessage("Invalid name!")
        					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
        						public void onClick(DialogInterface dialog, int which) {
        							dialog.dismiss();
        						}
        					})
        					.show();
        				}
        			
        		
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	private void setupDiscussionDatabase() {
		Bundle bundle = getIntent().getExtras();
		if (null!=bundle && bundle.containsKey(Constants.keyDiscussionDatabaseId)) {
			int discussionDatabaseId = bundle.getInt(Constants.keyDiscussionDatabaseId);
			discussionDatabase = DatabaseManager.getInstance().getDiscussionDatabaseWithId(discussionDatabaseId);
			edit.setText(discussionDatabase.getName());
			hostNameEdit.setText(discussionDatabase.getHostName());
			dbPathEdit.setText(discussionDatabase.getDbPath());
			userNameEdit.setText(discussionDatabase.getUserName());
			passwordEdit.setText(discussionDatabase.getPassword());
			useSSLEdit.setChecked(discussionDatabase.isUseSSL());
			httpPortEdit.setText(discussionDatabase.getHttpPort());
		}
	}

//	private void setupButton(Button btn) {
//		final Activity activity = this;
//		btn.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				String name = edit.getText().toString();
//				hostName = hostNameEdit.getText().toString();
//				dbPath = dbPathEdit.getText().toString();
//				userName = userNameEdit.getText().toString();
//				password = passwordEdit.getText().toString();
//				useSSL = useSSLEdit.isChecked();
//				httpPort = httpPortEdit.getText().toString();
//				
//				if (null!=name && name.length()>0) {
//					if (null!=discussionDatabase) {
//						updateDiscussionDatabase(name);
//					} else {
//						createNewDiscussionDatabase(name);
//					}
//					finish();
//				} else {
//					new AlertDialog.Builder(activity)
//					.setTitle("Error")
//					.setMessage("Invalid name!")
//					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					})
//					.show();
//				}
//			}
//		});
//	}

	private void updateDiscussionDatabase(String name) {
		if (null!=discussionDatabase) {
			discussionDatabase.setName(name);
			discussionDatabase.setDbPath(dbPath);
			discussionDatabase.setHostName(hostName);
			discussionDatabase.setHttpPort(httpPort);
			discussionDatabase.setPassword(password);
			discussionDatabase.setUserName(userName);
			discussionDatabase.setUseSSL(useSSL);
			DatabaseManager.getInstance().updateDiscussionDatabase(discussionDatabase);
	        dbController = new DiscussionDatabaseController(discussionDatabase, context);
	        dbController.handleMessage(DiscussionDatabaseController.MESSAGE_REPLICATE, discussionDatabase);
		}
	}

	private void createNewDiscussionDatabase(String name) {
		DiscussionDatabase db = new DiscussionDatabase();
		db.setName(name);
		db.setDbPath(dbPath);
		db.setHostName(hostName);
		db.setHttpPort(httpPort);
		db.setPassword(password);
		db.setUserName(userName);
		db.setUseSSL(useSSL);
		db.setDisableComputeWithForm(disableComputeWithForm);
		DatabaseManager.getInstance().addDiscussionDatabase(db);
        dbController = new DiscussionDatabaseController(db, context);
        dbController.handleMessage(DiscussionDatabaseController.MESSAGE_REPLICATE, db);
	}
	
	private void deleteDiscussionDatabase() {
		DatabaseManager.init(this);
		ApplicationLog.i("Preparing to delete " + discussionDatabase.getName() + " and all Discussion entries related to it.");
		
		List<DiscussionEntry> discussionEntries = discussionDatabase
				.getDiscussionEntries();
		ApplicationLog.i("Deleting " + discussionEntries.size() + " entries");
				
		for (DiscussionEntry discussionEntry : discussionEntries) {
			DatabaseManager.getInstance().deleteDiscussionEntry(discussionEntry);
		}
		ApplicationLog.i("Deleting database configuration for " + discussionDatabase.getName());
		DatabaseManager.getInstance().deleteDiscussionDatabase(
				discussionDatabase);
		ApplicationLog.i("Deleting deletion finished");
		finish();
	}
}
