package router;

import math.geom2d.*;
import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import org.openstreetmap.travelingsalesman.*;
import java.io.*;
import java.util.*; 

public class loadM25Route extends DefaultInternalAction {

    @Override
	
	

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		//These values are pretty critical, conversion of lat/long where we are centered the OSM map data, into USM, and then using these as 
		//the effective 0,0 position for all coordinate points.
		double x0 = 672807.21;
		double y0 = 5683536.37;
		
		Point2D[] points = new Point2D[6];
		ArrayList<Point2D> journeyPoints = new ArrayList<Point2D>();
	
        // execute the internal action
		try{
		System.out.println("will load m25 route");	
	
		FileReader fr = new FileReader("m25route.txt");
		BufferedReader br = new BufferedReader(fr);
		String readPosition;
		while((readPosition = br.readLine()) != null) 
		{
			String[] sepCoords = readPosition.split(",");
			Double xPos = Double.parseDouble(sepCoords[0]);
			Double yPos = Double.parseDouble(sepCoords[1]);
			journeyPoints.add(new Point2D(yPos-y0,xPos-x0));
			//System.out.println(readPosition);
		}
		fr.close();
 	
		System.out.println("finished loading route, with " + journeyPoints.size() + " waypoints");
		
		for(int currentJourneyPoint = 0;currentJourneyPoint < journeyPoints.size();currentJourneyPoint++)
		{
				Double currentXpos = journeyPoints.get(currentJourneyPoint).x;
				Double currentYpos = journeyPoints.get(currentJourneyPoint).y;
				String resultWaypointBelief= (currentJourneyPoint +","+journeyPoints.get(currentJourneyPoint).x+","+journeyPoints.get(currentJourneyPoint).y);
				Literal newLit= Literal.parseLiteral("wayPoint("+resultWaypointBelief+")");
				ts.getAg().addBel(newLit);
		}
		
		//Could do some return of success/fail condition I guess..
		NumberTerm newone = new NumberTermImpl(1);
		
		return un.unifies(newone,args[0]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


