import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailAddress implements Serializable {
	private String m_emailAddress;

	public EmailAddress(String i_emailAddress) {
		m_emailAddress = i_emailAddress;
	}

	public static boolean checkIfEmailAddressIsValid(String i_emailAddress) {
		boolean emailIsValid = false;
		Pattern emailPattern = Pattern.compile(".*@.*");
		Matcher emailMathcer = emailPattern.matcher(i_emailAddress);
		if (emailMathcer.matches()) {
			emailIsValid = true;
		}
		return emailIsValid;
	}

	public String getEmailAddress() {
		return m_emailAddress;
	}

	public void setEmailAddress(String i_emailAddress)
			 {
		if (checkIfEmailAddressIsValid(i_emailAddress)) {
			m_emailAddress = i_emailAddress;
		}
	}

	@Override
	public boolean equals(Object o) {
		EmailAddress e = (EmailAddress) o;
		return getEmailAddress().equals(e.getEmailAddress());
	}

	@Override
	public int hashCode() {

		return getEmailAddress().hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getEmailAddress();
	}
}
