/*
 * MindSensor 
 * - Sensor in Jason agent side, extending from BSF sensor
 *   Major role is to publish actions to body part via 'ACTION' node,
 *   and also publish queries via 'QUERY' node to the institution manager.   
 * 
 * 		@author		JeeHang
 * 		@date		17 Apr 2013
 */

import java.io.UnsupportedEncodingException;

//import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;

public class MindSensor extends Sensor {
	
	public MindSensor(String server, String user, String pwd, String node) throws XMPPException {
		super(server, user, pwd, node);
	}
	
	public MindSensor(XMPPTCPConnection conn, String user, String pwd, String node) throws XMPPException {
		super(conn, user, pwd, node);
	}
	
	public void releaseAction(String action) {
		JsonReading jr = new JsonReading();
		jr.addValue("ACTION", action);
		try {
			publish(jr);
		} catch (UnsupportedEncodingException ue) {
			System.out.println("publish failed!");
		}
	}
}
