package edu.bath.sensorframework.sensor;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringEscapeUtils;

//support XMPP+smack
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.xdata.packet.*;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smack.ConnectionConfiguration;

 // support JSON
import org.json.simple.JSONObject;

import edu.bath.sensorframework.Config;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.JsonReading;

//support MQTT
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Sensor class, should be extended by any agent wishing to act as a sensor 
 * within the framework. This uses a publish/subscribe design pattern, with 
 * other agents sending a subscription request message to the sensor, which 
 * then publishes sensor data to all subscribers as it becomes available.
 * 
 * All sensors should periodically call one of the receiveSensorMessages 
 * methods in order to pick up messages directed at the sensor framework 
 * rather than the sensor itself.
 * 
 * @author adan
 *
 */
public abstract class Sensor {
	private LeafNode leaf;
	private PubSubManager mgr;
	private String nodeName; //also means topic
	private XMPPTCPConnection connection;
	private String username, password;
	private boolean useMQTT=false;
	private String serverName;
	private MqttClient sensorClient;
	private MemoryPersistence persistence = new MemoryPersistence();
	private int qos = 0;

	/**
	 * Creates a sensor from an existing connection.
	 * VB: Leave this as the default constructor for XMPP instead of MQTT
	 * @param serverAddress Address of XMPP server.
	 * @param username Username to authenticate as.
	 * @param password Password to authenticate with.
	 * @param nodeName Node to publish to.
	 * @throws XMPPException
	 */
	public Sensor(String serverAddress, String username, String password, String nodeName) throws XMPPException {
		// Set up XMPP server connection first
		Config.configure();
		/*ConnectionConfiguration newConnectConfig = new ConnectionConfiguration(serverAddress);
		newConnectConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);*/
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
        		.setServiceName(serverAddress)
        		.setUsernameAndPassword(username, password)
			.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        		.setCompressionEnabled(false).build();
		//config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		connection = new XMPPTCPConnection(config);
		//connection = new XMPPTCPConnection(newConnectConfig);
		try {
			connection.connect();
		}
		catch (Exception e) {
			System.out.println("Error in Sensor connection, is " + serverAddress + " valid?");
			e.printStackTrace();
		}
		try {
			connection.login(username, password);
		}
		catch (Exception e2) {
			System.out.println("Error in Sensor connection-login");
			e2.printStackTrace();
		}
		createSensorCommon(username, password, nodeName);
	}

	/**
	 * Creates a sensor from an existing connection. Similar params to XMPP to help backwards compability
	 * VB: Use this one for an MQTT Sensor
	 * @param serverAddress Address of MQTT server.
	 * @param username ClientID (could change to username if we start using auth).
	 * @param password Password to authenticate with - probably not used.
	 * @param nodeName Node (Topic) to publish to.
	 * @param mqtt Boolean use MQTT (true for yes).
	 * @param qosVal int QoS value.
	 * @throws XMPPException
	 */

