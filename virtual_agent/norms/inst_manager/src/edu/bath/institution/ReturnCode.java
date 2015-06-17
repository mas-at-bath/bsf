/* 
 * @(#)ReturnCode.java	2.0 05/09/2010
 *
 * Dimitrios Traskas
 * Bath University 
 */

package edu.bath.institution;

/**
 * The ReturnCode enumerator holds all the different return codes of the InstitutionEngine.
 *  
 * @version 1.0 25/05/2010
 * @author Dimitris Traskas
 */
public enum ReturnCode {
	FAILURE(-1, "Failed"), SUCCESS(1, "Succeeded"), 
	CANNOTFINDCONTRACT(2, "Cannot find contract for specified agent"),
	CANNOTWRITEEVENTS(3, "Cannot update events file"), CANNOTUPDATEDOMAIN(4, "Cannot update domain file"), 
	CANNOTEXECINSTAL(5, "Cannot execute InstAL script"), CANNOTEXECSOLVER(6, "Cannot execute ASP solver"), CANNOTEXECTIKZ(6, "Cannot execute tikz generator");
	
	private int rc;
	private String message;
	
	ReturnCode(int rc, String message){
		this.rc = rc;
	}
	
	public int RC(){
		return this.rc;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
