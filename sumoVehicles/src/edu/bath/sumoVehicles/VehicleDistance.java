package edu.bath.sumoVehicles;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class VehicleDistance implements Comparable
{
	private String vehicleID;
	private double totalDist;
	private double nearestVehAheadDist = -1d;

	public VehicleDistance(String id, double dist)
	{
		vehicleID=id;
		totalDist=dist;
	}

    	@Override
    	public int compareTo(Object o) 
	{
        	VehicleDistance f = (VehicleDistance)o;

        	if (totalDist > f.totalDist) 
		{
            		return 1;
        	}
        	else if (totalDist <  f.totalDist) {
            		return -1;
        	}
        	else {
            	return 0;
        	}
    	}

	public void setDistanceAheadVeh(Double val)
	{
		nearestVehAheadDist = val;
	}
		
	public Double getAheadVehicleDist()
	{
		return nearestVehAheadDist;
	}

	public String getID()
	{
		return vehicleID;
	}

	public void setID(String newID)
	{
		vehicleID=newID;
	}

	public Double getDist()
	{
		return totalDist;
	}

	public void setDist(Double dist)
	{
		totalDist=dist;
	}
}
