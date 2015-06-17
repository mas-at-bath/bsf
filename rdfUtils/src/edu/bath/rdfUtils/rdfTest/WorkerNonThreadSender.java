package edu.bath.rdfUtils.rdfTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.JsonReading;


import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import javax.vecmath.*;

public class WorkerNonThreadSender extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private String temp;
	private String URIRequestsURL = "http://127.0.0.1/request/";
	private boolean debug = true;
	private int currentO = 0;
	private String vehicleName="rdfTest";
	private static Point3d currentXYZLocation = new Point3d(0, 0, 0);
	private long lastTime=0;
	private int counter=0;
	
	private class RDFHalf
	{
		String pred;
		String val;
	}
	
	public WorkerNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}

	public void generateAndSendTestMsg() {

		if (currentO > 359 ) { currentO = 0; }
		currentO=currentO+10;

		try 
		{				
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/vehicles/"+vehicleName);
			String tidyPosInfo = currentXYZLocation.toString();
			String posResult = tidyPosInfo.substring(1, tidyPosInfo.length()-1);
			String spatialInfo = (posResult + "," + currentO);
			testReading.addDataValue(null, "http://127.0.0.1/sensors/types#spatial", spatialInfo, false);
			//System.out.println(testReading.toRDF());
			publish(testReading);
			counter++;
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void generateAndSendTestJSONMsg() {
		JsonReading jr = new JsonReading();
		jr.addValue("takenAt", System.currentTimeMillis());
		jr.addValue("takenBy", "http://127.0.0.1/vehicles/"+vehicleName);
		String tidyPosInfo = currentXYZLocation.toString();
		String posResult = tidyPosInfo.substring(1, tidyPosInfo.length()-1);
		String spatialInfo = (posResult + "," + currentO);
		jr.addValue("object", spatialInfo);
		try {
			publish(jr);
		}
		catch (Exception jrE) 
		{
			jrE.printStackTrace();
		}
	}
	
	public void sendPublishedRate(int quantity)
	{
		try {
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/vehicles/"+vehicleName);
			String msgInfo = quantity + ""; 
			testReading.addDataValue(null, "http://127.0.0.1/simDefinitions/publishedRate", msgInfo, false);
			publish(testReading);
		}
		catch (Exception e)
		{
			System.out.println("couldnt construct sim message count message");
		}
	}
}
