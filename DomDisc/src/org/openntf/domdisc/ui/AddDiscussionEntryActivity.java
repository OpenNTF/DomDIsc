package org.openntf.domdisc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


public class AddDiscussionEntryActivity extends SherlockActivity {
	private EditText editSubject;
	private EditText editBody;
	private EditText editOptionalNewCategory;
	private Spinner editCategory;
	private DiscussionDatabase discussionDatabase;
	private DiscussionEntry parentDiscussionEntry;
	private boolean shouldCommitToLog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shouldCommitToLog = getLogALot(this);
		ApplicationLog.d(getClass().getSimpleName() + " onCreate", shouldCommitToLog);
		
		DatabaseManager.init(getApplicationContext());
        ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.add_discussion_entry, null);
        editSubject = (EditText) contentView.findViewById(R.id.edit_subject);
        editBody = (EditText) contentView.findViewById(R.id.edit_body);
        editOptionalNewCategory = (EditText) contentView.findViewById(R.id.edit_categories);
        editCategory = (Spinner) contentView.findViewById(R.id.choose_categories);
        setContentView(contentView);
        setupDiscussionDatabaseAndParent();
        
        Set<String> databaseCategoriesSet =  discussionDatabase.getCategories();
        
        List<String> list = new ArrayList<String>();
        list.addAll(databaseCategoriesSet);
        
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
        editCategory.setAdapter(dataAdapter2);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title = "";
        if (parentDiscussionEntry != null) {
        	title = "New response to " + parentDiscussionEntry.getSubject();
        	editOptionalNewCategory.setVisibility(View.GONE);
        	editCategory.setVisibility(View.GONE);
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
	    	  String optionalCategory = editOptionalNewCategory.getText().toString();
	    	  String category = (String) editCategory.getSelectedItem();
	    	  
	    	  createNewDiscussionEntry(subject, body, category, optionalCategory);
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

	private void createNewDiscussionEntry(String subject,String body, String category, String optionalCategory) {
		if (null!=discussionDatabase) {
			ApplicationLog.d(getClass().getSimpleName() + " Creating a new entry", shouldCommitToLog);
			DiscussionEntry discussionEntry = new DiscussionEntry();
			discussionEntry.setSubject(subject);
			discussionEntry.setBody(body);
			discussionEntry.setDiscussionDatabase(discussionDatabase);
			UUID uuid = UUID.randomUUID();
			discussionEntry.setUnid(String.valueOf(uuid)); //All entries have to have a value in unid - therefore we create one

			if (parentDiscussionEntry != null) {
				discussionEntry.setForm("Response");
				discussionEntry.setParentid(parentDiscussionEntry.getUnid());
				ApplicationLog.d(getClass().getSimpleName() + " A Response with parent id: " + discussionEntry.getParentid(), shouldCommitToLog);
			} else {
				discussionEntry.setForm("MainTopic");
				
				if (optionalCategory != null && optionalCategory.length()>0) {
					discussionEntry.setCategories(optionalCategory);
					ApplicationLog.d(getClass().getSimpleName() + " Category: " + optionalCategory, shouldCommitToLog);
				} else if (category != null && category.length()>0 ) {
					discussionEntry.setCategories(category);	
					ApplicationLog.d(getClass().getSimpleName() + " Category: " + category, shouldCommitToLog);
				}
				
				ApplicationLog.d(getClass().getSimpleName() + " A Main Document", shouldCommitToLog);
			}
			
			
			DatabaseManager.getInstance().createDiscussionEntry(discussionEntry);
			
		} else {
			ApplicationLog.w(getClass().getSimpleName() + " No discussionDatabase available for saving the entry");
		}
	}
	
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
}