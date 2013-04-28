package org.openntf.domdisc.general;

import java.util.Date;

import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.model.AppLog;

import android.util.Log;

public class ApplicationLog {
	
	private static String TAG = "DomDisc";
	
	public final static void e(String logText) {
		String level = "e";
			Log.e(TAG, logText);
			add(logText, level);	
	}

	public final static void i(String logText) {
		String level = "i";
			Log.i(TAG, logText);
			add(logText, level);	
	}
	
	/**
	 * Log debug. Only saves to Log database if shouldCommit is true
	 * @param logText
	 * @param shouldCommitToLog
	 */
	public final static void d(String logText, boolean shouldCommitToLog) {
		String level = "d";
		if (shouldCommitToLog) {
			Log.d(TAG, logText);
			add(logText, level);	
		}
	}

	public final static void w(String logText) {
		String level = "w";
			Log.w(TAG, logText);
			add(logText, level);
	}
	
	private final static void add(String logText, String level) {
		if (logText == null) {
			logText = "No logtext";
		}
		
		try {
			AppLog l = new AppLog();
			l.setMessage(logText);
			l.setLevel(level);
			String currentDateTimeString = java.text.DateFormat.getTimeInstance()
					.format(new Date());
			l.setLogTime(currentDateTimeString);
			
			DatabaseManager.getInstance().addAppLog(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
