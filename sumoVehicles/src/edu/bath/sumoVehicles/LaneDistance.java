package edu.bath.sumoVehicles;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class LaneDistance
{
	public String laneID;
	public double totalDist;

	public LaneDistance(String id, double dist)
	{
		laneID=id;
		totalDist=dist;
	}
		
}
