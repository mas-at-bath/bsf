/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Dome;

public class DetectionZone
{
 
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
    private Dome enliten1Detection;
    private Geometry detgeom2;
    private AssetManager assetMan;
    private String zoneName;
    private ColorRGBA newColour= new ColorRGBA(10f,10f,10f, 0.0f);
    private boolean newDetection = false;
    private float alphaVal = 0f;
    
     public DetectionZone(Vector3f location, String name, AssetManager asset)
     {
            assetMan = asset;
            zoneName = name;
            
            Material greyMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            greyMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            greyMat.setColor("Color", newColour);
        
            enliten1Detection =  new Dome(Vector3f.ZERO, 2, 32, 1f,false); 
            detgeom2 = new Geometry("", enliten1Detection);
            detgeom2.setLocalTranslation(-0f, 10, 7.2f );
            detgeom2.setLocalScale(new Vector3f(0.5f, 5f,0.5f));
            detgeom2.rotate(PITCH090);
            detgeom2.rotate(ROLL090);
            detgeom2.setQueueBucket(Bucket.Transparent);
            detgeom2.setMaterial(greyMat);
         //   detgeom2.getMaterial().setColor("Color", new ColorRGBA(100f,10f,10f, 0.6f));
            
            //parentHandle.getRootNode().attachChild(detgeom2);
    }
     
    private void reduceVisibility()
    {
        if (alphaVal >= 1f)
        {
            alphaVal--;
            detgeom2.getMaterial().setColor("Color", new ColorRGBA(newColour.r,newColour.g,newColour.b,alphaVal));
        }
    }
     
    public void update()
    {
        if (newDetection)
        {
            float initialAlpha = 1f;
            detgeom2.getMaterial().setColor("Color", new ColorRGBA(newColour.r,newColour.g,newColour.b,initialAlpha));
            alphaVal = initialAlpha;
            newDetection=false;
        }
        else
        {
            reduceVisibility();
        }
    }
    
    public String getZoneName()
    {
        return zoneName;
    }
     
    public Geometry getGeometry()
    {
        return detgeom2;
    }
    
    public void newDetection()
    {
        newDetection = true;
    }
}