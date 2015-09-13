package mygame;

import com.jme3.math.Vector3f;
import com.jme3.util.SkyFactory;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.DirectionalLight;

import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.shape.Line;
import com.jme3.math.Quaternion;
import com.jme3.niftygui.NiftyJmeDisplay;

import com.jme3.scene.Geometry;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import com.jme3.app.state.ScreenshotAppState;

import com.jme3.font.BitmapText;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.RenderState;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Caps;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.SpotLightShadowFilter;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.system.NanoTimer;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.JsonReading;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jme3utilities.sky.SkyControl;

public class Main extends SimpleApplication implements ScreenController {
    
    private static Main app;
    CopyOnWriteArrayList<VehicleState> myVehicleStates = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<Node> newNodes = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<VehicleJasonText> myChildJStates = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<VehicleCollisionBox> myVehCollisionBoxes = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<AOI> myAOINodes = new CopyOnWriteArrayList();
    private static CopyOnWriteArrayList<TrafficLight> myTrafficLights = new CopyOnWriteArrayList();
    CopyOnWriteArrayList<Node> nodesToAdd = new CopyOnWriteArrayList();
    ArrayList<BoundingBox> renderedBB = new ArrayList<BoundingBox>();
    ArrayList<String> knownVehicles = new ArrayList<String>();
    CopyOnWriteArrayList<WayPoint> wayPointList = new CopyOnWriteArrayList<WayPoint>();
    CopyOnWriteArrayList<Vector3f> myKnownPoints = new CopyOnWriteArrayList<Vector3f>();
    private static String aoiNodeName = "aoiSensor";
    private static String homeSensors = "homeSensor";
    private static Long startTime = 0L;
    private static Long currentTime = 0L;
    private static Node timeNode;
    private static Node AOINode;
    public static final Quaternion ROLL045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 0, 1));
    public static final Quaternion YAW045n = new Quaternion().fromAngleAxis(-FastMath.PI / 4, new Vector3f(0, 1, 0));
    public static final Quaternion YAW045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 1, 0));
    public static final Quaternion YAW090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 1, 0));
    public static final Quaternion YAW180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0));
    public static final Quaternion YAW270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 1, 0));
    public static final Quaternion PITCH045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(1, 0, 0));
    public static final Quaternion UPRIGHT = new Quaternion().fromAngleAxis(-FastMath.PI / 2, new Vector3f(1, 0, 0));
    private WorkerSimNonThreadSender simNonThreadSender;
    private static Long currentSimTime = 0L;
    private static Long previousSimTime = 0L;
    private static boolean timeFromSUMO = false;
    private Material greenMat, blueMat;
    private Nifty nifty;
    private static boolean addedAlready = false;
    private static boolean addedCamera = false;
    private static boolean showDebugShapes = true;
    private static ChaseCamera chaseCam;
    private static VideoRecorderAppState myVidState;
    private static String XMPPServer = "127.0.0.1";
    private static DebugShapes dbShapes;
    private static HouseShapes houseShapes;
    private static String currentCamTarget = "none";
    private static Node debugNode = new Node();
    private static Node routeNode = new Node();
    private static Node collisionNode = new Node();
    private static Node trafficLightNode = new Node();
    private boolean flyMode = false;
    private static int xmppCount, xmppCountAlt = 0;
    private static float xmppUpdateTime = 0.0f;
    private static JFreeGraphHUD newGraphHud;
    private static boolean inDestroy = false;
    private static Double lastXMPPUpdateTime = 0d;
    private static NanoTimer myNanoTimer = new NanoTimer();
    private static SimpleDateFormat formatter;
    private static SimpleDateFormat formatterHour;
    private static SimpleDateFormat formatterMin;
    private static SimpleDateFormat formatterDay;
    private static SimpleDateFormat formatterMonth;
    protected static float terrainXscale = 1f;
    protected static float terrainYscale = 1f;
    protected static float terrainXtrans = 1f;
    protected static float terrainYtrans = 1f;
    protected static float terrainZtrans = 1f;
    private static Spatial terrainModel;
    private static Spatial trafficLight;
    private static SensorClient mySensorClient, instSensorClient;
    private double lastVehReadingUpdateTime = 0;
    private static String jasonSensorVehicles = "jasonSensorVehicles";
    private static String jasonSensorStates = "jasonSensorStates";
    private static String scenarioLocation = "unset";
    private static Double sumoOffSetX = 0D;
    private static Double sumoOffSetY = 0D;
    private static ScreenshotAppState screenshotState;
    private static String followVehicle = "c1.1";
    private long startupTime = 0L;
    private long startupDelay = 1000L;
    private static String dateTimeVal = "unknown";
    private static File storeDir;
    private static String currentPath = "";
    private static boolean runningAndroid=false;
    private static boolean issuedDiscardWarning=false;
    private static boolean useXMPP=false;
    private static boolean useMQTT=false;
    private static boolean useNoNet=false;
    private static SkyControl sc;
    private static boolean useSky = true;    
    private TerrainQuad terrain;
    Material mat_terrain;
        
    public static void main(String[] args)  {

        app = new Main();
        AppSettings appSet = new AppSettings(true);
        appSet.setAudioRenderer(null);
        appSet.setRenderer(AppSettings.LWJGL_OPENGL2);

        //setup video capture
        /*File video = File.createTempFile("video-output", ".mp4");
        app.setTimer(new IsoTimer(30));*/
        app.setSettings(appSet);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);

        //use below to generate videos
        //  Capture.captureVideo(app, video);
        //  System.out.println("WARNING: capturing video, this will eat up disk space...");

        app.start();
    }

    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

    public void pauseSim() {
        if (simNonThreadSender != null) {
            System.out.println("pause");
            simNonThreadSender.addMessageToSend("simState", "pause");
            simNonThreadSender.send();
        }
    }

    public void rewindSim() {
        if (simNonThreadSender != null) {
            System.out.println("rewind");
            simNonThreadSender.addMessageToSend("simState", "rewind");
            simNonThreadSender.send();
        }
    }
    
    public void fastforwardSim() {
        if (simNonThreadSender != null) {
            System.out.println("fastforward");
            simNonThreadSender.addMessageToSend("simState", "fastforward");
            simNonThreadSender.send();
        }
    }

    public void playSim() {
        if (simNonThreadSender != null) {
            System.out.println("play");
            simNonThreadSender.addMessageToSend("simState", "play");
            simNonThreadSender.send();
        }
    }

    private void switchCams() {
        flyMode = !flyMode;
        flyCam.setEnabled(flyMode);
        chaseCam.setEnabled(!flyMode);
    }

    @Override
    public void simpleInitApp() {
        
        assetManager.registerLoader(TextLoader.class, "txt");
        assetManager.registerLoader(TextLoader.class, "cfg");
        ArrayList<String> configContents = (ArrayList<String>) assetManager.loadAsset("config.txt");
        System.out.println("config file contains " +configContents.size());
        
        //this duplicates the main check, but im not 100% sure thats working, and no harm doing it here too
        if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) {
            System.out.println("running on android");
            runningAndroid=true;
        } else {
            System.out.println("not android");
        }
        
        Collection<Caps> caps = renderer.getCaps();
        System.out.println("graphics capability: " + caps.toString());
        
        //moved from Main() to try to get working with Android..
        //little bit hacky, if you run from IDE then it be in a different relative path..
        try 
        {
           for (String line : configContents)
           {
                if (line.contains("OPENFIRE")) {
                    String[] configArray = line.split("=");
                    XMPPServer = configArray[1];
                    System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
                }
                if (line.contains("LOCATION")) {
                    String[] configArray = line.split("=");
                    scenarioLocation = configArray[1];
                    System.out.println("Using scenario location: " + scenarioLocation);
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
                        else if(configArray[1].equals("NONE"))
			{
				useNoNet=true;
			}
		}
            }
            if (useNoNet)
            {
                System.out.println("WARNING: No network comms enabled");
            }
	    if (!useMQTT && !useXMPP && !useNoNet)
	    {
			System.out.println("no COMMUNICATION value found in config.txt, should be = MQTT or XMPP or NONE");
			System.exit(1);
	    }

            if (scenarioLocation.equals("unset")) {
                System.out.println("didn't read LOCATION value in config.txt, should be m25 or bath, defaulting to bath");
                scenarioLocation = "bath";
            } else if (scenarioLocation.equals("m25")) {
                System.out.println("setting scenario location to m25");
            }
            else if (scenarioLocation.equals("home")) {
                System.out.println("setting scenario location to home");
                scenarioLocation = "home";
            } else {
                if (!scenarioLocation.equals("bath")) {
                    System.out.println("couldn't read LOCATION value properly in config.txt, check its m25 or bath, defaulting to bath now");
                    scenarioLocation = "bath";
                }
                terrainXtrans = 281.5698f;
                terrainYtrans = 266.6075f;
                sumoOffSetX = 2385.33;
                sumoOffSetY = 1691.39;
                //add method/vars here to sort out a conversion function to flip a received x,y to an offset y,x based on bath or m25
            }
        }
        catch (Exception e)
        {
            System.out.println("failed in config section");
            e.printStackTrace();
        }

        java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

        timeNode = new Node("timeNode");
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatterHour = new SimpleDateFormat("HH");
        formatterMin = new SimpleDateFormat("mm");
        formatterDay = new SimpleDateFormat("dd");
        formatterMonth = new SimpleDateFormat("MM");
  
        if (useSky && !runningAndroid)
        {
                sc = new SkyControl(assetManager, cam, 0.9f,true,true);
                sc.getSunAndStars().setHour(12f);
                sc.getSunAndStars().setObserverLatitude(51.5f * FastMath.DEG_TO_RAD);
                sc.getSunAndStars().setSolarLongitude(Calendar.AUGUST, 20);
                if (runningAndroid)
                {
                    sc.clearStarMaps();
                    sc.getUpdater().setBloomEnabled(false);
                }
                      
                rootNode.addControl(sc);
                sc.setEnabled(true);
        }
        
        if (scenarioLocation.equals("bath"))
        {
            try {
                //create instances of all traffic lights defined in here, created in off state by default
                //then if we get a sim update on them, they light up!

                ArrayList<String> lightContents = (ArrayList<String>) assetManager.loadAsset("lights.cfg");
                System.out.println("lights file contains " +lightContents.size());

                for (String lightline : lightContents)
                {
                    String[] lightArray = lightline.split(",");
                    if (lightArray.length == 4) {
                        String lightID = lightArray[0];
                        float x = Float.valueOf(lightArray[1]);
                        float z = Float.valueOf(lightArray[2]);
                        float head = Float.valueOf(lightArray[3]);
                        TrafficLight newLight = new TrafficLight(this, lightID);
                        System.out.println("adding light: " + lightID + " " + x + " " + z);
                        newLight.setPosition(x, -1.0f, z);
                        newLight.setAngle(head);
                        // System.out.println("adding");
                        myTrafficLights.add(newLight);

                    } else {
                        System.out.println("couldnt process traffic light line: " + lightline);
                    }
                }
            } catch (Exception err) {
                System.out.println("error handling traffic light creation");
                err.printStackTrace();
            }
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date();
        dateTimeVal = dateFormat.format(date);
        /*System.out.println("Setting date time folder to " + dateTimeVal);
         storeDir = new File(dateTimeVal);
         try
         {
         storeDir.mkdir();
         } 
         catch(Exception fe)
         {
         System.out.println("Couldn't create dir for logging to for some reason..");
         fe.printStackTrace();
         } */

        currentPath = "";
        try {
            currentPath = new java.io.File(".").getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Current dir: " + currentPath);

        screenshotState = new ScreenshotAppState();
        this.stateManager.attach(screenshotState);
       // screenshotState.setIsNumbered(false);

        myNanoTimer.reset();
        if (timeNode != null)
        {
            guiNode.attachChild(timeNode);
        }
        else
        {
            System.out.println("guiNode is null for some reason..");
        }
        setDisplayStatView(false);

        AOINode = new Node();
        rootNode.attachChild(AOINode);

        blueMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blueMat.setColor("Color", ColorRGBA.Blue);
        greenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        greenMat.setColor("Color", ColorRGBA.Green);

        //      myGraphHud = new GraphHUD(this,guiFont,settings.getHeight(),settings.getWidth());  
        //not working on android..
        if (!runningAndroid)
        {
            newGraphHud = new JFreeGraphHUD();
        }
        
        myVehicleStates = new CopyOnWriteArrayList<VehicleState>();

	if (useXMPP && !useNoNet)
	{
		System.out.println("Using XMPP");
		while (instSensorClient == null) 
		{
	   		try {
	        		instSensorClient = new SensorXMPPClient(XMPPServer, "xmppviewer-inst", "jasonpassword");
	    		} 
			catch (Exception e1) 
			{
                            System.out.println("Exception in establishing inst client.");
                            e1.printStackTrace();
	    		}
		}
	}
	else if (useMQTT && !useNoNet)
	{
		System.out.println("Using MQTT");
	   	try {
	        	instSensorClient = new SensorMQTTClient(XMPPServer, "xmppviewer-inst");
	    	} 
		catch (Exception e1) 
		{
                    System.out.println("Exception in establishing inst client.");
                    e1.printStackTrace();
	    	}
	}

        startupTime = System.currentTimeMillis();
        // for norms
        if (!useNoNet)
        {
            instSensorClient.addHandler("NODE_NORM", new ReadingHandler() {
                @Override
                public void handleIncomingReading(String node, String rdf) {
                    if ((startupTime + startupDelay) < System.currentTimeMillis()) {
                        System.out.println("received norm!");
                        try {
                            JsonReading jr = new JsonReading();
                            jr.fromJSON(rdf);
                            JsonReading.Value val = jr.findValue("COUNT");
                            Long i = (Long) val.m_object;
                            //System.out.println("got COUNT of " + i);
                            val = jr.findValue("CONTENT");
                            if (val != null) {
                                String mainVal = val.m_object.toString();
                                for (int idx = 0; idx < i; idx++) {
                                    String str = mainVal + idx;
                                    //System.out.println("trying to findValue on " + str);
                                    val = jr.findValue(str);
                                    if (val != null) {
                                        //System.out.println("obligation: " + val.m_object.toString());
                                        processInstMessage(val.m_object.toString());
                                    } else {
                                        System.out.println("searching for " + str + " failed");
                                    }
                                }
                            } else {
                                System.out.println("null val found for CONTENT");
                            }

                        } catch (Exception e) {
                            System.out.println("error in inst handler");
                            e.printStackTrace();
                        }
                    }
                }
            });
            try {
                instSensorClient.subscribe("NODE_NORM");
            } catch (Exception xe) {
                System.out.println("failed to subscribe: " + "NODE_NORM");
                xe.printStackTrace();
            }
        }

	if (useXMPP && !useNoNet)
	{
		System.out.println("Using XMPP");
		while (mySensorClient == null) 
		{
			try 
			{
				mySensorClient = new SensorXMPPClient(XMPPServer, "xmppviewer", "jasonpassword");
			} catch (Exception e1) 
			{
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}
        }
	else if (useMQTT &&!useNoNet)
	{
		System.out.println("Using MQTT");
		try 
		{
			mySensorClient = new SensorMQTTClient(XMPPServer, "xmppviewer");
		} catch (Exception e1) 
		{
			System.out.println("Exception in establishing client.");
			e1.printStackTrace();
		}
        }

        if (!useNoNet)
        {
            mySensorClient.addHandler(aoiNodeName, new ReadingHandler() {
                @Override
                public void handleIncomingReading(String node, String rdf) {
                    if ((startupTime + startupDelay) < System.currentTimeMillis()) {
                        try {
                            DataReading dr = DataReading.fromRDF(rdf);
                            String takenBy = dr.getTakenBy();

                            DataReading.Value AOIVal = dr.findFirstValue(null, "http://127.0.0.1/trafficLightSensors/position", null);
                            if (AOIVal != null) {
                                String tempAOIReading = (String) AOIVal.object;
                                String[] readPoints = tempAOIReading.split(",");
                                Float xPoint = Float.parseFloat(readPoints[0]);
                                Float yPoint = Float.parseFloat(readPoints[1]);
                                MyPoint2D extLoc = new MyPoint2D(xPoint, yPoint);
                                MyPoint2D convertedPoints = convertExternalLocation(extLoc);
                                //System.out.println("creating at " + xPoint + "," + yPoint);
                                //createXMPPGenericSpatialObject((float)convertedPoints.getX(),1f,(float)convertedPoints.getY());
                            }

                            DataReading.Value AOISpatial = dr.findFirstValue(null, "http://127.0.0.1/AOISensors/spatial", null);
                            if (AOISpatial != null) {
                                String tempAOIReading = (String) AOISpatial.object;
                                String[] readPoints = tempAOIReading.split(",");
                                Float xPoint = Float.parseFloat(readPoints[0]);
                                Float yPoint = Float.parseFloat(readPoints[1]);

                                Float radiusPoint = Float.parseFloat(readPoints[2]);

                                MyPoint2D convertedPoints = convertExternalLocation(new MyPoint2D(xPoint, yPoint));

                                //System.out.println("creating AOI at " + convertedPoints.getX() + "," + convertedPoints.getY());
                                //System.out.println("converting from " + xPoint + ", " + yPoint);
                                createXMPPAOISpatialObject(takenBy, (float) convertedPoints.getX(), 1f, (float) convertedPoints.getY(), radiusPoint);

                            } else if (dr.getTakenBy().contains("trafficLights")) {
                                List<DataReading.Value> laneLightValues = dr.findValues(null, "http://127.0.0.1/trafficLightSensors/entryExitColour", null);
                                for (DataReading.Value lVal : laneLightValues) {
                                    String fullInfo = (String) lVal.object;
                                    String[] splitInfo = fullInfo.split(",");
                                    String entryLane = splitInfo[0];
                                    String exitLane = splitInfo[1];
                                    char colourState = splitInfo[2].charAt(0);
                                    processTrafficLight(entryLane, colourState);
                                }

                                //System.out.println("told a traffic light reading");
                                   /* DataReading.Value tLightState = dr.findFirstValue(null, "http://127.0.0.1/trafficLightSensors/state", null);
                                 DataReading.Value tLightLanes = dr.findFirstValue(null, "http://127.0.0.1/trafficLightSensors/lanes", null);
                                 if(tLightState != null && tLightLanes != null) 
                                 {
                                 System.out.println("updated a light");
                                 String colourState = tLightState.object.toString();         
                                 String junctionID = dr.getTakenBy();
                                 String laneList = tLightLanes.object.toString();
                                 processTrafficLight(junctionID,tLightLanes.object.toString(),colourState);
                                 }                                
                                 else
                                 {
                                 //System.out.println("didnt get correct data pair");
                                 }*/
                            }

                        } catch (Exception e) {
                            System.out.println("Error adding new message to queue..");
                            e.printStackTrace();
                        }
                    }
                    else if (!issuedDiscardWarning)
                    {
                        System.out.println("WARNING! Got reading but ignoring it during init phase..");
                        issuedDiscardWarning=true;
                    }
                }
            });
            try {
                mySensorClient.subscribe(aoiNodeName);
            } catch (Exception e1) {
                System.out.println("Exception while subscribing to " + aoiNodeName);
                e1.printStackTrace();
            }
        
            
            mySensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() {
                @Override
                public void handleIncomingReading(String node, String rdf) {
                    if ((startupTime + startupDelay) < System.currentTimeMillis()) {
                        try {
                            DataReading dr = DataReading.fromRDF(rdf);
                            if (dr.getLocatedAt().equals("http://127.0.0.1/vehicleSensors")) {

                                DataReading.Value spatialVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null);
                                if (spatialVal != null) {
                                    String tempReading = (String) spatialVal.object;
                                    if (dr.getTimestamp() < lastVehReadingUpdateTime) {
                                        System.out.println("XXXXXXXXXXXXX Received DataReading out of order!!!! XXXXXXXXXXXXXXXX");
                                        XMPPPAlert("someAlert");
                                    }
                                    lastVehReadingUpdateTime = dr.getTimestamp();
                                    processXMPPData("spatial," + tempReading, dr.getTakenBy());
                                }

                                //TODO: its possible just a single data reading of some other type was received, but could be worth checking
                               /* else
                                 {
                                 System.out.println("got a vehicle update that wasn't handled:");
                                 System.out.println(dr.getTakenBy() + " " + dr.getLocatedAt());
                                 System.out.println(dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null).subject.toString());
                                 System.out.println(dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null).predicate.toString());
                                 System.out.println(dr.findFirstValue(null, "http://127.0.0.1/sensors/types#spatial", null).object.toString());
                                 }  */

                                DataReading.Value vehLightsVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#LightState", null);
                                if (vehLightsVal != null) {
                                    String tempIndicatorsReading = (String) vehLightsVal.object;
                                    //System.out.println("and was told LRBF light state: " + tempIndicatorsReading);
                                    processXMPPVehicleLightData(tempIndicatorsReading, dr.getTakenBy());
                                }

                                DataReading.Value vehHealthVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#healthState", null);
                                if (vehHealthVal != null) {
                                    String tempHealthReading = (String) vehHealthVal.object;
                                    //System.out.println("and was told LRBF light state: " + tempIndicatorsReading);
                                    processXMPPVehicleHealthData(tempHealthReading, dr.getTakenBy());
                                }

                                /*DataReading.Value simTimeVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#simTime", null);
                                if (simTimeVal != null) {

                                    Integer timeValTemp = (Integer) simTimeVal.object;
                                    currentSimTime = timeValTemp * 1L;
                                    //System.out.println("received a SUMO sim time set to: " + currentSimTime);
                                    timeFromSUMO = true;
                                }*/


                            } else {
                                System.out.println("didnt handle value from " + dr.getLocatedAt());
                            }
                        } catch (Exception e) {
                            System.out.println("Exception in jasonSensorVehicles handler");
                            e.printStackTrace();
                        }
                    }
                }
            });
            try {
                mySensorClient.subscribe(jasonSensorVehicles);
            } catch (Exception e1) {
                System.out.println("Exception while subscribing to sensor.");
                e1.printStackTrace();
            }

            mySensorClient.addHandler(jasonSensorStates, new ReadingHandler() {
                @Override
                public void handleIncomingReading(String node, String rdf) {
                    if ((startupTime + startupDelay) < System.currentTimeMillis()) {
                        try {
                            DataReading dr = DataReading.fromRDF(rdf);

                            //   System.out.println("received dr, from " + dr.getTakenBy());
                            if (dr.getLocatedAt().equals("http://127.0.0.1/agentJState")) {
                                //    System.out.println("received dr of agentJState, from " + dr.getTakenBy());

                                //eughh this is such a bad way of doing this.. if its a specified type, handle it, otherwise assume we can else it
                                //TODO: FIX ME!!!
                                boolean processedVal = false;
                                DataReading.Value geomVal = dr.findFirstValue(null, "http://127.0.0.1/JState/geometry/collisionVolume", null);
                                if (geomVal != null) {
                                    // System.out.println("got told new geom value! " + geomVal.object.toString() + " from " + dr.getTakenBy());
                                    processJStateXMPPCollisionData(geomVal.object.toString(), dr.getTakenBy());
                                    processedVal = true;
                                }
                                DataReading.Value spatialVal = dr.findFirstValue(null, "http://127.0.0.1/JState/geometry/genericSpatial", null);
                                if (spatialVal != null) {
                                    // System.out.println("got told new generic spatial value! " + spatialVal.object.toString() + " from " + dr.getTakenBy());
                                    processJStateXMPPGenericSpatialData(spatialVal.object.toString(), dr.getTakenBy());
                                    processedVal = true;
                                }
                                DataReading.Value wayVal = dr.findFirstValue(null, "http://127.0.0.1/JState/message/waypoint", null);
                                if (wayVal != null) {
                                    // System.out.println("XXXXXXX got told new waypoint value! " + wayVal.object.toString() + " from " + dr.getTakenBy());
                                    processJStateXMPPWaypointData(wayVal.object.toString(), dr.getTakenBy());
                                    processedVal = true;
                                }
                                if (!processedVal) {
                                    DataReading.Value jStateVal = dr.findFirstValue(null, null, null);
                                    if (jStateVal != null) {
                                        // System.out.println("handling jStateVal, but clean up this handling! dangerous..");
                                        processJStateXMPPData(jStateVal.predicate.toString(), jStateVal.object.toString(), dr.getTakenBy());
                                    }
                                }
                                // System.out.println("This dataReading contains " + drValues.size() + " items");
                            }

                        } catch (Exception e) {
                            System.out.println("error in handling incoming reading");
                            e.printStackTrace();
                        }
                    }
                }
            });

            try {
                mySensorClient.subscribe(jasonSensorStates);
            } catch (Exception e1) {
                System.out.println("Exception while subscribing to sensor.");
                e1.printStackTrace();
            }

            mySensorClient.addHandler("simStateSensor", new ReadingHandler() {
                @Override
                public void handleIncomingReading(String node, String rdf) {
                    if ((startupTime + startupDelay) < System.currentTimeMillis()) {
                        try {
                            DataReading dr = DataReading.fromRDF(rdf);
                            boolean gotMsg = false;
                            DataReading.Value simStateVal = dr.findFirstValue(null, "http://127.0.0.1/simDefinitions/simTime", null);
                            if (simStateVal != null) {
                                String tempSimState = (String) simStateVal.object;
                                processSimTime(tempSimState);
                                gotMsg = true;
                            }

                            DataReading.Value simMsgVal = dr.findFirstValue(null, "http://127.0.0.1/simDefinitions/simMsgCount", null);
                            if (simMsgVal != null) {
                                String tempSimState = (String) simMsgVal.object;
                                processXMPPCount(tempSimState.toString());
                                gotMsg = true;
                            }

                        } catch (Exception e) {
                            System.out.println("Exception in simState handler");
                            e.printStackTrace();
                        }
                    }
                }
            });

            try {
                mySensorClient.subscribe("simStateSensor");
            } catch (Exception e1) {
                System.out.println("Exception while subscribing to sensor.");
                e1.printStackTrace();
            }

            mySensorClient.addHandler(homeSensors, new ReadingHandler() {
                    @Override
                    public void handleIncomingReading(String node, String rdf) 
                    {
                            try
                            {
                                    DataReading dr = DataReading.fromRDF(rdf);
                                    //System.out.println("received home sensor reading");
                                    if (dr.getLocatedAt().equals("http://127.0.0.1/HueSensors")) 
                                    {
                                            String lightName = "";
                                            int redLight=0;
                                            int greenLight=0;
                                            int blueLight=0;
                                            int brightness=0;
                                            String state="";
                                            String model="";

                                            DataReading.Value nameVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/name", null);
                                            if (nameVal != null)
                                            {
                                                    lightName = (String) nameVal.object;
                                            }
                                            DataReading.Value redVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/redval", null);
                                            if (redVal != null)
                                            {
                                                    redLight = (int) redVal.object;
                                            }
                                            DataReading.Value greenVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/greenval", null);
                                            if (greenVal != null)
                                            {
                                                    greenLight = (int) greenVal.object;
                                            }
                                            DataReading.Value blueVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/blueval", null);
                                            if (blueVal != null)
                                            {
                                                    blueLight = (int) blueVal.object;
                                            }

                                            DataReading.Value brightVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/brightness", null);
                                            if (brightVal != null)
                                            {
                                                    brightness = (int) brightVal.object;
                                            }

                                            DataReading.Value stateVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/state", null);
                                            if (stateVal != null)
                                            {
                                                    state = (String) stateVal.object;
                                            }

                                            DataReading.Value modelVal = dr.findFirstValue(null, "http://127.0.0.1/components/lights/model", null);
                                            if (modelVal != null)
                                            {
                                                    model = (String) modelVal.object;
                                            }

                                            //System.out.println("got reading for " + lightName + " model "+ model + " at rgb " + redLight + "," + greenLight + "," + blueLight + ", brightness: " + brightness + " and is " + state);


                                            if (houseShapes != null)
                                            {
                                                    float r = redLight/255f;
                                                    float g = greenLight/255f;
                                                    float b = blueLight/255f;

                                                    if (model.equals("LWB004"))
                                                    {
                                                            r = 1f;
                                                            g = 1f;
                                                            b = 1f;
                                                    }
                                                    //System.out.println("r: "+ r);
                                                    if (state.equals("OFF"))
                                                    {
                                                            brightness=0;
                                                    }

                                                    houseShapes.updateLight(lightName, new ColorRGBA(r,g,b,0f), (float)brightness/255);
                                            }
                                    }
                                    else if (dr.getTakenBy().equals("http://127.0.0.1/components/houseSensors/piSensor1") || dr.getTakenBy().equals("http://127.0.0.1/components/houseSensors/enlitenSensor1"))
                                    {
                                            //piSensor1 should be unique name in unique location
                                            for (DataReading.Value foundVal : dr.findValues(null,null,null))
                                            {
                                                    String predName = (String)foundVal.predicate;
                                                    boolean msgHandled = false;
                                                    String oClass = foundVal.object.getClass()+"";
                                                    //System.out.println("got a reading, didn't handle it, but it had at least: " + predName);
                                                    if (predName.equals("http://127.0.0.1/sensors/types#DHT22humidity"))
                                                    {
                                                            Double humidVal=0d;
                                                            //TODO: generic passing of this test
                                                            if(oClass.equals("class java.lang.Double"))
                                                            {
                                                                    humidVal = (Double)foundVal.object;
                                                            }
                                                            else if (oClass.equals("class java.lang.String"))
                                                            {
                                                                    String strMsg = (String)foundVal.object;
                                                                    humidVal = Double.parseDouble(strMsg);
                                                            }
                                                            else
                                                            {
                                                                    System.out.println("didn't handle type: " + oClass);
                                                            }
                                                            //System.out.println("Humidity " + humidVal);
                                                            msgHandled=true;

                                                    }
                                                    else if (predName.equals("http://127.0.0.1/sensors/types#DHT22temperature"))
                                                    {
                                                            Double tempVal=0d;

                                                            if(oClass.equals("class java.lang.Double"))
                                                            {
                                                                    tempVal = (Double)foundVal.object;
                                                            }
                                                            else if (oClass.equals("class java.lang.String"))
                                                            {
                                                                    String strMsg = (String)foundVal.object;
                                                                    tempVal = Double.parseDouble(strMsg);
                                                            }
                                                            houseShapes.updateTemp(dr.getTakenBy(), tempVal);
                                                            msgHandled=true;		
                                                    }
                                                    else if (predName.equals("http://127.0.0.1/sensors/types#PIR"))
                                                    {
                                                            //System.out.println("Movement detected by " + dr.getTakenBy());
                                                            houseShapes.newMovementDetection(dr.getTakenBy());
                                                            msgHandled=true;		 	
                                                    }

                                                    if (!msgHandled)
                                                    {
                                                            System.out.println("didnt handle " + predName + " from " + dr.getTakenBy());

                                                    }					
                                            }
                                    }
                                    else
                                    {
                                            System.out.println("unknown message type by " + dr.getTakenBy() + " from " + dr.getLocatedAt());
                                    }
                            }
                            catch(Exception e) 
                            { 
                                    System.out.println("error handling data in " + homeSensors);
                                    e.printStackTrace();
                                    System.out.println("due to:");
                                    System.out.println(rdf);
                            }
                    }
            });
            try {
                mySensorClient.subscribe(homeSensors);
            } catch (Exception e1) {
                System.out.println("Exception while subscribing to sensor.");
                e1.printStackTrace();
            }
        }

        try {
		if (useXMPP && !useNoNet)
		{
            		simNonThreadSender = new WorkerSimNonThreadSender(XMPPServer, "xmppviewersimstatesender", "jasonpassword", "simStateSensor", "http://127.0.0.1/localSensors", "http://127.0.0.1/localSensors/viewerSender");
		}
		else if (useMQTT && !useNoNet)
		{
			simNonThreadSender = new WorkerSimNonThreadSender(XMPPServer, "xmppviewersimstatesender", "jasonpassword", "simStateSensor", "http://127.0.0.1/localSensors", "http://127.0.0.1/localSensors/viewerSender", true, 0);
		}
           	System.out.println("Created simThreadSender, now entering its logic!");
        } catch (Exception e) {
            System.out.println("couldn't start sim thread sender");
            System.out.println(e.getStackTrace());
        }
        
        
        //use this for overlaying debug route from waypoint .txt file..
        //rootNode.attachChild(debugNode);

        //bathModel = assetManager.loadModel("Models/bath3.j3o");
        if (scenarioLocation.equals("bath")) {
            terrainModel = assetManager.loadModel("Models/bathJCoordConv.j3o");
            terrainModel.setLocalRotation(PITCH270);
            terrainModel.rotate(ROLL180);
            terrainModel.rotate(ROLL090);
            terrainXscale = 1f;
            terrainYscale = 1f;
            terrainXtrans = 281.5698f;
            terrainYtrans = 266.6075f;
            terrainZtrans = -1f;
            terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
            terrainModel.setLocalScale(1f, 1f, 1.0f);
            rootNode.attachChild(terrainModel);
        }
        else if (scenarioLocation.equals("home")) {
            houseShapes = new HouseShapes(this, runningAndroid);
        } 
        else if (scenarioLocation.equals("m25")) {
            terrainModel = assetManager.loadModel("Models/m25j10FinalConv2.j3o");
            // terrainModel = assetManager.loadModel("Models/newM25.j3o");

            //this is useful if you can only load the obj and want to save it back
           /*(BinaryExporter exporter = BinaryExporter.getInstance();
             File outputF = new File("newM25.j3o");
             try { 
             System.out.println("trying to save to j3o");
             exporter.save(terrainModel,outputF);
             }
             catch (Exception e) {
             e.printStackTrace();
             }*/

            terrainModel.setLocalRotation(PITCH270);
            terrainModel.rotate(ROLL180);
            terrainModel.rotate(ROLL090);
            terrainXscale = 1f;
            terrainYscale = 1f;

            terrainXtrans = 5059.39f;
            terrainYtrans = 8107.33f;
            terrainZtrans = -321f;


            terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
            terrainModel.setLocalScale(1f, 1f, 1.0f);
            rootNode.attachChild(terrainModel);
        } else {
            System.out.println("something went pretty wrong loading the terrain model!!");
        }


        if (!runningAndroid)
        {
            FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
            BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
            fpp.addFilter(bloom);
            viewPort.addProcessor(fpp);
        }

        //VB turn on for the original route
        dbShapes = new DebugShapes(this, scenarioLocation);

        rootNode.attachChild(trafficLightNode);

        if (scenarioLocation.equals("bath"))
        {
            addTrafficLights();
        }
        else if (!scenarioLocation.equals("home"))
        {
            addSky();
        }

        cam.setFrustumFar(10000);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/Nifty/GUI.xml", "start", this);

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);

        if (!scenarioLocation.equals("home"))
        {
            AmbientLight al = new AmbientLight();
            al.setColor(ColorRGBA.White.mult(0.7f));
            rootNode.addLight(al);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            rootNode.addLight(sun);
            DirectionalLight sun2 = new DirectionalLight();
            sun2.setDirection(new Vector3f(0.5f, 0.5f, 0.5f).normalizeLocal());
            rootNode.addLight(sun2);
        }

        inputManager.addMapping("SpatialPos", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping("FastLeft", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("FastRight", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("FastForward", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("FastBackward", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("FastUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("FastDown", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("Hints", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("ChangeCam", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping("ChangeCentralCam", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("ChangeCamType", new KeyTrigger(KeyInput.KEY_9));
        inputManager.addMapping("TakeShot", new KeyTrigger(KeyInput.KEY_B));
	inputManager.addMapping("RandomLights", new KeyTrigger(KeyInput.KEY_R));

        inputManager.addMapping("incX", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("incY", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("decX", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("decY", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("incXtrans", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("incYtrans", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addMapping("decXtrans", new KeyTrigger(KeyInput.KEY_7));
        inputManager.addMapping("decYtrans", new KeyTrigger(KeyInput.KEY_8));

        // Add the names to the action listener.
        inputManager.addListener(analogListener, new String[]{"incXtrans", "incYtrans", "decXtrans", "decYtrans", "incX", "incY", "decX", "decY", "FastLeft", "FastRight", "FastForward", "FastBackward", "FastUp", "FastDown"});
        inputManager.addListener(actionListener, new String[]{"RandomLights","TakeShot","SpatialPos", "Hints", "ChangeCam", "ChangeCamType", "ChangeCentralCam"});
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Hints") && !keyPressed) {
                if (showDebugShapes) {
                    System.out.println("hints off");
                    rootNode.detachChild(debugNode);
                    showDebugShapes = false;
                } else {
                    System.out.println("hints on");
                    rootNode.attachChild(debugNode);
                    showDebugShapes = true;
                }

            }
            if (name.equals("TakeShot") && !keyPressed) {
                //screenshotState.setFilePath(currentPath+"/"+dateTimeVal+"/");
                //double newfNum = 1000000+currentSimTime;
                screenshotState.setFilePath("");
                System.out.println("taking screenshot");
                screenshotState.takeScreenshot();
            }
            if (name.equals("SpatialPos") && !keyPressed) {
                System.out.println("Pos: " + cam.getLocation());
                System.out.println("Angle: " + cam.getDirection());
            }
            if (name.equals("RandomLights") && !keyPressed) {
		houseShapes.randomizeLights();
		System.out.println("randomized lights!");
            }
            if (name.equals("ChangeCamType") && !keyPressed) {
                System.out.println("changing cam type");
                switchCams();
            }
            if (name.equals("ChangeCentralCam") && !keyPressed) {
                System.out.println("changing camera to jason vehicle based on 'central' in name..");
                boolean existsJason = false;
                for (String checkS : knownVehicles) {
                    if (checkS.contains("central")) {
                        existsJason = true;
                    }
                }
                int currentValue = knownVehicles.indexOf(currentCamTarget);
                if ((currentValue != -1) && existsJason) {
                    int numberKnownVeh = knownVehicles.size();
                    boolean foundNewCentral = false;
                    boolean loopedTwice = false;
                    boolean loopedOnce = false;

                    while (!foundNewCentral && ((currentValue + 1) < (numberKnownVeh)) && !loopedTwice) {
                        currentValue++;
                        currentCamTarget = knownVehicles.get(currentValue);
                        if (currentCamTarget.contains("central")) {
                            rootNode.getChild(currentCamTarget).removeControl(chaseCam);
                            rootNode.getChild(currentCamTarget).addControl(chaseCam);
                            foundNewCentral = true;
                        } else if (currentValue + 1 >= numberKnownVeh) {
                            currentValue = 0;
                            if (loopedOnce) {
                                loopedTwice = true;
                            }
                            loopedOnce = true;
                        }
                    }
                }
            }
            if (name.equals("ChangeCam") && !keyPressed) {
                int currentValue = knownVehicles.indexOf(currentCamTarget);
                if (currentValue != -1) {
                    int numberKnownVeh = knownVehicles.size();
                    if ((currentValue + 1) < (numberKnownVeh)) {
                        rootNode.getChild(currentCamTarget).removeControl(chaseCam);
                        currentCamTarget = knownVehicles.get(currentValue + 1);
                        rootNode.getChild(currentCamTarget).addControl(chaseCam);

                    } else {
                        rootNode.getChild(currentCamTarget).removeControl(chaseCam);
                        currentCamTarget = knownVehicles.get(0);
                        rootNode.getChild(currentCamTarget).addControl(chaseCam);
                    }
                }
            }
        }
    };
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("incX")) {
                terrainXscale = terrainXscale + 0.001f;
                terrainModel.setLocalScale(terrainXscale, terrainYscale, 1.0f);
                System.out.println("increase X");
                System.out.println("terrain scale " + terrainModel.getLocalScale().toString());
            }
            if (name.equals("incY")) {
                terrainYscale = terrainYscale + 0.001f;

                terrainModel.setLocalScale(terrainXscale, terrainYscale, 1.0f);
                System.out.println("increase Y");
                System.out.println("terrain scale " + terrainModel.getLocalScale().toString());
            }
            if (name.equals("decX")) {
                terrainXscale = terrainXscale - 0.001f;
                terrainModel.setLocalScale(terrainXscale, terrainYscale, 1.0f);
                System.out.println("increase X");
                System.out.println("terrain scale " + terrainModel.getLocalScale().toString());
            }
            if (name.equals("decY")) {
                terrainYscale = terrainYscale - 0.001f;
                terrainModel.setLocalScale(terrainXscale, terrainYscale, 1.0f);
                System.out.println("increase Y");
                System.out.println("terrain scale " + terrainModel.getLocalScale().toString());
            }

            if (name.equals("incXtrans")) {
                terrainXtrans = terrainXtrans + 1f;
                terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
                System.out.println("increase X trans");
                System.out.println("terrain trans " + terrainModel.getLocalTranslation().toString());
            }
            if (name.equals("incYtrans")) {
                terrainYtrans = terrainYtrans + 1f;
                terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
                System.out.println("increase Y trans");
                System.out.println("terrain trans " + terrainModel.getLocalTranslation().toString());
            }
            if (name.equals("decXtrans")) {
                terrainXtrans = terrainXtrans - 1f;
                terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
                System.out.println("increase X trans");
                System.out.println("terrain trans " + terrainModel.getLocalTranslation().toString());
            }
            if (name.equals("decYtrans")) {
                terrainYtrans = terrainYtrans - 1f;
                terrainModel.setLocalTranslation(terrainXtrans, terrainZtrans, terrainYtrans);
                System.out.println("increase X trans");
                System.out.println("terrain trans " + terrainModel.getLocalTranslation().toString());
            }

            if (name.equals("FastRight")) {
                Vector3f v = cam.getLocation();
                Vector3f moveL = new Vector3f(v.x + 10, v.y, v.z);
                cam.setLocation(moveL);
                // System.out.println("turn left");
            }

            if (name.equals("FastLeft")) {
                Vector3f v = cam.getLocation();
                Vector3f moveR = new Vector3f(v.x - 10, v.y, v.z);
                cam.setLocation(moveR);
                // System.out.println("turn right");
            }
            if (name.equals("FastForward")) {
                Vector3f v = cam.getLocation();
                Vector3f moveR = new Vector3f(v.x, v.y, v.z - 5);
                cam.setLocation(moveR);
                // System.out.println("forward");
            }
            if (name.equals("FastBackward")) {
                Vector3f v = cam.getLocation();
                Vector3f moveR = new Vector3f(v.x, v.y, v.z + 5);
                cam.setLocation(moveR);
                // System.out.println("backward");
            }
            if (name.equals("FastUp")) {
                Vector3f v = cam.getLocation();
                Vector3f moveR = new Vector3f(v.x, v.y + 5, v.z);
                cam.setLocation(moveR);
                // System.out.println("go up");
            }
            if (name.equals("FastDown")) {
                Vector3f v = cam.getLocation();
                Vector3f moveR = new Vector3f(v.x, v.y - 5, v.z);
                cam.setLocation(moveR);
                // System.out.println("go down");
            }
        }
    };

    public void addVisual(Geometry newVis) {
        debugNode.attachChild(newVis);
    }

    private void addNewVehicle(String vehicleName, Float x, Float y, Float z, Float h) {
        VehicleState newVehicle = new VehicleState(x, y, z, h, vehicleName, assetManager);
        //newVehicle.vehicleName = vehicleName;
        newVehicle.updateValues(x, y, z, h);

        String[] newData = vehicleName.split("/");
        String shortName = newData[newData.length - 1];
        newVehicle.updateText4(shortName);

        myVehicleStates.add(newVehicle);
        Node nodeToAdd = newVehicle.getVehicleNode();
        //System.out.println("adding vehicle: " + vehicleName);
        nodeToAdd.setName(vehicleName);
        knownVehicles.add(vehicleName);

        newNodes.add(nodeToAdd);
        //System.out.println("added node to list to add in, size is now " + newNodes.size());
    }

    public void processJStateXMPPGenericSpatialData(String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        String[] findVehName;
        findVehName = vehicleName.split("/");
        String lastStrVal = findVehName[findVehName.length - 1];
        String[] coords = newItem.split(",");
        Float x1 = Float.valueOf(coords[0]);
        Float y1 = Float.valueOf(coords[1]);
        Float z1 = Float.valueOf(coords[2]);

        Box boxNew = new Box(Vector3f.ZERO, 1.5f, 1.5f, 1.5f);
        Geometry blueNew = new Geometry("genericBox", boxNew);
        blueNew.setMaterial(blueMat);
        blueNew.setLocalTranslation(x1, y1, z1);

        //TODO: fix this as when node updated during render it will crash
        // debugNode.attachChild(blueNew);
    }

    public void createXMPPGenericSpatialObject(Float x, Float y, Float z) {
        Box boxNew = new Box(Vector3f.ZERO, 1.5f, 50.5f, 1.5f);
        Geometry blueNew = new Geometry("genericBox", boxNew);
        blueNew.setMaterial(blueMat);

        Node newGenNode = new Node();
        newGenNode.attachChild(blueNew);
        newGenNode.setLocalTranslation(x, y, z);
        System.out.println("creating generic spatial at " + x + "," + y + "," + z);

        nodesToAdd.add(newGenNode);

    }

    public void createXMPPAOISpatialObject(String name, Float x, Float y, Float z, Float radius) {
        boolean updated = false;
        for (AOI testAOI : myAOINodes) {
            if (testAOI.getID().equals(name)) {
                testAOI.setPosition(x, y, z);
                testAOI.setRadius(radius);
                updated = true;
            }
        }
        if (!updated) {
            AOI newAOI = new AOI(this, name);
            myAOINodes.add(newAOI);
        }
    }

    public void processJStateXMPPWaypointData(String newItem, String vehicleName) {
        String[] waypointDetails = newItem.split(",");

        int wayNum = Integer.parseInt(waypointDetails[0]);
        float wayX = Float.parseFloat(waypointDetails[1]);
        float wayY = Float.parseFloat(waypointDetails[2]);
        System.out.append("updating waypoint info to " + waypointDetails[0] + " at " + wayX + "," + wayY);

        xmppCount = xmppCount + 1;

        if (myVehicleStates.isEmpty()) {
            System.out.println("no vehicles to relate jason state data back to!!");
        }
        for (VehicleState vehState : myVehicleStates) {
            String[] findVehName;
            findVehName = vehState.vehicleName.split("/");
            String lastStrVal = findVehName[findVehName.length - 1];

            if (vehicleName.contains(lastStrVal)) {
                vehState.updateText3("Waypoint: " + wayNum);
            }
        }

        WayPoint newWaypoint = new WayPoint(wayNum, wayX, wayY, assetManager);
        wayPointList.add(newWaypoint);
    }

    public void processJStateXMPPCollisionData(String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        //so, each vehicle can only have 1 collision volume
        //we need a class of vehicleNames and collision volumes
        //then on new collision data, if no vehiclename with a volume, add it
        //need to send it over to the render cycle somehow..
        //oh, that could have a list of boundedboxes, and update their geometry from the list of vehicle-geom pairs
        String[] findVehName;
        findVehName = vehicleName.split("/");
        String lastStrVal = findVehName[findVehName.length - 1];
        String[] coords = newItem.split(",");
        Float x1 = Float.valueOf(coords[0]);
        Float y1 = Float.valueOf(coords[1]);
        Float z1 = Float.valueOf(coords[2]);
        Float x2 = Float.valueOf(coords[3]);
        Float y2 = Float.valueOf(coords[4]);
        Float z2 = Float.valueOf(coords[5]);

        Vector3f origin = new Vector3f(x1, y1, z1);
        if (myVehCollisionBoxes.isEmpty()) {
            VehicleCollisionBox newVBox = new VehicleCollisionBox(assetManager, vehicleName);
            newVBox.update(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
            myVehCollisionBoxes.add(newVBox);
            System.out.println("adding first ever collision volume");
        } else {
            boolean foundVehBoxMatch = false;
            for (VehicleCollisionBox vehBox : myVehCollisionBoxes) {
                if (vehBox.getVehName().equals(lastStrVal)) {
                    foundVehBoxMatch = true;
                    vehBox.update(x1, y1, z1, x2, y2, z2);
                }
            }

            //add a new one if not found
            if (!foundVehBoxMatch) {
                System.out.println("adding new veh-col pairing");
                VehicleCollisionBox newVBox = new VehicleCollisionBox(assetManager, lastStrVal);
                newVBox.update(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
                myVehCollisionBoxes.add(newVBox);
            }
        }
    }

    public void processJStateXMPPData(String pred, String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        if (myVehicleStates.isEmpty()) {
            System.out.println("no vehicles to relate jason state data back to!!");
        }
        for (VehicleState vehState : myVehicleStates) {
            String[] findVehName;
            findVehName = vehState.vehicleName.split("/");
            String lastStrVal = findVehName[findVehName.length - 1];
            //check if msg for this veh, and if its a 'message' type
            if (vehicleName.contains(lastStrVal) && pred.contains("message")) {
                vehState.updateText3(newItem);
            } else if (vehicleName.contains(lastStrVal) && pred.contains("beliefcount")) {
                String[] findAgName = pred.split("/");
                String agName = findAgName[findAgName.length - 1];
                String newBeliefStateString = "";
                boolean vehicleJTextExists = false;
                for (VehicleJasonText vehJState : myChildJStates) {
                    if (vehJState.vehicleName.equals(vehicleName)) {
                        ///     System.out.println("Found a vehJState.vehicleName of " + vehJState.vehicleName + " which matches " + vehicleName);
                        vehicleJTextExists = true;
                        int maxPos = vehJState.childAgents.size();
                        int jStatePos = 0;
                        boolean foundChild = false;
                        //lets try and speed things up, as soon as we've found a child we can leave the loop as we'll have updated it
                        while (jStatePos < maxPos && !foundChild) {

                            if (vehJState.childAgents.get(jStatePos).agentName.equals(agName)) {
                                vehJState.childAgents.get(jStatePos).count = newItem;
                                foundChild = true;
                            }
                            jStatePos++;
                        }
                        //we didnt find a child to update, so must be a new data piece.. add it
                        if (!foundChild) {
                            vehJState.addChildPair(agName, newItem);
                        }
                        //we've now updated the string containing belief data for all the child of this veh, so get the string ready for render update
                        newBeliefStateString = vehJState.getChildData();
                    }
                }
                //check if we ever found a jstate object for this vehicle, if not then create new one and populate with jstate belief info just received..
                if (!vehicleJTextExists) {
                    VehicleJasonText newJText = new VehicleJasonText();
                    newJText.vehicleName = vehicleName;
                    newJText.addChildPair(agName, newItem);
                    myChildJStates.add(newJText);
                    newBeliefStateString = newJText.getChildData();
                }
                vehState.updateText4("beliefs: " + newBeliefStateString);
            }
        }
    }

    public void processXMPPCount(String newCount) {
        int newCountVal = Integer.parseInt(newCount);
        xmppCountAlt = xmppCountAlt + newCountVal;
    }

    public void processSimTime(String newTime) {
        currentSimTime = Long.parseLong(newTime);

        //xmppCountAlt = xmppCountAlt + newCountVal;
    }

    public void processInstMessage(String instMessage) {
        System.out.println("processing message from institution: " + instMessage);
        if (instMessage.startsWith("obl(")) {
            String[] oblData = instMessage.split("\\(");
            if (oblData.length == 4) {
                String oblTarget = oblData[2].split("\\)")[0];
                String oblType = oblData[1];
                System.out.println("trying to find " + oblTarget + " to tell " + oblType + " veh list currently " + myVehicleStates.size());

                if (oblType.equals("merge")) {
                    String mergeTarget = oblTarget.split(",")[1];
                    oblTarget = oblTarget.split(",")[0];
                    System.out.println("Tell: " + oblTarget + " and msg is " + instMessage);
                    System.out.println("Tell: " + mergeTarget + " and msg is " + instMessage);

                    for (VehicleState vehState : myVehicleStates) {
                        if ((vehState.vehicleName.contains(oblTarget)) || (vehState.vehicleName.contains(oblTarget))) {
                            System.out.println("updating obligation " + instMessage);
                            vehState.updateText3(instMessage);
                        }
                    }
                }


            } else {
                System.out.println("thought that was an obligation, but its " + oblData.length + " not what expected..");
            }
        } else {
            System.out.println("don't know how to handle msg: " + instMessage);
        }
    }

    public void processXMPPVehicleHealthData(String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        boolean updatedVehicle = false;
        for (VehicleState vehState : myVehicleStates) {
            if (vehState.vehicleName.equals(vehicleName)) {
                System.out.println("updating health state for " + vehicleName + " to " + newItem);
                vehState.updateDamageState(true);
            }

        }
    }

    public void processXMPPVehicleLightData(String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        boolean updatedVehicle = false;
        for (VehicleState vehState : myVehicleStates) {
            if (vehState.vehicleName.equals(vehicleName)) {
                final String[] newData = newItem.split(",");
                boolean turnL = false;
                boolean turnR = false;
                boolean braking = false;
                boolean frontLights = false;
                if (newData.length == 4) {
                    if (newData[0].equals("true")) {
                        turnL = true;
                        // if (vehicleName.endsWith("c1.1")) {System.out.println(vehicleName +"L:"+turnL);}
                    }
                    if (newData[1].equals("true")) {
                        turnR = true;
                        // if (vehicleName.endsWith("c1.1")) {System.out.println(vehicleName +"R:"+turnR);}
                    }
                    if (newData[2].equals("true")) {
                        braking = true;
                    }
                    if (newData[3].equals("true")) {
                        System.out.println("setting lights on");
                        frontLights = true;
                    }
                    //TODO: get front light state, 
                    vehState.updateLightStates(turnL, turnR, braking, frontLights);

                } else {
                    System.out.println("WARNING: some problem with light state..");
                }

                updatedVehicle = true;
            }
        }

    }

    public void processXMPPData(String newItem, String vehicleName) {
        xmppCount = xmppCount + 1;
        // System.out.println("xmpp count is " + xmppCount);
        boolean updatedVehicle = false;
        for (VehicleState vehState : myVehicleStates) {
            if (vehState.vehicleName.equals(vehicleName)) {
                final String[] newData = newItem.split(",");
                if (newData[0].equals("spatial")) {
                    vehState.updateValues(Float.valueOf(newData[1]), Float.valueOf(newData[2]), Float.valueOf(newData[3]), Float.valueOf(newData[4]));
                    Vector3f newPoint = new Vector3f(Float.valueOf(newData[1]), Float.valueOf(newData[2]), Float.valueOf(newData[3]));
                    myKnownPoints.add(newPoint);
                }
                updatedVehicle = true;
            }
        }
        //check if we didn't update any vehicles, in that case we need to add this as a new one
        if (!updatedVehicle) {
            //TODO: probably should put an add method in the vehicle class to do this, not sure nodes are done properly at the moment
            //System.out.println("trying to add new vehicle");
            String[] newData = newItem.split(",");
            if (newData[0].equals("spatial")) {
                Float myH = -Float.valueOf(newData[4]);
                Float myX = Float.valueOf(newData[1]);
                Float myY = Float.valueOf(newData[2]);
                Float myZ = Float.valueOf(newData[3]);
                addNewVehicle(vehicleName, myX, myY, myZ, myH);
            }

        }
    }

    public void XMPPPAlert(String alertVal) {
        System.out.println("main process received XMPP alert " + alertVal);
    }

    public void processTrafficLight(String jID, String lanesList, String colourStates) {
        //break down a list of lanes and their colour states, to update the existing list of lights

        System.out.println(jID + ", " + lanesList + ", " + colourStates);
        String[] newLanes = lanesList.split(",");

        for (int laneVal = 0; laneVal < newLanes.length; laneVal++) {
            String thisLane = newLanes[laneVal];
            String fullID = jID + "/" + thisLane;
            char colourVal = colourStates.charAt(laneVal);

            boolean updatedLane = false;

            System.out.println(laneVal + ": checking if " + fullID + " is in the list yet");
            for (TrafficLight tempLight : myTrafficLights) {
                System.out.println(" does " + tempLight.getID() + " equal " + fullID);
                if (tempLight.getID().equals(fullID)) {
                    // System.out.println(" yes so updating " + fullID);
                    tempLight.setColourState(colourVal);
                    updatedLane = true;
                }
            }

            if (!updatedLane) {
                System.out.println("didnt find " + fullID + " so not updating anything");
                // TrafficLight newLight = new TrafficLight(this,fullID);
                //  myTrafficLights.add(newLight);
                // updateTList(fullID);
            }

        }

        /* for (TrafficLight tempLight : myTrafficLights)
         {
         System.out.println("list has: " + tempLight.getID());
         }*/
    }

    public void processTrafficLight(String lane, char colourState) {
        boolean updatedLane = false;
        for (TrafficLight tempLight : myTrafficLights) {
            //System.out.println(" does " + tempLight.getID() + " equal " + lane);
            if (tempLight.getID().contains(lane)) {
                //System.out.println(" yes so updating " + lane + " to " + colourState);
                tempLight.setColourState(colourState);
                updatedLane = true;
            }
        }

        if (!updatedLane) {
            // System.out.println("didnt find " + lane + " so not updating anything");
        }
    }

    private void addTrafficLights() {
        //TrafficLight newLight = new TrafficLight(this);
        //myTrafficLights.add(newLight);
    }

    private void addSky() {

        Texture west, east, north, south, up, down;
        west = assetManager.loadTexture("Textures/west.png");
        east = assetManager.loadTexture("Textures/east.png");
        north = assetManager.loadTexture("Textures/north.png");
        south = assetManager.loadTexture("Textures/south.png");
        down = assetManager.loadTexture("Textures/bottom.png");
        up = assetManager.loadTexture("Textures/top.png");

        rootNode.attachChild(SkyFactory.createSky(assetManager, west, east, north, south, up, down));
    }

    @Override
    public void destroy() {
        System.out.println("in destroy method");
        stateManager.detach(myVidState);
        System.out.println("detached vid state");
        System.out.println("stopping messaging");
        if (!useNoNet)
        {
            instSensorClient.disconnect();
            mySensorClient.disconnect();
            simNonThreadSender.disconnect();
        }
        super.destroy();


    }

    @Override
    public void simpleUpdate(float tpf) {

	if (houseShapes != null)
	{
		houseShapes.update();
	}

        guiNode.detachAllChildren();

        if (currentSimTime != 0L) {
            //guiNode.detachChildNamed("timeNode");

            timeNode.detachAllChildren();
            BitmapText hudTime = new BitmapText(guiFont, false);
            hudTime.setSize(20);      // font size
            hudTime.setColor(ColorRGBA.Blue);                             // font color
            //hudTime.setText("Sim Time: " + currentSimTime);             // the text
            if (timeFromSUMO) {
                double newTime = ((double) currentSimTime) / ((double) 1000);
                hudTime.setText("Sim Time: " + newTime);
            } else {
                hudTime.setText(formatter.format(currentSimTime));
                if (useSky && sc.isEnabled())
                {
                    String hourTime = formatterHour.format(currentSimTime);
                    String minTime = formatterMin.format(currentSimTime);
                    String dayString = formatterDay.format(currentSimTime);
                    String monthString = formatterMonth.format(currentSimTime);
                    float minComponent = Float.parseFloat(minTime)/60;
                    float hourComponent = Float.parseFloat(hourTime);
                    int monthVal = Integer.parseInt(monthString);
                    int dayVal = Integer.parseInt(dayString);
                    
                    //TODO:figure out the day of the year from the time stamp and set calendar value from that
                    if (!runningAndroid)
                    {
                        sc.getSunAndStars().setHour(hourComponent+minComponent);
                        //System.out.println("date " + monthVal + " " +  dayVal);
                        //sc.getSunAndStars().setSolarLongitude(Integer.parseInt(monthString), Integer.parseInt(dayString));
                        if (sc.getUpdater() != null && houseShapes != null && houseShapes.started())
                        {
                            houseShapes.setLightState(sc.getUpdater().getMainColor(), sc.getUpdater().getBloomIntensity());
                        }
                    }
                }
            }
            hudTime.setLocalTranslation(0, hudTime.getLineHeight() + 450, 300);
            timeNode.attachChild(hudTime);
            guiNode.attachChild(timeNode);

            //generate screenshot if we've moved a sim step
            //System.out.println(previousSimTime + " v " + currentSimTime + " and suMO: " + timeFromSUMO);
               /* if ((previousSimTime != currentSimTime) && timeFromSUMO)
             {
             //System.out.println("saving bmp");
             newGraphHud.addNewPoint(xmppCountAlt, xmppCount, currentSimTime);
             xmppCountAlt=0;
             xmppCount=0;
             // double newTime = ((double)currentSimTime)/((double)1000);
             double newfNum = 1000000+currentSimTime;

             screenshotState.setFilePath(currentPath+"/"+dateTimeVal+"/");

             screenshotState.setFileName(newfNum+"");
             screenshotState.takeScreenshot();

             previousSimTime=currentSimTime;
             }*/

        }

        if (!runningAndroid)
        {
            BufferedImage img = newGraphHud.getBI();
            Quad qd_background = new Quad(160f, 120f);
            Geometry geo_background = new Geometry("Background", qd_background);
            geo_background.setLocalTranslation(450f, 350f, 0.0f);
            Material mat_background = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat_background.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

            Texture2D myTex = new Texture2D();
            AWTLoader awtLoader = new AWTLoader();
            myTex.setImage(awtLoader.load(img, true));

            mat_background.setTexture("ColorMap", myTex); // NULLPOINTER EXCEPTION
            geo_background.setMaterial(mat_background);
            guiNode.attachChild(geo_background);
        }
        
        // guiNode.detachChildNamed("graphNode");
        //Node graphNodeToAdd = new Node();
        //  graphNodeToAdd = myGraphHud.update();
        // guiNode.attachChild(graphNodeToAdd); 

        //AOINode.detachAllChildren();
        for (AOI testAOI : myAOINodes) {
            testAOI.update();
            rootNode.attachChild(testAOI.getSpatial());
        }


        if (nodesToAdd.size() > 0) {
            for (Node newN : nodesToAdd) {
                rootNode.attachChild(newN);
            }
            nodesToAdd.clear();
        }


        if (!timeFromSUMO && (myNanoTimer.getTimeInSeconds() >= 1)) {
            //System.out.println("updating GraphHUD with xmpp count of " + xmppCountAlt + " and old alt method says " + xmppCount);
            // myGraphHud.addNewPoint(xmppCountAlt, xmppCount);
            if (!runningAndroid)
            {
                newGraphHud.addNewPoint(xmppCountAlt, xmppCount);
            }    
            xmppCountAlt = 0;
            xmppCount = 0;
            myNanoTimer.reset();
        }

        //uncomment this to have camera on car
        for (Node addNewNode : newNodes) {
            rootNode.attachChild(addNewNode);
            if (!addedAlready && !addedCamera) {
                flyCam.setEnabled(false);
                currentCamTarget = addNewNode.getName();
                chaseCam = new ChaseCamera(cam, addNewNode.getChild("vehicleVisual"), inputManager);
                chaseCam.setSmoothMotion(true);
                chaseCam.setMaxDistance(100f);
                chaseCam.setChasingSensitivity(2f);
                chaseCam.setRotationSensitivity(0.8f);
                chaseCam.setTrailingRotationInertia(0.4f);
                chaseCam.setTrailingEnabled(true);
                chaseCam.setTrailingSensitivity(0.2f);
                //  chaseCam.setRotationSpeed(0.5f);
                addedCamera = true;
            }


            if (addNewNode.getName().endsWith(followVehicle)) {
                rootNode.getChild(currentCamTarget).removeControl(chaseCam);
                currentCamTarget = addNewNode.getName();
                rootNode.getChild(currentCamTarget).addControl(chaseCam);
            } else {
                //System.out.println(addNewNode.getName() + " is not target");
            }



        }
        newNodes.clear();

        //update traffic light positions/light states for render
        trafficLightNode.detachAllChildren();
        for (TrafficLight currentLight : myTrafficLights) {
            currentLight.update();
            trafficLightNode.attachChild(currentLight.getSpatial());
        }

        for (VehicleState vehState : myVehicleStates) {

            vehState.updateRenderState();
            //TODO: write a method for the VehicleState class to update itself to tidy this up
            /*vehState.label.setText(vehState.newInfo);
             vehState.labelTop.setText(vehState.topBox);
             vehState.labelTopName.setText(vehState.nameBox);
             vehState.labelWayTop.setText(vehState.newWaypointInfo);*/

        }

        //addWaypoint objects to render scene
        for (WayPoint currentWayPoint : wayPointList) {
            Node newWayPointNode = currentWayPoint.getNode();
            if (!debugNode.hasChild(newWayPointNode)) {
                System.out.println("adding new waypoint node to scene");
                debugNode.attachChild(newWayPointNode);
            }
        }

        debugNode.detachChild(routeNode);
        routeNode.detachAllChildren();
        // System.out.println("myKnownPoints.size " + myKnownPoints.size());
        for (int vehPoint = 0; vehPoint < myKnownPoints.size(); vehPoint++) {
            if (vehPoint + 1 < myKnownPoints.size()) {
                Line newVLine = new Line(myKnownPoints.get(vehPoint), myKnownPoints.get(vehPoint + 1));
                newVLine.setLineWidth(3f);
                Geometry geomVNew = new Geometry("", newVLine);
                geomVNew.setMaterial(greenMat);
                routeNode.attachChild(geomVNew);
            }
        }
        debugNode.attachChild(routeNode);

        //TODO: this could be rewritten more optimally.. no time.. :(
        for (VehicleCollisionBox vehBox : myVehCollisionBoxes) {
            Spatial tempNode = rootNode.getChild(vehBox.getVehName());
            if (tempNode != null) {
                //add collision lines into the debug Node, could be useful 
                Vector3f startP = new Vector3f(vehBox.getStart().x, 0.2f, vehBox.getStart().z);
                Vector3f finP = new Vector3f(vehBox.getEnd().x, 0.2f, vehBox.getEnd().z);
                Line newLine = new Line(startP, finP);
                newLine.setLineWidth(3f);
                Geometry geomNew = new Geometry("", newLine);

                geomNew.setMaterial(greenMat);
                debugNode.attachChild(geomNew);

                vehBox.updateRender();
            } else {
                rootNode.attachChild(vehBox.getMyNode());
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public MyPoint2D convertExternalLocation(MyPoint2D oldLoc) {
        Double newX = oldLoc.getY() - sumoOffSetY;
        Double newY = oldLoc.getX() - sumoOffSetX;
        // System.out.println("converted " + oldLoc.getX() +","+ oldLoc.getY() + " to " + newX + "," + newY);
        return new MyPoint2D(newX, newY);
    }
    
    public void addTerrain()
    {
            /** 1. Create terrain material and load four textures into it. */
            mat_terrain = new Material(assetManager, 
                    "Common/MatDefs/Terrain/Terrain.j3md");

            /** 1.1) Add ALPHA map (for red-blue-green c
             * oded splat textures) */
            mat_terrain.setTexture("Alpha", assetManager.loadTexture(
                    "Textures/alphamap.png"));

            /** 1.2) Add GRASS texture into the red layer (Tex1). */
            Texture grass = assetManager.loadTexture(
                    "Textures/grass.jpg");
            grass.setWrap(WrapMode.Repeat);
            mat_terrain.setTexture("Tex1", grass);
            mat_terrain.setFloat("Tex1Scale", 64f);

            /** 1.3) Add DIRT texture into the green layer (Tex2) */
            Texture dirt = assetManager.loadTexture(
                    "Textures/dirt.jpg");
            dirt.setWrap(WrapMode.Repeat);
            mat_terrain.setTexture("Tex2", dirt);
            mat_terrain.setFloat("Tex2Scale", 32f);

            /** 1.4) Add ROAD texture into the blue layer (Tex3) */
            Texture rock = assetManager.loadTexture(
                    "Textures/road.jpg");
            rock.setWrap(WrapMode.Repeat);
            mat_terrain.setTexture("Tex3", rock);
            mat_terrain.setFloat("Tex3Scale", 128f);

            /** 2. Create the height map */
            AbstractHeightMap heightmap = null;
            Texture heightMapImage = assetManager.loadTexture(
                    "Textures/mountains512.png");
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
            heightmap.setHeightScale(0.000001f);
            heightmap.load();

            

            /** 3. We have prepared material and heightmap. 
             * Now we create the actual terrain:
             * 3.1) Create a TerrainQuad and name it "my terrain".
             * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
             * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
             * 3.4) As LOD step scale we supply Vector3f(1,1,1).
             * 3.5) We supply the prepared heightmap itself.
             */
            int patchSize = 65;
            terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());

            /** 4. We give the terrain its material, position & scale it, and attach it. */
            terrain.setMaterial(mat_terrain);
            terrain.setLocalTranslation(0, 8.8f, 0);
            terrain.setLocalScale(2f, 1f, 2f);
            rootNode.attachChild(terrain);

            /** 5. The LOD (level of detail) depends on were the camera is: */
            TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
            terrain.addControl(control);
    }
    
    public Main getSelf() {
        return this;
    }
   
}
