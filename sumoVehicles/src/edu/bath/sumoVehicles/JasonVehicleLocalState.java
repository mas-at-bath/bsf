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

public class JasonVehicleLocalState {

	private String vehicleName="";
	private boolean isParked=false;		
	private int currentLane=0;
	private String currentEdge="";
	private Double currentSpeed=0.0d;
	private double currentPos=0.0d;
	
	public JasonVehicleLocalState(String vehName)
	{
		vehicleName=vehName;
	}

	public void setSpeed(Double newSpeed)
	{
		//System.out.println("set new speed to " + newSpeed);
		currentSpeed=newSpeed;
	}

	public Double getSpeed()
	{
		return currentSpeed;
	}
	
	public double getPosition()
	{
		return currentPos;
	}

	public void setPosition(double newPos)
	{
		currentPos=newPos;
	}

	public void setParked(boolean state)
	{
		isParked=state;
	}
		
	public boolean getIsParkedState()
	{
		return isParked;
	}
		
	public String getName()
	{
		return vehicleName;
	}

	public void setCurrentLane(int newLane)
	{
		currentLane=newLane;
	}

	public int getCurrentLane()
	{
		return currentLane;
	}

	public void setCurrentEdge(String newEdge)
	{
		currentEdge=newEdge;
	}

	public String getCurrentEdge()
	{
		return currentEdge;
	}
}
