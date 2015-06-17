package edu.bath.rdfUtils.rdfReplayer;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

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
import java.io.*;

import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;


public class RDFReplayAgent {

	class TimeResPair{
		public Long time;
		public String res;
	}
	
	private static ArrayList<TimeResPair> myPairs = new ArrayList<TimeResPair>();
	
	private boolean alive = true;

	private static SensorClient mySimSensorClient;

	private static String agServerURL, agUsername, agPassword, agCatID, agRepoID;
	
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String simSensorName = "simStateSensor";
	private static WorkerSimNonThreadSender simThreadSender;
	
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	
	private static boolean useLocalFile=true;
	private static String fileName = "Sensor.nt";
	
	private static Model model; 
	
	//orig good start point here...
	//private long simStartTime=1345914767112L;
	//jstates sent here..
	//private long simStartTime=1350143023000L;
	private static long pushScenario2StartTime=1350416205000L;
	//private long pullScenario3StartTime=1350827502000L;
	//pull with jstate, cars 3 and 4 go AWOL

	private static long pullScenario3StartTime=1350827502000L;
	private static long pullScenario3FinTime=1350827670000L;
	
	private static long pushFullRoute1Start=1365939090000L;
	private static long   pullFullRoute1Fin=1365939846000L;
	
	private static long msgRate333Start=1369248278214L;
	private static long  msgRate3331Fin=1369248312000L;
	
	private static long msgRate167Start=1369248541682L;
	private static long  msgRate167Fin=1369248578000L;
									  
	private static long simStartTime = msgRate167Start;
	private static long simFinishTime = msgRate167Fin;
	
	private int intervalTime=1000;
	
	private AGServer server;
	private AGCatalog catalogue;
	private AGRepository repository;
	private AGRepositoryConnection conn;
	private WorkerNonThreadSender myVehicleSimSender;
	private WorkerNonThreadSender myJStateSimSender;
	private boolean simPaused=false;
	private boolean simForward=true;
	
