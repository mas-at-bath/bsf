package edu.bath.AOI;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import javax.vecmath.*;

import java.util.*;
import java.awt.geom.Point2D;	
import math.geom2d.conic.Circle2D;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class AOI extends Sensor {

	private boolean alive = true;
	private long timeSentLastAOILightMessage = 0;
	private boolean askedStartupData = false;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String aoiNodeName = "aoiSensor";
	private long nanoToMili=1000000;
	private static String componentName="AOI";
	//private CopyOnWriteArrayList<String> pendingMessages = new CopyOnWriteArrayList<String>();
	private CopyOnWriteArrayList<VehicleInfo> myVehicles = new CopyOnWriteArrayList<VehicleInfo>();
	private ArrayList<String> myJasonVehicleNames = new ArrayList<String>();
	private CopyOnWriteArrayList<String> controlledLanes = new CopyOnWriteArrayList<String>();
	private CopyOnWriteArrayList<LaneLightPair> laneLights = new CopyOnWriteArrayList<LaneLightPair>();
	private static 	WorkerNonThreadSender aoiMsgSender;
	private static Double offSetX = 0.0D;
	private static Double offSetY = 0.0D;
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingVehicleCmdMessages = new LinkedBlockingQueue<String>();
	private LinkedBlockingQueue<String> pendingAOIMessages = new LinkedBlockingQueue<String>();	

	private static String scenarioUsed = "m25";

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	
	public AOI(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
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
		catch (Exception e) {System.out.println("Error loading config.txt file");}

		if (args.length > 0) 
		{
			String scenarioFlag = args[0];
			System.out.println(scenarioFlag);
			if (scenarioFlag.equals("m25"))
			{
				scenarioUsed = scenarioFlag;
			}
			else if (scenarioFlag.equals("bath"))
			{
				scenarioUsed = scenarioFlag;
				offSetX = 2385.33;
				offSetY = 1691.39;
			}
			else
			{
				scenarioUsed = "m25";
			}
		}
		else
		{
			System.out.println("WARNING: No scenario area defined, defaulting to m25");
		}

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/AOISensors/AOI");
		AOI ps = new AOI(XMPPServer, componentName, "jasonpassword", jasonSensorVehicles , "http://127.0.0.1/AOISensors", "http://127.0.0.1/AOISensors/AOI");

		Thread.currentThread().sleep(1000);
		System.out.println("Created jasonSensor, now entering its logic!");
		

		aoiMsgSender = new WorkerNonThreadSender(XMPPServer, componentName+"-sender", "jasonpassword", aoiNodeName, "http://127.0.0.1/AOISensors", "http://127.0.0.1/AOISensors/AOI");
		System.out.println("Created aoiSender, now entering its logic!");
		Thread.currentThread().sleep(1000);
		
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

		startupTime=System.currentTimeMillis();

		sensorClient.addHandler(aoiNodeName, new ReadingHandler() 
		{ 
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
				try {
					pendingAOIMessages.put(rdf);
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
			sensorClient.subscribeAndCreate(aoiNodeName);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + aoiNodeName);
			e1.printStackTrace();
		}

		sensorClient.addHandler(jasonSensorVehiclesCmds, new ReadingHandler() 
		{ 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				//System.out.println("received a vehicle command rdf");
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/request/addVehicle", null);
					if(reqVal != null) 
					{
						String id = (String)reqVal.object;
						System.out.println("picked up a request for this vehicle to be created by Jason, so adding to list of Jason vehicles " + id);
						//check if new format request aimed at sumo containing lane, speed info, and if so split out name
						if (id.contains(","))
						{
							String[] splitName = id.split(",");
							id = splitName[0];
						}
						String fullName = "http://127.0.0.1/vehicles/"+id;
						if (!myJasonVehicleNames.contains(fullName))
						{
							myJasonVehicleNames.add(fullName);
						}					
					}
				}
				catch (Exception e)
				{
					System.out.println("Error processing message...");
					e.printStackTrace();
				}
				}
				
			}
		});
		try {
			sensorClient.subscribeAndCreate(jasonSensorVehiclesCmds);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + jasonSensorVehiclesCmds);
			e1.printStackTrace();
		}

		sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() 
		{ 
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
				try {
					pendingVehicleCmdMessages.put(rdf);
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
			sensorClient.subscribeAndCreate(jasonSensorVehicles);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + jasonSensorVehicles);
			e1.printStackTrace();
		}		

		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();

			try {
				if(sensorClient.checkReconnect())
				sensorClient.subscribeAndCreate(jasonSensorVehicles);
			} catch (XMPPException e1) {
				System.out.println("Couldn't reconnect to " + jasonSensorVehicles);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			//fire off requests for static info which won't change during sim run, e.g. lane info, traffic light info
			if (!askedStartupData)
			{
				waitUntil(2000*nanoToMili);
				System.out.println("sending RDF for all traffic light data");
				aoiMsgSender.generateAndSendMsg("http://127.0.0.1/request/sendAllTraficLights", "");
				askedStartupData=true;
			}

			String rdfAOI = pendingAOIMessages.poll();
			while (rdfAOI != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfAOI);
					String takenBy = dr.getTakenBy();

					/*if (!rdfAOI.contains("trafficLightSensors"))
					{
						System.out.println(rdf);
					}*/
					//System.out.println("received an RDF from " + takenBy);
					Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/route#edges", null);
					if(reqVal != null) 
					{
						String reqMsg = (String)reqVal.object;
						updateRoute(takenBy, reqMsg);
						System.out.println("received route info: " + reqMsg + " from " + takenBy);
					}

					List<Value> laneLightValues = dr.findValues(null, "http://127.0.0.1/trafficLightSensors/entryExitColour" ,null);
					if(laneLightValues.size() > 0) 
					{
						//System.out.println("got a laneLightValue RDF");
						for (Value lVal : laneLightValues)
						{
							String fullInfo=(String)lVal.object;
							String[] splitInfo = fullInfo.split(",");
							String entryLane = splitInfo[0];
							String exitLane = splitInfo[1];
							String colourState = splitInfo[2];
							//so we have a list of all lanes which are controlled by tlights, and another list of all lights with the end position of the lane they control stored
							//for each lane, update its ExitLaneLightState if it has one
							for (LaneLightPair llp : laneLights)
							{
								if (llp.getName().equals(entryLane))
								{
									//System.out.println("found a match for " + llp.getName() + " for exit lane " + exitLane + " so setting to " + colourState);
									boolean updatedExitState=false;
									for(ExitLaneLightState testState : llp.getExitLanes())
									{
										String testExitName = testState.getExitName();
										if(testExitName.equals(exitLane))
										{
											//System.out.println("updating exit state");
											testState.setLightState(colourState);
											updatedExitState=true;
										}
									}
									if (!updatedExitState)
									{
										ExitLaneLightState newExitState = new ExitLaneLightState(exitLane,colourState);
										llp.addExitLane(newExitState);
										//System.out.println("adding exit state");
									}
								}
							}
						}
					}

					List<Value> laneValues = dr.findValues(null, "http://127.0.0.1/trafficLights/Lanes" ,null);
					//System.out.println("received " + laneValues.size() + " lights");
					//if its zero, then this was an rdf but probably not valid.. 
					if(laneValues.size() > 0) 
					{
						//System.out.println("received an RDF with " + laneValues.size() + " lanes");
						for (Value currentVal : laneValues)
						{
							String laneName = (String) currentVal.object;
							if (!controlledLanes.contains(laneName))
							{
								controlledLanes.add(laneName);	
								Value endPosVal = dr.findFirstValue(null,"http://127.0.0.1/lanes/"+laneName , null);								if (endPosVal !=null)
								{	
									LaneLightPair newPair = new LaneLightPair(laneName,(String)endPosVal.object);
									laneLights.add(newPair);
								}
								//System.out.println("adding " + laneName);
							}
							else
							{
								//System.out.println("weird, " + laneName + " is a duplicate");
							}
						}
						System.out.println("controlledLanes now has " + controlledLanes.size() + " elements");
					}
				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfAOI = pendingAOIMessages.poll();
			}

			String rdf = pendingVehicleCmdMessages.poll();
			while (rdf != null)
			{
				try 
				{
					if (rdf.contains("edges"))
					{
						System.out.println("INFO: received new route info ok in handleIncomingReading");
					}
					try
					{
						DataReading dr = DataReading.fromRDF(rdf);
						String takenBy = dr.getTakenBy();
	
						/*if ((dr.getTimestamp() < lastUpdateTime) && lastUpdateTime != 0)
						{
							System.out.println("XXXXXXXX out of sync dr.timestamp XXXXXXXXXXX ");
							System.out.println("Time stamp of dr is " + dr.getTimestamp());
							System.out.println("Time stamp of last updated is " + lastUpdateTime);
						}
						else if (dr.getTimestamp() == lastUpdateTime)
						{
							//	System.out.println("maybe sync issue occuring");
						} */
						lastUpdateTime = dr.getTimestamp();

                    				if (dr.getLocatedAt().equals("http://127.0.0.1/vehicleSensors"))
                   				{
                           				DataReading.Value spatialVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null);
                            				if(spatialVal != null) 
                            				{
								String tempReading = (String)spatialVal.object;
                                				processVehiclePosition(tempReading, dr.getTakenBy());   
							}  

							DataReading.Value speedVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#vehicleSpeed", null);
                            				if(speedVal != null) 
                            				{
								Double tempSpeedReading = (Double)speedVal.object;
                                				processVehicleSpeed(tempSpeedReading, dr.getTakenBy());   
							}  

							DataReading.Value edgeLaneVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#EdgeLane", null);
                            				if(edgeLaneVal != null) 
                            				{
								String tempReading = (String)edgeLaneVal.object;
                                				processVehicleEdgeLane(tempReading, dr.getTakenBy());   
							}  

							DataReading.Value routePosVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#RouteDistance", null);
							if(routePosVal != null) 
                            				{
								Double tempPosReading = (Double)routePosVal.object;
								//System.out.println("route pos: " + tempPosReading);
                                				processVehicleRoutePos(tempPosReading, dr.getTakenBy());   
							}  

						}

						//handles receipt of controlled lane info from sumo, to store all controlled lanes in a list of strings. Also create a list of LaneLightPairs to match a endlane name to its location


						else
						{
							//System.out.println("couldn't pass message..");
						}

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdf = pendingVehicleCmdMessages.poll();
			}
		

			//Now check AOIs for each vehicle we know about
			//actually, why do it for all vehicles? Only Jason vehicles are going to do anything about it..
			for (VehicleInfo checkVeh : myVehicles)
			{
				//lets just do this for Jason created vehicles at the moment
				if (myJasonVehicleNames.contains(checkVeh.getName()))
				{			
				//System.out.println("checking " + checkVeh.getName() );
				Point2D currentLoc = checkVeh.getPosition();
				Double currentAOI = checkVeh.getAOIRadius();
				Circle2D newCirc = new Circle2D(currentLoc.getX(), currentLoc.getY(), currentAOI);
				
				try {
					DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
					testReading.setTakenBy(checkVeh.getName());
					testReading.addDataValue(null, "http://127.0.0.1/AOISensors/spatial", currentLoc.getX()+","+currentLoc.getY()+","+currentAOI, false);
					aoiMsgSender.generateAndSendMsg(testReading);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				//check if there are any vehicles (or could be obstacles too) inside the current AOI and publish if so
				String names="";
				String xs="";
				String ys="";
				String sameLane="";
				Double closestBehindInLaneDist = 99999d;
				String closestBehindInLaneName = "";
				Double closestBehindOneLaneDist = 99999d;
				String closestBehindOneLaneName = "";
				for (VehicleInfo otherVeh : myVehicles)
				{
					String otherVehName = otherVeh.getName();
					if ((!otherVehName.equals(checkVeh.getName())) && (myJasonVehicleNames.contains(otherVehName)))
					{
						Point2D thisVehicleLoc = checkVeh.getPosition();
						//System.out.println("checking if " + otherVehName + " is in " + checkVeh.getName() + " AOI");
						if (newCirc.isInside(thisVehicleLoc))
						{
							//System.out.println(" and it is..");
							//(sumoPos.getY()-offSetY) + ", 0, " + (sumoPos.getX()-offSetX)
							Double distanceBehind = checkVeh.getRoutePos() - otherVeh.getRoutePos();
							//System.out.println("checking distance to " + otherVeh.getName());
							if ((distanceBehind > 0) && (distanceBehind < closestBehindInLaneDist) && (checkVeh.getLane() == otherVeh.getLane()))
							{
								closestBehindInLaneDist = distanceBehind;
								closestBehindInLaneName = otherVeh.getName();
							}
							else if ((distanceBehind > 0) && (distanceBehind < closestBehindInLaneDist) && (checkVeh.getLane()-1 == otherVeh.getLane()))
							{
								closestBehindOneLaneDist = distanceBehind;
								closestBehindOneLaneName = otherVeh.getName();
							}
	
							if (names.equals(""))
							{
								names=otherVehName;
								xs="" + otherVeh.getPosition().getX();
								ys="" + otherVeh.getPosition().getY();
								if (checkVeh.getLane() == otherVeh.getLane())
								{
									sameLane="y";
								}
								else {sameLane="n";}
								//System.out.println("adding " + otherVehName + " to AOI at " + otherVeh.getPosition().getX() + ", " + otherVeh.getPosition().getY());
							}
							else
							{
								names=","+ otherVehName;
								xs=","+ otherVeh.getPosition().getX();
								ys=","+ otherVeh.getPosition().getY();
								if (checkVeh.getLane() == otherVeh.getLane())
								{
									sameLane=",y";
								}
								else {sameLane=",n";}
							}
						}
						else
						{
							//System.out.println(" and it isnt..");
						}
					}
				}
				if (!names.equals(""))
				{
					//found at least one vehicle in the AOI so send out an RDF
					//System.out.println("sending AOI vehicle RDF");
					DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
					testReading.setTakenBy(checkVeh.getName());

					if (!closestBehindInLaneName.equals(""))
					{
						//System.out.println("closest vehicle behind " + checkVeh.getName() + " is " + closestBehindInLaneName + " at " + closestBehindInLaneDist + "m");
						testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleBehind", closestBehindInLaneName , false);
						testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleBehindDistance", closestBehindInLaneDist , false);
					}

					if (!closestBehindOneLaneName.equals(""))
					{
						//System.out.println("closest vehicle one lane inside " + checkVeh.getName() + " is " + closestBehindOneLaneName + " at " + closestBehindOneLaneDist + "m");
						testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleBehindOneLaneIn", closestBehindOneLaneName , false);
						testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleBehindOneLaneInDistance", closestBehindOneLaneDist , false);
					}

					testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleNames", names , false);
					testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleX", xs , false);
					testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleY", ys , false);
					testReading.addDataValue(null, "http://127.0.0.1/AOISensors/vehicleSameLane", sameLane , false);
					aoiMsgSender.generateAndSendMsg(testReading);
				}

				//now check previously calculated upcoming lights and see if any points lie within the AOI
				List<LaneLightPair> lightPairs = checkVeh.getUpcomingLights();

				LaneLightPair nearestLight = null;
				for (LaneLightPair lPair : lightPairs)
				{
					//System.out.println("checking against " + lPoint.getX() + "," + lPoint.getY());
					if (newCirc.isInside(lPair.getPosition()) )	
					{
						//System.out.println("XXXXXXX Light " + lPair.getName() + " at: " + lPair.getPosition().getX() + "," + lPair.getPosition().getY() + " is in AOI");
						Double distanceToLight = currentLoc.distance(lPair.getPosition());
						//System.out.println("distance to light: " + distanceToLight);
						
						//maybe here, we just worry about distance to nearest light, I think working out
						//situation where we have two lights both in AOI and how to treat them would be tricky
						if (nearestLight == null)
						{
							//System.out.println("first light found, so adding this as closest");
							nearestLight=lPair;
						}
						else if (distanceToLight < currentLoc.distance(nearestLight.getPosition()))
						{
							//System.out.println("this is the nearest light, so replacing the other one");
							nearestLight=lPair;
						}							
					
					}				
				}
				if (!(nearestLight==null))
				{
					Double finalLightDistance = currentLoc.distance(nearestLight.getPosition());
					for (LaneLightPair testMainLight : laneLights)
					{
						if (testMainLight.getName().equals(nearestLight.getName()))
						{
							//System.out.println("this light controls : " + testMainLight.getExitLanes().size() + " exits");
							//if one lane, then just send that lane colour
							//otherwise take current edge of vehicle route, and find next edge, then
							//find which exitlane matches that
							String lightColour = "";
							boolean foundCorrectExit = false;
							if (testMainLight.getExitLanes().size() == 1)
							{
								lightColour = testMainLight.getExitLanes().get(0).getLightState();
								foundCorrectExit=true;
							}
							else if (testMainLight.getExitLanes().size() > 1)
							{
								String routeExitLane = checkVeh.getNextRouteSectionAfter(testMainLight.getName());
								//System.out.println("checking " + testMainLight.getName() + " junction with multiple exits, for: " + routeExitLane);
								for (ExitLaneLightState testExit : testMainLight.getExitLanes())
								{
									//System.out.println("against: " + testExit.getExitName());
									if (testExit.getExitName().startsWith(routeExitLane))
									{
										if (foundCorrectExit)
										{
											System.out.println("agh found a duplicate exit lane, fix me!");
										}
										foundCorrectExit=true;
										lightColour = testExit.getLightState();
									}
								}
							}
							else
							{
								System.out.println("no exit lanes found, thats odd");
							}
							if (foundCorrectExit)
							{
								//System.out.println("nearest light " + finalLightDistance + "m away is " + lightColour);
								long testTime = timeSentLastAOILightMessage+1000;
								if (testTime < System.currentTimeMillis())
								{
									try {
										DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
										testReading.setTakenBy(checkVeh.getName());
										testReading.addDataValue(null, "http://127.0.0.1/AOISensors/upcomingLight", finalLightDistance+","+lightColour, false);
										aoiMsgSender.generateAndSendMsg(testReading);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
									timeSentLastAOILightMessage = System.currentTimeMillis();
								}
					
							}
							else
							{
								System.out.println("sorry couldn't find an exit lane or light state for some reason");
							}
						}
					}
				}
				}
				
							
			}

			long currentNanoTime = System.nanoTime();
			long loopTimeNano = currentNanoTime - currentLoopStartedNanoTime;

			//System.out.println("loop took: " + loopTimeNano/nanoToMili + "ms");
			long remainingTime = (500*nanoToMili)-loopTimeNano;
			waitUntil(remainingTime);
			//System.out.println("delayed for " + remainingTime/nanoToMili + "ms");
		}
					
	}	
		
	public void updateRoute(String id, String newRoute)
	{
		boolean foundVeh = false;
		//System.out.println("trying to update info for " + id);
		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(id))
			{
				//System.out.println("found existing vehicle, updating its route");
				currentVeh.newRoute(newRoute);
				foundVeh=true;
			}
		}
		if (!foundVeh)
		{
			//System.out.println("creating new vehicle... ");
			VehicleInfo newVeh = new VehicleInfo(id);
			newVeh.newRoute(newRoute);
			myVehicles.add(newVeh);
		}

		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(id))
			{
				List<String> currentRoute = currentVeh.getRoute();
				currentVeh.clearLaneSignals();
				currentVeh.clearUpcomingLights();
				for (String currentEdge : currentRoute)
				{				
					int edgeOccuranceInLanes = 0;
					String controlledSignal = "";
					for (String checkLane : controlledLanes)
					{
						if (checkLane.startsWith(currentEdge))
						{
							//System.out.println("found a potential controlled lane in new route: " + checkLane);
							if (edgeOccuranceInLanes > 0)
							{
								System.out.println("WARNING: DUP: overwriting " + controlledSignal);
							}
							edgeOccuranceInLanes++;
							controlledSignal = checkLane;
						}
					}
					if (edgeOccuranceInLanes==1)
					{
						//System.out.println("adding " + controlledSignal + " to list of signals for " + id + " new route");	
						currentVeh.addLaneSignalControl(controlledSignal);
					}
					else if (edgeOccuranceInLanes > 1)
					{
						System.out.println("WARNING: multiple lanes in edge controlled by signal, not sure how to handle this yet!!!");
						System.out.println("WARNING: using " + controlledSignal + " for the moment, but check is ok");
						currentVeh.addLaneSignalControl(controlledSignal);
					}					
				}
				
				//System.out.println(id + " new route is controlled by " + currentVeh.getControlledLanes().size() + " traffic lights");
				
				//now go through this list, find each match in the laneLights list, find the xy and send a simple rdf for xmppviewer to send
				for (String cLane : currentVeh.getControlledLanes())
				{
					for (LaneLightPair lPair : laneLights)
					{
						if(lPair.getName().equals(cLane))
						{
							//System.out.println("at " + lPair.getPosition());
							try {
								DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
								testReading.setTakenBy("http://127.0.0.1/controlledLaneLight");
								testReading.addDataValue(null, "http://127.0.0.1/trafficLightSensors/position", lPair.getPosition().getX() + "," + lPair.getPosition().getY() , false);
								aoiMsgSender.generateAndSendMsg(testReading);
								LaneLightPair llp = new LaneLightPair(lPair.getName(),new Point2D.Double(lPair.getPosition().getX(), lPair.getPosition().getY())); 
								currentVeh.addUpcomingLight(llp);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public void processVehiclePosition(String reading, String takenBy)
	{
		//System.out.println("told a position update of " + reading + " from " + takenBy);
		String[] spatialInfo = reading.split(",");
		Double xPos = Double.parseDouble(spatialInfo[0]);
		Double zPos = Double.parseDouble(spatialInfo[1]);
		Double yPos = Double.parseDouble(spatialInfo[2]);
		Double orient = Double.parseDouble(spatialInfo[3]);
		Point2D newOffsetXY = convertInternalLocation(new Point2D.Double(xPos, yPos));

		boolean foundVeh = false;
		//System.out.println("trying to update info for " + takenBy);
		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(takenBy))
			{
				//System.out.println("found existing vehicle, updating its spatial: " + newOffsetXY.getX() +","+newOffsetXY.getY()+","+orient	);
				currentVeh.updatePosition(new Point2D.Double(newOffsetXY.getX(),newOffsetXY.getY()));
				currentVeh.updateOrientation(orient);
				foundVeh=true;
			}
		}
		if (!foundVeh)
		{
			//System.out.println("creating new vehicle... ");
			VehicleInfo newVeh = new VehicleInfo(takenBy);
			newVeh.updatePosition(new Point2D.Double(xPos,yPos));
			newVeh.updateOrientation(orient);
			myVehicles.add(newVeh);
		}
	}

	public void processVehicleEdgeLane(String edgeLane, String takenBy)
	{
		boolean foundVeh = false;
		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(takenBy))
			{
				//System.out.println("in AOI, setting edgeLane to be " + edgeLane);
				currentVeh.updateEdgeLane(edgeLane);
				foundVeh=true;
			}
		}
		if (!foundVeh)
		{
			VehicleInfo newVeh = new VehicleInfo(takenBy);
			//System.out.println("in AOI, setting edgeLane to be " + edgeLane);
			newVeh.updateEdgeLane(edgeLane);
			myVehicles.add(newVeh);
		}
	}

	public void processVehicleRoutePos(Double routePos, String takenBy)
	{

		boolean foundVeh = false;
		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(takenBy))
			{
				currentVeh.updateRoutePos(routePos);
				foundVeh=true;
			}
		}
		if (!foundVeh)
		{
			VehicleInfo newVeh = new VehicleInfo(takenBy);
			newVeh.updateRoutePos(routePos);
			myVehicles.add(newVeh);
		}
	}
	
	public void processVehicleSpeed(Double reading, String takenBy)
	{

		boolean foundVeh = false;
		//System.out.println("trying to update speed info for " + takenBy);
		for (VehicleInfo currentVeh : myVehicles)
		{
			if (currentVeh.getName().equals(takenBy))
			{
				//System.out.println("found existing vehicle, updating its speed: " + reading);
				currentVeh.updateSpeed(reading);
				foundVeh=true;
			}
		}
		if (!foundVeh)
		{
			//System.out.println("creating new vehicle... ");
			VehicleInfo newVeh = new VehicleInfo(takenBy);
			newVeh.updateSpeed(reading);
			myVehicles.add(newVeh);
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

    	public Point2D convertInternalLocation(Point2D oldLoc)
    	{
       		Double newX = oldLoc.getY() +offSetX;
        	Double newY = oldLoc.getX() +offSetY;
        	//System.out.println("converting " + oldLoc.getX() + "," + oldLoc.getY() + " to " + newX + "," + newY);
        	return new Point2D.Double(newX,newY);
    	}
}
