/*
 * To change this template, choose Tools | Templates
 * and open t   he template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Dome;
import java.io.File;

/**
 *
 * @author vin
 */
public class HouseShapes {

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
    private Main parentHandle;
    private SpotLight spotKitchen1, spotKitchen2, spotKitchen3, spotBath1, spotBath2, spotBath3;
    private PointLight hall_light1, kitchen_lamp, lounge_mainlamp, lounge_sidelamp1, lounge_sidelamp2, hall_light2, bed_light1, bed_sidelamp1, bed2_light1;
    private CopyOnWriteArrayList<LightInfo> myLights = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<DetectionZone> detectionZones = new CopyOnWriteArrayList();
    private AmbientLight al = new AmbientLight();
    DirectionalLight dl = new DirectionalLight();
    private boolean initValue=false;
    private boolean addedSceneLight=false;
    private static Gauge room2TempGauge;
    private static AssetManager assetMan;
    private Node loungeNode = new Node();
    private Node bathNode = new Node();
    private Node mainBedNode = new Node();
    private Node hallNode = new Node();
    private Node secondBedNode = new Node();
    private boolean regenModels=false;
    private boolean runningAndroid=false;
    

    
    public HouseShapes(Main pHandle, boolean isAndroid)
    {
        parentHandle=pHandle;
        runningAndroid=isAndroid;
        assetMan = parentHandle.getAssetManager();
        System.out.println("adding house");
        setupRooms();
            
            if (!runningAndroid)
            {
                room2TempGauge = new Gauge("Room 2 Temperature");
            }
            setupDetectionZones();
           
          //  Vector3f v = parentHandle.getCamera().getLocation();
          //  Vector3f moveL = new Vector3f(v.x, v.y+20, v.z);
            parentHandle.getCamera().setLocation(new Vector3f(-2.310954f, 23.39234f, 11.726002f));
            parentHandle.getCamera().setRotation(new Quaternion(0.0f, 0.85316384f, -0.5216431f, 0.0f));
            parentHandle.getRootNode().attachChild(loungeNode);
            parentHandle.getRootNode().attachChild(mainBedNode);
	    parentHandle.getRootNode().attachChild(secondBedNode);
	    parentHandle.getRootNode().attachChild(hallNode);
	    parentHandle.getRootNode().attachChild(bathNode);
            
            if (!addedSceneLight)
            {
                dl.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
                parentHandle.getRootNode().addLight(dl);
                addedSceneLight=true;
            }
            
            //lightScene();
            initValue=true;
            
    }
            
    public void randomizeLights()
    {
	    for (LightInfo tempLight : myLights)
	    {
		Random rand = new Random();
		ColorRGBA newCol = new ColorRGBA(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0f);
		tempLight.updateInfo(newCol, 1f);
	    }
    }

    protected void update()
    {
	    for (LightInfo tempLight : myLights)
	    {
		tempLight.updateState();
	    }
            
            for (DetectionZone testZone : detectionZones)
            {
                testZone.update();
            }
            
            if (!runningAndroid)
            {
                secondBedNode.detachChildNamed("Gauge");
                BufferedImage img = room2TempGauge.getBI();
                Quad qd_background = new Quad(5f, 3f);
                Geometry geo_gauge = new Geometry("Gauge", qd_background);
                geo_gauge.setLocalTranslation(-10f,11f, 5.0f);
                Material mat_background = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
                mat_background.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

                Texture2D myTex = new Texture2D();
                AWTLoader awtLoader = new AWTLoader();
                myTex.setImage(awtLoader.load(img, true));

                mat_background.setTexture("ColorMap", myTex); // NULLPOINTER EXCEPTION
                geo_gauge.setMaterial(mat_background);
                geo_gauge.setQueueBucket(Bucket.Transparent);
                BillboardControl billboard = new BillboardControl();
                geo_gauge.addControl(billboard);
                secondBedNode.attachChild(geo_gauge);
            }
    }  

    public void updateLight(String name, ColorRGBA colour, float intensity)
    {
	//System.out.println("trying to update state of " + name + " to " + colour.getRed() + " intensity " + intensity);
	for (LightInfo tempLight : myLights)
	{
		if (tempLight.getName().equals(name))
		{
			//System.out.println("found it!");
			tempLight.updateInfo(colour, intensity);
		}
	}
    }  
    
    public boolean started()
    {
        return initValue;
    }
    
