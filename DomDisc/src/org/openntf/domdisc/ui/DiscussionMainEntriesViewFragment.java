package org.openntf.domdisc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;
import org.openntf.domdisc.model.DiscussionEntryModifiedComparable;
import org.openntf.domdisc.model.DiscussionEntryThreadModifiedComparable;
import org.openntf.domdisc.tools.UserSessionTools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;

import org.openntf.domdisc.R;

public class DiscussionMainEntriesViewFragment extends SherlockFragment {


	private OnItemSelectedListener listener;
	ListView listView;
	private DiscussionDatabase currentDiscussionDatabase = null;
	private String currentSearchQuery = "";
	private boolean shouldCommitToLog = false;
	String sortPreference = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DatabaseManager.init(getActivity());
		shouldCommitToLog = getLogALot(getActivity());
		sortPreference = UserSessionTools.getSortPreference(getActivity());
		View view = inflater.inflate(R.layout.discussion_entry_list, container, false);
		listView = (ListView) view.findViewById(R.id.list_view);
		return view;
	}
	
	/**
	 * Hooks the listener to the enclosing Activity
	 */	
	 @Override
	    public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnItemSelectedListener) {
	        listener = (OnItemSelectedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement DiscussionMainEntriesViewFragment.OnItemSelectedListener");
	      }
	    }
	
	
	public void setDiscussionDatabase(DiscussionDatabase discussionDatabase) {
//		currentDiscussionDatabase = discussionDatabase;
//		currentSearchQuery = "";
//		populateListView();
		
		if (currentDiscussionDatabase == null) {
			currentDiscussionDatabase = discussionDatabase;
			currentSearchQuery = "";
			populateListView();
		} else {
			currentDiscussionDatabase = discussionDatabase;
			currentSearchQuery = "";
			populateListView();	
		}
	}
	
	public void setDiscussionDatabase(DiscussionDatabase discussionDatabase, String searchString) {
		if (currentDiscussionDatabase == null) {
			currentDiscussionDatabase = discussionDatabase;
			currentSearchQuery = searchString;
			populateListView();
		} else {
			currentDiscussionDatabase = discussionDatabase;
			currentSearchQuery = searchString;
			populateListView();	
		}
	}

	private void populateListView() {
		if (null != currentDiscussionDatabase) {
			ApplicationLog.d(getClass().getSimpleName() + " populateListView", shouldCommitToLog);
			sortPreference = UserSessionTools.getSortPreference(getActivity()); //refresh if changed
			List<DiscussionEntry> tempDiscussionEntries = null;
			
			if(currentSearchQuery != "") {
				tempDiscussionEntries = DatabaseManager.getInstance().getMainDiscussionEntriesByQuery(currentDiscussionDatabase, currentSearchQuery);	
			} else {
				tempDiscussionEntries = currentDiscussionDatabase.getMainEntries();
			}
			ApplicationLog.d(getClass().getSimpleName() + " tempDiscussionEntries.count: " + tempDiscussionEntries.size(), shouldCommitToLog);
			
			final List<DiscussionEntry> discussionEntries = tempDiscussionEntries;
			
//			ApplicationLog.d(getClass().getSimpleName() + " populateListview - sorting", shouldCommitToLog);
//			Collections.sort(discussionEntries, new DiscussionEntryModifiedComparable());
//			
			
			Comparator preferredSortMechanism = null;
			String hottestSortString = getResources().getString(R.string.menu_sort_hottest);
			String dateSortString = getResources().getString(R.string.menu_sort_newest);
			if(sortPreference.contentEquals(hottestSortString) ) {
				preferredSortMechanism = new DiscussionEntryThreadModifiedComparable();
			} else if (sortPreference.contentEquals(dateSortString) ) {
				preferredSortMechanism = new DiscussionEntryModifiedComparable();
			} else {
				preferredSortMechanism = new DiscussionEntryModifiedComparable();
			}
			ApplicationLog.d(getClass().getSimpleName() + " populateListview - sorting", shouldCommitToLog);
			ApplicationLog.d(getClass().getSimpleName() + " using " + preferredSortMechanism.getClass().getSimpleName(), shouldCommitToLog);
			Collections.sort(discussionEntries, preferredSortMechanism);
			
			
			
				
			List<String> titles = new ArrayList<String>();
			for (DiscussionEntry discussionEntry : discussionEntries) {
				String title = discussionEntry.getSubject();
				String modified = discussionEntry.getModified();
				if (modified == null) {
					modified = "?";
				} else {
					modified = modified.substring(0, 10);
				}
				titles.add(title + " (" + modified + ")");
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, titles);
			listView.setAdapter(adapter);
			
			/**
			 * When clicked activate the OnViewItemSelected method with the unid. The enclosing Activity will have to handle
			 * what happens next
			 */
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					DiscussionEntry item = discussionEntries.get(position);
					listener.onViewItemSelected(item.getUnid());
				}
			});
		}
	}

	public interface OnItemSelectedListener {
		public void onViewItemSelected(String unid);
	}

	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

}
