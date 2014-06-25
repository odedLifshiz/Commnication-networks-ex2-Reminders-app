import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class IdGenerator {
	private static long m_currIdToGive;
	private static final String lock = "";
	private static final String m_LastIdgivenFilePath = WebServer.s_root
			+ "/lastId.txt";

	public static void init() {
		BufferedReader reader;
		long lastID;
		try {
			reader = new BufferedReader(new FileReader(m_LastIdgivenFilePath));
			lastID = Long.parseLong(reader.readLine());
			m_currIdToGive = lastID + 1;
		} catch (FileNotFoundException fnfe) {
			System.err
					.println("Did not found the file: lastGivenID.txt ===>>> begin counting from 0");
			m_currIdToGive = 0;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String generateId() {
		String toReturn;
		synchronized (lock) {
			String toWrite = "" + m_currIdToGive;
			toReturn = "ID" + m_currIdToGive;
			m_currIdToGive++;
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						m_LastIdgivenFilePath));
				writer.write(toWrite);
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return toReturn;
	}
}
