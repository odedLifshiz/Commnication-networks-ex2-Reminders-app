import java.util.Date;

public class Reminder extends ApplicationObject {
	private Date m_dateDeadLine;
	private SerializableTimer m_timer;
	private ApplicationObjectTask m_applicationObjectTask;

	public Reminder(String i_subject, String i_content,
			Date i_dateAndtimeOfReminding, EmailAddress i_ownerEmail) {
		super(i_subject, i_content, i_ownerEmail);
		setDateAndtimeOfReminding(i_dateAndtimeOfReminding);
		m_timer = new SerializableTimer();
		m_applicationObjectTask = new ApplicationObjectTask(this);
		m_timer.schedule(m_applicationObjectTask, i_dateAndtimeOfReminding);
	}

	public void resetTimer() {
		m_timer.cancel();
		if (!getDeadLineHandled()) {
			m_timer = new SerializableTimer();
			m_timer.schedule(new ApplicationObjectTask(this), m_dateDeadLine);
		}
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
		SMTPClient.sendMessage("Reminder: " + getSubject(),
				"The following reminder reached due time:\r\n" + getContent(),
				getOwnerEmailAddress(), getOwnerEmailAddress());

	}

	@Override
	public String toString() {
		return ("Subject:" + getSubject() + ", " + "Content:" + getContent()
				+ ", " + "Date Of Creation:"
				+ getDateAndtimeOfCreation().toString() + ", "
				+ "Date Of Reminding:" + getDateAndtimeOfReminding() + ".");
	}

	@Override
	public void handleApllicationObjectDeletion() {
		m_timer.cancel();
		m_applicationObjectTask.cancel();

	}
}
