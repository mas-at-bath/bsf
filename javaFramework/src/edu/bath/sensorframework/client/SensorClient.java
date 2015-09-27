package edu.bath.sensorframework.client;

import edu.bath.sensorframework.Config;

/**
 * This is the class which controls client operations for the sensor framework.
 * Simply instantiate this object, and use!
 * 
 * @author adan
 *
 */
public abstract class SensorClient {
	/*private PubSubManager mgr = null;
	private Map<String,List<String>> rawPendingData;
	private String myJID;
	private ReadingReceiver handler;
	private XMPPTCPConnection connection;
	private String username, password;
	private Map<String, List<ReadingHandler>> handlersList;
	private List<String> subscriptionList = new ArrayList<String>();
	
	private Chat mChat;
	private String mTarget;*/

	public abstract void addHandler(String nodeID, ReadingHandler handler);
	public abstract void subscribe(String nodeName)
		throws Exception;
	public abstract boolean checkReconnect() 
		throws Exception;
	public abstract void disconnect();
	public abstract boolean checkIsConnected();
}

