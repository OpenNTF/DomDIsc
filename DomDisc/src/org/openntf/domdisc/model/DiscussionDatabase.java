package org.openntf.domdisc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	
	@DatabaseField
	private boolean disableComputeWithForm;
	
	public static final String LASTSUCCESFULREPLICATIONDATE_FIELD_NAME = "lastsuccesrep";  //accessible from outside to enable querying using the column name
	@DatabaseField (columnName = LASTSUCCESFULREPLICATIONDATE_FIELD_NAME)
	private Date lastSuccesfulReplicationDate;

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
		ArrayList<DiscussionEntry> itemList = new ArrayList<DiscussionEntry>();
		for (DiscussionEntry discussionEntry : discussionEntries) {
			String parentid = discussionEntry.getParentid();
			if (parentid == null || parentid.length() == 0) {
				itemList.add(discussionEntry);	
			}
		}
		return itemList;
	}
	
	/**
	 * @return a Set of categories used in the DiscussionDatabase
	 */
	public Set<String> getCategories() {
		Set<String> returnSet = new TreeSet<String>();
		
		for (DiscussionEntry discussionEntry : discussionEntries) {
			String category = discussionEntry.getCategories();
			if (category != null && category.length() > 0) {
				returnSet.add(category);	
			}
		}
		return returnSet;
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
	

	public boolean isDisableComputeWithForm() {
		return disableComputeWithForm;
	}

	public void setDisableComputeWithForm(boolean disableComputeWithForm) {
		this.disableComputeWithForm = disableComputeWithForm;
	}
	


	public Date getLastSuccesfulReplicationDate() {
		if (lastSuccesfulReplicationDate != null){
			return lastSuccesfulReplicationDate;
		} else {
			return new Date(0); //1-1-1970 is default
		}
//		return lastSuccesfulReplicationDate;
	}

	public void setLastSuccesfulReplicationDate(Date lastSuccesfulReplicationDate) {
		this.lastSuccesfulReplicationDate = lastSuccesfulReplicationDate;
	};

}
