package org.openntf.domdisc.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class AppLog {
	public static final String ID_FIELD_NAME = "id";
	
	@DatabaseField(generatedId=true, columnName = ID_FIELD_NAME)
	private int id;
	
	@DatabaseField
	private String message;
	
	@DatabaseField
	private String level;
	
	@DatabaseField
	private String logTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	
	

}
