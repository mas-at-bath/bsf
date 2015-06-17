package edu.bath.rdfUtils.rdfLogger;

import java.io.ByteArrayInputStream;

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
import java.io.BufferedReader;
import java.io.FileReader;

import edu.bath.sensorframework.client.*;

public class TripleStoreAgent {
	private boolean alive = true;
	private SensorClient sensorClient;
	private String agUsername, agPassword, agCatID, agRepoID;
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static String agServerURL = "http://127.0.0.1:10035";
	
	private AGServer server;
	private AGCatalog catalogue;
	private AGRepository repository;
	private AGRepositoryConnection conn;
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String aoiNodeName = "aoiSensor";
	private static String nodeName;
	
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
	
		TripleStoreAgent tsa = new TripleStoreAgent(XMPPServer, "tripleStore", "jasonpassword", jasonSensorVehicles);
		tsa.run();
	}
	
	public TripleStoreAgent(String serverAddress, String id, String password, String nodeName) throws XMPPException {
		sensorClient = new SensorXMPPClient(serverAddress, id, password);
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
		server = new AGServer(agServerURL, agUsername, agPassword);
		try {
		System.out.println("available cats: " + server.listCatalogs());

		catalogue = server.getCatalog(agCatID);
		//System.exit(0);
		} catch (Exception newE) {
			newE.printStackTrace();
		}
		try {
			repository = catalogue.createRepository(agRepoID);
			repository.initialize();
			conn = repository.getConnection();
		} catch (RepositoryException e1) {
			e1.printStackTrace();
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
		
		System.out.println("Started");
		
		sensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() {
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try
				{
					conn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
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
					conn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
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
					conn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
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
					conn.add(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.RDFXML);
				}
				catch(Exception e) 
				{ 
					System.out.println("error adding more data in " + jasonSensorStates);
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
