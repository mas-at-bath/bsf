package edu.bath.sensorframework.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.bath.sensorframework.Config;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * This extends SensorClient with MQTT specifics
 * 
 * @author vbaines
 *
 */
public class SensorMQTTClient extends SensorClient {

	private Map<String,List<String>> rawPendingData;
	private ReadingMQTTReceiver handler;
	private String username;
	private Map<String, List<ReadingHandler>> handlersList;
	private List<String> subscriptionList = new ArrayList<String>();
	private MqttClient sensorConnection;
	Object  waiter = new Object();
	private int qos=0;
	private MqttConnectOptions connOpts;
	
	public SensorMQTTClient(String broker, String clientID) {

		try
		{
			MemoryPersistence persistence = new MemoryPersistence();		
			sensorConnection = new MqttClient("tcp://"+broker+":1883", clientID, persistence);
			connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting sensor: " + clientID + " to broker: "+broker);
			sensorConnection.connect(connOpts);
			System.out.println("Connected: " + checkIsConnected() + ", keep alive: " + connOpts.getKeepAliveInterval());
			sensorClientCommon(clientID);	
		}

		catch(MqttException me) 
		{
			System.out.println("Crashed in SUBSCRIBE");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
        	}	
	}

	//password being ignored for the moment..
	public SensorMQTTClient(String broker, String clientID, String passwd) {

		try
		{
			MemoryPersistence persistence = new MemoryPersistence();
			sensorConnection = new MqttClient("tcp://"+broker+":1883", clientID, persistence);
			connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting sensor: " + clientID + " to broker: "+broker);
			sensorConnection.connect(connOpts);
			System.out.println("Connected");
			sensorClientCommon(clientID);	
		}

		catch(MqttException me) 
		{
			System.out.println("Crashed in SUBSCRIBE");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
        	}	
	}
	
	
	
	/**
	 * Common parts of constructor.
	 * @param id
	 */
	private void sensorClientCommon(String id) {
		this.username = id; 
		this.rawPendingData = Collections.synchronizedMap(new HashMap<String, List<String>>());
		this.handlersList = Collections.synchronizedMap(new HashMap<String, List<ReadingHandler>>());
		this.handler = new ReadingMQTTReceiver(rawPendingData, handlersList);
	}
	
	/**
	 * Subscribes to a node.
	 * @param topicName Node to subscribe to.
	 * @param addToList Whether this is a new subscription, or a reconnect.
	 * @throws MqttException
	 */
	private void subscribe(String topicName, boolean addToList, boolean createIfNotExist) throws Exception {

		System.out.println("SensorMQTTClient subscribing to " + topicName + " with qos " + qos);
		try
		{
			sensorConnection.subscribe(topicName,qos);
			if(addToList) {
				this.rawPendingData.put(topicName, new ArrayList<String>(10));
				subscriptionList.add(topicName);
			}
			sensorConnection.setCallback(handler);
		}
		catch (MqttException me)
		{
			System.out.println("got this error when trying to subscribe to " + topicName + "");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}
	
	/**
	 * Subscribes to a node.
	 * @param nodeName Node to subscribe to.	
	 */
	@Override
	public void subscribe(String nodeName) throws Exception {
		subscribe(nodeName, true, false);
	}

	
	/**
	 * Unsubscribe from a node.
	 * @param nodeName
	 * @throws XMPPException
	 */
	/*public void unsubscribe(String nodeName) throws XMPPException {
		try
		{
			Node node = mgr.getNode(nodeName);
			node.removeItemEventListener(handler);
			node.unsubscribe(myJID);
			subscriptionList.remove(nodeName);
		}
		catch (Exception e)
		{
			System.out.println("Error in unsubscribe");
			e.printStackTrace();
		}
		
	}*/
	
	
	/**
	 * Fetch all pending data for a particular node.
	 * @param nodeID Node to fetch data for.
	 * @return List of incoming RDF data.
	 * @see edu.bath.sensorframework.DataReading#fromRDF(String)
	 */
	public List<String> getPendingData(String nodeID) {
		if(this.rawPendingData.get(nodeID) == null)
			return new ArrayList<String>(0);
		
		List<String> newList = new ArrayList<String>(this.rawPendingData.get(nodeID).size());
		newList.addAll(this.rawPendingData.get(nodeID));
		this.rawPendingData.clear();
		return newList;
	}
	
	/**
	 * Check if data is waiting to be handled for a particular node.
	 * @param nodeID Node to check.
	 * @return true if there is data waiting, false otherwise.
	 */
	public boolean isPendingData(String nodeID) {
		if(this.rawPendingData.get(nodeID) == null)
			return false;
		
		return (this.rawPendingData.get(nodeID).size()==0?false:true);
	}
	
	/**
	 * Checks if this connection is still active, if not, automatically 
	 * try to reconnect and resubscribe.
	 * @return true if a reconnection was attempted, otherwise false.
	 * @throws Exception
	 */
	@Override
	public boolean checkReconnect() throws Exception {
		boolean output = false; 

		if(!sensorConnection.isConnected()) 
		{
			System.out.println("Not connected!");
			try
			{
				sensorConnection.connect(connOpts);
			}
			catch (Exception e2)
			{
				System.out.println("error in checkReconnect");
				e2.printStackTrace();
			}
			output = true;
			
		}
		
		if(output) {// Resubscribe to everything
			for(String sub : subscriptionList)
				subscribe(sub);
		}
		return output;
	}
	
	@Override
	public void addHandler(String nodeID, ReadingHandler handler) {
		System.out.println("adding handler " + nodeID);
		List<ReadingHandler> handlers = this.handlersList.get(nodeID);
		if(handlers == null) {
			handlers = Collections.synchronizedList(new ArrayList<ReadingHandler>(3));
			this.handlersList.put(nodeID, handlers);
		}
		else
		{
			System.out.println("problem in addHandler, handlers is null!");
		}
		
		handlers.add(handler);
	}

	@Override
	public void disconnect()
	{
		try
		{	
			System.out.println("Disconnecting MQTT " + sensorConnection.getClientId());
			sensorConnection.disconnect();	
		}
		catch (MqttException er)
		{
			System.out.println("couldn't disconnect for some reason");
			er.printStackTrace();
		}
	}

	@Override
	public boolean checkIsConnected()
	{
		return sensorConnection.isConnected();
	}
	
	/**
	 * Fetches the underlying connection (should you wish to use it for 
	 * other XMPP operations).
	 * @return Connection to XMPP server.
	 */
	/*public XMPPTCPConnection getConnection() {
		return this.connection;
	}*/
	
}

