/*
 * InstManager 
 * - Main instance of the institution manager. Once receive the percepts from agents,
 * 	then convert them into exogenous events that are acceptable to institutions. Finally,
 * 	these events are delivered to the instance of institutions. Also, it is able to receive 
 *  the norms from institutions and send them to agents for the deliberation inside agents.
 * 
 * 		@author		JeeHang
 * 		@date		29 Mar 2012
 * 
 * (+) Adding multiple institution (Aug 2013, JeeHang Lee) 
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

/*
 * class InstManager
 * 	- Institution Manager
 * 	- The abstraction layer between institutional models and BDI agents
 * 	- Subscribe event from the external environment, publish corresponding norms to BDI agents 
 */
public class InstManager {
	
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
	private static boolean useMQTT=true;
	
	/*
	 * Percept node reading handler 
	 */
	public class PerceptReadingHandler implements ReadingHandler {
		@Override
		public void handleIncomingReading(String node, String rdf) {

			System.out.println("got a message..");
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
	public InstManager(String[] args) throws XMPPException, InterruptedException {
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



			AOIReadingHandler newAOIhandler = new AOIReadingHandler();
			newAOIhandler.init(this);
			aoiPercept.addHandler(AOINODE, newAOIhandler); 
			aoiPercept.subscribe(AOINODE);
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
				pubNorm.releaseNorm(norms);
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
			//VB added a small sleep, I seem to have messages lost/not processed if this isn't present.. strangely
			sleep(100);
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
			System.out.println("Usage : instManager.jar server username password ial_filename1 ial_filename2 ...");
			return;
		}
		
		InstManager man = new InstManager(args);
		man.initialiseBSF();
		man.run();
	}
}

