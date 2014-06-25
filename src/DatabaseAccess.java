import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DatabaseAccess {

	public static GlobalPollsHash readGlobalPolls(String pollfilepath) {
		GlobalPollsHash usersPollsHash = (GlobalPollsHash) readObjectFromDatabase(pollfilepath);
		System.err.println("Deserialized PollsHash...");

		return usersPollsHash;
	}

	public static GlobalRemindersHash readGlobalReminders(String reminderfilepath) {
		GlobalRemindersHash usersRemindersHash = (GlobalRemindersHash) readObjectFromDatabase(reminderfilepath);
		System.err.println("Deserialized ReminderHash...");

		return usersRemindersHash;
	}

	public static GlobalTasksHash readGlobalTasks(String taskfilepath) {
		GlobalTasksHash usersTasksHash = (GlobalTasksHash) readObjectFromDatabase(taskfilepath);
		System.err.println("Deserialized TasksHash...");

		return usersTasksHash;

	}

	public static void writePolls(GlobalPollsHash i_globalPollsHash, String pollfilepath) {
		synchronized (i_globalPollsHash) {
			writeObjectToDatabase(pollfilepath, i_globalPollsHash);
		}
		System.err.println("Serialized PollsHash...");
	}

	public static void writeReminders(GlobalRemindersHash i_globalRemindersHash, String reminderfilepath) {
		synchronized (i_globalRemindersHash) {
			writeObjectToDatabase(reminderfilepath,i_globalRemindersHash);
		}
		System.err.println("Serialized RemindersHash...");
	}

	public static void writeTasks(GlobalTasksHash i_globalTaskHash, String taskfilepath) {
		synchronized (i_globalTaskHash) {
			writeObjectToDatabase(taskfilepath, i_globalTaskHash);
		}
		System.err.println("Serialized TasksHash...");
	}

	private static Object readObjectFromDatabase(String i_filePath) {
		Object objectToReadFromFile = null;
		try {
			FileInputStream fileIn = new FileInputStream(i_filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			objectToReadFromFile = in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			System.err.println("IO EXCEPTION!! -> returning null");
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Reminder class not found");
			c.printStackTrace();
			return null;
		}
		return objectToReadFromFile;
	}

	private static void writeObjectToDatabase(String i_filePath,
			Object i_objectToWriteToDatabase) {
		try {
			FileOutputStream fileOut = new FileOutputStream(i_filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(i_objectToWriteToDatabase);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in " + i_filePath);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
}
