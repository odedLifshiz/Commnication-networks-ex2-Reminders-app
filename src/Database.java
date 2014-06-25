public class Database {
	private static GlobalPollsHash s_globalPollsHash;
	private static GlobalRemindersHash s_globalRemindersHash;
	private static GlobalTasksHash s_globalTasksHash;

	private static void saveAll() {
		savePolls();
		saveReminders();
		saveTasks();
	}

	public static void readDataFromDatabase() {
		// try to read objects from database
		s_globalPollsHash = DatabaseAccess.readGlobalPolls(getsPollfilepath());
		s_globalRemindersHash = DatabaseAccess
				.readGlobalReminders(getsReminderfilepath());
		s_globalTasksHash = DatabaseAccess.readGlobalTasks(getsTaskfilepath());
		// if any of the objects are null create new instances and write to
		// database
		if (s_globalPollsHash == null) {
			s_globalPollsHash = new GlobalPollsHash();
		}

		if (s_globalRemindersHash == null) {
			s_globalRemindersHash = new GlobalRemindersHash();
		} else {
			resetRemindersTimers();
		}

		if (s_globalTasksHash == null) {
			s_globalTasksHash = new GlobalTasksHash();
		} else {
			resetTasksTimers();
		}

		saveAll();
	}

	private static void resetRemindersTimers() {
		GlobalRemindersHash GRH = s_globalRemindersHash;
		if (s_globalRemindersHash.keySet() != null) {
			for (String email : s_globalRemindersHash.keySet()) {
				ReminderHash reminderHash = s_globalRemindersHash.get(email);
				if (reminderHash.keySet() != null) {
					for (String id : reminderHash.keySet()) {
						Reminder reminder = reminderHash.get(id);
						reminder.resetTimer();
					}
				}
			}
		}
	}

	private static void resetTasksTimers() {
		String userEmailString = null;
		if (s_globalTasksHash.keySet() != null) {
			for (String email : s_globalTasksHash.keySet()) {
				TaskHash taskHash = s_globalTasksHash.get(email);
				if (taskHash.keySet() != null) {
					for (String id : taskHash.keySet()) {
						Task task = taskHash.get(id);
						task.renewTimerOnStartup();
					}
				}
			}
		}
	}

	public static void savePolls() {
		DatabaseAccess.writePolls(s_globalPollsHash, getsPollfilepath());
	}

	public static void saveReminders() {
		DatabaseAccess.writeReminders(s_globalRemindersHash,
				getsReminderfilepath());
	}

	public static void saveTasks() {
		DatabaseAccess.writeTasks(s_globalTasksHash, getsTaskfilepath());
	}

	private static String getsReminderfilepath() {
		return WebServer.s_ReminderFilePath;
	}

	private static String getsTaskfilepath() {
		return WebServer.s_TaskFilePath;
	}

	private static String getsPollfilepath() {
		return WebServer.s_PollFilePath;
	}

	public static GlobalPollsHash getGlobalPollsHash() {
		return s_globalPollsHash;
	}

	public static GlobalRemindersHash getGlobalRemindersHash() {
		return s_globalRemindersHash;
	}

	public static GlobalTasksHash getGlobalTasksHash() {
		return s_globalTasksHash;
	}

	public static boolean containsDataOfLoggedInUser(EmailAddress userEmail) {
		GlobalPollsHash hash = getGlobalPollsHash();
		return (hash.get(userEmail.getEmailAddress()) != null);
	}

	public static void createNewDataForLoggedInUser(EmailAddress userEmail) {
		s_globalPollsHash.put(userEmail.getEmailAddress(), new PollHash());
		savePolls();
		s_globalRemindersHash.put(userEmail.getEmailAddress(),
				new ReminderHash());
		saveReminders();
		s_globalTasksHash.put(userEmail.getEmailAddress(), new TaskHash());
		saveTasks();
	}

	public static void addReminder(String i_ID, Reminder i_reminder) {

		ReminderHash hashOfUser = s_globalRemindersHash.get(i_reminder
				.getOwnerEmailAddress().getEmailAddress());
		if (hashOfUser == null) {
			String hisEmai = i_reminder.getOwnerEmailAddress()
					.getEmailAddress();
			s_globalRemindersHash.put(i_reminder.getOwnerEmailAddress()
					.getEmailAddress(), hashOfUser = new ReminderHash());
		}
		hashOfUser.put(i_ID, i_reminder);
		System.err.println("added new Reminder for username "
				+ i_reminder.getOwnerEmailAddress().getEmailAddress()
				+ "with ID named " + i_ID);
		saveReminders();
	}

	public static void addTask(String i_ID, Task i_task) {
		TaskHash hashOfUser = getGlobalTasksHash().get(
				i_task.getOwnerEmailAddress().getEmailAddress());
		if (hashOfUser == null) {
			String hisEmai = i_task.getOwnerEmailAddress().getEmailAddress();
			getGlobalTasksHash().put(
					i_task.getOwnerEmailAddress().getEmailAddress(),
					hashOfUser = new TaskHash());
		}

		hashOfUser.put(i_ID, i_task);
		System.err.println("added new task for username "
				+ i_task.getOwnerEmailAddress().getEmailAddress()
				+ "with ID named " + i_ID);
		saveTasks();
	}

	public static void addPoll(String i_ID, Poll i_poll) {
		PollHash hashOfUser = getGlobalPollsHash().get(
				i_poll.getOwnerEmailAddress());
		if (hashOfUser == null) {
			String hisEmai = i_poll.getOwnerEmailAddress().getEmailAddress();
			getGlobalPollsHash().put(
					i_poll.getOwnerEmailAddress().getEmailAddress(),
					hashOfUser = new PollHash());
		}

		hashOfUser.put(i_ID, i_poll);
		System.err.println("added new Poll for username "
				+ i_poll.getOwnerEmailAddress().getEmailAddress()
				+ "with ID named " + i_ID);
		savePolls();
	}

	public static void deletePoll(EmailAddress i_owner, String i_ID) {
		if (getPoll(i_owner, i_ID) != null) {
			getGlobalPollsHash().get(i_owner.getEmailAddress()).remove(i_ID);
			savePolls();
		}
	}

	public static void deleteReminder(EmailAddress i_owner, String i_ID) {
		if (getReminder(i_owner, i_ID) != null) {
			getGlobalRemindersHash().get(i_owner.getEmailAddress())
					.remove(i_ID);
			saveReminders();
		}
	}

	public static void deleteTask(EmailAddress i_owner, String i_ID) {
		if (getTask(i_owner, i_ID) != null) {
			getGlobalTasksHash().get(i_owner.getEmailAddress()).remove(i_ID);
			saveTasks();
		}
	}

	public static Task getTask(EmailAddress i_userEmail, String i_id) {
		Task toReturn = null;
		if (s_globalTasksHash.containsKey(i_userEmail.getEmailAddress())
				&& s_globalTasksHash.get(i_userEmail.getEmailAddress())
						.containsKey(i_id)) {
			toReturn = s_globalTasksHash.get(i_userEmail.getEmailAddress())
					.get(i_id);
		}

		return toReturn;
	}

	public static Reminder getReminder(EmailAddress i_userEmail, String i_id) {

		Reminder toReturn = null;
		if (s_globalRemindersHash.containsKey(i_userEmail.getEmailAddress())
				&& s_globalRemindersHash.get(i_userEmail.getEmailAddress())
						.containsKey(i_id)) {

			toReturn = s_globalRemindersHash.get(i_userEmail.getEmailAddress())
					.get(i_id);
		}

		return toReturn;
	}

	public static Poll getPoll(EmailAddress i_userEmail, String i_id) {
		Poll toReturn = null;
		if (s_globalPollsHash.get(i_userEmail.getEmailAddress()).containsKey(
				i_id)
				&& s_globalPollsHash.get(i_userEmail.getEmailAddress())
						.containsKey(i_id)) {
			toReturn = s_globalPollsHash.get(i_userEmail.getEmailAddress())
					.get(i_id);
		}

		return toReturn;
	}
}