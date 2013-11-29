/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package org.openntf.domdisc.general;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openntf.domdisc.R;
import org.openntf.domdisc.controllers.ApplicationLogController;
import org.openntf.domdisc.controllers.DiscussionDatabaseController;
import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.tools.DateUtil;
import org.openntf.domdisc.tools.UserSessionTools;
import org.openntf.domdisc.ui.LogListActivity;
import org.openntf.domdisc.ui.StartActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.commonsware.cwac.wakeful.WakefulIntentService;


/**
 * @author jbr
 *
 */
public class ScheduledService extends WakefulIntentService {
	
	boolean shouldLogALot = false;
	public ScheduledService() {
		super("ScheduledService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
//		Log.d(getClass().getSimpleName(), "I ran from DomDisc service");
		DatabaseManager.init(this);
		Context applicationContext = getApplicationContext();
		ApplicationLog.i("Background replication service starting");
		long timePassedSinceLastRpelicationMinutes = minutesSinceLastReplication(applicationContext); 
		ApplicationLog.i(" Minutes since last replication: " + timePassedSinceLastRpelicationMinutes);

		GregorianCalendar now = new GregorianCalendar();
		setLastReplication(now, applicationContext);
		
		shouldLogALot = getLogALot(applicationContext);

		try {
			double batteryLevel = -1;

			batteryLevel = UserSessionTools.getBatteryLevel(batteryLevel, getApplicationContext());
			ApplicationLog.d("Current battery level: " + batteryLevel, shouldLogALot);
			if (batteryLevel < 0.2) {
								ApplicationLog.i("background replication is disabled because of low battery - " + batteryLevel + " (below 20%)");
			} else {
				DiscussionReplicator replicator = new DiscussionReplicator(applicationContext);

				List<DiscussionDatabase> discussionDatabases = DatabaseManager.getInstance().getAllDiscussionDatabases();

				if ((null != discussionDatabases) && (discussionDatabases.size() > 0)) {


					for (int i = 0, size = discussionDatabases.size(); i < size; i++)  
					{  
						int updateCounter = 0;
						DiscussionDatabase discussionDatabase = discussionDatabases.get(i);
						ApplicationLog.i("== == == == ==");
						ApplicationLog.i("background Replicating " + discussionDatabase.getName());
						updateCounter = replicator.replicateDiscussionDatabase(discussionDatabase);
						if (updateCounter > 0) {
							if (UserSessionTools.getNotifyWhenNew(applicationContext)){
								notifyUser("Added " + updateCounter + " entries to " + discussionDatabase.getName(), "New entries", applicationContext);
							}
							
						} else if (updateCounter < 0) {
							if (hasTimeSinceLastSuccesfulReplicationBeenTooLong(discussionDatabase.getLastSuccesfulReplicationDate(), applicationContext)) {
								if (UserSessionTools.getNotifyWhenFail(applicationContext)){
									notifyUserError("Failed for " + discussionDatabase.getName() + " - Last replication was on " + DateUtil.getDateLong(discussionDatabase.getLastSuccesfulReplicationDate()), "Failed replication", applicationContext);	
								}
							}
						}
						ApplicationLog.i("== == == == ==");
					}


				} else {
					ApplicationLog.i("Background replication stops - no databases configured");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			ApplicationLog.e("Background replication stops due to Exception");
			ApplicationLog.e(e.getMessage());
		}
		
//		ApplicationLogController appLogController = new ApplicationLogController(getApplicationContext());
//		appLogController.handleMessage(ApplicationLogController.MESSAGE_CLEANUP, null);
		int keepThisManyRows = 1000;
		ApplicationLog.d(getClass().getSimpleName() + " Will purge all but the last " + keepThisManyRows + " log entries - in background thread", shouldLogALot);
		DatabaseManager.getInstance().removeAllExceptNEntriesFromAppLog(keepThisManyRows); //Wrong method I am calling here.

	}


//	private double getBatteryLevel(double batteryLevel) {
//		//Battery: http://stackoverflow.com/questions/3661464/get-battery-level-before-broadcast-receiver-responds-for-intent-action-battery-c
//		Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
//				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//		int rawlevel = batteryIntent.getIntExtra("level", -1);
//		double scale = batteryIntent.getIntExtra("scale", -1);
//		ApplicationLog.d("battery rawLevel: " + rawlevel, shouldLogALot);
//		ApplicationLog.d("battery scale: " + scale, shouldLogALot);
//		
//		if (rawlevel == 0) {
//			ApplicationLog.i("battery rawLevel: " + rawlevel + " looks wrong. Assuming 50. Will not be able to properly measure battery level on this device.");
//			rawlevel = 50;
//		}
//		if (rawlevel >= 0 && scale > 0) {
//			batteryLevel = rawlevel / scale;
//		}
//		return batteryLevel;
//	}

	private void setLastReplication(GregorianCalendar replicationTime, Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		SharedPreferences.Editor editor = prefs.edit();
		long millis = replicationTime.getTimeInMillis();
		String toSave = Long.toString(millis);
		editor.putString("lastReplicationTime", toSave);
		editor.commit();
	}

	/**
	 * 
	 * @param ctxt
	 * @return GregorianCalendar for when replication happened last. 1970 if this is the first time.
	 */
	private GregorianCalendar getLastreplication(Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);

		String lastReplication = prefs.getString("lastReplicationTime", "0");

		long milliseconds = Long.parseLong(lastReplication);

		GregorianCalendar lastReplicationTime = new GregorianCalendar();
		lastReplicationTime.setTimeInMillis(milliseconds);
		return lastReplicationTime;
	}


	/**
	 * 
	 * @param applicationContext
	 * @return Long - minutes since background replication task ran last
	 */
	private int minutesSinceLastReplication(Context applicationContext) {
		GregorianCalendar lastReplication = getLastreplication(applicationContext);
		GregorianCalendar now = new GregorianCalendar();

		long timePassedSinceLastReplication = now.getTimeInMillis() - lastReplication.getTimeInMillis();
		//		ApplicationLog.i(" Time passed since last replication: " + timePassedSinceLastReplication.toString());
		long timePassedSinceLastRpelicationMinutes = (timePassedSinceLastReplication/1000)/60;
		return (int)timePassedSinceLastRpelicationMinutes;
	}
	/**
	 * 
	 * @param ctxt
	 * @return boolean true if we should log all debuglevels to the
	 *         ApplicationLog
	 */
	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}
	
	/**
	 * @param notificationText Permanently displayed text
	 * @param tickerText Shows briefly when notifying
	 * @param context 
	 */
	private void notifyUser(String notificationText, String tickerText, Context context) {
		Intent intent = new Intent(this, StartActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder notificationBuilder  = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle("Replication");
		notificationBuilder.setContentText(notificationText);		
		notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify);

		notificationBuilder.setTicker(tickerText);
		notificationBuilder.setContentIntent(pIntent);
//		notificationBuilder.setOnlyAlertOnce(true);
		notificationBuilder.setAutoCancel(true);
		  
		NotificationManager notificationManager = 
		  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(0, notificationBuilder.build());
		if (UserSessionTools.getNotifyToPebble(context)){
			sendToPebble(notificationText);	
		}
	}
	
	/**
	 * @param notificationText Permanently displayed text
	 * @param tickerText Shows briefly when notifying
	 */
	private void notifyUserError(String notificationText, String tickerText, Context context) {
		Intent intent = new Intent(this, LogListActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder notificationBuilder  = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle("Replication");
		notificationBuilder.setContentText(notificationText);
		notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify);
		notificationBuilder.setTicker(tickerText);
		notificationBuilder.setContentIntent(pIntent);
//		notificationBuilder.setOnlyAlertOnce(true);
		notificationBuilder.setAutoCancel(true);
		  
		NotificationManager notificationManager = 
		  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(0, notificationBuilder.build());
		if (UserSessionTools.getNotifyToPebble(context)){
			sendToPebble(notificationText);	
		}
		
	}
	
	public void sendToPebble(String pebbleMessage) {
	    final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

	    final Map<String, String> data = new HashMap<String, String>();
	    data.put("title", "replication");
	    data.put("body", pebbleMessage);
	    final JSONObject jsonData = new JSONObject(data);
	    final String notificationData = new JSONArray().put(jsonData).toString();

	    i.putExtra("messageType", "PEBBLE_ALERT");
	    i.putExtra("sender", "DomDisc");
	    i.putExtra("notificationData", notificationData);

//	    Log.d(TAG, "About to send a modal alert to Pebble: " %2B notificationData);
	    
	    ApplicationLog.i("Will send alert to Pebble: " + notificationData);
	    sendBroadcast(i);
	    
	}
	
	/**
	 * @param lastSuccesfulReplication
	 * @param context
	 * @return true if unacceptably long time has passed since the database was last replicated with success
	 * the acceptable time is based on the configured replication interval. If the interval is frequent we accept a relatively long
	 * wait. If the interval is infrequent we are less accepting.
	 */
	private boolean hasTimeSinceLastSuccesfulReplicationBeenTooLong(Date lastSuccesfulReplication, Context context) {
		//Samples of acceptable wait times =(A3*3)-(A3/2)+5
		// replication configured for X minutes | acceptable wait time minutes (approximate)
		// 60	1hr		| 155	2.5hr 
		// 240	4hr		| 605	10hr
		// 1440	24hr	| 3605	60hr
		Date nowDateTime = new Date();
		long difference = nowDateTime.getTime()- lastSuccesfulReplication.getTime();
		long diffMinutes = difference / (60 * 1000) % 60; // how long time has passed since replication happened
		
		int scheduledReplicationIntervalMinutes = getReplicationScheduleMinutes(context);
		
		int acceptableIntervalBetweenReplicationMinutes = (int) ((scheduledReplicationIntervalMinutes)*3) -(scheduledReplicationIntervalMinutes/2) + 5; 
		ApplicationLog.d("acceptableIntervalBetweenReplication (mins): " +acceptableIntervalBetweenReplicationMinutes, shouldLogALot );
		ApplicationLog.d("current time since succesful repl (mins): " + diffMinutes, shouldLogALot );
		return (diffMinutes > acceptableIntervalBetweenReplicationMinutes);
	}
	
	/**
	 * 
	 * @return int how many minutes should be between background replication
	 *         events
	 */
	private static int getReplicationScheduleMinutes(Context ctxt) {
		 
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);

		int replicationSchedulePreferenceMinutes = Integer.parseInt(prefs
				.getString("list_preference_logschedule", "60"));

		DatabaseManager.init(ctxt);

		boolean shouldLogAlot = getLogALot(ctxt);

		ApplicationLog.d("Read list_preference_logschedule: "
				+ replicationSchedulePreferenceMinutes, shouldLogAlot);

		return replicationSchedulePreferenceMinutes;

	}

}
