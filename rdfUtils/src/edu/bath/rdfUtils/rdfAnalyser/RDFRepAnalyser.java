package edu.bath.rdfUtils.rdfAnalyser;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jivesoftware.smack.XMPPException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.QueryLanguage;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.franz.agraph.repository.AGTupleQuery;
import com.franz.agraph.repository.AGValueFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;

import math.geom2d.*;
import java.io.*;

public class RDFRepAnalyser {

	class KnownVehicles {
		Float x,z = 0f;
		long timeVal = 0L;
	}
	
	class KnownMessage {
		String msgName = "";
		int numOccurances = 0;
	}
	
	class TimeResPair{
		public Long time;
		public int res;
	}
	
	//private static ArrayList<TimeResPair> brakePairs = new ArrayList<TimeResPair>();
	//private static ArrayList<TimeResPair> accelerationPairs = new ArrayList<TimeResPair>();
	
	private static ArrayList<Long> brakePairs = new ArrayList<Long>();
	private static ArrayList<Long> accelerationPairs = new ArrayList<Long>();
	
	private ArrayList<KnownVehicles> vehicle1, vehicle2,vehicle3,vehicle4; // = new ArrayList<KnownVehicles>();
	private ArrayList<String> allResults;
	private ArrayList<KnownMessage> knownMsg;

	private boolean alive = true;	
	private static String agServerURL;
	private String agUsername, agPassword, agCatID, agRepoID;
	private int accelerateCount =0;
	private int brakeCount = 0;
	
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";

	private long pushScenario1StartTime=1350148288000L;
	private long pushScenario1FinTime=1350148488000L;
	
	//this has got full jstate info on
	private long pushScenario2StartTime=1350416205000L;
	private long pushScenario2FinTime=1350416400000L;
	private long pushScenario3StartTime=1350416449000L;
	private long pushScenario3FinTime=1350416602000L;

	private long pullScenario1StartTime=1350143023000L;
	private long pullScenario1FinTime=1350143178000L;
	private long pullScenario2StartTime=1350152676000L;
	private long pullScenario2FinTime=1350152835000L;
	
	//pull with jstate, cars 3 and 4 go AWOL
	private long pullScenario3StartTime=1350827502000L;
	private long pullScenario3FinTime=1350827670000L;
	
	private long pushScenariov2StartTime=1368376616793L;
	private long pushScenariov2FinTime=  1368377270000L;
	
	private long waypointScenariov1StartTime=1368986542562L;
	private long waypointScenariov1FinTime=  1368987196000L;	
	
	private long simStartTime=waypointScenariov1StartTime;
	private long simFinTime=  waypointScenariov1FinTime;
	
	int beliefCountValue = 0;
	int tellCountValue = 0;
	int achieveCountValue = 0;

	private int intervalTime=500;
	
	private static AGServer server;
	private AGCatalog catalogue;
	private AGRepository repository;
	private AGRepositoryConnection conn;

	
	public static void main(String[] args) throws Exception {
	
		//get IP addressed from config file
	    BufferedReader br = new BufferedReader(new FileReader("config.txt"));
        String line;
        while((line = br.readLine()) != null) {
			if (line.contains("OPENFIRE"))
			{
				String[] configArray = line.split("=");
				XMPPServer = configArray[1];
				System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
			}
			if (line.contains("ALLEGROGRAPH"))
			{
				String[] configArray = line.split("=");
				agServer = configArray[1];
				agServerURL = new String("http://" + agServer + ":10035");
				System.out.println("Using config declared IP address of AllegroGraph server as: " + agServer);
			}
        }
	
		RDFRepAnalyser analyserAgent = new RDFRepAnalyser();
		analyserAgent.run();
	}
	
	private String nodeName;// TODO: Remove this, it is just a debugging option
	public RDFRepAnalyser() {//throws XMPPException {

	}
	
