package edu.bath.sumoVehicles;

/*Derived from MOEAFramework-2.3-Manual.pdf*/

import java.util.Arrays;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.Analyzer;

public class MOEAExperiment {

	public static void main(String[] args)
	{

		//no reference set available..
		/*Instrumenter instrumenter = new Instrumenter()
			.withProblemClass(MOEAExperimentProblem.class)
			.withFrequency(10)
			.attachAll();*/

		Analyzer analyzer = new Analyzer()
		.withProblemClass(MOEAExperimentProblem.class)
		.includeAllMetrics()
		.showStatisticalSignificance();

		NondominatedPopulation result= new Executor()
			.withProblemClass(MOEAExperimentProblem.class)
			.withAlgorithm("NSGAII")
			.withMaxEvaluations(20000)
			.withProperty("populationSize", 100)
			//.withInstrumenter(instrumenter)
			.run();

		int n=0;
		for (Solution solution : result)
		{
			System.out.println("Solution " + n + ": " + solution.getObjective(0) + " " + solution.getObjective(1));	
			n++;
		}
		/*Accumulator accumulator = instrumenter.getLastAccumulator();
		for (int i=0; i<accumulator.size("NFE"); i++) {
			System.out.println(accumulator.get("NFE", i) + "\t" +
			accumulator.get("GenerationalDistance", i));
		}*/


		/*this runs in some sort of analysis mode..
		Executor executor = new Executor()
			.withProblemClass(MOEAExperimentProblem.class)
			.withMaxEvaluations(10000);

		analyzer.addAll("NSGAII",
		executor.withAlgorithm("NSGAII").runSeeds(50));
		try
		{
			analyzer.printAnalysis();
		}
		catch (Exception e)
		{
			System.out.println("error with analysis");
			e.printStackTrace();
		}*/

	}


}