    protected void setLightState(ColorRGBA sceneCol, float bloom)
    {
        if (addedSceneLight)
        {
            if (bloom == 0.0f)
             {
                // System.out.println("set light level for night-time");
                 al.setColor(sceneCol.mult(0.3f));
                 dl.setColor(sceneCol.mult(0.3f));
             }
             else
             {
                // System.out.println("daytime! bloom: " + bloom);
                 al.setColor(sceneCol.mult(bloom/2));
                 dl.setColor(sceneCol.mult(bloom/2));
             }
        }
    }
    
    private void setupDetectionZones()
    {
            DetectionZone bedroom2Zone = new DetectionZone(new Vector3f(0.5f, 5f,0.5f), "http://127.0.0.1/components/houseSensors/enlitenSensor1", assetMan);
            parentHandle.getRootNode().attachChild(bedroom2Zone.getGeometry());
            detectionZones.add(bedroom2Zone);
    }

    private void lightScene()
    {

           // DirectionalLight sun2 = new DirectionalLight();
           // sun2.setDirection(new Vector3f(0.5f, 0.5f, 0.5f).normalizeLocal());
           // parentHandle.getRootNode().addLight(sun);

    }
    
    protected void newMovementDetection(String takenBy)
    {
        System.out.println("new movement");
        for (DetectionZone testZone : detectionZones)
        {
            if (testZone.getZoneName().equals(takenBy))
            {
                testZone.newDetection();
            }
        }
    }
    
