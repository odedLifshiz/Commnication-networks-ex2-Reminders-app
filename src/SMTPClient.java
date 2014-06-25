import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.xml.bind.DatatypeConverter;

public class SMTPClient {

	private static final String s_CRLF = "\r\n";
	private static String s_initialConnectionServerResponse = "220";
	private static String s_enterMailServerResponse = "354";
	private static String s_okServerResponse = "250";
	private static String s_authenticatedServerResponse = "235";
	private static String s_client = "C:";
	private static String s_server = "S:";
	private static String s_EhloMessage = "EHLO OdedLifshiz NimrodCinman";
	private static String s_HeloMessage = "HELO tasker";
	private static String s_authLoginMessage = "AUTH LOGIN";
	private static String s_dataMessage = "DATA";
	private static String s_fromMessage = "From: Mr. tasker";
	private static String s_quitMessage = "QUIT";
	private static String s_subjectMessage = "Subject: ";
	private static String s_mailFromMessage = "MAIL FROM: taskker@idc.ac.il";
	private static String s_rcptToMessage = "RCPT TO:  <";
	private static String s_notOkMessage = "250 reply not received from server.\n";
	private static String s_initialConnectionResponseErrorMessage = "220 reply not received from server.\n";
	private static String s_badEnterMailMessage = "354 reply not received from server.\n";
	private static String s_authenticationErrorMessage = "235 reply not received from server";

	public static void sendMessage(String i_subject, String i_content,
			EmailAddress i_ownerEmailAddress,
			EmailAddress i_recipientEmailAddress) {
		// Establish a TCP connection with the mail server.
		Socket emailSocket = null;
		try {
			emailSocket = new Socket(WebServer.s_SMTPName, WebServer.s_SMTPPort);

			// Create a BufferedReader to read a line at a time.
			DataOutputStream dos = new DataOutputStream(
					emailSocket.getOutputStream());
			DataInputStream dis = new DataInputStream(
					emailSocket.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));

			// Read greeting from the server.
			String response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_initialConnectionServerResponse)) {
				throw new Exception(s_initialConnectionResponseErrorMessage);
			}

			// Get a reference to the socket's output stream.
			OutputStream os = emailSocket.getOutputStream();

			String fullHeloCommand;

			// Send HELO command and get server response.
			if (WebServer.s_SMTPIsAuthLogin) {
				fullHeloCommand = s_EhloMessage + s_CRLF;
				System.out.println(s_client + s_EhloMessage);
				os.write(fullHeloCommand.getBytes());
				for (int i = 0; i < 3; i++) {
					response = br.readLine();
					System.out.println(s_server + response);
				}
				System.out.println(s_client + s_authLoginMessage);
				os.write((s_authLoginMessage + s_CRLF).getBytes());
				response = br.readLine();
				System.out.println(s_server + response);
				String userNameBase64 = DatatypeConverter
						.printBase64Binary(WebServer.s_SMTPUserName.getBytes());
				os.write(userNameBase64.getBytes());
				System.out.println(s_client + userNameBase64);
				os.write(s_CRLF.getBytes());
				response = br.readLine();
				System.out.println(s_server + response);

				String passwordbase64 = DatatypeConverter
						.printBase64Binary((WebServer.s_SMTPPassword)
								.getBytes());
				System.out.println(s_client + passwordbase64);
				os.write(passwordbase64.getBytes());
				os.write(s_CRLF.getBytes());

			} else {
				fullHeloCommand = s_HeloMessage + s_CRLF;
				os.write(fullHeloCommand.getBytes());
				System.out.println(s_client + s_HeloMessage);
			}

			response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_authenticatedServerResponse)) {
				throw new Exception(s_authenticationErrorMessage);
			}

			// Send MAIL FROM command.
			String mailFromCommand = s_mailFromMessage + s_CRLF;
			System.out.println(s_client + mailFromCommand);
			os.write(mailFromCommand.getBytes());
			response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_okServerResponse)) {
				throw new Exception(s_notOkMessage);
			}

			// Send RCPT TO command.
			String fullAddress = s_rcptToMessage
					+ i_recipientEmailAddress.getEmailAddress() + ">" + s_CRLF;
			System.out.println(s_client + fullAddress);
			os.write(fullAddress.getBytes());
			response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_okServerResponse)) {
				throw new Exception(s_notOkMessage);
			}

			// Send DATA.
			// Send data headers.
			String dataString = s_dataMessage + s_CRLF;
			System.out.println(s_client + dataString);
			os.write(dataString.getBytes());
			response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_enterMailServerResponse))
				throw new Exception(s_badEnterMailMessage);

			String subject = s_subjectMessage + i_subject + s_CRLF;
			System.out.println(s_client + subject);
			os.write(subject.getBytes());

			String from = s_fromMessage + s_CRLF;
			System.out.println(s_client + from);
			os.write(from.getBytes());
			String sender = "Sender:" + i_ownerEmailAddress.getEmailAddress()
					+ s_CRLF;
			System.out.println(s_client + sender);
			os.write(sender.getBytes());

			// send content
			String content = s_CRLF + i_content + s_CRLF + "." + s_CRLF;
			System.out.println(s_client + content);
			os.write(content.getBytes());
			response = br.readLine();
			System.out.println(s_server + response);
			if (!response.startsWith(s_okServerResponse))
				throw new Exception(s_notOkMessage);

			// Send QUIT command.
			System.out.println(s_client + s_quitMessage);
			os.write(s_quitMessage.getBytes());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				emailSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
