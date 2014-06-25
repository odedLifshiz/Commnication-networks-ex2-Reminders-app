import java.io.Serializable;
import java.util.Date;
import java.util.Timer;

public abstract class ApplicationObject implements Serializable {
	private String m_subject;
	private String m_content;
	private Date m_dateAndtimeOfCreation;
	private EmailAddress m_ownerEmail;
	private boolean m_deadLineWasHandled;

	public ApplicationObject(String i_subject, String i_content,
			EmailAddress i_ownerEmail) {
		setDeadLineWasHandled(false);
		setDataAndTimeOfCreation(new Date());
		setSubject(i_subject);
		setContent(i_content);
		setOwnerEmail(i_ownerEmail);
	}

	public boolean getDeadLineHandled() {
		return m_deadLineWasHandled;
	}

	public void setDeadLineWasHandled(boolean i_deadLineArrived) {
		m_deadLineWasHandled = i_deadLineArrived;
	}

	private void setDataAndTimeOfCreation(Date i_date) {
		m_dateAndtimeOfCreation = i_date;
	}

	public Date getDateAndtimeOfCreation() {
		return m_dateAndtimeOfCreation;
	}

	public String getSubject() {
		return m_subject;
	}

	public void setSubject(String i_subject) {
		m_subject = i_subject;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String i_content) {
		m_content = i_content;
	}

	public EmailAddress getOwnerEmailAddress() {
		return m_ownerEmail;
	}

	public void setOwnerEmail(EmailAddress i_ownerEmail) {
		m_ownerEmail = i_ownerEmail;
	}

	public void handleDeadlineOccured() {
		setDeadLineWasHandled(true);
	}

	public abstract void handleApllicationObjectDeletion();
}
