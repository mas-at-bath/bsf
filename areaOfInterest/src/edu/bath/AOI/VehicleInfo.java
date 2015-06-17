package edu.bath.AOI;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import javax.vecmath.*;

import java.util.*;
import java.awt.geom.Point2D;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class VehicleInfo
{
	private String vehicleName;
	private CopyOnWriteArrayList<String> currentRoute = new CopyOnWriteArrayList<String>();	
	private CopyOnWriteArrayList<String> laneSignalsOnRoute = new CopyOnWriteArrayList<String>();
	private CopyOnWriteArrayList<LaneLightPair> upcomingTrafficLightsOnRoute = new CopyOnWriteArrayList<LaneLightPair>();
	private Point2D position = new Point2D.Double(0d,0d);
	private Double orientation = 0d;	
	private Double AOIRadius = 10.0d;
	private Double minAOIRadius = 10.0d;
	private Double currentSpeed = 0d;
	//if speed is metres per second, then how many seconds do we want to reach out our radius by.. 15 seems fair
	private Double speedToAOIFactor = 20d;
	private String edgeLane = "";
	private String edge = "";
	private int lane =0;
	private Double routePos = 0d;

	public VehicleInfo(String vehName)
	{
		vehicleName = vehName;
	}

	public String getCurrentEdge()
	{
		return edge;
	}

	//this method has problems, i think when called and the vehicle is stuck in a junction so picks up an internal lane as its current edge
	/*public String getNextRouteSection()
	{
		String nextRouteSection = "";
		boolean foundMatch = false;
		for (int i=0; i< currentRoute.size(); i++)
		{
			//System.out.println("testing if " + currentRoute.get(i) + " equals " + edge);
			if (currentRoute.get(i).equals(edge))
			{
				nextRouteSection = currentRoute.get(i+1);
				foundMatch = true;
				System.out.println("found a match, setting next route segment equals to " + currentRoute.get(i+1));
			}
		}
		if (nextRouteSection.equals("") || !foundMatch)
		{
			System.out.println("something gone wrong finding next part of this vehicles route, im at " + edge);
			System.out.println("foundMatch for that is: " + foundMatch);

		}
		return nextRouteSection;
	}*/

	public String getNextRouteSectionAfter(String testSection)
	{
		String nextRouteSection = "";
		boolean foundMatch = false;
		//check if been passed a edge_lane value rather than just edge, if so then split out just the edge
		if (testSection.contains("_"))
		{
			String splitInfo[] = testSection.split("_");
			testSection=splitInfo[0];
		}
		for (int i=0; i< currentRoute.size(); i++)
		{
			//System.out.println("testing if " + currentRoute.get(i) + " equals " + testSection);
			if (currentRoute.get(i).equals(testSection))
			{
				nextRouteSection = currentRoute.get(i+1);
				foundMatch = true;
				//System.out.println("found a match, setting next route segment equals to " + currentRoute.get(i+1));
			}
		}
		if (nextRouteSection.equals("") || !foundMatch)
		{
			System.out.println("something gone wrong finding next part of this vehicles route, based on " + testSection);
			System.out.println("foundMatch for that is: " + foundMatch);

		}
		return nextRouteSection;
	}

	public void updateSpeed(Double newSpeed)
	{
		currentSpeed = newSpeed;
		double tempAOIRadius = currentSpeed*speedToAOIFactor;
		if (tempAOIRadius > minAOIRadius)
		{
			AOIRadius = tempAOIRadius;
		}
		else
		{
			AOIRadius = minAOIRadius;
		}
		//System.out.println("have updated AOI radius to " + AOIRadius);
	}

	public void updateEdgeLane(String newEdgeLane)
	{
		if (!newEdgeLane.equals(edgeLane))
		{
			//if the edge we've just left contained a traffic light in upcomingTrafficLightsOnRoute then we can remove it now
			int posFound=-1;
			for (int i=0; i<upcomingTrafficLightsOnRoute.size(); i++)
			{
				LaneLightPair testllp = upcomingTrafficLightsOnRoute.get(i);
				if (testllp.getName().equals(edgeLane))
				{
					posFound = i;
				}
			}
			if (posFound > 0)
			{
				System.out.println("removing " + upcomingTrafficLightsOnRoute.get(posFound).getName() + " from upcoming lights");
				upcomingTrafficLightsOnRoute.remove(posFound);
			}
		}
		//System.out.println(edgeLane);
		edgeLane = newEdgeLane;
		String[] splitInfo = edgeLane.split("_");
		//System.out.println("0: " +  splitInfo[0] + " 1: " +  splitInfo[1]);
		edge = splitInfo[0];
		lane = Integer.parseInt(splitInfo[1]);

	
	}

	public int getLane()
	{
		return lane;
	}

	public Double getAOIRadius()
	{
		return AOIRadius;
	}

	public Point2D getPosition()
	{
		return position;
	}

	public void updatePosition(Point2D newpos)
	{
		position=newpos;
	}

	public void updateOrientation(Double newO)
	{
		orientation = newO;
	}

	public void updateRoutePos(Double newP)
	{
		routePos = newP;
	}

	public void newRoute(String routeInfo)
	{
		currentRoute.clear();
		String[] csvRoute = routeInfo.split(",");
		int num=0;
		for (String routePiece : csvRoute)
		{	
			System.out.println("adding " + num + " as : " + routePiece);
			num++;
			currentRoute.add(routePiece);
		}
		System.out.println(vehicleName + " route has been updated and contains " + currentRoute.size() + " elements");
	}

	public Double getRoutePos()
	{
		return routePos;
	}

	public List<String> getRoute()
	{
		return currentRoute;
	}
		
	public String getName()
	{
		return vehicleName;
	}

	public void clearLaneSignals()
	{
		laneSignalsOnRoute.clear();
	}

	public void addLaneSignalControl(String laneName)
	{
		laneSignalsOnRoute.add(laneName);
		System.out.println("now contains : " + laneSignalsOnRoute.size());
	}

	public List<String> getControlledLanes()
	{
		return laneSignalsOnRoute;
	}

	public List<LaneLightPair> getUpcomingLights()
	{
		return upcomingTrafficLightsOnRoute;
	}

	public void clearUpcomingLights()
	{
		upcomingTrafficLightsOnRoute.clear();
	}	

	public void addUpcomingLight(LaneLightPair newLight)
	{
		System.out.println("added an upcoming traffic light on route of " + vehicleName + ", now there are " + upcomingTrafficLightsOnRoute.size());
		upcomingTrafficLightsOnRoute.add(newLight);
	}
}
