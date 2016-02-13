package edu.bath.xmppVehicle;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import javax.vecmath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import math.geom2d.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class remoteXMPPSim extends Sensor {
	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private String temp;
	private final double updateRate = 0.5f;
	private Double currentSpeed=0.0;
	private static Double currentOrientation=180.0;
	private static Point3d currentXYZLocation = new Point3d(0, 0, 0);
	private static String vehicleName="jasonreceiver";
	private static String XMPPServer = "127.0.0.1";
	private static double lastUpdateTime=0;
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static remoteXMPPSim ps;
	
	public remoteXMPPSim(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public remoteXMPPSim(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public static void main(String[] args) throws Exception {
	
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));
			String line;
			while((line = br.readLine()) != null) 
			{
				if (line.contains("OPENFIRE"))
				{
					String[] configArray = line.split("=");
					XMPPServer = configArray[1];
					//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
				if (line.contains("COMMUNICATION"))
				{
					String[] configArray = line.split("=");
					if(configArray[1].equals("MQTT"))
					{
						useMQTT=true;
					}
					else if(configArray[1].equals("XMPP"))
					{
						useXMPP=true;
					}
					//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
			}
			if (!useMQTT && !useXMPP)
			{
				System.out.println("no COMMUNICATION value found in config.txt, should be = MQTT or XMPP");
				System.exit(1);
			}
		}
		catch (Exception e) {System.out.println("Error loading config.txt file");}
	
		if (args.length == 0)
		{
			System.out.println("Using defaults: " + XMPPServer + ", central, jasonpassword, jasonSensor, http://127.0.0.1/vehicleSensors, http://127.0.0.1/vehicleSensors/vehicle1");
			if (useXMPP)
			{
				ps = new remoteXMPPSim(XMPPServer, "test1-vehicle", "jasonpassword", jasonSensorVehicles , "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/test1-vehicle");
			}
			else if (useMQTT)
			{
				ps = new remoteXMPPSim(XMPPServer, "test1-vehicle", "jasonpassword", jasonSensorVehicles , "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/test1-vehicle", true, 0);
			}
			vehicleName="central";
			Thread.currentThread().sleep(1000);
			System.out.println("Created jasonSensor, now entering its logic!");
			ps.run();
		}
		if (args.length == 1)
		{
			System.out.println("Using defaults with naming of " +args[0]);
			if (useXMPP)
			{
				ps = new remoteXMPPSim(XMPPServer, args[0]+"-vehicle", "jasonpassword", jasonSensorVehicles , "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/"+args[0]+"-vehicle");
			}
			else if (useMQTT)
			{
				ps = new remoteXMPPSim(XMPPServer, args[0]+"-vehicle", "jasonpassword", jasonSensorVehicles , "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/"+args[0]+"-vehicle", true, 0);
			}
			vehicleName=args[0];
			if (vehicleName.indexOf("1") > 0)
			{
				System.out.println("assigning position 1");
				currentXYZLocation.x = -11.00f;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = 21.00f;
				currentOrientation = 311d;
			}
			else if (vehicleName.indexOf("2") > 0)
			{
				System.out.println("assigning position 2");
				currentXYZLocation.x = -22.00f;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = 21.00f;
				currentOrientation = 0d;
			}
			else if (vehicleName.indexOf("3") > 0)
			{
				System.out.println("assigning position 3");
				currentXYZLocation.x = -33.00f;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = 21.00f;
			}
			else if (vehicleName.indexOf("4") > 0)
			{
				System.out.println("assigning position 4");
				currentXYZLocation.x = -44.00f;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = 21.00f;
			}
			else if (vehicleName.indexOf("5") > 0)
			{
				System.out.println("assigning position 5");
				currentXYZLocation.x = 351;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = -395f;
				currentOrientation = 90d;
			}
			else if (vehicleName.equals("centralMemberTest"))
			{
				System.out.println("assigning test vehicle default position");
				currentXYZLocation.x = -11.00f;
				currentXYZLocation.y = 0f;
				currentXYZLocation.z = 21.00f;
				currentOrientation = 311d;
			}
			
			Thread.currentThread().sleep(1000);
			System.out.println("Created jasonSensor, now entering its logic!");
			ps.run();
		}
		else if (args.length == 5)
		{
			if (useXMPP)
			{
				ps = new remoteXMPPSim(XMPPServer, args[0]+"-vehicle", args[1], args[2], args[3], args[4]);
			}
			else if (useMQTT)
			{
				ps = new remoteXMPPSim(XMPPServer, args[0]+"-vehicle", args[1], args[2], args[3], args[4], true, 0);
			}
			vehicleName=args[0];
			System.out.println("Created jasonSensor, now entering its logic!");
			ps.run();
		}
		else
		{
			System.out.println("Expecting 5 arguments: username, password, sensorName, location, locationName");
			System.out.println("e.g. central jasonpassword jasonSensor http://127.0.0.1/vehicleSensors http://127.0.0.1/vehicleSensors/vehicle1");
		}
	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException {
		System.out.println("Running as vehicle: " + vehicleName);
		if (useXMPP)
		{
			System.out.println("XMPP subscription");
			while(sensorClient == null) {
				try {
					sensorClient = new SensorXMPPClient(XMPPServer, vehicleName+"-vehicle-receiver", "jasonpassword");
					System.out.println("Guess sensor connected OK then!");
				} catch (XMPPException e1) {
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
				}
			}
		}
		else if (useMQTT)
		{
			System.out.println("MQTT subscription");
			try {
				sensorClient = new SensorMQTTClient(XMPPServer, vehicleName+"-vehicle-receiver");
				System.out.println("connected subscriber");
			} catch (Exception e1) {
				System.out.println("Exception in establishing MQTT client.");
				e1.printStackTrace();
			}
		}
		sensorClient.addHandler(jasonSensorVehiclesCmds, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try 
				{
					DataReading dr = DataReading.fromRDF(rdf);
					String takenBy = dr.getTakenBy();
				//	System.out.println("received " + takenBy);// + " and checking if contains http://127.0.0.1/agent/"+vehicleName);
					if (takenBy.equals("http://127.0.0.1/agent/"+vehicleName))
					{
                                                //pass the lastUpdateTime compared to this, is it less than or equal:w

                                                if ((dr.getTimestamp() < lastUpdateTime) && lastUpdateTime != 0)
                                                {
                                                        System.out.println("XXXXXXXX out of sync dr.timestamp XXXXXXXXXXX ");
                                                        System.out.println("Time stamp of dr is " + dr.getTimestamp());
                                                        System.out.println("Time stamp of last updated is " + lastUpdateTime);
                                                }
						else if (dr.getTimestamp() == lastUpdateTime)
						{
							System.out.println("maybe sync issue occuring");
						} 
						else
                                                {
                                                        //System.out.println("in sync");
                                                        //System.out.println("Time stamp of dr is " + dr.getTimestamp());
                                                        //System.out.println("Time stamp of last updated is " + lastUpdateTime);

                                                }
                                                lastUpdateTime = dr.getTimestamp();

						Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/request/setOrientation", null);
						if(reqVal != null) 
						{
							String reqMsg = (String)reqVal.object;
							//String[] orientationReq = reqMsg.split(",");
							Double newOrientation = Double.parseDouble(reqMsg);
							//System.out.println("AA turning to " + newOrientation);
							currentOrientation = newOrientation;
						}
			
						reqVal = dr.findFirstValue(null, "http://127.0.0.1/request/setSpeed", null);
						if(reqVal != null) 
						{
							String reqMsg = (String)reqVal.object;
							//String[] speedReq = reqMsg.split(",");
							Double newSpeed = Double.parseDouble(reqMsg);
							//System.out.println("AA changing speed to " + newSpeed);
							currentSpeed = newSpeed;
						}		
					}

				}
				catch(Exception e) 
				{
					System.out.print("error handling incoming..");
					e.printStackTrace();
				}
			}
		});
		try {
			sensorClient.subscribe(jasonSensorVehiclesCmds);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to " + jasonSensorVehiclesCmds);
			e1.printStackTrace();
		}
		//end rec section
		
		
		//given any requests for data that have come in, pop them into a message queue
		//include current pos info, and send those messages out
		System.out.println("in Run section");
		
//		final List<String> readings = Collections.synchronizedList(new ArrayList<String>(100));
		Runnable publishThread = new Runnable() {
			@Override
			public void run() {
				Point3d testLocation = new Point3d(0, 0, 0);
				while(alive) 
				{
					try 
					{
						if(sensorClient.checkReconnect())
						{
							sensorClient.subscribe(jasonSensorVehiclesCmds);
						}
					} 
					catch (Exception e1) 
					{
						System.out.println("Couldn't reconnect to " + jasonSensorVehiclesCmds);
						e1.printStackTrace();
						try 
						{
							System.out.println("trying to reconnect");
							Thread.sleep(30*1000);
						} catch (InterruptedException e) {}
						continue;
					}

					try 
					{
						DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
						testReading.setTakenBy("http://127.0.0.1/vehicles/"+vehicleName);
						String tidyPosInfo = currentXYZLocation.toString();
						String posResult = tidyPosInfo.substring(1, tidyPosInfo.length()-1);
						String spatialInfo = (posResult + "," + currentOrientation);
						testReading.addDataValue(null, "http://127.0.0.1/sensors/types#spatial", spatialInfo, false);
						System.out.println(testReading.toRDF());
						publish(testReading);

					} 
					catch (Exception e) 
					{
						System.out.println("error in main loop..");
						e.printStackTrace();
					}
					
					try 
					{
						Long sleepTime = (new Double(updateRate*1000)).longValue();
						Thread.sleep(sleepTime);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					//yeah not the tidiest, but it'll do, lets update vehicle position based on current
					//speed and orientation then
					Point2D startPosition = new Point2D(currentXYZLocation.x,currentXYZLocation.z);
					Point2D endPosition = startPosition.createPolar(startPosition, currentSpeed*updateRate, currentOrientation*0.01745329238474369f);
					currentXYZLocation.set(endPosition.getX(),0,endPosition.getY());

				}

			}
			
		};
		Thread pThread = new Thread(publishThread);
		pThread.start();
		
		while(alive) 
		{
			try 
			{
				Long sleepTime = (new Double(updateRate*1000)).longValue();
				Thread.sleep(sleepTime);
			} catch (Exception e) {
//				Log.console ("Exception reading data");
				continue;
			}
		}
		
		cleanup();
	}
}
