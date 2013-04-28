package org.openntf.domdisc.model;

/**
 * @author Jens
 * Class for holding just the fields that are used when submitting a new entry to Domino. 
 * A subset of what is found in DiscussionEntry
 */
public class DiscussionEntryForSubmitting {

	private String form; 
	private String body;
	private String subject; 
	private String categories;
	private String parentid;
	
	public String getForm() {
		return form;
	}
	public void setForm(String form) {
		this.form = form;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	
	public String getParentid() {
		return parentid;
	}
	public void setParentid(String parentid) {
		this.parentid = parentid;
	}
	/**
	 * @param discussionEntry
	 * Feed me a DiscussionEntry and I populate this object with the fields that i CAN hold
	 */
	public void createFromDiscussionEntry(DiscussionEntry discussionEntry) {
		setBody(discussionEntry.getBody());
		setCategories(discussionEntry.getCategories());
		setForm(discussionEntry.getForm());
		setSubject(discussionEntry.getSubject());
		setParentid(discussionEntry.getParentid());
	}
	
}
