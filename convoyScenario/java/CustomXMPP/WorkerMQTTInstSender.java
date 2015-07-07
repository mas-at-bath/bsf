package CustomXMPP;

import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;

public class WorkerInstSender extends Sensor  {

	public WorkerInstSender(String server, String user, String pwd, String node) throws XMPPException {
		super(server, user, pwd, node);
	}
	
	/*public WorkerInstSender(Connection conn, String user, String pwd, String node) throws XMPPException {
		super(conn, user, pwd, node);
	}*/
	
	public void releaseEvent(String event) {
		JsonReading jr = new JsonReading();
		jr.addValue("EVENT", event);
		try {
			publish(jr);
			System.out.println("XX: published to INST: " + event);
		} catch (Exception ue) {
			System.out.println("publish failed!");
		}
	}

	public void releaseEventWithInitials(String event, String initials) {
		JsonReading jr = new JsonReading();
		jr.addValue("EVENT", event);
		jr.addValue("INITIALS", initials);
		try {
			publish(jr);
			System.out.println("XX: published to INST: " + event + " with initials: " + initials);
		} catch (Exception ue) {
			System.out.println("publish failed!");
		}
	}
}
