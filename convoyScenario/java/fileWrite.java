// Internal action code for project baianoTeam.mas2j



package io;

import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;

import java.io.*;



public class fileWrite extends DefaultInternalAction {



    @Override

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action
		//double myX;
		try
		{
       			//ts.getAg().getLogger().info("executing internal action 'io.fileWrite'");
		
			//try
			//{
			///System.out.println("Number of args: " + args.length);
			NumberTerm agentFileNameTerm = (NumberTerm)args[3];
			int agentFileName = (int) agentFileNameTerm.solve();
			///System.out.println(agentFileName);
			String myNumberString = Integer.toString(agentFileName);
			String appendName = "DataFile.txt";
   			String filename= myNumberString.concat(appendName);
			///String filename="test.txt";
		///	System.out.println(filename);
			boolean append = true;
			FileWriter fw = new FileWriter(filename,append);
			int numArgs = args.length;
			if (numArgs == 4)
			{
			NumberTerm currentX = (NumberTerm)args[0];
			double myX = currentX.solve();
			String stringX= Double.toString(myX);
			NumberTerm currentZ = (NumberTerm)args[1];
			double myZ = currentZ.solve();
			String stringZ= Double.toString(myZ);
			NumberTerm currentAngle = (NumberTerm)args[2];
			double myAngle = currentAngle.solve();
			String stringAngle= Double.toString(myAngle);
			//fw.write("test line\n");
    			
   			fw.write(stringX + " " + stringZ + " " + stringAngle +" \n");//appends the string to the file
			}
			else
			{
				fw.write("wrong arguments!\n");
			}
			fw.close();

		return true;

		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


