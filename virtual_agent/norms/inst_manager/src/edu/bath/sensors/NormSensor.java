package edu.bath.sensors;

import java.io.UnsupportedEncodingException;
import java.util.List;

//import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;

public class NormSensor extends Sensor {
	public NormSensor(String server, String user, String pwd, String node) throws XMPPException {
		super(server, user, pwd, node);
	}

	public NormSensor(String server, String user, String pwd, String node, boolean useMQTT, int qos) throws XMPPException {
		super(server, user, pwd, node, useMQTT, qos);
	}
	
	//public NormSensor(XMPPTCPConnection conn, String user, String pwd, String node) throws XMPPException {
	//	super(conn, user, pwd, node);
	//}
	
	public void releaseNorm(String norms) {
		JsonReading jr = new JsonReading();
		jr.addValue("STATE", norms);
		try {
			publish(jr);
		} catch (UnsupportedEncodingException ue) {
			System.out.println("publish failed!");
		}
	}
	
	public void releaseNorm(List<String> norms) {
		int index = 0;
		JsonReading jr = new JsonReading();
		jr.addValue("CONTENT", "NORM");
		jr.addValue("COUNT", norms.size());
		for (String norm : norms) {
			jr.addValue("NORM" + (index++), norm);
			System.out.println("NormSensor added: NORM"+index+ " " + norm);
		}

		try {
			publish(jr);
		} catch (UnsupportedEncodingException ue) {
			System.out.println("publish failed!");
		}
	}
}
