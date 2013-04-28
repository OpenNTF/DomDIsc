package org.openntf.domdisc.ui;

import java.util.ArrayList;
import java.util.List;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.Constants;
import org.openntf.domdisc.model.DiscussionEntry;
import org.openntf.domdisc.ui.DiscussionMainEntriesViewFragment.OnItemSelectedListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.openntf.domdisc.R;

public class ReadDiscussionEntryFragment extends SherlockFragment implements OnClickListener {

public final static int create_menu_id = 9874;
	private DiscussionEntry currentDiscussionEntry = null;
	private String currentUnid = "";
	private boolean shouldCommitToLog = false;
	Activity myActivity = null;


	private TextView subjectView;
	private TextView authorView;
	private WebView webView;
	private ListView responseView;
	private Button toggleBodyResponsesVisible;
	
	int responseCount = 0; // Nuber of resposes for the currentDiscussionEntry
	
	ArrayAdapter<String> adapter = null;
	
	//Default is to display the body and not the responses
	private boolean showBody = true;
	
	private OnResponseItemSelectedListener listener;


    /**
     * Create a new instance of ReadDiscussionEntryFragment that will be initialized
     * with the given argument that points at a document to display.
     */
    static ReadDiscussionEntryFragment newInstance(CharSequence unid) {
    	ReadDiscussionEntryFragment f = new ReadDiscussionEntryFragment();
        Bundle b = new Bundle();
        b.putCharSequence("unid", unid);
        f.setArguments(b);
        return f;
    }
    
    /**
     * During creation, if arguments have been supplied to the fragment
     * then parse those out.
     */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = getActivity();
		shouldCommitToLog = getLogALot(myActivity);
        DatabaseManager.init(myActivity);

