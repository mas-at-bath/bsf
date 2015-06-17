package edu.bath.AOI;

public class ExitLaneLightState
{
	private String exitLaneName="";
	private String lightState="";

	public ExitLaneLightState(String name)
	{
		exitLaneName = name;
	}

	public ExitLaneLightState(String name, String colour)
	{
		exitLaneName = name;
		lightState = colour;
	}

	public void setLightState(String colour)
	{
		lightState=colour;
	}

	public String getLightState()
	{
		return lightState;
	}

	public String getExitName()
	{
		return exitLaneName;
	}
}
