package CustomXMPP;

import jason.asSyntax.*;
import jason.architecture.*;
import java.util.logging.*;
import java.util.*;
import jason.asSemantics.*;
import jason.RevisionFailedException;
import jason.infra.centralised.RunCentralisedMAS;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.XMPPException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.CopyOnWriteArrayList;

import java.awt.geom.Rectangle2D;
import com.jme3.scene.shape.Box;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.bounding.*;
import com.jme3.scene.Geometry;

import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.JsonReading;
import edu.bath.sensorframework.JsonReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;

public class VehicleArch extends AgArch {

	private Map<String, Method> annotatedMethods;
	private ArrayList<String> receivedData;
	private WorkerSender mySender;
	private WorkerJStateSender myJStateSender;
	private static String XMPPServer = "127.0.0.1";
	private Box newBox = new Box(new Vector3f(0.f,0.f,0.f), 0,1,1.5f);
	private BoundingVolume newBV;
	private Vector3f myStart,myFin, myCent,zeroVec, myLoc;
	private Double myAngle = 0d;
 	private Float myDistance = 0f;
	private Quaternion myRotAngle;
	private int lastDirection = 0;
	private boolean jStateInfoOn = true;
	//private Literal clearLit = Literal.parseLiteral("info(_,_,_,_,_,_,_)");
	private static String jasonSensorVehicles = "jasonSensorVehicles";
    	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private int mc = 0, pc = 0;
	private WorkerInstSender instSensor;
	private String myName = "";
	private String instInitialVals = "";
	private CopyOnWriteArrayList<String> obstaclesReactedToInVol = new CopyOnWriteArrayList<String>();
	private CopyOnWriteArrayList<KnownCollisionNameLoc> knownCollisonPairs = new CopyOnWriteArrayList<KnownCollisionNameLoc>();
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;

