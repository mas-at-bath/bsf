package CustomXMPP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import org.jivesoftware.smack.XMPPException;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;

import java.util.Random;

public class InstSensClient  {

	private boolean alive = true;

	SensorClient sensorClient;

	private boolean debug = true;
	XMPPWorld parentObj;

	
	public InstSensClient(String serverAddress, String id, String password, XMPPWorld myParent) throws XMPPException {

		try
		{
			sensorClient = new SensorClient(serverAddress, id, password);
			parentObj = myParent;
			System.out.println("connected institution sensor client ok");
		}
		catch (Exception e)
		{
			System.out.println("error starting sensor");
		}

	}
	
	public void run()
	{
		// for norms
		sensorClient.addHandler("NODE_NORM", new ReadingHandler() {
			public void handleIncomingReading(String node, String rdf) {
				try	{
					System.out.println("received a msg on NODE_NORM");
					JsonReading jr = new JsonReading();
					jr.fromJSON(rdf);
					Value val = jr.findValue("STATE");
					System.out.println(val.m_object);
					parentObj.addData(val.m_object.toString());
					//m_percept.add(val.m_object.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
		try {
			
			sensorClient.subscribeAndCreate("NODE_NORM");
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + "NODE_NORM");
		}

		while (true)
		{
			
		}
	}
}
