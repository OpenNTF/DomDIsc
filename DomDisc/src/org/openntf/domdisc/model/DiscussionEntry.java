package org.openntf.domdisc.model;

import com.j256.ormlite.field.DatabaseField;

public class DiscussionEntry {

	/*
	 * Felter der er nødvendige af hensyn til ORM
	 */
	public static final String DISCUSSIONDB_FIELD_NAME = "discussiondatabase";  //accessible from outside to enable querying using the column name
	@DatabaseField(foreign=true,foreignAutoRefresh=true,columnName = DISCUSSIONDB_FIELD_NAME)
	private DiscussionDatabase discussionDatabase;
	
	/*
	 * Alle felter her er taget fra JSON output fra Domino Discussion Database
	 * Der er to felter hvor navnene er ændret for at de kan passe ind i Java - det er nævnt i kommentaren
	 * til feltet
	 */
	@DatabaseField
	private String href; //http:\/\/www.jens.bruntt.dk:80\/androiddev\/discussi.nsf\/api\/data\/documents\/unid\/BFD0FF2E75F184C3C1257A3E003BC74D"
	@DatabaseField (id=true,canBeNull=false) // <- KEY for entries
	private String unid; //BFD0FF2E75F184C3C1257A3E003BC74D"
	
	public static final String NOTEID_FIELD_NAME = "noteid";  //accessible from outside to enable querying using the column name
	@DatabaseField (columnName = NOTEID_FIELD_NAME)
	private String noteid; // 936
	
	
	public static final String PARENTID_FIELD_NAME = "parentid";  //accessible from outside to enable querying using the column name
	@DatabaseField (columnName = PARENTID_FIELD_NAME) 
	private String parentid; //50497357D971A985C1257B440026BE53
	
	@DatabaseField
	private String created; //2012-07-17T10:52:56Z"
	@DatabaseField
	private String modified; //2012-07-17T17:45:05Z
	@DatabaseField
	private String authors; //CN=Jens Bruntt\/O=bruntt
	@DatabaseField
	private String form; //MainTopic
	@DatabaseField
	private String mimeVersion; // Ændret fra MIME_Version = 1.0
	@DatabaseField
	private String from; //CN=Jens Bruntt\/O=bruntt
	@DatabaseField
	private String abbreviateFrom; //Jens Bruntt\/bruntt
	@DatabaseField
	private String altFrom; //CN=Jens Bruntt\/O=bruntt"
	@DatabaseField
	private String threadId; //SKAK-8WAES5
	@DatabaseField
	private String remote_User; //CN=Jens Bruntt\/O=bruntt"
	@DatabaseField
	private String mainID; //BFD0FF2E75F184C3C1257A3E003BC74D
	@DatabaseField
	private String abrFrom; //Jens_Bruntt__bruntt
	@DatabaseField
	private String webCategories; //Fra Android
	@DatabaseField
	private String body;
    /* Can have multiple formats. Here is one example:
     * {
    "type":"multipart",
    "content":        [
      {
        "contentType":"multipart\/alternative; Boundary=\"0__=4EBBF199DFFF23048f9e8a93df938690918c4EBBF199DFFF2304\"",
        "contentDisposition":"inline"
      },
      {
        "contentType":"text\/plain; charset=ISO-8859-1",
        "contentTransferEncoding":"quoted-printable",
        "data":"\r\nTilf=F8jet til test5 body=\r\n",
        "boundary":"--0__=4EBBF199DFFF23048f9e8a93df938690918c4EBBF199DFFF2304"
      },
      {
        "contentType":"text\/html; charset=ISO-8859-1",
        "contentDisposition":"inline",
        "contentTransferEncoding":"quoted-printable",
        "data":"<html><body><font size=3D\"2\" face=3D\"sans-serif\">Tilf=F8jet til test5 b=\r\nody<\/font><\/body><\/html>=\r\n",
        "boundary":"--0__=4EBBF199DFFF23048f9e8a93df938690918c4EBBF199DFFF2304"
      }
    ]
}
     */
	@DatabaseField
	private String newsLetterSubject; //test5
	@DatabaseField
	private String path_Info; //"\/androiddev\/discussi.nsf\/MainTopic?CreateDocument"
	@DatabaseField
	private String subject; //test5
	@DatabaseField
	private String categories; //Fra Android
	@DatabaseField
	private String abstractDoc; // ændret navn Abstract. Tilf\u00F8jet til test5 body

	/*
	 * Getters og setters
	 */
//	public int getId() {
//		return id;
//	}
//	public void setId(int id) {
//		this.id = id;
//	}
	
	public DiscussionDatabase getDiscussionDatabase() {
		return discussionDatabase;
	}
	public void setDiscussionDatabase(DiscussionDatabase discussionDatabase) {
		this.discussionDatabase = discussionDatabase;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * 
	 * @return the unique key in local storage and universalid in Domino
	 */
	public String getUnid() {
		return unid;
	}
	/**
	 * Set this to the value of universalid in Domino
	 * @param unid
	 */
	public void setUnid(String unid) {
		this.unid = unid;
	}
	public String getNoteid() {
		return noteid;
	}
	public void setNoteid(String noteid) {
		this.noteid = noteid;
	}
	public String getParentid() {
		return parentid;
	}
	public void setParentid(String parentid) {
		this.parentid = parentid;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	public String getForm() {
		return form;
	}
	public void setForm(String form) {
		this.form = form;
	}
	public String getMimeVersion() {
		return mimeVersion;
	}
	public void setMimeVersion(String mimeVersion) {
		this.mimeVersion = mimeVersion;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getAbbreviateFrom() {
		return abbreviateFrom;
	}
	public void setAbbreviateFrom(String abbreviateFrom) {
		this.abbreviateFrom = abbreviateFrom;
	}
	public String getAltFrom() {
		return altFrom;
	}
	public void setAltFrom(String altFrom) {
		this.altFrom = altFrom;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public String getRemote_User() {
		return remote_User;
	}
	public void setRemote_User(String remote_User) {
		this.remote_User = remote_User;
	}
	public String getMainID() {
		return mainID;
	}
	public void setMainID(String mainID) {
		this.mainID = mainID;
	}
	public String getAbrFrom() {
		return abrFrom;
	}
	public void setAbrFrom(String abrFrom) {
		this.abrFrom = abrFrom;
	}
	public String getWebCategories() {
		return webCategories;
	}
	public void setWebCategories(String webCategories) {
		this.webCategories = webCategories;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getNewsLetterSubject() {
		return newsLetterSubject;
	}
	public void setNewsLetterSubject(String newsLetterSubject) {
		this.newsLetterSubject = newsLetterSubject;
	}
	public String getPath_Info() {
		return path_Info;
	}
	public void setPath_Info(String path_Info) {
		this.path_Info = path_Info;
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
	public String getAbstractDoc() {
		return abstractDoc;
	}
	public void setAbstractDoc(String abstractDoc) {
		this.abstractDoc = abstractDoc;
	}
	
	

	
}