	@Override 
	public void init() 
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));
			String line;
			while((line = br.readLine()) != null) 
			{
				if (line.contains("OPENFIRE"))
				{
					String[] configArray = line.split("=");
					XMPPServer = configArray[1];
					//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
				if (line.contains("COMMUNICATION"))
				{
					String[] configArray = line.split("=");
					if(configArray[1].equals("MQTT"))
					{
						useMQTT=true;
					}
					else if(configArray[1].equals("XMPP"))
					{
						useXMPP=true;
					}
					//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
			}
			if (!useMQTT && !useXMPP)
			{
				System.out.println("no COMMUNICATION value found in config.txt, should be = MQTT or XMPP");
				System.exit(1);
			}
		}
		catch (Exception e) {
			System.out.println("error with config file..");
			e.printStackTrace();
		}
		
		annotatedMethods = new HashMap<String, Method>();

          	myStart = new Vector3f(0f,0f,0f);
          	myFin = new Vector3f(0f,0f,0f); 
          	myCent = new Vector3f(0f,0f,0f);	
        	zeroVec = new Vector3f(0.f,0.f,0.f);
        	newBox = new Box(new Vector3f(0.f,0.f,0.f), 0,1,1.5f);
       	 	myRotAngle = new Quaternion();
        	myRotAngle.fromAngles(0, myAngle.floatValue(), 0);
		
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (Method method : allMethods) {
			if (method.isAnnotationPresent(Action.class)) {
				String actionName = method.getAnnotation(Action.class).value();
				if (actionName.equals("")) {
					actionName = method.getName();
				}
				annotatedMethods.put(actionName, method);
			}
		}
		myName = getAgName();
		receivedData = new ArrayList<String>();

		//XMPP for sending requests.. I think thats OK to not pass thru env for moment
		
		try
		{
			String myConnectionLogin=getAgName()+"-sender";
			String myURI="http://127.0.0.1/agent/"+getAgName();
			if (useXMPP)
			{
				mySender = new WorkerSender(XMPPServer, myConnectionLogin, "jasonpassword", jasonSensorVehiclesCmds, "http://127.0.0.1/agent", myURI);
				instSensor = new WorkerInstSender(XMPPServer, getAgName()+"-inst-sender", "jasonpassword", "NODE_PERCEPT");
			}
			else if (useMQTT)
			{
				mySender = new WorkerSender(XMPPServer, myConnectionLogin, "jasonpassword", jasonSensorVehiclesCmds, "http://127.0.0.1/agent", myURI, true, 0);
				instSensor = new WorkerInstSender(XMPPServer, getAgName()+"-inst-sender", "jasonpassword", "NODE_PERCEPT", true, 0);
			}

		}
		catch (XMPPException err)
		{
			System.out.println("Couldnt start XMPP send thread for "+getAgName()+"-inst-sender");
			err.printStackTrace();
		}
		
		if (jStateInfoOn)
		{
			try
			{
				String myConnectionLogin=getAgName()+"-JState";
				//System.out.println("connecting as " + myConnectionLogin);
				String myURI="http://127.0.0.1/agentJState/"+getAgName();
				if (useXMPP)
				{
					myJStateSender = new WorkerJStateSender(XMPPServer, myConnectionLogin, "jasonpassword", jasonSensorStates, "http://127.0.0.1/agentJState", myURI, getAgName(), getTS().getAg().getPL());
				}
				else if (useMQTT)
				{
					myJStateSender = new WorkerJStateSender(XMPPServer, myConnectionLogin, "jasonpassword", jasonSensorStates, "http://127.0.0.1/agentJState", myURI, getAgName(), getTS().getAg().getPL(), true, 0);
				}
			}
			catch (XMPPException err)
			{
				System.out.println("Couldnt start XMPP JState send thread for"+getAgName()+"-JState");
				err.printStackTrace();
			}
		}
	}
	
	@Action
	public boolean brake(Term[] terms) {
		//int tempValue = getParamAsInt(0, terms);
		//String stValue = Integer.toString(tempValue);
		//mySender.addMessageToSend("applyBrakes",stValue);
		//mySender.send();
		//System.out.println("brakes are not currently implemented in VehicleArch");
		return true;
	}

	@Action
	public boolean setSUMOroute(Term[] terms) {
		//String vehicleName = terms[0] + "";
		String routeName = terms[0] + "";
		mySender.addAndSendTestMsg("setRoute",routeName);
		return true;
	}
	
	@Action
	public boolean setSpeed(Term[] terms) {
		int tempValue = getParamAsInt(0, terms);
		String stValue = Integer.toString(tempValue);
		//System.out.println("called original setSpeed to " + stValue);
		mySender.addAndSendTestMsg("setSpeed",stValue);
		return true;
	}

	@Action
	public boolean setSpeedOverTime(Term[] terms) {
		int tempValue = getParamAsInt(0, terms);
		String stValue = Integer.toString(tempValue);
		System.out.println("setting speed to change over interval to " + stValue);
		mySender.addAndSendTestMsg("setSpeedOverTime",stValue);
		return true;
	}
	
	@Action
	public boolean setAutonomy(Term[] terms) {
		int setAValue = getParamAsInt(0, terms);
		String stValue = Integer.toString(setAValue);
		mySender.addAndSendTestMsg("setAutonomy",stValue);
		return true;
	}

	@Action
	public boolean setLaneChange(Term[] terms) {
		int setAValue = getParamAsInt(0, terms);
		String stValue = Integer.toString(setAValue);
		mySender.addAndSendTestMsg("setLaneChange",stValue);
		return true;
	}


	@Action
	public boolean quickLaneChange(Term[] terms) {
		//just passing dummy 0 for the moment
		int setAValue = 0;
		String stValue = Integer.toString(setAValue);
		mySender.addAndSendTestMsg("quickLaneChange",stValue);
		return true;
	}
	
	@Action
	public boolean resume(Term[] terms) {
		mySender.addAndSendTestMsg("resume","");
		return true;
	}
	
	@Action
	public boolean moveTo(Term[] terms) {
		double tempX = getParamAsDouble(0, terms);
		double tempY = getParamAsDouble(1, terms);
		String stValue = Double.toString(tempX) + "," + Double.toString(tempY);
		mySender.addAndSendTestMsg("moveTo",stValue);;
		return true;
	}
	
	@Action
	public boolean addSUMOVehicle(Term[] terms) {
		String vehicleName = terms[0] + "";
		System.out.println("addSUMO has : " + terms.length);
		if (terms.length == 2)
		{
			String routeName = terms[1] + "";
			System.out.println("adding vehicle with non default route: " + routeName);
			mySender.addAndSendTestMsg("addVehicle", vehicleName + "," + routeName);
		}
		else if (terms.length == 3)
		{
			String routeName = terms[1] + "";
			int laneNumber = getParamAsInt(2, terms);
			System.out.println("adding vehicle with non default route: " + routeName + " and starting in lane " + laneNumber);
			mySender.addAndSendTestMsg("addVehicle", vehicleName + "," + routeName + "," + laneNumber);
		}
		else if (terms.length == 4)
		{
			String routeName = terms[1] + "";
			int laneNumber = getParamAsInt(2, terms);
			int speedVal = getParamAsInt(3, terms);
			System.out.println("adding vehicle with non default route: " + routeName + " and starting in lane " + laneNumber + " and speed " + speedVal);
			mySender.addAndSendTestMsg("addVehicle", vehicleName + "," + routeName + "," + laneNumber + "," + speedVal);
		}
		else if (terms.length == 5)
		{
			String routeName = terms[1] + "";
			int laneNumber = getParamAsInt(2, terms);
			int speedVal = getParamAsInt(3, terms);
			int departTimeVal = getParamAsInt(4, terms);
			System.out.println("adding vehicle with non default route: " + routeName + " and starting in lane " + laneNumber + " and speed " + speedVal + " and depart time of " + departTimeVal);
			mySender.addAndSendTestMsg("addVehicle", vehicleName + "," + routeName + "," + laneNumber + "," + speedVal + "," + departTimeVal);
		}
		else
		{
			mySender.addAndSendTestMsg("addVehicle", vehicleName);
		}
		return true;
	}

	@Action
	public boolean setLights(Term[] terms) {
		String lightState = terms[0] + "";
		mySender.addAndSendTestMsg("setFrontLights", lightState);;
		return true;
	}
	
	@Action
	public boolean setOrientation(Term[] terms) {
		int tempValue = getParamAsInt(0, terms);
			
		String stValue = Integer.toString(tempValue);
		System.out.println("last direction is " + lastDirection +", checking if " + (tempValue+40) + " is less than it, or " + (tempValue-40) + " + is greater than it");
		if ((tempValue+40 < lastDirection) || (tempValue-40 > lastDirection))
		{
			System.out.println("course change of more than 40 degrees requested");
		}
		lastDirection = tempValue;
		mySender.addAndSendTestMsg("setOrientation",stValue);
		return true;
	}
	
	//duplicate method to above for compatibility with older agents from tankcoders..
	@Action
	public boolean turnToAngle(Term[] terms) {
		int tempValue = getParamAsInt(0, terms);
		String stValue = Integer.toString(tempValue);
		mySender.addAndSendTestMsg("setOrientation",stValue);
		return true;
	}
	
	@Action
	public boolean changeCollisionVolume(Term[] terms) {
		//System.out.println("Asked agArch to update collision volume");
		Float startX = (float) getParamAsDouble(0, terms);
		Float startY = (float) getParamAsDouble(1, terms);
		Float startZ = (float) getParamAsDouble(2, terms);
		Float finX = (float) getParamAsDouble(3, terms);
		Float finY = (float) getParamAsDouble(4, terms);
		Float finZ = (float) getParamAsDouble(5, terms);

            	myStart.x=startX;
            	myStart.y=startY;
            	myStart.z=startZ;
            	myFin.x=finX;
            	myFin.y=finY;
            	myFin.z=finZ;
            	myCent = myCent.interpolate(myStart,myFin, 0.5f);
		//BoundingBox myBound = new BoundingBox();
            	myDistance = myStart.distance(myFin);
		//System.out.println("length of box calculated to be " + myDistance);
            	//myAngle = -Math.atan2((myFin.z-myStart.z), (myFin.x-myStart.x));
		myAngle = -Math.atan2((myFin.x-myStart.x), (myFin.z-myStart.z));
		//System.out.println("angle calculated as " + Math.toDegrees(myAngle));
            	myRotAngle.fromAngles(0, myAngle.floatValue(), 0);
		//newBox.updateGeometry(zeroVec, myDistance,1f,1.5f);
		//System.out.println("distance length is " + myDistance);
		//newBox.updateGeometry(myCent, 1.5f,3f,myDistance);
		newBox.updateGeometry(myCent,  1.5f, 2f, myDistance);

		//System.out.println("box x size is " + 
            	Geometry newGeom = new Geometry("vehicleGeom", newBox);
		newGeom.setModelBound(new BoundingBox());
            	newGeom.setLocalRotation(myRotAngle);
		newGeom.setLocalTranslation(myCent);	
		newGeom.updateModelBound();
		newBV = newGeom.getModelBound();


		/*if (myName.equals("centralMember2"))
		{
			System.out.println(myName + "updating geometry: " + myCent.toString() + ",  1.5f, 2f, "+ myDistance );
			System.out.println(myName + "start at " + myStart.toString() + " end at " + myFin.toString());
			System.out.println(myName + "and setting localrotation " + myRotAngle);
		}*/	
		//System.out.println("center " + myCent.toString() + " in boundingvol ok? " + newBV.contains(myCent));	

		/*newBox.updateGeometry(new Vector3f(0f,0f,0f), 10f, 1f,1.5f);
		Geometry newGeom = new Geometry("vehicleGeom", newBox);
		newGeom.setModelBound(new BoundingBox());
	
		newGeom.updateModelBound();
		newBV = newGeom.getModelBound();
		Vector3f testVec = new Vector3f(5f,0f,0f);
		System.out.println("ok? " + newBV.contains(testVec));*/


		//System.out.println("bounding volume is center at " + newBV.getCenter().toString() + " compared to " + myCent.toString());
		//System.out.println("extents are: " + newGeom.getModelBound().getXExtent() + " " + newGeom.getModelBound().getYExtent() + " " + newGeom.getModelBound().getZExtent() );

		//Point2D.Double centralLocation = new Point2D.Double((startX + finX) * 0.5, (startZ + finZ) * 0.5);
		//Line2D line = new Line2D(new Point2D(startX, startZ), new Point2D(finX, finZ));
		//StraightLine2D bottomLine = line.perpendicular(startX, startZ);
		//myCollisionVolume = 
		
		//System.out.println("At: " + startX + ", " + startY + ", " + startZ );
		//this will probably be a volume calculated by the vehicle, as to what space it will occupy over next N seconds
		//in tankcoders work, each frame looked at collisions with that volume, but thats a dependency on the renderer
		//which wont always run
		//so here, we should have some knowledge of known objects (if not done yet then port over code from tankcoders)
		//assign them some nominal volume space, and check if that volume lies within our collision volume,
		//but need to normalise it centered on this vehicle at 0,0 at coord system is based there I think	
		String collisionVal = (startX+","+startY+","+startZ+","+finX+","+finY+","+finZ);
		//System.out.println("collision vol is: " + startX+","+startY+","+startZ+","+finX+","+finY+","+finZ);
		//System.out.println("sending collision volume XMPP as: " + collisionVal);
		myJStateSender.addAndSendMsg("geometry/collisionVolume", collisionVal);
		//myJStateSender.send();
		return true;
	}

	@Action
	public boolean hadToBrakeBecause(Term[] terms) 
	{
		String vehicleCausingHardBrake = terms[0] + "";
		//System.out.println("adding " + vehicleCausingHardBrake + " to check if it leaves collision volume ");
		//only add it if its not already in there
		if (!obstaclesReactedToInVol.contains(vehicleCausingHardBrake))
		{
			obstaclesReactedToInVol.add(vehicleCausingHardBrake);
		}
		return true;
	}

	//here we are being passed a vehicle name and its xy location, to check if it is within our collision volume.
	@Action 
	public boolean checkCollisionVolume(Term[] terms) 
	{
		if (newBV != null)
		{
			String vehFullName = getAtomAsString(0, terms);
			
			String[] agentArrayText= vehFullName.split("/");
			String otherVehName = agentArrayText[4];
			Float xLoc = (float) getParamAsDouble(2, terms);
			Float zLoc = (float) getParamAsDouble(1, terms);
			Vector3f otherLocVec = new Vector3f(xLoc, 0f, zLoc);

			//TODO: hack, that distance is bigger than 14
			if (newBV.contains(otherLocVec) && (myDistance >= 14 ))
			{	
				
				try {
					getTS().getAg().addBel(Literal.parseLiteral("detectionInCollisionZone("+otherVehName+","+myStart.distance(otherLocVec)+")"));
					//TODO: this should check if vehicle already in list, as it might have moved
					//KnownCollisionNameLoc newCol = new KnownCollisionNameLoc(otherVehName, otherLocVec);
					//knownCollisonPairs.add(newCol);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else if (newBV.contains(otherLocVec) && (myDistance < 14 ))
			{
				System.out.println("ignored collision volume detection due to speed, need to fix this!");	
			}

			//now check for vehicles which were in the collision volume, and caused the agent to take emergency action, are they still in AOI and we've just been told about them, but no longer in collision volume?
			boolean canDelete = false;
			for (String checkName : obstaclesReactedToInVol)
			{
				if (otherVehName.equals(checkName))
				{
					System.out.println("checking if " + checkName + " has left collision volume but still in AOI");
					if (!newBV.contains(otherLocVec))
					{
						System.out.println("its not longer in collision volume, resume normal actions");

						canDelete=true;
					}
				}
			}
			if (canDelete)
			{
				obstaclesReactedToInVol.remove(otherVehName);
				if (obstaclesReactedToInVol.size()==0)
				{
					System.out.println("nothing that has been reacted to via emergency is left in collision vol, back to normal..");
					try
					{
						getTS().getAg().addBel(Literal.parseLiteral("collisionVolClear"));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			System.out.println("bounding volume is null, cannot calculate collisions right now..");
		}
		return true;
	}

	@Action 
	public boolean updateJasonWayPoint(Term[] terms) {
		int tempValue = getParamAsInt(0, terms);
		Double newX = getParamAsDouble(1, terms);
		Double newZ = getParamAsDouble(2, terms);
		//String stValue = Integer.toString(tempValue);
		String stValue = (tempValue + "," + newX + "," + newZ);
		myJStateSender.addAndSendMsg("message/waypoint",stValue);
		//myJStateSender.send();
		return true;
	}
	
	@Action
	public boolean instInitials(Term[] terms) {
	
		instInitialVals = terms[0] + "";		
		System.out.println("set initials to " + instInitialVals +  " ahead of next inst event transmission");
		return true;
	}	

	@Action
	public boolean instEvent(Term[] terms) {
	
		String testMsg = terms[0] + "";	

		if (instInitialVals.equals(""))
		{	
			instSensor.releaseEvent(testMsg);
			System.out.println("sent " + testMsg +  " out of testInstUpdate now..");
		}
		else if (instInitialVals.length() > 1)
		{
			instSensor.releaseEventWithInitials(testMsg,instInitialVals);
			System.out.println("sent " + testMsg + " " + instInitialVals + " out of testInstUpdate now..");
			instInitialVals="";
			System.out.println("reset initial conditions to be empty again..");
		}
		return true;
	}
	
	@Action
	public boolean updateJasonVisualItem(Term[] terms) {
		//System.out.println("Asked agArch to update jason visual item");
		Double newX = getParamAsDouble(0, terms);
		Double newY = getParamAsDouble(1, terms);
		Double newZ = getParamAsDouble(2, terms);
		
		//System.out.println("Creating object at: " + newX + ", " + newY + ", " + newZ );
		String jasonVal = (newX+","+newY+","+newZ);
		//System.out.println("sending volume XMPP as: " +jasonVal);
		myJStateSender.addAndSendMsg("geometry/genericSpatial", jasonVal);
		//myJStateSender.send();
		return true;
	}
	
	@Action
	public boolean sendToBSF(Term[] terms) {
		String from = terms[0] + "";
		String msgCont = terms[1] + "";
		String newMsg = from + "," + msgCont;
		
		//System.out.println("In VehicleArch to send: " + newMsg);

		myJStateSender.addAndSendMsg("message",newMsg);
		//myJStateSender.send();
		return true;
	}
	
	@Action
	public boolean monitorAgent(Term[] terms) 
	{
		if (jStateInfoOn)
		{
			String agentName = ((VarTerm)terms[0]).toString();
			System.out.println(	"agArch has been told to also monitor " + agentName);
			System.out.println("all agents currently running are: " + getArchInfraTier().getRuntimeServices().getAgentsQty());
		
			AgArch monAgArch = RunCentralisedMAS.getRunner().getAg(agentName);
			System.out.println("agent name is on: " + monAgArch.getAgName() + " and has beliefs: "  + monAgArch.getTS().getAg().getBB().size());

			myJStateSender.addChildBBMonitor(monAgArch);
		}
		else
		{
			System.out.println("jason state data is turned off in VehicleArch class at the moment though!");
		}
		return true;
	}
	
	public void addData(String newItem)
	{
		//receivedData.add(newItem);
		//System.out.println("agent told " + newItem);
		//Literal newLit = new LiteralImpl(true, new Pred("position"));;
		//info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) 
		String[] newData = newItem.split(",");
		if (newData[0].equals("spatial"))
		{
			Literal newLit = Literal.parseLiteral("info("+newData[1]+","+newData[2]+","+newData[3]+",0,0,"+newData[4]+",0)");
		//	System.out.println("adding : info("+newData[1]+","+newData[2]+","+newData[3]+",0,0,"+newData[4]+",0)");
			Agent ag = getTS().getAg();
			try {
			//Literal clearLit = Literal.parseLiteral("info(_,_,_,_,_,_,_)");
			Literal clearLit = Literal.parseLiteral("info("+newData[1]+","+newData[2]+","+newData[3]+",0,0,"+newData[4]+",0)");
			ag.abolish(clearLit,null);
			//System.out.println("deleted all previous info beliefs: " + resultDel);
			}
			catch (Exception eee) {
				System.out.println("error in addData");
				eee.printStackTrace();
			} ; //5
			this.addInfoBel(newLit);
		}
	}

	@Override 
	public void sendMsg(Message m) throws Exception {
	
		getTS().getLogger().info(System.currentTimeMillis() + " Msg sent counter = "+ (mc++));
		mc++;
		//System.out.println("sending Message: " + m.toString());
		if (jStateInfoOn)
		{
			String senderString=m.getSender();
			String recString=m.getReceiver();
			String ilString=m.getIlForce();
			Literal conString=(Literal)m.getPropCont();
			myJStateSender.addAndSendMsg("message/"+ilString, recString + "," + conString.toString());
			//myJStateSender.addJMessageToSend(senderString,recString,ilString,conString.toString());
			//myJStateSender.send();
		}
		super.sendMsg(m);
	}
	@Override
	public List<Literal> perceive()  {
		//String filename = this.getAgName();
		getTS().getLogger().info(System.currentTimeMillis() + " Perceive counter = "+ (pc++));
		pc++;
		
		return super.perceive();
	}
	
	/**
	 * This method is called when agent wants to execute an action.
	 */
	public void act(ActionExec action, List<ActionExec> feedback) {
		super.act(action, feedback);
		///System.out.println("Performing action: " + action.toString());
		Structure actionTerm = action.getActionTerm();
		String functor = actionTerm.getFunctor();
		Method m = annotatedMethods.get(functor);
		if (m != null) {
			try {
				m.invoke(this, (Object)actionTerm.getTermsArray());
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}	
	
	public void addInfoBel(Literal b) {
		try {
			Agent ag = getTS().getAg();
			ag.addBel(b);
			
		} catch (RevisionFailedException e) {
			e.printStackTrace();
		}
	}
	private int getParamAsInt(int paramIndex, Term[] terms) {
		int foundIntTerm = 0;
		try 
		{
			foundIntTerm = (int)((NumberTerm)terms[paramIndex]).solve();
		}	
		catch (Exception e) { 
				System.out.println("error converting value to int in getParaAsInt..");
				e.printStackTrace();
			}
		return foundIntTerm;
	}
	
	private double getParamAsDouble(int paramIndex, Term[] terms) {
		double foundDoubleTerm =0.0;
		try
		{
			foundDoubleTerm=(double)((NumberTerm)terms[paramIndex]).solve();
		}
		catch (Exception e) { 
				System.out.println("error converting value to double in getParaAsDouble..");
				e.printStackTrace();
			}
		return foundDoubleTerm;
	}
	
	private String getParamAsString(int paramIndex, Term[] terms) {
		//System.out.println("getting string, this might be dodgy");
		return (String)((StringTerm)terms[paramIndex]).getString();
	}

	private String getAtomAsString(int paramIndex, Term[] terms) {
		//System.out.println("getting atom as string, this might be dodgy");
		return (String)((Atom)terms[paramIndex]).toString();
	}


}