	public Sensor(String serverAddress, String username, String password, String nodeName, boolean mqtt, int qosVal)  {
		this.nodeName = nodeName;
		this.serverName = serverAddress;
		this.qos=qosVal;
		this.useMQTT=true;
		if (!mqtt)
		{
			System.out.println("WARNING! You've used constructor for MQTT, but set mqtt val to false.. something wrong?");
		}

		try {
			sensorClient = new MqttClient("tcp://"+serverName+":1883", username, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: "+serverName);
			sensorClient.connect(connOpts);
			System.out.println("Connected");
		}
		catch(MqttException me) 
		{
			System.out.println("Crashed in MQTT connection");
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
        	}	
	}

	//null sensor, so we can still use largely the same code with sensors inplace, just point them to nothing if we are running disconnected from a network / testing
	public Sensor(String name)
	{
		this.nodeName=name;
	}
	
	/**
	 * Creates a sensor from an existing connection.
	 * @param connection Connection to use.
	 * @param username Username used to reconnect.
	 * @param password Password used to reconnect.
	 * @param nodeName Node to publish to.
	 * @throws XMPPException
	 */
	public Sensor(XMPPTCPConnection connection, String username, String password, String nodeName) throws XMPPException  {
		this.connection = connection;
		if (useMQTT)
		{
			System.out.println("WARNING!! Method not implemented yet for MQTT");
		}
		else
		{
			createSensorCommon(username, password, nodeName);
		}
	}
	
	/**
	 * Creates a sensor from an existing connection.
	 * @param connection Connection to use.
	 * @param username Username used to reconnect.
	 * @param password Password used to reconnect.
	 * @throws XMPPException
	 * @description Creates a sensor for messaging (not publish)
	 */
	public Sensor(String serverAddress, String username, String password) throws XMPPException  {
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
			System.out.println("Error in Sensor connection, is " + serverAddress + " valid?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Common part of constructor.
	 * @param username
	 * @param password
	 * @param nodeName
	 * @throws XMPPException
	 */
	private void createSensorCommon(String username, String password, String nodeName) throws XMPPException  {
		this.username = username; this.password = password;
		if (useMQTT)
		{
			System.out.println("WARNING!! Method not implemented yet for MQTT");
		}
		else
		{
			if(connection.isConnected())
				System.out.println("Now connected!");
			else
				System.out.println("Not connected!");
		
			mgr = new PubSubManager(connection, "pubsub."+connection.getServiceName());
		
			this.nodeName = nodeName;
			//System.out.println("created node: " + nodeName);
			try {
				leaf = mgr.createNode(nodeName);
				//ConfigureForm form = new ConfigureForm(FormType.submit);
				ConfigureForm form = new ConfigureForm(DataForm.Type.submit);
				form.setAccessModel(AccessModel.open);
				form.setDeliverPayloads(true);
				form.setNotifyRetract(false);
				form.setPersistentItems(false);
				form.setPublishModel(PublishModel.open);
				leaf.sendConfigurationForm(form);
			} 
			catch(Exception e) 
			{
				System.out.println("Node creation failed, fetching old one.");
				try {
					leaf = (LeafNode)mgr.getNode(nodeName);
				}
				catch (Exception e2) {
					System.out.println("Error getting old node");
					//e2.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Publishes an item of data to all current subscribers.
	 * @param data Data to publish.
	 * @see edu.bath.sensorframework.DataReading
	 * @throws UnsupportedEncodingException 
	 * @throws RDFHandlerException 
	 * @throws RepositoryException 
	 */
	protected void publish(DataReading data) throws UnsupportedEncodingException {
		if (useMQTT)
		{
			String rdfdString = data.toRDF();
			String msgString = "<RDF>"+StringEscapeUtils.escapeXml(rdfdString)+"</RDF>";

			try
			{
				MqttMessage message = new MqttMessage(msgString.getBytes());
				message.setQos(qos);
				sensorClient.publish(nodeName, message);
			}
			catch(MqttException me) 
			{
				System.out.println("Crash in PUBLISH:");
				System.out.println("reason "+me.getReasonCode());
				System.out.println("msg "+me.getMessage());
				System.out.println("loc "+me.getLocalizedMessage());
				System.out.println("cause "+me.getCause());
				System.out.println("excep "+me);
				me.printStackTrace();
			}
		}
		else
		{
			//long nanoToMili=1000000;
			//long preTime = System.nanoTime();
			String rdfdString = data.toRDF();
			//System.out.println("time to create rdfdString " + ((System.nanoTime()-preTime)/nanoToMili));
			String msgString = "<RDF>"+StringEscapeUtils.escapeXml(rdfdString)+"</RDF>";
			//System.out.println("time to create msgString " + ((System.nanoTime()-preTime)/nanoToMili));
			SimplePayload sp = new SimplePayload("RDF", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", msgString);
			//System.out.println("time to create simplePayload " + ((System.nanoTime()-preTime)/nanoToMili));
			PayloadItem<SimplePayload> pi = new PayloadItem<SimplePayload>("pwrsensor"+ System.currentTimeMillis(), sp);
			//System.out.println("time to create payloadItem " + ((System.nanoTime()-preTime)/nanoToMili));
			//long postTime = System.nanoTime();
			//System.out.println("full time to create msg " + ((System.nanoTime()-preTime)/nanoToMili));
			try
			{
				leaf.publish(pi);
			}
			catch (Exception e) {
				System.out.println("Error publishing DataReading");
				e.printStackTrace();
			}
			//System.out.println("time to publish msg " + ((System.nanoTime()-postTime)/nanoToMili));
		}
	}
	
	/**
	 * Publishes an item of data to all current subscribers.
	 * @param data Data to publish in the form of JSON.
	 * @see edu.bath.sensorframework.DataReading
	 * @throws UnsupportedEncodingException 
	 * @throws RDFHandlerException 
	 * @throws RepositoryException 
	 */
	protected void publish(JsonReading jr) throws UnsupportedEncodingException {
		if (useMQTT)
		{
			try
			{
				String msgString = "<JSON>" +jr.getJsonObject().toString() + "</JSON>";
				//System.out.println("Publishing JSON message: "+msgString);
				MqttMessage message = new MqttMessage(msgString.getBytes());
				message.setQos(qos);
				sensorClient.publish(nodeName, message);
				//System.out.println("Message published");
			}
			catch(MqttException me) 
			{
				System.out.println("Crash in PUBLISH:");
				System.out.println("reason "+me.getReasonCode());
				System.out.println("msg "+me.getMessage());
				System.out.println("loc "+me.getLocalizedMessage());
				System.out.println("cause "+me.getCause());
				System.out.println("excep "+me);
				me.printStackTrace();
			}
		}
		else
		{
			try
			{
				leaf.publish(new PayloadItem<SimplePayload>("JsonItem" + System.currentTimeMillis(), 
					new SimplePayload("JSON", "http://www.json.org/temp-ns#", "<JSON>" + jr.getJsonObject().toString() + "</JSON>")));
			}
			catch (Exception e) {
				System.out.println("Error publishing json reading");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Cleans up upon sensor exit.
	 * @throws XMPPException
	 */
	public void cleanup() 
	{
		//System.out.println("called cleanup");
		if (useMQTT)
		{
			try 
			{
				System.out.println("Disconnecting " + sensorClient.getClientId());
				sensorClient.disconnect();
			} 
			catch (Exception e) {
				System.out.println("Cleanup failed");
				e.printStackTrace();
			}
		}
		else
			{
			try {
				mgr.deleteNode(nodeName);
				connection.disconnect();
			} catch (Exception e) {
				System.out.println("Cleanup failed - Failure in deleteNode(" + nodeName + ")");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks if this connection is still active, if not, automatically 
	 * try to reconnect and resubscribe.
	 * @return true if a reconnection was attempted, otherwise false.
	 * @throws XMPPException
	 */
	protected boolean checkReconnect() throws XMPPException {

		boolean output = false;
		if (useMQTT)
		{
			System.out.println("WARNING!! Method not implemented yet for MQTT");
		}
		else
		{
			try
			{
				if(!connection.isConnected()) {
					System.out.println("Not connected!");
					connection.connect();
					output = true;
				}
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
		}
		return output;
	}
	
	/**
	 * Fetches the underlying connection (should you wish to use it for 
	 * other XMPP operations).
	 * @return Connection to XMPP server.
	 */
	public XMPPTCPConnection getConnection() {
		if (useMQTT)
		{
			System.out.println("WARNING!! Method not implemented yet for MQTT");
		}
		else
		{
		}
		return this.connection;
	}
}
