/*
 * InstEthManager 
 * - Main instance of the institution manager. Once receive the percepts from agents,
 * 	then convert them into exogenous events that are acceptable to institutions. Finally,
 * 	these events are delivered to the instance of institutions. Also, it is able to receive 
 *  the norms from institutions and send them to agents for the deliberation inside agents.
 * 
 * 		@author		JeeHang
 * 		@date		29 Mar 2012
 * 
 * (+) Adding multiple institution (Aug 2013, JeeHang Lee) 
 * (+) Reworked to publish to/from blockchain (March 2016, V Baines)
 */

package edu.bath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;

import edu.bath.institution.*;
import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.client.*;
import edu.bath.sensors.NormSensor;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.message.BasicHeader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader; 
import org.apache.http.message.BasicHttpResponse; 
import org.apache.http.protocol.HTTP; 
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;

/*
 * class InstEthManager
 * 	- Institution Manager
 * 	- The abstraction layer between institutional models and BDI agents
 * 	- Subscribe event from the external environment, publish corresponding norms to BDI agents 
 */
public class InstEthManager {
	
	private static final String PERCEPT = "NODE_PERCEPT";
	private static final String NORM = "NODE_NORM";
	private static final String AOINODE = "aoiSensor";

	private NormSensor pubNorm; 		// sensor publishing current states		
	private SensorClient aoiPercept, subPercept;	// sensor client perceiving events from environments	
	private String server, username, password;	// login information
	
	private InstFactory factory;		// container of multiple institution
	private Queue<String> m_req;		// 

	private long startupTime=0L;
	private long startupDelay=10000L;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;

	private EthWorker myEthWorker = new EthWorker("http://127.0.0.1:8100");
	
