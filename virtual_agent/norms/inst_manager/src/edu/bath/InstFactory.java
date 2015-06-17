/*
 * InstFactory 
 * - Institution Factory, a container of multiple institution,  manages the multiple instances 
 * of institutional models implemented by Governor. Each instance of Governors performs the 
 * social reasoning in accordance with each institution specification. 
 * 
 * 		@author		JeeHang Lee
 * 		@date		05 Aug 2013
 * 
 * (+) Adding multiple institution (Aug 2013, JeeHang Lee) 
 */

package edu.bath;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import edu.bath.institution.Governor;
import edu.bath.institution.Contract;
import edu.bath.institution.WebGovernor;

public class InstFactory {
	
	private boolean webBasedEnabled = false;

	// A collection of an local institutional model, <name, instance> pair.
	HashMap<String, Governor> gmap = null;
	// A collection of an web based institutional model, <name, instance> pair.
	HashMap<String, WebGovernor> wgmap = null;
	
	// Constructor. check the instance of the institution map,
	// and create governor instances in the hash map.
	public InstFactory(String[] args) {
		if (gmap == null) gmap = new HashMap<String, Governor>();
		for (String arg : args) {
			if (isInstSpec(arg) == true) {
				String filename = arg.substring(0, arg.indexOf('.'));
				gmap.put(filename,  initGovernor(filename, arg));
			}
		}
		
		if (webBasedEnabled)
		{
			if (wgmap == null) wgmap = new HashMap<String, WebGovernor>();
			wgmap.put("webQ", initWebGovernor());
		}

	}
	
	// initialise the governor
	private Governor initGovernor(String dir, String instfile) {
		Governor gov = new Governor(dir, instfile);
		String initialState = "initial state";
		String domainState = "domain state";
		Contract contract = new Contract(dir, initialState, domainState);
		gov.addContract(contract);
		return gov;
	}
	
	private WebGovernor initWebGovernor() {
		WebGovernor wg = new WebGovernor();
		return wg;
	}
	
	public void updateInitials(String evt) {
		for (String name : gmap.keySet()) {
			System.out.println("updating " + name + " with initials " + evt);
			gmap.get(name).updateInitials(evt, name);
		}
	}

	// update state of all institutional model in the map
	public void updateStates(String evt) {
		for (String name : gmap.keySet()) {
			System.out.println("updating " + name + " with " + evt);
			gmap.get(name).updateState(evt, name);
		}

		if (webBasedEnabled)
		{
			for (String name : wgmap.keySet()) {
				wgmap.get(name).updateState(evt);
			}
		}
	}
	
	// update state of single institution
	public void updateStates(String evt, String model) {
		gmap.get(model).updateState(evt, model);
	}
	
	// get all current normative states from multiple institution
	public List<String> getCurrentStates() {
		List<String> state = new ArrayList<String>();
		String nc = null;
		for (String name : gmap.keySet()) {
			//VB changed for multiple returns
			//nc = gmap.get(name).getCurrentState(name);
			//if (nc != null) state.add(nc);
			List<String> foundReturns = gmap.get(name).getCurrentState(name);
			for (String returnItr : foundReturns)
			{
				//VB dropping inst name from returned val.. actually maybe not needed..
				//System.out.println("dropping " + returnItr.substring(returnItr.lastIndexOf(','), returnItr.length()));
				//returnItr = returnItr.substring(0,returnItr.lastIndexOf(','));
				state.add(returnItr);
			}
		}
		
		if (webBasedEnabled)
		{
			for (String name : wgmap.keySet()) {
				List<String> foundReturns = wgmap.get(name).getCurrentState();
				for (String returnItr : foundReturns)
				{
					state.add(returnItr);
				}
			}
		}
		return state;
	}
	
	// get current state of particular institution
	public List<String> getCurrentStates(String model) {
		List<String> state = new ArrayList<String>();
		List<String> foundReturns = gmap.get(model).getCurrentState(model);

		if (webBasedEnabled)
		{
			foundReturns = wgmap.get(model).getCurrentState();
		}

		for (String returnItr : foundReturns)
		{
			state.add(returnItr);			
		}

		return state;
	}
	
	// Query the current normative consequences of the 'action'.
	// if found then the obligation will be accumulated and published afterwards.
	// Otherwise, no result at all for agents.
	public List<String> queryObligations(String action) {
		List<String> res = new ArrayList<String>();
		for (String name : gmap.keySet()) {
			if (gmap.get(name).queryObligation(action, name) == true)
				res = (gmap.get(name).getObligation(action, name));
		}
		return res;
	}
	
	// utility. input "filenname.ext", handle only file path, not full path
	private String getFilename(String path) {
		return path.substring(0, path.indexOf("."));
	}
	
	private boolean isInstSpec(String str) {
		return (str.indexOf(".ial") > 0);
	}
}
