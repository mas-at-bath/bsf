package mygame;

import com.jme3.math.ColorRGBA;
import com.jme3.light.*;

  class LightInfo
  {
      private String myName;
      private ColorRGBA myCol;
      private float myIntensity;
      private Light lightObj;

      LightInfo(String name, Light lSource, ColorRGBA col, float intensity)
      {
		myName=name;
		lightObj=lSource;
		myCol=col;
		myIntensity=intensity;
      }
         
      public void updateInfo(ColorRGBA col, float intensity)
      {
		myCol=col;
		myIntensity=intensity;
      }

      public ColorRGBA getColour()
      {
		return myCol;
      }

      public float getIntensity()
      {
		return myIntensity;
      }

      public String getName()
      {
		return myName;
      }

      public void updateState()
      {
		lightObj.setColor(myCol.mult(myIntensity));
      }
      
  }
