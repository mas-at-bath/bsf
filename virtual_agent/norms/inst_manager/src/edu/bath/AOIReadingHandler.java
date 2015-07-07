/*
 * InstManager 
 * - Main instance of the institution manager. Once receive the percepts from agents,
 * 	then convert them into exogenous events that are acceptable to institutions. Finally,
 * 	these events are delivered to the instance of institutions. Also, it is able to receive 
 *  the norms from institutions and send them to agents for the deliberation inside agents.
 * 
 * 		@author		JeeHang
 * 		@date		29 Mar 2012
 * 
 * (+) Adding multiple institution (Aug 2013, JeeHang Lee) 
 */

package edu.bath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;

import edu.bath.institution.*;
import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.client.ReadingHandler;


public class AOIReadingHandler implements ReadingHandler 
{

	private long startupTime=0L;
	private long startupDelay=10000L;
	private InstManager parentInstManager;

	public void init(InstManager parent)
	{
		parentInstManager=parent;
	}


	@Override
	public void handleIncomingReading(String node, String rdf) 
	{

				//System.out.println("AOIReadingHandler got a message..");
				try 
				{
					DataReading dr = DataReading.fromRDF(rdf);
					if ((startupTime + startupDelay) < System.currentTimeMillis())
					{
					String takenBy = dr.getTakenBy();
					DataReading.Value aoiLight = dr.findFirstValue(null, "http://127.0.0.1/AOISensors/upcomingLight", null);
					if(aoiLight != null)
					{
						String tempAOIval = (String)aoiLight.object;
						System.out.println("told about an AOI light: " + tempAOIval);
						//split out the info, if distance certain threshold then generate well formed event for institution
						String resPair[] = tempAOIval.split(",");
						Double distance = Double.parseDouble(resPair[0]);
						String state = resPair[1];
						String[] takenBySplit = takenBy.split("/");
						String byName = takenBySplit[takenBySplit.length-1];
						System.out.println("was taken by " + byName);
						if (((distance > 100) && (distance < 300)) && ((state.equals("r") || (state.equals("R")))))
						{
							System.out.println("traffic light detected within suitable distance and currently red!");
						}
						//TODO!! Move this into if statement above, testing
						System.out.println("WARNING:: move this!!");
						parentInstManager.addMsg("upcomingRedLight("+byName+")");
						
					}

					/*else
					{
						DataReading.Value unknownrd = dr.findFirstValue(null, null, null);
						System.out.println("didn't handle this: " + (String)unknownrd.object);
					}*/
					}
					else
					{
						System.out.println("discarded msg");
					}

				}
				catch (Exception e)
				{
					System.out.println("Error adding new message to queue..");
					e.printStackTrace();
				}
				
			}
	}
	