    private void setupRooms()
    {
        Material mat1 = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        
        parentHandle.terrainXscale = 1f;
        parentHandle.terrainYscale = 1f;
        parentHandle.terrainXtrans = 0f;
        parentHandle.terrainYtrans = 0f;
        parentHandle.terrainZtrans = 9f;

        Spatial loungeModel = assetMan.loadModel("Models/houseLoung2.j3o");
        loungeModel.setLocalRotation(PITCH270);
        loungeModel.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        loungeModel.setLocalScale(1f, 1f, 1.0f);
        //something up with this.. 
        //loungeModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        loungeNode.attachChild(loungeModel);

        Spatial terrainModelbed1 = assetMan.loadModel("Models/houseMainbed2.j3o");
        terrainModelbed1.setLocalRotation(PITCH270);
        terrainModelbed1.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelbed1.setLocalScale(1f, 1f, 1.0f);
	//terrainModelbed1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        mainBedNode.attachChild(terrainModelbed1);

        Spatial terrainModelHall = assetMan.loadModel("Models/houseHallway2.j3o");
        terrainModelHall.setLocalRotation(PITCH270);
        terrainModelHall.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelHall.setLocalScale(1f, 1f, 1.0f);
	//terrainModelHall.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        hallNode.attachChild(terrainModelHall);

        Spatial terrainModelBath = assetMan.loadModel("Models/houseBathroom2.j3o");
        terrainModelBath.setLocalRotation(PITCH270);
        terrainModelBath.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelBath.setLocalScale(1f, 1f, 1.0f);
	//terrainModelBath.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        bathNode.attachChild(terrainModelBath);

        Spatial terrainModelbed2 = assetMan.loadModel("Models/houseSecondroom3.j3o");
        terrainModelbed2.setLocalRotation(PITCH270);
        terrainModelbed2.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelbed2.setLocalScale(1f, 1f, 1.0f);
	//terrainModelbed2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        secondBedNode.attachChild(terrainModelbed2);
        
        if (regenModels)
        {
            genModel(loungeModel, "houseLoung2.j3o");
            genModel(terrainModelbed1, "houseMainbed2.j3o");
            genModel(terrainModelHall, "houseHallway2.j3o");
            genModel(terrainModelBath, "houseBathroom2.j3o");
            genModel(terrainModelbed2, "houseSecondroom3.j3o");
        }
            
        hall_light1 = new PointLight();
	LightInfo hall1 = new LightInfo("Light_GF_Hallway_Ceiling1", hall_light1, ColorRGBA.White, 0.7f);
	myLights.add(hall1);
        hall_light1.setRadius(10f);
        hall_light1.setPosition(new Vector3f(0f,11f,6.8f));
	hallNode.addLight(hall_light1);

        hall_light2 = new PointLight();
	LightInfo hall2 = new LightInfo("Light_GF_Hallway_Ceiling2", hall_light2, ColorRGBA.White, 0.7f);
	myLights.add(hall2);
        hall_light2.setRadius(10f);
        hall_light2.setPosition(new Vector3f(-1.6354923f, 11f, 7.096022f));
	hallNode.addLight(hall_light2);

        bed_light1 = new PointLight();
	LightInfo bed1_l1 = new LightInfo("Light_GF_Bedroom1_Ceiling", bed_light1, ColorRGBA.White, 1f);
	myLights.add(bed1_l1);
        bed_light1.setRadius(10f);
        bed_light1.setPosition(new Vector3f(-2.96869f, 11f, 5.284356f));
	mainBedNode.addLight(bed_light1);

        bed_sidelamp1 = new PointLight();
	LightInfo bed1_l2 = new LightInfo("Light_GF_Bedroom1_Sidelight1", bed_sidelamp1, ColorRGBA.Red, 0.5f);
	myLights.add(bed1_l2);
        bed_sidelamp1.setRadius(5f);
        bed_sidelamp1.setPosition(new Vector3f(-1.528491f, 10.158647f, 4.390631f));
        mainBedNode.addLight(bed_sidelamp1);

        bed2_light1 = new PointLight();
	LightInfo bed2_l1 = new LightInfo("Light_GF_Bedroom2_Ceiling", bed2_light1, ColorRGBA.White, 1f);
	myLights.add(bed2_l1);
        bed2_light1.setRadius(10f);
        bed2_light1.setPosition(new Vector3f(-4.053558f, 11f, 7.932183f));
	secondBedNode.addLight(bed2_light1);

        kitchen_lamp = new PointLight();
	LightInfo kitchen_l1 = new LightInfo("Light_GF_Lounge_Lamp1", kitchen_lamp, ColorRGBA.White, 0.5f);
	myLights.add(kitchen_l1);
        kitchen_lamp.setRadius(10f);
        kitchen_lamp.setPosition(new Vector3f(-3.2279096f, 10.3579855f, 2.553022f));
        loungeNode.addLight(kitchen_lamp);

        lounge_mainlamp = new PointLight();
	LightInfo lounge_l1 = new LightInfo("Light_GF_Lounge_Ceiling", lounge_mainlamp, ColorRGBA.White, 0.8f);
	myLights.add(lounge_l1);
        lounge_mainlamp.setRadius(10f);
        lounge_mainlamp.setPosition(new Vector3f(-1.1730436f, 11.600092f, 1.3343358f));
        loungeNode.addLight(lounge_mainlamp);

        lounge_sidelamp1 = new PointLight();
	LightInfo lounge_s1 = new LightInfo("Light_GF_Lounge_Side1", lounge_sidelamp1, ColorRGBA.Red, 0.5f);
	myLights.add(lounge_s1);
        lounge_sidelamp1.setRadius(5f);
        lounge_sidelamp1.setPosition(new Vector3f(-0.1043277f, 9.20818f, 0.121054f));
        loungeNode.addLight(lounge_sidelamp1);

        lounge_sidelamp2 = new PointLight();
	LightInfo lounge_s2 = new LightInfo("Light_GF_Lounge_Side2", lounge_sidelamp2, ColorRGBA.Red, 0.5f);
	myLights.add(lounge_s2);
        lounge_sidelamp2.setRadius(5f);
        lounge_sidelamp2.setPosition(new Vector3f(-3.6860095f, 9.198965f, 0.39722221f));
        loungeNode.addLight(lounge_sidelamp2);

        spotBath1 = new SpotLight();
	LightInfo spotBath_l1 = new LightInfo("Light_GF_Bathroom_SpotLight1", spotBath1, ColorRGBA.White, 0.7f);
	myLights.add(spotBath_l1);
        spotBath1.setSpotRange(10f);                           // distance
        spotBath1.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotBath1.setSpotOuterAngle(90f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotBath1.setPosition(new Vector3f(-2.2815683f, 11f, 8.666689f));               // shine from camera loc
        spotBath1.setDirection(new Vector3f(-0.29196092f, -0.95503396f, 0.05166161f));             // shine forward from camera loc
        bathNode.addLight(spotBath1);

        spotBath2 = new SpotLight();
	LightInfo spotBath_l2 = new LightInfo("Light_GF_Bathroom_SpotLight2", spotBath2, ColorRGBA.White, 0.7f);
	myLights.add(spotBath_l2);
        spotBath2.setSpotRange(10f);                           // distance
        spotBath2.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotBath2.setSpotOuterAngle(90f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotBath2.setPosition(new Vector3f(-2.2815683f, 11f, 8.666689f));               // shine from camera loc
        spotBath2.setDirection(new Vector3f(-0.12450379f, -0.99057317f, 0.05712527f));             // shine forward from camera loc
        bathNode.addLight(spotBath2);

        spotBath3 = new SpotLight();
	LightInfo spotBath_l3 = new LightInfo("Light_GF_Bathroom_SpotLight3", spotBath3, ColorRGBA.White, 0.7f);
	myLights.add(spotBath_l3);
        spotBath3.setSpotRange(10f);                           // distance
        spotBath3.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotBath3.setSpotOuterAngle(90f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotBath3.setColor(ColorRGBA.White.mult(0.7f));         // light color
        spotBath3.setPosition(new Vector3f(-2.2815683f, 11f, 8.666689f));               // shine from camera loc
        spotBath3.setDirection(new Vector3f(0.099721655f, -0.9747329f, 0.1998784f));             // shine forward from camera loc
        bathNode.addLight(spotBath3);
                
        Material bulbYel = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        bulbYel.setColor("Color", ColorRGBA.Yellow);
        bulbYel.setColor("GlowColor", ColorRGBA.Yellow);
            
        spotKitchen1 = new SpotLight();
	LightInfo spotKitchen_l1 = new LightInfo("Light_GF_Kitchen_SpotLight1", spotKitchen1, ColorRGBA.White, 0.8f);
	myLights.add(spotKitchen_l1);
        spotKitchen1.setSpotRange(30f);                           // distance
        spotKitchen1.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotKitchen1.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotKitchen1.setPosition(new Vector3f(-1.9f, 11f, 2.5f));               // shine from camera loc
        spotKitchen1.setDirection(new Vector3f(0.25208452f, -0.9664893f, 0.04849559f));             // shine forward from camera loc
        loungeNode.addLight(spotKitchen1);
            
        Vector3f kitchenBulb1Loc = new Vector3f(-2.7f, 11f, 3.1f);
        Sphere bulb2 = new Sphere(3, 3, 0.105f);
        Geometry yelgeo = new Geometry("yel", bulb2);
        yelgeo.setMaterial(bulbYel);
        yelgeo.setLocalTranslation(kitchenBulb1Loc);
        yelgeo.setShadowMode(RenderQueue.ShadowMode.Off);
        //loungeNode.attachChild(yelgeo);      
    
        spotKitchen2 = new SpotLight();
	LightInfo spotKitchen_l2 = new LightInfo("Light_GF_Kitchen_SpotLight2", spotKitchen2, ColorRGBA.White, 0.8f);
	myLights.add(spotKitchen_l2);
        spotKitchen2.setSpotRange(30f);                           // distance
        spotKitchen2.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotKitchen2.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotKitchen2.setPosition(kitchenBulb1Loc);               // shine from camera loc
        spotKitchen2.setDirection(new Vector3f(0.0010834784f, -0.9736858f, 0.22789204f));             // shine forward from camera loc
        loungeNode.addLight(spotKitchen2);
            
        spotKitchen3 = new SpotLight();
	LightInfo spotKitchen_l3 = new LightInfo("Light_GF_Kitchen_SpotLight3", spotKitchen3, ColorRGBA.White, 0.8f);
	myLights.add(spotKitchen_l3);
        spotKitchen3.setSpotRange(30f);                           // distance
        spotKitchen3.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spotKitchen3.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spotKitchen3.setPosition(new Vector3f(-2.7f, 11.2f, 3.1f));               // shine from camera loc
        spotKitchen3.setDirection(new Vector3f(-0.31732267f, -0.9135653f, -0.2543713f));             // shine forward from camera loc
        loungeNode.addLight(spotKitchen3);
            
        final int SHADOWMAP_SIZE=1024;
        SpotLightShadowRenderer slsr1 = new SpotLightShadowRenderer(assetMan, SHADOWMAP_SIZE);
        slsr1.setLight(spotKitchen1);
        parentHandle.getViewPort().addProcessor(slsr1);
        SpotLightShadowRenderer slsr2 = new SpotLightShadowRenderer(assetMan, SHADOWMAP_SIZE);
        slsr2.setLight(spotKitchen2);
        parentHandle.getViewPort().addProcessor(slsr2);
        SpotLightShadowRenderer slsr3 = new SpotLightShadowRenderer(assetMan, SHADOWMAP_SIZE);
        slsr3.setLight(spotKitchen3);
        parentHandle.getViewPort().addProcessor(slsr3);
    }
    
    protected void updateTemp(String takenBy, Double temperature)
    {
        if (takenBy.equals("http://127.0.0.1/components/houseSensors/piSensor1"))
        {
            //System.out.println("setting to " + temperature);
            if (!runningAndroid)
            {
                room2TempGauge.setValue(temperature);
            }
        }
    }
    
    private void genModel(Spatial saveM, String modelName)
    {
        BinaryExporter exporter = BinaryExporter.getInstance();
        File outputF = new File(modelName);
        try { 
              System.out.println("trying to save " + modelName + " as j3o");
              exporter.save(saveM,outputF);
        }
        catch (Exception e) {
              e.printStackTrace();
        }
    
    }
}
