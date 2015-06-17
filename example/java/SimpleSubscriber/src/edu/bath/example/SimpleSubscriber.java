package edu.bath.example;

import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.JsonReading;
//import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.client.ReadingHandler;

public class SimpleSubscriber 
{
	/**
	 * @param args
	 * @throws XMPPException 
	 */
	public static void main(String[] args) throws XMPPException 
	{
		// TODO Auto-generated method stub
		SensorClient sc = new SensorClient("172.16.125.2", "user3", "bathstudent");
		
		sc.addHandler("BELIEF", new ReadingHandler() {
			public void handleIncomingReading (String node, String rdf) {
				try {
					JsonReading jr = new JsonReading();
					jr.fromJSON(rdf);
					Value val = jr.findValue("EVENT"); 
					
					if (val != null)
					{
						String temp = val.m_object.toString();
						System.out.println("simpleSubscriber" + temp);
					}
					/*
					DataReading dr = DataReading.fromRDF(rdf);
					Value val = dr.findFirstValue(null,"http://127.0.0.1/sensors/type#requestString", null);
					
					if (val != null)
					{
						String temp = (String) val.object.toString();
						System.out.println(temp);
					}
					*/
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		sc.subscribe("BELIEF");
		
		while (true)
		{
		
		}
	}
}
