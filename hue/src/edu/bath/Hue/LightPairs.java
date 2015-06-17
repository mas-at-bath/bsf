package edu.bath.HueBSF;

import com.philips.lighting.model.*;

public class LightPairs {

	private PHLight myLight;
	//private PHLightState myLightState;
	private float x;
	private float y;

	public LightPairs(PHLight newL, float newx, float newy) {
		myLight = newL;
		x=newx;
		y=newy;
		System.out.println(myLight.getName() + " set to " + x + " , " + y);
	}

	public PHLight getLight()
	{
		return myLight;
	}

 	public float getX() { return x; }
	public float getY() { return y; }

	//public PHLightState getLightState()
	//{
	//	return myLightState;
	//}
	
}
