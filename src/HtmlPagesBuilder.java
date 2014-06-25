import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;

public class HtmlPagesBuilder {

	private static final String r_CRLF = "\r\n";

	public static String buildRemindersHtml(EmailAddress i_ownerEmail) {
		String emailAddress = i_ownerEmail.getEmailAddress();
		Reminder currReminder = null;
		GlobalRemindersHash globalRemindersHash = Database
				.getGlobalRemindersHash();
		ReminderHash userReminderHash = globalRemindersHash.get(emailAddress);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Reminders </title>" + r_CRLF);
		builder.append("<body>" + r_CRLF);

		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"reminder_editor.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"hidden\" name=\"operation\" value="
				+ "\"" + "addNew" + "\"" + " />" + r_CRLF);
		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\"" + ""
				+ "\"" + " />" + r_CRLF);
		builder.append("<input type=\"submit\" value=\"New Reminder\" />"
				+ r_CRLF);

		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);
		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"main.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"submit\" value=\"Home\" />" + r_CRLF);
		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);

		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);
		PriorityQueue<String> queue = new PriorityQueue<String>();

		if (userReminderHash != null) {
			for (String id : userReminderHash.keySet()) {
				queue.add(id);
			}
			for (String id : queue) {
				currReminder = userReminderHash.get(id);
				builder.append("<TR>" + r_CRLF);
				builder.append("	<TD>Title: " + currReminder.getSubject()
						+ "</TD>" + r_CRLF);
				builder.append("	<TD>Date and time of creation: "
						+ currReminder.getDateAndtimeOfCreation().toString()
						+ "</TD>" + r_CRLF);
				builder.append("	<TD>Date and time of reminding: "
						+ currReminder.getDateAndtimeOfReminding().toString()
						+ "</TD>" + r_CRLF);

				builder.append("	<TD>" + r_CRLF);
				builder.append("<form name=\"input\" action=\"reminder_editor.html\" method=\"get\">"
						+ r_CRLF);
				builder.append("<input type=\"hidden\" name=\"operation\" value="
						+ "\"" + "edit" + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"hidden\" name=\"id\" value="
						+ "\"" + id + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"submit\" value=\"Edit\" />"
						+ "\n");
				builder.append("</form>" + r_CRLF);
				builder.append("	</TD>" + r_CRLF);

				builder.append("	<TD>" + r_CRLF);
				builder.append("<form name=\"input\" action=\"reminders.html\" method=\"get\">"
						+ r_CRLF);
				builder.append("<input type=\"hidden\" name=\"operation\" value="
						+ "\"" + "delete" + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"hidden\" name=\"id\" value="
						+ "\"" + id + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"submit\" value=\"Delete\" />"
						+ r_CRLF);
				builder.append("</form>" + r_CRLF);
				builder.append("	</TD>" + r_CRLF);

				builder.append("</TR>" + r_CRLF);
			}
		}

		builder.append("</TABLE>" + r_CRLF);
		builder.append("</body>" + r_CRLF);
		builder.append("</html>" + r_CRLF);
		return builder.toString();
	}

	public static String buildTasksHtml(EmailAddress i_ownerEmail) {
		String i_owner = i_ownerEmail.getEmailAddress();
		Task currTask = null;
		GlobalTasksHash globalTasksHash = Database.getGlobalTasksHash();
		TaskHash userTaskHash = globalTasksHash.get(i_owner);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Tasks </title>" + r_CRLF);
		builder.append("<body>" + r_CRLF);

		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"task_editor.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"hidden\" name=\"operation\" value="
				+ "\"" + "addNew" + "\"" + " />" + r_CRLF);
		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\"" + ""
				+ "\"" + " />" + r_CRLF);
		builder.append("<input type=\"submit\" value=\"New Task\" />" + r_CRLF);
		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);

		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"main.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"submit\" value=\"Home\" />" + r_CRLF);
		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);
		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);
		if (userTaskHash != null) {
			PriorityQueue<String> queue = new PriorityQueue<String>();
			for (String id : userTaskHash.keySet()) {
				queue.add(id);
			}
			for (String id : queue) {
				currTask = userTaskHash.get(id);
				builder.append("<TR>" + r_CRLF);
				builder.append("	<TD>Title: " + currTask.getSubject() + "</TD>"
						+ r_CRLF);
				builder.append("	<TD>Date and time of creation: "
						+ currTask.getDateAndtimeOfCreation().toString()
						+ "</TD>" + r_CRLF);
				builder.append("	<TD>Date and time of reminding: "
						+ currTask.getDateAndtimeOfReminding().toString()
						+ "</TD>" + r_CRLF);
				builder.append("	<TD>Status: " + currTask.getTaskStatus()
						+ "</TD>" + r_CRLF);
				if (currTask.getTaskStatus().equals(TaskStatus.InProgress)) {
					builder.append("	<TD>" + r_CRLF);
					builder.append("<form name=\"input\" action=\"tasks.html\" method=\"get\">"
							+ r_CRLF);
					builder.append("<input type=\"hidden\" name=\"operation\" value="
							+ "\"" + "delete" + "\"" + " />" + r_CRLF);
					builder.append("<input type=\"hidden\" name=\"id\" value="
							+ "\"" + id + "\"" + " />" + r_CRLF);
					builder.append("<input type=\"submit\" value=\"Delete\" />"
							+ r_CRLF);
					builder.append("</form>" + r_CRLF);

				}
			}
			builder.append("</TR>");
		}
		builder.append("</TABLE>" + r_CRLF);
		builder.append("</body>" + r_CRLF);
		builder.append("</html>" + r_CRLF);
		return builder.toString();
	}

	public static String buildPollsHtml(EmailAddress i_ownerEmail) {
		String emailAddress = i_ownerEmail.getEmailAddress();
		Poll currPoll = null;
		GlobalPollsHash globalPollsHash = Database.getGlobalPollsHash();
		PollHash userPollHash = globalPollsHash.get(emailAddress);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Polls </title>" + r_CRLF);
		builder.append("<body>" + r_CRLF);

		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"poll_editor.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"hidden\" name=\"operation\" value="
				+ "\"" + "addNew" + "\"" + " />" + r_CRLF);
		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\"" + ""
				+ "\"" + " />" + r_CRLF);
		builder.append("<input type=\"submit\" value=\"New Poll\" />" + r_CRLF);
		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);

		builder.append("	<TD>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"main.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<input type=\"submit\" value=\"Home\" />" + r_CRLF);
		builder.append("</form>" + r_CRLF);
		builder.append("	</TD>" + r_CRLF);

		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);
		if (userPollHash != null) {
			PriorityQueue<String> queue = new PriorityQueue<String>();
			for (String id : userPollHash.keySet()) {
				queue.add(id);
			}
			for (String id : queue) {
				currPoll = userPollHash.get(id);
				builder.append("<TR>" + r_CRLF);
				builder.append("	<TD>Title: " + currPoll.getSubject() + "</TD>"
						+ r_CRLF);
				builder.append("	<TD>Date and time of creation: "
						+ currPoll.getDateAndtimeOfCreation().toString()
						+ "</TD>" + r_CRLF);
				builder.append("	<TD>Recipients replies : "
						+ currPoll.getRecipientsReplies() + "</TD>" + r_CRLF);

				builder.append("	<TD>" + r_CRLF);
				builder.append("<form name=\"input\" action=\"polls.html\" method=\"get\">"
						+ r_CRLF);
				builder.append("<input type=\"hidden\" name=\"operation\" value="
						+ "\"" + "delete" + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"hidden\" name=\"id\" value="
						+ "\"" + id + "\"" + " />" + r_CRLF);
				builder.append("<input type=\"submit\" value=\"Delete\" />"
						+ r_CRLF);
				builder.append("</form>" + r_CRLF);
				builder.append("	</TD>" + r_CRLF);
				builder.append("</TR>" + r_CRLF);
			}
		}
		builder.append("</TABLE>" + r_CRLF);
		builder.append("</body>" + r_CRLF);
		builder.append("</html>" + r_CRLF);
		return builder.toString();
	}

	public static String buildReminderEditorHtmll(String i_owner, String i_id,
			boolean i_isNewReminder) throws BadHttpRequestException {
		String subject = "";
		String content = "";
		Date dateAndtimeOfReminding = null;
		String[] dateAndtimeOfRemindingStringArray = new String[2];
		dateAndtimeOfRemindingStringArray[0] = "";
		dateAndtimeOfRemindingStringArray[1] = "";
		if (!i_isNewReminder) {
			Reminder reminderToEdit = Database.getGlobalRemindersHash()
					.get(i_owner).get(i_id);
			if (reminderToEdit == null) {
				throw new BadHttpRequestException("could not edit reminder");
			}
			subject = reminderToEdit.getSubject();
			content = reminderToEdit.getContent();
			dateAndtimeOfReminding = reminderToEdit.getDateAndtimeOfReminding();
			dateAndtimeOfRemindingStringArray = getDateAndTimeString(dateAndtimeOfReminding);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Reminder editor </title>" + r_CRLF);
		builder.append("<body>Reminder Editor</body><br><br>" + r_CRLF);
		builder.append("<a href=\"reminders.html\"> Reminders</a>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"submit_reminder.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append(" <TD>Subject:</TD>" + r_CRLF);
		builder.append("  <TD>" + r_CRLF);
		builder.append("<input type=\"text\" value=\"" + subject
				+ "\" name=\"subject\"><br>" + r_CRLF);
		builder.append("</TD>" + r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Reminder content: </TD>" + r_CRLF);
		builder.append("<TD><textarea name=\"content\">" + content
				+ "</textarea><br></TD>" + r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Date of reminder due date(DD/MM/YYYY): </TD>"
				+ r_CRLF);
		builder.append("<TD><input type=\"text\" value=\""
				+ dateAndtimeOfRemindingStringArray[0]
				+ "\" name=\"dueDate\"><br></TD>" + r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Time of reminder due date(HH:MM): </TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" value=\""
				+ dateAndtimeOfRemindingStringArray[1]
				+ "\" name=\"dueTime\"><br></TD>" + r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\""
				+ i_id + "\"" + " />" + r_CRLF);

		builder.append("</TABLE>" + r_CRLF);
		builder.append("<P><input type=\"submit\" value=\"Save\"><br>" + r_CRLF);
		builder.append("</FORM>" + r_CRLF);
		builder.append("<a href=\"reminders.html\"> Cancel </a>" + r_CRLF);
		builder.append("</html>" + r_CRLF);

		return builder.toString();
	}

	public static String buildTaskEditorHtml(String i_owner, String i_id) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Task editor </title>" + r_CRLF);
		builder.append("<body>Task Editor</body><br><br>" + r_CRLF);
		builder.append("<a href=\"tasks.html\"> Tasks</a>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"submit_task.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append(" <TD>Task Subject:</TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" name=\"subject\"><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Task content: </TD>" + r_CRLF);
		builder.append("<TD><textarea name=\"content\"></textarea><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Task recipient: </TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" name=\"recipient\"><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Date of task due date: </TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" name=\"dueDate\"><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Time of task due date: </TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" name=\"dueTime\"><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\""
				+ i_id + "\"" + " />" + r_CRLF);

		builder.append("</TABLE>" + r_CRLF);
		builder.append("<P><input type=\"submit\" value=\"Send\"><br>" + r_CRLF);
		builder.append("</FORM>" + r_CRLF);
		builder.append("<a href=\"reminders.html\"> Cancel </a>" + r_CRLF);
		builder.append("</html>" + r_CRLF);

		return builder.toString();
	}

	public static String buildPollEditorHtml(String i_owner, String i_id) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>" + r_CRLF);
		builder.append("<title>Poll editor </title>" + r_CRLF);
		builder.append("<body>Poll Editor</body><br><br>" + r_CRLF);
		builder.append("<a href=\"polls.html\"> Polls</a>" + r_CRLF);
		builder.append("<form name=\"input\" action=\"submit_poll.html\" method=\"get\">"
				+ r_CRLF);
		builder.append("<TABLE BORDER=\"1\">" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append(" <TD>Poll Subject:</TD>" + r_CRLF);
		builder.append("<TD><input type=\"text\" name=\"subject\"><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Poll content: </TD>" + r_CRLF);
		builder.append("<TD><textarea name=\"content\"></textarea><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Poll recipients: </TD>" + r_CRLF);
		builder.append("<TD><textarea name=\"recipients\"></textarea><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<TR>" + r_CRLF);
		builder.append("<TD>Poll answers: </TD>" + r_CRLF);
		builder.append("<TD><textarea name=\"answers\"></textarea><br></TD>"
				+ r_CRLF);
		builder.append("</TR>" + r_CRLF);

		builder.append("<input type=\"hidden\" name=\"id\" value=" + "\""
				+ i_id + "\"" + " />" + r_CRLF);

		builder.append("</TABLE>" + r_CRLF);
		builder.append("<P><input type=\"submit\" value=\"Send\"><br>" + r_CRLF);
		builder.append("</FORM>" + r_CRLF);
		builder.append("<a href=\"reminders.html\"> Cancel </a>" + r_CRLF);
		builder.append("</html>" + r_CRLF);

		return builder.toString();
	}

	private static String[] getDateAndTimeString(Date i_date) {
		String[] toReturn = new String[2];
		Calendar cal = Calendar.getInstance();
		cal.setTime(i_date);
		String year = "" + cal.get(Calendar.YEAR);
		String monthString;
		int month = cal.get(Calendar.MONTH);
		month++;
		monthString = "" + month;
		if (monthString.length() == 1)
			monthString = "0" + monthString;
		String day = "" + cal.get(Calendar.DAY_OF_MONTH);
		String hour = "" + cal.get(Calendar.HOUR_OF_DAY);
		String minute = "" + cal.get(Calendar.MINUTE);
		if (minute.length() == 1)
			minute = "0" + minute;
		String dateString = day + "/" + monthString + "/" + year;
		toReturn[0] = dateString;
		String timeString = hour + ":" + minute;
		toReturn[1] = timeString;
		return toReturn;
	}

	public static String buildReminderSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Reminder</title>"
				+ r_CRLF
				+ "<body>Reminder was submitted succesfully!</body><br><br>"
				+ r_CRLF + "<a href=\"reminders.html\">Go back</a><br>"
				+ r_CRLF;

		return result;
	}

	public static String buildTaskSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Task</title>"
				+ r_CRLF
				+ "<body>Task was submitted succesfully!</body><br><br>"
				+ r_CRLF + "<a href=\"tasks.html\">Go back</a><br>" + r_CRLF;
		;

		return result;
	}

	public static String buildPollSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Poll</title>"
				+ r_CRLF
				+ "<body>Poll was submitted succesfully!</body><br><br>"
				+ r_CRLF + "<a href=\"polls.html\">Go back</a><br>" + r_CRLF;

		return result;
	}

	public static String buildReminderWasNotSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Reminder</title>"
				+ r_CRLF
				+ "<body>Reminder was not submitted succesfully</body><br><br>"
				+ r_CRLF + "<a href=\"reminders.html\">Go back</a><br>"
				+ r_CRLF;

		return result;
	}

	public static String buildTaskWasNotSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Reminder</title>"
				+ r_CRLF
				+ "<body>Reminder was not submitted succesfully</body><br><br>"
				+ r_CRLF + "<a href=\"reminders.html\">Go back</a><br>"
				+ r_CRLF;

		return result;
	}

	public static String buildPollWasNotSubmittedSuccessfully() {
		String result = "<html>" + r_CRLF + "<title>Submit Poll</title>"
				+ r_CRLF
				+ "<body>Poll was not submitted succesfully</body><br><br>"
				+ r_CRLF + "<a href=\"polls.html\">Go back</a><br>" + r_CRLF;

		return result;
	}

	public static String buildTaskReplyPage() {
		String result = "<html>" + r_CRLF + "<title>Task_reply</title>"
				+ r_CRLF + "<body>Task was succesfully updated</body><br><br>"
				+ r_CRLF;

		return result;

	}

	public static String buildPollReplyPage() {
		String result = "<html>" + r_CRLF + "<title>Poll_reply</title>"
				+ r_CRLF + "<body>Poll was succesfully updated</body><br><br>"
				+ r_CRLF;

		return result;

	}

	public static String buildPollClosed() {
		String result = "<html>" + r_CRLF + "<title>Poll_reply</title>"
				+ r_CRLF + "<body>Poll has been closed</body><br><br>" + r_CRLF;

		return result;
	}

	public static String buildTaskClosed() {
		String result = "<html>" + r_CRLF + "<title>Task_reply</title>"
				+ r_CRLF + "<body>Task has been closed</body><br><br>" + r_CRLF;

		return result;
	}
}