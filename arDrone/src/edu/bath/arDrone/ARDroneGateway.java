package edu.bath.arDrone;

import com.shigeodayo.ardrone.ARDrone;
import com.shigeodayo.ardrone.navdata.javadrone.NavData;
import com.shigeodayo.ardrone.navdata.javadrone.NavDataListener;
import com.shigeodayo.ardrone.navdata.AttitudeListener;
import com.shigeodayo.ardrone.navdata.BatteryListener;
import com.shigeodayo.ardrone.navdata.DroneState;
import com.shigeodayo.ardrone.navdata.StateListener;
import com.shigeodayo.ardrone.navdata.VelocityListener;
import com.shigeodayo.ardrone.video.ImageListener;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import org.jivesoftware.smack.XMPPException;
import java.io.*;

public class ARDroneGateway implements NavDataListener 
{

	private static ARDrone ardrone;
	private static WorkerNonThreadSender droneMsgSender, simSender;
	private static String XMPPServer = "127.0.0.1";
	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";
	private static String currentLocation;
	private static String primaryHandle;
	private static String myId,myPassword,myNodeName;
	private static long lastNavUpdate=0;
	private static SensorClient sensorClient;
	private double lastUpdateTime = 0;
	private static double intervalWait = 0; //500 is good for updates every 0.5 seconds too..
	private static String vehicleRecName = "ardrone1-vehicle-receiver";
	private static String videoURLLocation = "rtmp://192.168.0.8/oflaDemo/myStream";
	private static boolean showingVideo = true;
	private static FileWriter fw;
	private static int msgCounter = 0;
	private static long perSecCheck = 0;
	
	public ARDroneGateway(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
		this.myId=id;
		this.myPassword=password;
		this.myNodeName=nodeName;
	}


