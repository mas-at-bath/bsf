package router;

import math.geom2d.*;
import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import org.openstreetmap.travelingsalesman.*;
import java.io.*;
import java.util.*; 

public class calculateBathRoute extends DefaultInternalAction {

    @Override
	
	

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		//These values are pretty critical, conversion of lat/long where we are centered the OSM map data, into USM, and then using these as 
		//the effective 0,0 position for all coordinate points.
		double x0 = 544482;
		double y0 = 5692044;
		
		Point2D[] points = new Point2D[6];
		ArrayList<Point2D> journeyPoints = new ArrayList<Point2D>();
	
        // execute the internal action
		try{
		System.out.println("will calculate route");
		
		points[0] = new Point2D(51.37789147,-2.36072481);
		//System.out.println("made first point");
	//	points[1] = new Point2D(51.3786847,-2.3634338);
		points[1] = new Point2D(51.38396844,-2.37120152);
		points[2] = new Point2D(51.38736981,-2.36163139);
		points[3] = new Point2D(51.38691452,-2.35931396);
		//points[1] = new Point2D(51.382492,-2.364936);
	//	points[3] = new Point2D(51.385157 , -2.360709);

		//points[4] = new Point2D(51.389161,-2.358949);
		points[4] = new Point2D(51.377925,-2.356975);
		points[5] = new Point2D(51.377865,-2.360299);

		//next set would be 51.385157 , -2.360709 
		
		System.out.println("point array length is " + points.length);
		FileWriter fw = new FileWriter("positions.txt",true);
		
		for (int pointPos=1; pointPos < points.length; pointPos++)
		{
			System.out.println("In loop for count " + pointPos);
			
			String[] params = new String[2];
			params[0] = new String("["+points[pointPos-1].x+","+points[pointPos-1].y+"]");
			params[1] = new String("["+points[pointPos].x+","+points[pointPos-0].y+"]");

			String[] coordResults = org.openstreetmap.travelingsalesman.Main.handleDirectRoute(params);
		
			System.out.println("Got " + coordResults.length + "results");
			for(int coordP=0;coordP < coordResults.length;coordP++)
			{
				String[] sepCoords = coordResults[coordP].split(",");
				Double xPos = Double.parseDouble(sepCoords[0]);
				Double yPos = Double.parseDouble(sepCoords[1]);
				journeyPoints.add(new Point2D(yPos-y0,xPos-x0));
				fw.write(xPos+" , " +yPos+" \n");
			}	
		}
		
		
		System.out.println("finished route calculation, with " + journeyPoints.size() + " points calculated");
		
		for(int currentJourneyPoint = 0;currentJourneyPoint < journeyPoints.size();currentJourneyPoint++)
		{
				Double currentXpos = journeyPoints.get(currentJourneyPoint).x;
				Double currentYpos = journeyPoints.get(currentJourneyPoint).y;
				String resultWaypointBelief= (currentJourneyPoint +","+journeyPoints.get(currentJourneyPoint).x+","+journeyPoints.get(currentJourneyPoint).y);
				//String resultWaypointBelief= (currentJourneyPoint +","+(currentXpos-x0)+","+(currentYpos-y0));
				//fw.write(resultWaypointBelief+" \n");
				//System.out.println(resultWaypointBelief);
				Literal newLit= Literal.parseLiteral("wayPoint("+resultWaypointBelief+")");
				ts.getAg().addBel(newLit);
		}
		fw.close();
		

		
		//Could do some return of success/fail condition I guess..
		NumberTerm newone = new NumberTermImpl(1);
		
		return un.unifies(newone,args[0]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


