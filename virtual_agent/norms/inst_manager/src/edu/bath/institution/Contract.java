/* 
 * @(#)Contract.java 1.0 15/09/2010
 *
 * Dimitrios Traskas
 * Bath University 
 */

package edu.bath.institution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Contract contains the institution-contract, domain information and current state of each Agent.
 * The Governor contains a collection of contracts per Agent or Group of Agents which client code can create.
 *  
 * @version 1.0 15/09/2010
 * @author Dimitris Traskas
 */
public class Contract {
	
	private String contractId;
	private String domainSpecification;
	private ArrayList<String> state;
	private ArrayList<String> violations;
	
	/**
     * Initialises a new instance of a <code>org.bath.agents.Contract</code> for the specified Agent id.
     * 
     * @param contractId - is the unique id that identifies this Contract.
     * @param initialState - is the initial state of the contract to be added in the .ial file.
     * @param domainSpecification - is the domain information for this specific contract.
     * 
     */
	public Contract(String contractId, String initialState, String domainSpecification){
		this.contractId = contractId;
		this.domainSpecification = domainSpecification;
		state = new ArrayList<String>();
		state.add(initialState);
		violations = new ArrayList<String>();
		System.out.println("Initial State: " + state.get(0));
	}
	
	/**
     * Gets the unique id of the Contract.
     */	
	public String getContractId(){
		return contractId;
	}
	
	/**
     * Gets the domain specification for the Contract.
     */	
	public String getDomainSpecification(){
		return domainSpecification;
	}
	
	/**
     * Gets the current state of the Contract.
     */	
	public ArrayList<String> getState(){
		return state;
	}
	
	/**
     * Resets the domain specification and the current state of the Contract.
     */	
	public void reset(){
		domainSpecification = "";
		state = new ArrayList<String>();
	}
	
	/**
     * Set the domain specification for the Contract.
     */	
	public void setDomainSpecification(String domainSpecification){
		this.domainSpecification = domainSpecification;
	}
	
	/**
     * Set the current state of the Contract (TO BE USED BY THE GOVERNOR).
     */
	public void setCurrentState(ArrayList<String> state){
		this.state = state;
	}
	
	/**
     * Updates the current state of the Contract (TO BE USED BY THE GOVERNOR).
     */
	public void updateCurrentState(ArrayList<String> state){
		System.out.println("new state has elements: " + state.size() + " and contains :" + state.toString());
		this.state = state; //MDV changed
	}
	
	public ArrayList<String> getViolations() {
		return violations;
	}
	
	public void updateViolations(ArrayList<String> violations) {
		System.out.println("violations: " + violations);
		for (Iterator<String> v = violations.iterator(); v.hasNext(); )
		{
			String el = v.next();			
			this.violations.add(el);
		}
	}
	
	public Boolean queryFluent(String fluent) {
		return state.contains(fluent);
	}
	
	//VB changed to multiple returns
	public List<String> getFluent(String fluent) {
		//String foundString = null;
		List<String> foundStrings = new ArrayList<String>();
		int countFound=0;
		for (String s : state) {
			if (s.startsWith(fluent))
			{ 
				System.out.println("for requested fluent " + fluent + " found: " + s);
				//foundString = s;
				foundStrings.add(s);
				countFound++;
			}
		}
		if (countFound > 1)
		{
			System.out.println("WARNING: Multiple returns found, this is still being tested!!");
		}
		return foundStrings;
	}
	
	public Boolean queryViolation(String violation)
	{
		return violations.contains(violation);	
	}
	
	
	/**
     * Queries the state retrieved from clingo for the specified pattern and returns the observed events.
     *
     * @param pattern - is the pattern we are querying in the state.
     * @return ArrayList<String> when the process completes successfully.
     */
	public ArrayList<String> queryObserved(String pattern){
		System.out.println("Pattern: " + pattern);
		Pattern p = Pattern.compile(pattern);
		ArrayList<String> events = new ArrayList<String>();
		System.out.println("In queryObserved()");
		for(int i=0; i<state.size(); i++){
			Matcher m = p.matcher(state.get(i));
			if (m.find()){
				String r = m.group();
				System.out.println(r);
				events.add(r);
			}
		}
		return events;
	}
	
	/**
     * Queries the state retrieved from clingo for the specified pattern and returns TRUE if fluent is in state, FALSE otherwise.
     *
     * @param pattern - is the pattern we are querying in the state. 
     * @return TRUE if the specified fluent exists in state, FALSE otherwise.
     */
	public boolean queryFluentPattern(String pattern){
		
		boolean rc = false;
		Pattern p = Pattern.compile(pattern);
		for(int i=0; i<state.size(); i++){
			Matcher m = p.matcher(state.get(i));
			if (m.find()){
				return true;
			}
		}
		return rc;
	}
	
	/**
     * Queries the state retrieved from clingo and returns TRUE when state exists in which fluents are true, FALSE otherwise.
     * TODO : Add later.
     * 
     * @return TRUE if the fluents are true in state, FALSE otherwise.
     */
	public boolean queryFutureState(ArrayList<String> fluents){
		// TODO : Add later.
		return false;
	}
}