	public static void main(String[] args)
	{
		try 
		{
			//get IP addressed from config file
        	BufferedReader br = new BufferedReader(new FileReader("config.txt"));
       		String line;
       		while((line = br.readLine()) != null) 
			{
           		if (line.contains("OPENFIRE"))
           		{
					String[] configArray = line.split("=");
					XMPPServer = configArray[1];
					System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
			}
		}
		catch (Exception e) 
		{
			System.out.println("couldnt load config.txt for openfire net address");
		}
		
		try 
		{
			fw = new FileWriter("publishRate.txt",false);
			ARDroneGateway ps = new ARDroneGateway(XMPPServer, "ardrone1-vehicle", "jasonpassword", "jasonSensor", "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/ardrone1-vehicle");
			System.out.println("Created jasonSensor, now entering its logic!");
			ps.run();
		}
		catch (Exception e) 
		{
			System.out.println("couldnt start instance");
		}	
		
	}

	public void run() throws XMPPException 
	{
       		try 
			{
            		droneMsgSender = new WorkerNonThreadSender(XMPPServer, myId, myPassword, jasonSensorVehicles , currentLocation, primaryHandle);
            		System.out.println("Created droneMsgSender, now entering its logic!");
					simSender = new WorkerNonThreadSender(XMPPServer, myId + "-simsender", "jasonpassword", "simStateSensor", "http://127.0.0.1/vehicleSensors", "http://127.0.0.1/vehicleSensors/ardrone1-vehicle");
					System.out.println("Created simSender, now entering its logic!");
        	}
        	catch (Exception e) 
        	{
            		System.out.println("couldn't start drone thread sender");
            		e.printStackTrace();
        	}
		
		while(sensorClient == null) {
			try {
				sensorClient = new SensorClient(XMPPServer, vehicleRecName, "jasonpassword");
			} catch (XMPPException e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}
		
		sensorClient.addHandler(jasonSensorVehiclesCmds, new ReadingHandler() { 
			@Override
			public void handleIncomingReading(String node, String rdf) {
				try 
				{
					DataReading dr = DataReading.fromRDF(rdf);
					String takenBy = dr.getTakenBy();
					System.out.println("received " + takenBy);

					if (takenBy.equals("http://127.0.0.1/agent/"+vehicleRecName))
					{
						//pass the lastUpdateTime compared to this, is it less than or equal:w
                        if ((dr.getTimestamp() < lastUpdateTime) && lastUpdateTime != 0)
                        {
                            System.out.println("XXXXXXXX out of sync dr.timestamp XXXXXXXXXXX ");
                        }

                        lastUpdateTime = dr.getTimestamp();
						Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/request/arCommand", null);
						if(reqVal != null) 
						{
							String reqMsg = (String)reqVal.object;
							if (reqMsg.equals("takeOff"))
							{
								ardrone.takeOff();
							}
							else if (reqMsg.equals("land"))
							{
								ardrone.landing();
							}
							else if (reqMsg.equals("setHorizontalCamera"))
							{
								ardrone.setHorizontalCamera();
							}
							else if (reqMsg.equals("setVerticalCamera"))
							{
								ardrone.setVerticalCamera();
							}
							else if (reqMsg.equals("toggleCamera"))
							{
								ardrone.toggleCamera();
							}
							else if (reqMsg.equals("reset"))
							{
								ardrone.reset();
							}
							else if (reqMsg.equals("backward"))
							{
								ardrone.backward();
							}
							else if (reqMsg.equals("forward"))
							{
								ardrone.forward();
							}
							else if (reqMsg.equals("spinRight"))
							{
								ardrone.spinRight();
							}
							else if (reqMsg.equals("up"))
							{
								ardrone.up();
							}
							else if (reqMsg.equals("down"))
							{
								ardrone.down();
							}
							else if (reqMsg.equals("spinLeft"))
							{
								ardrone.spinLeft();
							}
							else if (reqMsg.equals("goLeft"))
							{
								ardrone.goLeft();
							}
							else if (reqMsg.equals("goRight"))
							{
								ardrone.goRight();
							}
							else if (reqMsg.equals("stop"))
							{
								ardrone.stop();
							}
						}						
					}
				}catch(Exception e) {System.out.println(e);}
			}
		});
		try {
			sensorClient.subscribeAndCreate(jasonSensorVehiclesCmds);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + jasonSensorVehiclesCmds);
			e1.printStackTrace();
		}

		ardrone=new ARDrone("192.168.1.1");
		ardrone.connect();
		ardrone.connectNav();
		lastNavUpdate=System.currentTimeMillis();
		ardrone.setVideoStreamURL(videoURLLocation);
		ardrone.connectVideo();
		ardrone.setVideoStream(showingVideo);
		ardrone.start();
		ardrone.addNavDataListener(this);
	}
  
	public void processXMPPMessage(String msg)
	{
		System.out.println("arDrone asked to process: "  + msg);
	}

  
	public void navDataUpdated(NavData navData)
	{
		if ((lastNavUpdate+intervalWait) < System.currentTimeMillis())
		{
		//	System.out.println("sending nav data update");
			droneMsgSender.addMessageToSend("state/altitude", navData.getAltitude() + "");
			droneMsgSender.addMessageToSend("state/battery", navData.getBattery() + "");
			droneMsgSender.addMessageToSend("state/roll", navData.getRoll() + "");
			droneMsgSender.addMessageToSend("state/vx", navData.getVx() + "");
			droneMsgSender.addMessageToSend("state/vz", navData.getVz() + "");
			droneMsgSender.addMessageToSend("state/yaw", navData.getYaw() + "");
			droneMsgSender.addMessageToSend("state/isEmergency", navData.isEmergency() + "");
			droneMsgSender.addMessageToSend("state/isFlying", navData.isFlying() + "");
			droneMsgSender.addMessageToSend("state/isMotorsDown", navData.isMotorsDown() + "");
			droneMsgSender.addMessageToSend("state/isGyrometersDown", navData.isGyrometersDown() + "");
			droneMsgSender.addMessageToSend("state/battery", navData.getBattery() + "");
			droneMsgSender.addMessageToSend("state/isNotEnoughPower", navData.isNotEnoughPower() + "");	
			droneMsgSender.addMessageToSend("state/isTooMuchWind", navData.isTooMuchWind() + "");
			droneMsgSender.addMessageToSend("state/isTrimReceived", navData.isTrimReceived() + "");
			droneMsgSender.addMessageToSend("state/isTrimRunning", navData.isTrimRunning() + "");
			droneMsgSender.addMessageToSend("state/isTrimSucceeded", navData.isTrimSucceeded() + "");
			droneMsgSender.addMessageToSend("state/isUltrasonicSensorDeaf", navData.isUltrasonicSensorDeaf() + "");
			msgCounter = msgCounter + 17;
			if (showingVideo)
			{
				droneMsgSender.addMessageToSend("video/URL", videoURLLocation);
				msgCounter++;
			}
			
			droneMsgSender.send();
			lastNavUpdate=System.currentTimeMillis();
			
			long newPerSecCheck = System.currentTimeMillis();
			if (perSecCheck+1000 <= newPerSecCheck)
			{
				System.out.println(newPerSecCheck + ", " + msgCounter);
				try
				{
					fw.write(System.currentTimeMillis() + ", " + msgCounter +" \n");
					fw.close();
					fw = new FileWriter("publishRate.txt",true);
					simSender.sendPublishedRate(msgCounter);
					perSecCheck = newPerSecCheck;
					msgCounter=0;
				}
				catch (Exception e) 
				{
					System.out.println("couldnt update log file");
				}
			}
		}	

	}  

}
