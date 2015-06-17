// Internal action code for project baianoTeam.mas2j



package test;


import math.geom2d.*;
import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;



public class test1 extends DefaultInternalAction {



    @Override

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action
		try{
       // ts.getAg().getLogger().info("executing internal action 'test.test1'");
		NumberTerm currentX = (NumberTerm)args[0];
		NumberTerm destinationX = (NumberTerm)args[1];
		NumberTerm currentZ = (NumberTerm)args[2];
		NumberTerm destinationZ = (NumberTerm)args[3];
		NumberTerm currentAngle = (NumberTerm)args[4];
		
		//return Math.atan(number);
		double myX = currentX.solve();
		double toX = destinationX.solve();
		double myZ = currentZ.solve();
		double toZ = destinationZ.solve();
		
		double dx = Math.abs(myX-toX);
		double dZ = Math.abs(myZ-toZ);
		
		double baseangle = Math.toDegrees(Math.atan(dZ/dx));	
		double resultangle = 0;
		double toZmyZ = toZ-myZ;
		double toXmyX = toX-myX;
		
		Point2D startPoint = new Point2D(myX,myZ);
		Point2D endPoint = new Point2D(toX,toZ);
		
		/*System.out.println("at: " + startPoint.toString());
		System.out.println("to: " + endPoint.toString());
		System.out.println("current heading: " + currentAngle);
		System.out.println("get hoz angle: " + Angle2D.getHorizontalAngle(startPoint,endPoint));
		System.out.println("get psuedo angle: " + Angle2D.getPseudoAngle(startPoint,endPoint));*/
		
		/*//work out angles depending on quadrant.. easier than polar
		if (toZmyZ > 0) //we're moving 'forward' i.e top left or right quad
		{
			if ((toX-myX) > 0) //top right quad
				{resultangle = baseangle;}
			else //negative X so top left quad
				{resultangle = baseangle+90;}			
				
		}			
		else
		{
			if ((toX-myX) > 0) //bottom right quad
				{resultangle = 360 - baseangle;}
			else
				{resultangle = (90-baseangle)+180;}
		}*/
		
		//grr so even the 'proper' library doesn't seem to be giving me correct angles if i've got negative positions	
		//NumberTerm newone = new NumberTermImpl(Angle2D.getPseudoAngle(startPoint,endPoint));

		//so lets try a 3rd way
		NumberTerm newone = new NumberTermImpl(180+(Math.toDegrees(Math.atan2(myZ-toZ, myX-toX))));

		return un.unifies(newone,args[5]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


