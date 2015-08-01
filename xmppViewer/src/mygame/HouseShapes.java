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
import static mygame.Main.PITCH180;

/**
 *
 * @author vin
 */
public class HouseShapes {

    private Main parentHandle;
    
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
        loungeModel.setLocalRotation(PITCH180);
        loungeModel.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        loungeModel.setLocalScale(1f, 1f, 1.0f);
        //something up with this.. 
        //loungeModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node loungeNode = new Node();
        loungeNode.attachChild(loungeModel);
        Spatial terrainModel2 = assetMan.loadModel("Models/houseMainbed2.obj");
        terrainModel2.setLocalRotation(PITCH180);
        terrainModel2.setLocalTranslation(parentHandle.terrainXtrans, parentHandle.terrainZtrans, parentHandle.terrainYtrans);
        terrainModel2.setLocalScale(1f, 1f, 1.0f);
        
        Node mainBedNode = new Node();
        mainBedNode.attachChild(terrainModel2);
            
            //flyCam.setEnabled(true);
            PointLight lamp_light = new PointLight();
            lamp_light.setColor(ColorRGBA.Yellow);
            lamp_light.setRadius(100f);
            lamp_light.setPosition(new Vector3f(20f,20f,20f));
           // loungeNode.addLight(lamp_light);
            
             
            Material bulbYel = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            bulbYel.setColor("Color", ColorRGBA.Yellow);
            bulbYel.setColor("GlowColor", ColorRGBA.Yellow);
            
          /*  SpotLight spotKitchen1 = new SpotLight();
            spotKitchen1.setSpotRange(30f);                           // distance
            spotKitchen1.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
            spotKitchen1.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
            spotKitchen1.setColor(ColorRGBA.White.mult(1.3f));         // light color
            spotKitchen1.setPosition(new Vector3f(-1.9f, 11f, 2.5f));               // shine from camera loc
            spotKitchen1.setDirection(new Vector3f(0.25208452f, -0.9664893f, 0.04849559f));             // shine forward from camera loc
            loungeNode.addLight(spotKitchen1);*/
            
            Vector3f kitchenBulb1Loc = new Vector3f(-2.7f, 11f, 3.1f);
            Sphere bulb2 = new Sphere(3, 3, 0.105f);
            Geometry yelgeo = new Geometry("yel", bulb2);
            yelgeo.setMaterial(bulbYel);
            yelgeo.setLocalTranslation(kitchenBulb1Loc);
            yelgeo.setShadowMode(RenderQueue.ShadowMode.Off);
            //loungeNode.attachChild(yelgeo);          
            SpotLight spotKitchen2 = new SpotLight();
            spotKitchen2.setSpotRange(30f);                           // distance
            spotKitchen2.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
            spotKitchen2.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
            spotKitchen2.setColor(ColorRGBA.White.mult(1.3f));         // light color
            spotKitchen2.setPosition(kitchenBulb1Loc);               // shine from camera loc
            spotKitchen2.setDirection(new Vector3f(0.0010834784f, -0.9736858f, 0.22789204f));             // shine forward from camera loc
            loungeNode.addLight(spotKitchen2);
            
           /* SpotLight spotKitchen3 = new SpotLight();
            spotKitchen3.setSpotRange(30f);                           // distance
            spotKitchen3.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
            spotKitchen3.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
            spotKitchen3.setColor(ColorRGBA.White.mult(1.3f));         // light color
            spotKitchen3.setPosition(new Vector3f(-2.7f, 11.2f, 3.1f));               // shine from camera loc
            spotKitchen3.setDirection(new Vector3f(-0.31732267f, -0.9135653f, -0.2543713f));             // shine forward from camera loc
            loungeNode.addLight(spotKitchen3);*/
            
            final int SHADOWMAP_SIZE=1024;
        //    SpotLightShadowRenderer slsr1 = new SpotLightShadowRenderer(assetManager, SHADOWMAP_SIZE);
        //    slsr1.setLight(spotKitchen1);
        //    viewPort.addProcessor(slsr1);
           SpotLightShadowRenderer slsr2 = new SpotLightShadowRenderer(assetMan, SHADOWMAP_SIZE);
            slsr2.setLight(spotKitchen2);
            parentHandle.getViewPort().addProcessor(slsr2);
      /*      SpotLightShadowRenderer slsr3 = new SpotLightShadowRenderer(assetManager, SHADOWMAP_SIZE);
            slsr3.setLight(spotKitchen3);
            viewPort.addProcessor(slsr3);*/
           
            
            
            //(-2.1505713, 1.6154995,
            
            //chaseCam = new ChaseCamera(cam, terrainModel, inputManager);
            //chaseCam.setSmoothMotion(true);
            Vector3f v = parentHandle.getCamera().getLocation();
            Vector3f moveL = new Vector3f(v.x, v.y+20, v.z);
            parentHandle.getCamera().setLocation(moveL);
            parentHandle.getRootNode().attachChild(loungeNode);
            parentHandle.getRootNode().attachChild(mainBedNode);
            
            //lightScene();
            
    }
    
    
    private void lightScene()
    {
            AmbientLight al = new AmbientLight();
            al.setColor(ColorRGBA.White.mult(0.3f));
            parentHandle.getRootNode().addLight(al);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            parentHandle.getRootNode().addLight(sun);

    }
   
}
