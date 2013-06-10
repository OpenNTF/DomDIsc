package org.openntf.domdisc.tools;

import org.openntf.domdisc.R;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.general.ApplicationLog;
import org.openntf.domdisc.model.DiscussionDatabase;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class UserSessionTools {
	
	
	
	/**
	 * @param ctx
	 * @return True if we have a network connection
	 */
	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return true;
		}
		return true;
	}
	
	public static double getBatteryLevel(double batteryLevel, Context context) {
		//Battery: http://stackoverflow.com/questions/3661464/get-battery-level-before-broadcast-receiver-responds-for-intent-action-battery-c
		Intent batteryIntent = context.registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
//		ApplicationLog.d("battery rawLevel: " + rawlevel, shouldLogALot);
//		ApplicationLog.d("battery scale: " + scale, shouldLogALot);
		
		if (rawlevel == 0) {
			ApplicationLog.i("battery rawLevel: " + rawlevel + " looks wrong. Assuming 50. Will not be able to properly measure battery level on this device.");
			rawlevel = 50;
		}
		if (rawlevel >= 0 && scale > 0) {
			batteryLevel = rawlevel / scale;
		}
		return batteryLevel;
	}
	
	
	public void setLastOpenDiscussionDatabase(int databaseId, Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		SharedPreferences.Editor editor = prefs.edit();
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
	public int getLastOpenDiscussionDatabase(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		int databaseId = prefs.getInt("lastOpenDiscussionDatabase", -1);

		// DatabaseManager.init(context);
		// ApplicationLog.d("Read lastOpenDiscussionDatabase: " + databaseId);

		DiscussionDatabase discussionDatabase = DatabaseManager.getInstance()
				.getDiscussionDatabaseWithId(databaseId);
		if (discussionDatabase != null) {
			return databaseId;
		} else {
			return -1;
		}

	}

	
	public static void setSortPreference(String sortPreference, Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("sortPreference", sortPreference);

		 DatabaseManager.init(context);
//		 ApplicationLog.d("Set sortPreference: " + sortPreference, true);

		// Commit the edits!
		editor.commit();
	};

	public static String getSortPreference(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String sortPreference = prefs.getString("sortPreference", context.getResources().getString(R.string.menu_sort_hottest));

		 DatabaseManager.init(context);
//		 ApplicationLog.d("Read sortPreference: " + sortPreference, true);

		return sortPreference;

	}

	/**
	 * 
	 * @param ctxt
	 * @return boolean true if we should log all debuglevels to the
	 *         ApplicationLog
	 */
	public static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

}
