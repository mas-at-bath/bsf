// Internal action code for project baianoTeam.mas2j



package geom;



import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;

import math.geom2d.*;



public class convert extends DefaultInternalAction {



    @Override

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action
		try{
       		//ts.getAg().getLogger().info("executing internal action 'geom.convert'");
		
		NumberTerm r1 = (NumberTerm)args[0];
		double rho = r1.solve();
		NumberTerm time = (NumberTerm)args[1];
		double timeValue = time.solve();
		NumberTerm t1 = (NumberTerm)args[2];
		double theta = t1.solve();

		NumberTerm x1 = (NumberTerm)args[3];
		double startX = (x1.solve());
		NumberTerm y1 = (NumberTerm)args[4];
		double startY = y1.solve();
		NumberTerm z1 = (NumberTerm)args[5];
		double startZ = z1.solve();

		//time passed in is seconds, rho passed in is metres per second, so multiply up the two
		double distanceCovered=rho*timeValue;
		//vehicle length is 6m, so lets offset the start position by 3m
		///double bonnetOffset = 3;

		Point2D startPosition = new Point2D(startX,startZ);
		///Point2D frontBonnetPosition = startPosition.createPolar(startX, startZ, bonnetOffset, theta);
		
		//Point2D endLocation = frontBonnetPosition.createPolar(frontBonnetPosition.x, frontBonnetPosition.y, distanceCovered, theta); 
		
		
		Point2D endLocation = startPosition.createPolar(startPosition.x, startPosition.y, distanceCovered, Math.toRadians(theta)); 
				

		NumberTerm finNewX = new NumberTermImpl(endLocation.x);
		//ASSUMPTION that we're not moving in the Y, major fudge really but i dont think we get orientation in the Y axis
		NumberTerm finNewY = new NumberTermImpl(startY);
		NumberTerm finNewZ = new NumberTermImpl(endLocation.y);
		un.unifies(finNewX,args[6]);
		un.unifies(finNewY,args[7]);
		return un.unifies(finNewZ,args[8]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


