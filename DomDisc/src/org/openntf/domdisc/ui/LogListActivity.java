package org.openntf.domdisc.ui;

import java.util.ArrayList;
import java.util.List;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.model.AppLog;
import org.openntf.domdisc.model.DiscussionDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.openntf.domdisc.R;


public class LogListActivity extends SherlockActivity {
	ListView listView;
	DiscussionDatabase discussionDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.log_list, null);
		listView = (ListView) contentView.findViewById(R.id.list_view);

		setContentView(contentView);
		// Show the Up button in the action bar:
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		DatabaseManager.init(getApplicationContext());
		setupListView();
		setTitle("Log list");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_log_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//	         NavUtils.navigateUpTo(this, new Intent(this, DiscussionEntriesViewActivity.class));
			NavUtils.navigateUpTo(this, new Intent(this, org.openntf.domdisc.ui.StartActivity.class));
			return true;
		case R.id.menu_empty_log:
			//Empty
			DatabaseManager.getInstance().emptyAppLog();
			setupListView();
			return true;
		case R.id.menu_refresh_log:
			//refresh
			setupListView();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupListView() {
		final List<AppLog> logEntries = DatabaseManager.getInstance().getAllAppLogs();

		List<String> titles = new ArrayList<String>();
		for (AppLog logEntry : logEntries) {
			titles.add(logEntry.getLogTime() + " " + logEntry.getMessage());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
		listView.setAdapter(adapter);
		listView.setSelection(logEntries.size());

	}

}