	public void run() {
	
		vehicle1 = new ArrayList<KnownVehicles>();
		vehicle2 = new ArrayList<KnownVehicles>();
		vehicle3 = new ArrayList<KnownVehicles>();
		vehicle4 = new ArrayList<KnownVehicles>();
		allResults = new ArrayList<String>();
		knownMsg = new ArrayList<KnownMessage>();
		System.out.println("v1 has " + vehicle1.size());
	
		agUsername = "super";
		agPassword = "jasonpassword";
		agCatID = "java-catalog";
		agRepoID = "SensorData";
	
		server = new AGServer(agServerURL, agUsername, agPassword);
		try {
		System.out.println("available cats: " + server.listCatalogs());

		catalogue = server.getCatalog(agCatID);
		} catch (Exception newE) {
			newE.printStackTrace();
		}
		try {
			repository = catalogue.createRepository(agRepoID);
			repository.initialize();
			conn = repository.getConnection();
			System.out.println("Repository " + (repository.getRepositoryID()) +
                " is up! It contains " + (conn.size()) +
                " statements.");
				
			try {
				String timeCount ="select ?subj ?value where { ?subj <http://bath.edu/sensors/predicates#takenAt> ?value FILTER (?value >= "+simStartTime+ " && ?value <= " + simFinTime + ") }";
				AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, timeCount);
				TupleQueryResult result = tupleQuery.evaluate();
				ArrayList<String> subjectResults = new ArrayList<String>();
				while (result.hasNext()) 
				{
					BindingSet bindingSet = result.next();				
					Value s = bindingSet.getValue("subj");
					subjectResults.add(s.toString());
				}
				System.out.println("Found " + subjectResults.size() + " data readings in this time period");
				
				for (String temp : subjectResults) 
				{
					String drQueryString ="select * where { <" + temp + "> ?pred ?obj . }";
					AGTupleQuery drTupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, drQueryString);
					TupleQueryResult drResult = drTupleQuery.evaluate();
					String locatedAtVal = "";
					String predVal = "";
					String dataReadingVal = "";
					String objectVal="";
					long takenAtVal=0;
					while (drResult.hasNext()) 
					{
						BindingSet drBindingSet = drResult.next();
						Value pred = drBindingSet.getValue("pred");
						Value obj = drBindingSet.getValue("obj");
						if (pred.toString().equals("http://bath.edu/sensors/predicates#locatedAt"))
						{
							locatedAtVal=obj.toString();
						}
						else if (pred.toString().equals("http://bath.edu/sensors/predicates#isDataReading"))
						{
							dataReadingVal=obj.toString();
						}
						else if (pred.toString().equals("http://bath.edu/sensors/predicates#takenAt"))
						{
							takenAtVal = Long.valueOf(obj.toString().split("\"")[1]);
						}
						else if (pred.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
						{
						}
						else
						{
							objectVal=obj.toString().split("\"")[1];
							predVal=pred.toString();
						}
					}
					
					//analyse the amount of various messages being exchanged
					//System.out.println("pred msg: " + predVal);
					if (locatedAtVal.contains("agentJState"))
					{
						if (predVal.contains("beliefcount"))
						{
							beliefCountValue++;
						}
						else if (predVal.contains("message"))
						{
							if (predVal.contains("tell"))
							{
								tellCountValue++;
							}
							if (predVal.contains("achieve"))
							{
								achieveCountValue++;
							}
							//System.out.println(objectVal);
							if (objectVal.contains(","))
							{
								final String[] splitMessage = objectVal.split(",");
								String msgSent = splitMessage[1];
								String msgFront = msgSent;
								
								//something strange in splits vs contains with this escaped char.. but as 
								//only using 1st element of array we should be fine...
								final String[] splitSubMessage = msgSent.split("\\(");
								msgFront = splitSubMessage[0];
								
								if (msgFront.equals("accelerating"))
								{
									TimeResPair newPair = new TimeResPair();
									newPair.time = takenAtVal;
									accelerateCount++;
									newPair.res = 0;
									accelerationPairs.add(takenAtVal);
								}
								if (msgFront.equals("braking"))
								{
									TimeResPair newPair = new TimeResPair();
									newPair.time = takenAtVal;
									brakeCount++;
									newPair.res = 0;
									brakePairs.add(takenAtVal);
								}
								
								boolean foundMsg = false;
								for (KnownMessage currentMsg : knownMsg)
								{
									if (currentMsg.msgName.equals(msgFront))
									{
										currentMsg.numOccurances++;
										foundMsg = true;
									}
								}
								if (!foundMsg)
								{
									KnownMessage newMsgType = new KnownMessage();
									newMsgType.msgName = msgFront;
									newMsgType.numOccurances = 1;
									knownMsg.add(newMsgType);
								}
								
							}	
						}
					}
					
					if (locatedAtVal.contains("vehicleSensors"))
					{
						final String[] newData = objectVal.split(",");
						float x = Float.valueOf(newData[0]);
						float y = Float.valueOf(newData[1]);
						float z = Float.valueOf(newData[2]);
						float heading = Float.valueOf(newData[3]);
						if (dataReadingVal.contains("centralMember1"))
						{
							//System.out.println("For leader, at " + takenAtVal + " its at " + x + " " + z);
							KnownVehicles newVehReport = new KnownVehicles();
							newVehReport.timeVal= takenAtVal;
							newVehReport.x = x;
							newVehReport.z = z;
							vehicle1.add(newVehReport);
						}
						else if (dataReadingVal.contains("centralMember2"))
						{
							//System.out.println("For follower1, at " + takenAtVal + " its at " + x + " " + z);
							KnownVehicles newVehReport = new KnownVehicles();
							newVehReport.timeVal= takenAtVal;
							newVehReport.x = x;
							newVehReport.z = z;
							vehicle2.add(newVehReport);
						}
						else if (dataReadingVal.contains("centralMember3"))
						{
							//System.out.println("For follower1, at " + takenAtVal + " its at " + x + " " + z);
							KnownVehicles newVehReport = new KnownVehicles();
							newVehReport.timeVal= takenAtVal;
							newVehReport.x = x;
							newVehReport.z = z;
							vehicle3.add(newVehReport);
						}
						else if (dataReadingVal.contains("centralMember4"))
						{
							//System.out.println("For follower1, at " + takenAtVal + " its at " + x + " " + z);
							KnownVehicles newVehReport = new KnownVehicles();
							newVehReport.timeVal= takenAtVal;
							newVehReport.x = x;
							newVehReport.z = z;
							vehicle4.add(newVehReport);
						}
					}
				}
				System.out.println("leader has " + vehicle1.size() + " time-pos pairs");
				System.out.println("follower 1 has " + vehicle2.size() + " time-pos pairs");
					
				int leaderPositions = vehicle1.size();
				for (int vp = 0; vp < leaderPositions; vp++)
				{
					Long currentSearchTime = vehicle1.get(vp).timeVal;
					float closestTime = 999999;
					int foundAt1 =0;
					for (int vC = 0; vC < vehicle2.size(); vC++)
					{
						Long currentVTime = vehicle2.get(vC).timeVal;
						float deltaV = (float)Math.sqrt((currentSearchTime - currentVTime)*(currentSearchTime - currentVTime));
						if (deltaV < closestTime)
						{
							closestTime=deltaV;
							foundAt1=vC;
						}
					}
					 closestTime = 999999;
					int foundAt2 =0;
					for (int vD = 0; vD < vehicle3.size(); vD++)
					{
						Long currentVTime = vehicle3.get(vD).timeVal;
						float deltaV = (float)Math.sqrt((currentSearchTime - currentVTime)*(currentSearchTime - currentVTime));
						if (deltaV < closestTime)
						{
							closestTime=deltaV;
							foundAt2=vD;
						}
					}
					 closestTime = 999999;
					int foundAt3 =0;
					for (int vE = 0; vE < vehicle4.size(); vE++)
					{
						Long currentVTime = vehicle4.get(vE).timeVal;
						float deltaV = (float)Math.sqrt((currentSearchTime - currentVTime)*(currentSearchTime - currentVTime));
						if (deltaV < closestTime)
						{
							closestTime=deltaV;
							foundAt3=vE;
						}
					}
					
					Point2D leaderPoint = new Point2D(vehicle1.get(vp).x,vehicle1.get(vp).z);
					double d1=0;
					double d2=0;
					double d3=0;
					if (foundAt1 != 0)
					{
						Point2D followerPoint1 = new Point2D(vehicle2.get(foundAt1).x,vehicle2.get(foundAt1).z);
						
						//to find distance to lead convoy vehicle
						d1 = leaderPoint.distance(followerPoint1);
					}
					if (foundAt2 != 0)
					{
						Point2D followerPoint2 = new Point2D(vehicle3.get(foundAt2).x,vehicle3.get(foundAt2).z);
						//comment out below to switch back to convoy leader
						if (foundAt1 != 0)
						{
							Point2D followerPoint1 = new Point2D(vehicle2.get(foundAt1).x,vehicle2.get(foundAt1).z);
							d2 = followerPoint1.distance(followerPoint2);
						}
						//to find distance to lead convoy vehicle
						//d2 = leaderPoint.distance(followerPoint2);
					}
					if (foundAt3 != 0)
					{
						Point2D followerPoint3 = new Point2D(vehicle4.get(foundAt3).x,vehicle4.get(foundAt3).z);
						if (foundAt2 != 0)
						{
							Point2D followerPoint2 = new Point2D(vehicle3.get(foundAt2).x,vehicle3.get(foundAt2).z);
							d3 = followerPoint2.distance(followerPoint3);
						}
						//to find distance to lead convoy vehicle
						//d3 = leaderPoint.distance(followerPoint3);
					}
					allResults.add(currentSearchTime + " , " + d1 + " , " + d2 + " , " + d3);
					
					//System.out.println(currentSearchTime + " , " + leaderPoint.distance(followerPoint1) + " , " + leaderPoint.distance(followerPoint2) + " , " + leaderPoint.distance(followerPoint3));
					//System.out.println("leader at " + vehicle1.get(vp).x +","+ vehicle1.get(vp).z + " and follower at " + vehicle2.get(foundAt).x +","+vehicle2.get(foundAt).z);
					//so go through each position report of the lead vehicle, and find the closest time stamp of the follower vehicle. then find the distance					
				}
				int resultsSize = (allResults.size()-1);
				System.out.println("results size is " + allResults.size() + " so I'm starting at " + resultsSize);
				
				FileWriter fw = new FileWriter("positionReports.csv",false);		
				for (int resPos = resultsSize; resPos >= 0; resPos--)
				{
					try {fw.write(allResults.get(resPos) +" \n");}
					catch (Exception e) {System.out.println("error writing to file");}
				}
				fw.close();
				
				FileWriter fw2 = new FileWriter("messageRates.csv",false);	
				System.out.println("creating messages at time intervals to messageRates.csv...");
				long timePos = simStartTime; //; timePos < simFinTime; timePos+500)
				while (timePos < simFinTime)
				{
					long upperTime = timePos+1000;
					String timeCount2 ="select ?subj ?value where { ?subj <http://bath.edu/sensors/predicates#takenAt> ?value FILTER (?value >= "+timePos+ " && ?value <= " + upperTime + ") }";
					AGTupleQuery tupleQuery2 = conn.prepareTupleQuery(QueryLanguage.SPARQL, timeCount2);
					TupleQueryResult result2 = tupleQuery2.evaluate();
					ArrayList<String> subjectResults2 = new ArrayList<String>();
					while (result2.hasNext()) 
					{
						BindingSet bindingSet2 = result2.next();				
						Value s2 = bindingSet2.getValue("subj");
						subjectResults2.add(s2.toString());
					}
					try {fw2.write(timePos + " , " + subjectResults2.size() +" \n");}
					catch (Exception e) {System.out.println("error writing to file");}
					timePos = upperTime;
				}
				fw2.close();
				
				//this will show convoy vehicle positions..
				System.out.println("creating vehicle position reports to positions.csv");
				FileWriter fw3 = new FileWriter("positions.csv",false);	
				for (int vp = 0; vp < vehicle1.size(); vp++)
				{
					try {fw3.write(vehicle1.get(vp).x + "," + vehicle1.get(vp).z + "," + vehicle2.get(vp).x + "," + vehicle2.get(vp).z + "," + vehicle3.get(vp).x + "," + vehicle3.get(vp).z + "," + vehicle4.get(vp).x + "," + vehicle4.get(vp).z +" \n");}
					catch (Exception e) {System.out.println("error writing to file");}
				}
				fw3.close();
				
				//jason perf data
				FileWriter msgPerf = new FileWriter("messages.csv",false);	
				msgPerf.write("beliefs, " + beliefCountValue +" \n");
				msgPerf.write("tells, " + tellCountValue +" \n");
				msgPerf.write("achieves,: " + achieveCountValue +" \n");
				for (KnownMessage currentMsg : knownMsg)
				{
					msgPerf.write(currentMsg.msgName + ", " + currentMsg.numOccurances +" \n");
				}
				msgPerf.write("brakeArrayList,: " + brakePairs.size() +" \n");
				msgPerf.write("accelerationArrayList,: " + accelerationPairs.size() +" \n");
				msgPerf.close();
				
				Collections.sort(brakePairs);
				FileWriter brakePerf = new FileWriter("brake.csv",false);	
				for (Long currentBrake : brakePairs)
				{
					brakePerf.write(currentBrake +" \n");
				}
				brakePerf.close();
				
				Collections.sort(accelerationPairs);
				FileWriter accPerf = new FileWriter("acceleration.csv",false);	
				for (Long currentAcc : accelerationPairs)
				{
					accPerf.write(currentAcc +" \n");
				}
				accPerf.close();				
				
				//for each leader position, what was the closest each following car ever got to it = how well it followed the route
				System.out.println("creating vehicle position reports to following.csv");
				FileWriter fw4 = new FileWriter("following.csv",false);	
				leaderPositions = vehicle1.size();
				for (int vp = 0; vp < leaderPositions; vp++)
				{
					Point2D leaderPoint = new Point2D(vehicle1.get(vp).x,vehicle1.get(vp).z);
					double closestDistance1 = 999999;
					double testDistance1 = 0;
					for (int vC = 0; vC < vehicle2.size(); vC++)
					{
						Point2D followerPointTest1 = new Point2D(vehicle2.get(vC).x,vehicle2.get(vC).z);
						testDistance1 = leaderPoint.distance(followerPointTest1);
						if (testDistance1 < closestDistance1)
						{
							closestDistance1=testDistance1;
						}
					}
					double closestDistance2 = 999999;
					double testDistance2 = 0;
					for (int vC = 0; vC < vehicle3.size(); vC++)
					{
						Point2D followerPointTest2 = new Point2D(vehicle3.get(vC).x,vehicle3.get(vC).z);
						testDistance2 = leaderPoint.distance(followerPointTest2);
						if (testDistance2 < closestDistance2)
						{
							closestDistance2=testDistance2;
						}
					}
					double closestDistance3 = 999999;
					double testDistance3 = 0;
					for (int vC = 0; vC < vehicle4.size(); vC++)
					{
						Point2D followerPointTest3 = new Point2D(vehicle4.get(vC).x,vehicle4.get(vC).z);
						testDistance3 = leaderPoint.distance(followerPointTest3);
						if (testDistance3 < closestDistance3)
						{
							closestDistance3=testDistance3;
						}
					}
					try {fw4.write(vehicle1.get(vp).timeVal + "," + closestDistance1 + "," + closestDistance2 + "," + closestDistance3 +" \n");}
					catch (Exception e) {System.out.println("error writing to file");}
				}
				fw4.close();
			}	
				
			catch (Exception ee1) 
			{
				ee1.printStackTrace();
			}
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}
		
	}
	
	// TODO: Could all of these static methods be rolled into a single "getAgentGateway" method, 
	// which returns an object which was initialised previously in the agent to interact with GWT?
	public static String getAgraphUsername() {
		return "root";
	}
	
	public static String getAgraphServerURL() {
		return "blah";
	}
	
	public static String getAgraphCatID() {
		return "cat1";
	}
	
	public static String getAgraphRepoID() {
		return "repo1";
	}
	
	public static boolean hasAgraphPassword() {
		return true;
	}
	
	// TODO: Make this encrypted somehow?
	public static void setAgraphPassword(String password) {
		
	}
	
	public static void setAgraphRepoID(String repoID) {
		
	}
	
	public static void setAgraphCatID(String catID) {
		
	}
	
	public static void setAgraphUsername(String username) {
		
	}
	
	public static void setAgraphServerURL(String serverURL) {
		
	}

	public static void addDataSource(String handle) throws RuntimeException {
		throw new RuntimeException("This is a test exception!");
	}
}
