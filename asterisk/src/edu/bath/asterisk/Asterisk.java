package edu.bath.AsteriskBSF;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;


public class Asterisk extends Sensor implements ManagerEventListener {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";
	private static String asteriskNodeName = "asterisk";
	private long nanoToMili=1000000;
	private static String componentName="asterisk";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingAsteriskMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	private String incomingNumber="";
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static Asterisk ps;

	public Asterisk(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public Asterisk(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
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
		catch (Exception e) 
		{
			System.out.println("Error loading config.txt file");
			e.printStackTrace();
		}

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/Phone/Asterisk");
		if (useXMPP)
		{
			ps = new Asterisk(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/Phone", "http://127.0.0.1/Phone/Asterisk");
		}
		else if (useMQTT)
		{
			ps = new Asterisk(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/Phone", "http://127.0.0.1/Phone/Asterisk", true, 0);
		}
		Thread.currentThread().sleep(1000);
		System.out.println("Created asteriskSensor, now entering its logic!");
		
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
			while(sensorClient == null) {
				try {
					sensorClient = new SensorXMPPClient(XMPPServer, componentName+"-receiver", "jasonpassword");
					System.out.println("Guess sensor connected OK then!");
				} catch (Exception e1) {
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
				}
			}
		}
		else if (useMQTT)
		{
			System.out.println("MQTT subscription");
			try {
				sensorClient = new SensorMQTTClient(XMPPServer, componentName+"-receiver");
				System.out.println("Guess sensor connected OK then!");
			} catch (Exception e1) 
			{
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
			}
		}

		startupTime=System.currentTimeMillis();

		sensorClient.addHandler(asteriskNodeName, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
					try {
						pendingAsteriskMessages.put(rdf);
					}
					catch (Exception e)
					{
						System.out.println("Error adding new message to queue..");
						e.printStackTrace();
					}
				}
			}
		});
		System.out.println("Added handler for " + asteriskNodeName);
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}

		System.out.println("trying to connect to asterisk server...");
		ManagerConnectionFactory factory = new ManagerConnectionFactory("192.168.0.95", "manager", "jas0npassw0rd");
		ManagerConnection managerConnection = factory.createManagerConnection();
		try
		{
			managerConnection.addEventListener(this);
			managerConnection.login();
			System.out.println("Asterisk connection state: " + managerConnection.getState());
			System.out.println("connected OK i think..");
		}
		catch (Exception asE)
		{
			System.out.println("error connecting to asterisk server");
			asE.printStackTrace();
		}

		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();

			try {
				if(sensorClient.checkReconnect())
				sensorClient.subscribe(homeSensors);
			} catch (Exception e1) {
				System.out.println("Couldn't reconnect to " + homeSensors);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			String rdfAsterisk = pendingAsteriskMessages.poll();
			while (rdfAsterisk != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfAsterisk);
					String takenBy = dr.getTakenBy();
					System.out.println("got Asterisk RDF message!");

					Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/requests/tv", null);
					if(reqVal != null) 
					{
						String reqAction = (String)reqVal.object;

					}

				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfAsterisk = pendingAsteriskMessages.poll();
				
			}

			waitUntil(cycleTime*nanoToMili);

		}
					
	}	

    	public void onManagerEvent(ManagerEvent event)
    	{
       		// just print received events
        	//System.out.println("EVENT IS: " + event);
		String event_name = event.getClass().getSimpleName();
		//System.out.println("EVENT NAME: " + event_name);
		if((event_name.equals("DialEvent")) && (event.toString().startsWith("org.asteriskjava.manager.event.DialEvent")))
		{
			DialEvent e=(DialEvent)event;
			if (e.getCallerId() != null)
			{
				if (incomingNumber.equals(e.getCallerId()))
				{
					//ignore double first register
				}
				else
				{	
					System.out.println("Incoming call from: " + e.getCallerId());
					incomingNumber = e.getCallerId();
					generateAndSendMsg("http://127.0.0.1/detections/phone/ringing",  e.getCallerId());
					//System.out.println("EVENT IS: " + event);
				}
			}
         	}
		else if(event_name.equals("HangupEvent"))
		{
			HangupEvent h=(HangupEvent)event;
			if (h.getCauseTxt().equals("Answered elsewhere"))
			{
				//System.out.println("another phoned picked it up, dont do anything..");
				//System.out.println("EVENT IS: " + event);
				//System.out.println("cause: " + h.getCauseTxt());
			}	
			else if ((h.getCauseTxt().equals("Normal Clearing")) && (h.getCallerId().equals(incomingNumber)))
			{	
				System.out.println("Call hung up");
				//System.out.println("EVENT IS: " + event);
				//System.out.println("cause: " + h.getCauseTxt());
				incomingNumber="";
			}
			else if ((h.getCauseTxt().equals("User alerting, no answer")) && (h.getCallerId().equals(incomingNumber)))
			{	
				System.out.println("Went to answerphone probably");
				//System.out.println("EVENT IS: " + event);
				//System.out.println("cause: " + h.getCauseTxt());
				incomingNumber="";
				generateAndSendMsg("http://127.0.0.1/detections/phone/ringing",  "Stopped");
			}
			else if ((h.getCauseTxt().equals("User busy")) && (h.getCallerId().equals(incomingNumber)))
			{	
				System.out.println("Was hungup on");
				//System.out.println("EVENT IS: " + event);
				//System.out.println("cause: " + h.getCauseTxt());
				incomingNumber="";
				generateAndSendMsg("http://127.0.0.1/detections/phone/ringing",  "Stopped");
			}
			else
			{
				/*System.out.println("unhandled hangup");
				{
					System.out.println("EVENT IS: " + event);
					System.out.println("cause: " + h.getCauseTxt());
				}*/
			}
         	}
		else if (event_name.equals("NewStateEvent"))
		{
			NewStateEvent n=(NewStateEvent)event;
			//System.out.println("got NewStateEvent, " + event);
			//System.out.println("state " + n.getState());
			if ((n.getState().equals("Up")) && (n.getCallerId().equals(incomingNumber)))
			{
				System.out.println("incoming call has been picked up");
				generateAndSendMsg("http://127.0.0.1/detections/phone/ringing",  "Stopped");
			}
		}
		//System.out.println(" " );
    	}

	public void waitUntil(long delayTo)
	{	
		delayTo=delayTo+System.nanoTime();	
		long currentT=0;
		do{
			currentT = System.nanoTime();
		}while(delayTo >= currentT);
	}

	public void generateAndSendMsg(String type, String msg) {

		try 
		{				
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/components/"+componentName);
			testReading.addDataValue(null, type, msg, false);
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
