package router;

import math.geom2d.*;
import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;

import org.openstreetmap.travelingsalesman.*;

import java.io.*;



public class calculateRoute extends DefaultInternalAction {

    @Override
	
	

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		//These values are pretty critical, conversion of lat/long where we are centered the OSM map data, into USM, and then using these as 
		//the effective 0,0 position for all coordinate points.
		double x0 = 544482;
		double y0 = 5692044;
	
        // execute the internal action
		try{
		System.out.println("will calculate route");
		
		NumberTerm fromLat = (NumberTerm)args[0];
		NumberTerm fromLong = (NumberTerm)args[1];
		NumberTerm toLat = (NumberTerm)args[2];
		NumberTerm toLong = (NumberTerm)args[3];

		//return Math.atan(number);
		double startLat = fromLat.solve();
		double startLong = fromLong.solve();
		double finLat = toLat.solve();
		double finLong = toLong.solve();

		//PrintStream realSystemOut = System.out;
		//File file  = new File("/tmp/routeout.log");
		//PrintStream printStream = new PrintStream(new FileOutputStream(file));
		
		System.out.println("From " + startLat + "," + startLong + " to " + finLat + "," + finLong);
		
		
		String[] params = new String[2];
		
		params[0] = new String("["+startLat+","+startLong+"]");
		params[1] = new String("["+finLat+","+finLong+"]");

		//System.setOut(printStream);
		String[] coordResults = org.openstreetmap.travelingsalesman.Main.handleDirectRoute(params);
		//System.setOut(realSystemOut);

		System.out.println("Got " + coordResults.length + "results");
		for(int coordP=0;coordP < coordResults.length;coordP++)
		{
			String[] sepCoords = coordResults[coordP].split(",");
			Double xPos = Double.parseDouble(sepCoords[0]);
			Double yPos = Double.parseDouble(sepCoords[1]);
			
			String resultWaypointBelief= (coordP +","+(xPos-x0)+","+(yPos-y0));
			System.out.println(resultWaypointBelief);
			Literal newLit= Literal.parseLiteral("wayPoint("+resultWaypointBelief+")");
			ts.getAg().addBel(newLit);
		}
		
		
		//Could do some return of success/fail condition I guess..
		NumberTerm newone = new NumberTermImpl(1);
		
		return un.unifies(newone,args[4]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


