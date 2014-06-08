package org.openntf.domdisc.general;

import org.openntf.domdisc.model.DiscussionDatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DiscussionDatabaseTools {
	Context context;
	private boolean shouldCommitToLog = false;
	private DiscussionDatabase discussionDatabase;

	public DiscussionDatabase getDiscussionDatabase() {
		return discussionDatabase;
	}

	public void setDiscussionDatabase(DiscussionDatabase discussionDatabase) {
		this.discussionDatabase = discussionDatabase;
	}

	public DiscussionDatabaseTools(Context context, DiscussionDatabase discussionDatabase) {
		super();
		this.context = context;
		shouldCommitToLog = getLogALot(context);
		this.discussionDatabase = discussionDatabase;
	}
	
	public boolean hasBasicReplicationRequiredFields() {
		String reportString = "";
		int problemCount = 0;
		
		if (discussionDatabase == null) {
			ApplicationLog.w(getClass().getSimpleName() + " hasBasicReplicationRequiredFields examining a null object for discussionDatabase");
			return false;
		}
		
		if (discussionDatabase.getHostName().length()>8) {
			reportString = reportString + " hostName has the minimum length required, ";
		} else {
			reportString = reportString + " hostName is too short - has it been filled in? ";
			problemCount++;
		};
		
		if (discussionDatabase.getDbPath().length()>4) {
			reportString = reportString + " dbPath has the minimum length required, ";
		} else {
			reportString = reportString + " dbPath is too short - has it been filled in? ";
			problemCount++;
		};
		
		if (discussionDatabase.getPassword().length()>0) {
			reportString = reportString + " Password has the minimum length required, ";
		} else {
			reportString = reportString + " Password is too short - has it been filled in? ";
			problemCount++;
		};
		

		if (discussionDatabase.getUserName().length()>2) {
			reportString = reportString + " Username has the minimum length required, ";
		} else {
			reportString = reportString + " Username is too short - has it been filled in? ";
			problemCount++;
		};
		
		//Checking SSL+port configuration
		
		if (discussionDatabase.getHttpPort().equals("") || discussionDatabase.getHostName().toString().equals("80")) {
			if (discussionDatabase.isUseSSL()) {
				ApplicationLog.w(" Database is configured to use SSL, while the port is configured to be 80.");
				ApplicationLog.w(" This is unlikely to be the combination you want. SSL usually is on port 443.");
				reportString = reportString + " Database is configured to use SSL, while the port is configured to be 80 - possibly a configuration error.";
			}
		};
		
		if (discussionDatabase.getHttpPort().equals("443") ) {
			if (!discussionDatabase.isUseSSL()) {
				ApplicationLog.w(" Database is configured to use port 443, while SSL is not enabled.");
				ApplicationLog.w(" This is unlikely to be the combination you want. ");
				reportString = reportString + " Database is configured to use port 443, while ssl is disabled - possibly a configuration error.";
			}
		}
		
		
		if (problemCount > 0) {
			ApplicationLog.w(getClass().getSimpleName() + " hasBasicReplicationRequiredFields examining a discussionDatabase (not ok): " + reportString);
			return false;
		} else {
			ApplicationLog.d(getClass().getSimpleName() + " hasBasicReplicationRequiredFields examining a discussionDatabase (ok): " + reportString , shouldCommitToLog);
			return true;	
		}
	};
	
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

	
}
