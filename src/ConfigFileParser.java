import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileParser {

	private HashMap<String, String> m_configFileParamsHashMap;

	public ConfigFileParser(String filePath) throws BadConfigFileException {
		m_configFileParamsHashMap = new HashMap<String, String>();
		parseConfigFile(filePath);
	}

	public int getIntConfigFileValue(String i_fieldName)
			throws BadConfigFileException {
		int requestedIntValue;
		if (m_configFileParamsHashMap.containsKey(i_fieldName)) {
			requestedIntValue = Integer.parseInt(m_configFileParamsHashMap
					.get(i_fieldName));
		} else {
			throw new BadConfigFileException("Bad field");
		}

		return requestedIntValue;

	}

	public String getStringConfigFileValue(String i_fieldName)
			throws BadConfigFileException {
		String requestedStringValue;
		if (m_configFileParamsHashMap.containsKey(i_fieldName)) {
			requestedStringValue = m_configFileParamsHashMap.get(i_fieldName);
		} else {
			throw new BadConfigFileException("Bad field");
		}

		return requestedStringValue;

	}

	public void parseConfigFile(String configFilePath)
			throws BadConfigFileException {
		String configFileContent = readConfigFile(configFilePath);
		parseFileContent(configFileContent);
	}

	@SuppressWarnings("resource")
	private String readConfigFile(String configFilePath)
			throws BadConfigFileException {
		BufferedReader reader = null;
		String line = null;
		String configFileContent = "";

		try {
			reader = new BufferedReader(new FileReader(configFilePath));

			while ((line = reader.readLine()) != null) {
				configFileContent += line + "\n";
			}

		} catch (FileNotFoundException e) {
			throw new BadConfigFileException("Could not find config file");

		} catch (IOException e) {
			throw new BadConfigFileException("Could not read config file");
		}

		return configFileContent;

	}

	private void parseFileContent(String configFileContent) {

		Pattern configFilePattern = Pattern.compile("(.*)=(.*)\\n");
		Matcher configFileMatcher = configFilePattern
				.matcher(configFileContent);
		while (configFileMatcher.find()) {
			m_configFileParamsHashMap.put(configFileMatcher.group(1),
					configFileMatcher.group(2));
		}

	}
}