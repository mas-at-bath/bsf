/*
 * To change this template, choose Tools | Templates
 * and open t   he template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author vin
 */
public class DebugShapes {
    
 //   private TerrainQuad myTerrain = new TerrainQuad();
   // Material mat_terrain;
    private Box box1;
    
    class XY {
        XY(float a, float b)
        {
            X=a;
            Y=b;
        }
        float X;
        float Y;
    }
    
    public DebugShapes(Main parentHandle, String scenarioLoc)
    {
        AssetManager assetMan = parentHandle.getAssetManager();
        System.out.println("adding debug route");
        Material mat1 = new Material(assetMan, 
        "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        ArrayList<MyPoint2D> journeyPoints = new ArrayList<MyPoint2D>();
        
        if (scenarioLoc.equals("bath"))
        {     
            double x0 = 544482;
            double y0 = 5692044;
        
            try
            {	
                ArrayList<String> routeContents = (ArrayList<String>) assetMan.loadAsset("route.txt");
                System.out.println("bath debug route file contains " + routeContents.size() + " points");
                               
                for (String readPosition : routeContents)
		{
			String[] sepCoords = readPosition.split(",");
			Double xPos = Double.parseDouble(sepCoords[0]);
			Double yPos = Double.parseDouble(sepCoords[1]);
			journeyPoints.add(new MyPoint2D(yPos-y0,xPos-x0));
		}
 	
		System.out.println("finished loading route, with " + journeyPoints.size() + " waypoints");
            }
            catch (Exception e) 
            {
                System.out.println("couldnt load route file");
            }
        }
        else if (scenarioLoc.equals("m25"))
        {     
            double x0 = 672807.21;
            double y0 = 5683536.37;
        
            try
            {
                ArrayList<String> routeContents = (ArrayList<String>) assetMan.loadAsset("m25route.txt");
                System.out.println("m25 debug route file contains " + routeContents.size() + " points");
                               
                for (String readPosition : routeContents)
		{
			String[] sepCoords = readPosition.split(",");
			Double xPos = Double.parseDouble(sepCoords[0]);
			Double yPos = Double.parseDouble(sepCoords[1]);
			journeyPoints.add(new MyPoint2D(yPos-y0,xPos-x0));
		}
 	
		System.out.println("finished loading route, with " + journeyPoints.size() + " waypoints");
            }
            catch (Exception e) 
            {
                System.out.println("couldnt load route file");
            }
        }
           
   /*     box1 = new Box( Vector3f.ZERO, 1,1,1);
        Geometry blue = new Geometry("Box", box1);
        blue.setMaterial(mat1);
        blue.setLocalTranslation(10f,0f,10f);
        parentHandle.addVisual(blue);
        
        
        Box box2 = new Box( Vector3f.ZERO, 1,1,1);
        Geometry blue2 = new Geometry("Box2", box2);
        blue2.setMaterial(mat1);
        blue2.setLocalTranslation(20f,0f,20f);
        parentHandle.addVisual(blue2);
        
        Box box3 = new Box( Vector3f.ZERO, 1,1,1);
        Geometry blue3 = new Geometry("Box3", box3);
        blue3.setMaterial(mat1);
        blue3.setLocalTranslation(30f,0f,30f);
        parentHandle.addVisual(blue3);*/
       
        for (int p=0;p < journeyPoints.size()-1; p++)
        {
            Vector3f startP = new Vector3f((float) journeyPoints.get(p).getX(),0.2f,(float) journeyPoints.get(p).getY());
            Vector3f finP = new Vector3f((float)journeyPoints.get(p+1).getX(),0.2f,(float)journeyPoints.get(p+1).getY());
            Line newLine = new Line(startP,finP);
            newLine.setLineWidth(3f);
            Geometry geom = new Geometry("", newLine);
            Material whiteMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            whiteMat.setColor("Color", ColorRGBA.Green);
            geom.setMaterial(whiteMat);
            parentHandle.addVisual(geom);

            Box boxNew = new Box( Vector3f.ZERO, 0.25f,0.25f,0.25f);
            Geometry blueNew = new Geometry("wayPointBox", boxNew);
            blueNew.setMaterial(mat1);
            blueNew.setLocalTranslation((float) journeyPoints.get(p).getX(),0.1f,(float) journeyPoints.get(p).getY());
            parentHandle.addVisual(blueNew);
        
        }

        System.out.println("finished terrain");
    }
   
}
