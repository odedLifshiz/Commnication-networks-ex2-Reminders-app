import java.util.HashMap;

@SuppressWarnings("serial")
public class Poll extends ApplicationObject {
	PollStatus m_pollStatus;
	private HashMap<EmailAddress, Boolean> m_recipientsHashMap;
	private HashMap<String, Integer> m_answers;
	int m_votesLeft;
	private String m_id;

	public Poll(String i_subject, String i_content, EmailAddress i_ownerEmail,
			EmailAddress[] i_recipientEmails, String[] i_answers, String i_id) {
		super(i_subject, i_content, i_ownerEmail);

		m_pollStatus = PollStatus.InProgress;
		m_votesLeft = i_recipientEmails.length;
		initializeAnswersHash(i_answers);
		setId(i_id);
		setRecipientEmailsCheckAnsweres(i_recipientEmails);
		sendNewPollMessages();
	}

	private void sendNewPollMessages() {

		for (EmailAddress recipient : m_recipientsHashMap.keySet()) {
			SMTPClient.sendMessage(getSubject(),
					getContentMessageForNewPoll(recipient),
					getOwnerEmailAddress(), recipient);
		}

	}

	private String getContentMessageForNewPoll(EmailAddress i_recipient) {

		String contentMessageForNewPoll = "The following poll was sent to you by: "
				+ getOwnerEmailAddress().getEmailAddress()
				+ "\r\n"
				+ getContent()
				+ "\r\n"
				+ "Please choose one of the following:\r\n";
		String linkToVote = "http://" + WebServer.s_ServerName + ":"
				+ WebServer.s_port + "/" + WebServer.s_pollReplyPage + "?id="
				+ getId() + "&recipient=" + i_recipient.getEmailAddress();
		for (String answer : m_answers.keySet()) {
			contentMessageForNewPoll += answer + " " + linkToVote + "&answer="
					+ answer + "&owner=" + getOwnerEmailAddress() + "\r\n";
		}
		return contentMessageForNewPoll;
	}

	public HashMap<EmailAddress, Boolean> getRecipientEmail_checkAnsweres() {
		return m_recipientsHashMap;
	}

	private void setRecipientEmailsCheckAnsweres(EmailAddress[] i_recipientEmail) {
		m_recipientsHashMap = new HashMap<EmailAddress, Boolean>();
		for (EmailAddress emailAddress : i_recipientEmail) {
			m_recipientsHashMap.put(emailAddress, new Boolean(false));
		}
	}

	public HashMap<String, Integer> getAnswers() {
		return m_answers;
	}

	public void voteAnswer(EmailAddress i_theReplyingRecipient, String i_answer)
			throws BadVoteException {
		if (m_recipientsHashMap.get(i_theReplyingRecipient) == true) {
			throw new BadVoteException();
		} else {
			m_recipientsHashMap.put(i_theReplyingRecipient, true);
		}
		Integer votedAnswerResult = getAnswers().get(i_answer);
		votedAnswerResult++;
		getAnswers().put(i_answer, votedAnswerResult);
		m_votesLeft--;
		sendUpdateMessageToOwner(i_theReplyingRecipient, i_answer);
		if (m_votesLeft == 0) {
			handleDeadlineOccured();
		}
	}

	private void sendUpdateMessageToOwner(EmailAddress i_theReplyingRecipient,
			String i_answer) {

		String remaining = m_votesLeft != 0 ? "Votes remaining: " + m_votesLeft
				: "This was the last vote";
		SMTPClient
				.sendMessage("Poll update: " + getSubject(),
						"The poll question was: " + getContent()
								+ "\r\nRecipient: " + i_theReplyingRecipient
								+ " answered your poll\r\nHis vote was: "
								+ i_answer + "\r\n" + remaining,

						new EmailAddress("someEmail@email.com"),
						getOwnerEmailAddress());

	}

	private void initializeAnswersHash(String[] i_answers) {
		m_answers = new HashMap<String, Integer>();
		for (String answer : i_answers) {
			m_answers.put(answer, new Integer(0));
		}
	}

	@Override
	public void handleDeadlineOccured() {
		super.handleDeadlineOccured();
		System.out.println("dead Line Occured!");
		m_pollStatus = PollStatus.Completed;
		String result = "Poll Results for the following poll:\r\n "
				+ getContent() + "\r\n";
		for (String answer : m_answers.keySet()) {
			result += "answer: " + answer + ". number of votes: "
					+ m_answers.get(answer) + "\r\n";
		}

		SMTPClient.sendMessage("Poll results", result, new EmailAddress(
				"some@email"), getOwnerEmailAddress());

	}

	public String getRecipientsReplies() {
		StringBuilder builder = new StringBuilder();
		for (String answer : getAnswers().keySet()) {
			builder.append(" " + answer + ": " + getAnswers().get(answer));
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Poll title: " + getSubject());
		for (String answer : getAnswers().keySet()) {
			builder.append(", " + answer + "= " + getAnswers().get(answer));
		}
		return builder.toString();
	}

	public String getId() {
		return m_id;
	}

	public void setId(String i_id) {
		m_id = i_id;
	}

	@Override
	public void handleApllicationObjectDeletion() {

	}

}
