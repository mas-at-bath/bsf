package edu.bath.openhab;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.awt.Color;
import java.util.Map;
import java.util.ArrayList;
import java.io.IOException;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class OpenhabInterface extends Sensor implements MqttCallback {

	private boolean alive = true;
	private long timeSentLastAOILightMessage = 0;
	private boolean askedStartupData = false;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";

	private long nanoToMili=1000000;
	private static String componentName="openhabBSF";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingOpenhabMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	//private final static ObjectMapper mapper = new ObjectMapper();

	private MqttClient mqttSensorConnection, mqttSenderClient;
	private String broker="192.168.0.8";
	private String clientID = "mqttBSFbridge";
	private String mqttNode = "/openHAB/#";
	private MemoryPersistence persistence = new MemoryPersistence();
	private int qos = 0;
	

	public OpenhabInterface(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws Exception {
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
			}
		}
		catch (Exception e) 
		{
			System.out.println("Error loading config.txt file");
			e.printStackTrace();
		}

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/OpenhabBSF/Openhab");
		OpenhabInterface ps = new OpenhabInterface(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/AOISensors", "http://127.0.0.1/OpenhabBSF/Openhab", true, 0);

		Thread.currentThread().sleep(1000);
		System.out.println("Created openhabREST Sensor, now entering its logic!");
		
		ps.run();
	}
	
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws Exception {	
		
		try {
			sensorClient = new SensorMQTTClient(XMPPServer, componentName+"-receiver");
			System.out.println("Guess sensor connected OK then!");
		} catch (Exception e1) {
			System.out.println("Exception in establishing client.");
			e1.printStackTrace();
		}

		startupTime=System.currentTimeMillis();

		sensorClient.addHandler(homeSensors, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
					try {
						pendingOpenhabMessages.put(rdf);
					}
					catch (Exception e)
					{
						System.out.println("Error adding new message to queue..");
						e.printStackTrace();
					}
				}
				//System.out.println(rdf);
			}
		});
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}

		try
		{
			mqttSensorConnection = new MqttClient("tcp://"+broker+":1883", clientID);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting MQTT client receiver: " + clientID + " to broker: "+broker);
			mqttSensorConnection.connect(connOpts);
			System.out.println("Connected MQTT client, subscribing to " + mqttNode);
			mqttSensorConnection.subscribe(mqttNode);
			mqttSensorConnection.setCallback(this);
		}
		catch(MqttException me) 
		{
			System.out.println("Crashed in MQTT subscriber connection");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
        	}
		try
		{
			System.out.println("Connecting MQTT client sender..");
			mqttSenderClient = new MqttClient("tcp://"+broker+":1883", clientID+"-sender", persistence);
            		MqttConnectOptions connSendOpts = new MqttConnectOptions();
            		connSendOpts.setCleanSession(true);
            		System.out.println("Connecting sender to broker: "+broker);
            		mqttSenderClient.connect(connSendOpts);
            		System.out.println("Connected sender");
		}
		catch(MqttException me) 
		{
			System.out.println("Crashed in MQTT sender connection");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
        	}



		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();

			try 
			{
				if(sensorClient.checkReconnect())
				{
					sensorClient.subscribe(homeSensors);
				}
			} catch (Exception e1) {
				System.out.println("Couldn't reconnect to " + homeSensors);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			String rdfOpenhab = pendingOpenhabMessages.poll();
			while (rdfOpenhab != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfOpenhab);
					String takenBy = dr.getTakenBy();
					if (takenBy.contains("houseSensors"))
					{
						String[] fullName = takenBy.split("/");
						String publisherName = fullName[fullName.length -1];
						for (Value foundVal : dr.findValues(null,null,null))
						{
							boolean processedMsg = false;
							String predName = (String)foundVal.predicate;
							System.out.println("For: " + publisherName + " got " + predName);
							if (predName.equals("http://127.0.0.1/sensors/types#BMP80Temp"))
							{
								Double tempVal = (Double)foundVal.object;
								String strMsg = Double.toString(tempVal);
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/Temperature_GF_Bedroom1/state", message);
								
							}		
							else if (predName.equals("http://127.0.0.1/sensors/types#DHT22humidity"))
							{
								//Float tempVal = (Float)foundVal.object;
								String strMsg = (String)foundVal.object;
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/Humidity_GF_Bedroom1/state", message);
								
							}
							else if (predName.equals("http://127.0.0.1/sensors/types#light"))
							{
								Float tempVal = (Float)foundVal.object;
								String strMsg = Float.toString(tempVal);
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/Light_GF_Bedroom1/state", message);
								
							}
							else if (predName.equals("http://127.0.0.1/sensors/types#GasNO"))
							{
								Float tempVal = (Float)foundVal.object;
								String strMsg = Float.toString(tempVal);
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/GasNO_GF_Bedroom1/state", message);
								
							}
							else if (predName.equals("http://127.0.0.1/sensors/types#GasCO"))
							{
								Float tempVal = (Float)foundVal.object;
								String strMsg = Float.toString(tempVal);
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/GasCO_GF_Bedroom1/state", message);
								
							}
							else if (predName.equals("http://127.0.0.1/sensors/types#BMP80Pressure"))
							{
								Double tempVal = (Double)foundVal.object;
								String strMsg = Double.toString(tempVal);
								System.out.println("Publishing message: "+strMsg);
            							MqttMessage message = new MqttMessage(strMsg.getBytes());
            							message.setQos(qos);
            							mqttSenderClient.publish("/openHAB/stateSubscriber/AirPressure_GF_Bedroom1/state", message);
								
							}
							//System.out.println("got Openhab RDF message!");
							//Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/requests/lights", null);
							//if(reqVal != null) 
							//{
							//	String reqAction = (String)reqVal.object;
							//}
						}
					}
				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfOpenhab = pendingOpenhabMessages.poll();
			}

			try
			{
				Thread.sleep(1000);
			}
			catch (Exception es)
			{
				es.printStackTrace();
			}


		}
					
	}

	public void convertAndSendOpenHABMsg(String fullNode, String msgState) {

		String[] nodeArray = fullNode.split("/");
		//System.out.println(nodeArray[1]);
		if ((nodeArray[2].equals("commandUpdates")) && (nodeArray.length > 3))
		{		
			String itemName = nodeArray[2];

			try 
			{				
				DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
				testReading.setTakenBy("http://127.0.0.1/components/"+componentName);
				testReading.addDataValue(null, "http://127.0.0.1/components/" + itemName , msgState, false);
				publish(testReading);
			} 							
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Not sure how to process " + msgState + " from " + fullNode);
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
	    System.out.println("MQTT Connection lost!!");

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception 
	{
	 	System.out.println("received MQTT message " + message + " from " + topic);  
		convertAndSendOpenHABMsg(topic,message.toString());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	    // TODO Auto-generated method stub

	}

}
