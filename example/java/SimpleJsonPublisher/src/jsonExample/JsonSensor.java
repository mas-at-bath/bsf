package jsonExample;

import java.io.UnsupportedEncodingException;
import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;

public class JsonSensor extends Sensor {
	
	public JsonSensor(String server, String user, String pwd, String node) throws XMPPException
	{
		super(server, user, pwd, node);
	}

	public void run() throws UnsupportedEncodingException
	{

		double i = 0.0f;
		int cnt = 0;
		
		while (cnt++ < 152139) {
			JsonReading jr = new JsonReading();
			jr.addValue("takenAt", System.currentTimeMillis());
			jr.addValue("ACTION", "convoyMember4,amMoving(true)");
			jr.addValue("position", "153.04442797954152,0.0,262.3134876039802,149.5465600849747,0.0,262.43563584243896");
			publish(jr);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
