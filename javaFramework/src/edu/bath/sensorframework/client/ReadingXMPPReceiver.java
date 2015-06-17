package edu.bath.sensorframework.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

/**
 * Used in the backend of the sensor client to handle incoming data.
 * @author adan
 *
 */
public class ReadingXMPPReceiver implements ItemEventListener<Item> {
	private Map<String,List<String>> dataList;
	private Map<String, List<ReadingHandler>> handlersList;
	public ReadingXMPPReceiver(Map<String,List<String>> dataList, Map<String, List<ReadingHandler>> handlersList) {
		this.dataList = dataList;
		this.handlersList = handlersList;
	}
	
	@Override
	public void handlePublishedItems(ItemPublishEvent<Item> items) {
		//System.out.println("Got item event.");
		List<Item> ilist = items.getItems();
		String nodeID = items.getNodeId();
		List<String> vals = dataList.get(nodeID);
		if(vals == null) {
			vals = new ArrayList<String>(10);
			dataList.put(nodeID, vals);
		}
		
		for(Item i : ilist) {
			@SuppressWarnings("unchecked")
			PayloadItem<SimplePayload> data = (PayloadItem<SimplePayload>)i;
			
			String rdf = data.getPayload().toXML().toString();
			rdf = rdf.replaceFirst("^<RDF(.*?)?>", "");
			rdf = rdf.replaceFirst("</(rdf:)?RDF>$", "");
			rdf = StringEscapeUtils.unescapeXml(rdf);
			
			// If handler is registered, don't store
			List<ReadingHandler> rhList = handlersList.get(nodeID);
			if(rhList == null || rhList.size() == 0) {
				// otherwise, do
				vals.add(rdf);
			} else {
				synchronized(rhList) {
					for(ReadingHandler rh : rhList) {
						try {
						rh.handleIncomingReading(nodeID, rdf);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
