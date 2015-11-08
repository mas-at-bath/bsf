package edu.bath.rdfUtils.rdfDebug;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.client.SensorXMPPClient;
import edu.bath.sensorframework.client.SensorMQTTClient;
import edu.bath.sensorframework.sensor.*;
import javax.vecmath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import math.geom2d.*;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class DebugRDF extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static String XMPPServer = "127.0.0.1";
	private static double lastUpdateTime=0;
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String simSensorName = "simStateSensor";
	private static String homeSensors = "homeSensor";
	private static DebugRDF ps;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;

	
	public DebugRDF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public DebugRDF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}	

	public static void main(String[] args) throws Exception 
	{
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
		catch (Exception e) 
		{
			System.out.println("couldnt load config.txt for openfire net address");
		}

		if (useXMPP)
		{
			ps = new DebugRDF(XMPPServer, "debug", "jasonpassword", jasonSensorVehicles, "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehilceSensors/test1-vehicle");
		}			
		else if (useMQTT)
		{
			ps = new DebugRDF(XMPPServer, "debug", "jasonpassword", jasonSensorVehicles, "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehilceSensors/test1-vehicle", true, 0);
		}

		Thread.currentThread().sleep(1000);
		System.out.println("Created debug sensor, now entering its logic!");
		ps.run();

	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException 
	{
		if (useXMPP)
		{
			System.out.println("XMPP subscription");
			while(sensorClient == null) 
			{
				try {
					sensorClient = new SensorXMPPClient(XMPPServer, "debug-client", "jasonpassword");
					System.out.println("connected subscriber");
				} catch (XMPPException e1) 
				{
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
				}
			}
		}
		else if (useMQTT)
		{
			System.out.println("MQTT subscription");
			try {
				sensorClient = new SensorMQTTClient(XMPPServer, "debug-client");
				System.out.println("connected subscriber");
			} catch (Exception e1) {
				System.out.println("Exception in establishing MQTT client.");
				e1.printStackTrace();
			}
		}

		sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					System.out.println("got " + dr.getTakenBy() + " " + dr.getLocatedAt() + " " + dr.getType());
					//Value newVal = dr.findFirstValue(null,null,null);
					//System.out.println("pred: " + newVal.predicate.toString() + " subj: " + newVal.subject.toString() + " obj; " + newVal.object.toString());
					List<Value> foundVals = dr.findValues(null,null,null);
					for (Value foundV : foundVals)
					{
						System.out.println("pred: " + foundV.predicate.toString() + " subj: " + foundV.subject.toString() + " obj; " + foundV.object.toString());
					}
				}catch(Exception e) {}
			}
		});
		try {
			sensorClient.subscribe(jasonSensorVehicles);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor.");
			e1.printStackTrace();
		}
		
		sensorClient.addHandler(simSensorName, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try {
					DataReading dr = DataReading.fromRDF(rdf);;
					System.out.println("got " + dr.getTakenBy() + " " + dr.getLocatedAt() + " " + dr.getType());
					//Value newVal = dr.findFirstValue(null,null,null);
					//System.out.println("pred: " + newVal.predicate.toString() + " subj: " + newVal.subject.toString() + " obj; " + newVal.object.toString());
					List<Value> foundVals = dr.findValues(null,null,null);
					for (Value foundV : foundVals)
					{
						System.out.println("pred: " + foundV.predicate.toString() + " subj: " + foundV.subject.toString() + " obj; " + foundV.object.toString());
					}
				}catch(Exception e) {}
			}
		});
		try {
			sensorClient.subscribe(simSensorName);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor.");
			e1.printStackTrace();
		}
		
		sensorClient.addHandler(jasonSensorVehiclesCmds, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					System.out.println("got " + dr.getTakenBy() + " " + dr.getLocatedAt() + " " + dr.getType());
					//Value newVal = dr.findFirstValue(null,null,null);
					//System.out.println("pred: " + newVal.predicate.toString() + " subj: " + newVal.subject.toString() + " obj; " + newVal.object.toString());
					List<Value> foundVals = dr.findValues(null,null,null);
					for (Value foundV : foundVals)
					{
						System.out.println("pred: " + foundV.predicate.toString() + " subj: " + foundV.subject.toString() + " obj; " + foundV.object.toString());
					}
				}catch(Exception e) {}
			}
		});
		try {
			sensorClient.subscribe(jasonSensorVehiclesCmds);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor.");
			e1.printStackTrace();
		}

		sensorClient.addHandler(homeSensors, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					System.out.println(homeSensors +" got " + dr.getTakenBy() + " " + dr.getLocatedAt() + " " + dr.getType());
					//Value newVal = dr.findFirstValue(null,null,null);
					//System.out.println("pred: " + newVal.predicate.toString() + " subj: " + newVal.subject.toString() + " obj; " + newVal.object.toString());
					List<Value> foundVals = dr.findValues(null,null,null);
					for (Value foundV : foundVals)
					{
						System.out.println("pred: " + foundV.predicate.toString() + " subj: " + foundV.subject.toString() + " obj; " + foundV.object.toString());
					}
				}catch(Exception e) {}
			}
		});
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to home sensors.");
			e1.printStackTrace();
		}

		System.out.println("in Run section");

		while(alive) 
		{
			try 
			{
				if(sensorClient.checkReconnect())
				{
					sensorClient.subscribe(jasonSensorVehicles);
				}
			} catch (Exception e1) {
				System.out.println("Couldn't reconnect.");
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		cleanup();
	}
}
