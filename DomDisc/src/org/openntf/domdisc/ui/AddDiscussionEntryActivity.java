package org.openntf.domdisc.ui;

import java.util.UUID;

import org.openntf.domdisc.R;
import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.Constants;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


public class AddDiscussionEntryActivity extends SherlockActivity {
	private EditText editSubject;
	private EditText editBody;
	private EditText editCategories;
	private Spinner chooseCategories;
	private DiscussionDatabase discussionDatabase;
	private DiscussionEntry parentDiscussionEntry;
	private boolean shouldCommitToLog = false;
//	private DiscussionEntry discussionEntry;
//	private Button deleteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shouldCommitToLog = getLogALot(this);
		DatabaseManager.init(getApplicationContext());
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.add_discussion_entry, null);
        editSubject = (EditText) contentView.findViewById(R.id.edit_subject);
        editBody = (EditText) contentView.findViewById(R.id.edit_body);
        editCategories = (EditText) contentView.findViewById(R.id.edit_categories);
        chooseCategories = (Spinner) contentView.findViewById(R.id.choose_categories);
//        chooseCategories.

        
        setupDiscussionDatabaseAndParent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title = "";
        if (parentDiscussionEntry != null) {
        	title = "New response to " + parentDiscussionEntry.getSubject();
        	editCategories.setVisibility(View.GONE);
        	contentView.findViewById(R.id.headline_categories).setVisibility(View.GONE);
        } else {
        	title = "New discussion thread";
        }
        getSupportActionBar().setTitle(title);
//        setupDiscussionEntry();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.activity_add_discussion_entry, menu);
       return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
//	    	  NavUtils.navigateUpFromSameTask(this);
	    	  finish(); // stops  this Activity
	         return true;
	      case R.id.menu_save:
	    	  
	    	  String subject = editSubject.getText().toString();
	    	  String body = editBody.getText().toString();
	    	  String categories = editCategories.getText().toString();
	    	  
	    	  createNewDiscussionEntry(subject, body, categories);
	    	  finish(); // stops  this Activity
	    	  return true;
	   }
	   return super.onOptionsItemSelected(item);
	}	
	
	
	


	private void setupDiscussionDatabaseAndParent() {
		Bundle bundle = getIntent().getExtras();
		if (null!=bundle) {
			if (bundle.containsKey(Constants.keyDiscussionDatabaseId)) {
				int discussionDatabaseId = bundle.getInt(Constants.keyDiscussionDatabaseId);
				discussionDatabase = DatabaseManager.getInstance().getDiscussionDatabaseWithId(discussionDatabaseId);
			}
			if (bundle.containsKey(Constants.keyDiscussionEntryId)) {
				String discussionEntryId = bundle.getString(Constants.keyDiscussionEntryId);
				parentDiscussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(discussionEntryId);
			}
		}	
	}

	boolean notEmpty(String s) {
		return null!=s && s.length()>0;
	}

	private void createNewDiscussionEntry(String subject,String body, String categories) {
		if (null!=discussionDatabase) {
			ApplicationLog.d(getClass().getSimpleName() + " Creating a new entry", shouldCommitToLog);
			DiscussionEntry discussionEntry = new DiscussionEntry();
			discussionEntry.setSubject(subject);
			discussionEntry.setBody(body);
			discussionEntry.setDiscussionDatabase(discussionDatabase);
			UUID uuid = UUID.randomUUID();
			discussionEntry.setUnid(String.valueOf(uuid)); //All entries have to have a value in unid - therefore we create one
//			ApplicationLog.d(getClass().getSimpleName() + " unid: " + String.valueOf(uuid), shouldCommitToLog);
			
			if (parentDiscussionEntry != null) {
				discussionEntry.setForm("Response");
				discussionEntry.setParentid(parentDiscussionEntry.getUnid());
				ApplicationLog.d(getClass().getSimpleName() + " A Response with parent id: " + discussionEntry.getParentid(), shouldCommitToLog);
			} else {
				discussionEntry.setForm("MainTopic");
				discussionEntry.setCategories(categories);
				ApplicationLog.d(getClass().getSimpleName() + " A Main Document", shouldCommitToLog);
			}
			
			
			DatabaseManager.getInstance().createDiscussionEntry(discussionEntry);
			
		} else {
			ApplicationLog.w(getClass().getSimpleName() + " No discussionDatabase available for saving the entry");
		}
	}
	
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
}