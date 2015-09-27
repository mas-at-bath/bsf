package edu.bath.sensorframework.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import org.eclipse.paho.client.mqttv3.*;

/**
 * Used in the backend of the sensor client to handle incoming data.
 * @author adan
 *
 */
public class ReadingMQTTReceiver implements MqttCallback {
	private Map<String,List<String>> dataList;
	private Map<String, List<ReadingHandler>> handlersList;

	public ReadingMQTTReceiver(Map<String,List<String>> dataList, Map<String, List<ReadingHandler>> handlersList) 
	{
		System.out.println("Created ReadingMQTTReceiver");
		this.dataList = dataList;
		this.handlersList = handlersList;
	}
	
	@Override
  	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Deliver complete token!");
	}

	@Override
  	public void messageArrived(String topic, MqttMessage message) throws Exception 
	{
		//System.out.println("got MQTT in BSF rec " + message);
		String rdf = message.toString();		
		rdf = rdf.replaceFirst("^<RDF(.*?)?>", "");
		rdf = rdf.replaceFirst("</(rdf:)?RDF>$", "");
		rdf = StringEscapeUtils.unescapeXml(rdf);
			
		// If handler is registered, don't store
		List<ReadingHandler> rhList = handlersList.get(topic);
		if(rhList == null || rhList.size() == 0) 
		{
			// otherwise, do
			///vals.add(rdf);
		} else {
			synchronized(rhList) {
				for(ReadingHandler rh : rhList) {
					try {
					rh.handleIncomingReading(topic, rdf);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
  	public void connectionLost(Throwable cause) 
	{
		System.out.println("Connection lost!! " + cause.getMessage());
		cause.printStackTrace();
	}
}
