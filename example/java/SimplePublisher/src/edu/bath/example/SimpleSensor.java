package edu.bath.example;

import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.DataReading;

/* this is test for comment out and add some code in the existing source code */
public class SimpleSensor extends Sensor 
{
	private String m_handle = "http://127.0.0.1/sensors/location/simplesensor/handle";
	private String m_location = "http://127.0.0.1/sensors/location/simplesensor";
	private String m_drType = "http://127.0.0.1/type#Requests";
	private String m_drValue = "http://127.0.0.1/sensors/type#requestString";
	
	public SimpleSensor(String server, String username, String password, String nodename) throws XMPPException
	{
		super(server, username, password, nodename);
	}
	
	public void run() throws UnsupportedEncodingException
	{
		while (true)
		{
			DataReading dr = new DataReading(m_handle, m_location, System.currentTimeMillis());
			
			dr.setType(m_drType);
			dr.addDataValue(null, m_drValue, "This is test message", false);
			
			publish(dr);
			
			try
			{
				Thread.sleep(2 * 1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
