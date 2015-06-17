

package edu.bath.faceRecog;

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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.Executors;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

//for newer versions
/*import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Pointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_legacy.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Loader;*/

//for 0.3 or so
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import static com.googlecode.javacpp.Loader.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FaceRecognizer extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private static double lastUpdateTime=0;
	private static String XMPPServer = "127.0.0.1";
	private static String homeSensors = "homeSensor";
	private long nanoToMili=1000000;
	private static String componentName="faceRecogition";
	private static int receivedMessageCount =0;
	private LinkedBlockingQueue<String> pendingMessages = new LinkedBlockingQueue<String>();	
	private long cycleTime=1000;

	private static long startupTime=0L;
	private static long startupDelay=1000L;
	private FaceRecognition faceRecognition;
	private CvMat trainPersonNumMat;

	private SimpleDateFormat sdfDate; 
	private SimpleDateFormat sdfPrintDate;
	private static MyHandler webHandler;


	public FaceRecognizer(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}
	
	public static void main(String[] args) throws Exception {

    		InetSocketAddress addr = new InetSocketAddress(8080);
    		HttpServer server = HttpServer.create(addr, 0);

		webHandler = new MyHandler();
    		server.createContext("/", webHandler);
    		server.setExecutor(Executors.newCachedThreadPool());
    		server.start();
    		System.out.println("Server is listening on port 8080" );
			
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
		FaceRecognizer ps = new FaceRecognizer(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/ID", "http://127.0.0.1/ID/FaceRecog");

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

		sdfDate = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
		sdfPrintDate = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss.SSS");

    		faceRecognition = new FaceRecognition();
		//current learning methods if needed
    		//faceRecognition.learn("all10.txt");
		//System.out.println("+++==== Learnt images, now testing");
		//faceRecognition.recognizeFileList("lower3.txt");


		// load the saved training data
    		trainPersonNumMat = faceRecognition.loadTrainingData();
    		if (trainPersonNumMat == null) {
			System.out.println("error loading training data..");
      			return;
    		}
		else
		{
			System.out.println("loaded training data ok..");
		}			

		//this training method was based on earlier lib, and instead now provides the ability to process a number of images, and 
		//extract the face region out to a predefined width, to be used in the test file of the learn method above
		boolean trainingImages = false;
		if (trainingImages)
		{
			runTraining();
		}


		//test recognitiion
		boolean runRecognition=false;
		if (runRecognition)
		{
			runRecognition();
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

		System.out.println("starting webcam");
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		
		try
		{
			grabber.setImageWidth(640);
			grabber.setImageHeight(480);
        		grabber.start();
			System.out.println("started webcam ok, " + grabber.getImageWidth() + " x " + grabber.getImageHeight());
	
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

		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();
			//debug purposes only 
			//generateAndSendMsg("http://127.0.0.1/detections/people", "vin");

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
				//System.out.println("trying to grab image");
				IplImage frame = grabber.grab();
				webHandler.updateImage(frame.getBufferedImage());
				//System.out.println("got image");
				//use these if you want to see local view
				//CanvasFrame canvasFrame = new CanvasFrame("Preview");
				//canvasFrame.setCanvasSize(frame.width(), frame.height());

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
					cvSmooth(grayImage, grayImage, CV_GAUSSIAN, 9, 9, 2, 2);

					if (firstFrame)
					{
						tempFrame = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
						firstFrame=false;
						saveImage(frame);
					}
					else
					{
						cvAbsDiff(grayImage, tempFrame, diffImage);
						cvThreshold(diffImage, diffImage, 20, 255, CV_THRESH_BINARY);			

						// recognize contours
						CvSeq contour = new CvSeq(null);
						cvFindContours(diffImage, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

						int contourCount=0;
						while (contour != null && !contour.isNull()) {
						    if (contour.elem_size() > 0) {
							//might revisit this later if want to add 
							/*CvBox2D box = cvMinAreaRect2(contour, storage);
							// test intersection
							if (box != null) {
							    CvPoint2D32f center = box.center();
							    CvSize2D32f size = box.size();
							}*/
							contourCount++;
							//drawContours(diffImage, contour, 4, new Scalar(40, 233, 45,0 ));
						    }
						    contour = contour.h_next();
						}
						if (contourCount > 0)
						{
							Date now = new Date();
							String strDateToPrint = sdfPrintDate.format(now);
							System.out.println(strDateToPrint + ", Detected Movement!! Contours: " + contourCount);
							//try to recognise anyone
           						CvSeq faces = faceRecognition.detectFace(frame);
							System.out.println(strDateToPrint + ", found " + faces.total() + " faces");
							if (faces.total() >= 1)
							{
            							CvRect r = new CvRect(cvGetSeqElem(faces, faces.total()-1));
 								int x = r.x(), y = r.y(), w = r.width(), h = r.height();
								RecognitionInfo newRecognition = faceRecognition.recognizeFromImage(faceRecognition.preprocessImage(frame, r), trainPersonNumMat);
								
								if (newRecognition.getConfidence() > 0.83)
								{
									System.out.print(strDateToPrint + ", seen: " + newRecognition.getName() + " with " + newRecognition.getConfidence() + " confidence");
									generateAndSendMsg("http://127.0.0.1/detections/people", newRecognition.getName());
								}					
						
								//totalImages[foundImages] = faceRecognition.preprocessImage(origImg, r);
							}

							saveImage(frame);
						}
						else
						{
							//System.out.println("no movement..");
						}
						//canvasFrame.showImage(diffImage);		
					}
					cvCopy(grayImage, tempFrame, null);
				
				}
				else
				{
					System.out.println("grabber return is null!!");
				}
				//grabber.stop();
				//canvasFrame.dispose();
			}
			catch (Exception cvE)
			{
				System.out.println("error grabbing image");
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

	private void runTraining()
	{
		File testLoc = new File("./data/testData");
		String[] names = testLoc.list();

		for(String name : names)
		{
			String dirPath = "./data/testData/" + name;
    			if (new File(dirPath).isDirectory())
    			{
        			//System.out.println(name);
				File subLoc = new File(dirPath);
				String[] imageNames = subLoc.list();
				int maxImages = imageNames.length;
				System.out.println("for subject " + name + " found " + maxImages + " images");

				int foundImages = 0;
				IplImage[] totalImages = new IplImage[maxImages];

				try {
					for (int im=0; im <maxImages; im++)
					{
						String fileName = dirPath + "/" + imageNames[im];
						File imgFile = new File(fileName);
						if (imgFile.exists()) 
						{
							//System.out.println("processing " + fileName);

							BufferedImage bImg =  ImageIO.read(imgFile);
							IplImage origImg = IplImage.createFrom(bImg);
							IplImage snapshot = cvCreateImage(cvGetSize(origImg), origImg.depth(), origImg.nChannels());
							cvFlip(origImg, snapshot, 1);
           						CvSeq faces = faceRecognition.detectFace(origImg);
							System.out.println("found " + faces.total() + " faces");
							if (faces.total() >= 1)
							{
            							CvRect r = new CvRect(cvGetSeqElem(faces, faces.total()-1));
 								int x = r.x(), y = r.y(), w = r.width(), h = r.height();

								totalImages[foundImages] = faceRecognition.preprocessImage(origImg, r);


								cvSaveImage(name+"_"+im+".jpg", totalImages[foundImages]);
	
                						cvRectangle(origImg, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
								cvSaveImage("original"+name+"_"+im+".jpg", origImg);
						 
								foundImages++;
						
							}
							else
							{
								System.out.println("no faces found in " + fileName);
							}
						}
						else
						{
							System.out.println("expected " + fileName + " but didnt find it..");
						}
					}			

					if (foundImages > 0)
					{
						/*IplImage[] trainImages = new IplImage[foundImages];
						for (int moveIm=0; moveIm< foundImages; moveIm++)
						{
							trainImages[moveIm] = totalImages[moveIm];
						}
						System.out.println("trying to learn " + name + " from " + foundImages + " images");
						fr.learnNewFace(name, trainImages);*/
					}
				}
				catch (Exception imgErr)
				{
					System.out.println("error processing image");
					imgErr.printStackTrace();
				}

    			}
		}
	}

	private void runRecognition()
	{

			try
			{
				String fileName = "./vin_11.jpg";
				File fileLoc = new File(fileName);
				BufferedImage bImg =  ImageIO.read(fileLoc);
				IplImage testImg = IplImage.createFrom(bImg);
				faceRecognition.recognizeFromImage(testImg, trainPersonNumMat);

					//old approach from earlier library, just saving for reference if needed...
					/*File testFile = new File(fileName);
					if (testFile.exists()) 
					{
						BufferedImage bImg =  ImageIO.read(testFile);
						System.out.println("created buffered Image..");
						IplImage img = IplImage.createFrom(bImg);
						IplImage snapshot = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
						//System.out.println("got snapshot");
						cvFlip(img, snapshot, 1);
						CvSeq faces = fr.detectFace(img);
						System.out.println("found " + faces.total() + " faces");
						if (faces.total() >= 1)
						{
			    				CvRect r = new CvRect(cvGetSeqElem(faces, faces.total()-1));
		 					int x = r.x(), y = r.y(), w = r.width(), h = r.height();
							String personName = fr.identifyFace(fr.preprocessImage(img, r));

							cvRectangle(img, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
							cvSaveImage("recogTest0.jpg", img);
							System.out.println("identified person as : " + personName);
						}
					}*/
				}
				catch (Exception rErr)
				{
					System.out.println("error in recognition");
					rErr.printStackTrace();
				}	
			}
		
	
	public void saveImage(IplImage recFrame)
	{
							
		//save the image with stamp
		Date now = new Date();
		String strDateToPrint = sdfPrintDate.format(now);
		String strDate = sdfDate.format(now);

                CvFont font = new CvFont();
                cvInitFont(font, CV_FONT_HERSHEY_COMPLEX, 0.5, 0.5, 1.0, 1, CV_AA);
                cvPutText(recFrame,strDateToPrint,cvPoint(recFrame.width()-230, recFrame.height()-10),font,CvScalar.RED);   

		cvSaveImage("Detection-"+strDate+".jpg", recFrame);
	}
}


class MyHandler implements HttpHandler {

	private BufferedImage currImage;

	public void updateImage(BufferedImage newImg)
	{
		currImage=newImg;
	}

  	public void handle(HttpExchange exchange) throws IOException 
	{
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    ImageIO.write(currImage, "png", output);
	    byte[] byteArray = output.toByteArray();

	    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, byteArray.length);
	    exchange.getResponseBody().write(byteArray);
	    exchange.close();
	}

/*    String requestMethod = exchange.getRequestMethod();
    if (requestMethod.equalsIgnoreCase("GET")) {
      Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, 0);

      OutputStream responseBody = exchange.getResponseBody();
      Headers requestHeaders = exchange.getRequestHeaders();
      Set<String> keySet = requestHeaders.keySet();
      Iterator<String> iter = keySet.iterator();
      while (iter.hasNext()) {
        String key = iter.next();
        List values = requestHeaders.get(key);
        String s = key + " = " + values.toString() + "\n";
        responseBody.write(s.getBytes());
      }
      responseBody.close();
    }
  }*/
}
