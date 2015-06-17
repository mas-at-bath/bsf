/*Uses examples from: NUI Chapter 3. Motion Detection: Optical Flow

From the website:

  Killer Game Programming in Java
  http://fivedots.coe.psu.ac.th/~ad/jg

  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th

combined with the example in https://github.com/bytedeco/javacv/blob/master/samples/OpticalFlowTracker.java
*/

package edu.bath.motionTrack;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.FloatBuffer;

//for newer versions
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_legacy.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Loader;

//for 0.3 or so
/*import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import static com.googlecode.javacpp.Loader.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;*/

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MotionTracker extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";
	private long nanoToMili=1000000;
	private static String componentName="motionTracker";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	private CvMat trainPersonNumMat;
	private CanvasFrame canvasFrame;
	private static final int MAX_CORNERS = 500;
	private static final int WIN_SIZE = 15;

	public MotionTracker(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
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
			}
		}
		catch (Exception e) 
		{
			System.out.println("Error loading config.txt file");
			e.printStackTrace();
		}

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/ID/FaceRecog");
		MotionTracker ps = new MotionTracker(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/ID", "http://127.0.0.1/ID/FaceRecog");

		Thread.currentThread().sleep(1000);
		System.out.println("Created faceSensor, now entering its logic!");
		
		ps.run();
	}
	
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}


	public void run() throws XMPPException {	
		
		while(sensorClient == null) {
			try {
				sensorClient = new SensorClient(XMPPServer, componentName+"-receiver", "jasonpassword");
				System.out.println("Guess sensor connected OK then!");
			} catch (XMPPException e1) {
				System.out.println("Exception in establishing client.");
				e1.printStackTrace();
			}
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
						pendingMessages.put(rdf);
					}
					catch (Exception e)
					{
						System.out.println("Error adding new message to queue..");
						e.printStackTrace();
					}
				}
			}
		});
		System.out.println("Added handler for " + homeSensors);
		try {
			sensorClient.subscribeAndCreate(homeSensors);
		} catch (XMPPException e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}

		//1 for 2nd webcam on laptop
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(1);
		canvasFrame = new CanvasFrame("Preview");
		try
		{
        		grabber.start();
			System.out.println("started webcam ok..");
			
		}
		catch (Exception grabE)
		{
			System.out.println("Problem starting grabber.. webcam OK?");
			grabE.printStackTrace();
		}

		//setup image vars before entering main loop
		boolean firstFrame = true;	
		IplImage grayImage = null;
		IplImage diffImage = null;
		IplImage tempFrame = null;
		CvMemStorage storage = CvMemStorage.create();
		IplImage testframe= new IplImage();
		try {
		   testframe = grabber.grab();
		}
		   
		catch (Exception tE)
		{
			System.out.println("prep error");
			tE.printStackTrace();
		}

    		// create data structures for feature detection and optical flow
		IplImage eigenIm = IplImage.create(testframe.width(), testframe.height(), IPL_DEPTH_32F, 1);
		IplImage tempIm = IplImage.create(testframe.width(), testframe.height(), IPL_DEPTH_32F, 1);

		// (image) buffers for pyramids
		IplImage pyramidA = IplImage.create(testframe.width()+8, testframe.height()/3, IPL_DEPTH_32F, 1);
		IplImage pyramidB = IplImage.create(testframe.width()+8, testframe.height()/3, IPL_DEPTH_32F, 1);

		IntPointer corner_count = new IntPointer(1).put(MAX_CORNERS);
		CvPoint2D32f cornersA = new CvPoint2D32f(MAX_CORNERS);     // used as an array in JavaCV
		CvPoint2D32f cornersB = new CvPoint2D32f(MAX_CORNERS);
		boolean findCorners = true;
		BytePointer features_found = new BytePointer(MAX_CORNERS);
		FloatPointer feature_errors = new FloatPointer(MAX_CORNERS);

		int plk_flags = 0;

		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();
			//debug purposes only 
			//generateAndSendMsg("http://127.0.0.1/detections/movement/", "");

			try {
				if(sensorClient.checkReconnect())
				sensorClient.subscribeAndCreate(homeSensors);
			} catch (XMPPException e1) {
				System.out.println("Couldn't reconnect to " + homeSensors);
				e1.printStackTrace();
				try {
					System.out.println("trying to reconnect");
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {}
				continue;
			}

			String rdfFaceRecogition = pendingMessages.poll();
			while (rdfFaceRecogition != null)
			{			
				try 
				{
					DataReading dr = DataReading.fromRDF(rdfFaceRecogition);
					String takenBy = dr.getTakenBy();
					//System.out.println("got faceRecogition RDF message!");

					Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/requests/something", null);
					if(reqVal != null) 
					{
						String reqAction = (String)reqVal.object;

					}

				}
				catch(Exception e) 
				{
					System.out.println(e);
				}
				rdfFaceRecogition = pendingMessages.poll();
				
			}

			try
			{
				
				IplImage frame = grabber.grab();
				//use these if you want to see local view
				
				canvasFrame.setCanvasSize(frame.width(), frame.height());

				//while (/*canvasFrame.isVisible() &&*/ (frame = grabber.grab()) != null) 
				if ((frame = grabber.grab()) != null) 
				{
		    			cvClearMemStorage(storage);
					if (grayImage == null)
					{
						grayImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
					}
					if (diffImage == null)
					{
						diffImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
					}
		                	cvCvtColor(frame, grayImage, CV_BGR2GRAY);
					//cvSmooth(grayImage, grayImage, CV_GAUSSIAN, 9, 9, 2, 2);

					if (firstFrame)
					{
						tempFrame = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
						firstFrame=false;
					}
					else
					{


					      	// store interesting corners in the cornersA 'array'
					      	cornersA.position(0);            // reset position in array
					      	if (findCorners) {
							CvArr nullCv = null;
							cvGoodFeaturesToTrack((CvArr)tempFrame, (CvArr)eigenIm, (CvArr)tempIm, cornersA, corner_count, 0.01, 5.0, nullCv, 3, 0, 0.04);
						}
					      	cornersA.position(0);   // reset position in arrays
					      	cornersB.position(0);

					      	cvCalcOpticalFlowPyrLK(tempFrame, grayImage, pyramidA, pyramidB, cornersA, cornersB, corner_count.get(), cvSize(WIN_SIZE,WIN_SIZE), 5, features_found, feature_errors, cvTermCriteria(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS, 20, 0.3), plk_flags); 
	
						double[] xChange = new double[corner_count.get()];
						double[] yChange = new double[corner_count.get()];
						boolean validReading = false;

					 	// Make an image of the results
						for (int i = 0; i < corner_count.get(); i++) 
						{
							if (features_found.get(i) == 0 || feature_errors.get(i) > 550) 
							{
								System.out.println("Error is " + feature_errors.get(i));
								//continue;
							}
							//System.out.println("Got it/n");
							else
							{
								cornersA.position(i);
								cornersB.position(i);
								CvPoint p0 = cvPoint(Math.round(cornersA.x()),					Math.round(cornersA.y()));
								CvPoint p1 = cvPoint(Math.round(cornersB.x()),					Math.round(cornersB.y()));
								xChange[i] = cornersA.x() - cornersB.x();
								yChange[i] = cornersA.y() - cornersB.y();
								//System.out.println(xChange[i] + ", " + yChange[i]);
								cvLine(frame, p0, p1, CV_RGB(0, 0, 255), 2, 8, 0);
								validReading=true;
							}
						}
						if (validReading)
						{
							Double sumX = 0D;
							Double sumY = 0D;
							for (Double numberx : xChange)
							{
								sumX = sumX+numberx;
							}
							for (Double numbery : yChange)
							{
								sumY = sumY+numbery;
							}
							Double avgX = sumX/corner_count.get();
							Double avgY = sumY/corner_count.get();
							System.out.println("Avg movement: " + avgX + " , " + avgY);
						
							//TODO: finish up..	
							//now really needs a method here to discount the outliers, and some calibration tests.. 
						}
						
	
						canvasFrame.showImage(frame);		
					}
					cvCopy(grayImage, tempFrame, null);
				
				}
				//grabber.stop();
				//canvasFrame.dispose();
			}
			catch (Exception cvE)
			{
				System.out.println("error grabbing image");
				cvE.printStackTrace();
			}

			//waitUntil(cycleTime*nanoToMili);

		}
		try
		{
			grabber.stop();
		}
		catch (Exception grabEnd)
		{
			System.out.println("Problem stopping grabber.. webcam OK?");
			grabEnd.printStackTrace();
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

	public void generateAndSendMsg(String type, String msg) {

		try 
		{				
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/components/"+componentName);
			testReading.addDataValue(null, type, msg, false);
			publish(testReading);
		} 							
		catch (Exception e) {
			e.printStackTrace();
		}
	}


}
