package edu.bath.sumoVehicles;

import java.util.*;

public class AllSpeeds
{
	//list of speeds
	private String myEdge;
	private ArrayList<Double> knownSpeeds = new ArrayList<Double>();

	public AllSpeeds(String edg, Double speed)
	{
		knownSpeeds.add(speed);
		myEdge=edg;
	}
		
	public void addSpeed(Double newSpeed)
	{
		knownSpeeds.add(newSpeed);
	}

	public String getEdge()
	{
		return myEdge;
	}

	public int getNumberSpeeds()
	{
		return knownSpeeds.size();
	}

	public Double getMeanSpeed()
	{
		Double speedTotal=0D;
		for (Double tempD : knownSpeeds)
		{
			speedTotal=speedTotal+tempD;
		}
		Double meanVal = speedTotal/getNumberSpeeds();
		return meanVal;
	}

}
