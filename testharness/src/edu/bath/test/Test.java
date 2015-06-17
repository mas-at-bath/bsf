package edu.bath.test;

import java.io.*;
 
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Test {

	private static long testStartTime = 0;
	private static long maxTestRunDuration = 60000;
	private static ByteArrayOutputStream baos;
	private static FileWriter testResults;
	private static final String endS = " \n";
	private static boolean laneCheck = false;
	private static boolean RDFCheck = false;
	private static boolean RDFLossCheck = false;
	private static int numLanes = 0;
	private static int RDFSent = 0;
	private static int RDFRec = 0;
	private static int RDFLossSent = 0;
	private static int RDFLossRec = 0;
	private static String rdfLossResult;
	private static float RDFLoss = 0.0f;
	private static long nanoToMili=1000000;
	private static ByteArrayOutputStream sumoStream;
	private static ByteArrayOutputStream rdfStream;

    	public static void main(String[] args) 
	{

		
		try {
			testResults = new FileWriter("testresult.xml",false);
			testResults.write("<testsuite name=\"BSF\" tests=\"2\">" + endS);
		}
		catch (Exception e) {
			System.out.println("error writing to file");
		}		

		testStartTime = System.currentTimeMillis();
		System.out.println("BEGIN: Testing middleware");
		testMiddleware();
		System.out.println("END: Testing middleware");
		waitUntil(1000*nanoToMili);
		System.out.println("BEGIN: Testing SUMO");
		testSUMO();
		System.out.println("END: Testing SUMO");

		try {
			testResults.write("</testsuite>");
			testResults.close();
		}
		catch (Exception e) {
			System.out.println("error writing to file");
		}

	}

	public static void testMiddleware()
	{
		rdfStream = new ByteArrayOutputStream();		
		Thread rdfThread = new Thread() 
		{
    			public void run() 
			{
				File buildFile = new File("../rdfUtils/build.xml");
   				Project project = new Project();
   				project.setUserProperty("ant.file", buildFile.getAbsolutePath());
				PrintStream ps = new PrintStream(rdfStream);

       				try {
            				project.fireBuildStarted();
        				DefaultLogger consoleLogger = new DefaultLogger();
        				consoleLogger.setErrorPrintStream(System.err);
					consoleLogger.setOutputPrintStream(ps);
        				consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        				project.addBuildListener(consoleLogger);
 
            				project.init();
            				ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            				project.addReference("ant.projectHelper", projectHelper);
            				projectHelper.parse(project, buildFile);
					project.executeTarget("run-rdfTest-nointeract");
					project.fireBuildFinished(null);
				}
				catch (Exception e) {
            				System.out.println(e);
        			}		
    			}  
		};

		rdfThread.start();

		System.out.println("RDF Module should be running, starting tests..");
		try {
			long endTime = testStartTime + maxTestRunDuration;
			System.out.println("Started at " + testStartTime + " and running until " + endTime);
			while ((endTime > System.currentTimeMillis()) && rdfThread.isAlive())
			{
				if (rdfStream.size() > 0)
				{
					String latestString = rdfStream.toString("UTF8");
					System.out.println(latestString);
					if (latestString.contains("TestRDF Rate"))
					{
						String[] splited = latestString.split("\\s+");
						RDFSent = Integer.parseInt(splited[8]);
						RDFRec = Integer.parseInt(splited[11]);
						System.out.println("RDF sent " + RDFSent + " and rec " + RDFRec);
						if (RDFSent > 1000)
						{
							//System.out.println("RDF min send/rec rate test passed");
							RDFCheck = true;
						}
					}
					else if (latestString.contains("TestRDF Loss"))
					{
						String[] splited = latestString.split("\\s+");
						RDFLossSent = Integer.parseInt(splited[7]);
						RDFLossRec = Integer.parseInt(splited[10]);
						System.out.println("RDFLoss sent " + RDFLossSent + " and rec " + RDFLossRec);
						rdfLossResult = splited[12];
						System.out.println("overall loss result " + rdfLossResult);
						if (rdfLossResult.equals("OK"))
						{
							RDFLossCheck = true;
							//System.out.println("RDF packet loss test passed");
						}
					}
					rdfStream.reset();
				}
			}

			System.out.println("ending test" + endS);
			if (rdfThread.isAlive())
			{
				rdfThread.interrupt();
			}
		}
		catch (Exception e)
		{
      			System.out.println(e);
        	}

		try {
			if (RDFCheck)
			{
				testResults.write("<testcase classname=\"RDF\" name=\"Sent\" status=\""+RDFSent+"\"/>" + endS);
				testResults.write("<testcase classname=\"RDF\" name=\"Received\" status=\""+RDFRec+"\"/>" + endS);
			}
			else
			{
				testResults.write("<testcase classname=\"RDF\" name=\"Sent\">"  + endS);
				testResults.write("  <failure type=\"Middleware Performanace, sent\"> "+RDFSent+" </failure>"  + endS);
				testResults.write("</testcase>"+ endS);
				testResults.write("<testcase classname=\"RDF\" name=\"Received\">"  + endS);
				testResults.write("  <failure type=\"Middleware Performanace, received\"> "+RDFRec+" </failure>"  + endS);
				testResults.write("</testcase>"+ endS);
			}

			if (RDFLossCheck)
			{
				testResults.write("<testcase classname=\"RDF\" name=\"Loss\" status=\""+(RDFLossSent-RDFLossRec)+"\"/>" + endS);
			}
			else
			{
				testResults.write("<testcase classname=\"RDF\" name=\"Loss\">"  + endS);
				testResults.write("  <failure type=\"Message loss \"> "+(RDFLossSent-RDFLossRec)+" </failure>"  + endS);
				testResults.write("</testcase>"+ endS);
			}
		}
		catch (Exception e) {
			System.out.println("error writing to file");
		}


	}

	public static void testSUMO()
	{
		sumoStream = new ByteArrayOutputStream();		
		Thread sumoThread = new Thread() 
		{
    			public void run() 
			{
				File buildFile = new File("../sumoVehicles/build.xml");
   				Project project = new Project();
   				project.setUserProperty("ant.file", buildFile.getAbsolutePath());
				PrintStream ps = new PrintStream(sumoStream);

       				try {
            				project.fireBuildStarted();
        				DefaultLogger consoleLogger = new DefaultLogger();
        				consoleLogger.setErrorPrintStream(System.err);
					consoleLogger.setOutputPrintStream(ps);
        				consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        				project.addBuildListener(consoleLogger);
 
            				project.init();
            				ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            				project.addReference("ant.projectHelper", projectHelper);
            				projectHelper.parse(project, buildFile);
					project.executeTarget("run-m25");
					project.fireBuildFinished(null);
				}
				catch (Exception e) {
            				System.out.println(e);
        			}		
    			}  
		};

		System.out.println("SUMO Started... waiting... ");
		sumoThread.start();
		System.out.println("Finished SUMO Sim..");

		waitUntil(1000*nanoToMili);

		//so for inst test, conditions should be, institution module receives flash, sends obligation. for jason should be no crash, and performs a lane change. not so worried about sumo. can leave off high debug level for that at the moment.

		System.out.println("Modules should be running, starting tests..");
		try {
			long endTime = testStartTime + maxTestRunDuration;
			System.out.println("Started at " + testStartTime + " and running until " + endTime);
			while (endTime > System.currentTimeMillis())
			{
				if (sumoStream.size() > 0)
				{
					String latestString = sumoStream.toString("UTF8");
					System.out.println(latestString);
					if (latestString.contains("to known lanes list"))
					{
						String[] splited = latestString.split("\\s+");
						numLanes = Integer.parseInt(splited[3]);
						System.out.println("checking lanes, expected 46801 and found " + numLanes);
						if (numLanes == 46801)
						{
							laneCheck = true;
						}
					}
					sumoStream.reset();
				}
			}

			System.out.println("ending test" + endS);
			sumoThread.interrupt();
		}
		catch (Exception e)
		{
      			System.out.println(e);
        	}


		try {
			if (laneCheck)
			{
				testResults.write("<testcase classname=\"SUMO\" name=\"Lane Count\" status=\"46801\"/>" + endS);
			}
			else
			{
				testResults.write("<testcase classname=\"SUMO\" name=\"Lane Count\">"  + endS);
				testResults.write("  <failure type=\"Incorrect Lane Count\"> "+numLanes+" </failure>"  + endS);
				testResults.write("</testcase>"+ endS);
			}
		}
		catch (Exception e) {
			System.out.println("error writing to file");
		}

	}

	public static void waitUntil(long delayTo)
	{		
		delayTo=delayTo+System.nanoTime();
		long currentT=0;
		do{
			currentT = System.nanoTime();
		}while(delayTo >= currentT);
	}

}
