package edu.bath.rdfUtils.rdfReplayer;

public class TimeControl {
    
	//60000 is then 1 minute of data to be replayed in publishDelayTime window as realtime
    	private int intervalTime=600000;
	private boolean isPaused=false;
    
    	public TimeControl()
    	{
    	}

	public void pause()
	{
		isPaused=true;
	}

	public void unPause()
	{
		isPaused=false;
	}

	public boolean getIsPaused()
	{
		return isPaused;
	}

	public void increaseSpeed()
	{
		intervalTime=intervalTime*10;
	}
    
	public void decreaseSpeed()
	{
	}

	public void normalTime()
	{
		intervalTime=1;
	}

	public int getInterval()
	{
		return intervalTime;
	}
 
}
