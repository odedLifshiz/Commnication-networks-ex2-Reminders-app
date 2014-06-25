import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HttpRequest implements Runnable {

	private static final String r_CRLF = "\r\n";
	private static final String r_okString = "200 OK";
	private static final String r_movedPeramanently = "301 Moved Permanently";
	private static final String r_badRequestString = "400 Bad Request";
	private static final String r_notFoundString = "404 Not Found";
	private static final String r_internalServerError = "500 Internal Server Error";
	private static final String r_notImplementedString = "501 Not Implemented";
	private static final String r_defaultVersion = "1.0";
	private static final String r_contentTypeHttp = "message/http";
	private static final String r_contentTypeHtml = "text/html";
	private static final String r_contentTypeJPG = "image/jpeg";
	private static final String r_contentTypeBMP = "image/bmp";
	private static final String r_contentTypeGIF = "image/gif";
	private static final String r_contentTypePNG = "image/png";
	private static final String r_contentTypeIcon = "image/x-icon";
	private static final String r_contentTypeApplicationOctetStream = "application/octet-stream";
	private static final String r_pathToDefaultPage = WebServer.s_root
			+ WebServer.s_defaultPage;
	private boolean m_responseNoPage;
	private HashMap<String, String> m_requestGetPostParamsHashMap,
			m_requestHeadersHashMap, m_responseHeadersHasMap;
	private Socket m_socket;
	private DataOutputStream m_os;
	private DataInputStream m_is;
	private String m_requestHttpMethod;
	private String m_requestFilePathString;
	private String m_requestHttpVersion;
	private String m_requestHeaders;
	private String m_requestInitialLineParams;
	private String m_requestFromUser;
	private String m_requestInitialLine;
	private File m_responseFile;
	private ResponseCode m_responeCode;
	private BufferedReader m_bufferedReader;
	private boolean m_connectionClosed;
	private EmailAddress m_emailAddressOfUser;
	private byte[] m_responseFileAsByteArray;
	private boolean m_responseIsChunked;
	private boolean m_requestIsVirtualPage;

	// Constructor
	public HttpRequest(Socket i_connection) {
		m_socket = i_connection;
		m_responseHeadersHasMap = new HashMap<String, String>();
		m_requestHeadersHashMap = new HashMap<String, String>();
		m_responseNoPage = false;
		m_connectionClosed = false;
	}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {
			processRequest();
		} catch (BadHttpRequestException e) {
			e.printStackTrace();
			try {
				m_responeCode = ResponseCode.BadRequest;
				m_responseNoPage = true;
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				m_responeCode = ResponseCode.InternalServerError;
				m_responseNoPage = true;
				buildHttpResponse();
			} catch (Exception e1) {
			}
			closeResources();
		} catch (IOException e) {
			e.printStackTrace();
			closeResources();
		} catch (NumberFormatException e) {
			try {
				m_responeCode = ResponseCode.BadRequest;
				m_responseNoPage = true;
				buildHttpResponse();
			} catch (Exception e1) {
			}
			return;
			// in case an error occured while editing a reminder we
			// change the request to a virtual page that displays failure
			// message
		} catch (BadReminder e) {
			e.printStackTrace();
			m_requestFilePathString = "bad_reminder.html";
			m_responeCode = ResponseCode.OK;
			try {
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (BadTask e) {
			m_requestFilePathString = "bad_task.html";
			m_responeCode = ResponseCode.OK;
			try {
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (BadPoll e) {
			m_requestFilePathString = "bad_poll.html";
			m_responeCode = ResponseCode.OK;
			try {
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (BadVoteException e) {
			m_requestFilePathString = "poll_closed.html";
			m_responeCode = ResponseCode.OK;
			try {
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (BadTaskReply e) {
			m_requestFilePathString = "task_closed.html";
			m_responeCode = ResponseCode.OK;
			try {
				buildHttpResponse();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeResources();
		}
		// this finally block will call closeGracefully only if another method
		// did not call it already
		finally {
			try {
				closeResources();
			} catch (Exception t) {
			}
		}
	}

	private void processRequest() throws Exception {
		m_requestFromUser = readRequest();
		System.out.println(m_requestFromUser);
		parseInitialLineOfRequest();
		parseRequestHeaders();
		checkRequestIsValid();

		if (isLogoutRequest()) {
			handleLogout();
		} else if (requestShouldBeRedirectedToLoginPage()) {
			handleRedirectToLoginPage();
		}

		else if (validLoginRequest()) {
			handleLoginRequest();
		} else if (isSubmitOrDeleteApplicationTaskRequest()) {
			changeApplicationObjectsFromSubmitRequests();

		} else if (isTaskReplyRequest()) {
			handleTaskReply();
		} else if (isPollReplyRequest()) {
			handlePollReplyRequest();
		} else {
			if (m_requestHttpMethod.equals("GET")) {
				handleMethodgGET();
			} else if (m_requestHttpMethod.equals("POST")) {
				handleMethodPOST();
			} else if (m_requestHttpMethod.equals("HEAD")) {
				handleMethodHEAD();
			} else if (m_requestHttpMethod.equals("TRACE")) {
				handleMethodTRACE();
			} else if (m_requestHttpMethod.equals("OPTIONS")) {
				handleMethodOPTIONS();
			} else {
				m_responeCode = ResponseCode.NotImplemented;
			}

		}

		buildHttpResponse();
	}

	private void handleLoginRequest() {
		m_responseNoPage = true;
		m_responseHeadersHasMap.put("Set-Cookie", "name="
				+ m_requestGetPostParamsHashMap.get("Email"));
		m_responeCode = ResponseCode.MovedPermanently;
		m_responseHeadersHasMap.put("Location", "http://"
				+ WebServer.s_ServerName + ":" + WebServer.s_port
				+ "/main.html");
	}

	private void handleRedirectToLoginPage() {
		m_responseNoPage = true;
		m_responeCode = ResponseCode.MovedPermanently;
		m_responseHeadersHasMap.put("Location", "http://"
				+ WebServer.s_ServerName + ":" + WebServer.s_port
				+ "/index.html");

	}

	private void handleLogout() {
		m_responseNoPage = true;
		m_responeCode = ResponseCode.MovedPermanently;
		m_responseHeadersHasMap.put("Location", "http://"
				+ WebServer.s_ServerName + ":" + WebServer.s_port
				+ "/index.html");
		m_responseHeadersHasMap.put("Set-Cookie", "name="
				+ m_emailAddressOfUser
				+ "; expires=Thu, 01 Jan 1970 00:00:01 GMT;");

	}

	private void handleTaskReply() throws BadTaskReply {
		Task taskToEdit = Database.getTask(new EmailAddress(
				m_requestGetPostParamsHashMap.get("owner")),
				m_requestGetPostParamsHashMap.get("id"));
		if (taskToEdit == null) {
			throw new BadTaskReply();
		} else if (!taskToEdit.getTaskStatus().equals(TaskStatus.InProgress)) {
			throw new BadTaskReply();
		}

		taskToEdit.handleTaskWasCompleted();
		Database.saveTasks();
		m_responeCode = ResponseCode.OK;
	}

	private void handlePollReplyRequest() throws BadVoteException {
		Poll pollToEdit = Database.getPoll(new EmailAddress(
				m_requestGetPostParamsHashMap.get("owner")),
				m_requestGetPostParamsHashMap.get("id"));
		if (pollToEdit == null) {
			throw new BadVoteException();
		} else if (pollToEdit.m_pollStatus.equals(PollStatus.Completed)) {
			throw new BadVoteException();
		}

		pollToEdit
				.voteAnswer(
						new EmailAddress(m_requestGetPostParamsHashMap
								.get("recipient")),
						m_requestGetPostParamsHashMap.get("answer"));
		Database.savePolls();
		m_responeCode = ResponseCode.OK;

	}

	private boolean isTaskReplyRequest() {
		boolean result = false;
		if (m_requestFilePathString.contains(WebServer.s_taskReplyPage)) {
			result = true;
		}
		return result;
	}

	private boolean isPollReplyRequest() {
		boolean result = false;
		if (m_requestFilePathString.contains(WebServer.s_pollReplyPage)) {
			result = true;
		}
		return result;
	}

	private void changeApplicationObjectsFromSubmitRequests() throws Exception {

		// handle delete
		ApplicationObject toDelete = null;
		if (m_requestGetPostParamsHashMap.containsKey("operation")
				&& m_requestGetPostParamsHashMap.get("operation").equals(
						"delete")) {

			if (m_requestFilePathString.contains("reminder")) {
				toDelete = Database.getReminder(m_emailAddressOfUser,
						m_requestGetPostParamsHashMap.get("id"));
				if (toDelete != null) {
					toDelete.handleApllicationObjectDeletion();
					Database.deleteReminder(toDelete.getOwnerEmailAddress(),
							m_requestGetPostParamsHashMap.get("id"));
				}
			} else if (m_requestFilePathString.contains("task")) {
				toDelete = Database.getTask(m_emailAddressOfUser,
						m_requestGetPostParamsHashMap.get("id"));
				if (toDelete != null) {
					toDelete.handleApllicationObjectDeletion();
					Database.deleteTask(toDelete.getOwnerEmailAddress(),
							m_requestGetPostParamsHashMap.get("id"));
				}
			} else if (m_requestFilePathString.contains("poll")) {
				toDelete = Database.getPoll(m_emailAddressOfUser,
						m_requestGetPostParamsHashMap.get("id"));
				if (toDelete != null) {
					toDelete.handleApllicationObjectDeletion();
					Database.deletePoll(m_emailAddressOfUser,
							m_requestGetPostParamsHashMap.get("id"));
				}
			}
		} else {

			// handle create or edit
			ApplicationObject newApplicationObject = null;

			if (m_requestFilePathString.contains("submit_reminder")) {

				newApplicationObject = new Reminder(
						m_requestGetPostParamsHashMap.get("subject"),
						m_requestGetPostParamsHashMap.get("content"),
						getDateFromRequest(
								m_requestGetPostParamsHashMap.get("dueDate"),
								m_requestGetPostParamsHashMap.get("dueTime")),
						m_emailAddressOfUser);

				// if the reminder already exists in database this is an
				// edit
				// and
				// we also know the task after editing is ok so we delete
				// the
				// old
				// before adding a new
				if (Database.getReminder(m_emailAddressOfUser,
						m_requestGetPostParamsHashMap.get("id")) != null) {
					Database.deleteReminder(m_emailAddressOfUser,
							m_requestGetPostParamsHashMap.get("id"));
				}
				Database.addReminder(m_requestGetPostParamsHashMap.get("id"),
						(Reminder) newApplicationObject);
			}

			else if (m_requestFilePathString.contains("submit_task")) {

				String id = IdGenerator.generateId();
				newApplicationObject = new Task(
						m_requestGetPostParamsHashMap.get("subject"),
						m_requestGetPostParamsHashMap.get("content"),
						getDateFromRequest(
								m_requestGetPostParamsHashMap.get("dueDate"),
								m_requestGetPostParamsHashMap.get("dueTime")),
						m_emailAddressOfUser,
						new EmailAddress(m_requestGetPostParamsHashMap
								.get("recipient")), id);
				Database.addTask(id, (Task) newApplicationObject);

			} else if (m_requestFilePathString.contains("submit_poll")) {
				String id = IdGenerator.generateId();
				newApplicationObject = new Poll(
						m_requestGetPostParamsHashMap.get("subject"),
						m_requestGetPostParamsHashMap.get("content"),
						m_emailAddressOfUser,
						buildArrayOfRecipients(m_requestGetPostParamsHashMap
								.get("recipients")),
						buildArrayOfAnswers(m_requestGetPostParamsHashMap
								.get("answers")), id);
				Database.addPoll(id, (Poll) newApplicationObject);
			}
		}
		m_responeCode = ResponseCode.OK;
	}

	private String[] buildArrayOfAnswers(String i_answers) {
		String answers[] = i_answers.split("\r\n");
		return answers;
	}

	private EmailAddress[] buildArrayOfRecipients(String i_recipients)
			throws BadPoll {
		String emails[] = i_recipients.split("\r\n");
		EmailAddress[] recipients = new EmailAddress[emails.length];
		for (int currentRecipient = 0; currentRecipient < emails.length; currentRecipient++) {
			if (EmailAddress
					.checkIfEmailAddressIsValid(emails[currentRecipient])) {
				recipients[currentRecipient] = new EmailAddress(
						emails[currentRecipient]);
			} else {
				throw new BadPoll();
			}
		}
		return recipients;
	}

	private Date getDateFromRequest(String i_dateAsString, String i_timeAsString)
			throws Exception {
		Date date = null;
		try {
			Calendar calendar = GregorianCalendar.getInstance();
			Pattern datePattern = Pattern
					.compile("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]");
			Pattern timePattern = Pattern.compile("[0-9][0-9]:[0-9][0-9]");
			Matcher dateMatcher = datePattern.matcher(i_dateAsString);
			Matcher timeMatcher = timePattern.matcher(i_timeAsString);
			if (!dateMatcher.matches() || !timeMatcher.matches()) {
				throw new IllegalArgumentException();
			}
			String[] dateAsArray = i_dateAsString.split("/");
			String[] timeAsString = i_timeAsString.split(":");

			calendar.set(Integer.parseInt(dateAsArray[2]),
					Integer.parseInt(dateAsArray[1]) - 1,
					Integer.parseInt(dateAsArray[0]),
					Integer.parseInt(timeAsString[0]),
					Integer.parseInt(timeAsString[1]));

			date = calendar.getTime();
		} catch (IllegalArgumentException e) {
			if (m_requestFilePathString.contains("reminder")) {
				throw new BadReminder();
			} else if (m_requestFilePathString.contains("task")) {
				throw new BadTask();
			} else if (m_requestFilePathString.contains("poll")) {
				throw new BadPoll();
			}
		}
		return date;
	}

	private boolean isSubmitOrDeleteApplicationTaskRequest() {
		boolean result = false;
		// a request to submit starts with submit_
		// a request to delete is to reminders.html or one of those and contains
		// operation=delete header
		String operation = "";
		if (m_requestGetPostParamsHashMap.containsKey("operation")) {
			operation = m_requestGetPostParamsHashMap.get("operation");
		}
		if (m_requestHttpMethod.equals("GET")
				&& (operation.equals("delete")
						|| m_requestFilePathString.contains("submit_reminder")
						|| m_requestFilePathString.contains("submit_task") || m_requestFilePathString
							.contains("submit_poll"))) {
			result = true;
		}
		return result;
	}

	private void checkRequestIsValid() throws Exception {
		if (m_requestHttpVersion.equals("1.1")
				&& m_requestHeadersHashMap.get("Host") == null) {
			throw new BadHttpRequestException(
					"Http Version 1.1 must include host");
		}

		if (m_requestHeadersHashMap.containsKey("Chunked")) {
			if (m_requestHttpVersion.equals("1.0")) {
				throw new BadHttpRequestException(
						"Chunked header not supported in version 1.0");
			}
		}

		checkRequestedResourceIsValid();
	}

	private boolean isLogoutRequest() throws Exception {
		boolean result = false;
		if (clientLoggedIn() && m_requestHttpMethod.equals("GET")
				&& m_requestFilePathString.contains("logout.html")) {
			result = true;
		}
		return result;
	}

	private boolean requestShouldBeRedirectedToLoginPage() {
		boolean result = false;

		if (m_emailAddressOfUser == null
				&& !WebServer
						.pageIsAllowedAccessToNotLoggedInUser(m_requestFilePathString)) {
			result = true;
		}
		return result;
	}

	private EmailAddress getUserNameOfClient() {
		EmailAddress result = null;
		if (m_requestHeadersHashMap.containsKey("Cookie")) {
			String[] splitTheCookieHeader = m_requestHeadersHashMap.get(
					"Cookie").split("=");
			String emailAddressInTheRequestAsString = splitTheCookieHeader[1];
			if (EmailAddress
					.checkIfEmailAddressIsValid(emailAddressInTheRequestAsString)) {
				result = new EmailAddress(emailAddressInTheRequestAsString);
			}
		}

		return result;
	}

	private boolean validLoginRequest() throws Exception {
		boolean requestContainsvalidEmail = false;
		if (m_requestFilePathString.equals(WebServer.s_root
				+ WebServer.s_defaultPage)
				&& m_requestGetPostParamsHashMap.containsKey("Email")
				&& EmailAddress
						.checkIfEmailAddressIsValid(m_requestGetPostParamsHashMap
								.get("Email"))) {
			requestContainsvalidEmail = true;
		}

		return requestContainsvalidEmail;
	}

	private void parseInitialLineOfRequest() throws BadHttpRequestException,
			UnsupportedEncodingException {
		Pattern checkRequestPattern = Pattern.compile(
				"(.*) /(.*) HTTP/(1\\.[0-1])\\n(.*)", Pattern.MULTILINE
						| Pattern.DOTALL);
		Matcher checkRequestMatcher = checkRequestPattern
				.matcher(m_requestFromUser);

		if (!checkRequestMatcher.matches()) {
			throw new BadHttpRequestException("Bad request");
		}

		m_requestInitialLine = m_requestFromUser.substring(0,
				m_requestFromUser.indexOf("\n"));
		m_requestHttpMethod = checkRequestMatcher.group(1);
		m_requestFilePathString = WebServer.s_root
				+ checkRequestMatcher.group(2);
		int indexOfQuestionMark = m_requestFilePathString.indexOf('?');
		if (indexOfQuestionMark > -1) {
			m_requestInitialLineParams = m_requestFilePathString
					.substring(indexOfQuestionMark + 1);
			m_requestFilePathString = m_requestFilePathString.substring(0,
					indexOfQuestionMark);

		}
		m_requestHttpVersion = checkRequestMatcher.group(3);
		m_requestHeaders = checkRequestMatcher.group(4);

		parseInitialLineGetPostParameters();
	}

	private void handleMethodOPTIONS() throws Exception {
		m_responseFile = null;
		m_responeCode = ResponseCode.OK;
	}

	private void handleMethodTRACE() throws Exception {
		m_responseHeadersHasMap.put("Content-Type", r_contentTypeHttp);
		m_responseFileAsByteArray = m_requestFromUser.getBytes();
		m_responeCode = ResponseCode.OK;
	}

	private void handleMethodHEAD() throws Exception {
		m_responseFile = null;
		m_responseHeadersHasMap.remove("Content-Length");
		String contentType = determineContentType(m_requestFilePathString);
		m_responseHeadersHasMap.put("Content-Type", contentType);
		m_responeCode = ResponseCode.OK;
	}

	private void handleMethodPOST() throws Exception {

		String postParamsString = null;
		int contentLength = Integer.parseInt(m_requestHeadersHashMap
				.get("Content-Length"));
		int byteRead = 0;
		byte[] postParamsByteArray = new byte[contentLength];
		int currentByte = 0;

		while (currentByte < contentLength
				&& (byteRead = m_bufferedReader.read()) > 0) {
			postParamsByteArray[currentByte] = (byte) byteRead;
			currentByte++;
		}
		postParamsString = new String(postParamsByteArray);
		parsePostParams(postParamsString);
		String contentType = determineContentType(m_requestFilePathString);
		m_responseHeadersHasMap.put("Content-Type", contentType);
		m_responeCode = ResponseCode.OK;
	}

	/***
	 * Checks if the requested resource is valid
	 * 
	 * @param i_requestedPagePath
	 * @throws Exception
	 */
	private void checkRequestedResourceIsValid() throws Exception {

		File file = new File(m_requestFilePathString);
		if (!filePathIsUnderRootFolder(m_requestFilePathString)) {
			throw new BadHttpRequestException(
					"Resource not under root directory");
		} else if (file.isDirectory()) {
			m_requestFilePathString = r_pathToDefaultPage;
			m_responseFile = new File(m_requestFilePathString);
			if (!m_responseFile.exists()) {
				throw new FileNotFoundException();
			}
		}
		// if the file does not exist check if it is one of the dynamic
		// pages we
		else if (isVirtualPage(m_requestFilePathString)) {
			m_requestIsVirtualPage = true;
		} else if (!file.isFile()) {
			throw new BadHttpRequestException("Resource does not exist");
		} else {
			m_responseFile = new File(m_requestFilePathString);
		}

	}

	private byte[] buildVirtualPage() throws BadHttpRequestException {
		String result = null;

		if (m_requestFilePathString.contains("reminders.html")) {
			result = HtmlPagesBuilder.buildRemindersHtml(m_emailAddressOfUser);
		} else if (m_requestFilePathString.contains("tasks.html")) {
			result = HtmlPagesBuilder.buildTasksHtml(m_emailAddressOfUser);
		} else if (m_requestFilePathString.contains("polls.html")) {
			result = HtmlPagesBuilder.buildPollsHtml(m_emailAddressOfUser);
		} else if (m_requestFilePathString.contains("reminder_editor")) {
			boolean isRequestForNewReminder = true;
			if (m_requestGetPostParamsHashMap.containsKey("operation")) {
				String operation = m_requestGetPostParamsHashMap
						.get("operation");
				if (operation.equals("edit")) {
					isRequestForNewReminder = false;
				} else if (operation.equals("remove")) {
					result = HtmlPagesBuilder
							.buildRemindersHtml(m_emailAddressOfUser);
				}
			} else {
				throw new BadHttpRequestException(
						"The request to reminder_editor did not contain an operation field");
			}
			result = HtmlPagesBuilder.buildReminderEditorHtmll(
					m_emailAddressOfUser.getEmailAddress(),
					isRequestForNewReminder ? IdGenerator.generateId()
							: m_requestGetPostParamsHashMap.get("id"),
					isRequestForNewReminder);

		} else if (m_requestFilePathString.contains("task_editor")) {
			result = HtmlPagesBuilder.buildTaskEditorHtml(
					m_emailAddressOfUser.getEmailAddress(),
					IdGenerator.generateId());
		} else if (m_requestFilePathString.contains("poll_editor")) {
			result = HtmlPagesBuilder.buildPollEditorHtml(
					m_emailAddressOfUser.getEmailAddress(),
					IdGenerator.generateId());
		} else if (m_requestFilePathString.contains("submit_reminder")) {
			result = HtmlPagesBuilder.buildReminderSubmittedSuccessfully();

		} else if (m_requestFilePathString.contains("submit_task")) {
			result = HtmlPagesBuilder.buildTaskSubmittedSuccessfully();
		} else if (m_requestFilePathString.contains("submit_poll")) {
			result = HtmlPagesBuilder.buildPollSubmittedSuccessfully();
		} else if (m_requestFilePathString.contains("bad_reminder")) {
			result = HtmlPagesBuilder
					.buildReminderWasNotSubmittedSuccessfully();
		} else if (m_requestFilePathString.contains("bad_task")) {
			result = HtmlPagesBuilder.buildTaskWasNotSubmittedSuccessfully();
		} else if (m_requestFilePathString.contains("bad_poll")) {
			result = HtmlPagesBuilder.buildPollWasNotSubmittedSuccessfully();
		} else if (m_requestFilePathString.contains(WebServer.s_taskReplyPage)) {
			result = HtmlPagesBuilder.buildTaskReplyPage();
		} else if (m_requestFilePathString.contains(WebServer.s_pollReplyPage)) {
			result = HtmlPagesBuilder.buildPollReplyPage();
		} else if (m_requestFilePathString.contains("poll_closed")) {
			result = HtmlPagesBuilder.buildPollClosed();
		} else if (m_requestFilePathString.contains("task_closed")) {
			result = HtmlPagesBuilder.buildTaskClosed();
		}

		if (result != null) {
			return result.getBytes();
		} else {
			return null;
		}
	}

	private boolean isVirtualPage(String i_requestedPagePath) {
		boolean result = false;
		for (String virtualPageName : WebServer.s_virtualPages) {
			if (i_requestedPagePath.contains(virtualPageName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	// handles parsing all of the GET parameters
	private void handleMethodgGET() throws Exception {
		String contentType = determineContentType(m_requestFilePathString);
		m_responseHeadersHasMap.put("Content-Type", contentType);
		m_responeCode = ResponseCode.OK;
	}

	private String determineContentType(String i_filePathString) {
		String contentType = null;

		if (i_filePathString.endsWith(".bmp")) {
			contentType = r_contentTypeBMP;

		} else if (i_filePathString.endsWith(".jpg")) {
			contentType = r_contentTypeJPG;

		} else if (i_filePathString.endsWith(".gif")) {
			contentType = r_contentTypeGIF;

		} else if (i_filePathString.endsWith(".png")) {
			contentType = r_contentTypePNG;

		} else if (i_filePathString.endsWith(".html")) {
			contentType = r_contentTypeHtml;

		} else if (i_filePathString.endsWith(".ico")) {
			contentType = r_contentTypeIcon;
		} else {
			contentType = r_contentTypeApplicationOctetStream;
		}
		return contentType;
	}

	// parses the request headers (that are after the initial line) and puts
	// them in a hash table
	private void parseRequestHeaders() throws UnsupportedEncodingException {

		Pattern requestParamsPattern = Pattern.compile("(.*): (.*)\\n");
		Matcher requestParamsMatcher = requestParamsPattern
				.matcher(m_requestHeaders);
		while (requestParamsMatcher.find()) {
			m_requestHeadersHashMap.put(requestParamsMatcher.group(1),
					requestParamsMatcher.group(2));
		}

		m_responseIsChunked = false;
		if (m_requestHeadersHashMap.containsKey("Chunked")
				&& (m_requestHeadersHashMap.get("Chunked")).equals("yes")) {
			m_responseIsChunked = true;
			m_responseHeadersHasMap.put("Transfer-Encoding", "Chunked");
		}
		m_emailAddressOfUser = getUserNameOfClient();

	}

	// parses the parameters that are in the get and put them in a hash table
	private void parseInitialLineGetPostParameters()
			throws UnsupportedEncodingException {

		// if there is a match we should have parameters
		// put the parameters in a hash table
		m_requestGetPostParamsHashMap = new HashMap<String, String>();

		// in order to more easily parse the parameters an & is
		// appended to the parameters part
		String getPostParameters = m_requestInitialLineParams + '&';
		Pattern splitGetPostParamsPattern = Pattern.compile("([^=]*)=([^&]*)&");
		Matcher splitGetPostParamsMatcher = splitGetPostParamsPattern
				.matcher(getPostParameters);
		while (splitGetPostParamsMatcher.find()) {
			m_requestGetPostParamsHashMap.put(
					splitGetPostParamsMatcher.group(1),
					splitGetPostParamsMatcher.group(2));
		}

	}

	private boolean filePathIsUnderRootFolder(String i_filePath)
			throws Exception {
		boolean isValid = false;
		File f = new File(i_filePath);
		String canonical = null;
		canonical = f.getCanonicalPath();

		String[] splitedPath = canonical.split("\\\\");
		String[] splitedRoot = WebServer.s_root.split("\\\\");

		isValid = (splitedPath[0].equalsIgnoreCase(splitedRoot[0]) && splitedPath[1]
				.equalsIgnoreCase(splitedRoot[1]));

		return isValid;

	}

	private void buildHttpResponse() throws Exception {

		// build initial line of response
		String httpResponseInitialLine = buildInitialLineOfHttpResponse(
				m_responeCode, m_requestHttpVersion);

		// build the parameter lines of response
		String httpResponseParamterLines = buildParameterLines();

		// print the response initial line and header
		String allResponse = httpResponseInitialLine
				+ httpResponseParamterLines + r_CRLF;
		System.out.println(allResponse);

		// write the response initial line and header
		m_os = new DataOutputStream(m_socket.getOutputStream());
		m_os.write(allResponse.getBytes());

		// if the response contains a page to return build it
		if (!m_responseNoPage) {
			if (m_requestIsVirtualPage) {
				m_responseFileAsByteArray = buildVirtualPage();
			} else {
				m_responseFileAsByteArray = getFileAsByteArray(m_responseFile);
			}
		}

		if (m_responseFileAsByteArray != null) {
			if (m_responseIsChunked) {
				sendChunkedFile(m_responseFileAsByteArray);
			} else {
				m_os.write(m_responseFileAsByteArray);
				m_os.write(r_CRLF.getBytes());
				m_os.flush();
			}
		} else {
			m_os.write(r_CRLF.getBytes());
			m_os.flush();
		}
	}

	private String buildParameterLines() {
		String headersLines = "";

		if (m_requestHttpMethod != null
				&& m_requestHttpMethod.equals("OPTIONS")) {
			headersLines += "Allow: OPTIONS, HEAD, TRACE, POST, GET" + r_CRLF;
			if (!m_responseIsChunked) {
				headersLines += "Content-Length: 0" + r_CRLF;
			}
		}

		for (String key : m_responseHeadersHasMap.keySet()) {
			headersLines += key + ": " + m_responseHeadersHasMap.get(key)
					+ r_CRLF;
		}

		return headersLines;
	}

	private void sendChunkedFile(byte[] pageContent) throws Exception {

		int maxSizeChunk = 50;
		String endOfChunkedFileSending = "0" + r_CRLF + r_CRLF;
		int startIndex = 0;
		int contentLengthRemaining = pageContent.length;
		byte[] currChunkContent = null;
		int currChunkContentLength;
		while (contentLengthRemaining > 0) {
			if (contentLengthRemaining >= maxSizeChunk)
				currChunkContent = new byte[maxSizeChunk];
			else
				currChunkContent = new byte[contentLengthRemaining];
			currChunkContentLength = currChunkContent.length;
			System.arraycopy(pageContent, startIndex, currChunkContent, 0,
					currChunkContentLength);
			m_os.write(Integer.toHexString(currChunkContentLength).getBytes());
			m_os.write(r_CRLF.getBytes());
			m_os.write(currChunkContent);
			m_os.write(r_CRLF.getBytes());

			contentLengthRemaining -= currChunkContentLength;
			startIndex += currChunkContentLength;
			currChunkContent = null;
		}
		m_os.write(endOfChunkedFileSending.getBytes());
	}

	private String buildInitialLineOfHttpResponse(ResponseCode i_responseCode,
			String i_version) {
		if (i_version == null) {
			i_version = r_defaultVersion;
		}
		String initialLineOfHttpResponse = "HTTP/" + i_version + " ";

		switch (i_responseCode) {
		case OK:
			initialLineOfHttpResponse += r_okString;
			break;
		case NotImplemented:
			initialLineOfHttpResponse += r_notImplementedString;
			break;
		case MovedPermanently:
			initialLineOfHttpResponse += r_movedPeramanently;
			break;
		case BadRequest:
			initialLineOfHttpResponse += r_badRequestString;
			break;
		case NotFound:
			initialLineOfHttpResponse += r_notFoundString;
			break;

		case InternalServerError:
			initialLineOfHttpResponse += r_internalServerError;
			break;
		default:
			initialLineOfHttpResponse += r_badRequestString;

		}
		initialLineOfHttpResponse += r_CRLF;

		return initialLineOfHttpResponse;
	}

	private byte[] getFileAsByteArray(File i_file) throws IOException {
		byte[] bFile = null;

		FileInputStream fis = new FileInputStream(i_file);
		bFile = new byte[(int) i_file.length()];

		// read until the end of the stream.
		while (fis.available() != 0) {
			fis.read(bFile, 0, bFile.length);
		}
		fis.close();
		return bFile;
	}

	private String readRequest() throws IOException {
		String request = "";
		m_is = new DataInputStream(m_socket.getInputStream());

		m_bufferedReader = new BufferedReader(new InputStreamReader(m_is));
		String line = null;

		while ((line = m_bufferedReader.readLine()) != null
				&& line.length() > 0) {
			request += line + "\n";
		}
		request = URLDecoder.decode(request, "UTF-8");
		return request;
	}

	private void parsePostParams(String i_parametersToParse) {
		String reqired = i_parametersToParse + "&";

		// in order to more easily parse the parameters an & is
		// appended to the parameters part
		Pattern splitGetPostParamsPattern = Pattern.compile("([^=]*)=([^&]*)&");
		Matcher splitGetPostParamsMatcher = splitGetPostParamsPattern
				.matcher(reqired);
		while (splitGetPostParamsMatcher.find()) {
			String key = splitGetPostParamsMatcher.group(1);
			String value = splitGetPostParamsMatcher.group(2);
			m_requestGetPostParamsHashMap.put(key, value);
		}
	}

	public String buildParamsInfoHtmlForResponse() {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body><table border=\"1\"><tr><td>name</td><td>value</td></tr>");
		Set<String> keySet = m_requestGetPostParamsHashMap.keySet();
		for (String key : keySet) {
			builder.append("<tr><td>");
			builder.append("" + key);
			builder.append("</td><td>");
			builder.append(m_requestGetPostParamsHashMap.get(key));
			builder.append("</td></tr>");
		}
		builder.append("</table></body></html>");
		return builder.toString();
	}

	public boolean clientLoggedIn() throws Exception {
		boolean hasCookie = false;
		if (m_requestHeadersHashMap.containsKey("Cookie")) {

			if (EmailAddress.checkIfEmailAddressIsValid(m_requestHeadersHashMap
					.get("Cookie"))) {
				hasCookie = true;
			}
		}

		return hasCookie;
	}

	private void closeResources() {

		try {
			if (!m_connectionClosed) {
				if (m_bufferedReader != null) {
					m_bufferedReader.close();
				}

				if (m_is != null) {
					m_is.close();
				}

				if (m_os != null) {
					m_os.close();
				}

				if (m_socket != null) {
					m_socket.close();
				}
			}
		} catch (Exception e) {
		} finally {
			m_connectionClosed = true;
		}

	}
}
