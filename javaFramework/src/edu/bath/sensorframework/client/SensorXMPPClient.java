package edu.bath.sensorframework.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.jivesoftware.smack.Connection;
//import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.*;//XMPPTCPConnection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.util.StringUtils;
import org.jxmpp.util.XmppStringUtils;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smack.ConnectionConfiguration;

//for ReceiveMessage
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.xdata.packet.*;
import org.jivesoftware.smackx.pubsub.ConfigureForm;

import edu.bath.sensorframework.Config;

/**
 * This is the class which controls client operations for the sensor framework.
 * Simply instantiate this object, and use!
 * 
 * @author adan
 *
 */
public class SensorXMPPClient extends SensorClient {
	private PubSubManager mgr = null;
	private Map<String,List<String>> rawPendingData;
	private String myJID;
	private ReadingXMPPReceiver handler;
	private XMPPTCPConnection connection;
	private String username, password;
	private Map<String, List<ReadingHandler>> handlersList;
	private List<String> subscriptionList = new ArrayList<String>();
	
	private Chat mChat;
	private String mTarget;
	
	/**
	 * Creates a sensor client.
	 * @param serverAddress Address of server to connect to.
	 * @param id Username to authenticate with.
	 * @param password Password to authenticate with.
	 * @throws XMPPException
	 */
	public SensorXMPPClient(String serverAddress, String id, String password) throws XMPPException {
		Config.configure();

		try
		{
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
        			.setServiceName(serverAddress)
        			.setUsernameAndPassword(username, password)
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        			.setCompressionEnabled(false).build();
			connection = new XMPPTCPConnection(config);

			//ConnectionConfiguration newConnectConfig = new ConnectionConfiguration(serverAddress);
			//newConnectConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			//connection = new XMPPTCPConnection(newConnectConfig);
			connection.connect();
			connection.login(id, password);
			sensorClientCommon(id, password);
		
		}

		catch (Exception e)
		{
			System.out.println("Error in sensor client method, is "+ serverAddress + " valid?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a sensor client with Message Communication
	 * @param serverAddress Address of server to connect to.
	 * @param id Username to authenticate with.
	 * @param password Password to authenticate with.
	 * @param useMessage This sensor client will be used for message based communication.
	 * @throws XMPPException
	 */
	public SensorXMPPClient(String serverAddress, String username, String password, Boolean useMessage) throws XMPPException  {
		Config.configure();

		try
		{
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
        			.setServiceName(serverAddress)
        			.setUsernameAndPassword(username, password)
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        			.setCompressionEnabled(false).build();
			connection = new XMPPTCPConnection(config);

			//ConnectionConfiguration newConnectConfig = new ConnectionConfiguration(serverAddress);
			//newConnectConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			//connection = new XMPPTCPConnection(newConnectConfig);
			connection.connect();
			connection.login(username, password);
		
			Presence presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
		}
		catch (Exception e)
		{
			System.out.println("Error in sensor client with message comms, is "+ serverAddress + " valid?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a sensorclient from a pre-existing connection.
	 * @param connection
	 */
	public SensorXMPPClient(XMPPTCPConnection connection, String id, String password) {
		this.connection = connection;
		sensorClientCommon(id, password);
	}
	
	/**
	 * Creates a sensor client.
	 * @param serverAddress Address of server to connect to.
	 * @param id Username to authenticate with.
	 * @param password Password to authenticate with.
	 * @param resource Resource to support multiple connections for same user
	 * @throws XMPPException
	 */
	public SensorXMPPClient(String serverAddress, String id, String password, String resource) throws XMPPException {
		try
		{
			Config.configure();
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
        			.setServiceName(serverAddress)
        			.setUsernameAndPassword(username, password)
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        			.setCompressionEnabled(false).build();
			connection = new XMPPTCPConnection(config);
	
			//ConnectionConfiguration newConnectConfig = new ConnectionConfiguration(serverAddress);
			//newConnectConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			//connection = new XMPPTCPConnection(newConnectConfig);
			connection.connect();
			connection.login(id, password, resource);
			sensorClientCommon(id, password);
		}
		catch (Exception e)
		{
			System.out.println("Error in SensorXMPPClient connection, is " + serverAddress + " valid?");
			e.printStackTrace();
		}

	}
	
	/**
	 * Common parts of constructor.
	 * @param id
	 * @param password
	 */
	private void sensorClientCommon(String id, String password) {
		this.username = id; this.password = password;
		this.mgr = new PubSubManager(connection, "pubsub."+connection.getServiceName());
		this.myJID = XmppStringUtils.parseBareAddress(connection.getUser());
		this.rawPendingData = Collections.synchronizedMap(new HashMap<String, List<String>>());
		this.handlersList = Collections.synchronizedMap(new HashMap<String, List<ReadingHandler>>());
		this.handler = new ReadingXMPPReceiver(rawPendingData, handlersList);
	}
	
	/**
	 * Subscribes to a node.
	 * @param nodeName Node to subscribe to.
	 * @param addToList Whether this is a new subscription, or a reconnect.
	 * @throws XMPPException
	 */
	private void subscribe(String nodeName, boolean addToList, boolean createNodeIfNone) throws Exception {
		try
		{
			Node node = mgr.getNode(nodeName);
			node.addItemEventListener(handler);
			node.subscribe(myJID);
			if(addToList) {
				this.rawPendingData.put(nodeName, new ArrayList<String>(10));
				subscriptionList.add(nodeName);
			}
		}
		catch (XMPPException e)
		{
			System.out.println("got this error when trying to subscribe to " + nodeName + " :");
			e.getMessage();
			System.out.println("stack trace:");
			e.printStackTrace();
			if (createNodeIfNone)
			{
				try {
					LeafNode leaf = mgr.createNode(nodeName);
					ConfigureForm form = new ConfigureForm(DataForm.Type.submit);
					form.setAccessModel(AccessModel.open);
					form.setDeliverPayloads(true);
					form.setNotifyRetract(false);
					form.setPersistentItems(false);
					form.setPublishModel(PublishModel.open);
					leaf.sendConfigurationForm(form);
				} 
				catch(Exception e2) 
				{
					System.out.println("hmm creating new node failed too");
				}
			}
		}
		catch (SmackException e)
		{ 	
			System.out.println("Error in subscribe, server problem?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribes to a node.
	 * @param nodeName Node to subscribe to.
	 * @throws XMPPException
	 */
	@Override
	public void subscribe(String nodeName) throws Exception {
		subscribe(nodeName, true, true);
	}
	
	/**
	 * Unsubscribe from a node.
	 * @param nodeName
	 * @throws XMPPException
	 */
	public void unsubscribe(String nodeName) throws XMPPException {
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
		
	}
	
	/**
	 * Send a message to target user.
	 * @param target which is target user id
	 * @param message
	 */
	public void setTargetUser(String target, ChatMessageListener listener) {
		mTarget = target;
		//ChatManager chatmanager = connection.getChatManager();
		ChatManager chatmanager = ChatManager.getInstanceFor(connection);
		mChat = chatmanager.createChat(mTarget, listener);
	}
	
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
	 * @throws XMPPException
	 */
	@Override
	public boolean checkReconnect() throws Exception {
		boolean output = false; 

		if(!connection.isConnected()) 
		{
			System.out.println("Not connected!");
			try
			{
				connection.connect();
			}
			catch (Exception e2)
			{
				System.out.println("error in checkReconnect");
				e2.printStackTrace();
			}
			output = true;
			
		}

		try
		{
			if(!connection.isAuthenticated()) {
				System.out.println("Not authenticated!");
				connection.login(username, password);
				output = true;
			}
		}
		catch (Exception e)
		{
			System.out.println("error in checkReconnect");
			e.printStackTrace();
		}
		
		if(output) {// Resubscribe to everything
			for(String sub : subscriptionList)
				subscribe(sub);
		}
		return output;
	}
	
	/**
	 * Adds a handler for incoming data.
	 * @param nodeID Node to listen on.
	 * @param handler Handler for data from that node.
	 * @see edu.bath.sensorframework.client.ReadingHandler
	 */
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
		System.out.println("disconnecting XMPP client..");
		connection.disconnect();
	}

	@Override
	public boolean checkIsConnected()
	{
		return connection.isConnected();
	}
	
	/**
	 * Fetches the underlying connection (should you wish to use it for 
	 * other XMPP operations).
	 * @return Connection to XMPP server.
	 */
	public XMPPTCPConnection getConnection() {
		return this.connection;
	}
	
}

