package org.openntf.domdisc.ui;

import java.util.ArrayList;
import org.openntf.domdisc.R;
import java.util.List;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.Constants;
import org.openntf.domdisc.general.PollReceiver;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;



/**
 * This is the Activity that launches when the App is started.
 * Note: Background replication is handled by Pollreceiver
 * @author jbr
 *
 */

public class StartActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener, DiscussionMainEntriesViewFragment.OnItemSelectedListener, ReadDiscussionEntryFragment.OnResponseItemSelectedListener, SearchView.OnQueryTextListener
 {
	DiscussionDatabase discussionDatabase;
	List<DiscussionDatabase> allDiscussionDatabases = null;
	ArrayList<String> spinnerSelectionList = null;

	private boolean shouldCommitToLog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shouldCommitToLog = getLogALot(this);
		DatabaseManager.init(this);
		/**
		 * Loading the fragment(s)
		 */
		ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.start_activity, null);
		setContentView(contentView);

		//We may have to load a fragment when the app is initially started. if we have a layout that includes the discussionEntryFragment
		if (savedInstanceState == null) {
			ApplicationLog.d(getClass().getSimpleName() + " savedInstanceState is null", shouldCommitToLog);
			//        	ReadDiscussionEntryFragment fragment = (ReadDiscussionEntryFragment) getSupportFragmentManager().findFragmentById(R.id.discussionEntryFragment);
			FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);
			// If the fragment is visible - Do first time initialization -- add initial fragment.
			if (containerForReadDiscussionEntryFragment != null ) {
				ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);
				Fragment newFragment = new InitialRightPaneFragment();
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.discussionEntryFragment, newFragment).commit();
			}
		}

		showSpinner();
		ApplicationLog.d("On resume in configurations view - calling scheduler to make sure scheduled replication is running", shouldCommitToLog);
		PollReceiver.scheduleAlarms(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		DatabaseManager.init(this);
		handleUpgradeCheck();
		ApplicationLog.d(getClass().getSimpleName() + " onStart", shouldCommitToLog);
		initializeDatabaseDisplay();
	}
	
	
	@Override 
	protected void onResume() {
		super.onResume();
//		ApplicationLog.d(getClass().getSimpleName() + " onResume", shouldCommitToLog);
//		
//		//We might be returning from the configurations Activity and want to display the updated list of Databases in the spinner
//		
//		if(discussionDatabase == null) {
//			initializeDatabaseDisplay();
//		}
	}



	private void initializeDatabaseDisplay() {
		int discussionDatabaseId = getLastOpenDiscussionDatabase();
		if (discussionDatabaseId < 0) {
			ApplicationLog.d("No database has been opened previously", shouldCommitToLog);
			discussionDatabase = DatabaseManager.getInstance()
					.getDiscussionDatabaseWithId(discussionDatabaseId);
			List<DiscussionDatabase> allDiscussionDatabases = DatabaseManager
					.getInstance().getAllDiscussionDatabases();
			if (allDiscussionDatabases.isEmpty()) {
				ApplicationLog.d("There are no DiscussionDatabases available", shouldCommitToLog);
				discussionDatabase = null;
			} else {
				ApplicationLog.d("Getting the first DiscussionDatabase", shouldCommitToLog);
				discussionDatabase = allDiscussionDatabases.get(0);
			}
		} else {
			discussionDatabase = DatabaseManager.getInstance().getDiscussionDatabaseWithId(discussionDatabaseId);
		}

		if (discussionDatabase != null) {
			ApplicationLog.d("displaying discussionDatabase="	+ discussionDatabase.getName() + " discussionDatabaseId="+ discussionDatabaseId, shouldCommitToLog);
			setupListView(discussionDatabase);//
			setLastOpenDiscussionDatabase(discussionDatabase.getId());
		} else {
			ApplicationLog.d("Displaying nothing", shouldCommitToLog);
		}
		
		showInitialRightPane();

		// Only do this if we have more than 1 database.
		if ((discussionDatabase != null) && allDiscussionDatabases.size() > 1) {
			int pos = spinnerSelectionList
					.indexOf(discussionDatabase.getName());
			if (pos > -1) {
				getSupportActionBar().setSelectedNavigationItem(pos);
			}
		}
	}


	private void showInitialRightPane() {
//		FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);
//
//		if (containerForReadDiscussionEntryFragment != null) {
//			ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);
//
//			// Instantiate a new fragment.
//			ReadDiscussionEntryFragment newFragment = ReadDiscussionEntryFragment.newInstance(unid);
//			// Add the fragment to the activity, pushing this transaction
//			// on to the back stack.
//			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//			ft.replace(R.id.discussionEntryFragment, newFragment);
//			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//			ft.commit();
//			ApplicationLog.d(getClass().getSimpleName() + "FragmentTransaction committed", shouldCommitToLog);
//		} else {
//			ApplicationLog.w(getClass().getSimpleName() + " Did not find the fragment container");
//		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ApplicationLog.d(getClass().getSimpleName() + " onCreateOptionsMenu start", shouldCommitToLog);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_discussion_entries_view, menu);
		//Search start
		SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
		searchView.setQueryHint("Search for countries…");
        searchView.setOnQueryTextListener(this);
        menu.add("Search")
        .setIcon(R.drawable.ic_action_search)
        .setActionView(searchView)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        //Search end
		// disable the home button and the up affordance:
		getSupportActionBar().setHomeButtonEnabled(false);
		
		return true;
	}
	
	//Search start
	 @Override
	    public boolean onQueryTextSubmit(String query) {
	        Toast.makeText(this, "You searched for: " + query, Toast.LENGTH_LONG).show();
	        setupListView(discussionDatabase, query);
	        return true;
	    }
	 
	 @Override
	    public boolean onQueryTextChange(String newText) {
	        return false;
	    }
	 //Search end

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Activity activity = this;
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			// Åbn database-konfiguration tror jeg
			intent = new Intent(activity, DatabaseConfigurationsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_log:
			// Åbne log Activity
			intent = new Intent(activity, LogListActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_compose_document:
			if (discussionDatabase != null) {
				intent = new Intent(activity, AddDiscussionEntryActivity.class);
				intent.putExtra(Constants.keyDiscussionDatabaseId,	discussionDatabase.getId());
				startActivity(intent);	
			}
			return true;
		case R.id.menu_refresh:
			// refresh
			if (discussionDatabase != null) {
				setupListView(discussionDatabase);	
			}
			return true;
		case R.id.menu_about:
			intent = new Intent(activity, AboutAppActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}




	/** 
	 * Give me a DiscussionDatabase to show. Feeds it to the fragment
	 */
	private void setupListView(DiscussionDatabase discussionDatabase) {
		// TODO Auto-generated method stub
		DiscussionMainEntriesViewFragment fragment = (DiscussionMainEntriesViewFragment) getSupportFragmentManager().findFragmentById(R.id.discussionMainEntriesFragment);
		fragment.setDiscussionDatabase(discussionDatabase);
	}
	
	/** 
	 * Give me a DiscussionDatabase to show and a query to limit what is shown. Feeds it to the fragment
	 */
	private void setupListView(DiscussionDatabase discussionDatabase, String query) {
		DiscussionMainEntriesViewFragment fragment = (DiscussionMainEntriesViewFragment) getSupportFragmentManager().findFragmentById(R.id.discussionMainEntriesFragment);
		fragment.setDiscussionDatabase(discussionDatabase, query);
	}


	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}


	private void showSpinner() {

		ApplicationLog.d("Preparing to show updated spinner", shouldCommitToLog);

		buildDiscussionDatabaseList();
		buildSpinnerList();

		if (allDiscussionDatabases.isEmpty()) {
			ApplicationLog.d("Nothing to show in spinner - not displaying it", shouldCommitToLog);
		}

		else if (allDiscussionDatabases.size() == 1) {
			getSupportActionBar().setTitle(allDiscussionDatabases.get(0).getName());
		}
		else {
			Context context = getSupportActionBar().getThemedContext();
			ArrayAdapter<String> list = new ArrayAdapter<String>(context,
					R.layout.sherlock_spinner_item, spinnerSelectionList);

			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			getSupportActionBar().setListNavigationCallbacks(list, this);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	private void buildDiscussionDatabaseList() {
		allDiscussionDatabases = DatabaseManager.getInstance().getAllDiscussionDatabases();
	}

	private void buildSpinnerList() {
		spinnerSelectionList = new ArrayList<String>();
		for (int i = 0; i < allDiscussionDatabases.size(); i++) {
			String name = allDiscussionDatabases.get(i).getName();
			spinnerSelectionList.add(name);
		}
	}


	private void handleUpgradeCheck() {
		int currentVersionNo = getAppVersion();
		int checkedVersionNo = getCheckedAppVersion(getBaseContext());
		if (currentVersionNo > checkedVersionNo) {
			ApplicationLog.i("This app has been upgraded to version "
					+ currentVersionNo
					+ ". Will make sure backgrund replication is OK.");
			PollReceiver.scheduleAlarms(this);
			setCheckedAppVersion(getBaseContext(), currentVersionNo);
		}
	}

	private int getAppVersion() {
		int version = -1;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionCode;
		} catch (NameNotFoundException e1) {
			Log.e(this.getClass().getSimpleName(), "Name not found", e1);
		}
		return version;
	}

	private static int getCheckedAppVersion(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getInt("checkedAppVersion", -2);
	}

	private void setCheckedAppVersion(Context ctxt, int versionNo) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("checkedAppVersion", versionNo);
		editor.commit();
	}


	private void setLastOpenDiscussionDatabase(int databaseId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		SharedPreferences.Editor editor = prefs.edit();
		// editor.putString("lastGoodSyncCategory", category);
		editor.putInt("lastOpenDiscussionDatabase", databaseId);

		// DatabaseManager.init(this);
		// ApplicationLog.d("Set lastOpenDiscussionDatabase: " + databaseId);

		// Commit the edits!
		editor.commit();
	};

	/**
	 * 
	 * @return int for last open database. -1 if never opened before
	 */
	private int getLastOpenDiscussionDatabase() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		DatabaseManager.init(this);

		int databaseId = prefs.getInt("lastOpenDiscussionDatabase", -1);
		// ApplicationLog.d("Read lastOpenDiscussionDatabase: " + databaseId);

		DiscussionDatabase discussionDatabase = DatabaseManager.getInstance()
				.getDiscussionDatabaseWithId(databaseId);
		if (discussionDatabase != null) {
			return databaseId;
		} else {
			return -1;
		}

	}


	/**
	 * When an entry in the Main Entries View (Fragment) has been selected this method gets called with the unid of the selected item
	 */
	@Override
	public void onViewItemSelected(String unid) {

		ApplicationLog.d("got a unid: " + unid, shouldCommitToLog);

		DiscussionEntry selectedEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid);

		if (selectedEntry == null) {
			ApplicationLog.w("Unable to find the selected Discussion Entry - not showing anything new");
		} else {
			//			ReadDiscussionEntryFragment fragment = (ReadDiscussionEntryFragment) getSupportFragmentManager().findFragmentById(R.id.discussionEntryFragment);
			FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);

			// If the fragment is visible - feed it the DiscussionEntry. If not - launch a new Activity with the unid of the DiscussionEntry
			if (containerForReadDiscussionEntryFragment != null) {
				ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);

				//Clearing the back stack - is this a good idea? I think so
				FragmentManager fm = getSupportFragmentManager();
				for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
				    fm.popBackStack();
				}
				
				// Instantiate a new fragment.
				ReadDiscussionEntryFragment newFragment = ReadDiscussionEntryFragment.newInstance(unid);
				// Add the fragment to the activity, pushing this transaction
				// on to the back stack.
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.discussionEntryFragment, newFragment);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
				ft.commit();
				ApplicationLog.d(getClass().getSimpleName() + "FragmentTransaction committed", shouldCommitToLog);
				//		        newFragment.setDiscussionEntry(selectedEntry);
			} else {
				ApplicationLog.d(getClass().getSimpleName() + " fragment is not layout. Launching new ReadDiscussionEntry2Activity", shouldCommitToLog);
				Intent intent = new Intent(getApplicationContext(),
						ReadDiscussionEntry2Activity.class);
				intent.putExtra(ReadDiscussionEntry2Activity.EXTRA_URL, unid);
				startActivity(intent);
			}	
		}

	}

	/**
	 * When an entry in the Response Entries View (Fragment) has been selected this method gets called with the unid of the selected item
	 */
	@Override
	public void onResponseViewItemSelected(String unid) {

		ApplicationLog.d(getClass().getSimpleName() + " got a unid: " + unid, shouldCommitToLog);

		DiscussionEntry selectedEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid);

		if (selectedEntry == null) {
			ApplicationLog.w(getClass().getSimpleName() + " Unable to find the selected Discussion Entry - not showing anything new");
		} else {
			FrameLayout containerForReadDiscussionEntryFragment = (FrameLayout) findViewById(R.id.discussionEntryFragment);

			// If the fragment is visible - feed it the DiscussionEntry. If not - launch a new Activity with the unid of the DiscussionEntry
			if (containerForReadDiscussionEntryFragment != null) {
				ApplicationLog.d(getClass().getSimpleName() + " fragment is in layout", shouldCommitToLog);

				// Instantiate a new fragment.
				ReadDiscussionEntryFragment newFragment = ReadDiscussionEntryFragment.newInstance(unid);
				// Add the fragment to the activity, pushing this transaction
				// on to the back stack.
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.discussionEntryFragment, newFragment);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
				ft.commit();
				ApplicationLog.d(getClass().getSimpleName() + "FragmentTransaction committed", shouldCommitToLog);
				//		        newFragment.setDiscussionEntry(selectedEntry);
			} else {
				ApplicationLog.d(getClass().getSimpleName() + " fragment is not layout. Launching new ReadDiscussionEntry2Activity", shouldCommitToLog);
				Intent intent = new Intent(getApplicationContext(),
						ReadDiscussionEntry2Activity.class);
				intent.putExtra(ReadDiscussionEntry2Activity.EXTRA_URL, unid);
				startActivity(intent);
			}	
		}

	}


	/* 
	 * This reacts when something is selected from the spinner
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		DiscussionDatabase selectedDiscussionDatabase = allDiscussionDatabases.get(itemPosition);
		discussionDatabase = selectedDiscussionDatabase;
		setupListView(discussionDatabase);
		setLastOpenDiscussionDatabase(discussionDatabase.getId());
		return true;
	}


}
