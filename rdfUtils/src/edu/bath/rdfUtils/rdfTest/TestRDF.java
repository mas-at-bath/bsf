package edu.bath.rdfUtils.rdfTest;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.jivesoftware.smack.XMPPException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.*;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import edu.bath.sensorframework.JsonReading;
import org.eclipse.paho.client.mqttv3.MqttException;

//CHANGE TO 
//nanotime

public class TestRDF {
	private boolean alive = true;
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static WorkerNonThreadSender testNonThreadSender, simSender;
	private static WorkerNonThreadMQTTSender testMQTTSender;
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static long intervalTime=1;
	private static String testMode = "empty";
	private Thread xmppThread;
	private static FileWriter fw,fw2, fw3;
	private long lastTime=0;
	private long lastTimeNano=0;
	private long ownTimer=0;
	private int counter=0;
	private int perSecCounter=0;
	private static boolean noSleep = false;
	private static SensorClient sensorClient;
	//private static SensorMQTTClient sensorMQTTClient;
	private double lastReadingUpdateTime = 0;
	private static boolean testJSON=false;
	private static boolean testRDF=false;
	private static boolean testMQTTRDF=false;
	private static boolean testMQTTJSON=false;
	private static final long nanoToMili=1000000;
	private static boolean writeToFile=false;
	private static boolean testRates=false;
	private static boolean testMQTTRates=false;
	private static int testDuration=10;
	
