package edu.bath.HueBSF;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.awt.Color;
import java.util.Map;
import java.util.ArrayList;

import com.philips.lighting.data.*;
import com.philips.lighting.gui.*;
import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.model.*;
import com.philips.lighting.hue.sdk.utilities.*;
import com.philips.lighting.hue.listener.*;



public class HueBSF extends Sensor {

	private static final int MAX_HUE=65535;
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
	private static String componentName="hueBSF";
	//private static WorkerNonThreadSender hueMsgSender;
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingHueMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	private PHHueSDK phHueSDK;
	private boolean stopWaitingForAuth=false;
	private boolean connectedHue=false;
	private PHBridgeResourcesCache cache;
	private Map<String, PHScene> sceneMap;
	private PHBridge myBridge;
	private String allLightsOnScene = "680bbff42-on-0";

	private List<PHLight> storedLights;
	ArrayList<LightPairs> previousLightPairs = new ArrayList<LightPairs>();


	public HueBSF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
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

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/HueBSF/Hue");
		HueBSF ps = new HueBSF(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/AOISensors", "http://127.0.0.1/HueBSF/Hue");

		Thread.currentThread().sleep(1000);
		System.out.println("Created hueSensor, now entering its logic!");
		
		ps.run();
	}
	
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException {	
		
		while(sensorClient == null) {
			try {
				sensorClient = new SensorClient(XMPPServer, componentName+"-receiver", "jasonpassword");
				System.out.println("Guess sensor connected OK then!");
			} catch (XMPPException e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}

		System.out.println("creating phHueSDK");
 		phHueSDK = PHHueSDK.getInstance();
		phHueSDK.setAppName("HueBSF"); 

    		// Register the PHSDKListener to receive callbacks from the bridge.
    		phHueSDK.getNotificationManager().registerSDKListener(listener);

		HueProperties.loadProperties();
 		String username = HueProperties.getUsername();
		System.out.println("username: " + username);
		String lastIpAddress = HueProperties.getLastConnectedIP(); 
		if (lastIpAddress !=null && !lastIpAddress.equals("")) 
		{
			PHAccessPoint lastAccessPoint = new PHAccessPoint();
			lastAccessPoint.setIpAddress(lastIpAddress);
			lastAccessPoint.setUsername(username);

 			if (!phHueSDK.isAccessPointConnected(lastAccessPoint))
			{
				System.out.println("trying to connect..");
				phHueSDK.connect(lastAccessPoint);
			}
			else
			{
				System.out.println("already connected..");
			}
		}
		else
		{
			System.out.println("no connection details found, doing search");
			// start the search
    			PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
    			sm.search(true, true); 
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
						pendingHueMessages.put(rdf);
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
			sensorClient.subscribeAndCreate(homeSensors);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}



		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();

			try {
				if(sensorClient.checkReconnect())
				sensorClient.subscribeAndCreate(homeSensors);
			} catch (XMPPException e1) {
				System.out.println("Couldn't reconnect to " + homeSensors);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			//Don't process any messages until we've connected to the bridge
			if(connectedHue)
			{
				String rdfHue = pendingHueMessages.poll();			

				while (rdfHue != null)
				{			
					try 
					{
						DataReading dr = DataReading.fromRDF(rdfHue);
						String takenBy = dr.getTakenBy();
						boolean processedMsg = false;
						//System.out.println("got Hue RDF message!");

						Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/requests/lights", null);
						if(reqVal != null) 
						{
							String reqAction = (String)reqVal.object;
							//if request intended for all lights, process accordingly
							if (takenBy.equals("all"))
							{
								List<PHLight> myLights = cache.getAllLights();
								System.out.println("found " + myLights.size() + " lights:");
	 							for (PHLight light : myLights) 
								{
									String lampModel = light.getModelNumber();
									PHLightState lState = light.getLastKnownLightState();
									if (reqAction.equals("off"))
									{
										lState.setOn(false);
									}
									else if (reqAction.equals("on"))
									{
										lState.setOn(true);
									}
									phHueSDK.getSelectedBridge().updateLightState(light, lState);
								}
							}
							//otherwise just target specific light
							else
							{
							}
							processedMsg=true;
						}

						Value personVal = dr.findFirstValue(null, "http://127.0.0.1/detections/people" , null);
						if(personVal != null) 
						{
							String personName = (String)personVal.object;
							if (personName.equals("vin") && connectedHue)
							{
								System.out.println("vin detected at door, turn lights on..");
								myBridge.activateScene(allLightsOnScene, "test", sceneListener);
							}
							else
							{
								System.out.println("told about " + personName + " but couldnt do anything..");
							}
							processedMsg=true;
						}

						Value phoneVal = dr.findFirstValue(null, "http://127.0.0.1/detections/phone/ringing" , null);
						if(phoneVal != null) 
						{
							String callerID = (String)phoneVal.object;
							if (callerID.equals("Stopped") && connectedHue)
							{
								System.out.println("phone stopped ringing, return to original");
								restoreLights();
							}
							else
							{
								System.out.println("phone started ringing");
								storeCurrentLightState();
								waitUntil(1000*nanoToMili);
								setLightsRinging();
							}
							processedMsg=true;
						}

						if (!processedMsg)
						{
							Value foundV = dr.findFirstValue(null, null , null);
							if (foundV != null)
							{
								System.out.println("pred: " + foundV.predicate.toString() + " subj: " + foundV.subject.toString() + " obj; " + foundV.object.toString());
							}
						}

					}
					catch(Exception e) 
					{
						System.out.println(e);
					}
					rdfHue = pendingHueMessages.poll();
				}
			}

			//System.out.println("waiting for " + cycleTime);
			if (connectedHue)
			{
				//randomLights();
			}
			//waitUntil(cycleTime*nanoToMili);
			try
			{
				Thread.sleep(2000);
			}
			catch (Exception es)
			{
				es.printStackTrace();
			}

		}
					
	}

    private PHSceneListener sceneListener = new PHSceneListener() {

	@Override
	public void onScenesReceived(List PHScene)
	{
		System.out.println("received list of scenes");
	}

        @Override
        public void onError(int code, final String message) {
		System.out.println("Hue scene error: " + code + " " + message);
        }

        @Override
        public void onSuccess() {  
        }

	@Override
	public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1)
	{
		System.out.println("received scene state update");
	}

    };	

		
    // Local SDK Listener
    private PHSDKListener listener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(List accessPoint) {
             	// Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list 
             	// and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.  
		System.out.println("found " + accessPoint.size() + " access point(s)");
		if (accessPoint.size() == 1 )
		{
			PHAccessPoint ap = (PHAccessPoint) accessPoint.get(0);
			System.out.println("trying to connect to " + ap.getIpAddress());
			String username = HueProperties.getUsername();
			HueProperties.storeUsername(username);
			ap.setUsername(username);
			phHueSDK.connect(ap);
		}          
        }
        
