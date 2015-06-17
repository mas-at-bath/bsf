package edu.bath.sumoVehicles;

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
//import math.geom2d.*;
import java.io.BufferedReader;
import java.io.FileReader;

import it.polito.appeal.traci.*;
import it.polito.appeal.traci.protocol.*;

public class StopPositionPair
{
	private RoadmapPosition stopRoadPos ;
	private String vehicleName;
	
	public StopPositionPair(String vehName, RoadmapPosition roadPos)
	{
		vehicleName = vehName;
		stopRoadPos = roadPos;
	}
		
	public String getStopVehicleName()
	{
		return vehicleName;
	}
		
	public RoadmapPosition getStopVehicleRoadpos()
	{
		return stopRoadPos;
	}
}
