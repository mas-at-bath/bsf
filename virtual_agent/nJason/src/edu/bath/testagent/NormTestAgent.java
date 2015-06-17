/*
 * NormTestAgent 
 * - Simply test the use of norms with 1) 2-APL approach, and 2) Jason approach
 * - For 1), simulate the 2-APL code using AgentSpeak language, and
 *   try to build norm aware agent inside the Jason using two approach
 *   1) Override event selection method to achieve the priority based scheduling, and
 *   2) doing action directly represented in the obligation
 * 
 * 		@author		JeeHang
 * 		@date		11 Jun 2013
 */

package edu.bath.testagent;

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.Settings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.XMPPException;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;

public class NormTestAgent extends AgArch {
	
	/*
	 * class NormReadingHandler
	 * 	- Subscription handler for normative frameworks
	 */
	public class NormReadingHandler implements ReadingHandler {
		@Override
		public void handleIncomingReading(String node, String rdf) {
			try	{
				JsonReading jr = new JsonReading();
				jr.fromJSON(rdf);
				Value val = jr.findValue("NORM");
//				m_percept.add(val.m_object.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Logger m_logger = Logger.getLogger(NormTestAgent.class.getName());
	
	// initial information for xmpp connection
	private String m_name;
	private String m_server;
	private String m_pwd;
	private String m_aslpath;

	// Percept buffer
	//private ArrayList<Literal> m_percept;
	public String m_percept;
	
	// sensorclients
	private SensorClient m_sc;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RunCentralisedMAS.setupLogger();
		
		if (args.length < 4) {
			System.out.println("Usage : agent.jar server username password asl_filename");
			return;
		}
				
		NormTestAgent nta = new NormTestAgent(args);
		
		nta.run();
	}

	
	public NormTestAgent(String[] arg) {
		initialize(arg);
		try {
			// Create agent for reasoning
			Agent ag = new Agent();
			new TransitionSystem(ag, new Circumstance(), new Settings(), this);
			ag.initAg(m_aslpath);
		} catch (Exception e) {
			m_logger.log(Level.SEVERE, "Init Error", e);
		}
	}
	
	/*
	 * Method : initialize(String[] arg)
	 * - initialize class internals
	 */
	private void initialize(String[] arg)
	{
		initConfiguration(arg);
		initSensors();
	}
	
	/*
	 * Method : initConfiguration(String[] arg)
	 * - Set Configurations for XMPP and sensor connections
	 */
	private void initConfiguration(String[] arg) {
		m_server	= arg[0];
		m_name 		= arg[1];
		m_pwd 		= arg[2];
		
		String filename = arg[3];
		String path = Paths.get("").toAbsolutePath().toString();
		m_aslpath = path + "\\asl\\" + filename;
		
		// percept buffer
		//m_percept = new ArrayList<Literal>();
	}
	
	private void initSensors()
	{
		try {
			m_sc = new SensorClient(m_server, m_name, m_pwd);
			
			// m_sc.addHandler("NODE_NORM", new ReadingHandler() {
			m_sc.addHandler("example", new ReadingHandler() {
				public void handleIncomingReading(String node, String rdf) {
					try {
						JsonReading jr = new JsonReading();
						jr.fromJSON(rdf);
						Value val = jr.findValue("NORM");
						if (val != null) {
							/*
							System.out.println(val.m_object);		
							String lit = "obl(at("+ jr.findValue("posX").m_object+","+jr.findValue("posY").m_object +"),"+ jr.findValue("deadline").m_object + ",violation)";
							m_percept = lit;
							*/
							System.out.println(val.m_object);
							m_percept = val.m_object.toString();
						}
						
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			// SensorClient for percepts
			m_sc.subscribe("example");
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + "NODE_NORM");
		}
	}

	public void run() {
		try	{
			while (isRunning())	{
				//if (m_percept != null) {
					// calls the jason engine to perform one reasoning cycle
					m_logger.fine("in reasoning");
					getTS().reasoningCycle();
				//}
			}
		}
		catch (Exception e)	{
			m_logger.log(Level.SEVERE, "Run error", e);
		}
	}
	
	public String getAgName() {
		return "mind";
	}
	
	// this method just add some perception for the agent
    @Override
    public List<Literal> perceive() {
        List<Literal> l = new ArrayList<Literal>();
        if (m_percept != null) {
	    	l.add(Literal.parseLiteral(m_percept));
        }
        return l;
    }

    // this method get the agent actions
    @Override
    public void act(ActionExec action, List<ActionExec> feedback) {
    	getTS().getLogger().info("Agent " + getAgName() + " is doing: " + action.getActionTerm());
        String act = action.getActionTerm().toString();

        if (act.isEmpty() == false)	{
			System.out.println(this.getAgName() + ": " + "action string: " + act);
			m_percept = null;
		}
        
        // set that the execution was ok
        action.setResult(true);
        feedback.add(action);
    }

    @Override
    public boolean canSleep() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    // a very simple implementation of sleep
    @Override
    public void sleep() {
        try {
        	Thread.sleep(2000);
        } catch (InterruptedException e) {
        	// no-op
        }
    }
    
    // Not used methods
    // This simple agent does not need messages/control/...
    @Override
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    	
    }

    @Override
    public void broadcast(jason.asSemantics.Message m) throws Exception {
    	
    }

    @Override
    public void checkMail() {
    	
    }	
}
