package org.openntf.domdisc.general;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openntf.domdisc.db.DatabaseManager;
import org.openntf.domdisc.model.DiscussionDatabase;
import org.openntf.domdisc.model.DiscussionEntry;
import org.openntf.domdisc.model.DiscussionEntryForSubmitting;
import org.openntf.domdisc.tools.DateUtil;
import org.openntf.domdisc.tools.UserSessionTools;
import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
/**
 * Classe for replicating Domino Discussion databases
 */
public class DiscussionReplicator {

	Context context;
	private boolean shouldCommitToLog = false;
	private static final String loginPath = "/names.nsf?login";
//	private static final String loginRedirectTo = "/icons/ecblank.gif"; // Used when authenticating. On Succesful login, the Domino server will redirect (302) to this file
	private static final String loginRedirectTo = "/";

	public DiscussionReplicator(Context context) {
		super();
		this.context = context;
		shouldCommitToLog = getLogALot(context);
	}

	/**
	 * Activate to replicate one Discussion database
	 */
	/**
	 * @param discussionDatabase to replicate
	 * @return number of entries added to the database. -1 if an error occurred
	 */
	public synchronized int replicateDiscussionDatabase(DiscussionDatabase discussionDatabase) {
		String authenticationCookie = "";
		
		if (discussionDatabase == null) {
			ApplicationLog.w(getClass().getSimpleName() + " Replication server->database failed because the discussionDatabase object was null");
			return -1;
		}
			
		DatabaseManager.init(context);
		ApplicationLog.i("Replicate " + discussionDatabase.getName());
		int additionCount = 0;

		if (UserSessionTools.haveInternet(context) == false) {
			ApplicationLog.i("Internet connection not available - Replication not possible");
		} else {
			ApplicationLog.d("Internet connection is available - Will replicate", shouldCommitToLog);

			String hostName = discussionDatabase.getHostName();
			String dbPath = discussionDatabase.getDbPath();
			String httpPort = discussionDatabase.getHttpPort();
			String password = discussionDatabase.getPassword();
			String userName = discussionDatabase.getUserName();
			Date lastSuccesfulReplication = discussionDatabase.getLastSuccesfulReplicationDate();
			
			DiscussionDatabaseTools discussionDatabaseTools = new DiscussionDatabaseTools(context, discussionDatabase);
			if (!discussionDatabaseTools.hasBasicReplicationRequiredFields()) {
				ApplicationLog.w(getClass().getSimpleName() + " Replication server->database failed because the discussionDatabase configuration is not filled in. One or more fields needs proper content.");
				return -1;
			} ;
			
						
//			String httpType = "";
//			if (discussionDatabase.isUseSSL()) {
//				httpType = "https";
//			} else {
//				httpType = "http";
//			}
			
			boolean disableComputeWithForm = discussionDatabase.isDisableComputeWithForm();
			
			// https://dims.dk:443/database.nsf/api/data/documents
			//String urlForDocuments = httpType + "://" + hostName;
			String urlForDocuments = getUrlRootForHost(discussionDatabase) + dbPath + "/api/data/documents/"; 

			
//			if (httpPort.contentEquals("80") || httpPort.contentEquals("") || httpPort.contentEquals(" ")) {
//				urlForDocuments = urlForDocuments + dbPath
//						+ "/api/data/documents/";
//			} else {
//				urlForDocuments = urlForDocuments + ":" + httpPort + dbPath
//						+ "/api/data/documents/";
//			}

			
			ApplicationLog.i("Starting replication for " + discussionDatabase.getName());
			ApplicationLog.i("Last succesful replication for " + discussionDatabase.getName() + " was : " + DateUtil.getDateLong(lastSuccesfulReplication));
			
			// getAuthenticationToken
			ApplicationLog.d("Activating getAuthenticationToken now", shouldCommitToLog);
			authenticationCookie = getAuthenticationToken(hostName, httpPort,
					userName, password, discussionDatabase.isUseSSL());

			if (authenticationCookie.equals("")) {
				ApplicationLog
				.w("Unable to start replication as Authentication with the server was not established. Stopping.");
				additionCount= -1;
			} else {
				replicateLocalDatabaseToServer(discussionDatabase, hostName,urlForDocuments, authenticationCookie, disableComputeWithForm);
				// Any entries submitted will be deleted when the replicateServerToLocalDatabase runs (because the locally created entries' unids are not found in the downloaded entries)
				additionCount = replicateServerToLocalDatabase(discussionDatabase, hostName,urlForDocuments, authenticationCookie);
				if ((additionCount) > -1) {
					ApplicationLog.d(getClass().getSimpleName() + " Replication OK", shouldCommitToLog);
					ApplicationLog.d(getClass().getSimpleName() + " Number of entries added: " + additionCount, shouldCommitToLog);
					
					Date nowDate = new Date();
					discussionDatabase.setLastSuccesfulReplicationDate(nowDate);
					DatabaseManager.getInstance().updateDiscussionDatabase(discussionDatabase); //persist the change
				} else {
					ApplicationLog.w(getClass().getSimpleName() + " Replication server->database failed for " + discussionDatabase.getName());
				}
				ApplicationLog.i(getClass().getSimpleName() + " - - - -");
			}
		}
		return additionCount;
	}

