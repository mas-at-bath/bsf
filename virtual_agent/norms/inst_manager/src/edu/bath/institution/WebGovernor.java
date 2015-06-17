/*
 * WebGovernor 
 * - The interface between web services and the institution manager. 
 * It passes through external events observed by agents to the web-based 
 * insitution service, and receives norms from the web services. 
 * The main role is absolutely same as the Governor running on local.   
 * 
 * 		@author		JeeHang Lee
 * 		@date		05 Aug 2013
 * 
 * (+) Adding multiple institution (Aug 2013, JeeHang Lee) 
 */

package edu.bath.institution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.JDOMException;

//import edu.bath.APP2REST.Admin_Tool.InstClient;
import edu.bath.institution.InstClient;

public class WebGovernor {

	private static final String SERVICE_URI = "http://fog.cs.bath.ac.uk:48084/edu.bath.APP2REST/WS/APP_Services/InstManager";
	private static final String DATA_URI = "http://fog.cs.bath.ac.uk:48084/edu.bath.APP2REST/WS/Datapool/A2";
	private String id = "admin";
	private String pwd = "admin";
	
	InstClient ic = null;
	List<String> states = null;
	
	public WebGovernor() {
		// to do
		List<String> states = new ArrayList<String>();
	}
	
	public ReturnCode updateState(String evt) {
		
		ReturnCode rc = ReturnCode.SUCCESS;
		if (ic == null) {
			ic = new InstClient(SERVICE_URI, DATA_URI, id, pwd, evt);
			try {
				states = ic.UploadEventandGetNewState();
			} catch (JDOMException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			
		}
		return rc;
	}

	public List<String> getCurrentState() {
		List<String> foundStrings = new ArrayList<String>();
		for (String s : states) {
			if (s.startsWith("obl(") == true)
				foundStrings.add(s);
		}
		return foundStrings;
	}
	
	public List<String> getCurrentState(String filter) {
		List<String> filtered = new ArrayList<String>();
		for (String state : states) {
			if (state.contains(filter) == true) {
				filtered.add(state);
			}
		}
		return filtered;
	}
}
