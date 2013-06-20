package org.openntf.domdisc.controllers;

import org.openntf.domdisc.controllers.DiscussionDatabaseController.ReplicateDiscussionDatabase;
import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.DiscussionReplicator;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;
import org.openntf.domdisc.tools.UserSessionTools;

import android.content.Context;
import android.os.AsyncTask;

public class ApplicationLogController {

	public static final int MESSAGE_CLEANUP = 1;
	public static final int MESSAGE_EMPTY = 2;
//	public static final int MESSAGE_ADD_DISCUSSIONENTRYANDREPLICATE = 3;
	
	boolean shouldCommitToLog = false;
	Context context;
	
	public ApplicationLogController(Context context) {
		this.context = context;
		shouldCommitToLog = UserSessionTools.getLogALot(context);
		DatabaseManager.init(context);
	}
	
	public boolean handleMessage(int messageType, Object messageData) {
		switch (messageType) {
		case MESSAGE_CLEANUP :
			//Work
			doCleanup();
			return true;
		case MESSAGE_EMPTY :
			//work
//			addDiscussionEntry((DiscussionEntry) messageData);
			return true;
			
		}
		return false;
	}
	
	
	
	private void doCleanup() {
		// TODO Auto-generated method stub
		Integer numberOfRowsToKeep = new Integer(200);
		ApplicationLog.d(getClass().getSimpleName() + " Will purge all but the last " + numberOfRowsToKeep + " log entries - in background thread", shouldCommitToLog);
		ApplicationLog.w(getClass().getSimpleName() + " NOTE: Doing this in the wrong way right now");
		new CleanupLog().execute(numberOfRowsToKeep);
	}



	class CleanupLog extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			Integer numberOfRowsToKeep = params[0];
			int keepThisManyRows = numberOfRowsToKeep.intValue();
			DatabaseManager.getInstance().removeFirstEntriesFromAppLog(keepThisManyRows); //Wrong method I am calling here.
			return null;
		}

	}
	
	

}
