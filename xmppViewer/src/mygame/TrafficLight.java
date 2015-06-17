/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;


/**
 *
 * @author vin
 */
public class TrafficLight {
    

    private Spatial trafficLight;
    private Node trafficNode;
    private static final Quaternion PITCH270 = new Quaternion().fromAngleAxis(FastMath.PI*3/2, new Vector3f(1,0,0));
    private Geometry redgeo,yelgeo,greengeo;
    private String myID;
    private Vector3f holderPosition = new Vector3f(0f,0f,0f);
    private char myColour='o';
    private float orientation = 0.0f;
    private Quaternion rotAngle = new Quaternion();
    
    
    public TrafficLight(Main parentHandle, String idName)
    {
        myID = idName;
        trafficNode = new Node();
        //trafficNode.setLocalTranslation(-4.0f, -1.0f, 33.0f);
              
        trafficLight = parentHandle.getAssetManager().loadModel("Models/trafficL.j3o");
        trafficLight.setLocalRotation(PITCH270);
        trafficLight.setLocalScale(0.013f);

        
        Sphere greenp = new Sphere(16, 16, 0.105f);
        Material green2 = new Material(parentHandle.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        green2.setColor("Color", ColorRGBA.Green);
        greengeo = new Geometry("green", greenp);
        greengeo.setMaterial(green2);
        greengeo.setLocalTranslation(-0.33f, 2.49f, -0.05f);
        green2.setColor("GlowColor", ColorRGBA.Green);
        greengeo.setLocalScale(0.1f, 1.0f, 1.0f);

        Sphere yelp = new Sphere(16, 16, 0.105f);
        Material yel2 = new Material(parentHandle.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        yel2.setColor("Color", ColorRGBA.Yellow);
        yelgeo = new Geometry("yel", yelp);
        yelgeo.setMaterial(yel2);
        yelgeo.setLocalTranslation(-0.33f, 2.89f, -0.05f);
        yel2.setColor("GlowColor", ColorRGBA.Yellow);
        yelgeo.setLocalScale(0.1f, 1.0f, 1.0f);
        
        Sphere redp = new Sphere(16, 16, 0.105f);
        Material red2 = new Material(parentHandle.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        red2.setColor("Color", ColorRGBA.Red);
        redgeo = new Geometry("red", redp);
        redgeo.setMaterial(red2);
        redgeo.setLocalTranslation(-0.33f, 3.29f, -0.05f);
        red2.setColor("GlowColor", ColorRGBA.Red);
        redgeo.setLocalScale(0.1f, 1.0f, 1.0f);

        trafficNode.attachChild(trafficLight);
        //trafficNode.attachChild(redgeo);
        //trafficNode.attachChild(yelgeo);
        //trafficNode.attachChild(greengeo);
    
    }
               
    public Spatial getSpatial()
    {
        return trafficNode;
    }
    
    public String getID()
    {
        return myID;
    }
    
    public void setPosition(float x1, float y1, float z1)
    {
        holderPosition.x = x1;
        holderPosition.y = y1;
        holderPosition.z = z1;
    }
    
    public void setAngle(float h)
    { 
        orientation = h;
        Float rotRads = h*FastMath.DEG_TO_RAD;
        rotAngle.fromAngles(0, rotRads, 0);
    }
    
    public void setColourState(char s)
    {
        myColour = s;
    }
    
    public void update()
    {
        trafficNode.setLocalTranslation(holderPosition); 
        trafficNode.setLocalRotation(rotAngle);
        if (myColour == 'o')
        {
           // System.out.println("lights are off");
        }
        else if (myColour == 'g')
        {
            trafficNode.detachChild(redgeo);
            trafficNode.detachChild(yelgeo);
            trafficNode.attachChild(greengeo);
        }      
        else if (myColour == 'r')
        {
            trafficNode.attachChild(redgeo);
            trafficNode.detachChild(yelgeo);
            trafficNode.detachChild(greengeo);
        }
        else if (myColour == 'R')
        {
            trafficNode.attachChild(redgeo);
            trafficNode.attachChild(yelgeo);
            trafficNode.detachChild(greengeo);
        }
        else if (myColour == 'y')
        {
            trafficNode.detachChild(redgeo);
            trafficNode.attachChild(yelgeo);
            trafficNode.detachChild(greengeo);
        }
    }
}