        @Override
        public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge) {
             // Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to   
             // check which cache was updated, e.g.
            if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
               //System.out.println("Lights Cache Updated ");
            }
        }

        @Override
        public void onBridgeConnected(PHBridge b) {
		//System.out.println("connected to bridge!");

		 // stop waiting for pushlink auth
		stopWaitingForAuth=true;

            	phHueSDK.setSelectedBridge(b);
            	phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);

		String username = HueProperties.getUsername();
		String lastIpAddress = b.getResourceCache().getBridgeConfiguration().getIpAddress();
		System.out.println("On connected: IP " + lastIpAddress);
		HueProperties.storeUsername(username);
		HueProperties.storeLastIPAddress(lastIpAddress);
		HueProperties.saveProperties();

		myBridge = phHueSDK.getSelectedBridge();
		cache = myBridge.getResourceCache();
		//get current scenes stored in bridge
		sceneMap  = cache.getScenes();

		for (Map.Entry<String, PHScene> entry : sceneMap.entrySet())
		{
    			System.out.println(entry.getKey() + ": " + entry.getValue().getName() +": "+ entry.getValue().getSceneIdentifier() + ", active? " + entry.getValue().getActiveState() + ", " + entry.getValue().getLightIdentifiers().size() + " lights");
		}



		//myBridge.activateScene(allLightsOnScene, "test", sceneListener);


		List<PHLight> myLights = cache.getAllLights();
		System.out.println("found " + myLights.size() + " lights:");
 		for (PHLight light : myLights) 
		{
			String lampModel = light.getModelNumber();
			PHLightState lState = light.getLastKnownLightState();
			Integer bri = lState.getBrightness();
			Integer hue = lState.getHue();
			int lightRed = -1;
			int lightGreen = -1;
			int lightBlue = -1;
			
			if (!lampModel.equals("LWB004"))
			{
				float xCol = lState.getX();
				float yCol = lState.getY();
				float colVal[] = new float[2];
				colVal[0] = xCol;
				colVal[1] = yCol;
				int hueColVal = PHUtilities.colorFromXY(colVal, lampModel);
				Color hueColor = new Color (hueColVal);
				lightRed = hueColor.getRed();
				lightGreen = hueColor.getGreen();
				lightBlue = hueColor.getBlue();
			}

			boolean on = lState.isOn();
			System.out.println(light.getName() + ", hue: " + hue + ", bri: " + bri + ", on: " + on + ", model: " + lampModel);
			generateAndSendLightMsg(light.getName(), lampModel, bri, lightRed, lightGreen, lightBlue);
		}
		connectedHue = true;
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
		System.out.println("authentication required, press access button on bridge within 30 seconds");
            	phHueSDK.startPushlinkAuthentication(accessPoint);
		long startTime = System.currentTimeMillis();
		long targetTime = startTime + 30000;
		while ((System.currentTimeMillis() < targetTime) || stopWaitingForAuth)
		{
			
		}
        }

        @Override
        public void onConnectionResumed(PHBridge bridge) {
		//System.out.println("Hue connection resumed...");
        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
		System.out.println("Hue connection lost...");
             // Here you would handle the loss of connection to your bridge.
        }
        
        @Override
        public void onError(int code, final String message) {
		System.out.println("Hue connection error: " + code + " " + message);
		if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
			System.out.println("bridge not responding..");
		}
		else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
			System.out.println("Make sure you've pressed the Hue sync button...");
		}
		else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
			System.out.println("pushlink failed..");
		}
		else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
			System.out.println("bridge not found..");
		}
             // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
        }

        @Override
        public void onParsingErrors(List parsingErrorsList) {
		System.out.println("Hue connection parsing error...");
            // Any JSON parsing errors are returned here.  Typically your program should never return these.      
        }
    };

	public void storeCurrentLightState()
	{
		previousLightPairs.clear();
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		storedLights = cache.getAllLights();
		for (PHLight light : storedLights) 
		{
			//System.out.println("light is: " + light.getName());
			PHLightState lState = light.getLastKnownLightState();
			if (light.getModelNumber().equals("LWB004"))
			{
				LightPairs newPair = new LightPairs(light,0, 0);
				previousLightPairs.add(newPair);
			}
			else
			{
				LightPairs newPair = new LightPairs(light,lState.getX(), lState.getY());
				previousLightPairs.add(newPair);
			}
		}
	}

	public void restoreLights()
	{
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		List<PHLight> allLights = cache.getAllLights();
		//System.out.println("checking " + allLights.size() + " lights against old " + previousLightPairs.size());
		int lightNum = 0;
		for (PHLight light : allLights) 
		{
			//System.out.println("checking " + lightNum);
			String findLight = light.getName();
			boolean foundMatch = false;
			for (LightPairs prevPair : previousLightPairs)
			{
				if (prevPair.getLight().getName().equals(findLight))
				{
					//System.out.println("setting " + findLight + " to " + prevPair.getX() + " , " + prevPair.getY());
					PHLightState lightState = new PHLightState();
					if (!light.getModelNumber().equals("LWB004"))
					{
						lightState.setX(prevPair.getX());
						lightState.setY(prevPair.getY());
						bridge.updateLightState(light, lightState);
						waitUntil(100*nanoToMili);
					}
					foundMatch=true;
				}
			}
			if (!foundMatch)
			{
				System.out.println("couldnt find match for " + findLight);
			}
			lightNum++;
		}
	}

	public void setLightsRinging()
	{
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		List<PHLight> allLights = cache.getAllLights();
		Random rand = new Random();
		for (PHLight light : allLights) 
		{
			if (!light.getModelNumber().equals("LWB004"))
			{
				float xy[] = PHUtilities.calculateXYFromRGB(25, 25, 255, light.getModelNumber());
				PHLightState lightState = new PHLightState();
				lightState.setX(xy[0]);
				lightState.setY(xy[1]);
				bridge.updateLightState(light, lightState); // If no bridge response is required then use this simpler form.
				waitUntil(100*nanoToMili);
			}
		}

	}

 	public void randomLights() 
	{
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		List<PHLight> allLights = cache.getAllLights();
		Random rand = new Random();
		for (PHLight light : allLights) 
		{
			PHLightState lightState = new PHLightState();
			lightState.setHue(rand.nextInt(MAX_HUE));
			bridge.updateLightState(light, lightState); // If no bridge response is required then use this simpler form.
		}
	}

	public void generateAndSendLightMsg(String lName, String lModel, Integer bri, int lightRed, int lightGreen, int lightBlue) {

		try 
		{				
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/components/"+componentName+"/"+lName);
			testReading.addDataValue(null, "http://127.0.0.1/components/lights/model" , lModel, false);
			testReading.addDataValue(null, "http://127.0.0.1/components/lights/brightness" , bri, false);
			testReading.addDataValue(null, "http://127.0.0.1/components/lights/redval" , lightRed, false);
			testReading.addDataValue(null, "http://127.0.0.1/components/lights/greenval" , lightGreen, false);
			testReading.addDataValue(null, "http://127.0.0.1/components/lights/blueval" , lightBlue, false);
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
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

}
