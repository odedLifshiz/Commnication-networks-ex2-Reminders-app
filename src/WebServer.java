import java.net.*;
import java.util.ArrayList;

public final class WebServer {

	public static String s_SMTPName;
	public static String s_ServerName;
	public static String s_SMTPUserName;
	public static String s_SMTPPassword;
	public static String s_ReminderFilePath;
	public static String s_TaskFilePath;
	public static String s_PollFilePath;
	public static boolean s_SMTPIsAuthLogin;
	public static int s_SMTPPort;
	public static String s_root;
	public static int s_port;
	public static int s_maxThreads;
	public static ArrayList<String> s_virtualPages;
	public static String s_defaultPage = "main.html";
	public static final String s_taskReplyPage = "task_reply.html";
	public static final String s_pollReplyPage = "poll_reply.html";

	public static void runWebserver() {
		try {

			readConfigFile();
			buildListOfVirtualPages();
			initializeDatabase();

			Socket connection = null;
			ServerSocket socket = null;

			ThreadPool threadPool = new ThreadPool(s_maxThreads);
			System.out.println("WebServer: Creating thread pool");

			// Establish the listen socket.
			socket = new ServerSocket(s_port);

			// Process HTTP service requests in an infinite loop.
			while (true) {

				// Listen for a TCP connection request.
				System.out.println("WebServer listening on port " + s_port);
				connection = socket.accept();
				System.out.println("WebServer: Accepted a new connection");
				threadPool.execute(new HttpRequest(connection));

			}
		} catch (BadConfigFileException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("Bad config file. exiting");
		} catch (Exception e) {
			System.out.println("An error occured");
		}

	}

	private static void buildListOfVirtualPages() {
		s_virtualPages = new ArrayList<String>();
		s_virtualPages.add("reminders.html");
		s_virtualPages.add("polls.html");
		s_virtualPages.add("tasks.html");
		s_virtualPages.add("submit_reminder.html");
		s_virtualPages.add("submit_poll.html");
		s_virtualPages.add("submit_task.html");
		s_virtualPages.add("task_editor.html");
		s_virtualPages.add("reminder_editor.html");
		s_virtualPages.add("poll_editor.html");
		s_virtualPages.add("logout.html");
		s_virtualPages.add(s_taskReplyPage);
		s_virtualPages.add(s_pollReplyPage);

	}

	private static void initializeDatabase() {

		Database.readDataFromDatabase();

	}

	private static void readConfigFile() throws Exception {
		ConfigFileParser parser = new ConfigFileParser("config.ini");

		s_SMTPName = parser.getStringConfigFileValue("SMTPName");
		s_SMTPPort = parser.getIntConfigFileValue("SMTPPort");
		s_ServerName = parser.getStringConfigFileValue("ServerName");
		s_SMTPUserName = parser.getStringConfigFileValue("SMTPUsername");
		s_SMTPPassword = parser.getStringConfigFileValue("SMTPPassword");
		s_SMTPIsAuthLogin = parser.getStringConfigFileValue("SMTPIsAuthLogin")
				.equals("TRUE");
		s_ReminderFilePath = parser
				.getStringConfigFileValue("reminderFilePath");
		s_TaskFilePath = parser.getStringConfigFileValue("taskFilePath");
		s_PollFilePath = parser.getStringConfigFileValue("pollFilePath");
		s_root = parser.getStringConfigFileValue("root");
		s_port = parser.getIntConfigFileValue("port");
		s_defaultPage = parser.getStringConfigFileValue("defaultPage");
		s_maxThreads = parser.getIntConfigFileValue("maxThreads");

	}

	public static boolean pageIsAllowedAccessToNotLoggedInUser(
			String i_requestedPagePath) {
		boolean result = false;
		if (i_requestedPagePath.equals(s_root + s_defaultPage)
				|| i_requestedPagePath.contains(s_pollReplyPage)
				|| i_requestedPagePath.contains(s_taskReplyPage)) {
			result = true;
		}

		return result;

	}

}
