import jason.*;

import jason.asSemantics.*;

import jason.asSyntax.*;

import math.geom2d.*;

/*import com.jme.scene.Node;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.bounding.*;
import com.jme.scene.shape.*;
import com.jme.scene.Geometry;*/

public class milliTime extends DefaultInternalAction {

//	protected Node collisionSpaceNode;
//	protected Box theBox;

    @Override

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action
		try{
       		//ts.getAg().getLogger().info("executing internal action 'geom.convert'");
		
		long timeVal = System.currentTimeMillis();
		NumberTerm collisionResult = new NumberTermImpl(timeVal);

		return un.unifies(collisionResult,args[0]);
		}
		catch (Exception e){
			throw new JasonException("Error");
		}
    }

}


