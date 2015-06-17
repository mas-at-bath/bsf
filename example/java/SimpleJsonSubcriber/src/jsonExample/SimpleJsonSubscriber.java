package jsonExample;

import java.io.UnsupportedEncodingException;
import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;

/*this is test to use svn*/
public class SimpleJsonSubscriber {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws XMPPException 
	{
		// TODO Auto-generated method stub
		SensorClient sc = new SensorClient("172.16.125.2", "uniofbath1", "bathstudent");
		
		sc.addHandler("example", new ReadingHandler() {
			public void handleIncomingReading (String node, String rdf) {
				try {
					JsonReading jr = new JsonReading();
					jr.fromJSON(rdf);
					Value val = jr.findValue("takenAt");
					
					
					if (val != null)
					{
						Double takenAt = new Double(val.m_object.toString());
						long elapsed = System.currentTimeMillis() - takenAt.longValue();
						System.out.println(elapsed);
						//String temp = val.m_object.toString();
						//System.out.println("simpleJSONSubscriber" + temp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		sc.subscribe("example");
		
		while (true)
		{
		
		}
	}
}
