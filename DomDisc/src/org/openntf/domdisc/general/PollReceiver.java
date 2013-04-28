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

import java.util.GregorianCalendar;

import org.openntf.domdisc.db.DatabaseManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;


public class PollReceiver extends BroadcastReceiver {
	// private static final int PERIOD=240000; // 60000 = 1 min 3600000=1hr

	@Override
	public void onReceive(Context ctxt, Intent i) {
		if (i.getAction() == null) {
			WakefulIntentService.sendWakefulWork(ctxt, ScheduledService.class);
		} else {
			scheduleAlarms(ctxt);
		}
	}

	/**
	 * Enables Background replication service. Will only re-enable if it looks
	 * like replication has not happened within the configured replication
	 * interval
	 * 
	 * @param ctxt
	 */
	public static void scheduleAlarms(Context ctxt) {
		DatabaseManager.init(ctxt);
		int replicationScheduleMinutes = getReplicationScheduleMinutes(ctxt);
		int plannedServiceRepeatIntervalMillis = replicationScheduleMinutes * 60000;
		int minutesSinceLastReplication = minutesSinceLastReplication(ctxt);
		int minuteDifference = minutesSinceLastReplication
				- replicationScheduleMinutes;
		int lastUsedReplicationSchedule = getLastUsedReplicationSchedule(ctxt);

		int unacceptableTimeSchedulePassed = 2; // If more than X minutes past
												// configured schedule we will
												// re-schedule

		boolean shouldLogALot = getLogALot(ctxt);

		ApplicationLog.d("configured replication interval (minutes): "
				+ replicationScheduleMinutes, shouldLogALot);
		ApplicationLog.d(
				"Last actually used schedule for replication interval (minutes): "
						+ lastUsedReplicationSchedule, shouldLogALot);
		ApplicationLog.d("minutes since last replication: "
				+ minutesSinceLastReplication, shouldLogALot);
		ApplicationLog.d("Time difference: " + minuteDifference, shouldLogALot);
		if (minuteDifference > unacceptableTimeSchedulePassed) {
			ApplicationLog
					.d("Time difference looks like background service is stopped. If more than "
							+ unacceptableTimeSchedulePassed
							+ " minutes then we will reschedule", shouldLogALot);
		}

		if ((lastUsedReplicationSchedule == replicationScheduleMinutes)
				&& (minuteDifference < unacceptableTimeSchedulePassed)) {

			ApplicationLog.d(
					"Configured and actually used schedule (minutes) are the same: "
							+ replicationScheduleMinutes
							+ " No rescheduling required", shouldLogALot);

		} else {

			ApplicationLog
					.d("Configured and actually used schedule (minutes) are not the same OR there has been a long time without replication - rescheduling required - will do now",
							shouldLogALot);

			AlarmManager mgr = (AlarmManager) ctxt
					.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(ctxt, PollReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime()
							+ plannedServiceRepeatIntervalMillis,
					plannedServiceRepeatIntervalMillis, pi);

			ApplicationLog.i("Background replication scheduled for every "
					+ replicationScheduleMinutes + " minutes");
			setLastUsedReplicationSchedule(ctxt, replicationScheduleMinutes);
		}

		// if (minuteDifference>0) {
		// if (getLogALot(ctxt)) {
		// ApplicationLog.d("Because background replication should have happened already, I suspect the background service has been disabled. I will now enable it.");
		// }
		// AlarmManager mgr=
		// (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
		// Intent i=new Intent(ctxt, PollReceiver.class);
		// PendingIntent pi=PendingIntent.getBroadcast(ctxt, 0, i, 0);
		//
		// mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		// SystemClock.elapsedRealtime() + plannedServiceRepeatIntervalMillis,
		// plannedServiceRepeatIntervalMillis, pi);
		//
		// ApplicationLog.i("Background replication scheduled for every " +
		// replicationScheduleMinutes + " minutes");
		// setLastUsedReplicationSchedule(ctxt, replicationScheduleMinutes);
		// }

	}

	/**
	 * 
	 * @param ctxt
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
	 * 
	 * @param ctxt
	 * @return GregorianCalendar for when replication happened last. 1970 if
	 *         this is the first time.
	 */
	private static GregorianCalendar getLastreplication(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);

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
	private static int minutesSinceLastReplication(Context applicationContext) {
		GregorianCalendar lastReplication = getLastreplication(applicationContext);
		// ApplicationLog.i("Backgrund service was last run at " +
		// lastReplication.toString());
		GregorianCalendar now = new GregorianCalendar();

		long timePassedSinceLastReplication = now.getTimeInMillis()
				- lastReplication.getTimeInMillis();
		// ApplicationLog.i(" Time passed since last replication: " +
		// timePassedSinceLastReplication.toString());
		long timePassedSinceLastRpelicationMinutes = (timePassedSinceLastReplication / 1000) / 60;

		return (int) timePassedSinceLastRpelicationMinutes;
	}

	/**
	 * 
	 * @param applicationContext
	 * @param scheduleMinutes
	 *            - set the last period (minutes) used for scheduling the
	 *            background task
	 */
	private static void setLastUsedReplicationSchedule(
			Context applicationContext, int scheduleMinutes) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("lastUsedReplicationSchedule", scheduleMinutes);
		editor.commit();
	};

	/**
	 * 
	 * @param applicationContext
	 * @return - int the period (minutes) that was last used for scheduling the
	 *         backgrund task
	 */
	private static int getLastUsedReplicationSchedule(Context applicationContext) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		int LastUsedReplicationSchedule = prefs.getInt(
				"lastUsedReplicationSchedule", 60);
		return LastUsedReplicationSchedule;

	};

}
