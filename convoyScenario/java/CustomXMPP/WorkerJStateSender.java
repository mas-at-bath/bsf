package CustomXMPP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;

import jason.bb.*;
import jason.asSyntax.*;
import jason.architecture.AgArch;

public class WorkerJStateSender extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle, agentName;
	SensorClient sensorClient;
	//private *ArrayList<RDFHalf> messageStore;
	private String URIRequestsURL = "http://127.0.0.1/JState/";
	private PlanLibrary parentPL;
	private ArrayList<AgArch> monitoredAgArchList;
	private boolean debug = true;
	
	/*private class RDFHalf
	{
		String pred;
		String val;
	}*/
	
	public WorkerJStateSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, String agName, PlanLibrary PL) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
		//this.messageStore = new ArrayList<RDFHalf>();
		this.parentPL = PL;
		this.agentName = agName;
		monitoredAgArchList = new ArrayList<AgArch>();
	}

	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}

	/*public synchronized void addJMessageToSend(String from, String to, String il, String msgContent) {
		//can we use from to be predicate instead?
		//ok lets pick the illocution as being the key here for predicate, as that will define what the object will look like, e.g. from someone, to someone, msg
		String predicate = il;
		//and comprise the object of csv for the minute
		String objectVal=new String(to + "," + msgContent);
		addAndSendMsg("message/"+predicate, objectVal);
	}*/

	public void genBBupdate() 
	{
		int j = 0;
		while (monitoredAgArchList.size() > j) {
			//System.out.println(monitoredAgArchList.get(j));
			int childBBSize = monitoredAgArchList.get(j).getTS().getAg().getBB().size();
			addAndSendMsg(URIRequestsURL+"beliefcount/"+agentName+"/"+monitoredAgArchList.get(j).getAgName(), Integer.toString(monitoredAgArchList.get(j).getTS().getAg().getBB().size()));
			j++;
		}
	}

	public void addChildBBMonitor(AgArch newAg)
	{
		monitoredAgArchList.add(newAg);
	}

	public synchronized void addAndSendMsg(String predicate, String objectVal) {

		try 
		{		
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.addDataValue(null, URIRequestsURL+predicate, objectVal, false);
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
