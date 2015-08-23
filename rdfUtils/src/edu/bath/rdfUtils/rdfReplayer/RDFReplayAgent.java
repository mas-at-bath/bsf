package edu.bath.rdfUtils.rdfReplayer;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;

import org.jivesoftware.smack.XMPPException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.*;
import org.openrdf.rio.RDFFormat;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.config.RepositoryConfig;

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
	class PredObjPair{
		public String pred;
		public Serializable obj;
	}
	
	private static ArrayList<PredObjPair> myPredObjPairs = new ArrayList<PredObjPair>();
	private static ArrayList<TimeResPair> myPairs = new ArrayList<TimeResPair>();
	
	private boolean alive = true;

	private static SensorClient mySimSensorClient;

	private static String agServerURL, agUsername, agPassword, agCatID, agRepoID;
	
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String homeSensors = "homeSensor";
	private static String simSensorName = "simStateSensor";
	private static WorkerSimNonThreadSender simThreadSender;
	
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static String DBServer = "127.0.0.1";
	private static String sesameServerURL = "http://127.0.0.1:8040";
	
	private static boolean useLocalFile=false;
	private static String fileName = "Sensor.nt";

	private static final long nanoToMili=1000000;

	
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
	private static long pullFullRoute1Fin=1365939846000L;
	
	private static long msgRate333Start=1369248278214L;
	private static long msgRate3331Fin=1369248312000L;
	
	private static long msgRate167Start=1369248541682L;
	private static long msgRate167Fin=1369248578000L;

	private static long trafficSimTestStart = 1436713489862L;
	private static long trafficSimTestFin = 1436713465722L;

	//private static long houseStart = 1439845200000L;
	private static long houseStart = 1440362470000L;
	private static long houseFin = 1440363470000L;
									  
	private static long simStartTime = houseStart;
	private static long simFinishTime = houseFin;
	
	private int publishDelayTime=1000;
	private int intervalTime= 60000; //60000 is then 1 minute of data to be replayed in publishDelayTime window as realtime
	private double lastMsgTimeStamp = 0L;
	private AGServer server;
	private AGCatalog catalogue;
	private AGRepository repository;
	private AGRepositoryConnection conn;
	private WorkerNonThreadSender myVehicleSimSender;
	private WorkerNonThreadSender myJStateSimSender;
	private WorkerNonThreadSender myHouseSimSender;
	private boolean simPaused=false;
	private boolean simForward=true;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static boolean useALLEGRO=false;
	private static boolean useSESAME=false;
	private static Repository sesameRepo;
	private static RepositoryConnection sesameRepoConnection;
	
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
			if (line.contains("RDFSTORE"))
			{
				String[] configArray = line.split("=");
				DBServer= configArray[1];
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
			if (line.contains("DATABASE"))
			{
				String[] configArray = line.split("=");
				if(configArray[1].equals("ALLEGROGRAPH"))
				{
					useALLEGRO=true;
					agServer = DBServer;
					agServerURL = new String("http://" + agServer + ":10035");
					System.out.println("Using config declared IP address of AllegroGraph server as: " + agServer);
				}
				else if(configArray[1].equals("SESAME"))
				{
					useSESAME=true;
					sesameServerURL = new String("http://" + DBServer + ":8040/openrdf-sesame");
				}
				//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
			}
		}
		if (!useMQTT && !useXMPP)
		{
			System.out.println("no COMMUNICATION value found in config.txt, should be = MQTT or XMPP");
			System.exit(1);
		}
		if (!useALLEGRO && !useSESAME)
		{
			System.out.println("no DATABASE value found in config.txt, should be = ALLEGROGRAPH or SESAME");
			System.exit(1);
		}
		
		if (useXMPP)
		{
			while(mySimSensorClient == null) {
				try {
					mySimSensorClient = new SensorXMPPClient(XMPPServer, "rdfreplay", "jasonpassword");
					System.out.println("Sim Sensor connected up OK");
				} catch (XMPPException e1) {
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
				}
			}
		}
		else if (useMQTT)
		{
			try {
				mySimSensorClient = new SensorMQTTClient(XMPPServer, "rdfreplay");
				System.out.println("Sim Sensor connected up OK");
			} catch (Exception e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}
		
		try 
		{
			if (useXMPP)
			{
				simThreadSender = new WorkerSimNonThreadSender(XMPPServer, "rdfreplaysimstatesender", "jasonpassword", "simStateSensor", "http://127.0.0.1/localSensors", "http://127.0.0.1/localSensors/viewerSender");
			}
			else if (useMQTT)
			{
				simThreadSender = new WorkerSimNonThreadSender(XMPPServer, "rdfreplaysimstatesender", "jasonpassword", "simStateSensor", "http://127.0.0.1/localSensors", "http://127.0.0.1/localSensors/viewerSender", true, 0);
			}
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
				}
				catch(Exception e) 
				{
					System.out.println("error handlings incoming sim state");
					e.printStackTrace();
				}
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
			if (useXMPP)
			{
				myVehicleSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender", "jasonpassword", jasonSensorVehicles);
				myJStateSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender-jstate", "jasonpassword", jasonSensorStates);
				myHouseSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender-house", "jasonpassword", homeSensors);
			}
			else if (useMQTT)
			{
				myVehicleSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender", "jasonpassword", jasonSensorVehicles, true, 0);
				myJStateSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender-jstate", "jasonpassword", jasonSensorStates, true, 0);
				myHouseSimSender = new WorkerNonThreadSender(XMPPServer, "xmppsimsender-house", "jasonpassword", homeSensors, true, 0);
			}
		}
		catch (Exception newEr) {
			System.out.println("error creating Sim sensors..");
			newEr.printStackTrace();
		}
	
		if (!useLocalFile)
		{
			agUsername = "super";
			agPassword = "jasonpassword";
			agCatID = "java-catalog";
			agRepoID = "SensorData";
	
			if (useALLEGRO)
			{
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
			else if (useSESAME)
			{
				try
				{
					RemoteRepositoryManager manager = new RemoteRepositoryManager(sesameServerURL);
					manager.initialize();
					Repository sensorRepo = manager.getRepository(agRepoID);
					System.out.println("Sesame server contains: " + manager.getAllRepositories().size() + " repositories");
					if (sensorRepo != null )
					{
						sesameRepoConnection = sensorRepo.getConnection();
						RepositoryResult<Statement> allStatements = sesameRepoConnection.getStatements(null, null, null, true);
						int statementCount=0;
						while (allStatements.hasNext())
						{
							statementCount++;
							allStatements.next();
						}
						System.out.println(agRepoID + " repo contains " + statementCount + " statements");
					}
					else
					{
						System.out.println(agRepoID + " cannot be found!!");
						System.exit(1);
					}

				}
				catch (Exception rE)
				{
					System.out.println("repo exception..");
					System.out.println(rE.getMessage());
					rE.printStackTrace();
				}
			}
		}
		else if (useLocalFile)
		{
			System.out.println("loading file to memory...");
			Long startTime = System.currentTimeMillis();
			model= ModelFactory.createDefaultModel();
			File f = new File(fileName);
			if (!f.exists())
			{
				System.out.println("Sensor.nt file not found, please download it or select DB server instead of file");
				System.exit(1);
			}
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
			long intervalStartTime = 0L;
				if (!simPaused) 
				{
					intervalStartTime = System.nanoTime();
					String queryEverythingString = "SELECT ?s ?p ?o  WHERE {?s ?p ?o .}";
					long finTime = simStartTime+intervalTime;
					String timeBoundedString = "select ?subj where { ?subj <http://bath.edu/sensors/predicates#takenAt> ?value FILTER (?value >= " + simStartTime + " && ?value <= " + finTime + ") } order by ?value";
					//System.out.println(timeBoundedString);
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
					}
					else
					{
						if (useALLEGRO)
						{	
							AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, timeBoundedString);
							try
							{
								TupleQueryResult result = tupleQuery.evaluate();
								while (result.hasNext()) 
								{
									BindingSet bindingSet = result.next();				
									Value s = bindingSet.getValue("subj");
									subjectResults.add(s.toString());
								}
							}
							catch (Exception e) 
							{
								System.out.println("Error in getting allegro result");
								e.printStackTrace();
							}
							
						}
						else if (useSESAME)
						{	
							//AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, timeBoundedString);
							try
							{
								TupleQuery tupleQuery = sesameRepoConnection.prepareTupleQuery(QueryLanguage.SPARQL, timeBoundedString);
								//System.out.println(timeBoundedString);
								TupleQueryResult result = tupleQuery.evaluate();
								while (result.hasNext()) 
								{
									BindingSet bindingSet = result.next();				
									Value s = bindingSet.getValue("subj");
									subjectResults.add(s.toString());
								}
							}
							catch (Exception e) 
							{
								System.out.println("Error getting sesame tuple result");
								e.printStackTrace();
							}
						}
					}
					System.out.println("Found " + subjectResults.size() + " results in this time period");
					Long query1ReadTime = System.currentTimeMillis() - query1StartTime;
					System.out.println("Which took " + query1ReadTime + "ms to find");
					String resultCount = new String("" + subjectResults.size());
					simThreadSender.addMessageToSend("simMsgCount", resultCount);
			
					Long query2StartTime = System.currentTimeMillis();
					for (String tempURI : subjectResults) 
					{
						String drQueryString ="select * where { <" + tempURI + "> ?pred ?obj . }";
						//System.out.println("temp query: " + drQueryString);
						String locatedAtVal = "";
						String predVal = "";
						String dataReadingVal = "";
						String objectVal="";
						long takenAtVal=0;
						
						if (useLocalFile)
						{
							myPredObjPairs.clear();
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
										PredObjPair tempPair = new PredObjPair();
										tempPair.pred=predVal;
										tempPair.obj=objectVal;
										myPredObjPairs.add(tempPair);
									}
							
								}
							} 
							catch (Exception e) {
								System.out.println("error getting statements from local file");
								e.printStackTrace();
							}
							finally { qexec.close() ; }
						}
						else //use an RDF database
						{
							TupleQueryResult drResult = null;
							if (useALLEGRO)
							{
								AGTupleQuery drTupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, drQueryString);
								try
								{
									drResult = drTupleQuery.evaluate();
								}
								catch (Exception e) 
								{
									System.out.println("Error evaluating allegro tuple result");
									e.printStackTrace();
								}
							}
							else if (useSESAME)
							{
								try
								{
									TupleQuery drTupleQuery = sesameRepoConnection.prepareTupleQuery(QueryLanguage.SPARQL, drQueryString);
									drResult = drTupleQuery.evaluate();
								}
								catch (Exception e) 
								{
									System.out.println("Error evaluating sesame tuple result");
									e.printStackTrace();
								}
							}
							//TupleQueryResult drResult = drTupleQuery.evaluate();
							//System.out.println("has " + drResult.getBindingNames().size());
							try
							{
								myPredObjPairs.clear();
								while (drResult.hasNext()) 
								{
									BindingSet drBindingSet = drResult.next();
									Value pred = drBindingSet.getValue("pred");
									Value obj = drBindingSet.getValue("obj");
									//TODO: this could perhaps be tidied up with Literals and then
									//getting their URI, rather than breaking up the strings.
									//achieves similar result just a bit safer..

									if ((pred !=null) && (obj != null))
									{
										if (pred.toString().equals("http://bath.edu/sensors/predicates#locatedAt"))
										{
											String cleanedObj = obj.toString();
											if(cleanedObj.contains("^^"))
											{
												cleanedObj = obj.toString().split("\\^\\^")[0];
											}
										
											locatedAtVal=cleanedObj;
											//System.out.println("Setting locatedAt to be: " + locatedAtVal);
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
											//System.out.println(obj.getDatatype());
											String typeURI = obj.toString().split("\\^")[2];
											objectVal=obj.toString().split("\"")[1];
											//System.out.println(typeURI);
											predVal=pred.toString();
											PredObjPair tempPair = new PredObjPair();

											if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#int>"))
												tempPair.obj = Integer.parseInt(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#long>"))
												tempPair.obj = Long.parseLong(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#byte>"))
												tempPair.obj = Byte.parseByte(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#boolean>"))
												tempPair.obj = Boolean.parseBoolean(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#double>"))
												tempPair.obj = Double.parseDouble(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#float>"))
												tempPair.obj = Float.parseFloat(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#short>"))
												tempPair.obj = Short.parseShort(objectVal);
											else if(typeURI.equals("<http://www.w3.org/2001/XMLSchema#string>"))
												tempPair.obj = objectVal;
											else
											{
												System.out.println("didnt handle type: " + typeURI);
												tempPair.obj = objectVal;
											}


											tempPair.pred=predVal;
											myPredObjPairs.add(tempPair);
										}
									}
								}
							}
							catch (Exception e) 
							{
								System.out.println("Error getting drNext result");
								e.printStackTrace();
							}

						}
						try 
						{
							DataReading testReading = new DataReading(dataReadingVal, locatedAtVal, takenAtVal);
							testReading.setURI(tempURI);							
							//testReading.addDataValue(null, predVal, objectVal, false);

							for (PredObjPair foundPair : myPredObjPairs)
							{
								//System.out.println("adding " + foundPair.pred);
								testReading.addDataValue(null, foundPair.pred, foundPair.obj, false);
							}
							
							lastMsgTimeStamp = takenAtVal;
							double intervalElapsedMsgRatio = (lastMsgTimeStamp - simStartTime)/intervalTime;
							float elapsedTime = (System.nanoTime() - intervalStartTime)/(nanoToMili);
							double targetTime = publishDelayTime*intervalElapsedMsgRatio;


							//System.out.println("ratio of interval time elapsed in step for this msg: " + intervalElapsedMsgRatio);
							//System.out.println("time elapsed in realtime for this msg: " + elapsedTime);
							//System.out.println("target msg send time would have been: " + targetTime);
							
							if (targetTime > elapsedTime)
							{
								try
								{
									Double sleepVal = targetTime - elapsedTime;
									Thread.sleep(sleepVal.intValue());
								}
								catch (Exception e) 
								{
									System.out.println("Error in message delay sleep");
									e.printStackTrace();
								}
							}

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
							else if (locatedAtVal.contains("PiSensors") || locatedAtVal.contains("HueSensors"))
							{
								myHouseSimSender.addMessageToSend(testReading);
								myHouseSimSender.send();
								/*if (locatedAtVal.contains("HueSensors") )
								{
									System.out.println("temp query: " + drQueryString);
									System.out.println(testReading.toRDF());
								}*/
							}
							else
							{
								System.out.println("couldn't handle: " + locatedAtVal);
							}
						}	
						catch (Exception e) 
						{
							System.out.println("Error in publishing Data Reading");
							e.printStackTrace();
						}	
					}
					Long query2ReadTime = System.currentTimeMillis() - query2StartTime;
					System.out.println("And it took " + query2ReadTime + "ms to find the RDF data and send it");
				}
				
				try
				{
					double elapsedLoopTime = (System.nanoTime() - intervalStartTime)/nanoToMili;
					Double remainingSleep = publishDelayTime - elapsedLoopTime;
					//System.out.println("need to sleep for: " + remainingSleep);
					if (remainingSleep > 0)
					{
						Thread.sleep(remainingSleep.intValue());
					}
					else
					{
						System.out.println("WARNING: overshot this time window by " + remainingSleep + "ms");
					}
				}
				catch (Exception e) 
				{
					System.out.println("Error in main sleep");
					e.printStackTrace();
				}	

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
