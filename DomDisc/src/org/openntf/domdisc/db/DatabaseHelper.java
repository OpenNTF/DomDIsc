package org.openntf.domdisc.db;

import org.openntf.domdisc.model.AppLog;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "DiscDB.sqlite";

	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 14;

	// the DAO object we use to access the SimpleData table
	private Dao<DiscussionDatabase, Integer> discussionDatabaseDao = null;
	// private Dao<DiscussionEntry, Integer> discussionEntryDao = null;
	private Dao<DiscussionEntry, String> discussionEntryDao = null;
	private Dao<AppLog, Integer> appLogDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, DiscussionDatabase.class);
			TableUtils.createTable(connectionSource, DiscussionEntry.class);
			TableUtils.createTable(connectionSource, AppLog.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		Log.d(DatabaseHelper.class.getName(), "Updating database");
		try {
			// List<String> allSql = new ArrayList<String>();
			// switch(oldVersion)
			// {
			// case 1:
			// //allSql.add("alter table AdData add column `new_col` VARCHAR");
			// //allSql.add("alter table AdData add column `new_col2` VARCHAR");
			// }
			// for (String sql : allSql) {
			// db.execSQL(sql);
			// }
			Log.i(DatabaseHelper.class.getName(),
					"Dropping all database tables and recreating");
			TableUtils.dropTable(connectionSource, DiscussionDatabase.class,
					true);
			TableUtils.dropTable(connectionSource, DiscussionEntry.class, true);
			TableUtils.dropTable(connectionSource, AppLog.class, true);
			TableUtils.createTable(connectionSource, DiscussionDatabase.class);
			TableUtils.createTable(connectionSource, DiscussionEntry.class);
			TableUtils.createTable(connectionSource, AppLog.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "exception during onUpgrade",
					e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}

	}

	public Dao<DiscussionDatabase, Integer> getDiscussionDatabaseDao() {
		if (null == discussionDatabaseDao) {
			try {
				discussionDatabaseDao = getDao(DiscussionDatabase.class);
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			}
		}
		return discussionDatabaseDao;
	}

	// public Dao<DiscussionEntry, Integer> getDiscussionEntryDao() {
	// if (null == discussionEntryDao) {
	// try {
	// discussionEntryDao = getDao(DiscussionEntry.class);
	// }catch (java.sql.SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// return discussionEntryDao;
	// }

	public Dao<DiscussionEntry, String> getDiscussionEntryDao() {
		if (null == discussionEntryDao) {
			try {
				discussionEntryDao = getDao(DiscussionEntry.class);
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			}
		}
		return discussionEntryDao;
	}

	public Dao<AppLog, Integer> getAppLogDao() {
		if (null == appLogDao) {
			try {
				appLogDao = getDao(AppLog.class);
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			}
		}
		return appLogDao;
	}

}
