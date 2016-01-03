package edu.bath.sumoVehicles;

/*Derived from MOEAFramework-2.3-Manual.pdf*/

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

public class MOEAExperimentProblem extends AbstractProblem {

	public MOEAExperimentProblem() {
		super(3, 2);
	}

	@Override
	public Solution newSolution() 
	{
		Solution solution = new Solution(numberOfVariables, numberOfObjectives);
		solution.setVariable(0, EncodingUtils.newInt(1, 20));
		solution.setVariable(1, EncodingUtils.newInt(-10, 10));
		solution.setVariable(2, EncodingUtils.newInt(-1, 1));

		/*for (int i = 0; i < numberOfVariables; i++) {
			solution.setVariable(i, new RealVariable(-5.0, 5.0));
		}*/
		return solution;
	}

	@Override
	public void evaluate(Solution solution) 
	{
		//double[] x = EncodingUtils.getReal(solution);
		int x = EncodingUtils.getInt(solution.getVariable(0));	
		int y = EncodingUtils.getInt(solution.getVariable(1));
		int z = EncodingUtils.getInt(solution.getVariable(2));

		System.out.println("attempting solution with x: " + x + ", y: " + y + " and z: " +z);
		try
		{
			SumoXMPPSim ps = new SumoXMPPSim("127.0.0.1", "sumo", "jasonpassword", "jasonSensorVehicles", "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/test1-vehicle", true, 0);
			String[] startArgs = new String[2];
			startArgs[0] = "m25-nogui";
			startArgs[1] = "50000";
			ps.setup(startArgs);
			//ps.setUseDebug(true);
			ps.setUseRealtime(false);
			ps.addAOISender();
			ps.run();
			System.out.println("XXXXXXXXXXX RUN FINISHED XXXXXXXXXXXXXXXX");
			ps.getResults();
		}
		catch (Exception e)
		{
			System.out.println("unable to start SUMO..");
			e.printStackTrace();
		}

		double f1 = 0.0;
		double f2 = 0.0;
		for (int i = 0; i < numberOfVariables - 1; i++) 
		{
			f1 += -10.0 * Math.exp(-0.2 * Math.sqrt(
			Math.pow(x, 2.0) + Math.pow(y, 2.0)));
		}
		for (int i = 0; i < numberOfVariables; i++) 
		{
			f2 += Math.pow(Math.abs(x), 0.8) + 5.0 * Math.sin(Math.pow(x, 3.0));
		}
		solution.setObjective(0, f1);
		solution.setObjective(1, f2);
	}
}	
