
package edu.bath.rdfUtils.rdfLogger;

import java.io.ByteArrayInputStream;

import org.jivesoftware.smack.XMPPException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.model.*;
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
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.config.RepositoryConfig;

import java.net.URL;
import java.io.*;
import java.util.*;

import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.DataReading;

public class TripleStoreSesameAgent {
	private boolean alive = true;
	private SensorClient sensorClient;
	private String agUsername, agPassword, agCatID, agRepoID;
	private static String XMPPServer = "127.0.0.1";
	private static String DBServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static String agServerURL = "http://127.0.0.1:10035";
	private static String sesameServerURL = "http://127.0.0.1:8040";
	/*private AGServer aGserver;
	private AGCatalog aGcatalogue;
	private AGRepository aGrepository;
	private AGRepositoryConnection aGconn;*/
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String homeSensors = "homeSensor";
	private static String aoiNodeName = "aoiSensor";
	private static String nodeName;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static boolean useALLEGRO=false;
	private static boolean useSESAME=false;
	private static boolean ignoreHistoricalTimeStamps=true;
	private static int timeLimit = 5000; //5 seconds to allow for non sync'd clocks.. 
	private static TripleStoreSesameAgent tsa;
	private static Repository sesameRepo;
	private static RepositoryConnection sesameRepoConnection;
	
	public static void main(String[] args) throws Exception {
		//get IP addressed from config file
		BufferedReader br = new BufferedReader(new FileReader("config.txt"));
		String line;
		while((line = br.readLine()) != null) 
		{
			if (line.contains("OPENFIRE"))
			{
				String[] configArray = line.split("=");
				XMPPServer = configArray[1];
				System.out.println("Using config declared IP address of rdf store as: " + XMPPServer);
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
			tsa = new TripleStoreSesameAgent(XMPPServer, "tripleStore", "jasonpassword", jasonSensorVehicles);
		}
		else if (useMQTT)
		{
			tsa = new TripleStoreSesameAgent(XMPPServer, "tripleStore", "jasonpassword", jasonSensorVehicles, true, 0);
		}
		tsa.run();
	}
	
	public TripleStoreSesameAgent(String serverAddress, String id, String password, String nodeName) throws XMPPException {
		sensorClient = new SensorXMPPClient(serverAddress, id, password);
		String jstateID = new String(id+"-jstate");
		this.nodeName = nodeName;
	}

	public TripleStoreSesameAgent(String serverAddress, String id, String password, String nodeName, boolean useMQTT, int qos) throws XMPPException {
		sensorClient = new SensorMQTTClient(serverAddress, id);
		String jstateID = new String(id+"-jstate");
		this.nodeName = nodeName;
	}

	public void run() {
		// TODO: Load allgeograph server settings from config file
		//agServerURL = "http://192.168.0.8:10035";
		agUsername = "super";
		agPassword = "jasonpassword";
		agCatID = "java-catalog";
		agRepoID = "SensorData";
		if (useSESAME)
		{
			try
			{
				//sesameRepo = new HTTPRepository(sesameServerURL, agRepoID);
				//sesameRepo.initialize();
				RemoteRepositoryManager manager = new RemoteRepositoryManager(sesameServerURL);
				manager.initialize();
				Repository sensorRepo = manager.getRepository(agRepoID);
				System.out.println("Sesame server contains: " + manager.getAllRepositories().size() + " repositories");
				if (sensorRepo != null )
				{
					System.out.println("connected to " + agRepoID + " repo");
					
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
					System.out.println(agRepoID + " not created yet, making it..");
					//boolean persist=true;
					String indexes = "spoc,posc,cspo";
					SailImplConfig backendConfig = new NativeStoreConfig(indexes);
					//use this if you want to switch to in memory storage instead
					//SailImplConfig backendConfig = new MemoryStoreConfig(persist);
					RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
					RepositoryConfig repConfig = new RepositoryConfig(agRepoID, repositoryTypeSpec);
					manager.addRepositoryConfig(repConfig);
					sensorRepo = manager.getRepository(agRepoID);
					sesameRepoConnection = sensorRepo.getConnection();
				}

			}
			catch (Exception rE)
			{
				System.out.println("repo exception..");
				rE.printStackTrace();
			}

		}

		else if (useALLEGRO)
		{
			/*server = new AGServer(agServerURL, agUsername, agPassword);
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
			} catch (RepositoryException e1) {
				e1.printStackTrace();
			}*/
		}
		
		try {
			sensorClient.subscribe(nodeName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sensorClient.subscribe(jasonSensorStates);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sensorClient.subscribe(jasonSensorVehicles);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sensorClient.subscribe(jasonSensorVehiclesCmds);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sensorClient.subscribe(aoiNodeName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		System.out.println("Started");
		
		sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					if (useALLEGRO)
					{
						//aGconn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
					}
					else if (useSESAME)
					{
						logToSesame(rdf);
					}
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + jasonSensorVehicles);
					e.printStackTrace();
				}
			}
		});

		sensorClient.addHandler(jasonSensorVehiclesCmds, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					if (useALLEGRO)
					{
						//aGconn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
					}
					else if (useSESAME)
					{
						logToSesame(rdf);
					}
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + jasonSensorVehiclesCmds);
					e.printStackTrace();
				}
			}
		});

		sensorClient.addHandler(aoiNodeName, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					if (useALLEGRO)
					{
						//aGconn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
					}
					else if (useSESAME)
					{
						logToSesame(rdf);
					}
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + aoiNodeName);
					e.printStackTrace();
				}
			}
		});
		
		sensorClient.addHandler(jasonSensorStates, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					if (useALLEGRO)
					{
						//aGconn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
					}
					else if (useSESAME)
					{
						logToSesame(rdf);
					}
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + jasonSensorStates);
					e.printStackTrace();
				}
			}
		});

		sensorClient.addHandler(homeSensors, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					if (useALLEGRO)
					{
						//aGconn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
					}
					else if (useSESAME)
					{
						logToSesame(rdf);
					}
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + homeSensors);
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("starting logging from epoch time in ms: " + System.currentTimeMillis());
		
		while(alive) 
		{
        		// Wait for until more data can have come in
        		try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				System.out.println("problem during wait..");
				e.printStackTrace();
			}
        	}
	}


	private void logToSesame(String rdf)
	{
		if (ignoreHistoricalTimeStamps)
		{
			//only worth checking if we would ignore it anyway
			try 
			{
				DataReading dr = DataReading.fromRDF(rdf);
				long timeToCheck = System.currentTimeMillis() - timeLimit;
				long gap = System.currentTimeMillis() - dr.getTimestamp();
				if (dr.getTimestamp() < timeToCheck)
				{
					//System.out.println("ignored reading older than " + timeLimit);
				}
				else
				{
					/*if (dr.toRDF().contains("HueSensors"))
					{
						System.out.println(dr.toRDF());
					}*/
					sesameRepoConnection.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
				}
			}
			catch(Exception e3) 
			{ 
				System.out.println("some issue with historical check, problematic RDF was: ");
				System.out.println(rdf);
				System.out.println("Stack: ");
				e3.printStackTrace();
			}
		}
		else
		{
			try
			{
				sesameRepoConnection.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
			}
			catch(Exception e4) 
			{ 
				System.out.println("some issue with historical check, problematic RDF was: ");
				System.out.println(rdf);
				System.out.println("Stack: ");
				e4.printStackTrace();
			}
		}
	}
}
