package org.openntf.domdisc.db;

import java.sql.SQLException;
import java.util.List;

import org.openntf.domdisc.model.AppLog;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;


import android.content.Context;
import android.text.GetChars;
import android.util.Log;


public class DatabaseManager {

	static private DatabaseManager instance;

	static public void init(Context ctx) {
		if (null==instance) {
			instance = new DatabaseManager(ctx);
		}
	}

	static public DatabaseManager getInstance() {
		if (null == instance) {
			Log.d("DatabaseManager", "instance not initialized");
		}
		return instance;
	}

	private DatabaseHelper helper;
	private DatabaseManager(Context ctx) {
		helper = new DatabaseHelper(ctx);
	}

	private DatabaseHelper getHelper() {
		return helper;
	}

	public List<DiscussionDatabase> getAllDiscussionDatabases() {
		//		Log.d("debug", "getAllDiscussionDatabases 1");
		List<DiscussionDatabase> DiscussionDatabases = null;
		try {
			//			Log.d("debug", "getAllDiscussionDatabases 2");
			DiscussionDatabases = getHelper().getDiscussionDatabaseDao().queryForAll();
			//			Log.d("debug", "getAllDiscussionDatabases 3");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return DiscussionDatabases;
	}

	public void addDiscussionDatabase(DiscussionDatabase l) {
		try {
			getHelper().getDiscussionDatabaseDao().create(l);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public DiscussionDatabase getDiscussionDatabaseWithId(int DiscussionDatabaseId) {
		DiscussionDatabase DiscussionDatabase = null;
		try {
			DiscussionDatabase = getHelper().getDiscussionDatabaseDao().queryForId(DiscussionDatabaseId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return DiscussionDatabase;
	}


	//	public DiscussionEntry getDiscussionEntryWithId(int discussionEntryId) {
	//		DiscussionEntry discussionEntry = null;
	//		try {
	//			discussionEntry = getHelper().getDiscussionEntryDao().queryForId(discussionEntryId);
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		return discussionEntry;
	//	}

	public DiscussionEntry getDiscussionEntryWithId(String unid) {
		DiscussionEntry discussionEntry = null;
		try {
			discussionEntry = getHelper().getDiscussionEntryDao().queryForId(unid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntry;
	}


	/**
	 * @param discussionEntry
	 * @return List of responses 
	 */
	public List<DiscussionEntry> getResponseDicussionEntries(DiscussionEntry discussionEntry) {

		List<DiscussionEntry> responseEntries = null;

		try {
			Dao<DiscussionEntry, String> discussionEntryDao = getHelper().getDiscussionEntryDao();
			QueryBuilder<DiscussionEntry, String> queryBuilder = discussionEntryDao.queryBuilder();
			queryBuilder.where().eq(DiscussionEntry.PARENTID_FIELD_NAME, discussionEntry.getUnid());
			PreparedQuery<DiscussionEntry> preparedQuery = queryBuilder.prepare();
			responseEntries = discussionEntryDao.query(preparedQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return responseEntries;
	}

	/**
	 * @param discussionDatabase
	 * @return List of DiscussionEntry containing entries that were created in the app
	 * but have not yet been submitted to the database. Entries with no noteid = locally created
	 */
	public List<DiscussionEntry> getDiscussionEntriesForSubmit(DiscussionDatabase discussionDatabase) {
		List<DiscussionEntry> discussionEntries = null;

		try {
			Dao<DiscussionEntry, String> discussionEntryDao = getHelper().getDiscussionEntryDao();
			QueryBuilder<DiscussionEntry, String> queryBuilder = discussionEntryDao.queryBuilder();
			Where<DiscussionEntry, String> where = queryBuilder.where();

//			where.eq(DiscussionEntry.NOTEID_FIELD_NAME, ""); // <- noteid empty means that document was created locally in the app
			where.isNull(DiscussionEntry.NOTEID_FIELD_NAME); // <- noteid empty means that document was created locally in the app
			where.and();
			where.eq(DiscussionEntry.DISCUSSIONDB_FIELD_NAME, discussionDatabase);
			
			PreparedQuery<DiscussionEntry> preparedQuery = queryBuilder.prepare();
			
			Log.d(getClass().getSimpleName(),  " PreparedQuery: " + preparedQuery.toString());
			discussionEntries = discussionEntryDao.query(preparedQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntries;
	}
	
	/**
	 * @param discussionDatabase
	 * @param query
	 * @return List of DiscussionEntries that have a subject containing the query string and that are in the discussionDatabase
	 */
	public List<DiscussionEntry> getMainDiscussionEntriesByQuery(DiscussionDatabase discussionDatabase, String query) {
		List<DiscussionEntry> discussionEntries = null;

		try {
			Dao<DiscussionEntry, String> discussionEntryDao = getHelper().getDiscussionEntryDao();
			QueryBuilder<DiscussionEntry, String> queryBuilder = discussionEntryDao.queryBuilder();
			Where<DiscussionEntry, String> where = queryBuilder.where();

			where.like(DiscussionEntry.SUBJECT_FIELD_NAME, "%" + query + "%");
			where.and();
			where.eq(DiscussionEntry.DISCUSSIONDB_FIELD_NAME, discussionDatabase);
						
			PreparedQuery<DiscussionEntry> preparedQuery = queryBuilder.prepare();
			
			Log.d(getClass().getSimpleName(),  " PreparedQuery: " + preparedQuery.toString());
			discussionEntries = discussionEntryDao.query(preparedQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntries;
	}

	public DiscussionEntry newDiscussionEntry() {
		DiscussionEntry discussionEntry = new DiscussionEntry();
		try {
			getHelper().getDiscussionEntryDao().create(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return discussionEntry;
	}

	public void createDiscussionEntry(DiscussionEntry discussionEntry) {
		//		DiscussionEntry discussionEntry = new DiscussionEntry();
		try {
			getHelper().getDiscussionEntryDao().create(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//		return discussionEntry;
	}

	public void deleteDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().delete(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteDiscussionEntry(DiscussionEntry discussionEntry) {
		try {
			getHelper().getDiscussionEntryDao().delete(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	public void updateDiscussionEntry(DiscussionEntry discussionEntry) {
		try {
			getHelper().getDiscussionEntryDao().update(discussionEntry);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refreshDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().refresh(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateDiscussionDatabase(DiscussionDatabase DiscussionDatabase) {
		try {
			getHelper().getDiscussionDatabaseDao().update(DiscussionDatabase);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	/*
	 * ApplicationLog
	 * 
	 */


	public List<AppLog> getAllAppLogs() {
		List<AppLog> appLogs = null;
		try {
			appLogs = getHelper().getAppLogDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return appLogs;
	}

	public void addAppLog(AppLog l) {
		try {
			getHelper().getAppLogDao().create(l);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AppLog getAppLogWithId(int appLogId) {
		AppLog appLog = null;
		try {
			appLog = getHelper().getAppLogDao().queryForId(appLogId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return appLog;
	}

	public void deleteAppLog(AppLog appLog) {
		try {
			getHelper().getAppLogDao().delete(appLog);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void emptyAppLogOld() {
		try {
			Dao appLogDao =  getHelper().getAppLogDao();
			List<AppLog> allAppLogs = appLogDao.queryForAll();
			appLogDao.delete(allAppLogs);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Deletes all entries in the log
	 */
	public void emptyAppLog() {

		Dao appLogDao =  getHelper().getAppLogDao();
		//			UpdateBuilder<AppLog, String> updateBuilder = appLogDao.updateBuilder();
		DeleteBuilder<AppLog, String> deleteBuilder = appLogDao.deleteBuilder();

		try {
			deleteBuilder.where().isNotNull(AppLog.ID_FIELD_NAME);
			deleteBuilder.delete();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Deletes first numberToDelete entries in the log
	 *
	 */
	
	//I am afraid that this is not good enough. It assumes that row IDs do not keep increasing 
	public void removeFirstEntriesFromAppLog(int numberToDelete) {

		Dao appLogDao =  getHelper().getAppLogDao();
		DeleteBuilder<AppLog, String> deleteBuilder = appLogDao.deleteBuilder();

		try {
			long rowCount = appLogDao.countOf();
			long deleteUpTo = rowCount - (rowCount-numberToDelete);
			Log.i(getClass().getSimpleName(),  " number of log rows: " + rowCount);
			Log.i(getClass().getSimpleName(),  " deleting rows lower than : " + deleteUpTo);
			deleteBuilder.where().le(AppLog.ID_FIELD_NAME, deleteUpTo);
			deleteBuilder.delete();
			rowCount = appLogDao.countOf();
			Log.i(getClass().getSimpleName(),  " number of log rows: " + rowCount);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}