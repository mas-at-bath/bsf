package edu.bath.institution;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;

import javax.ws.rs.core.MediaType;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class InstClient {

	private String aPPService_URL;

	private String datapool_URL;

	// private String event_URL;

	private String newState_URL;

	private String username;

	private String password;

	private String response_APPService;

	// private String event;

	private List<String> newState;

	private Map<String, String> mInputsLocalAddresses;

	public InstClient(String APPService_URL, String Datapool_URL,
			String Username, String Password,
			Map<String, String> mDataAddressPair) {
		this.aPPService_URL = APPService_URL;
		this.datapool_URL = Datapool_URL;
		this.username = Username;
		this.password = Password;
		this.mInputsLocalAddresses = mDataAddressPair;
	}
	
	public InstClient(String APPService_URL, String Datapool_URL,
			String Username, String Password,
			String Request) {
		this.aPPService_URL = APPService_URL;
		this.datapool_URL = Datapool_URL;
		this.username = Username;
		this.password = Password;
		Map<String, String> mDataAddressPair = new HashMap<String, String>();
		mDataAddressPair.put("Request", Request);
		this.mInputsLocalAddresses = mDataAddressPair;
	}

	public List<String> UploadEventandGetNewState() throws IOException,
			ClassNotFoundException, JDOMException {
		// putOutputDataObject2ExternalDatapool(this.username,
		// this.password,this.datapool_URL, this.event);
		Iterator<String> it = this.mInputsLocalAddresses.keySet().iterator();
		while (it.hasNext()) {
			String dataItemName = it.next();
			this.putData2ExternalDatapool(this.username, this.password,
					this.datapool_URL, dataItemName,
					this.mInputsLocalAddresses.get(dataItemName));
		}
		executeAPPService(this.username, this.password, this.aPPService_URL,
				this.datapool_URL);
		String result_URL = getURLfromResponse(this.response_APPService);
		this.newState_URL = result_URL;
		this.newState = getContentofNewState(this.username, this.password,
				result_URL);
		return this.newState;
	}

	// public List<String> UploadEventandGetNewState_Query()
	// throws ClassNotFoundException, ClientHandlerException,
	// UniformInterfaceException, IOException, JDOMException {
	//
	// executeAPPService_Query(this.username, this.password,
	// this.aPPService_URL, this.event);
	// this.newState = getContentofNewState(this.username, this.password,
	// getURLfromResponse(this.response_APPService));
	//
	// return this.newState;
	// }

	// public static List<String> GetNewStatebyURL(String Username,
	// String Password, String NewState_URL) throws IOException,
	// ClassNotFoundException {
	// ArrayList<String> content = new ArrayList<String>();
	// Client c = Client.create();
	// c.addFilter(new HTTPBasicAuthFilter(Username, Password));
	// WebResource service = c.resource(NewState_URL);
	// ClientResponse response = service.accept(MediaType.TEXT_PLAIN).get(
	// ClientResponse.class);
	// // String Content = response.getEntity(String.class);
	// InputStream Content = response.getEntityInputStream();
	// ObjectInputStream ois = new ObjectInputStream(Content);
	// content = (ArrayList<String>) ois.readObject();
	// return content;
	// }

	public List<String> GetNewState() {
		return this.newState;
	}

	// public String GetEvent_URL() {
	// return this.event_URL;
	// }

	public String GetNewState_URL() {
		return this.newState_URL;
	}

	private int putDataFromLocalAddress2ExternalDatapool(String Username,
			String Password, String DP_URL, String DataItemName,
			String LocalAddress) throws IOException {
		Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter(Username, Password));
		// String content = "";
		// File file = new File(Address);
		// InputStream content = new FileInputStream(file);
		String object_DP_URL = DP_URL + "/" + DataItemName;

		InputStream fis = new FileInputStream(LocalAddress);

		WebResource service = c.resource(object_DP_URL);
		int status = 0;
		try {
			ClientResponse response = service.type(MediaType.TEXT_PLAIN).put(
					ClientResponse.class, fis);
			// this.event_URL = object_DP_URL;
			//System.out.println("Response Status : " + response.getStatus());
			status = response.getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// content.close();
		return status;
	}

	private int putData2ExternalDatapool(String Username, String Password,
			String DP_URL, String DataItemName, String Data) throws IOException {
		Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter(Username, Password));
		// String content = "";
		// File file = new File(Address);
		// InputStream content = new FileInputStream(file);
		String object_DP_URL = DP_URL + "/" + DataItemName;
		WebResource service = c.resource(object_DP_URL);
		int status = 0;
		try {
			ClientResponse response = service.type(MediaType.TEXT_PLAIN).put(
					ClientResponse.class, Data);
			// this.event_URL = object_DP_URL;
			//System.out.println("Response Status : " + response.getStatus());
			status = response.getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// content.close();
		return status;
	}

	// private void putOutputDataObject2ExternalDatapool(String Username,
	// String Password, String DP_URL, String Event) throws IOException {
	// Client c = Client.create();
	// c.addFilter(new HTTPBasicAuthFilter(Username, Password));
	// // String content = "";
	// // File file = new File(Address);
	// // InputStream content = new FileInputStream(file);
	// String object_DP_URL = DP_URL + "/Request";
	//
	// WebResource service = c.resource(object_DP_URL);
	// try {
	// ClientResponse response = service.type(MediaType.TEXT_PLAIN).put(
	// ClientResponse.class, Event);
	// // this.event_URL = object_DP_URL;
	// System.out.println("Response Status : " + response.getStatus());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// // content.close();
	//
	// }

	private void executeAPPService(String Username, String Password,
			String APP_URL, String Datapool_URL) {
		String appURL = APP_URL + "?DP_URL=" + Datapool_URL;
		Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter(Username, Password));
		WebResource service = c.resource(appURL);
		ClientResponse response = service.accept(MediaType.APPLICATION_XML)
				.get(ClientResponse.class);
		this.response_APPService = response.getEntity(String.class);
		//System.out.println("Response:" + this.response_APPService);
	}

	public void executeAPPService_Query(String Username, String Password,
			String APP_URL, String Event) throws IOException,
			ClassNotFoundException, ClientHandlerException,
			UniformInterfaceException, JDOMException {
		String appURL = APP_URL + "?Request=" + Event;
		Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter(Username, Password));
		WebResource service = c.resource(appURL);
		ClientResponse response = service.accept(MediaType.APPLICATION_XML)
				.get(ClientResponse.class);
		this.response_APPService = response.getEntity(String.class);
		//System.out.println("Response:" + this.response_APPService);
	}

	private List<String> getContentofNewState(String Username, String Password,
			String NewState_URL) throws IOException, ClassNotFoundException {
		ArrayList<String> content = new ArrayList<String>();
		Client c = Client.create();
		c.addFilter(new HTTPBasicAuthFilter(Username, Password));
		WebResource service = c.resource(NewState_URL);
		ClientResponse response = service.accept(MediaType.TEXT_PLAIN).get(
				ClientResponse.class);
		// String Content = response.getEntity(String.class);
		InputStream Content = response.getEntityInputStream();
		ObjectInputStream ois = new ObjectInputStream(Content);
		content = (ArrayList<String>) ois.readObject();
		return content;
	}

	private String getURLfromResponse(String Response) throws JDOMException,
			IOException {
		String NewState_URL = "";

		SAXBuilder builder = new SAXBuilder();
		Document read_doc = builder.build(new StringReader(Response));

		Element ele = read_doc.getRootElement();
		Element elex = ele.getChild("state_URL");
		NewState_URL = elex.getText();
		//this.newState_URL = NewState_URL;
		return NewState_URL;
	}

}
