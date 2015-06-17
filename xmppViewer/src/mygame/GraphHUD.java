package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import java.util.ArrayList;


/**
 *
 * @author vin
 */
public class GraphHUD {
    
    int bottomXVal = 0;
    int bottomYVal = 0;
    int farXVal = 0;
    int farYVal = 0;
    
    Material blueMat,greenMat;
    
    Node axisNode = new Node("axis");
    
    float[] heightArray = new float[30];
    float[] heightArrayAlt = new float[30];
    Box[] myBoxes = new Box[30];
    Box[] myAltBoxes = new Box[30];
    
    public GraphHUD(Main parentHandle, BitmapFont guiFont, int height, int width)
    {
        //AssetManager assetMan = parentHandle.getAssetManager();
        bottomXVal = height-60;
        bottomYVal = width-200;
        farXVal = height-10;
        farYVal = width-20;
        
        blueMat = new Material(parentHandle.getAssetManager(), 
        "Common/MatDefs/Misc/Unshaded.j3md");
        blueMat.setColor("Color", ColorRGBA.Blue);
        
        greenMat = new Material(parentHandle.getAssetManager(), 
        "Common/MatDefs/Misc/Unshaded.j3md");
        greenMat.setColor("Color", ColorRGBA.Green);
        
        Material axisMat = new Material(parentHandle.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        axisMat.setColor("Color", ColorRGBA.White);
        Line newLineX = new Line(new Vector3f(bottomYVal,bottomXVal,1f),new Vector3f(farYVal,bottomXVal,1f));
        Geometry geomxAxis = new Geometry("xAxis", newLineX);

        geomxAxis.setMaterial(axisMat);
        Line newLineY = new Line(new Vector3f(bottomYVal,bottomXVal,1f),new Vector3f(bottomYVal,farXVal,1f));
        Geometry geomyAxis = new Geometry("yAxis", newLineY);
        geomyAxis.setMaterial(axisMat);
        
        BitmapText graphText = new BitmapText(guiFont, false);          
        graphText.setSize(guiFont.getCharSet().getRenderedSize()-3);      // font size
        graphText.setColor(ColorRGBA.White);                             // font color
        graphText.setText("XMPP Message Volume");             // the text
        graphText.setLocalTranslation(width-180,height-65,1f); // position*/ 
        
        BitmapText xText = new BitmapText(guiFont, false);          
        xText.setSize(guiFont.getCharSet().getRenderedSize()-5);      // font size
        xText.setColor(ColorRGBA.White);                             // font color
        xText.setText("20");             // the text
        xText.setLocalTranslation(bottomYVal-18,bottomXVal+28,1f); // position*/ 
        
        geomxAxis.setMaterial(axisMat);
        Line xLine = new Line(new Vector3f(bottomYVal-2,bottomXVal+20,1f),new Vector3f(bottomYVal+2,bottomXVal+20,1f));
        Geometry geomxLine = new Geometry("xLine", xLine);
        geomxLine.setMaterial(axisMat);
        
        
       // graphNode.attachChild(graphText);
      //  graphNode.attachChild(geomxAxis);
      //  graphNode.attachChild(geomyAxis);
        AmbientLight alGUI = new AmbientLight();
        alGUI.setColor(ColorRGBA.White.mult(1.3f));
      //  graphNode.addLight(//alGUI);
        
        axisNode.attachChild(graphText);
        axisNode.attachChild(geomxAxis);
        axisNode.attachChild(geomyAxis);
        axisNode.attachChild(xText);
        axisNode.attachChild(geomxLine);
        
        System.out.println("finished building 2D graph");
        
        for (int i=0; i < heightArray.length; i++)
        {
          //  Double tempD = Math.random()*20;
            heightArray[i] = 0f;
        }
        for (int i=0; i < heightArrayAlt.length; i++)
        {
          //  Double tempD = Math.random()*20;
            heightArrayAlt[i] = 0f;
        }
        
        int arraySize = heightArray.length;
        for (int i=0; i < arraySize; i++)
        {
            myBoxes[i] = new Box( new Vector3f(bottomXVal, bottomYVal, 0f), new Vector3f(bottomXVal+5, bottomYVal+1, 0f));
            myAltBoxes[i] = new Box( new Vector3f(bottomXVal, bottomYVal, 0f), new Vector3f(bottomXVal+5, bottomYVal+1, 0f));
        }
        
    }
    
    public void addNewPoint(float pointValue)
    {
        for (int i=heightArray.length-1; i > 0 ; i--)
        {
            heightArray[i] = heightArray[i-1];
        }
        heightArray[0] = pointValue;
    }
    
    public void addNewPoint(float pointValue, float pointValueAlt)
    {
        for (int i=heightArray.length-1; i > 0 ; i--)
        {
            heightArray[i] = heightArray[i-1];
        }
        heightArray[0] = pointValue;
        
        for (int i=heightArrayAlt.length-1; i > 0 ; i--)
        {
            heightArrayAlt[i] = heightArrayAlt[i-1];
        }
        heightArrayAlt[0] = pointValueAlt;
    }
    
    public Node update()
    {
        Node newGraphNode = new Node("graphNode");
        if (axisNode !=null)
        {
            newGraphNode.attachChild(axisNode);
        }
        
        int arraySize = heightArray.length;
        for (int i=0; i < arraySize; i++)
        {
          //  Box newBox = new Box( new Vector3f(bottomXVal, bottomYVal, 0f), new Vector3f(bottomXVal+5, bottomYVal+heightArray[i], 0f));
            Geometry newGeom = new Geometry("BoxGeom", myBoxes[i]);
            newGeom.setLocalScale(heightArray[i], heightArray[i], heightArray[i]);
            newGeom.setMaterial(blueMat);
            float offset = 20+(5*i);
            newGeom.setLocalTranslation(offset,-19f,0f);
            newGraphNode.attachChild(newGeom);
        }
        
        int arraySizeAlt = heightArrayAlt.length;
        for (int i=0; i < arraySizeAlt; i++)
        {
            Box newBox = new Box( new Vector3f(bottomXVal, bottomYVal, 0f), new Vector3f(bottomXVal+5, bottomYVal+heightArrayAlt[i], 0f));
            Geometry newGeom = new Geometry("BoxGeom", newBox);
            //Geometry newGeom = new Geometry("BoxGeom", myAltBoxes[i]);
            newGeom.setMaterial(greenMat);
           // System.out.println("settings scale to " + heightArrayAlt[i]);

            float offset = 20+(5*i);
            newGeom.setLocalTranslation(offset,-19f,0f);
            newGraphNode.attachChild(newGeom);
        }
        
        
        return newGraphNode;
    }

    
}
