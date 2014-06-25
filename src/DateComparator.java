import java.util.Comparator;

public class DateComparator implements Comparator<Reminder> {

	@Override
	public int compare(Reminder i_reminder1, Reminder i_reminder2) {
		int result = 0;
		result = i_reminder1.getDateAndtimeOfReminding()
				.compareTo(i_reminder2.getDateAndtimeOfReminding());
		return result;
	}

}
