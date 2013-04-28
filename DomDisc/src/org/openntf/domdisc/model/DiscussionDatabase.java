package org.openntf.domdisc.model;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class DiscussionDatabase {
	@DatabaseField(generatedId=true)
	private int id;
	
	@DatabaseField
	private String name;
	
	@ForeignCollectionField
	private ForeignCollection<DiscussionEntry> discussionEntries;

	@DatabaseField
	private String hostName;

	@DatabaseField
	private String dbPath;
	
	@DatabaseField
	private String userName;
	
	@DatabaseField
	private String password;
	
	@DatabaseField
	private boolean useSSL;
	
	@DatabaseField
	private String httpPort;	
	

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDiscussionEntries(ForeignCollection<DiscussionEntry> discussionEntries) {
		this.discussionEntries = discussionEntries;
	}

	/**
	 * @return List<DiscussionEntry> of all stored DiscussionEntries
	 */
	public List<DiscussionEntry> getDiscussionEntries() {
		ArrayList<DiscussionEntry> itemList = new ArrayList<DiscussionEntry>();
		for (DiscussionEntry discussionEntry : discussionEntries) {
			itemList.add(discussionEntry);
		}
		return itemList;
	}
	
	public List<DiscussionEntry> getMainEntries() {
//		Log.d(getClass().getSimpleName(), "getMainEntries");
		ArrayList<DiscussionEntry> itemList = new ArrayList<DiscussionEntry>();
		for (DiscussionEntry discussionEntry : discussionEntries) {
//			Log.d(getClass().getSimpleName(), " checking 0 : " + discussionEntry.getSubject());
			String parentid = discussionEntry.getParentid();
			if (parentid == null || parentid.length() == 0) {
				itemList.add(discussionEntry);	
//				Log.d(getClass().getSimpleName(), "  added to list");
			} else {
//				Log.d(getClass().getSimpleName(), "  not added to list ");
//				Log.d(getClass().getSimpleName(), "  parentid is>" + parentid + "<");
			}
		}
		return itemList;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(String httpPort) {
		this.httpPort = httpPort;
	}
}
