package edu.bath.TivoBSF;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.*;
import java.util.List;

import java.net.*;


public class Tivo extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";
	private static String tivoNodeName = "tivo";
	private long nanoToMili=1000000;
	private static String componentName="tivo";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingTivoMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	private static ServerSocket s = null;
	private static client_handler myTivoClient;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static Tivo ps;

	public Tivo(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public Tivo(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public static void main(String[] args) throws Exception {

	       	Runtime.getRuntime().addShutdownHook(new Thread()
		{
		
		    @Override
		    public void run()
		    {
		        System.out.println("Shutdown hook ran!");
			try 
			{
				if (myTivoClient != null)
				{
					myTivoClient.disconnect();
				}
                    	} catch (Exception e) 
			{
				System.out.println("error in shutdown");
				e.printStackTrace();
                    	}
		    }
		});
			
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

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/TV, http://127.0.0.1/TV/Tivo");
		if (useXMPP)
		{
			ps = new Tivo(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/TV", "http://127.0.0.1/TV/Tivo");
		}
		else if (useMQTT)
		{
			ps = new Tivo(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/TV", "http://127.0.0.1/TV/Tivo", true, 0);
		}

		Thread.currentThread().sleep(1000);
		System.out.println("Created tivoSensor, now entering its logic!");
		
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
				try 
				{
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
			try 
			{
				sensorClient = new SensorMQTTClient(XMPPServer, componentName+"-receiver");
				System.out.println("Guess sensor connected OK then!");
			} catch (Exception e1) 
			{
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
				
			}
		}

		startupTime=System.currentTimeMillis();

		sensorClient.addHandler(tivoNodeName, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
					try {
						pendingTivoMessages.put(rdf);
					}
					catch (Exception e)
					{
						System.out.println("Error adding new message to queue..");
						e.printStackTrace();
					}
				}
			}
		});
		try {
			sensorClient.subscribe(tivoNodeName);
		} catch (	Exception e1) {
			System.out.println("Exception while subscribing to " + tivoNodeName);
			e1.printStackTrace();
		}


		myTivoClient = new client_handler("192.168.0.79", 31339);
		myTivoClient.start();


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

			String rdfTivo = pendingTivoMessages.poll();
			while (rdfTivo != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfTivo);
					String takenBy = dr.getTakenBy();
					System.out.println("got Tivo RDF message!");

					Value irVal = dr.findFirstValue(null, "http://127.0.0.1/requests/tv/ircode", null);
					if(irVal != null) 
					{
						String reqAction = (String)irVal.object;
						myTivoClient.send("IRCODE", reqAction + "\r");
					}

					Value setVal = dr.findFirstValue(null, "http://127.0.0.1/requests/tv/setchannel", null);
					if(setVal != null) 
					{
						String reqAction = (String)setVal.object;
						myTivoClient.send("SETCH", reqAction + "\r");
					}

				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfTivo = pendingTivoMessages.poll();
				
			}

			try
			{
				Thread.sleep(500);
			}
			catch (Exception es)
			{
				es.printStackTrace();
			}
			//System.out.println("trying to send IR command");

		}
					
	}	

	public void waitUntil(long delayTo)
	{	
		delayTo=delayTo+System.nanoTime();	
		long currentT=0;
		do{
			currentT = System.nanoTime();
		}while(delayTo >= currentT);
	}


	//from http://www.binarytides.com/java-socket-programming-tutorial/
	class client_handler extends Thread
	{
	    	private String address;
		private int destPort;
	        private Socket s = new Socket();
    		private PrintWriter s_out = null;
    		private BufferedReader s_in = null;
	     
	    	client_handler(String ip, int port)
	    	{
			this.address = ip;
			this.destPort = port;
	    	}
	 
	    	public void run()
	    	{
			try
			{
				//Socket s = new Socket();
				s.connect(new InetSocketAddress(address , destPort));
				System.out.println("connected to " + address);

				s_out = new PrintWriter( s.getOutputStream(), true);
				s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			}
				 
			//Host not found
			catch (Exception e)
			{
			    	System.err.println("Exception connecting..");
				e.printStackTrace();
			}

			try {
    				String response;
    				while ((response = s_in.readLine()) != null)
    				{
        				System.out.println( response );
					try 
					{
						String[] splitArray = response.split("\\s+");
						if (splitArray.length == 3)
						{
							String msgType = splitArray[0];
							String msgContent = splitArray[1];
							if (msgType.equals("CH_STATUS"))
							{
								generateAndSendMsg("http://127.0.0.1/components/tv/"+msgType, msgContent);
							}
						}

					} catch (Exception ex) {
						System.out.println("received tivo msg but couldnt process it");
					}
    				}
			}
			catch (Exception e2)
			{
			    	System.err.println("Exception in connection loop..");
				e2.printStackTrace();
			}

			try
			{
				Thread.sleep(500);
			}
			catch (Exception es)
			{
				es.printStackTrace();
			}
	    	}

		public void send(String type, String msg)
		{
			if (s != null)
			{
				System.out.println("sending " + type + " " + msg);
				s_out.println(type + " " + msg);
			}
			else
			{
				System.out.println("couldn't send command to tivo, likely no connection established");
			}
		}

		public void disconnect()
		{
			if (s_out != null)
			{
         			s_out.close();
      			}
      			if (s != null) 
			{
         			try 
				{
            				s.close();
         			} catch (IOException e) 
				{
            				System.out.println("couldn't disconnect socket");
         			}
     			 }

		}
	}

	public void generateAndSendMsg(String type, String msg) {
		System.out.println("sending: " + type + " " + msg);
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
