package org.openntf.domdisc.ui;

import java.util.ArrayList;
import java.util.List;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.Constants;
import org.openntf.domdisc.general.PollReceiver;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.tools.DateUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.openntf.domdisc.R;

public class DatabaseConfigurationsActivity extends SherlockActivity {
	ListView listView;
	boolean shouldCommitToLog = false;
	View helpLayout;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DatabaseManager.init(this);
		shouldCommitToLog = getLogALot(this);

		ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.main, null);
		listView = (ListView) contentView.findViewById(R.id.list_view);

		setContentView(contentView);
		
		helpLayout = findViewById(R.id.top_layout);
		if (!hasNoConfiguration()) {
			helpLayout.setVisibility(View.INVISIBLE);
		} else {
			helpLayout.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					helpLayout.setVisibility(View.INVISIBLE);
					return false;
				}

			});
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		
	}

	@Override
	protected void onStart() {
		super.onStart();
		setupListView(listView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		shouldCommitToLog = getLogALot(this);
		DatabaseManager.init(this);

		ApplicationLog.d("On resume in configurations view - calling scheduler", shouldCommitToLog);
		PollReceiver.scheduleAlarms(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_discussion_databases, menu);
		// disable the home button and the up affordance:
		//       getSupportActionBar().setHomeButtonEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Activity activity = this;
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			//                NavUtils.navigateUpTo(this, new Intent(this, DiscussionEntriesViewActivity.class));
			NavUtils.navigateUpTo(this, new Intent(this, org.openntf.domdisc.ui.StartActivity.class));
			return true;
		case R.id.menu_add_database:
			//Start det

			intent = new Intent(activity,AddDiscussionDatabaseActivity.class);
			startActivity (intent);
			return true;
		case R.id.menu_settings:

			intent = new Intent(activity, PreferenceActivity.class);
			startActivity (intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void setupListView(ListView lv) {
		final List<DiscussionDatabase> discussionDatabases = DatabaseManager.getInstance().getAllDiscussionDatabases();

		List<String> titles = new ArrayList<String>();
		for (DiscussionDatabase db : discussionDatabases) {
			titles.add(db.getName()  + " [rep: " + DateUtil.getDateLong(db.getLastSuccesfulReplicationDate()) + "]");
		}

		
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
		lv.setAdapter(adapter);

		final Activity activity = this;

		//Hop ind i en liste af items under databasen
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DiscussionDatabase discussionDatabase = discussionDatabases.get(position);
				//				Intent intent = new Intent(activity, DiscussionEntriesViewActivity.class);
				Intent intent = new Intent(activity, AddDiscussionDatabaseActivity.class);
				intent.putExtra(Constants.keyDiscussionDatabaseId, discussionDatabase.getId());
				startActivity(intent);
			}
		});
	}

	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
	
	private boolean hasNoConfiguration() {
		List<DiscussionDatabase> discussionDatabases = DatabaseManager.getInstance().getAllDiscussionDatabases();
		if (discussionDatabases == null || discussionDatabases.size()<1) {
			return true;
		} else {
			return false;
		}	
	}
}
