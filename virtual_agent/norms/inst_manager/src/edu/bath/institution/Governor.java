package edu.bath.institution;

import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Paths;
import java.io.FileNotFoundException;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Governor {
        
    private HashMap<String, Contract> contracts;
    
    private String mainPath;
    private String solverPath;
    private String runPath;
    private String institutionFile;
    
    private ScriptProcessor institution;
    private ScriptProcessor solver; 
    private ScriptProcessor tikz; 
    
    private TextWriter timeWriter;
    private TextWriter queryWriter;
    private TextWriter dynamicWriter;
    private TextWriter domainWriter;
    
    private String createEvent;
    private String instName;
    private String query;
    private boolean isWindows=false;
    private List<String> iniStates = new ArrayList<String>();
    private String instShortName = "";

    public Governor(String institutionFile){
        this.contracts = new HashMap<String, Contract>();

	if (System.getProperty("os.name").startsWith("Windows")) 
	{
      		//Covers most flavours of windows at present..
		isWindows=true;
    	} else {
        	isWindows=false;
    	} 

	if (isWindows) 
	{
		mainPath = System.getProperty("user.dir") + "\\inst_model\\";
        	setTimeWriter(new TextWriter(mainPath + "\\clingo\\timeline.lp", false));
        	setQueryWriter(new TextWriter(mainPath + "\\clingo\\query.lp", false));
	}
	else
	{
        	mainPath = System.getProperty("user.dir") + "/inst_model/";
        	setTimeWriter(new TextWriter(mainPath + "/clingo/timeline.lp", false));
        	setQueryWriter(new TextWriter(mainPath + "/clingo/query.lp", false));
	}
        this.institutionFile = institutionFile;
        createEvent = searchCreateEvent(mainPath + institutionFile);
        instName = searchInstitutionName(mainPath + institutionFile);
        //System.out.println("name: "+ instName);
        
        setDynamicWriter(new TextWriter(mainPath + "dynamic.ial", false));
        setDomainWriter(new TextWriter(mainPath + "domain.idc", false));
        
        setInstitution(new ScriptProcessor(mainPath + "instal_script.sh"));
        setSolver(new ScriptProcessor(mainPath + "solver_script.sh"));
	setTikz(new ScriptProcessor(mainPath + "genStatePDF.sh"));
    }
    
    public Governor(String dir, String institutionFile){
        this.contracts = new HashMap<String, Contract>();
	if (System.getProperty("os.name").startsWith("Windows")) 
	{
      		//Covers most flavours of windows at present..
		isWindows=true;
    	} else {
        	isWindows=false;
    	} 

	if (isWindows)
	{
        	solverPath = Paths.get("").toAbsolutePath().getParent().toString() + "\\inst_model\\";
        	mainPath = solverPath + dir  + "\\";
        	setInstitution(new ScriptProcessor(mainPath + "instal_script.sh"));
        	setSolver(new ScriptProcessor(mainPath + "solver_script.sh"));
	}
	else
	{
	        solverPath = Paths.get("").toAbsolutePath().getParent().toString() + "/inst_model/";
        	mainPath = solverPath + dir  + "/";
		setInstitution(new ScriptProcessor(mainPath + "instal_script.sh", mainPath));
        	setSolver(new ScriptProcessor(mainPath + "solver_script.sh", mainPath));

		setTikz(new ScriptProcessor(mainPath + "genStatePDF.sh", mainPath));

	}
        runPath = Paths.get("").toAbsolutePath().toString();
        this.institutionFile = institutionFile;

        createEvent = searchCreateEvent(mainPath + institutionFile);
        instName = searchInstitutionName(mainPath + institutionFile);
        //System.out.println("name: "+ instName);
        
        setTimeWriter(new TextWriter(mainPath + "timeline.lp", false));
        setQueryWriter(new TextWriter(mainPath + "query.lp", false));
        setDynamicWriter(new TextWriter(mainPath + "dynamic.ial", false));
        setDomainWriter(new TextWriter(mainPath + "domain.idc", false));
        
    }
    
    public void addContract(Contract contract){
        contracts.put(contract.getContractId(), contract);
	System.out.println("added contract " + contract.getContractId());
    }
    
    public Contract getContract(String contractId){
        return contracts.get(contractId);
    }
    
	/**
	 * Removes the <code>org.bath.agents.Contract</code> instance of the specified Agent id.
	 * 
	 * @param contractId - is the unique id of the contract to retrieve.
	 */
    public void removeContract(String contractId){
        contracts.remove(contractId);
    }
    
    //VB this method is a bit of a messy error prone quick way to get initials added to the query.lp file.. use with caution..
    public void updateInitials(String inis, String contractId) 
    {
	inis = inis.replaceFirst("initially", "");
	inis = inis.substring(1, inis.length()-1);
	String[] initials = inis.split("\\),");
	for (String iniString : initials)
	{
		
		if(iniString.charAt(iniString.length()-1) != ')' )
		{
			iniString = iniString + ")";
		}
		iniStates.add(iniString);
		System.out.println(iniString);
	}
    }

	/**
	 * Updates the state of the Agent Id's Contract when the Governor calls InstAL and Clingo.
	 *
	 * @param event - is the event to be added in the Governor.
	 * @param contractId - is the unique id of the Contract to be used.
	 * @param eventPattern - is the pattern we are searching in the traces.
	 * 
	 * @return <code>org.bath.agents.ReturnCode.SUCCESS</code> when the process completes successfully.
	 */
    public ReturnCode updateState(String event, String contractId) {

        ReturnCode rc = ReturnCode.SUCCESS;     

	if (contractId.indexOf("_") != -1)
	{
		instShortName=contractId.substring(0,contractId.indexOf("_"));
		System.out.println("inst name set to " + instShortName);
	}
        
        // first retrieve the Contract for the specified agent id.
        Contract contract = contracts.get(contractId);
        if (contract == null) return ReturnCode.CANNOTFINDCONTRACT;
        
        // blocked by jeehang. For the flexibility
        /*
        String domain = contract.getDomainSpecification();
        if (domain.length() > 0) {              
                //System.out.println("Update domain");
                //System.out.println("Domain:" + domain);
                rc = domainWriter.update(domain);
                if (rc == ReturnCode.FAILURE) return ReturnCode.CANNOTUPDATEDOMAIN;
        }
        */       
                
        // get the appropriate timeline
        //rc = timeWriter.copy(mainPath + "time2.lp");
                        
        // get the appropriate query
        rc = queryWriter.update(updateQuery(event));
        
        // get dynamic file 
        ArrayList<String> state = contract.getState();
        
        // blocked by jeehang. No more needed
        // rc = dynamicWriter.copyAppend(mainPath + institutionFile,createInitial(state));
        
        // execute InstAL at this point.
        if ((rc = institution.run()) == ReturnCode.FAILURE) return ReturnCode.CANNOTEXECINSTAL;
        
        // execute clingo at this point and collect the results.
        if ((rc = solver.run()) == ReturnCode.FAILURE) return ReturnCode.CANNOTEXECSOLVER;

        // execute tikz 
	System.out.println("XXXXXXXXXXXXX: calling tikz PDF generation");
        if ((rc = tikz.run()) == ReturnCode.FAILURE) return ReturnCode.CANNOTEXECTIKZ;
        
        // parse the results from clingo for the specified pattern and then add them in the current state.
        contract.updateCurrentState(parseResults("holdsat..*.,1."));
        
        // needs also update violations
        contract.updateViolations(parseResults("occured.viol..*.,1."));
        
        return rc;
    }
    
    private String updateQuery(String event)
    {
	String returnStr = "observed("+createEvent+",t0).\n" +
               "observed("+event+",t1).\n";
	System.out.println("short inst name: " + instShortName);
	
	if (createEvent.equals("") && !instShortName.equals(""))
	{
		//this line for VSL scenario returnStr = "observed(start,vsl,t0).\n" + "observed("+event+",vsl,t1).\n";
               //returnStr = "compObserved(start,t0).\n" + "compObserved("+event+",t1).\n";
		if (instShortName.equals("multi"))
		{
			returnStr = "compObserved("+event+",0).\n";
		}
		else
		{
			returnStr = "observed("+event+","+instShortName+",0).\n";
		}

		if (iniStates.size() > 0)
		{
			String tempShortName = instShortName;
			if (instShortName.equals("multi")) { tempShortName="crashgroup"; }
			System.out.println("adding initials to query.lp too");
			for (String iniState : iniStates)
			{
				String fullIni="holdsat("+iniState+","+tempShortName+",I) :-\n"+
   					"inst("+tempShortName+"), start(I).\n";
				returnStr=returnStr+fullIni;
			}
			iniStates.clear();
		}
	}
	else
	{
		System.out.println("don't seem to know inst name.. " + instShortName);
	}


	
        return returnStr;
    }
    
    public ArrayList<String> parseResults(String pattern){
            
        ArrayList<String> parsed = new ArrayList<String>();
        try {           
            Pattern p = Pattern.compile(pattern);
            Scanner in = new Scanner(new File(mainPath + "results.txt"));           
            while (in.hasNextLine()){
                String s = in.nextLine();
                String[] line = s.split(" ");

                for(int i=0; i<line.length; i++){
                    Matcher m = p.matcher(line[i]);
                    if (m.find()){
                        String match = m.group();
                        if (!match.matches(".*live.*"))
                        {
                            parsed.add(Extractor.patternExtractor(pattern, match,0));                                    
                        }
                    }                                       
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println(parsed.size());
        return parsed;
    }
    
    public Boolean queryDomainFluent(String fluent,String contractId)
    {
        Contract contract = contracts.get(contractId);
        if (contract == null) return false;
        return contract.queryFluent(fluent);
    }
    
    public Boolean queryPower(String action,String contractId)
    {
        Contract contract = contracts.get(contractId);
        if (contract == null) return false;
        return contract.queryFluent("pow("+instName+","+action+")");
    }
    
    public Boolean queryPermission(String action,String contractId)
    {
        Contract contract = contracts.get(contractId);
        if (contract == null) return false;
        return contract.queryFluent("perm("+action+")");
    }
    
    public Boolean queryObligation(String action,String contractId)
    {
        Contract contract = contracts.get(contractId);
        if (contract == null) return false;
        return contract.queryFluent("obl("+action);
    }
    
    public Boolean queryViolation(String action,String contractId)
    {
        Contract contract = contracts.get(contractId);
        if (contract == null) return false;
        return contract.queryViolation(action);
    }
    
    // assuming that only one obligation should be generated (jeehang)
    // VB changed to multiple as I think thats valid..List<String>
    //public String getCurrentState(String contractId) {
    public List<String> getCurrentState(String contractId) {
	List<String> foundStrings = new ArrayList<String>();
    	Contract contract = contracts.get(contractId);
	foundStrings = contract.getFluent("obl(");
    	return foundStrings;
    }
    
    public List<String> getObligation(String action, String contractId) {
    	List<String> foundStrings = new ArrayList<String>();
    	Contract contract = contracts.get(contractId);
        if ((contract != null) && (contract.queryFluent("obl("+ action) == true))
        	foundStrings = contract.getFluent("obl("+ action);
        return foundStrings;
    }
    
    public void setTimeWriter(TextWriter timeWriter){
        this.timeWriter = timeWriter;
    }
    
    public void setQueryWriter(TextWriter queryWriter){
        this.queryWriter = queryWriter;
    }
    
    public void setDomainWriter(TextWriter domainWriter){
        this.domainWriter = domainWriter;
    }
    
    public void setDynamicWriter(TextWriter dynamicWriter){
        this.dynamicWriter = dynamicWriter;     
    }
    
    private String searchCreateEvent(String filename)
    {
        String event = "";
        try {                   
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));             
    
            String line = null;
	    boolean createEventFound = false;    

            while((line = reader.readLine()) != null){  
            	if (line.startsWith("create event")) 
            	{
            		String[] w = line.split(" ");
            		String sub = w[w.length-1]; 
            		event = sub.substring(0, sub.length()-1);
			createEventFound=true;
            	}
            }
            reader.close();  

	    if (!createEventFound)
	    {
		System.out.println("WARNING: No create event found, this may or may not cause you problems!");
	    }                       
        } catch (IOException e) {
            e.printStackTrace();
            event = null;
        }               
        return event;
    }
    
    private String searchInstitutionName(String filename)
    {
        String name = "";
        try {                   
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));             
            String line = null;
            while((line = reader.readLine()) != null){  
            	if (line.startsWith("institution")) 
	            {
	                    String[] w = line.split(" ");
	                    String sub = w[w.length-1]; 
	                    name = sub.substring(0, sub.length()-1);
	            }
            }
	        reader.close();                         
        } catch (IOException e) {
            e.printStackTrace();
            name = null;
        }               
        return name;
    }
    
   /* private String createInitial(ArrayList<String> updatedState) {
        String result = "";
           for(int i=0; i<updatedState.size(); i++){
                result += "initially " + updatedState.get(i) + ";\n";
           }
           return result;
    }*/

    public void setInstitution(ScriptProcessor institution){
        this.institution = institution;
    }

    public void setSolver(ScriptProcessor aspSolver){
        this.solver = aspSolver;
    }

    public void setTikz(ScriptProcessor tikzCall){
        this.tikz = tikzCall;
    }
    
    // Deletes the backup files
    public void cleanUp()
    {
        File file = new File(mainPath + "timeline.lp");
        file.delete();
        
        file = new File(mainPath + "query.lp");
        file.delete();
        
        file = new File(mainPath + "domain.idc");
        file.delete();
        
        file = new File(mainPath + "results.txt");
        file.delete();
        
        file = new File(mainPath + "dynamic.ial");
        file.delete();
    }
    
    public void finalize()
    {
        cleanUp();
    }
}
