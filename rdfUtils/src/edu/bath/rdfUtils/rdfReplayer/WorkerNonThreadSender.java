package edu.bath.rdfUtils.rdfReplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;

public class WorkerNonThreadSender extends Sensor   {

	private boolean alive = true;
	SensorClient sensorClient;
	private ArrayList<DataReading> messageStore;
	
	public WorkerNonThreadSender(String serverAddress, String id, String password, String nodeName) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		messageStore = new ArrayList<DataReading>();
	}
	

	public synchronized void addMessageToSend(DataReading drSend) 
	{
		synchronized(messageStore)
		{
			messageStore.add(drSend);
		}
	}

	public void send() {
		synchronized(messageStore)
		{
			for (DataReading msg : messageStore) 
			{
				try 
				{
					publish(msg);
				} 							
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			messageStore.clear();
		}
	}
				
}
