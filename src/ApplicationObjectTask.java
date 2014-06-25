import java.io.Serializable;
import java.util.TimerTask;

public class ApplicationObjectTask extends TimerTask implements Serializable {
	public ApplicationObject m_applicationObject;

	public ApplicationObjectTask(ApplicationObject i_applicationObject) {
		m_applicationObject = i_applicationObject;
	}

	@Override
	public void run() {
		System.out.println("ApplicationObjectTask: sending a message");
		m_applicationObject.handleDeadlineOccured();

	}
}
