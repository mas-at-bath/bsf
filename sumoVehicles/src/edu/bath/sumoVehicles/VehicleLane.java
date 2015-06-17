package edu.bath.sumoVehicles;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class VehicleLane
{
	private String myID;
	private String myEdge;

	public VehicleLane(String id, String edg)
	{
		myID=id;
		myEdge=edg;
	}
		
	public String getID()
	{
		return myID;
	}

	public String getEdge()
	{
		return myEdge;
	}

}
