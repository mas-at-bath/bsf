package edu.bath.sumoVehicles;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class VehicleAdd
{
	private String myID;
	private VehicleType myVType;
	private Route myRoute;
	private Integer myLane;
	private Double myLanePos;
	private Double mySpeed;

	public VehicleAdd(String id, VehicleType vType, Route route, int lane, double lanePos, double speed)
	{
		myID=id;
		myVType=vType;
		myRoute=route;
		myLane=lane;
		myLanePos=lanePos;
		mySpeed=speed;
	}
		
	public String getID()
	{
		if (myID == null) { System.out.println("WARN: myID null");}
		return myID;
	}

	public VehicleType getVType()
	{
		if (myVType == null) { System.out.println("WARN: myVType null");}
		return myVType; 
	}
	
	public Route getRoute()
	{
		if (myRoute == null) { System.out.println("WARN: myRoute null");}
		return myRoute;
	}

	public int getLane()
	{
		if (myLane == null) { System.out.println("WARN: myLane null");}
		return myLane;
	}

	public double getLanePos()
	{
		if (myLanePos == null) { System.out.println("WARN: myLanePos null");}
		return myLanePos;
	}

	public double getSpeed()
	{
		if (mySpeed == null) { System.out.println("WARN: mySpeed null");}
		return mySpeed;
	}
}
