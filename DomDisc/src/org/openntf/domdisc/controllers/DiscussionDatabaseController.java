package org.openntf.domdisc.controllers;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.general.DiscussionReplicator;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;
import org.openntf.domdisc.tools.UserSessionTools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;

public class DiscussionDatabaseController{

	public static final int MESSAGE_REPLICATE = 1;
	public static final int MESSAGE_ADD_DISCUSSIONENTRY = 2;
	public static final int MESSAGE_ADD_DISCUSSIONENTRYANDREPLICATE = 3;
	
	private DiscussionDatabase model;
	
	boolean shouldCommitToLog = false;
	Context context;
	
	public DiscussionDatabase getModel() {
		return model;
	}
	
	public DiscussionDatabaseController(DiscussionDatabase discussionDatabase, Context context) {
		this.model = discussionDatabase;
		this.context = context;
		shouldCommitToLog = UserSessionTools.getLogALot(context);
	}
	
	public boolean handleMessage(int messageType, Object messageData) {
		switch (messageType) {
		case MESSAGE_REPLICATE :
			//Work
			replicate();
			return true;
		case MESSAGE_ADD_DISCUSSIONENTRY :
			//work
			addDiscussionEntry((DiscussionEntry) messageData);
			return true;
		case MESSAGE_ADD_DISCUSSIONENTRYANDREPLICATE :
			//work
			addDiscussionEntryAndReplicate((DiscussionEntry) messageData);
			return true;
			
		}
		return false;
	}

	//to call controller.handleMessage(TapListController.MESSAGE_INCREMENT_COUNTER, contextCounter);

//	protected final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {
//        if (!outboxHandlers.isEmpty()) {
//            for (Handler handler : outboxHandlers) {
//                Message msg = Message.obtain(handler, what, arg1, arg2, obj);
//                msg.sendToTarget();
//            }
//        }
//    }

	private void replicate() {
		ApplicationLog.d(getClass().getSimpleName() + " Kicking off background replication", shouldCommitToLog);
		new ReplicateDiscussionDatabase().execute(model);
	}
	private void addDiscussionEntry(DiscussionEntry discussionEntry) {
		ApplicationLog.d(getClass().getSimpleName() + " Adding discussionEntry " + discussionEntry.getSubject(), shouldCommitToLog);
		DatabaseManager.getInstance().createDiscussionEntry(discussionEntry);
	}
	
	private void addDiscussionEntryAndReplicate(DiscussionEntry discussionEntry) {
		ApplicationLog.d(getClass().getSimpleName() + " Adding discussionEntry " + discussionEntry.getSubject(), shouldCommitToLog);
		DatabaseManager.getInstance().createDiscussionEntry(discussionEntry);
		ApplicationLog.d(getClass().getSimpleName() + " Kicking off background replication because a new document was created", shouldCommitToLog);
		new ReplicateDiscussionDatabase().execute(model);
	}

	
	
	class ReplicateDiscussionDatabase extends AsyncTask<DiscussionDatabase, Void, Void> {

		@Override
		protected Void doInBackground(DiscussionDatabase... params) {
			DiscussionDatabase databaseToReplicate = params[0];
			if (databaseToReplicate != null) {
				try {
					double batteryLevel = -1;

					batteryLevel = UserSessionTools.getBatteryLevel(batteryLevel, context);
					ApplicationLog.d("Current battery level: " + batteryLevel, shouldCommitToLog);
					if (batteryLevel < 0.2) {
						ApplicationLog.i("background replication is disabled because of low battery - " + batteryLevel + " (below 20%)");
					} else {
						DiscussionReplicator replicator = new DiscussionReplicator(context);
						replicator.replicateDiscussionDatabase(model);
					}
				} catch (Exception e) {
					e.printStackTrace();
					ApplicationLog.e("Background replication stops due to Exception");
					ApplicationLog.e(e.getMessage());
				}		
			}
			return null;
		}

	}	
}
