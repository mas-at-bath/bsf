/* 
 * @(#)DomainWriter	1.0 15/09/2010
 *
 * Dimitrios Traskas
 * Bath University 
 */

package edu.bath.institution;

import java.io.*;

/**
 * The TextWriter updates the specified text file.
 *  
 * @version 1.0 15/09/2010
 * @author Dimitris Traskas
 */
public class TextWriter {
	private String filename;
	private File file;
	private BufferedWriter writer;	
	private boolean bAppend;
	
	/**
     * Constructs a TextWriter with the specified filename. 
     */
	public TextWriter(String filename, boolean bAppend){
		this.filename = filename;
		this.bAppend = bAppend;
	}
	
	/**
     * Updates the text file.
     *
     * @return <code>org.bath.agents.ReturnCode.SUCCESS</code> if the event was successfully added.
     * 
     */
	public ReturnCode replace(String pattern, String text){
		
		ReturnCode rc = ReturnCode.FAILURE;
		try {			
			 File file = new File(filename);
             BufferedReader reader = new BufferedReader(new FileReader(file));             
             
             String line = "";
             String oldtext = "";
             while((line = reader.readLine()) != null){             
                 oldtext += line + "\r\n";
             }
             reader.close();                         
             String newText = oldtext.replaceAll(pattern, text);
            
             writer = new BufferedWriter(new FileWriter(file));
 			 writer.write(newText);
 			 writer.close();				
 			 rc = ReturnCode.SUCCESS;
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return rc;
	}

	/**
     * Updates the text file.
     *
     * @return <code>org.bath.agents.ReturnCode.SUCCESS</code> if the event was successfully added.
     * 
     */
	public ReturnCode update(String text){
		//System.out.println("filename: "+ filename);
		//System.out.println("update: " + text);
		ReturnCode rc = ReturnCode.FAILURE;
		try {
			file = new File(filename);
			writer = new BufferedWriter(new FileWriter(file, bAppend));
			writer.write(text);		
			writer.close();
			rc = ReturnCode.SUCCESS;
		} catch (IOException e) {
			e.printStackTrace();
		}		
		//System.out.println("rc: "+ rc);
		return rc;
	}
	
	public ReturnCode copy(String toCopy)
	{
		ReturnCode rc = ReturnCode.FAILURE;
		
		try {
			File inputFile = new File(toCopy);
		    File outputFile = new File(filename);
	
		    FileReader in = new FileReader(inputFile);
		    FileWriter out = new FileWriter(outputFile);
		   
		    int c;
		    while ((c = in.read()) != -1)
		      out.write(c);
		    
		    in.close();
			out.close();
			rc = ReturnCode.SUCCESS;
		} catch (IOException ex){
			ex.printStackTrace();
		}
		
		return rc;
	}
	
	public ReturnCode copyAppend(String toCopy,String text)
	{
		ReturnCode rc = ReturnCode.FAILURE;
		try {
			File inputFile = new File(toCopy);
		    File outputFile = new File(filename);
	
		    FileReader in = new FileReader(inputFile);
		    FileWriter out = new FileWriter(outputFile);
		   
		    int c;
		    while ((c = in.read()) != -1)
		      out.write(c);
		    
		    out.write(text);
		    
		    in.close();
			out.close();
			rc = ReturnCode.SUCCESS;
		} catch (IOException ex){
			ex.printStackTrace();
		}
		return rc;
	}
	
	
	/**
	 * Returns the filename used by the TextWriter.
	 */
	public String getFilename(){
		return this.filename;
	}		
}