	/*
	 * Percept node reading handler 
	 */
	public class PerceptReadingHandler implements ReadingHandler {
		@Override
		public void handleIncomingReading(String node, String rdf) {

			System.out.println("got a message: " + rdf);
			try {
				if ((rdf != null) && (rdf.isEmpty() != true)) 
				{
					JsonReading jr = new JsonReading();
					jr.fromJSON(rdf);
					//VB make sure we process any initials first
					if ((startupTime + startupDelay) < System.currentTimeMillis())
					{
					Value inival = jr.findValue("INITIALS");
					if (inival !=null)
					{
						System.out.println("received initials too: " + inival.m_object.toString());
						m_req.add(inival.m_object.toString());
					}
					Value val = jr.findValue("EVENT");
					if (val != null)
					{
						System.out.println("processed it ok to " + val.m_object.toString());
						m_req.add(val.m_object.toString());
					}
					}
					else
					{
						System.out.println("discarded msg");
					}
				};
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	// Constructor
	public InstEthManager(String[] args) throws XMPPException, InterruptedException {
		m_req = new LinkedList<String>();
		initConfig(args);
		initFactory(args);
	}
	
	private void initConfig(String[] args) {
		System.out.println("Connecting to XMPP server on: " + server);
		server = args[0];
		username = args[1];
		password = args[2];
	}
	
	private void initFactory(String[] args) {
		factory = new InstFactory(args);
	}
	
	public void initialiseBSF() {
		try {
			
			//sleep(5000);	// ensure the enough creation time
			
			startupTime = System.currentTimeMillis();
			//VB maybe put this on demand depending on inst type started..?
			if (useXMPP)
			{
				System.out.println("Using XMPP");
				pubNorm = new NormSensor(server, username, password, NORM); 
				sleep(500);		
				subPercept = new SensorXMPPClient(server, username+"-instPer", password);
				subPercept.addHandler(PERCEPT, new PerceptReadingHandler()); 
				subPercept.subscribe(PERCEPT);
				sleep(500);
				aoiPercept = new SensorXMPPClient(server, username+"-instAOI", password);
			}
			else if (useMQTT)
			{
				System.out.println("Using MQTT");
				pubNorm = new NormSensor(server, username, password, NORM, true, 0); 
				sleep(500);
				subPercept = new SensorMQTTClient(server, username+"-instPer");
				subPercept.addHandler(PERCEPT, new PerceptReadingHandler()); 
				subPercept.subscribe(PERCEPT);
				//System.out.println("Using MQTT Here..");
				sleep(500);
				aoiPercept = new SensorMQTTClient(server, username+"-instAOI");
			}


			//TODO: rework this
			/*AOIReadingHandler newAOIhandler = new AOIReadingHandler();
			newAOIhandler.init(this);
			aoiPercept.addHandler(AOINODE, newAOIhandler); 
			aoiPercept.subscribe(AOINODE);*/
		} catch (Exception e) {
			System.out.println("NormSensor creation failed");
			e.printStackTrace();
		}
	}
	
	public void invokeRequest(String evt) {
		if (factory != null) {
			System.out.println("started invokeRequest with " + evt);
			if (evt.startsWith("initially("))
			{
				factory.updateInitials(evt);
			}
			else
			{
				factory.updateStates(evt);
			}
			List<String> norms = factory.getCurrentStates();
			if ((norms != null) && (norms.isEmpty() != true)) {
				//pubNorm.releaseNorm(norms); //publish to MQTT-XMPP node, old way
				//assumes obl string is going to look like this
				//(changeLane(centralMember2),deadline,vioMove(centralMember2)),roadusers
				for (String pubStr : norms)
				{
					pubStr = pubStr.substring(4);
					String[] parts = pubStr.split(",");
					String firstPart = parts[0].substring(0,parts[0].length()-1);				
					String[] innerParts = firstPart.split("\\(");
					String subj = innerParts[1];
					String content = innerParts[0];
					String dline = parts[1];
					String instName = parts[3];
					String vio = parts[2];
					myEthWorker.createNewOblContract(subj, content,dline, instName,vio);
				}
			}
		}
	}
	
	public void addMsg(String newMsg)
	{
		m_req.add(newMsg);
	}
	
	public void run() throws XMPPException	{
		//VB ORIG CODE: 
		/*while (true) {
			if ((m_req != null) && (m_req.isEmpty() != true)) {
				invokeRequest(m_req.poll());
			}
		}*/
		
		//preamble.. 
		String envContractAddress = "0xd19eb2281956189b3df3d10d599674155653fa0c";
		String myAccount = myEthWorker.getAccountNumber();
		float myAccountBal = myEthWorker.getEthBalance(myAccount);
		System.out.println("balance for account " + myAccount + " is " + myAccountBal);

		//add a filter to pick up new blocks (as they may have contract details)
		String newBlockFilterID = myEthWorker.addNewBlockFilter();
		//add a general filter 
		String generalFilterID = myEthWorker.addGeneralFilter();
		//myEthWorker.createNewRDFContract();
		//myEthWorker.createNewOblContract();

		String req;
		while (true)
		{
			if ((m_req != null) && (m_req.isEmpty() != true)) {
				System.out.println("called invokeRequest");
				invokeRequest(m_req.poll());
			}
			//This can be useful to comment back on if trying to debug message receipt..
			else if (m_req != null)
			{
				//System.out.println("mreq is null");
				if (m_req.isEmpty() != true)
				{
					System.out.println("and empty");
				}
			}
			else
			{
				//System.out.println("mreq is empty");
			}

			//check for changes..
			JSONObject generalChanges = myEthWorker.getFilterChanges(generalFilterID);
			String addressChange = myEthWorker.findResultOfArray(generalChanges, "address");
			if (addressChange.equals (envContractAddress))
			{
				System.out.println("change to environment contract!!");
				String dataChange = myEthWorker.findResultOfArray(generalChanges, "data");
				if (!dataChange.equals("none"))
				{
					SimpleRDF foundRDF = myEthWorker.getRDF(dataChange);
					System.out.println(foundRDF.getPred());
					if (foundRDF.getPred().contains("vehicleAction"))
					{
						System.out.println(foundRDF.getObj() + " !!");
						m_req.add(foundRDF.getObj());
					}
				}
			}
			else if (!generalChanges.toString().equals("{\"id\":73,\"result\":null,\"jsonrpc\":\"2.0\"}"))
			{
				System.out.println("unknown change..");
				System.out.println(generalChanges);
			}

			JSONObject blockChanges = myEthWorker.getFilterChanges(newBlockFilterID);
			if (!blockChanges.toString().equals("{\"id\":73,\"result\":[],\"jsonrpc\":\"2.0\"}"))
			{
				System.out.println("block change..");
				JSONArray jArrayChanges = myEthWorker.findSingleArrayResult(blockChanges);
				for (int i = 0 ; i < jArrayChanges.size(); i++) 
				{
					String rStr = (String) jArrayChanges.get(i);
					JSONArray jArrTX = myEthWorker.getBlockTransactions(rStr);
					for (int j = 0 ; j < jArrTX.size(); j++) 
					{
						JSONObject rObj = (JSONObject) jArrTX.get(j);
						if (myEthWorker.checkIfNewOblContract(rObj))
						{
							myEthWorker.getOblDetails(rObj);
						}
						else
						{
							System.out.println("new block / tx but not obligation?");
						}
					}
				}
			}

			//VB added a small sleep, I seem to have messages lost/not processed if this isn't present.. strangely
			sleep(1000);

		}

	}
	
	public void sleep(long mili) {
        try {
        	Thread.sleep(mili);
        } catch (InterruptedException e) {
        	// no-op
        }
    }
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws XMPPException, InterruptedException {
		if (args.length < 4) {
			System.out.println("Usage : instEthManager.jar server username password ial_filename1 ial_filename2 ...");
			return;
		}
		
		InstEthManager man = new InstEthManager(args);
		man.initialiseBSF();
		man.run();
	}
		
}
