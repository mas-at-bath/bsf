/*
 * To change this template, choose Tools | Templates
 * and open t   he template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

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
    
    public HouseShapes(Main pHandle)
    {
        parentHandle=pHandle;
        AssetManager assetMan = parentHandle.getAssetManager();
        System.out.println("adding house");
        Material mat1 = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        
        parentHandle.terrainXscale = 1f;
        parentHandle.terrainYscale = 1f;
        parentHandle.terrainXtrans = 0f;
        parentHandle.terrainYtrans = 0f;
        parentHandle.terrainZtrans = 9f;

        Spatial loungeModel = assetMan.loadModel("Models/houseLoung2.obj");
        loungeModel.setLocalRotation(PITCH270);
        loungeModel.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        loungeModel.setLocalScale(1f, 1f, 1.0f);
        //something up with this.. 
        //loungeModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node loungeNode = new Node();
        loungeNode.attachChild(loungeModel);

        Spatial terrainModelbed1 = assetMan.loadModel("Models/houseMainbed2.obj");
        terrainModelbed1.setLocalRotation(PITCH270);
        terrainModelbed1.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelbed1.setLocalScale(1f, 1f, 1.0f);
	//terrainModelbed1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node mainBedNode = new Node();
        mainBedNode.attachChild(terrainModelbed1);

        Spatial terrainModelHall = assetMan.loadModel("Models/houseHallway2.obj");
        terrainModelHall.setLocalRotation(PITCH270);
        terrainModelHall.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelHall.setLocalScale(1f, 1f, 1.0f);
	//terrainModelHall.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node hallNode = new Node();
        hallNode.attachChild(terrainModelHall);

        Spatial terrainModelBath = assetMan.loadModel("Models/houseBathroom2.obj");
        terrainModelBath.setLocalRotation(PITCH270);
        terrainModelBath.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelBath.setLocalScale(1f, 1f, 1.0f);
	//terrainModelBath.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node bathNode = new Node();
        bathNode.attachChild(terrainModelBath);

        Spatial terrainModelbed2 = assetMan.loadModel("Models/houseSecondroom3.obj");
        terrainModelbed2.setLocalRotation(PITCH270);
        terrainModelbed2.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModelbed2.setLocalScale(1f, 1f, 1.0f);
	//terrainModelbed2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node secondBedNode = new Node();
        secondBedNode.attachChild(terrainModelbed2);
            
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
           
            Vector3f v = parentHandle.getCamera().getLocation();
            Vector3f moveL = new Vector3f(v.x, v.y+20, v.z);
            parentHandle.getCamera().setLocation(moveL);
            parentHandle.getRootNode().attachChild(loungeNode);
            parentHandle.getRootNode().attachChild(mainBedNode);
	    parentHandle.getRootNode().attachChild(secondBedNode);
	    parentHandle.getRootNode().attachChild(hallNode);
	    parentHandle.getRootNode().attachChild(bathNode);
            
            //lightScene();
            
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

    protected void updateLightState()
    {
	    for (LightInfo tempLight : myLights)
	    {
		tempLight.updateState();
	    }
    }  

    public void updateLight(String name, ColorRGBA colour, float intensity)
    {
	System.out.println("trying to update state of " + name + " to " + colour.getRed() + " intensity " + intensity);
	for (LightInfo tempLight : myLights)
	{
		if (tempLight.getName().equals(name))
		{
			System.out.println("found it!");
			tempLight.updateInfo(colour, intensity);
		}
	}
    }  

    private void lightScene()
    {
            AmbientLight al = new AmbientLight();
            al.setColor(ColorRGBA.White.mult(0.8f));
            parentHandle.getRootNode().addLight(al);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            parentHandle.getRootNode().addLight(sun);

           // DirectionalLight sun2 = new DirectionalLight();
           // sun2.setDirection(new Vector3f(0.5f, 0.5f, 0.5f).normalizeLocal());
           // parentHandle.getRootNode().addLight(sun);

    }
   
}
