package edu.bath.sumoVehicles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import javax.vecmath.*;
import org.eclipse.paho.client.mqttv3.*;

public class WorkerNonThreadSender extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	private String URIRequestsURL = "http://127.0.0.1/request/";
	private String componentName="AOIVehCmd";
	private long lastTime=0;
	
	public WorkerNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public WorkerNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean mqtt, int qos) throws MqttException {
		super(serverAddress, id, password, nodeName, mqtt, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}

	public void generateAndSendMsg(String type, String msg) {

		try 
		{				
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/components/"+componentName);
			testReading.addDataValue(null, type, msg, false);
			System.out.println(testReading.toRDF());
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateAndSendMsg(DataReading testReading) 
	{
		try 
		{				
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
