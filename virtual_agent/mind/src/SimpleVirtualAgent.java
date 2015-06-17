/*
 * SimpleVirtualAgent 
 * - Simple Secondlife bot agent that used only Jason BDI engine. 
 * 
 * 		@author		JeeHang
 * 		@date		15 Feb 2012
 */

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

import common.Constants;

/*
 * class SimpleVirtualAgent
 * 	- 
 */
public class SimpleVirtualAgent extends AgArch {
	
	/*
	 * class PerceptReadingHandler
	 * 	- Subscription handler for Percepts from body
	 */
	public class PerceptReadingHandler implements ReadingHandler {
		@Override
		public void handleIncomingReading(String node, String rdf) {
			try {
				JsonReading jr = new JsonReading();
				jr.fromJSON(rdf);
				Value val = jr.findValue("EVENT");
				System.out.println(val.m_object);
				
				m_percept.add(val.m_object.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
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
				m_percept.add(val.m_object.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Logger m_logger = Logger.getLogger(SimpleVirtualAgent.class.getName());
	
	// initial information for xmpp connection
	private String m_name;
	private String m_server;
	private String m_pwd;
	private String m_aslpath;

	// Percept buffer
	private ArrayList<String> m_percept;
	
	// sensors and sensorclients
	private MindSensor m_ssAction;
	private SensorClient m_sc;

	/*
	 * Method : main(String[] arg)
	 */
	public static void main(String[] arg) throws Exception {
		RunCentralisedMAS.setupLogger();
		
		if (arg.length < 4) {
			System.out.println("Usage : agent.jar server username password asl_filename");
			return;
		}
				
		SimpleVirtualAgent sva = new SimpleVirtualAgent(arg);
		
		sva.run();
	}
	
	public SimpleVirtualAgent(String[] arg) {
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
		m_percept = new ArrayList<String>();
	}
	
	/*
	 * Method : initSensors(String[] arg)
	 * - init sensor and sensorClient to communicate with body and normative framework
	 
	private void initSensors()
	{
		// Sensor
		try {
			m_ssAction = new MindSensor(m_server, m_name, m_pwd, Constants.NODE_ACTION);	
			sleep(); // ensure the enough creation time for sensor
		} catch (XMPPException xe) {
			System.out.println("sensor creation failed!");
		}
		
		try {
			// SensorClient for percepts
			m_sc = new SensorClient(m_server, m_name, m_pwd);
			m_sc.addHandler(Constants.NODE_PERCEPT, new PerceptReadingHandler());
			m_sc.subscribe(Constants.NODE_PERCEPT);
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + Constants.NODE_PERCEPT);
		}
		
		try {
			// for norms
			m_sc.addHandler(Constants.NODE_INST, new NormReadingHandler());
			m_sc.subscribe(Constants.NODE_INST);
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + Constants.NODE_INST);
		}
	}
	*/
	private void initSensors()
	{
		// Sensor
		try {
			m_ssAction = new MindSensor(m_server, m_name, m_pwd, m_name + "_action");	
			sleep(); sleep(); sleep(); // ensure the enough creation time for sensor
		} catch (XMPPException xe) {
			System.out.println("sensor creation failed!");
		}
		
		try {
			m_sc = new SensorClient(m_ssAction.getConnection(), m_name, m_pwd);
			m_sc.addHandler(Constants.NODE_PERCEPT, new ReadingHandler() {
				public void handleIncomingReading(String node, String rdf) {
					try {
						JsonReading jr = new JsonReading();
						jr.fromJSON(rdf);
						Value val = jr.findValue("EVENT");
						if (val != null) {
							System.out.println(val.m_object);
							m_percept.add(val.m_object.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			// SensorClient for percepts
			m_sc.subscribe(m_name + "_percept");
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + Constants.NODE_PERCEPT);
		}
		
		// for norms
		m_sc.addHandler(Constants.NODE_INST, new ReadingHandler() {
			public void handleIncomingReading(String node, String rdf) {
				try	{
					JsonReading jr = new JsonReading();
					jr.fromJSON(rdf);
					Value val = jr.findValue("STATE");
					if (val != null) {
						System.out.println(val.m_object);
						m_percept.add(val.m_object.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
		try {
			m_sc.subscribe(Constants.NODE_INST);
		} catch (XMPPException xe) {
			System.out.println("failed to subscribe: " + Constants.NODE_INST);
		}
	}

	public void run() {
		try	{
			while (isRunning())	{
				//if (m_percept.isEmpty() != true) {
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
        if (m_percept.isEmpty() != true) {
        	Iterator<String> iter = m_percept.iterator();
    		while (iter.hasNext()) {
    			l.add(Literal.parseLiteral((String)iter.next()));
    		}
        }
        
        return l;
    }

    // this method get the agent actions
    @Override
    public void act(ActionExec action, List<ActionExec> feedback) {
    	getTS().getLogger().info("Agent " + getAgName() + " is doing: " + action.getActionTerm());
        String act = action.getActionTerm().toString();

        if (act.isEmpty() == false)	{
        	m_ssAction.releaseAction(act);
			System.out.println("action string: " + act);
			m_percept.clear();	// do we need?
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
