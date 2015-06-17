package edu.bath.arDrone;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.jivesoftware.smack.XMPPException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.io.*;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;


public class ARDroneController {
	private boolean alive = true;
	
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static ControllerWorkerNonThreadSender ARThreadSender;
	private static String vehicleRecName = "ardrone1-vehicle-receiver";
	
	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static Long intervalTime = 200L;
	
	public static void main(String[] args) throws Exception {
	
		//get IP addressed from config file
	    BufferedReader br = new BufferedReader(new FileReader("config.txt"));
        String line;
        while((line = br.readLine()) != null) {
			if (line.contains("OPENFIRE"))
			{
				String[] configArray = line.split("=");
				XMPPServer = configArray[1];
				System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
			}
        }
		
		try {
            ARThreadSender = new ControllerWorkerNonThreadSender(XMPPServer, "ardrone1-sender", "jasonpassword", jasonSensorVehiclesCmds, "http://127.0.0.1/agent", "http://127.0.0.1/agent/"+vehicleRecName);
            System.out.println("Created simSender, now entering its logic!");
        }
        catch (Exception e) 
        {
            System.out.println("couldn't start  sender");
            System.out.println(e.getStackTrace());
        }
		
		
		ARDroneController arAgent = new ARDroneController();
		arAgent.run();
	}
	
	public ARDroneController() {//throws XMPPException {

	}
	
	public void run() {
	
		System.out.println("basic keyboard example control arDrone:");
		System.out.println("press r=reset, t=takeoff, l=land, s=stop");
		System.out.println("and press enter after selection");
		
		while(alive) 
		{
		try 
			{

				Thread.sleep(intervalTime);
				DataInputStream input = new DataInputStream(System.in); String string = input.readLine();
				if (string.equals("t"))
				{
					System.out.println("take off");
					ARThreadSender.addMessageToSend("arCommand", new String("takeOff"));
					ARThreadSender.send();
				}
				else if (string.equals("l"))
				{
					System.out.println("land");
					ARThreadSender.addMessageToSend("arCommand", new String("land"));
					ARThreadSender.send();
				}
				else if (string.equals("s"))
				{
					System.out.println("stop");
					ARThreadSender.addMessageToSend("arCommand", new String("stop"));
					ARThreadSender.send();
				}
				else if (string.equals("r"))
				{
					System.out.println("reset");
					ARThreadSender.addMessageToSend("arCommand", new String("reset"));
					ARThreadSender.send();
				}
		
			}
			catch (Exception ee1) 
			{
				ee1.printStackTrace();
			}
		}
	}
	
}
