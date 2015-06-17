package edu.bath.sumoVehicles;

import java.util.*;

public class VehicleDistancesPerLane
{
	private int laneNumber;
	private ArrayList<VehicleDistance> vehicleDistances = new ArrayList<VehicleDistance>();

	public VehicleDistancesPerLane(int lane)
	{
		laneNumber = lane;;
	}

	public int getLaneVal()
	{
		return laneNumber;
	}

	public void clearDistances()
	{
		vehicleDistances.clear();
	}
		
	public List<VehicleDistance> getDistances()
	{
		return vehicleDistances;
	}

	public void addVehicleDistance(VehicleDistance newVD)
	{
		vehicleDistances.add(newVD);
	}

	public void sortAlongRoute()
	{
		Collections.sort(vehicleDistances);
	}

	public void populateGaps()
	{
		for (VehicleDistance vD : vehicleDistances)
		{
			Double nearestDist = -1d;
			String nameAhead = "";
			for (VehicleDistance checkVD : vehicleDistances)
			{
				Double distAhead = checkVD.getDist() - vD.getDist();
				if (nearestDist == -1 && distAhead > 0)
				{
					nearestDist = distAhead;
					nameAhead = checkVD.getID();
				}
				else if ((nearestDist > 0) && (distAhead < nearestDist) && (distAhead > 0))
				{
					nearestDist = distAhead;
					nameAhead = checkVD.getID();
				}
			}
			if (nearestDist > 0)
			{
				vD.setDistanceAheadVeh(nearestDist);
			}
			//System.out.println("for " + vD.vehicleID + " nearest vehicle ahead is " + nameAhead + " by " + nearestDist);
		}
	}
}
