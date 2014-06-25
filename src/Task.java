import java.util.Date;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class Task extends ApplicationObject {
	private Date m_dateDeadLine;
	private SerializableTimer m_timer;
	private TaskStatus m_taskStatus;
	private EmailAddress m_recipientEmail;
	private String m_id;
	private ApplicationObjectTask m_applicationObjectTask;

	public Task(String i_subject, String i_content,
			Date i_dateAndtimeOfReminding, EmailAddress i_ownerEmail,
			EmailAddress i_recipientEmail, String i_id) {
		super(i_subject, i_content, i_ownerEmail);
		setTaskStatus(TaskStatus.InProgress);
		setRecipientEmail(i_recipientEmail);
		setDateAndtimeOfReminding(i_dateAndtimeOfReminding);
		setId(i_id);
		m_timer = new SerializableTimer();
		m_applicationObjectTask = new ApplicationObjectTask(this);
		m_timer.schedule(m_applicationObjectTask, i_dateAndtimeOfReminding);
		// send the recipient an email notifying the task was created
		sendNewTaskMessage();

	}

	private void sendNewTaskMessage() {
		SMTPClient
				.sendMessage(
						"Task:" + getSubject(),
						getContent()
								+ "\r\n"
								+ "Click the following link to mark the tas as complete:\r\nhttp://"
								+ WebServer.s_ServerName + ":"
								+ +WebServer.s_port + "/"
								+ WebServer.s_taskReplyPage + "?owner="
								+ getOwnerEmailAddress() + "&id=" + getId()
								+ "\r\n", getOwnerEmailAddress(),
						getRecipientEmailAddress());
	}

	public void renewTimerOnStartup() {
		m_timer.cancel();
		if (!getDeadLineHandled()) {
			m_timer = new SerializableTimer();
			m_timer.schedule(new ApplicationObjectTask(this), m_dateDeadLine);
		}
	}

	public TaskStatus getTaskStatus() {
		return m_taskStatus;
	}

	public void setTaskStatus(TaskStatus i_taskStatus) {
		m_taskStatus = i_taskStatus;
	}

	public EmailAddress getRecipientEmailAddress() {
		return m_recipientEmail;
	}

	public void setRecipientEmail(EmailAddress i_recipientEmail) {
		m_recipientEmail = i_recipientEmail;
	}

	public void setDateAndtimeOfReminding(Date i_dateAndtimeOfReminding) {
		m_dateDeadLine = i_dateAndtimeOfReminding;
	}

	public Date getDateAndtimeOfReminding() {
		return m_dateDeadLine;
	}

	@Override
	public void handleDeadlineOccured() {
		super.handleDeadlineOccured();
		setTaskStatus(TaskStatus.timeIsDue);
		SMTPClient.sendMessage("Task: " + getSubject(),
				"Task time is due for the following task for you:\r\n"
						+ getContent(), getOwnerEmailAddress(),
				getRecipientEmailAddress());
		SMTPClient.sendMessage(getSubject(),
				"Task time is due for the following task you created:\r\n"
						+ getContent(), getOwnerEmailAddress(),
				getOwnerEmailAddress());
	}

	public void handleTaskWasCompleted() {
		super.handleDeadlineOccured();
		m_taskStatus = TaskStatus.Completed;
		m_timer.cancel();
		m_applicationObjectTask.cancel();
	}

	@Override
	public String toString() {
		return ("Subject:" + getSubject() + ", " + "Content:" + getContent()
				+ ", " + "Date Of Creation:"
				+ getDateAndtimeOfCreation().toString() + ", "
				+ "Date Of Reminding:" + getDateAndtimeOfReminding() + ".");
	}

	public String getId() {
		return m_id;
	}

	public void setId(String i_id) {
		m_id = i_id;
	}

	@Override
	public void handleApllicationObjectDeletion() {
		m_timer.cancel();
		m_applicationObjectTask.cancel();

	}
}