        Bundle args = getArguments();
        if (args != null) {
            CharSequence unid = args.getCharSequence("unid");
            if (unid != null) {
            	ApplicationLog.d(getClass().getSimpleName() + " got a unid: " + unid, shouldCommitToLog);
                currentUnid = unid.toString();
                currentDiscussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(currentUnid);
            }
        }
        setHasOptionsMenu(true);
    }

	
	@Override
	public void onStart() {
		super.onStart();
		//Mved this here from the OnCreateView method in order to do a refresh when composing Reponse documents and returning to this Activity
		if (currentDiscussionEntry == null)  {
			ApplicationLog.w(getClass().getSimpleName() +  " onStart: No discussionEntry to show");
		} else {
			populateFooter();
		}
		
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ApplicationLog.d(getClass().getSimpleName() +  " onCreateView", shouldCommitToLog);
		DatabaseManager.init(myActivity);
		View view = inflater.inflate(R.layout.read_discussion_entry_with_children, container, false);
		//temp
		subjectView = (TextView) view.findViewById(R.id.subject);
		authorView = (TextView) view.findViewById(R.id.author);
		toggleBodyResponsesVisible = (Button) view.findViewById(R.id.toggle_body_responses);
		toggleBodyResponsesVisible.setOnClickListener(this);
		
		webView = (WebView) view.findViewById(R.id.bodyhtml);
//		webView.setVisibility(View.GONE);
		responseView = (ListView) view.findViewById(R.id.responsesview);
		
//		Moved to onStart
//		If we were fed a unid from a Bundle, we will proceed and load the Document and show it
//		if (currentUnid != null) {
//			DatabaseManager.init(myActivity);
//			currentDiscussionEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(currentUnid);
//			if (currentDiscussionEntry != null) {
//				setDiscussionEntry(currentDiscussionEntry);
//			}
//		}
		
		if (currentDiscussionEntry != null) {
			setDiscussionEntry(currentDiscussionEntry);
		}
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		
//		menu.add("Menu 1a").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		String noteId = currentDiscussionEntry.getNoteid();
		
		if (noteId != null && noteId.length() > 0) {
			menu.add(com.actionbarsherlock.view.Menu.NONE, create_menu_id, com.actionbarsherlock.view.Menu.NONE, "Create Response").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} {
			ApplicationLog.d(getClass().getSimpleName() + " onCreateOptionsMenu not displaying Create Response button because current entry was created locally and not yet replicated to the server", shouldCommitToLog);
		}
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Activity activity = getActivity();
		Intent intent = null;
		switch (item.getItemId()) {
		case create_menu_id:
			intent = new Intent(activity,AddDiscussionEntryActivity.class);
			intent.putExtra(Constants.keyDiscussionDatabaseId, currentDiscussionEntry.getDiscussionDatabase().getId());
			intent.putExtra(Constants.keyDiscussionEntryId, currentDiscussionEntry.getUnid());
			startActivity (intent);
			return true;
		}
		return true;
		}
	
	@Override
	public void onClick(View v){
		
		//If this is the toggle button we will go on and toggle - checking because we might add other clickables
		if (v.getId() == toggleBodyResponsesVisible.getId()) {
			toggleShowBodyResponses();			
		}
		

	}

	private void toggleShowBodyResponses() {
		if (showBody == true) {
			showBody = false;
		} else {
			showBody = true;
		}
		enforceBodyResponsesVisibility();
	}

	private void enforceBodyResponsesVisibility() {
		if (showBody == true) {
			webView.setVisibility(View.VISIBLE);
			responseView.setVisibility(View.GONE);
			String buttonText = getResources().getString(R.string.toggle_body_responses_button_body_visible);
			buttonText = buttonText.replace("%1", String.valueOf(responseCount));
			toggleBodyResponsesVisible.setText(buttonText);
			if (responseCount == 0) {
				toggleBodyResponsesVisible.setTextColor(Color.GRAY);	
			}
//			toggleBodyResponsesVisible.setText(R.string.toggle_body_responses_button_body_visible);
			
		} else {
			webView.setVisibility(View.GONE);
			responseView.setVisibility(View.VISIBLE);
			toggleBodyResponsesVisible.setText(R.string.toggle_body_responses_button_responses_visible); 
		}
	}

	/**
	 * Will display the discussionEntry and its children
	 * @param discussionEntry
	 */
	private void setDiscussionEntry(DiscussionEntry discussionEntry) {
		
		if (discussionEntry == null)  {
			ApplicationLog.w(getClass().getSimpleName() +  " setDiscussionEntry: No discussionEntry to show");
		} else {
			ApplicationLog.d(getClass().getSimpleName() +  " Showing dicsussionentry " + discussionEntry.getSubject(), shouldCommitToLog);
			currentDiscussionEntry = discussionEntry;
			populateHeader();
			populateBody();
			populateFooter(); // 	
			showBody = true; //Default is to show the body text
			enforceBodyResponsesVisibility();
		}
		
		// Show UP ?
		//SHow Action buttons?
	}
	
	private void populateFooter() {

//		Populate Response View
		ApplicationLog.d(getClass().getSimpleName() +  " building footer ", shouldCommitToLog);
		
		final List<DiscussionEntry> responseEntries = DatabaseManager.getInstance().getResponseDicussionEntries(currentDiscussionEntry);
		
		if (responseEntries != null) {
			responseCount = responseEntries.size();	
		}
		
		if (responseEntries == null || responseCount == 0	) {
			ApplicationLog.d(getClass().getSimpleName() + " No responses. Will not display any", shouldCommitToLog);
			if (adapter != null) {
				adapter.clear();
				adapter.notifyDataSetInvalidated();	
			}
		} else {
			ApplicationLog.d(getClass().getSimpleName() + " number of responses: " + responseCount, shouldCommitToLog);
			
			if (responseCount > 0) {
				List<String> titles = new ArrayList<String>();
				for (DiscussionEntry responseEntry : responseEntries) {
					titles.add(responseEntry.getSubject());
				}
				adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, titles);
				responseView.setAdapter(adapter);
				
				/**
				 * When clicked activate the OnViewItemSelected method with the unid. The enclosing Activity will have to handle
				 * what happens next
				 */
				responseView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
						DiscussionEntry item = responseEntries.get(position);
						listener.onResponseViewItemSelected(item.getUnid());
					}
				});
			}
		}
		
		
		
		
		
		
		
	}
	
	

	private void populateBody() {
		ApplicationLog.d(getClass().getSimpleName() +  " building body", shouldCommitToLog);
		String bodyHtml = currentDiscussionEntry.getBody() ;
		webView.loadDataWithBaseURL(null, bodyHtml, "text/html", "UTF-8", null);
	}

	private void populateHeader() {
		ApplicationLog.d(getClass().getSimpleName() +  " building header", shouldCommitToLog);
		String subject = currentDiscussionEntry.getSubject();
		String abbrFrom = currentDiscussionEntry.getAbbreviateFrom();
		String authorToDisplay = "";
		if (abbrFrom != null && abbrFrom.length()>0) {
			authorToDisplay = abbrFrom;
		} else {
			String author = currentDiscussionEntry.getAuthors(); // getAuthors can be an arry - not handlede in the app yet - we prefer abbrFrom
			if (author != null && author.length()>0) {
				authorToDisplay = author;
			} else {
				authorToDisplay = "Unknown";
			}
			
		}
		subjectView.setText(subject);
		authorView.setText(authorToDisplay);
	}


	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
	
	
	/**
	 * Forcing Activities that use this Class to implement this interface
	 * @author Jens
	 *
	 */
	public interface OnResponseItemSelectedListener {
		public void onResponseViewItemSelected(String unid);
	}
	
	/**
	 * Hooks the listener to the enclosing Activity
	 */	
	 @Override
	    public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnResponseItemSelectedListener) {
	        listener = (OnResponseItemSelectedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement ReadDiscussionEntryFragment.OnResponseItemSelectedListener");
	      }
	    }

}
