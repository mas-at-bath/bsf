package edu.bath.rdfUtils.rdfSender;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;

import java.io.*;


public class SendRDF extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	//SensorClient sensorClient;
	private static String XMPPServer = "127.0.0.1";
	private static double lastUpdateTime=0;
	private static String nodeNameTest = "testSensor";

	
	public SendRDF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Enter node name: ");
		BufferedReader brKeyb = new BufferedReader(new InputStreamReader(System.in));
		nodeNameTest = (String) brKeyb.readLine();
		System.out.println("node: " + nodeNameTest);

		try {
			//get IP addressed from config file
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));


	       		String line;
	       		while((line = br.readLine()) != null) 
			{
		   		 if (line.contains("OPENFIRE"))
		   		 {
					String[] configArray = line.split("=");
					XMPPServer = configArray[1];
					System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
		    		}
			}
		}
		catch (Exception e) 
		{
			System.out.println("couldnt load config.txt for openfire net address");
		}

		SendRDF ps = new SendRDF(XMPPServer, "debug-sender", "jasonpassword", nodeNameTest, "http://127.0.0.1/components/rdfDebug", "http://127.0.0.1/components/rdfDebug/sender");
		Thread.currentThread().sleep(2000);
		System.out.println("Created debug sensor, now entering its logic!");
		ps.run();

	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException {
		/*while(sensorClient == null) {
			try {
				sensorClient = new SensorClient(XMPPServer, "debug-sender", "jasonpassword");
				System.out.println("Guess sensor connected OK then!");
			} catch (XMPPException e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}*/

		System.out.println("in Run section");

		while(alive) 
		{
			/*try 
			{
				if(sensorClient.checkReconnect())
				{
					sensorClient.subscribeAndCreate(nodeNameTest);
				}
			} catch (XMPPException e1) {
				System.out.println("Couldn't reconnect.");
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}*/

			try 
			{
				//System.out.println("sleeping...");
				//Thread.sleep(1000);
				System.out.println("Enter takenBy: ");
				BufferedReader brKeyb = new BufferedReader(new InputStreamReader(System.in));
				String takenByVal = (String) brKeyb.readLine();
				System.out.println("takenBy: " + takenByVal);

				DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
				testReading.setTakenBy(takenByVal);

				System.out.println("Enter predicate: ");
				//BufferedReader brKeyb = new BufferedReader(new InputStreamReader(System.in));
				String predVal = (String) brKeyb.readLine();
				System.out.println("pred: " + predVal);
				System.out.println("Enter object: ");
				//BufferedReader brKeyb = new BufferedReader(new InputStreamReader(System.in));
				String objVal = (String) brKeyb.readLine();
				System.out.println("obj: " + objVal);
				testReading.addDataValue(null, predVal, objVal, false);
				System.out.println(testReading);
				publish(testReading);
			} 
			catch (Exception e) {
				System.out.println("Hit exception:" );
				e.printStackTrace();
				continue;
			}
		}
		
		cleanup();
	}
}
