package router;

import math.geom2d.*;
import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;

import org.openstreetmap.travelingsalesman.*;




public class importMap extends DefaultInternalAction {

    @Override

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action
		try{
		String mapName = new String(args[0]+".osm");
		System.out.println("will set route calculation map database using " + mapName);


		org.openstreetmap.travelingsalesman.Main.handleDirectImport(mapName);
		
		NumberTerm newone = new NumberTermImpl(1);

		return un.unifies(newone,args[1]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