	/**
	 * Handles replication FROM the local database TO the server
	 * @param discussionDatabase
	 * @param hostName
	 * @param urlForDocuments
	 * @param authenticationCookie
	 * @return True if replication went as expected
	 */
	private boolean replicateLocalDatabaseToServer(DiscussionDatabase discussionDatabase, String hostName,
			String urlForDocuments, String authenticationCookie, boolean disableComputeWithForm) {

		ApplicationLog.d(getClass().getSimpleName() + " start", shouldCommitToLog);

		// Bruge en metode der kan oprette en body

		int succesfulUploadCount = 0;
		int failedUploadCount = 0;

		ArrayList<DiscussionEntry> discussionEntriesForSubmitting = (ArrayList<DiscussionEntry>) DatabaseManager.getInstance().getDiscussionEntriesForSubmit(discussionDatabase);
		if (discussionEntriesForSubmitting == null ) {
			ApplicationLog.d(getClass().getSimpleName() + " Found no entries for submitting to the server", shouldCommitToLog);			
		} else {
			ApplicationLog.d(getClass().getSimpleName() + " Found " + discussionEntriesForSubmitting.size() + " entries for submitting to the server", shouldCommitToLog);
			Iterator<DiscussionEntry> discussionEntriesForSubmittingIterator = discussionEntriesForSubmitting.iterator();
			while (discussionEntriesForSubmittingIterator.hasNext()) {
				DiscussionEntry currentEntry = discussionEntriesForSubmittingIterator.next();
				ApplicationLog.d(getClass().getSimpleName() + " Will submit " + currentEntry.getSubject(), shouldCommitToLog);
				String submittedLocation = submitDiscussionEntry(currentEntry, urlForDocuments, authenticationCookie, disableComputeWithForm);
				if (submittedLocation.length()> 0) {
					succesfulUploadCount++;	
					ApplicationLog.d(getClass().getSimpleName() + " successfully submitted to " + submittedLocation, shouldCommitToLog);
				} else {
					failedUploadCount++;
					ApplicationLog.d(getClass().getSimpleName() + " submit failed", shouldCommitToLog);
				}
			}

			ApplicationLog.d(getClass().getSimpleName() + " total succesful submits to " + discussionDatabase.getName() + ": " + succesfulUploadCount , shouldCommitToLog);
			ApplicationLog.d(getClass().getSimpleName() + " total failed submits to " + discussionDatabase.getName() + ": " + failedUploadCount , shouldCommitToLog);

		}

		return (succesfulUploadCount > 0);

	}

	/**
	 * @param discussionEntry
	 * @param urlForDocuments
	 * @param authenticationCookie
	 * @return http location of the newly created DiscussionEntry. Empty String if unsuccesful.  
	 */
	private String submitDiscussionEntry(DiscussionEntry discussionEntry, String urlForDocuments,	String authenticationCookie, boolean disableComputeWithForm) {
		String returnString = "";

		//Using a simple class to hold what gets submitted - keeps things simpler
		DiscussionEntryForSubmitting entryToSubmit = new DiscussionEntryForSubmitting();
		entryToSubmit.createFromDiscussionEntry(discussionEntry);

		String formForSubmit = "";
		String parentid = entryToSubmit.getParentid();
		if (parentid != null && parentid.length() > 0) {
			formForSubmit = "Response";
		} else {
			formForSubmit = "MainTopic";
		}

		String url = urlForDocuments + "?form=" + formForSubmit + "&computewithform=";
		//String url = urlForDocuments + "?form=" + formForSubmit + "&computewithform=true";
		
		//Having computewithform=true will let the application logic compute extra fields on the submitted documents
		//Disabling computewithform may make it possible to get the app working with discussion databases that have more logic than the standard 
		// Discussion template
		if (disableComputeWithForm) {
			url = url + "false";
		} else {
			url = url + "true";
		}
		

		if (parentid != null && parentid.length() > 0) {
			url = url + "&parentid=" + parentid;
		}


		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptableMediaTypes);
		requestHeaders.add("Cookie", authenticationCookie);

		HttpEntity<DiscussionEntryForSubmitting> requestEntity = new HttpEntity<DiscussionEntryForSubmitting>(entryToSubmit,requestHeaders);