	public static void main(String[] args) throws Exception {
	
	if (args.length == 1)
	{
		String argMode = args[0];
		if (argMode.equals("subscribeJSON")) 
		{ 
			testMode = argMode;
			testJSON=true;
			if (writeToFile) {	
				fw = new FileWriter("subRes.txt",false);
				fw2 = new FileWriter("msgPerSec.txt",false);
			}
		}
		else if (argMode.equals("subscribeRDF")) 
		{ 
			testMode = argMode;
			testRDF=true;
			if (writeToFile) {
				fw = new FileWriter("subRes.txt",false);
				fw2 = new FileWriter("msgPerSec.txt",false);
			}
		}
		else if (argMode.equals("subscribeMQTTRDF")) 
		{ 
			testMode = argMode;
			testMQTTRDF=true;
			if (writeToFile) {
				fw = new FileWriter("subRes.txt",false);
				fw2 = new FileWriter("msgPerSec.txt",false);
			}
		}
		else if (argMode.equals("testMode-RDF"))
		{
			testRates=true;
			testRDF=true;
			testMode="subscribeRDF";
		}
		else if (argMode.equals("testMode-JSON"))
		{
			testRates=true;
			testJSON=true;
			testMode="subscribeRDF";
		}
		else if (argMode.equals("testMode-MQTT-RDF"))
		{
			testMQTTRates=true;
			testMQTTRDF=true;
			testMode="subscribeMQTTRDF";
		}
		else if (argMode.equals("testMode-MQTT-JSON"))
		{
			testMQTTRates=true;
			testMQTTJSON=true;
			testMode="subscribeMQTTRDF";
		}

		else
		{
			System.out.println("Didn't understand argument, should be publishRDF, publishJSON, publishMQTTRDF or subscribeRDF,subscribeJSON, subscribeMQTTRDF mode, for publish mode specify number of messages to send e.g. 'publish 50'");
			System.exit(0);
		}
		//System.out.println("Running in " +args[0]);
	}
	else if (args.length == 2)
	{
		String argMode = args[0];
		if (argMode.equals("publishJSON")) 
		{ 
			testMode = argMode;
			testJSON=true;
			if (writeToFile) {
				fw3 = new FileWriter("sentPerSec.txt",false);
			}
		}
		else if (argMode.equals("publishRDF")) 
		{ 
			testMode = argMode;
			testRDF=true;
			if (writeToFile) {
				fw3 = new FileWriter("sentPerSec.txt",false);
			}
		}
		else if (argMode.equals("publishMQTTRDF")) 
		{ 
			testMode = argMode;
			testMQTTRDF=true;
			if (writeToFile) {
				fw3 = new FileWriter("sentPerSec.txt",false);
			}
		}

		else
		{
			System.out.println("Didn't understand parameters, publish mode requires messages per second to send, e.g 'publishRDF 50' or 'publishJSON max' for maximum");
			System.exit(0);
		}

		if (args[1].equals("max"))
		{
			noSleep=true;
		}
		else
		{
			try 
			{
				int rateArg = Integer.parseInt(args[1]);
				
				long fullIntervalTime = 1000 * nanoToMili;
				intervalTime = (fullIntervalTime/rateArg);
				System.out.println("so thats one message every " + (fullIntervalTime/rateArg)/nanoToMili + " ms");
			
			} 
			catch (NumberFormatException e) 
			{
				System.err.println("Messages to send per second entered must be an integer");
				System.exit(0);
			}
		}
	}
	else if (testRates || testMQTTRates)
	{
		System.out.println("test mode");
		//testRDF=true;
	}
	else
	{
		System.out.println("Need to specify whether running in publish or subscribe mode, e.g. testRDF publishRDF 50");
		System.exit(0);
	}
		
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
		
	if (testMode.equals("publishJSON") || testMode.equals("publishRDF") || testRates )
	{
		try {
			testNonThreadSender = new WorkerNonThreadSender(XMPPServer, "rdfTest-sender", "jasonpassword", jasonSensorVehicles, "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/rdfTest-vehicle");
			System.out.println("Created vehicleSender, now entering its logic!");
			simSender = new WorkerNonThreadSender(XMPPServer, "rdfTest-simsender", "jasonpassword", "simStateSensor", "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/rdfTest-vehicle");
			System.out.println("Created simSender, now entering its logic!");
		}
		catch (Exception e) 
		{
			System.out.println("couldn't start thread senders");
			System.out.println(e.getStackTrace());
		}
	}
	
	if (testMode.equals("publishMQTTRDF") || testMQTTRates)
	{
		testMQTTSender = new WorkerNonThreadMQTTSender(XMPPServer, "rdfTest-sender", "jasonpassword", jasonSensorVehicles, "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/rdfTest-vehicle");
		System.out.println("Created MQTT Sender, now entering its logic!");
	}
	
	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() 
		{ 
			System.out.println("Shutting down..");
			try {
				if (writeToFile) {
					fw.close();
					fw2.close();
					fw3.close();
				}
			}
			catch (Exception er) {} ;
		 }
	});
	
	TestRDF testAgent = new TestRDF();
	testAgent.run();
	
	}
	
	public TestRDF() {
	}
	
	//OK below it might be neater to all use the same method from child threads, but a) then we've maybe got sync issues,
	//and b) thats not representative of whats happening in the real sim software so not a fair test comparison
	
	public void processXMPPData(String newItem, String vehicleName, Long timeValue)
	{
		if (writeToFile) {
			Long deltaTime = System.nanoTime() - timeValue*nanoToMili;
			try {fw.write("vehicle-XMPPData, time, "+deltaTime+" \n");}
			catch (Exception e) {System.out.println("error writing to file");}
		}
		counter++;
		perSecCounter++;
	}
	
	public void run() 
	{
		if (testMode.equals("subscribeJSON") || testMode.equals("subscribeRDF") || testMode.equals("subscribeMQTTRDF") || testRates || testMQTTRates)
		{
			if (testMode.equals("subscribeJSON") || testMode.equals("subscribeRDF") || testRates)	
			{
				while(sensorClient == null) 
				{
					try 
					{
						sensorClient = new SensorXMPPClient(XMPPServer, "rdfTest", "jasonpassword");
						System.out.println("connected subscriber");
					} 
					catch (Exception e1) 
					{
						System.out.println("Exception in establishing client.");
						e1.printStackTrace();
					}
				}
			}
			else if (testMode.equals("subscribeMQTTRDF") || testMQTTRates)
			{
				System.out.println("Testing MQTT subscription");
				try {
					sensorClient = new SensorMQTTClient(XMPPServer, "rdfTest-subscriber");
					System.out.println("connected subscriber");
				} catch (Exception e1) {
					System.out.println("Exception in establishing MQTT client.");
					e1.printStackTrace();
				}
			}
		
		
			if (testRDF || testMQTTRDF) 
			{
				sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() 
				{
					@Override
					public void handleIncomingReading(String node, String rdf) 
					{
						try 
						{
							DataReading dr = DataReading.fromRDF(rdf);
							if (dr.getLocatedAt().equals("http://127.0.0.1/vehicleSensors"))
							{
								List<DataReading.Value> drValues = dr.findValues(null, null, null);
								int drSize = drValues.size();
								if (drSize == 1)
								{
									DataReading.Value spatialVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null);
									if(spatialVal != null) 
									{
										if (dr.getTimestamp() <= lastReadingUpdateTime)
										{
											//System.out.println("XXXXXXXXXXXXX Received DataReading out of order!!!! XXXXXXXXXXXXXXXX");
										}
										lastReadingUpdateTime = dr.getTimestamp();
										processXMPPData("spatial,"+spatialVal.object, dr.getTakenBy(), dr.getTimestamp());        
									}
									else
									{
										DataReading.Value spatialValFailback = dr.findFirstValue(null, null, null);
										if(spatialValFailback != null) 
										{
											if (dr.getTimestamp() <= lastReadingUpdateTime)
											{
												//System.out.println("XXXXXXXXXXXXX Received DataReading out of order!!!! XXXXXXXXXXXXXXXX");
											}
											lastReadingUpdateTime = dr.getTimestamp();
											counter++;     
										}
									}
								}
							}
						}catch(Exception e) {}
					}
				});
			}

			else if (testJSON || testMQTTJSON) 
			{
				sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() 
				{
					@Override
					public void handleIncomingReading(String node, String rdf) 
					{
						try 
						{
							JsonReading jr = new JsonReading();
							jr.fromJSON(rdf);
							JsonReading.Value val = jr.findValue("takenAt");
							String takenAtStr = val.m_object.toString();
							long takenAtTime = Long.valueOf(takenAtStr).longValue();
							val = jr.findValue("takenBy");
							String takenByStr = val.m_object.toString();
							val = jr.findValue("object");
							String objStr = val.m_object.toString();
							processXMPPData("spatial,"+objStr, takenByStr, takenAtTime);
						}catch(Exception e) {}
					}
				});
			}
			System.out.println("testMode:" + testMode);
			try {
				sensorClient.subscribe(jasonSensorVehicles);
				System.out.println("subscribed to " + jasonSensorVehicles);
			} 
			catch (Exception e1) {
				System.out.println("Exception while subscribing to sensor.");
				e1.printStackTrace();
			}
		}
	
		if (testRates || testMQTTRates)
		{
			//long newTime = System.currentTimeMillis();
			long newTime = System.nanoTime();
			int sentCount = 0;
			perSecCounter=0;
			long checkEachSecondTime = System.nanoTime();
			System.out.println("Running 10 second of sending at max rate, checking received quantity matches");
			while (	(newTime + testDuration*1000*nanoToMili) > System.nanoTime())
			{
				if (testRates) 
				{
					if (testRDF)
					{
						testNonThreadSender.generateAndSendTestMsg();
					}
					else if (testJSON)
					{
						testNonThreadSender.generateAndSendTestJSONMsg();
					}
				}
				else if (testMQTTRates)
				{
					if (testMQTTRDF)
					{					
						testMQTTSender.generateAndSendTestMsg();
					}
					else if (testMQTTJSON)
					{
						testMQTTSender.generateAndSendTestJSONMsg();
					}
				}
				sentCount++;
				if ((checkEachSecondTime + 1000*nanoToMili) < System.nanoTime())
				{
					System.out.println("Quantity per sec received was: " + perSecCounter);
					perSecCounter=0;
					checkEachSecondTime = System.nanoTime();
				}
			}
			System.out.println("Done...");
			//rough guess of 5 seconds to process the backlog of built up messages
			//testWait(10*1000 * nanoToMili);
			long checkEachSecondTime2 = System.nanoTime();
			boolean rateLimitTriggered=false;
			while (!rateLimitTriggered)
			{
				if ((checkEachSecondTime + 1000*nanoToMili) < System.nanoTime())
				{
					System.out.println("Quantity per sec received was: " + perSecCounter);
					//stop checking if messages per second drops below 10
					if (perSecCounter < 10)
					{
						rateLimitTriggered=true;
					}
					else
					{
						perSecCounter=0;
						checkEachSecondTime = System.nanoTime();
						testWait(1*1000 * nanoToMili);
					}
				}
			}

			System.out.println("TestRDF Rate: " + testDuration + " second test, sent " + sentCount + " and received " + counter);
		
			counter=0;
			sentCount=0;
			while (sentCount < 5000)
			{
				if (testRDF) {
					testNonThreadSender.generateAndSendTestMsg();
				}
				if (testJSON) {
					testNonThreadSender.generateAndSendTestJSONMsg();
				}
				else if (testMQTTRDF) {						
					testMQTTSender.generateAndSendTestMsg();
				}
				else if (testMQTTJSON)
				{
					testMQTTSender.generateAndSendTestJSONMsg();
				}
				sentCount++;
			}
			long checkTime = System.nanoTime();
			while ((counter < 5000) && ((checkTime + 60*1000*nanoToMili) > System.nanoTime()))
			{
				//System.out.println(counter);
				testWait(1*1000 * nanoToMili);
			}
			
			if (counter == 5000)
			{
				System.out.println("TestRDF Loss max 60s: Sent 5000 and received " + counter + " - OK");
			}
			else
			{
				System.out.println("TestRDF Loss max 60s: Sent 5000 and received " + counter + " - FAIL");
			}

			//need to quit with MQTT, maybe thread/client issue..?
			System.out.println("Quitting...");
			System.exit(0);

		}
		else
		{
			while(alive) 
			{
				if (testMode.equals("publishRDF") || testMode.equals("publishJSON") || testMode.equals("publishMQTTRDF") || testMode.equals("publishMQTTJSON") )
				{
					try 
					{
						if (!noSleep)
						{
							long newTime = System.nanoTime();
							if (lastTime+intervalTime < newTime)
							{	
								if (testRDF) {	
									testNonThreadSender.generateAndSendTestMsg();
								}
								else if (testMQTTRDF)
								{					
									testMQTTSender.generateAndSendTestMsg();
								}
								else if (testMQTTJSON)
								{
									testMQTTSender.generateAndSendTestJSONMsg();
								}
								else if (testJSON) {	
									testNonThreadSender.generateAndSendTestJSONMsg();
								}
								counter++;
								lastTime=newTime;
								testWait(intervalTime);				
							}
						}
						else
						{
							if (testRDF) {
								testNonThreadSender.generateAndSendTestMsg();
							}
							else if (testMQTTRDF) {	
								testMQTTSender.generateAndSendTestMsg();
							}
							else if (testMQTTJSON)
							{
								testMQTTSender.generateAndSendTestJSONMsg();
							}
							else if (testJSON) {
								testNonThreadSender.generateAndSendTestJSONMsg();
							}
							counter++;
						}
					
						long newTimeNano = System.nanoTime();
						long fullDig=1000*nanoToMili;
						if (lastTimeNano+fullDig <= newTimeNano)
						{
							System.out.println("sent " + counter + " messages per second with " + intervalTime + " delay");
							if (testRDF) {						
								simSender.sendPublishedRate(counter);
							}
						
							if (writeToFile) {
								fw3.write(System.currentTimeMillis() + ", " + counter);
								fw3.close();
								fw3 = new FileWriter("sentPerSec.txt",true);
							}
							lastTimeNano=newTimeNano;
							counter=0;
						}
					}
					catch (Exception ee1) 
					{
						ee1.printStackTrace();
					}
				}
				else
				{
					long newTime = System.currentTimeMillis();
					if (lastTime+1000 <= newTime)
					{
						try {
							//oddly quitting if ran from ant doesn't seem to call the shutdown hook,
							//so closing the files each second so data isn't lost..
							if (writeToFile) {
								fw2.write("XMPPData, count, " + newTime + "m "+counter+" \n");
								fw.close();
								fw2.close();
								fw = new FileWriter("subRes.txt",true);
								fw2 = new FileWriter("msgPerSec.txt",true);
							}
						}
						catch (Exception eee1) 
						{
							eee1.printStackTrace();
						}
						System.out.println(counter + " messages per second");
						lastTime=newTime;
						counter=0;
					}
				}
			}
		}
	}
	
	public void testWait(long delayVal)
	{
		long start = System.nanoTime();
		long end=0;
		do{
			end = System.nanoTime();
		}while(start + delayVal >= end);
	}
}
