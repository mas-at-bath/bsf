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

public class LaneLightPair
{
	private String laneName="";
	
	private Point2D location = new Point2D.Double(0d,0d);	
	private CopyOnWriteArrayList<ExitLaneLightState> exitLanesControlled = new CopyOnWriteArrayList<ExitLaneLightState>();
	

	public LaneLightPair(String name, String position)
	{
		laneName = name;
		String[] splitCoord = position.split(",");
		double x = Double.parseDouble(splitCoord[0]);
		double y = Double.parseDouble(splitCoord[1]);
		location = new Point2D.Double(x,y);
	}

	public List<ExitLaneLightState> getExitLanes()
	{
		return exitLanesControlled;
	}

	public void addExitLane(ExitLaneLightState exitlane)
	{
		exitLanesControlled.add(exitlane);
	}
	
	public LaneLightPair(String name, Point2D position)
	{
		laneName = name;
		location = position;
	}

	public String getName()
	{
		return laneName;
	}

	public Point2D getPosition()
	{
		return location;
	}
}
