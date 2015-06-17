/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;


/**
 *
 * @author vin
 */
public class AOI {
    
    private Spatial aoiSpatial;
    private Node aoiNode;
    private static final Quaternion PITCH270 = new Quaternion().fromAngleAxis(FastMath.PI*3/2, new Vector3f(1,0,0));
    private Geometry cylgeo;
    private String myID;
    private Vector3f holderPosition = new Vector3f(0f,0f,0f);
    private float currentRadius =10f;
    
    public AOI(String idName)
    {
        myID= idName;
    }
        
    public AOI(Main parentHandle, String idName)
    {
        myID = idName;
        aoiNode = new Node();

        Cylinder myCylinder =  new Cylinder(50,50,1f,2f,true);
        Material grey = new Material(parentHandle.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        grey.setColor("Color", new ColorRGBA(255f,0f,0f, 0.4f));
        //grey.setColor("Color", ColorRGBA.Gray);
        grey.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        cylgeo = new Geometry("cylinder", myCylinder);
        cylgeo.setMaterial(grey);
        cylgeo.setLocalScale(1.0f, 1.0f, 1.0f);
        cylgeo.setLocalRotation(PITCH270);
        cylgeo.setQueueBucket(Bucket.Transparent); 
        //aoiNode.attachChild(cylgeo);
    }
    
               
    public Spatial getSpatial()
    {
        return aoiNode;
    }
    
    public String getID()
    {
        return myID;
    }
    
    public void setRadius(float newRad)
    {
        currentRadius = newRad;
    }
    
    public void setPosition(float x1, float y1, float z1)
    {
       // System.out.println("AOI obj set position");
        holderPosition.x = x1;
        holderPosition.y = y1;
        holderPosition.z = z1;
    }
    
    public void update()
    {
       // System.out.println("called update");
        aoiNode.setLocalTranslation(holderPosition); 
        aoiNode.setLocalScale(currentRadius, currentRadius, 20.0f);
    }
}
