package edu.bath.faceRecog;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
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
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Pointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;

//for 0.3 or so
/*import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;
import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import com.googlecode.javacv.FFmpegFrameGrabber;
import static com.googlecode.javacpp.Loader.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;*/

import java.io.File;
import java.io.FilenameFilter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.nio.IntBuffer;


public class FaceRecogBSF extends Sensor {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	private static SensorClient sensorClient;
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
//	private FaceRecognition faceRecognition;
	private CvMat trainPersonNumMat;

	private SimpleDateFormat sdfDate; 
	private SimpleDateFormat sdfPrintDate;
	private static MyHandler webHandler;
	private static boolean useXMPP=false;
	private static boolean useMQTT=false;
	private static FaceRecogBSF ps;
	private static final String CASCADE_FILE = "./data/haarcascade_frontalface_alt.xml";
	private static final String CASCADE_FILE_OLD = "./data/haarcascade_frontalface_alt_cv2.xml";
	final CvHaarClassifierCascade cascade_old = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_OLD));
	private CascadeClassifier cascade;
	private BasicFaceRecognizer faceRecognitionObj;
	private CvMemStorage faceStorage;
	private OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	private OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
	private Java2DFrameConverter converterJ2D = new Java2DFrameConverter();
	private ArrayList<String> mappedIDs = new ArrayList<String>();
	private CanvasFrame canvasFrame;
	private boolean displayLocal = false;

	public FaceRecogBSF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
	}

	public FaceRecogBSF(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
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

		
		System.out.println("Using defaults: " + XMPPServer + ", " + componentName + ", jasonpassword, jasonSensor, http://127.0.0.1/AOISensors, http://127.0.0.1/ID/FaceRecog");
		if (useXMPP)
		{
			System.out.println("Using XMPP");
			ps = new FaceRecogBSF(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/ID", "http://127.0.0.1/ID/FaceRecog");
		}
		else if (useMQTT)
		{
			System.out.println("Using MQTT");
			ps = new FaceRecogBSF(XMPPServer, componentName, "jasonpassword", homeSensors , "http://127.0.0.1/ID", "http://127.0.0.1/ID/FaceRecog", true, 0);
		}

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
		
		if (useXMPP)
		{
			System.out.println("XMPP subscription");
			while(sensorClient == null) 
			{
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
			System.out.println("MQTT subscription");
			try {
				sensorClient = new SensorMQTTClient(XMPPServer, componentName+"-receiver");
				System.out.println("connected subscriber " +componentName+"-receiver");
			} catch (Exception e1) {
				System.out.println("Exception in establishing MQTT client.");
				e1.printStackTrace();
			}
		}

		Loader.load(opencv_objdetect.class);
		cascade = new CascadeClassifier(CASCADE_FILE);

		sdfDate = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
		sdfPrintDate = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss.SSS");

		CvMemStorage storage = CvMemStorage.create();
		faceStorage = CvMemStorage.create();

		//this training method was based on earlier lib, and instead now provides the ability to process a number of images, and 
		//extract the face region out to a predefined width, to be used in the test file of the learn method above
		boolean trainingImages = false;
		if (trainingImages)
		{
			createFaceSamples();
		}

		//build a new training file based on images stored in ./data/NameX
		boolean trainNew = true;
		if (trainNew)
		{
			trainFromSamples();
		}
		else
		{
			faceRecognitionObj = createFisherFaceRecognizer();
			//BasicFaceRecognizer faceRecognitionObj = createEigenFaceRecognizer();
			faceRecognitionObj.load("trainingResult.set");
		}

		//test recognitiion
		boolean runRecognition=false;
		if (runRecognition)
		{
			runRecognition();
		}	


	
		startupTime=System.currentTimeMillis();

		System.out.println("is connected? " + sensorClient.checkIsConnected());

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
		try {
			sensorClient.subscribe(homeSensors);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to " + homeSensors);
			e1.printStackTrace();
		}

		System.out.println("starting webcam");
		//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("/dev/video0");
		IplImage frame = null;
		Frame nativeFrame = null;
		
		try
		{
              		/*grabber.setFormat("V4L2");
                   	grabber.setFrameRate(10);
                   	grabber.setOption("max_picture_buffer", "1024*100");
                  	grabber.setOption("probesize","192");
                   	grabber.setOption("max_index_size","1024*50");*/
			//grabber.setImageWidth(1280);
			//grabber.setImageHeight(720);
			grabber.setImageWidth(640);
			grabber.setImageHeight(480);
        		grabber.start();
			nativeFrame = grabber.grab();
			frame = converter.convert(nativeFrame);
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

		//use these if you want to see local view
		if (displayLocal)
		{
			canvasFrame = new CanvasFrame("Preview");
			canvasFrame.setCanvasSize(frame.width(), frame.height());
		}

		while(alive) 
		{
			long currentLoopStartedNanoTime = System.nanoTime();
			//debug purposes only 
			//generateAndSendMsg("http://127.0.0.1/detections/people", "vin");

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
					System.out.println("Error handling received RDF");
					System.out.println(e);
				}
				rdfFaceRecogition = pendingMessages.poll();
				
			}

			try
			{

				if (frame != null)
				{
					nativeFrame = grabber.grab();
					webHandler.updateImage(converterJ2D.convert(nativeFrame));
					frame = converter.convert(nativeFrame);
		    			cvClearMemStorage(storage);
					cvSmooth(grayImage, grayImage, CV_GAUSSIAN, 9, 9, 2, 2);

					if (grayImage == null)
					{
						grayImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
					}
					if (diffImage == null)
					{
						diffImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
					}
		                	cvCvtColor(frame, grayImage, CV_BGR2GRAY);
					

					if (firstFrame)
					{
						tempFrame = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
						firstFrame=false;
						saveImage(frame);
					}
					else
					{
						cvAbsDiff(grayImage, tempFrame, diffImage);
						cvThreshold(diffImage, diffImage, 64, 255, CV_THRESH_BINARY);			

						// recognize contours
						CvSeq contour = new CvSeq(null);
						cvFindContours(diffImage, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

						int contourCount=0;
						while (contour != null && !contour.isNull()) 
						{
						    	if (contour.elem_size() > 0) 
							{
								contourCount++;
								//drawContours(diffImage, contour, 4, new Scalar(40, 233, 45,0 ));
						   	}
						    	contour = contour.h_next();
						}
						if (contourCount > 0)
						{
							Date now = new Date();
							String strDateToPrint = sdfPrintDate.format(now);
							//System.out.println(strDateToPrint + ", Detected Movement!! Contours: " + contourCount);
							Mat mat = converterToMat.convertToMat(nativeFrame);
							//try to recognise anyone
           						CvSeq faces = detectFace(mat, grayImage);
							if (faces.total() > 0)
							{
								boolean foundKnownFace = false;

								CvRect r = new CvRect(cvGetSeqElem(faces, faces.total()-1));
	 							int x = r.x(), y = r.y(), w = r.width(), h = r.height();
								IplImage foundFace = preprocessImage(frame, r);
								//BufferedImage bf = converterJ2D.convert(converter.convert(foundFace));
								Mat faceResized = converterToMat.convertToMat(converter.convert(foundFace));
								
								if (displayLocal)
								{					
									canvasFrame.showImage(converter.convert(foundFace));
								}
							
								int[] plabel = new int[1];
								double[] pconfidence = new double[1];
								//int predictedLabel = faceRecognitionObj.predict(faceResized);
								faceRecognitionObj.predict(faceResized, plabel, pconfidence);
								int predictedLabel = plabel[0];
								double confidence = pconfidence[0];

								System.out.println("Predicted label: " + mappedIDs.get(predictedLabel-1) + " at " + confidence + " confidence");
							
								if (confidence < 1000)
								{
		    							System.out.print(strDateToPrint + ", seen: " + mappedIDs.get(predictedLabel-1) + " with " + confidence + " confidence");
									generateAndSendMsg("http://127.0.0.1/detections/people", mappedIDs.get(predictedLabel-1));
									foundKnownFace=true;
								}
								if (!foundKnownFace)
								{
									//System.out.println("sending unknown movement");
									generateAndSendMsg("http://127.0.0.1/detections/movement", "unknown");
								}
							}
							else
							{
								//System.out.println("no movement..");
							}
						}
					}
					cvCopy(grayImage, tempFrame, null);
				
				}

			}
			catch (Exception cvE)
			{
				System.out.println("error grabbing image");
				cvE.printStackTrace();
			}

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

	public synchronized void generateAndSendMsg(String type, String msg) {

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

	private void trainFromSamples()
	{
		File testLoc = new File("./data");
		String[] names = testLoc.list();
		int label = 1;
		int counter = 0;
		int numImages = 0;
		FilenameFilter imgFilter = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			    }
			};

		for(String name : names)
		{
			String dirPath = "./data/" + name;
    			if (new File(dirPath).isDirectory() && !name.equals("testData"))
    			{
				File subLoc = new File(dirPath);
				File[] imageFiles = subLoc.listFiles(imgFilter);
				numImages = numImages + imageFiles.length;
			}
		}
		System.out.println("found " + numImages + " total images");
		MatVector images = new MatVector(numImages);

        	Mat labels = new Mat(numImages, 1, CV_32SC1);
        	IntBuffer labelsBuf = labels.getIntBuffer();

		for(String name : names)
		{
			String dirPath = "./data/" + name;
    			if (new File(dirPath).isDirectory() && !name.equals("testData"))
    			{
				File subLoc = new File(dirPath);
				File[] imageFiles = subLoc.listFiles(imgFilter);

				//String[] imageNames = subLoc.list();
				int maxImages = imageFiles.length;
				System.out.println("for training subject " + name + " found " + maxImages + " images");
				mappedIDs.add(name);
				System.out.println("added " + name + " in " + mappedIDs.size());

				for (File image : imageFiles)
				{
					//System.out.println("adding " + image.getAbsoluteFile());
            				Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
           				images.put(counter, img);
           				labelsBuf.put(counter, label);
            				counter++;
        			}
				label++;
			}

		}
 		faceRecognitionObj = createFisherFaceRecognizer();
        	//faceRecognitionObj = createEigenFaceRecognizer();
        	//faceRecognitionObj = createLBPHFaceRecognizer()

        	faceRecognitionObj.train(images, labels);
		faceRecognitionObj.save("trainingResult.set");
	}

	private void createFaceSamples()
	{
		File testLoc = new File("./data/testData");
		String[] names = testLoc.list();

		for(String name : names)
		{
			String dirPath = "./data/testData/" + name;
    			if (new File(dirPath).isDirectory())
    			{
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
							IplImage origImg = cvLoadImage(fileName);
							IplImage snapshot = cvCreateImage(cvGetSize(origImg), origImg.depth(), origImg.nChannels());
							cvFlip(origImg, snapshot, 1);
							Mat mat = converterToMat.convertToMat(converter.convert(origImg));
							IplImage grayImage = IplImage.create(origImg.width(), origImg.height(), IPL_DEPTH_8U, 1);
		                			cvCvtColor(origImg, grayImage, CV_BGR2GRAY);
           						CvSeq faces = detectFace(mat, grayImage);

							System.out.println("found " + faces.total() + " faces");
							if (faces.total() >= 1)
							{
            							CvRect r = new CvRect(cvGetSeqElem(faces, faces.total()-1));
 								int x = r.x(), y = r.y(), w = r.width(), h = r.height();
								totalImages[foundImages] = preprocessImage(origImg, r);
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
		String testSubjectName = "vin2";
		String dirPath = "./data/" + testSubjectName;
    		if (new File(dirPath).isDirectory())
    		{
			File subLoc = new File(dirPath);
			String[] imageNames = subLoc.list();
			int maxImages = imageNames.length;
			System.out.println("for test subject " + testSubjectName + " found " + maxImages + " images");
			IplImage[] totalImages = new IplImage[maxImages];

			try {
				for (int im=0; im <maxImages; im++)
				{
					String fileName = dirPath + "/" + imageNames[im];
					File imgFile = new File(fileName);
					if (imgFile.exists()) 
					{
						System.out.println("testing " + fileName);
						IplImage testImg = cvLoadImage(fileName);
						Mat faceResized = converterToMat.convertToMat(converter.convert(testImg));
						Mat greyMat = new Mat();
						cvtColor(faceResized, greyMat, COLOR_BGRA2GRAY);
							
						int[] plabel = new int[1];
						double[] pconfidence = new double[1];
						faceRecognitionObj.predict(greyMat, plabel, pconfidence);
						int predictedLabel = plabel[0];
						double confidence = pconfidence[0];

        					System.out.println("Predicted label: " + mappedIDs.get(predictedLabel-1) + " at " + confidence + " confidence");
					}
				}
			}
				
			catch (Exception rErr)
			{
				System.out.println("error in recognition");
				rErr.printStackTrace();
			}	
		}
	}

	protected CvSeq detectFace(Mat originalImageMat, IplImage greyImgIpl) {
                CvSeq faces = null;
		cvClearMemStorage(faceStorage);
		//VB: TODO tidy this up! alot of wasted processing, maybe put it in an alternative method that just doesnt get used..
			
                try {
			CvRect facesDetection = new CvRect(null);
			Mat greyMat = new Mat();
			cvtColor(originalImageMat, greyMat, COLOR_BGRA2GRAY);
			equalizeHist(greyMat, originalImageMat);
			RectVector faceRect = new RectVector();

			//OLD
			//cascade.detectMultiScale(originalImage, faceRect);
			//cascade.detectMultiScale(greyMat, faceRect, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH, new Size(), new Size(greyMat.cols(), greyMat.rows()));
                        //faces = cvHaarDetectObjects(originalImage, cascade, faceStorage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
			//faces = cvHaarDetectObjects(originalImage, cascade, storage, 1.2, 2, CV_HAAR_DO_CANNY_PRUNING);


			faces = cvHaarDetectObjects(greyImgIpl, cascade_old, faceStorage, 1.1, 2, CV_HAAR_DO_ROUGH_SEARCH | CV_HAAR_FIND_BIGGEST_OBJECT);
			faces = cvHaarDetectObjects(greyImgIpl, cascade_old, faceStorage, 1.1, 2, CV_HAAR_DO_CANNY_PRUNING);

		

                } catch (Exception e) {
			System.out.println("Error in detectFace method");
                        e.printStackTrace();
                }
                return faces;
        }

        public IplImage preprocessImage(IplImage image, CvRect r){
                IplImage gray = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
                //IplImage roi = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
		//VB: TODO: eugh hardcoded..
		IplImage roi = IplImage.create(92,112, IPL_DEPTH_8U, 1);

                CvRect r1 = cvRect(r.x()-20, r.y()-50, r.width()+40, r.height()+100);
                cvCvtColor(image, gray, CV_BGR2GRAY);
                cvSetImageROI(gray, r1);
                cvResize(gray, roi, CV_INTER_LINEAR);
                cvEqualizeHist(roi, roi);
                return roi;
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

		Headers responseHeaders=exchange.getResponseHeaders();
		responseHeaders.set("Refresh", "1");

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
