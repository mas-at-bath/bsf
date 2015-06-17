package CustomXMPP;

import javax.vecmath.*;

import java.util.*;


public class KnownCollisionNameLoc
{
	private String vehName="";
	
	private Vector3f location;		

	public KnownCollisionNameLoc(String name, Vector3f position)
	{
		vehName = name;
		location = position;
	}

	public String getName()
	{
		return vehName;
	}

	public Vector3f getPosition()
	{
		return location;
	}
}
