package edu.bath.PiDevice;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.*;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;


public class PiDevice extends Sensor {

	private boolean alive = true;
	private long timeSentLastAOILightMessage = 0;
	private boolean askedStartupData = false;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";
	private long nanoToMili=1000000;
	private static String componentName="piSensor1";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingPIMessages = new LinkedBlockingQueue<String>();	

	private static long startupTime=0L;
	private static long startupDelay=3000L;
	private static ProcessBuilder pythonDHT22pb;
	private static ProcessBuilder pythonK30pb;

 	private static int ADC_CHANNEL0_ldr = MCP3008Reader.MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008
	private static int ADC_CHANNEL2_mics2710 = MCP3008Reader.MCP3008_input_channels.CH2.ch(); // Between 0 and 7, 8 channels on the MCP3008
	private static int ADC_CHANNEL3_mics5525 = MCP3008Reader.MCP3008_input_channels.CH3.ch();
	private static int ADC_CHANNEL4_snd = MCP3008Reader.MCP3008_input_channels.CH4.ch();

	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static PiDevice ps;
	private int co2result =0 ;

	private BMP085Device device;

	public PiDevice(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public PiDevice(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws Exception {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public static void main(String[] args) throws Exception {
			
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));
			String line;
			while((line = br.readLine()) != null) 
			{
				if (line.contains("OPENFIRE"))
				{
					String[] configArray = line.split("=");
					XMPPServer = configArray[1];
					//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
				}
				if (line.contains("COMMUNICATION"))
					{
						String[] configArray = line.split("=");
						if(configArray[1].equals("MQTT"))
						{
							useMQTT=true;
						}
						else if(configArray[1].equals("XMPP"))
						{
							useXMPP=true;
						}
						//System.out.println("Using config declared IP address of openfire server as: " + XMPPServer);
					}
				}
				if (!useMQTT && !useXMPP)
				{
					System.out.println("no COMMUNICATION value found in config.txt, should be = MQTT or XMPP");
					System.exit(1);
				}
		}
		catch (Exception e) 
		{
			System.out.println("Error loading config.txt file");
			e.printStackTrace();
		}

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/PISensors, http://127.0.0.1/PISensors/PI");
		if (useXMPP)
		{
			ps = new PiDevice(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/PISensors", "http://127.0.0.1/PiSensors/Pi");
		}
		else if (useMQTT)
		{
			ps = new PiDevice(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/PISensors", "http://127.0.0.1/PiSensors/Pi", true, 0);
		}

		Thread.currentThread().sleep(1000);
		System.out.println("Created jasonSensor, now entering its logic!");

		try
		{
			String fileNameLoc = new String(System.getProperty("user.dir") + "/AdafruitDHT.py");
			System.out.println("calling " + fileNameLoc);
			pythonDHT22pb = new ProcessBuilder(fileNameLoc ,"22","4");

			Process p = pythonDHT22pb.start();
	 
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String ret = new String(in.readLine());
			System.out.println("value is : "+ret);
		}
		catch (Exception pbEr)
		{
			System.out.println("python call for dht22 failed...");
			System.out.println("it might be you're not running with root access, if so there will be a big crash soon...");
			pbEr.printStackTrace();
		}

		try
		{
			String fileNameLocK30 = new String(System.getProperty("user.dir") + "/K-30.py");
			System.out.println("calling " + fileNameLocK30);
			pythonK30pb = new ProcessBuilder(fileNameLocK30);

			Process pk30 = pythonK30pb.start();
	 
			BufferedReader in = new BufferedReader(new InputStreamReader(pk30.getInputStream()));
			String ret = new String(in.readLine());
			System.out.println("CO2 Value is : "+ret);
		}
		catch (Exception pbKEr)
		{
			System.out.println("python call for k30 failed...");
			System.out.println("it might be you're not running with root access, if so there will be a big crash soon...");
			pbKEr.printStackTrace();
		}

		ps.run();
	}
	
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException {	
		
		if (useXMPP)
		{
			System.out.println("Using XMPP");
			while(sensorClient == null) {
				try {
					sensorClient = new SensorXMPPClient(XMPPServer, componentName+"-receiver", "jasonpassword");
					System.out.println("Guess sensor connected OK then!");
				} catch (XMPPException e1) {
					System.out.println("Exception in establishing client.");
					e1.printStackTrace();
				}
			}
		}
		else if (useMQTT)
		{
			System.out.println("Using MQTT");
			try {
				sensorClient = new SensorMQTTClient(XMPPServer, componentName+"-receiver");
				System.out.println("Guess sensor connected OK then!");
			} catch (Exception e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
		}

		try {
			device = new BMP085Device(false); 
			double reading[] = device.getReading();
			System.out.println("temperature (" + reading[1] + ") and pressure (" + reading[0]*0.01 + " read from sensor..");

		}
		catch (Exception piE)
		{
			System.out.println("Error starting BMP085 device");
			piE.printStackTrace();
		}

		startupTime=System.currentTimeMillis();

		sensorClient.addHandler(homeSensors, new ReadingHandler() 
		{ 
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				if ((startupTime + startupDelay) < System.currentTimeMillis())
				{
				try {
					pendingPIMessages.put(rdf);
				}
				catch (Exception e)
				{
					System.out.println("Error adding new message to queue..");
					e.printStackTrace();
				}
				}
			}
		});
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}

		System.out.println("init MCP3008...");
		MCP3008Reader.initMCP3008();
		System.out.println("done init..");

		/*System.out.println("Starting serial port for CO2 sensor..");
		final Serial serial = SerialFactory.createInstance();
		serial.addListener(new SerialDataListener() {
		    @Override
		    public void dataReceived(SerialDataEvent event) {
		        // print out the data received to the console
		       // System.out.print(event.getData());
			String data = event.getData();

			byte[] array = data.getBytes();

			//System.out.println("Read: ");
			int high = array[4];
			int low = array[5];
			co2result = (high*256)+low;
			System.out.println("CO2: " + co2result);
			for (int i = 0; i < array.length; i++) {
			//	System.out.println(array[i]);
			    System.out.printf("%02X ", array[i]);
			}
		    }            
		});
		try {
		    // open the default serial port provided on the GPIO header
		    serial.open(Serial.DEFAULT_COM_PORT, 9600);
		}
		catch (Exception e)
		{
			System.out.println("error opening serial port..");
		}*/

		while(alive) 
		{
			//long currentLoopStartedNanoTime = System.nanoTime();
			//piMsgSender.generateAndSendMsg("http://127.0.0.1/request/sendAllTraficLights", "tt");


			try {
				if(sensorClient.checkReconnect())
				sensorClient.subscribe(homeSensors);
			} catch (Exception e1) {
				System.out.println("Couldn't reconnect to " + homeSensors);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			String rdfPI = pendingPIMessages.poll();
			while (rdfPI != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfPI);
					String takenBy = dr.getTakenBy();
					//System.out.println("got Pi message!");

					/*Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/route#edges", null);
					if(reqVal != null) 
					{
						String reqMsg = (String)reqVal.object;
						updateRoute(takenBy, reqMsg);
						System.out.println("received route info: " + reqMsg + " from " + takenBy);
					}*/

				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfPI = pendingPIMessages.poll();
			}

			try
			{
				DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
				testReading.setTakenBy("http://127.0.0.1/components/houseSensors/"+componentName);

				///byte[] bytes = new byte[] {(byte)0xfe, (byte)0x44, (byte)0x00, (byte)0x08, (byte)0x02, (byte)0x9F, (byte)0x25};
				//byte[] bytes = new byte[] {"\u00fe".getBytes("UTF-8"), "\u0044".getBytes("UTF-8")}
				//serial.write("\u00fe\u0044\u0000\u0008\u0002\u009f\u0025".getBytes("UTF-8"));
				///serial.write(bytes);

				double reading[] = device.getReading();
				double bmpTemp = reading[1];
				double bmpPressure = reading[0]*0.01;
				//System.out.println("temperature (" + bmpTemp + ") and pressure (" + bmpPressure + " read from sensor..");
				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#BMP80Temp", bmpTemp, false);
				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#BMP80Pressure", bmpPressure, false);

				//pulldowN: (self.pullDown*self.sensorVoltage)/vout - self.pullDown
				//pullup: self.pullUp/((self.sensorVoltage/vout)-1)
				
				float ldr_val = MCP3008Reader.readMCP3008(ADC_CHANNEL0_ldr)/1023f * 3.3f;
				//System.out.println("ldr_val " + ldr_val);
				float ldr_res = (float) 10000f/((3.3f/ldr_val)-1f);
				float lux_rs = (float) Math.exp((Math.log(ldr_res/1000)-4.125)/-0.6704);

				float NO_val = MCP3008Reader.readMCP3008(ADC_CHANNEL2_mics2710)/1023f * 3.3f;//pullDownResistance=10000
				float NO_res = (10000f*3.3f)/NO_val - 10000f;
				float CO_val = MCP3008Reader.readMCP3008(ADC_CHANNEL3_mics5525)/1023f * 3.3f;//pullDownResistance=100000
				float CO_res = (100000f*3.3f)/CO_val - 100000f;

				float mic_val = MCP3008Reader.readMCP3008(ADC_CHANNEL4_snd)/1023f * 3.3f;
					float mic_res = mic_val*1000;

				//System.out.println("lux: " +lux_rs + ", NO: " + NO_res + ", CO: " + CO_res + ", Mic: " + mic_res);

				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#light", lux_rs, false);
				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#GasNO", NO_res, false);
				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#GasCO", CO_res, false);
				testReading.addDataValue(null, "http://127.0.0.1/sensors/types#noise", mic_res, false);

				Process pK30 = pythonK30pb.start();
				BufferedReader ink30 = new BufferedReader(new InputStreamReader(pK30.getInputStream()));
				String retK30= new String(ink30.readLine());
				if (retK30.startsWith("Co2"))
				{
					System.out.println(retK30);
					String[] resultArray = retK30.split("=");
					//System.out.println("co2 " + resultArray[1]);
					testReading.addDataValue(null, "http://127.0.0.1/sensors/types#GasCO2", resultArray[1], false);
				}

				//dataReading.add all those
				Process p = pythonDHT22pb.start();
	 
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String ret = new String(in.readLine());
				if (ret.startsWith("Temp"))
				{
					String[] resultArray = ret.split("  ");
					String[] tempVal= resultArray[0].split("=");
					String tempRes = tempVal[1].substring(0, tempVal[1].length()-2);
					String[] humiVal= resultArray[1].split("=");
					String humiRes = humiVal[1].substring(0, humiVal[1].length()-1);
					System.out.println("dht22 temp: " + tempRes + ", humidity: " + humiRes);
					testReading.addDataValue(null, "http://127.0.0.1/sensors/types#DHT22temperature", tempRes, false);
					testReading.addDataValue(null, "http://127.0.0.1/sensors/types#DHT22humidity", humiRes, false);
					//then add these as data readings
				}
				else
				{
					System.out.println("strange result from dht22 python call: " + ret);
				}
				//System.out.println("value is : "+ret);
				
				publish(testReading);

			}
			catch (Exception err)
			{
				System.out.println("error getting sensor readings.. ");
					err.printStackTrace();
			}
			waitUntil(5000*nanoToMili);
		}
					
	}	


	public void waitUntil(long delayTo)
	{	
		delayTo=delayTo+System.nanoTime();	
		long currentT=0;
		do{
			currentT = System.nanoTime();
		}while(delayTo >= currentT);
	}

}