		//		ApplicationLog.d(getClass().getSimpleName() + " Adding MappingJackson2HttpMessageConverter AND StringHttpMessageConverter to restTemplate", shouldCommitToLog);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		ApplicationLog.d(getClass().getSimpleName() + " POST to " + url, shouldCommitToLog);
		ResponseEntity<String> httpResponse;
		try {
			httpResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			ApplicationLog.d(getClass().getSimpleName() + " resultString: " + httpResponse.toString(), shouldCommitToLog);
			ApplicationLog.d(getClass().getSimpleName() + " statuscode: " + httpResponse.getStatusCode().value(), shouldCommitToLog);
			if (httpResponse.hasBody()) {
				ApplicationLog.d(getClass().getSimpleName() + " body: " + httpResponse.getBody().toString(), shouldCommitToLog);
			} else {
				ApplicationLog.d(getClass().getSimpleName() + " body: None received" , shouldCommitToLog);
			}

			HttpHeaders responseHeaders = httpResponse.getHeaders();
			if (responseHeaders.isEmpty()) {
				ApplicationLog.d("No response headers - then the POST did not succeeed", shouldCommitToLog);
			} else {
				List<String> locationList = responseHeaders.get("Location");
				if (null != locationList) {
					String location = locationList.get(0);  //Assuming the header we are looking for will always be first - unlikely to have two
					ApplicationLog.d("Location of newly created Note: " + location, shouldCommitToLog);
					returnString = location;
				}
			}
		} catch (RestClientException e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			ApplicationLog.e("Exception: " + errorMessage);

		}
		return returnString;
	}

	/**
	 * Handles replication FROM the server TO the database
	 * @param discussionDatabase
	 * @param hostName
	 * @param urlForDocuments
	 * @param authenticationCookie
	 * @return Number of additions performed to local database. -1 if an error happened
	 */
	private int replicateServerToLocalDatabase(
			DiscussionDatabase discussionDatabase, String hostName,
			String urlForDocuments, String authenticationCookie) {
		// example String url =
		// "http://www.jens.bruntt.dk/androiddev/discussi.nsf/api/data/documents/";
		urlForDocuments = urlForDocuments + "?compact=true";
		int additionCount = 0;
		boolean replicationOK = false;
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());

		// Add the gzip Accept-Encoding header
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);

		ApplicationLog.d("Setting Authentication token in request header: " + authenticationCookie,
				shouldCommitToLog);
		requestHeaders.add("Cookie", authenticationCookie);

		requestHeaders.add("Referer", urlForDocuments);
		requestHeaders.setCacheControl("max-age=0");
		requestHeaders.set("Connection", "Close"); //to remove  EOFException

		HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

		ApplicationLog.d("HTTP connection now", shouldCommitToLog);

		String jsonString;
		try {
			ApplicationLog.d("Accesing " + urlForDocuments, shouldCommitToLog);

			// Make the HTTP GET request, marshaling the response to a
			// String
			ResponseEntity<String> response = restTemplate.exchange(
					urlForDocuments, HttpMethod.GET, requestEntity,
					String.class);

			jsonString = response.getBody();

			ApplicationLog.d(
					"String received length: " + jsonString.length(),
					shouldCommitToLog);
			
			ApplicationLog.d(
					"Content encoding: " + response.getHeaders().getContentEncoding().toString(), shouldCommitToLog);

			if (!isThisALoginForm(jsonString)) {
				try {
					JSONArray jsonArray = new JSONArray(jsonString);

					ApplicationLog.d(
							"Number of entries downloaded: " + jsonArray.length(), shouldCommitToLog);

					if (jsonArray.length() > 0) {
						/**
						 * Because no entries were downloaded we will not enter here. This has the effect that if the server database
						 * actually is empty, this will not reflect on the local database, because deletions are handled in handleJsonDiscussionEntries.
						 * We assume that if we download 0 entries, something is wrong with the downloaded content, and we do not dare let
						 * the code do deletions locally
						 */
						additionCount = handleJsonDiscussionEntries(jsonArray, discussionDatabase, authenticationCookie); 
					} else {
						ApplicationLog.i("No entries retrieved. Will not do any local deletions");
					}
					replicationOK = true;

				} catch (Exception e) {
					replicationOK = false;
					ApplicationLog.e(getClass().getSimpleName() + " Exception" + e.getMessage());
				}

			} else {
				ApplicationLog.e("There is a login issue when accessing " + urlForDocuments);
				ApplicationLog.e("The server at " + hostName + " prompts for login");
				replicationOK = false;
			}

		} catch (RestClientException e1) {
			replicationOK = false;
			String errorMessage = e1.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			ApplicationLog.e(getClass().getSimpleName() + " Exception: " + errorMessage);

			if (errorMessage.contains("403")) {
				ApplicationLog.i("403 - Looks like the Domino Data Service is not enabled for the database " + discussionDatabase.getDbPath());
			}

			else if (errorMessage.contains("404")) {
				String myErrorMessage = "404 - Looks like the Domino Database-path is wrong - typing error in the Configuration? ";
				ApplicationLog.i(myErrorMessage + " " + discussionDatabase.getDbPath());
			}

			else {
				String localizedErrorMessage = e1.getLocalizedMessage();
				if (localizedErrorMessage != null) {
					ApplicationLog.e("localizedErrorMessage: " + localizedErrorMessage);
				} else {
					ApplicationLog
					.e("Unable to get data from the database " + discussionDatabase.getDbPath());
				}
			}
			e1.printStackTrace();

		} catch (Exception e2) {
			replicationOK = false;
			String message = e2.getMessage();
			if (message == null) {
				message = "Exception with no message";
			}
			ApplicationLog.e(getClass().getSimpleName() + " " + message);
			e2.printStackTrace();
		}
		if (!replicationOK) {
			additionCount = -1;
		} 
		return additionCount;
	}

	/**
	 * If String contains FORM tag and HTML tag - this is a Domino login form
	 * 
	 * @param checkString
	 * @return true if this is a login form
	 */
	private boolean isThisALoginForm(String checkString) {

		boolean returnValue = false;
		if (checkString.contains("<form") || checkString.contains("<FORM")) {
			if (checkString.contains("html") || checkString.contains("HTML")) {
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Feed in a jsonArray from the /api/data/documents/ output and a
	 * DiscussionDatabase this method will make sure that the database is
	 * updated
	 * 
	 * @param discussionArray
	 * @param discussionDatabase
	 * 
	 * @return number of additions to the local database. -1 if an error condition was met
	 */
	private int handleJsonDiscussionEntries(JSONArray discussionArray, DiscussionDatabase discussionDatabase, String authenticationCookie) {

		//		ArrayList<DiscussionEntry> serverDiscussionEntryList = new ArrayList<DiscussionEntry>();
		HashMap<String, DiscussionEntry> serverDiscussionEntryMap = new HashMap<String, DiscussionEntry>();
		
		int updatesCount = 0;

		for (int i = 0; i < discussionArray.length(); i++) {
			JSONObject jsonObject;
			try {
				jsonObject = discussionArray.getJSONObject(i);
				try {

					String modified = jsonObject.getString("@modified");
					String unid = jsonObject.getString("@unid");
					String href = jsonObject.getString("@href");

					ApplicationLog.d("entry with unid: " + unid, shouldCommitToLog);

					DiscussionEntry discussionEntry = new DiscussionEntry();
					discussionEntry.setHref(href);
					discussionEntry.setUnid(unid);
					discussionEntry.setModified(modified);
					//					serverDiscussionEntryList.add(discussionEntry);  // Should be removed 
					serverDiscussionEntryMap.put(unid, discussionEntry);
				} catch (JSONException e) {
					ApplicationLog.e(getClass().getSimpleName() + " Error while accessing JSON object values");
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} 


		if((serverDiscussionEntryMap == null) || (serverDiscussionEntryMap.isEmpty())) {
			ApplicationLog.i("The JSON retrievede did not contain any documents");
		} else {
			/*
			 * Check all retrieved DiscussionDatabaseEntries - do they exist in
			 * database if no: retrieve content and add if yes: check if modified is
			 * changed if no: next if yes: retrieve newer content and update
			 */
			ApplicationLog.d("Checking all downloaded entries: are they already stored locally?", shouldCommitToLog);
			Set<Entry<String, DiscussionEntry>> set = serverDiscussionEntryMap.entrySet();
			Iterator<Entry<String, DiscussionEntry>> serverDiscussionEntryIterator = set.iterator();
			while (serverDiscussionEntryIterator.hasNext()) {
				Map.Entry<String, DiscussionEntry> currentMapEntry = (Map.Entry<String, DiscussionEntry>)serverDiscussionEntryIterator.next();
				DiscussionEntry currentEntry = currentMapEntry.getValue();

				String unid = currentEntry.getUnid();
				// Check if the entry is already in the database
				ApplicationLog.d("Lookup for unid: " + unid, shouldCommitToLog);
				DiscussionEntry dbEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(unid);

				if (dbEntry == null) {

					ApplicationLog.d("This entry has not been stored before - retrieving full entry ", shouldCommitToLog);
					currentEntry.setDiscussionDatabase(discussionDatabase);
					DiscussionEntry fullDiscussionEntry = getFullEntryFromServer(currentEntry, authenticationCookie);
					String entryForm = fullDiscussionEntry.getForm();
					if (isAcceptableFormType(entryForm)) {
						DatabaseManager.getInstance().createDiscussionEntry(fullDiscussionEntry);
						ApplicationLog.d("This entry has been stored with values: " + fullDiscussionEntry.getSubject(), shouldCommitToLog);
						updateParentThreadDates(fullDiscussionEntry);
						++updatesCount;
					} else {
						ApplicationLog.d("This entry has not been stored before, but the Form Type is not one of the accepted types. Will not store", shouldCommitToLog);
					}
				} else {
					ApplicationLog.d("This entry is already in the database: " + dbEntry.getSubject(), shouldCommitToLog);
					ApplicationLog.d("Checking if modified dates are the same", shouldCommitToLog);

					String currentEntryModified = currentEntry.getModified();
					String dbEntryModified = dbEntry.getModified();
					if (currentEntryModified.contentEquals(dbEntryModified)) {
						ApplicationLog.d("Modified date is unchanged", shouldCommitToLog);
					} else {
						ApplicationLog.d(
								"Modified date is changed. Updating dbEntry", shouldCommitToLog);
						DiscussionEntry fullDiscussionEntry = getFullEntryFromServer(currentEntry, authenticationCookie);
						fullDiscussionEntry.setDiscussionDatabase(discussionDatabase);
						dbEntry = fullDiscussionEntry;
						DatabaseManager.getInstance().updateDiscussionEntry(dbEntry);
						updateParentThreadDates(dbEntry);
						++updatesCount;
					}
				}
			}

			/**
			 * Checking all locally stored entries - are they in the download
			 * if not - delete the local entry
			 */
			ApplicationLog.d("Checking all database entries: should they be deleted?", shouldCommitToLog);
			ArrayList<DiscussionEntry> localDiscussionEntryList = new ArrayList<DiscussionEntry>();
			try {
				localDiscussionEntryList = (ArrayList<DiscussionEntry>) discussionDatabase.getDiscussionEntries();
			} catch (Exception e) {
				//This throws exception if the database has just done its first server-to-local replication
				ApplicationLog.w("Unable to retrieve local discussion entries. This is OK if it is the first replication of this database: " + discussionDatabase.getName());
			}
			
			if (localDiscussionEntryList.size()>0) {
				//Example on using Map http://www.java-tips.org/java-se-tips/java.util/how-to-use-of-hashmap.html
				//
				Iterator<DiscussionEntry> localDiscussionEntryListIterator = localDiscussionEntryList.iterator();
				while (localDiscussionEntryListIterator.hasNext()) {
					DiscussionEntry currentEntry = localDiscussionEntryListIterator.next();
					String unid = currentEntry.getUnid();
					// Check if the was downloaded 
					ApplicationLog.d("Lookup for unid: " + unid, shouldCommitToLog);
					DiscussionEntry serverEntry = serverDiscussionEntryMap.get(unid);
					if (serverEntry != null) {
						ApplicationLog.d("Found the entry " + currentEntry.getSubject() + " in the downloaded list", shouldCommitToLog);
					} else {
						ApplicationLog.d("Did not find the entry " + currentEntry.getSubject() + " in the downloaded list. Deleting in local DB", shouldCommitToLog);
						DatabaseManager.getInstance().deleteDiscussionEntry(currentEntry);
					}

				}
			}
		}
		/**
		 * Done checking if new or modified entries were downloaded
		 */
		return updatesCount;		

	}

	//	}




	/**
	 * @param entryForm
	 * @return true if one of the Form types that we want to allow you to store has been used
	 */
	private boolean isAcceptableFormType(String entryForm) {

		if (entryForm == null) {
			return false;
		}

		if (entryForm.equalsIgnoreCase("maintopic")) {
			return true;
		}

		if (entryForm.equalsIgnoreCase("response")) {
			return true;
		}

		if (entryForm.equalsIgnoreCase("responsetoresponse")) {
			return true;
		}

		return false;
	}

	/**
	 * Retrieve a full entry from the server
	 * 
	 * @param discussionEntry
	 * @param AutheticationCookie
	 * @return discussionEntry
	 */
	private DiscussionEntry getFullEntryFromServer(
			DiscussionEntry discussionEntry, String authenticationCookie) {

		//		if (UserSessionTools.haveInternet(context)) {
		//			ApplicationLog
		//			.i("Internet connection is available - will replicate");

		DiscussionDatabase discussionDatabase = discussionEntry
				.getDiscussionDatabase();

		String urlForDocuments = discussionEntry.getHref();
		
		//prefixing http and host etc to get a proper URL 
		if (urlForDocuments.startsWith("/")){
			urlForDocuments = getUrlRootForHost(discussionDatabase) + urlForDocuments;
		}
		urlForDocuments = urlForDocuments + "?compact=true";

		ApplicationLog.d("Accesing " + urlForDocuments, shouldCommitToLog);

		ApplicationLog.d("Starting", shouldCommitToLog);

		// Add the gzip Accept-Encoding header
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
		requestHeaders.set("Connection", "Close"); //to remove  EOFException

		if (authenticationCookie != "") {

			ApplicationLog.d("Setting authentication token in request header",
					shouldCommitToLog);

			requestHeaders.add("Cookie", authenticationCookie);
		}

		HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());

		String jsonString;
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					urlForDocuments, HttpMethod.GET, requestEntity,
					String.class);
			jsonString = response.getBody();

			ApplicationLog.d("String received length: " + jsonString.length(),shouldCommitToLog);

			if (!isThisALoginForm(jsonString)) {
				try {
					JSONObject jsonDocument = new JSONObject(jsonString);
					if (jsonDocument.length() > 0) {
						discussionEntry = enrichDiscussionEntryFromJson(
								discussionEntry, jsonDocument);
					} else {
						ApplicationLog
						.i("No Document retrieved. Nothing to do");
					}
				} catch (Exception e) {
					ApplicationLog.e(getClass().getSimpleName() + " Exception: " + e.getMessage());
				}

			} else {
				ApplicationLog.e("There is a login issue when accessing "
						+ urlForDocuments);
				ApplicationLog.e("The server prompts for login");
			}

		} catch (RestClientException e1) {
			String errorMessage = e1.getMessage();
			ApplicationLog.e("Exception: " + errorMessage);
			if (errorMessage.contains("403")) {
				ApplicationLog
				.i("403 - Looks like the Domino Data Service is not enabled for the database "
						+ discussionDatabase.getDbPath());
			} else {
				String localizedErrorMessage = e1.getLocalizedMessage();
				if (localizedErrorMessage != null) {
					ApplicationLog.e("localizedErrorMessage: "
							+ localizedErrorMessage);
				} else {
					ApplicationLog
					.e("Unable to get data from the database "
							+ discussionDatabase.getDbPath());
				}
			}
			e1.printStackTrace();
		} catch(OutOfMemoryError e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Unknown error message";
			}
			ApplicationLog.e("Out of Memory Error: " + errorMessage + ". Possibly we ran into a very large document");
			e.printStackTrace();			
		}
		catch (Exception e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Unknown error message";
			}
			ApplicationLog.e("Exception: " + errorMessage);
			e.printStackTrace();
		}

		return discussionEntry;

	}

	private DiscussionEntry enrichDiscussionEntryFromJson(
			DiscussionEntry discussionEntry, JSONObject jsonDocument) {

		discussionEntry.setAbbreviateFrom(getDominoValueFromJson(jsonDocument,
				"AbbreviateFrom"));
		discussionEntry.setAbrFrom(getDominoValueFromJson(jsonDocument,
				"AbrFrom"));
		discussionEntry.setAbstractDoc(getDominoValueFromJson(jsonDocument,
				"AbstractDoc"));
		discussionEntry.setAltFrom(getDominoValueFromJson(jsonDocument,
				"AltFrom"));
		discussionEntry.setBody(getDominoValueFromJson(jsonDocument, "Body"));
		discussionEntry.setCategories(getDominoValueFromJson(jsonDocument,
				"Categories"));
		discussionEntry.setFrom(getDominoValueFromJson(jsonDocument, "From"));
		discussionEntry
		.setMainID(getDominoValueFromJson(jsonDocument, "MainID"));
		discussionEntry.setMimeVersion(getDominoValueFromJson(jsonDocument,
				"MimeVersion"));
		discussionEntry.setNewsLetterSubject(getDominoValueFromJson(
				jsonDocument, "NewsLetterSubject"));
		discussionEntry.setPath_Info(getDominoValueFromJson(jsonDocument,
				"Path_Info"));
		discussionEntry.setRemote_User(getDominoValueFromJson(jsonDocument,
				"Remote_User"));
		discussionEntry.setSubject(getDominoValueFromJson(jsonDocument,
				"Subject"));
		discussionEntry.setThreadId(getDominoValueFromJson(jsonDocument,
				"ThreadId"));
		discussionEntry.setWebCategories(getDominoValueFromJson(jsonDocument,
				"WebCategories"));
		// System fields below
		discussionEntry.setModified(getDominoValueFromJson(jsonDocument,
				"@modified"));
		discussionEntry.setHref(getDominoValueFromJson(jsonDocument, "@href"));
		discussionEntry.setUnid(getDominoValueFromJson(jsonDocument, "@unid"));
		discussionEntry.setCreated(getDominoValueFromJson(jsonDocument,
				"@created"));
		discussionEntry.setForm(getDominoValueFromJson(jsonDocument, "@form"));
		discussionEntry.setNoteid(getDominoValueFromJson(jsonDocument,
				"@noteid"));
		discussionEntry.setAuthors(getDominoValueFromJson(jsonDocument,
				"@authors"));
		discussionEntry.setParentid(getDominoValueFromJson(jsonDocument, "@parentid"));

		return discussionEntry;
	}

	private String getDominoValueFromJson(JSONObject jsonDocument,	String fieldName) {
		String returnValue = "";

		if (fieldName.contains("Body")) {
			// If the Body field is not just plain text we will go for the more complicated extraction
			ApplicationLog.d(getClass().getSimpleName() + " getDominoValueFromJson handling Body", shouldCommitToLog);
			returnValue = getBodyFieldValue(jsonDocument, fieldName, returnValue);


		} else { // All other fields than Body
			try {
				returnValue = jsonDocument.getString(fieldName);
			} catch (JSONException e) {
				//				ApplicationLog.d("Unable to find field " + fieldName, shouldCommitToLog);
				returnValue = "";
			}

		}

		return returnValue;
	}

	private String getBodyFieldValue(JSONObject jsonDocument, String fieldName,	String returnValue) {
		ApplicationLog.d(getClass().getSimpleName() + " getBodyFiledValue start", shouldCommitToLog);
		try {
			JSONObject bodyObjectArray = jsonDocument.getJSONObject(fieldName); // If Body content is simple we will handle this in the JSONException
			JSONArray bodyArray = bodyObjectArray.getJSONArray("content");
			for (int i = 0; i < bodyArray.length(); i++) {
				JSONObject bodyItem = bodyArray.getJSONObject(i);
				if (bodyItem.has("contentType")) {
					String thisContentType = bodyItem
							.getString("contentType");
					if (thisContentType.contains("text/html")) {

						ApplicationLog.d("Found body item with html - adding", shouldCommitToLog);
						String bodyHtml = bodyItem.getString("data");

						// Finding the charset - START
						int charSetPos = thisContentType.indexOf("charset="); // Returns the
						// position of the c in the string "charset=" 8 characters long

						int contentTypeLength = thisContentType.length();

						String charsetValue = thisContentType.substring(
								charSetPos + 8, contentTypeLength);

						ApplicationLog.d("charsetValue: " + charsetValue, shouldCommitToLog);
						// Finding the charset - END

						// Checking for quoted-printable content - START
						//contentTransferEncoding
						String bodyHtmlDecoded = ""; 
						if (bodyItem.has("contentTransferEncoding")) {
							String contentTransferEncoding = bodyItem.getString("contentTransferEncoding");
							ApplicationLog.d("contentTransferEncoding is specified as " + contentTransferEncoding + " will do decoding", shouldCommitToLog);

							if (contentTransferEncoding.contains("quoted-printable")) {
								QuotedPrintableCodec dims = new QuotedPrintableCodec(); 

								// Stripping newline characters as they will make QuotedPrintableCodec throw an Exception
								String newstr = bodyHtml.replaceAll("=\r\n", "");

								bodyHtml = newstr;
								//							ApplicationLog.d("decoding: " + bodyHtml, shouldCommitToLog);

								try {
									bodyHtmlDecoded = dims.decode(bodyHtml, charsetValue);
								} catch (DecoderException e) {
									//								e.printStackTrace();
									ApplicationLog.d("Exception: " + e.getMessage(), shouldCommitToLog);
								} catch (UnsupportedEncodingException e) {
									//								e.printStackTrace();
									ApplicationLog.d("Exception: " + e.getMessage(), shouldCommitToLog);
								}
	
							} else if(contentTransferEncoding.contains("base64")) {
								byte[] byteArray = Base64.decodeBase64(bodyHtml.getBytes());
								bodyHtmlDecoded = new String(byteArray);
								
							}
							
						}

						// Checking for quoted-printable content - END

						if (bodyHtmlDecoded.equals("")) {
							returnValue = bodyHtml;
						} else {
							returnValue = bodyHtmlDecoded;
						}
					}
				}
			}
		}

		catch (JSONException e) {
			// Most likely we're here because the Body item is not an array but a simple field instead
			ApplicationLog.d(fieldName + " is not an array - looking for simple field ", shouldCommitToLog);
			try {
				String contentValue =  jsonDocument.getString(fieldName);
				String htmlValue = contentValue.replaceAll("\n", "<br>");
				returnValue = htmlValue;
			} catch (JSONException e1) {
				// If we're here we give up trying
				ApplicationLog.d("Exception: " + e.getMessage(), shouldCommitToLog);
				ApplicationLog.d("Unable to find field " + fieldName,	shouldCommitToLog);
				returnValue = "";
			}
		}
		return returnValue;
	}

	private static boolean getLogALot(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		return prefs.getBoolean("checkbox_preference_logalot", false);
	}

	/**
	 * @param hostName
	 * @param httpPort
	 * @param userName
	 * @param password
	 * @param useSSL
	 * @return a Domino authentication token (or "" if not possible to authenticate) 
	 * examples: DominoAuthSessID=xyz  OR  LtpaToken=abc 
	 */
	private String getAuthenticationToken(String hostName, String httpPort,
			String userName, String password, boolean useSSL) {

		String authenticationCookie = "";

		String httpType = "";
		if (useSSL) {
			httpType = "https";
		} else {
			httpType = "http";
		}
		String urlForLogin = httpType + "://" + hostName + ":" + httpPort
				+ loginPath;

		ApplicationLog.d("URL for login: " + urlForLogin, shouldCommitToLog);

		RestTemplate template = new RestTemplate();
		template.getMessageConverters().add(new FormHttpMessageConverter());
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
		requestHeaders.set("Connection", "Close"); //to remove  EOFException
		ApplicationLog.d("Getting LtpaToken and SessionID", shouldCommitToLog);

		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("username", userName);
		requestBody.add("password", password);
		requestBody.add("redirectto", loginRedirectTo);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
				requestBody, requestHeaders);
		ApplicationLog.d("HTTP connection now", shouldCommitToLog);
		ResponseEntity<?> httpResponse;
		try {

			httpResponse = template.exchange(urlForLogin, HttpMethod.POST, request, null);
			HttpHeaders responseHeaders = httpResponse.getHeaders();

			if (responseHeaders.isEmpty()) {
				ApplicationLog.d("No response headers", shouldCommitToLog);
			} else {

				//Working out if we already have a Cookie or if we have a Set-cookie to work with - START
				List<String> setCookieList = responseHeaders.get("Set-Cookie");
				//Working out if we already have a Cookie or if we have a Set-cookie to work with - END				
				if (null != setCookieList) {
					String cookie = setCookieList.get(0);  //Assuming the cookie we are looking for will always be first
					ApplicationLog.d("Cookie: " + cookie, shouldCommitToLog);

					int indexOfEndPos = 0; //Position of last character in the coookie string
					if (cookie.contains(";")) {
						indexOfEndPos = cookie.indexOf(";");
					} else {
						indexOfEndPos = cookie.length();
					}

					if (cookie.startsWith("LtpaToken=")) {
						ApplicationLog.d("Cookie is an LtpaToken", shouldCommitToLog);
					} else if (cookie.startsWith("DomAuthSessID=")) {
						ApplicationLog.d("Cookie is a DomAuthSessID", shouldCommitToLog);
					}

					String actualToken = (String) cookie.subSequence(0,indexOfEndPos);
					if (actualToken != null) {
						ApplicationLog.d("Token value= " + actualToken, shouldCommitToLog);
						authenticationCookie = actualToken;
					} else {
						ApplicationLog.d("Did not get the Authetication token value", shouldCommitToLog);
					}

				} else {
					ApplicationLog.d("No Authentication Cookie available", shouldCommitToLog);
				}
			}

		} catch (RestClientException e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			ApplicationLog.e("RestClientException: " + errorMessage);
			e.printStackTrace();
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "Error message not available";
			}
			//			Log.e(getClass().getSimpleName(), "getmessage: " + errorMessage);
			ApplicationLog.e("Exception: " + errorMessage);
			e.printStackTrace();
		}

		if (authenticationCookie.contentEquals("")) {
			ApplicationLog.i("Did not get an authentication cookie");
		}

		return authenticationCookie;
	}

	private void updateParentThreadDates(DiscussionEntry discussionEntry) {
		ApplicationLog.d(getClass().getSimpleName() + " updateParentThreadDates for " + discussionEntry.getSubject(), shouldCommitToLog);
		String parentId =discussionEntry.getParentid();
		if (parentId != null && parentId.length()>0) {
			DiscussionEntry parentEntry = DatabaseManager.getInstance().getDiscussionEntryWithId(parentId);
			if (parentEntry != null) {
				String parentThreadLastModifiedDate = parentEntry.getThreadLastModifiedDate();
				String entryThreadLastModifiedDate = discussionEntry.getThreadLastModifiedDate();
				ApplicationLog.d("updateParentThreadDates", shouldCommitToLog);
				ApplicationLog.d("parentThreadLastModifiedDate: " + parentThreadLastModifiedDate, shouldCommitToLog);
				ApplicationLog.d("entryThreadLastModifiedDate: " + entryThreadLastModifiedDate, shouldCommitToLog);
				int compareValue = entryThreadLastModifiedDate.compareTo(parentThreadLastModifiedDate);
				ApplicationLog.d("compareValue: " + compareValue, shouldCommitToLog);
				if (compareValue>0) {
					ApplicationLog.d("Will update parent Thread last mod to: " + entryThreadLastModifiedDate, shouldCommitToLog);
					parentEntry.setThreadLastModifiedDate(entryThreadLastModifiedDate);
					DatabaseManager.getInstance().updateDiscussionEntry(parentEntry);
					updateParentThreadDates(parentEntry);
				}
			} else {
				ApplicationLog.d("Parent not found in local db", shouldCommitToLog);
			}
		} else {
			ApplicationLog.d("Does not have a parent", shouldCommitToLog);
		}
	};

	private String getUrlRootForHost(DiscussionDatabase discussionDatabase ){
		String hostName = discussionDatabase.getHostName();
		String httpPort = discussionDatabase.getHttpPort();
		String httpType = "";
		if (discussionDatabase.isUseSSL()) {
			httpType = "https";
		} else {
			httpType = "http";
		}
		
		// https://dims.dk:443/database.nsf/api/data/documents
		String urlForHost = httpType + "://" + hostName;

		if (httpPort.contentEquals("80") || httpPort.contentEquals("") || httpPort.contentEquals(" ")) {
			//nothing to change
		} else {
			urlForHost = urlForHost + ":" + httpPort;
		}
		
		
		return urlForHost;
		
	}
	
	

}