	public static void main(String[] args) throws Exception 
	{
		if (args.length == 2 && args[1].equals("example"))
		{
			System.out.println("replaying from example scenario file");
			simStartTime=pushFullRoute1Start;
			simFinishTime=pullScenario3FinTime;
			useLocalFile=true;
			fileName = "Sensor.nt";
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
			if (line.contains("ALLEGROGRAPH"))
			{
				String[] configArray = line.split("=");
				agServer = configArray[1];
				agServerURL = new String("http://" + agServer + ":10035");
				System.out.println("Using config declared IP address of AllegroGraph server as: " + agServer);
			}
		}
		
		while(mySimSensorClient == null) {
			try {
				mySimSensorClient = new SensorXMPPClient(XMPPServer, "rdfreplay", "jasonpassword");
				System.out.println("Sim Sensor connected up OK");
			} catch (XMPPException e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}
		
		try 
		{
			simThreadSender = new WorkerSimNonThreadSender(XMPPServer, "rdfreplaysimstatesender", "jasonpassword", "simStateSensor", "http://127.0.0.1/localSensors", "http://127.0.0.1/localSensors/viewerSender");
		}
		catch (Exception e) 
		{
			System.out.println("couldn't start sim thread sender");
			System.out.println(e.getStackTrace());
		}	
		
		RDFReplayAgent replayAgent = new RDFReplayAgent();
		replayAgent.run();
	}
	
	private String nodeName;// TODO: Remove this, it is just a debugging option
	public RDFReplayAgent() {//throws XMPPException {

	}
	
	public void simPause() {
		System.out.println("RDF replay agent has been set to paused");
		simPaused=true;
	}
	
	public void simPlay() {
		System.out.println("RDF replay agent has been set to play");
		simPaused=false;
		simForward=true;
	}
	public void simRewind() {
		System.out.println("RDF replay agent has been set to rewind");
		simPaused=false;
		simForward=false;
	}

	public void run() 
	{	
		mySimSensorClient.addHandler(simSensorName, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				//System.out.println("handle incomming");
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					//Value simStateVal = dr.findFirstValue(null, "http://127.0.0.1/request/simState", null);
					if(dr.findFirstValue(null, "http://127.0.0.1/request/simState", null) != null) 
					{		
						String tempReading = (String)dr.findFirstValue(null, "http://127.0.0.1/request/simState", null).object;
					
						System.out.println("simState msg received, asked for a: " + tempReading);
						if (tempReading.equals("pause"))
						{
							System.out.println("Sending pause to parent");
							simPause();
						}
						else if (tempReading.equals("play"))
						{
							System.out.println("Sending play to parent");
							simPlay();
						}
						else if (tempReading.equals("rewind"))
						{
							System.out.println("Sending rewind to parent");
							simRewind();
						}
					}
				}catch(Exception e) {}
			}
		});
		try {
			mySimSensorClient.subscribe(simSensorName);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor.");
			e1.printStackTrace();
		}
		
		try 
		{
			myVehicleSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender", "jasonpassword", jasonSensorVehicles);
			myJStateSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender-jstate", "jasonpassword", jasonSensorStates);
		}
		catch (Exception newEr) {
			newEr.printStackTrace();
		}
	
		if (!useLocalFile)
		{
			agUsername = "super";
			agPassword = "jasonpassword";
			agCatID = "java-catalog";
			agRepoID = "SensorData";
	
			server = new AGServer(agServerURL, agUsername, agPassword);
			try 
			{
				System.out.println("available cats: " + server.listCatalogs());
				catalogue = server.getCatalog(agCatID);
			} catch (Exception newE) 
			{
				newE.printStackTrace();
			}
			try 
			{
				repository = catalogue.createRepository(agRepoID);
				repository.initialize();
				conn = repository.getConnection();
				System.out.println("Repository " + (repository.getRepositoryID()) + " is up! It contains " + (conn.size()) + " statements.");
			} catch (RepositoryException e1) 
			{
				e1.printStackTrace();
			}	
		}
		else if (useLocalFile)
		{
			System.out.println("loading file to memory...");
			Long startTime = System.currentTimeMillis();
			model= ModelFactory.createDefaultModel();
			InputStream in= FileManager.get().open(fileName);
			model.read(in, "", "N-TRIPLES");
			Long readTime = System.currentTimeMillis() - startTime;
			
			
			System.out.println("loaded file");
			
			//build up whole list of messages and their time values in the range we're interested in
			//TODO optimise for including end time of run??
			String fullTimeBoundedString = "select ?subj ?value where { ?subj <http://bath.edu/sensors/predicates#takenAt> ?value FILTER (?value >= " + simStartTime + ") } ";
			ArrayList<String> fullSubjectResults = new ArrayList<String>();
			QueryExecution fullqexec = QueryExecutionFactory.create(fullTimeBoundedString, model);
			try {
				ResultSet fullResults = fullqexec.execSelect() ;
				for ( ; fullResults.hasNext() ; )
				{
					QuerySolution fullSoln = fullResults.nextSolution();
					Resource res = fullSoln.getResource("subj"); 
					//subjectResults.add(res.toString());
					Literal lit = fullSoln.getLiteral("value"); 
					TimeResPair newPair = new TimeResPair();
					newPair.time = Long.valueOf(lit.toString().split("\\^\\^")[0]);
					newPair.res = res.toString();
					myPairs.add(newPair);
					//System.out.println(lit.toString());
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			finally { fullqexec.close() ; }
			System.out.println("read file and it contains: " + model.size() + " entries and took " + readTime + "ms to load");
			System.out.println("got " + myPairs.size() + " results");
		}
		
		while(alive) 
		{
			try 
			{
				if (!simPaused) 
				{
					String queryEverythingString = "SELECT ?s ?p ?o  WHERE {?s ?p ?o .}";
					long finTime = simStartTime+intervalTime;
					String timeBoundedString = "select ?subj where { ?subj <http://bath.edu/sensors/predicates#takenAt> ?value FILTER (?value >= " + simStartTime + " && ?value <= " + finTime + ") } ";
					ArrayList<String> subjectResults = new ArrayList<String>();
					
					Long query1StartTime = System.currentTimeMillis();
					if (useLocalFile)
					{
						for (TimeResPair currPair : myPairs)
						{
							if ((currPair.time >= simStartTime) && (currPair.time <= finTime))
							{
								subjectResults.add(currPair.res);
							}
						}
						/*QueryExecution qexec = QueryExecutionFactory.create(timeBoundedString, model);
						try {
							ResultSet results = qexec.execSelect() ;
							for ( ; results.hasNext() ; )
							{
								QuerySolution soln = results.nextSolution();
								Resource r = soln.getResource("subj"); 
								subjectResults.add(r.toString());
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
						finally { qexec.close() ; }*/
					}
					else
					{
						
						AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, timeBoundedString);
						TupleQueryResult result = tupleQuery.evaluate();
						while (result.hasNext()) 
						{
							BindingSet bindingSet = result.next();				
							Value s = bindingSet.getValue("subj");
							subjectResults.add(s.toString());
						}
					}
					System.out.println("Found " + subjectResults.size() + " results in this time period");
					Long query1ReadTime = System.currentTimeMillis() - query1StartTime;
					System.out.println("Which took " + query1ReadTime + "ms to find");
					String resultCount = new String("" + subjectResults.size());
					simThreadSender.addMessageToSend("simMsgCount", resultCount);
			
					Long query2StartTime = System.currentTimeMillis();
					for (String temp : subjectResults) 
					{
						String drQueryString ="select * where { <" + temp + "> ?pred ?obj . }";
						String locatedAtVal = "";
						String predVal = "";
						String dataReadingVal = "";
						String objectVal="";
						long takenAtVal=0;
						
						if (useLocalFile)
						{
							QueryExecution qexec = QueryExecutionFactory.create(drQueryString, model);
							try {
								ResultSet results = qexec.execSelect() ;
								for ( ; results.hasNext() ; )
								{
									QuerySolution soln = results.nextSolution() ;
									
									RDFNode objFound = soln.get("obj");      
									RDFNode predFound = soln.get("pred"); 
																			
									if (predFound.toString().equals("http://bath.edu/sensors/predicates#locatedAt"))
									{
										String cleanedObj = objFound.toString();
										if(cleanedObj.contains("^^"))
										{
											cleanedObj = objFound.toString().split("\\^\\^")[0];
										}
										//System.out.println("Setting locatedAt to be: " + cleanedObj);
										locatedAtVal=cleanedObj;
									}
									else if (predFound.toString().equals("http://bath.edu/sensors/predicates#isDataReading"))
									{
										String cleanedObj = objFound.toString();
										if(cleanedObj.contains("^^"))
										{
											cleanedObj = objFound.toString().split("\\^\\^")[0];
										}
										//System.out.println("Setting dataReadingVal to be: " + cleanedObj);
										dataReadingVal=cleanedObj;
									}
									else if (predFound.toString().equals("http://bath.edu/sensors/predicates#takenAt"))
									{
										String cleanedObj = objFound.toString();
										if(cleanedObj.contains("^^"))
										{
											cleanedObj = objFound.toString().split("\\^\\^")[0];
										}
										//System.out.println("Setting takenAtVal to be: " + cleanedObj);
										takenAtVal = Long.valueOf(cleanedObj);
									}
									else if (predFound.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
									{
									}
									else
									{
										String cleanedObj = objFound.toString();
										if(cleanedObj.contains("^^"))
										{
											cleanedObj = objFound.toString().split("\\^\\^")[0];
										}
										//System.out.println("Setting object to be: " + cleanedObj);
										objectVal=cleanedObj;
										
										String cleanedPred = predFound.toString();
										if(cleanedPred.contains("^^"))
										{
											cleanedPred = predFound.toString().split("\\^\\^")[0];
										}
										//System.out.println("Setting pred to be: " + cleanedPred);
										predVal=cleanedPred;
									}
							
								}
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
							finally { qexec.close() ; }
						}
						else
						{
							AGTupleQuery drTupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, drQueryString);
							TupleQueryResult drResult = drTupleQuery.evaluate();

							while (drResult.hasNext()) 
							{
								BindingSet drBindingSet = drResult.next();
								Value pred = drBindingSet.getValue("pred");
								Value obj = drBindingSet.getValue("obj");
					
								if (pred.toString().equals("http://bath.edu/sensors/predicates#locatedAt"))
								{
									//System.out.println("Setting locatedAt to be: " + obj.toString());
									locatedAtVal=obj.toString();
								}
								else if (pred.toString().equals("http://bath.edu/sensors/predicates#isDataReading"))
								{
									//System.out.println("Setting dataReadingVal to be: " + obj.toString());
									dataReadingVal=obj.toString();
								}
								else if (pred.toString().equals("http://bath.edu/sensors/predicates#takenAt"))
								{
									//System.out.println("Setting takenAtVal to be: " + obj.toString().split("\"")[1]);
									//String tempTakenAt=obj.toString();
									//System.out.println(obj.toString().split("\"")[1]);
									takenAtVal = Long.valueOf(obj.toString().split("\"")[1]);
								}
								else if (pred.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
								{
								}
								else
								{
									//System.out.println("Setting object to be: " + obj.toString().split("\"")[1]);
									objectVal=obj.toString().split("\"")[1];
									//System.out.println("Setting pred to be: " + pred.toString());
									predVal=pred.toString();
								}
							}
						}
						try 
						{
							DataReading testReading = new DataReading(dataReadingVal, locatedAtVal, takenAtVal);
							testReading.addDataValue(null, predVal, objectVal, false);
							
							if (locatedAtVal.contains("agentJState"))
							{
								myJStateSimSender.addMessageToSend(testReading);
								myJStateSimSender.send();
							}
							else if (locatedAtVal.contains("vehicleSensors"))
							{
								myVehicleSimSender.addMessageToSend(testReading);
								myVehicleSimSender.send();
							}
							else
							{
								System.out.println("couldn't handle: " + locatedAtVal);
							}
						}	
						catch (Exception e) 
						{
							e.printStackTrace();
						}	
					}
					Long query2ReadTime = System.currentTimeMillis() - query2StartTime;
					System.out.println("And it took " + query2ReadTime + "ms to find the RDF data and send it");
				}
				Thread.sleep(intervalTime);

				if (simPaused)
				{
					System.out.println("next time step, simulation is paused..." + simStartTime);	
					simThreadSender.addMessageToSend("simTime", new String("" + simStartTime));
					simThreadSender.send();
				}
				else if (!simPaused & simForward)
				{
					System.out.println("next time step in sim..." + simStartTime);
					simStartTime=simStartTime+intervalTime;
					simThreadSender.addMessageToSend("simTime", new String("" + simStartTime));
					simThreadSender.send();
				}
				else if (!simPaused & !simForward)
				{
					System.out.println("previous time step in sim..." + simStartTime);
					simStartTime=simStartTime-intervalTime;
					simThreadSender.addMessageToSend("simTime", new String("" + simStartTime));
					simThreadSender.send();
				}
			}
			catch (Exception ee1) 
			{
				ee1.printStackTrace();
			}
		}
	}
	
	public static void processSimStateXMPPData(String pred, String newItem, String simName)
	{
		System.out.println("RDF Replay Agent attempting to process " + pred + ", " + newItem + ", " + simName);
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
