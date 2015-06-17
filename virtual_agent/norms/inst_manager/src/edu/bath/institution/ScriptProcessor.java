/* 
 * @(#)ScriptProcessor.java	2.0 05/09/2010
 *
 * Dimitrios Traskas
 * Bath University 
 */

package edu.bath.institution;

import java.io.*;


/**
 * The ScriptProcessor executes the specified script and connects with the <code>org.bath.agents.Governor</code>
 * so that new percepts are added in the environment.
 * 
 * @version 1.0 25/05/2010
 * @author Dimitris Traskas
 */
public class ScriptProcessor {

	private String filename;
	private String dir=null;
	
	public ScriptProcessor(String filename){
		this.filename = filename;
	}

	public ScriptProcessor(String filename, String dirname){
		this.filename = filename;
		this.dir=dirname;
	}
	
	public ReturnCode run() {
		
		ReturnCode rc = ReturnCode.FAILURE;
		
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			
			if (dir != null)
			{		
				//System.out.println("should run in " + dir);
				File dirFile = new File(dir);
				process = runtime.exec("sh " + filename,null,dirFile);
			}
			else
			{
				process = runtime.exec("sh " + filename);
			}
			
			InputStreamReader isrStdout = new InputStreamReader( process.getInputStream() );
			InputStreamReader isrStderr = new InputStreamReader( process.getErrorStream() );
			
			BufferedReader brStdout = new BufferedReader( isrStdout );
			BufferedReader brStderr = new BufferedReader( isrStderr );

			String line = null;
			while((line=brStdout.readLine())!=null)
			{
				System.out.println("ScriptProcessor: " + line );
			}
			
			while((line=brStderr.readLine())!=null)
			{
				System.out.println("ScriptProcessor: " + line );
			}
			
			process.waitFor();
			
			rc = ReturnCode.SUCCESS;
		} catch(Exception ex) {
		    Thread.currentThread().interrupt();
		    ex.printStackTrace();
		} finally {
		    if (process != null)
		    	process.destroy();
		}
		  
		return rc;
	}
}
