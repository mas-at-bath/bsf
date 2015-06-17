package edu.bath.rdfUtils.rdfSUMO;

import java.io.ByteArrayInputStream;
import javax.imageio.*;
import java.awt.image.*;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.jivesoftware.smack.XMPPException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.math.*;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.Visualisation;
import java.net.URL;
import java.net.URLClassLoader;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.chart.title.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.category.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SUMOMonitorRDF extends JFrame {
	private boolean alive = true;

	private static String jasonSensorVehicles = "jasonSensorVehicles";
	private static String jasonSensorStates = "jasonSensorStates";
	private static String jasonSensorVehiclesCmds = "jasonSensorVehiclesCmds";

	private static String XMPPServer = "127.0.0.1";
	private static String agServer = "127.0.0.1";
	private static int intervalTime=1;
	private static String testMode = "empty";

	private long simReplayTimeAdjust=0;
	private long lastTime=0;
	private long lastTime2=0;
	private long lastTimeNano=0;
	private int frameNum=1;
	private long ownTimer=0;
	private double incrementCounter=0;
	private int jSensVehCounter=0;
	private int jSensStateCounter=0;
	private double incrementValue=500;
	private double incrementValue2=1000;
	private static boolean noSleep = false;
	private long nanoToMili=1000000;
	private static SensorClient mySimSensorClient;
	JButton saveButton = new JButton("Save Graphs");
	JButton csvButton = new JButton("Write to CSV");
	JButton resetButton = new JButton("Reset Graphs");
	JButton quitButton = new JButton("Quit");
	GridLayout experimentLayout = new GridLayout(2,3);
	private static SUMOMonitorRDF testAgent;
	private final static int maxGap = 20;
	private XYDataset fueldataset, emissionsdataset, lightdataset, vehdataset, gapdataset, speeddataset, cspeeddataset;
	private XYSeriesCollection edgeSpeeddataset, edgeCountdataset;
	private static XYSeries co2series, coseries, hcseries, pmxseries, noxseries, fuelseriesV1, fuelseriesV2, ind1TimeSeries, ind1SpeedSeries, ind2TimeSeries, ind2SpeedSeries, ind3TimeSeries, ind3SpeedSeries, ind4SpeedSeries, ind5SpeedSeries, brakeSeries, totalVehSeries, gapSeries1, allSpeedSeries1, gapSeries1Internal, allSpeedSeries1Internal, gapSeries2, allSpeedSeries2, gapSeries2Internal, allSpeedSeries2Internal, gapSeries3, gapSeries3Internal, allSpeedSeries3, allSpeedSeries3Internal, gapSeries4, allSpeedSeries4, gapSeries4Internal, allSpeedSeries4Internal, c1SpeedSeries, c2SpeedSeries, c3SpeedSeries, c4SpeedSeries, c5SpeedSeries, edgeSpeedSeries, edgeVehCountSeries;
	private static Double fuelTimeZeroOffset = 0d;
	private static Double emissionTimeZeroOffset = 0d;
	private static Double indTimeZeroOffset = 0d;
	private static Double lightTimeZeroOffset = 0d;
	private static Double gapTimeZeroOffset = 0d;
	private static Double cSpeedsTimeZeroOffset = 0d;
	private static Double edgeTimeZeroOffset = 0d;
	private static FileWriter fwFuel,fwEmissions,fwDetectors,fwLights,fwVehicleCount, fwGaps, fwCSpeeds, fwEdgeVals;

	private JFreeChart chart, chart3, lightChart, gapChart, cSpeedChart, edgeChart;
	private long timeBaseline = 0;
	private static String scenarioUsed="";
	private static int brakeCount=0;
	private static int lightTimeCount=0;
	private static Double simTime = -1d;
	//private boolean lockingWrite = false;
	//private boolean publishAllLanes = false;
	private static boolean initialised=false;
	private BufferedImage reloadedImage = null;
	private JLabel imageLabel = null;
	private static String dateTimeVal = "unknown";
	private static File storeDir;
	private long startupTime =0L;
	private long startupDelay =1000L;

	//CopyOnWriteArrayList<NameDataPair> gap3List = new CopyOnWriteArrayList<NameDataPair>();
	
	//on receipt of a new simtime, gather all data points, up until the next sim step, plot them..
	
	public static void main(String[] args) throws Exception 
	{
		if (args.length > 0) 
		{
			String scenarioFlag = args[0];
			System.out.println(scenarioFlag);
			if (scenarioFlag.equals("m25"))
			{
				scenarioUsed = scenarioFlag;
			}
			else if (scenarioFlag.equals("bath"))
			{
				scenarioUsed = scenarioFlag;
			}
			else
			{
				System.out.println("WARNING: didnt understand flag " + scenarioFlag + ", using m25 config");
				scenarioUsed = "m25";
			}
		}
		else
		{
			System.out.println("WARNING: no flag, using m25 config");
		}
	
	    try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
		UIManager.put("swing.boldMetal", Boolean.FALSE);


	DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	Date date = new Date();
	dateTimeVal = dateFormat.format(date);
	System.out.println("Setting date time folder to " + dateTimeVal);
	storeDir = new File(dateTimeVal);
  	try
	{
       		storeDir.mkdir();
     	} 
	catch(Exception fe)
	{
        	System.out.println("Couldn't create dir for logging to for some reason..");
		fe.printStackTrace();
     	}    


        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	
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

	while(mySimSensorClient == null) {
		try {
			mySimSensorClient = new SensorXMPPClient(XMPPServer, "rdfmonitor", "jasonpassword");
			System.out.println("Sim Sensor monitor connected up OK");
		} catch (Exception e1) {
			System.out.println("Exception in establishing client.");
			e1.printStackTrace();
		}
	}
	
	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() 
		{ 
			System.out.println("Shutting down..");
		 }
	});


	
	testAgent.run();	
	}
	
	public SUMOMonitorRDF(String name) {
	      super(name);
        setResizable(true);
	}
	

	private static void createAndShowGUI() 
	{
        	testAgent = new SUMOMonitorRDF("SUMOMonitorRDF");
        	testAgent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	testAgent.addComponentsToPane(testAgent.getContentPane());
        	testAgent.pack();
        	testAgent.setVisible(true);
    	}
	
	private XYDataset createEmissionsDataset() 
	{ 
	    	co2series = new XYSeries("CO2");
        	co2series.add(0.0, 0.0);
        	coseries = new XYSeries("CO");
        	coseries.add(0.0, 0.0);
		hcseries = new XYSeries("HC");
        	hcseries.add(0.0, 0.0);
		pmxseries = new XYSeries("PMx");
        	pmxseries.add(0.0, 0.0);
		noxseries = new XYSeries("NOx");
        	noxseries.add(0.0, 0.0);

		final XYSeriesCollection dataset = new XYSeriesCollection();
        	dataset.addSeries(co2series);
        	dataset.addSeries(coseries);          
		dataset.addSeries(hcseries); 		
		dataset.addSeries(pmxseries);
		dataset.addSeries(noxseries);
        	return dataset;	  
  	}

	private XYDataset createLightDataset() 
	{ 
	    	brakeSeries = new XYSeries("Braking");
        	//brakeSeries.add(0.0, 0.0);

		final XYSeriesCollection dataset = new XYSeriesCollection();
        	dataset.addSeries(brakeSeries);
        	return dataset;	  
  	}

	private XYDataset createSpeedDataset() 
	{ 
		final XYSeriesCollection dataset = new XYSeriesCollection();	 
   	
		//if (publishAllLanes)
		//{
			allSpeedSeries1 = new XYSeries("Vehicle Speeds L1");
        		dataset.addSeries(allSpeedSeries1);
			allSpeedSeries2 = new XYSeries("Vehicle Speeds L2");
        		dataset.addSeries(allSpeedSeries2);
			allSpeedSeries3 = new XYSeries("Vehicle Speeds L3");
        		dataset.addSeries(allSpeedSeries3);
			allSpeedSeries4 = new XYSeries("Vehicle Speeds L4");
        		dataset.addSeries(allSpeedSeries4);
		//}
		//else
		//{
		//	allSpeedSeries3 = new XYSeries("Vehicle Speeds L3");
        //		dataset.addSeries(allSpeedSeries3);
	//	}
        	return dataset;	  
  	}

	private XYSeriesCollection createEdgeSpeedDataset() 
	{ 
		final XYSeriesCollection dataset = new XYSeriesCollection();	 

		edgeSpeedSeries = new XYSeries("Edge Average Speeds");
        	dataset.addSeries(edgeSpeedSeries);

        	return dataset;	  
  	}

	private XYSeriesCollection createEdgeCountDataset() 
	{ 
		final XYSeriesCollection dataset = new XYSeriesCollection();	 

		edgeVehCountSeries = new XYSeries("Edge Vehicle Counts");
        	dataset.addSeries(edgeVehCountSeries);

        	return dataset;	  
  	}

	private XYDataset createCSpeedDataset() 
	{ 
		final XYSeriesCollection dataset = new XYSeriesCollection();	 
		c1SpeedSeries = new XYSeries("C1 Vehicle Speeds");
       		dataset.addSeries(c1SpeedSeries);
		c2SpeedSeries = new XYSeries("C2 Vehicle Speeds");
       		dataset.addSeries(c2SpeedSeries);
		c3SpeedSeries = new XYSeries("C3 Vehicle Speeds");
       		dataset.addSeries(c3SpeedSeries);
		c4SpeedSeries = new XYSeries("C4 Vehicle Speeds");
       		dataset.addSeries(c4SpeedSeries);
		c5SpeedSeries = new XYSeries("C5 Vehicle Speeds");
       		dataset.addSeries(c5SpeedSeries);
        	
		return dataset;	  
  	}

	private XYDataset createGapDataset() 
	{ 
		final XYSeriesCollection dataset = new XYSeriesCollection();

		//if (publishAllLanes)
		//{
	    		gapSeries1 = new XYSeries("Vehicle Gaps L1");	
        		dataset.addSeries(gapSeries1);
	    		gapSeries2 = new XYSeries("Vehicle Gaps L2");	
        		dataset.addSeries(gapSeries2);
	    		gapSeries3 = new XYSeries("Vehicle Gaps L3");	
        		dataset.addSeries(gapSeries3);
	    		gapSeries4 = new XYSeries("Vehicle Gaps L4");	
        		dataset.addSeries(gapSeries4);
		//}
		//else
		//{
		//	gapSeries3 = new XYSeries("Vehicle Gaps L3");	
        	//	dataset.addSeries(gapSeries3);
		//}

        	return dataset;	  
  	}

	private XYDataset createVehCountDataset() 
	{ 
	    	totalVehSeries = new XYSeries("Total Vehicles");

		final XYSeriesCollection dataset = new XYSeriesCollection();
        	dataset.addSeries(totalVehSeries);
        	return dataset;	  
  	}
  
	private XYDataset createFuelDataset() 
	{ 
		fuelseriesV1= new XYSeries("Vehicle 1");
		if (scenarioUsed.equals("m25"))
		{
			fuelseriesV2 = new XYSeries("Vehicle 2");
        		fuelseriesV2.add(0.0, 0.0);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
        	dataset.addSeries(fuelseriesV1);
		if (scenarioUsed.equals("m25"))
		{
			dataset.addSeries(fuelseriesV2);
		}		
        	return dataset;	  
 	}
  
    	private JFreeChart createEmissionsChart(final XYDataset dataset) 
	{
		//for m25, we're interested in what vehicle 2 does, for Bath more interested in vehicle 1
		String title = "Vehicle 1 Emissions";	
		if (scenarioUsed.equals("m25"))
		{
			title = "Vehicle 2 Emissions";	
		}
       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Time (seconds)",                      // x axis label
            		"Quantity (mg)",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}

    	private JFreeChart createEdgeChart(final XYDataset dataset) 
	{
		//for m25, we're interested in what vehicle 2 does, for Bath more interested in vehicle 1
		String title = "Edge values";	

       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Time (seconds)",                      // x axis label
            		"Average speed (m/s)",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}

    	private JFreeChart createLightChart(final XYDataset dataset) 
	{
		//based on received lights (brake, indicator)
		String title = "Total Vehicle Braking";	
       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Time (seconds)",                      // x axis label
            		"Braking instances",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}

    	private JFreeChart createGapChart(final XYDataset dataset) 
	{
		//based on received lights (brake, indicator)
		String title = "Vehicle gaps";	
       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Distance along entire route (m)",                      // x axis label
            		"Distance to vehicle infront (m)",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}

    	private JFreeChart createCSpeedChart(final XYDataset dataset) 
	{
		//based on received lights (brake, indicator)
		String title = "Agent vehicle speeds";	
       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Time (s)",                      // x axis label
            		"Speed (mph)",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}
  
   	private JFreeChart createFuelChart(final XYDataset dataset) 
	{
		String title2 = "Vehicle Fuel Consumption";	
		if (scenarioUsed.equals("bath"))
		{
			title2 = "Vehicle 1 Fuel Consumption";	
		}
       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title2,      // chart title
            		"Time (seconds)",                      // x axis label
            		"Fuel consumed (ml)",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		true,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  		return chart;
  	}
	
	public void addComponentsToPane(final Container pane) 
	{    
        	final JPanel compsToExperiment = new JPanel();
        	compsToExperiment.setLayout(experimentLayout);
        	JPanel controls = new JPanel();
        	controls.setLayout(new GridLayout(1,3));
		
		cspeeddataset = createCSpeedDataset();
		cSpeedChart = createCSpeedChart(cspeeddataset);
		edgeSpeeddataset = createEdgeSpeedDataset();
		edgeCountdataset = createEdgeCountDataset();
		edgeChart = createEdgeChart(edgeSpeeddataset);

		speeddataset = createSpeedDataset();
		gapdataset = createGapDataset();
		gapChart = createGapChart(gapdataset);
		lightdataset = createLightDataset();
		vehdataset = createVehCountDataset();
		lightChart = createLightChart(lightdataset);
		fueldataset = createFuelDataset();
		chart3 = createFuelChart(fueldataset);
		emissionsdataset = createEmissionsDataset();
		chart = createEmissionsChart(emissionsdataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		final XYPlot plot = chart.getXYPlot();
		XYItemRenderer defaultRenderer = new XYLineAndShapeRenderer();
		plot.setRenderer( 0, defaultRenderer );
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		final XYPlot plot3 = chart3.getXYPlot();
		//XYItemRenderer render2= new XYLineAndShapeRenderer(false,true); //this one doesnt join dots
		XYItemRenderer render2 = new XYLineAndShapeRenderer();
		plot3.setRenderer( 0, render2 );
		final ChartPanel chartPanel3 = new ChartPanel(chart3);
		chartPanel3.setPreferredSize(new java.awt.Dimension(500, 270));
		if (scenarioUsed.equals("bath"))
		{
			chart3.removeLegend();
		}

		final ChartPanel chartPanel4 = new ChartPanel(lightChart);
		final XYPlot plot4 = lightChart.getXYPlot();
		ValueAxis axis = plot4.getRangeAxis();
		NumberAxis axis2 = new NumberAxis("Total Vehicles");
		Font font = new Font("Arial", Font.BOLD, 14);
		axis2.setLabelFont(font);
		axis.setLabelFont(font);
		plot4.setRangeAxis(1, axis2);
		plot4.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);	
		XYItemRenderer defaultRendererLight2 = new XYLineAndShapeRenderer();
		XYItemRenderer defaultRendererLight = new XYLineAndShapeRenderer();
		plot4.setDataset(1, vehdataset);
		plot4.setRenderer( 1, defaultRendererLight2 );
		plot4.mapDatasetToRangeAxis(1, 1);
		plot4.setDataset(0, lightdataset);
		plot4.setRenderer( 0, defaultRendererLight );
		chartPanel4.setPreferredSize(new java.awt.Dimension(500, 270));
		//lightChart.removeLegend();	


		final ChartPanel chartPanelEdge = new ChartPanel(edgeChart);
		final XYPlot plotEdge = edgeChart.getXYPlot();
		ValueAxis axisEdge = plotEdge.getRangeAxis();
		NumberAxis axis2Edge = new NumberAxis("Vehicle Count");
		Font fontEdge = new Font("Arial", Font.BOLD, 14);
		axis2Edge.setLabelFont(fontEdge);
		axisEdge.setLabelFont(fontEdge);
		plotEdge.setRangeAxis(1, axis2Edge);
		plotEdge.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);	
		XYItemRenderer defaultRendererLight2Edge = new XYLineAndShapeRenderer();
		XYItemRenderer defaultRendererLightEdge = new XYLineAndShapeRenderer();
		plotEdge.setDataset(1, edgeSpeeddataset );
		plotEdge.setRenderer( 1, defaultRendererLight2Edge );
		plotEdge.mapDatasetToRangeAxis(1, 1);
		plotEdge.setDataset(0, edgeCountdataset );
		plotEdge.setRenderer( 0, defaultRendererLightEdge );
		chartPanelEdge.setPreferredSize(new java.awt.Dimension(500, 270));


		final ChartPanel chartPanel5 = new ChartPanel(gapChart);
		final XYPlot plot5 = gapChart.getXYPlot();
		//XYItemRenderer defaultRendererGap = new XYLineAndShapeRenderer();
		NumberAxis axis3 = new NumberAxis("Vehicle Speeds (mph)");
		axis3.setRange(0.0, 100.0);
		ValueAxis axisA = plot5.getRangeAxis();
		axis3.setLabelFont(font);
		axisA.setLabelFont(font);
		plot5.setRangeAxis(1, axis3);
		plot5.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	
		XYItemRenderer defaultRendererGap2 = new XYLineAndShapeRenderer();
		XYItemRenderer defaultRendererGap = new XYLineAndShapeRenderer();

		plot5.setDataset(1, speeddataset);
		plot5.setRenderer( 1, defaultRendererGap2 );
		plot5.mapDatasetToRangeAxis(1, 1);
		plot5.setDataset(0, gapdataset);
		plot5.setRenderer( 0, defaultRendererGap );
		
        	NumberAxis domain5 = (NumberAxis) plot5.getDomainAxis();
        	domain5.setRange(0.00, 13200.00);
       	 	//domain5.setTickUnit(new NumberTickUnit(0.1));
        	//domain5.setVerticalTickLabels(true);
        	NumberAxis range5 = (NumberAxis) plot5.getRangeAxis();
        	range5.setRange(0.0, 500.0);
        	//range5.setTickUnit(new NumberTickUnit(0.1));
		//plot5.setRenderer( 0, defaultRendererGap );
		chartPanel5.setPreferredSize(new java.awt.Dimension(500, 270));	

		//mod to attempt to load jpeg from file system once bufferedimage written, rather than always having empty graph
		reloadedImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_RGB);  
      		reloadedImage.getGraphics().setColor(Color.white);  
      		reloadedImage.getGraphics().drawString("TEMP HOLDER!",20,20); 

		final ChartPanel chartPanel6 = new ChartPanel(cSpeedChart);
		final XYPlot plot6 = cSpeedChart.getXYPlot();
		XYItemRenderer defaultRenderer6 = new XYLineAndShapeRenderer();
		plot6.setRenderer( 0, defaultRenderer6 );
		chartPanel6.setPreferredSize(new java.awt.Dimension(500, 270));
	

        	compsToExperiment.setPreferredSize(new Dimension(800, 270*2));
		compsToExperiment.add(chartPanel);
		compsToExperiment.add(chartPanel3);
		compsToExperiment.add(chartPanel4);
		compsToExperiment.add(chartPanel6);
		compsToExperiment.add(chartPanelEdge);

		imageLabel = new JLabel(new ImageIcon(reloadedImage));
		compsToExperiment.add(imageLabel);
		controls.add(saveButton);
		controls.add(csvButton);
		controls.add(resetButton);
		controls.add(quitButton);
		
		quitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
				System.exit(0);
            }
        });        
		
		resetButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
				co2series.clear();
				coseries.clear();
				hcseries.clear();
				pmxseries.clear();
				noxseries.clear();
				fuelseriesV1.clear();
				fuelseriesV2.clear();
				ind1TimeSeries.clear();
				ind1SpeedSeries.clear();
				ind2TimeSeries.clear();
				ind2SpeedSeries.clear();
				ind3TimeSeries.clear();
				ind3SpeedSeries.clear();
				ind4SpeedSeries.clear();
				ind5SpeedSeries.clear();
				gapSeries1.clear();
				gapSeries2.clear();
				gapSeries3.clear();
				gapSeries4.clear();
				c1SpeedSeries.clear();
				c2SpeedSeries.clear();
				c3SpeedSeries.clear();
				c4SpeedSeries.clear();
				c5SpeedSeries.clear();
				cSpeedsTimeZeroOffset=0d;
				incrementCounter=0;
				fuelTimeZeroOffset=0d;
				emissionTimeZeroOffset=0d;
				indTimeZeroOffset=0d;
				gapTimeZeroOffset=0d;
				lightTimeZeroOffset=0d;
				edgeTimeZeroOffset=0d;
				timeBaseline = System.currentTimeMillis();
            }
        });
		
	saveButton.addActionListener(new ActionListener()
	{
        	public void actionPerformed(ActionEvent e)
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
			Date date = new Date();
			System.out.println(dateFormat.format(date));
			File filename_png = new File(storeDir, "Emissions-"+dateFormat.format(date)+".png");
			File filename2_png = new File(storeDir, "Detections-"+dateFormat.format(date)+".png");
			File filename3_png = new File(storeDir, "Fuel-"+dateFormat.format(date)+".png");
			File filename4_png = new File(storeDir, "Lights-"+dateFormat.format(date)+".png");
			File filename5_png = new File(storeDir, "Gaps-"+dateFormat.format(date)+".png");
			File filename6_png = new File(storeDir, "JasonSpeeds-"+dateFormat.format(date)+".png");
			try 
			{
				ChartUtilities.saveChartAsPNG(filename_png, chart, 980, 550);
				ChartUtilities.saveChartAsPNG(filename3_png, chart3, 980, 550);
				ChartUtilities.saveChartAsPNG(filename4_png, lightChart, 980, 550);
				ChartUtilities.saveChartAsPNG(filename5_png, gapChart, 980, 550);
				ChartUtilities.saveChartAsPNG(filename6_png, cSpeedChart, 980, 550);
			} 
			catch (IOException ex) 
			{
				throw new RuntimeException("Error saving a file",ex);
			}
            	}
        });

	csvButton.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
				// co2series, coseries, hcseries, pmxseries, noxseries, fuelseriesV1, fuelseriesV2;
				File filename_emi = new File(storeDir, "Emissions-"+dateFormat.format(date)+".csv");
				File filename_fuel = new File(storeDir, "Fuel-"+dateFormat.format(date)+".csv");
				File filename_detectors = new File(storeDir, "Detectors-"+dateFormat.format(date)+".csv");
				File filename_lights = new File(storeDir, "Lights-"+dateFormat.format(date)+".csv");
				File filename_vehCount = new File(storeDir, "Vehicles-"+dateFormat.format(date)+".csv");

				File f_speedC1 = new File(storeDir, "C1Speed-"+dateFormat.format(date)+".csv");
				File f_speedC2 = new File(storeDir, "C2Speed-"+dateFormat.format(date)+".csv");
				File f_speedC3 = new File(storeDir, "C3Speed-"+dateFormat.format(date)+".csv");
				File f_speedC4 = new File(storeDir, "C4Speed-"+dateFormat.format(date)+".csv");
				File f_speedC5 = new File(storeDir, "C5Speed-"+dateFormat.format(date)+".csv");
				

				try {
					fwCSpeeds = new FileWriter(f_speedC1,false);
					fwCSpeeds.write("Time, Speed \n");
					for (int i=0; i < c1SpeedSeries.getItemCount(); i++)
					{
						fwCSpeeds.write(c1SpeedSeries.getDataItem(i).getX() + "," + c1SpeedSeries.getDataItem(i).getY() + " \n");
					}
					fwCSpeeds.close();

					fwCSpeeds = new FileWriter(f_speedC2,false);
					fwCSpeeds.write("Time, Speed \n");
					for (int i=0; i < c2SpeedSeries.getItemCount(); i++)
					{
						fwCSpeeds.write(c2SpeedSeries.getDataItem(i).getX() + "," + c2SpeedSeries.getDataItem(i).getY() + " \n");
					}
					fwCSpeeds.close();

					fwCSpeeds = new FileWriter(f_speedC3,false);
					fwCSpeeds.write("Time, Speed \n");
					for (int i=0; i < c3SpeedSeries.getItemCount(); i++)
					{
						fwCSpeeds.write(c3SpeedSeries.getDataItem(i).getX() + "," + c3SpeedSeries.getDataItem(i).getY() + " \n");
					}
					fwCSpeeds.close();

					fwCSpeeds = new FileWriter(f_speedC4,false);
					fwCSpeeds.write("Time, Speed \n");
					for (int i=0; i < c4SpeedSeries.getItemCount(); i++)
					{
						fwCSpeeds.write(c4SpeedSeries.getDataItem(i).getX() + "," + c4SpeedSeries.getDataItem(i).getY() + " \n");
					}
					fwCSpeeds.close();

					fwCSpeeds = new FileWriter(f_speedC5,false);
					fwCSpeeds.write("Time, Speed \n");
					for (int i=0; i < c5SpeedSeries.getItemCount(); i++)
					{
						fwCSpeeds.write(c5SpeedSeries.getDataItem(i).getX() + "," + c5SpeedSeries.getDataItem(i).getY() + " \n");
					}
					fwCSpeeds.close();


					fwEmissions = new FileWriter(filename_emi,false);
					fwFuel = new FileWriter(filename_fuel,false);
					fwDetectors = new FileWriter(filename_detectors,false);
					fwLights = new FileWriter(filename_lights,false);
					fwVehicleCount = new FileWriter(filename_vehCount,false);

					fwLights.write("Time, BrakeCount \n");
					for (int i=0; i < brakeSeries.getItemCount(); i++)
					{
						fwLights.write(brakeSeries.getDataItem(i).getX() + "," + brakeSeries.getDataItem(i).getY() + " \n");
					}
					fwLights.close();

					fwVehicleCount.write("Time, VehicleCount \n");
					for (int i=0; i < brakeSeries.getItemCount(); i++)
					{
						fwVehicleCount.write(totalVehSeries.getDataItem(i).getX() + "," + totalVehSeries.getDataItem(i).getY() + " \n");
					}
					fwVehicleCount.close();

					fwEmissions.write("CO2, CO, HC, PMX, NOX \n");
					for (int i=0; i < co2series.getItemCount(); i++)
					{
				    		fwEmissions.write(co2series.getDataItem(i).getY() + "," + coseries.getDataItem(i).getY() + "," + hcseries.getDataItem(i).getY() + "," + pmxseries.getDataItem(i).getY() + "," + noxseries.getDataItem(i).getY() + " \n");
					}
					fwEmissions.close();

					
					List<XYSeries> foundSeries = edgeSpeeddataset.getSeries();
					for (XYSeries testSeries : foundSeries)
					{
						File outputF = new File(storeDir, "Edge-"+testSeries.getKey()+"-"+dateFormat.format(date)+".csv");
						fwEdgeVals = new FileWriter(outputF ,false);
						fwEdgeVals.write("Time, AverageSpeed \n");
						for (int i=0; i < testSeries.getItemCount(); i++)
						{
							fwEdgeVals.write(testSeries.getDataItem(i).getX() + "," + testSeries.getDataItem(i).getY() + " \n");
						}
						fwEdgeVals.close();
					}	

					List<XYSeries> foundCountSeries = edgeCountdataset.getSeries();
					for (XYSeries testSeries : foundCountSeries)
					{
						File outputF = new File(storeDir, "Edge-"+testSeries.getKey()+"-"+dateFormat.format(date)+".csv");
						fwEdgeVals = new FileWriter(outputF ,false);
						fwEdgeVals.write("Time, VehicleCount \n");
						for (int i=0; i < testSeries.getItemCount(); i++)
						{
							fwEdgeVals.write(testSeries.getDataItem(i).getX() + "," + testSeries.getDataItem(i).getY() + " \n");
						}
						fwEdgeVals.close();
					}									


					/*if (scenarioUsed.equals("m25"))
					{
 						fwDetectors.write("Time,D1,Time,D2,Time,D3,Time,D4,Time,D5\n");
						int maxLength = ind1SpeedSeries.getItemCount();
						//Ah, can't assume that the first detector will see the most traffic!						
						if (ind2SpeedSeries.getItemCount() > maxLength) {maxLength=ind2SpeedSeries.getItemCount();}
						if (ind3SpeedSeries.getItemCount() > maxLength) {maxLength=ind3SpeedSeries.getItemCount();}
						if (ind4SpeedSeries.getItemCount() > maxLength) {maxLength=ind4SpeedSeries.getItemCount();}
						if (ind5SpeedSeries.getItemCount() > maxLength) {maxLength=ind2SpeedSeries.getItemCount();}
						String[] resultsStr = new String[maxLength];
						//TODO: if its working, combine into one for loop now 
						//System.out.println("adding d1 results");
						for (int d1=0; d1 < maxLength; d1++)
						{
							if (d1 < ind1SpeedSeries.getItemCount())
							{		 
								resultsStr[d1] = ind1SpeedSeries.getDataItem(d1).getX() + "," + ind1SpeedSeries.getDataItem(d1).getY();
							}
							else {resultsStr[d1] = ",";}
						}

						//System.out.println("adding d2 results");
						for (int d2=0; d2 < maxLength ; d2++)
						{
							if (d2 < ind2SpeedSeries.getItemCount())
							{
								resultsStr[d2] = resultsStr[d2] + "," + ind2SpeedSeries.getDataItem(d2).getX() + "," + ind2SpeedSeries.getDataItem(d2).getY();
							}
							else {resultsStr[d2] = resultsStr[d2] + ",,";}
							
						}

						//System.out.println("adding d3 results");
						for (int d3=0; d3 < maxLength ; d3++)
						{
							if (d3 < ind3SpeedSeries.getItemCount())
							{
								resultsStr[d3] = resultsStr[d3] + "," + ind3SpeedSeries.getDataItem(d3).getX() + "," + ind3SpeedSeries.getDataItem(d3).getY();
							}
							else {resultsStr[d3] = resultsStr[d3] + ",,";}
						}

						//System.out.println("adding d4 results");
						for (int d4=0; d4 < maxLength ; d4++)
						{
							if (d4 < ind4SpeedSeries.getItemCount())
							{
								resultsStr[d4] = resultsStr[d4] + "," + ind4SpeedSeries.getDataItem(d4).getX() + "," + ind4SpeedSeries.getDataItem(d4).getY();
							}
							else {resultsStr[d4] = resultsStr[d4] + ",,";}
						}

						//System.out.println("adding d5 results");
						for (int d5=0; d5 < maxLength ; d5++)
						{
							if (d5 < ind5SpeedSeries.getItemCount())
							{
								resultsStr[d5] = resultsStr[d5] + "," + ind5SpeedSeries.getDataItem(d5).getX() + "," + ind5SpeedSeries.getDataItem(d5).getY();
							}
							else {resultsStr[d5] = resultsStr[d5] + ",,";}
						}
						
						//System.out.println("adding return character to lines");
						for (String line : resultsStr)
						{
							//System.out.println(line);
							fwDetectors.write(line + "\n");
						}
						fwDetectors.close();
					}*/

					int fuelNum = fuelseriesV1.getItemCount();
					if (scenarioUsed.equals("m25"))
					{
						if (fuelseriesV2.getItemCount() < fuelNum)
						{
							fuelNum = fuelseriesV2.getItemCount();
						}
					}

					if (scenarioUsed.equals("m25"))
					{
						fwFuel.write("Vehicle1, Vehicle 2 \n");
						for (int j=0; j<fuelNum; j++)
						{
							fwFuel.write(fuelseriesV1.getDataItem(j).getY() + "," + fuelseriesV2.getDataItem(j).getY() + " \n");
						}
					}
					else
					{
						fwFuel.write("Vehicle1 \n");
						for (int j=0; j<fuelNum; j++)
						{
							fwFuel.write(fuelseriesV1.getDataItem(j).getY() + " \n");
						}
					}
					fwFuel.close();				
				} 
				catch (Exception ex) 
				{
					throw new RuntimeException("Error saving a file",ex);
				}
            }
        });
		
	pane.add(compsToExperiment, BorderLayout.NORTH);
        pane.add(new JSeparator(), BorderLayout.CENTER);
        pane.add(controls, BorderLayout.SOUTH);
	}
	
	public void run() 
	{
		timeBaseline = System.currentTimeMillis();	
		startupTime = System.currentTimeMillis();	

		SwingWorker<Void, XYSeries> worker = new SwingWorker<Void, XYSeries>() 
		{

				
			@Override
   			protected Void doInBackground() throws Exception 
			{
				gapSeries1Internal = new XYSeries("Vehicle Gaps L1 Internal Holder");
				allSpeedSeries1Internal = new XYSeries("Vehicle Speeds L1 Internal Holder");
				gapSeries2Internal = new XYSeries("Vehicle Gaps L2 Internal Holder");
				allSpeedSeries2Internal = new XYSeries("Vehicle Speeds L2 Internal Holder");
				gapSeries3Internal = new XYSeries("Vehicle Gaps L3 Internal Holder");
				allSpeedSeries3Internal = new XYSeries("Vehicle Speeds L3 Internal Holder");
				gapSeries4Internal = new XYSeries("Vehicle Gaps L4 Internal Holder");
				allSpeedSeries4Internal = new XYSeries("Vehicle Speeds L4 Internal Holder");

				mySimSensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() 
				{
					@Override
					public void handleIncomingReading(String node, String rdf) 
					{	
						if ((startupTime + startupDelay) < System.currentTimeMillis())
						{			
						try 
						{
							long startProcTime = System.currentTimeMillis();
							DataReading dr = DataReading.fromRDF(rdf);
							String takenBy = dr.getTakenBy();
							double timeStampD = (double) dr.getTimestamp(); 
							Value emissionVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#emissions", null);
							if(emissionVal != null) 
							{	
								if (emissionTimeZeroOffset == 0)
								{
									emissionTimeZeroOffset = timeStampD;
								}
								String emissioncsv = (String) emissionVal.object;
								//System.out.println("got emissions! from " + takenBy + " of " + emissioncsv); 
								String[] splitString = emissioncsv.split(",");
								Double co2Val = Double.parseDouble(splitString[0]);
								Double coVal = Double.parseDouble(splitString[1]);
								Double hcVal = Double.parseDouble(splitString[2]);
								Double pmxVal = Double.parseDouble(splitString[3]);
								Double noxVal = Double.parseDouble(splitString[4]);
						
								if ((scenarioUsed.equals("m25")) && (takenBy.contains("centralMember2")))
								{ 
									co2series.add((timeStampD-emissionTimeZeroOffset)/1000 ,co2Val);
									coseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,coVal);
									hcseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,hcVal);
									pmxseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,pmxVal);
									noxseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,noxVal);
								}
								else if ((scenarioUsed.equals("bath")) && (takenBy.contains("centralMember1")))	
								{
									co2series.add((timeStampD-emissionTimeZeroOffset)/1000 ,co2Val);
									coseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,coVal);
									hcseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,hcVal);
									pmxseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,pmxVal);
									noxseries.add((timeStampD-emissionTimeZeroOffset)/1000 ,noxVal);
								}

							}

							Value lightVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#LightState", null);
							if(lightVal != null) 
							{	
								if (lightTimeZeroOffset==0)
								{
									lightTimeZeroOffset=timeStampD;
									lightTimeCount=1;
								}
								//string should be of format left ind, right ind, brakes, front lights
								String lightString = (String) lightVal.object;
								String lightVals[] = lightString.split(",");
								String braking = lightVals[2];

								//if we're in this 1s interval then aggregate
								double upperVal = lightTimeZeroOffset + (lightTimeCount*1000);
								if ( timeStampD < upperVal)
								{ 
									if (braking.equals("true"))
									{
										brakeCount++;
									}
								}
								else //if we've gone over then publish and increment time counter
								{
									brakeSeries.add((timeStampD-lightTimeZeroOffset)/1000 ,brakeCount);
									brakeCount=0;
									lightTimeCount++;
									//if the last received msg happened to also be braking, add it to the new time interval
									if (braking.equals("true"))
									{
										brakeCount++;
									}	
								}
							}

							Value totalVeh = dr.findFirstValue(null, "http://127.0.0.1/sensors/vehicleCount",  null);
							if (totalVeh != null)
							{
								if (lightTimeZeroOffset==0)
								{
									lightTimeZeroOffset=timeStampD;
									lightTimeCount=1;
								}
								int vehTotal = (int) totalVeh.object;
								totalVehSeries.add((timeStampD-lightTimeZeroOffset)/1000 ,vehTotal);
							}

							Value simTimeVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#simTime",  null);
							if (simTimeVal != null)
							{
								int timeValTemp = (int) simTimeVal.object;
								simTime = timeValTemp *1d;

								XYSeries[] newSeriesList = new XYSeries[8];
								newSeriesList[0] = (XYSeries) gapSeries1Internal.clone();
								newSeriesList[1] = (XYSeries) allSpeedSeries1Internal.clone();
								newSeriesList[2] = (XYSeries) gapSeries2Internal.clone();
								newSeriesList[3] = (XYSeries) allSpeedSeries2Internal.clone();
								newSeriesList[4] = (XYSeries) gapSeries3Internal.clone();
								newSeriesList[5] = (XYSeries) allSpeedSeries3Internal.clone();
								newSeriesList[6] = (XYSeries) gapSeries4Internal.clone();
								newSeriesList[7] = (XYSeries) allSpeedSeries4Internal.clone();

								publish(newSeriesList);
								gapSeries1Internal.clear();
								allSpeedSeries1Internal.clear();
								gapSeries2Internal.clear();
								allSpeedSeries2Internal.clear();
								gapSeries3Internal.clear();
								allSpeedSeries3Internal.clear();
								gapSeries4Internal.clear();
								allSpeedSeries4Internal.clear();
							}				

							Value fuelVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#fuelUsedML", null);
							if(fuelVal != null) 
							{	
								if (fuelTimeZeroOffset==0)
								{
									fuelTimeZeroOffset=timeStampD;
								}
								Double fuelMeasure = (Double) fuelVal.object;			
					
								if (takenBy.contains("centralMember1"))
								{
									fuelseriesV1.add((timeStampD-fuelTimeZeroOffset)/1000 ,fuelMeasure);
								}
								else if (takenBy.contains("centralMember2"))
								{
									fuelseriesV2.add((timeStampD-fuelTimeZeroOffset)/1000 ,fuelMeasure);
								}
							}

							Value routePosVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#RouteDistance", null);
							if (routePosVal != null)
							{
								int laneVal = -1;
								Value edgeLaneVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#EdgeLane", null);
								if (edgeLaneVal != null)
								{
									String laneValString = (String) edgeLaneVal.object;
									String[] splitEdgeLane = laneValString.split("_");
									laneVal = Integer.parseInt(splitEdgeLane[1]);
								}
								else
								{
									System.out.println("no edge found");
								}
					
								Double routeDistance = (Double) routePosVal.object;
								Value distVehAheadVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#VehicleAheadDistance", null); 
								if (distVehAheadVal != null)
								{
							
									Double aheadDistance = (Double) distVehAheadVal.object;

									switch (laneVal) {
										case 0: gapSeries1Internal.add(routeDistance,aheadDistance);
											break;
										case 1: gapSeries2Internal.add(routeDistance,aheadDistance);
											break;
										case 2: gapSeries3Internal.add(routeDistance,aheadDistance);
											break;
										case 3: gapSeries4Internal.add(routeDistance,aheadDistance);
											break;
										default: System.out.println("couldn't process distance dataset");
											break;
										}

								}
						
								Value speedVal = dr.findFirstValue(null, "http://127.0.0.1/sensors/types#vehicleSpeed", null);
								if (speedVal != null)
								{
									//convert ms to mph by 2.2369 multiplier for mph
									Double vehSpeed = (Double) speedVal.object * 2.2369;
									if (vehSpeed > 80)
									{
										//System.out.println("weird, " +takenBy+ " with speed " + vehSpeed);
									}
									Double msSpeed = (Double) speedVal.object;		
							
									if (fuelTimeZeroOffset==0)
									{
										cSpeedsTimeZeroOffset=timeStampD;
									}

									if (takenBy.contains("centralMember1"))
									{
										c1SpeedSeries.add(simTime/1000,vehSpeed);
									}
									else if (takenBy.contains("centralMember2"))
									{
										c2SpeedSeries.add(simTime/1000,vehSpeed);
									}
									else if (takenBy.contains("centralMember3"))
									{
										c3SpeedSeries.add(simTime/1000,vehSpeed);
									}
									else if (takenBy.contains("centralMember4"))
									{
										c4SpeedSeries.add(simTime/1000,vehSpeed);
									}
									else if (takenBy.contains("centralMember5"))
									{
										c5SpeedSeries.add(simTime/1000,vehSpeed);
									}

									switch (laneVal) {
										case 0:	allSpeedSeries1Internal.add(routeDistance,vehSpeed);
											break;	
										case 1:	allSpeedSeries2Internal.add(routeDistance,vehSpeed);
											break;
										case 2:	allSpeedSeries3Internal.add(routeDistance,vehSpeed);
											break;
										case 3:	allSpeedSeries4Internal.add(routeDistance,vehSpeed);
											break;
										default: System.out.println("couldn't process speed dataset");
											break;
										}				
								}
							}

							if (takenBy.equals("http://127.0.0.1/sumo/globalSensor"))
							{
								List<DataReading.Value> drValues = dr.findValues(null, null, null);
								for (Value edgeVal : drValues)
								{
									double timeStampEdge = (double) dr.getTimestamp();
									if (edgeTimeZeroOffset==0)
									{
										edgeTimeZeroOffset=timeStampEdge;
									}
									
									//System.out.println(edgeVal.predicate);
									String[] splitString = edgeVal.predicate.split("/");
									String edgeName = splitString[splitString.length-2];
									//System.out.println("edge is " + edgeName);	

									if (edgeVal.predicate.endsWith("averageSpeed"))
									{
										Double readingVal = (Double) edgeVal.object;
										//if there's only one series, set it to the first edge name
										if (edgeSpeeddataset.getSeriesCount() == 1)
										{
											if (edgeSpeeddataset.getSeries(0).getKey().equals("Edge Average Speeds"))
											{
												edgeSpeeddataset.getSeries(0).setKey(edgeName + "_speed");
											}
										}
										List<XYSeries> foundSeries = edgeSpeeddataset.getSeries();
										boolean foundMatchSeries = false;
										for (XYSeries testSeries : foundSeries)
										{
											if (testSeries.getKey().equals(edgeName + "_speed"))
											{
												testSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);
												foundMatchSeries=true;
											}
										}
										if (!foundMatchSeries)
										{
											System.out.println("Adding : " + edgeVal.predicate  + " as a series");
											XYSeries newSpeedSeries = new XYSeries(edgeName + "_speed");
        										edgeSpeeddataset.addSeries(newSpeedSeries);
											newSpeedSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);

										}
										
										
										//edgeSpeedSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);
									}
									else if (edgeVal.predicate.endsWith("vehicleCount"))
									{
										Integer readingVal = (Integer) edgeVal.object;
										//if there's only one series, set it to the first edge name
										if (edgeCountdataset.getSeriesCount() == 1)
										{
											if (edgeCountdataset.getSeries(0).getKey().equals("Edge Vehicle Counts"))
											{
												edgeCountdataset.getSeries(0).setKey(edgeName + "_count");
											}
										}
										List<XYSeries> foundSeries = edgeCountdataset.getSeries();
										boolean foundMatchSeries = false;
										for (XYSeries testSeries : foundSeries)
										{
											if (testSeries.getKey().equals(edgeName + "_count"))
											{
												testSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);
												foundMatchSeries=true;
											}
										}
										if (!foundMatchSeries)
										{
											System.out.println("Adding : " + edgeVal.predicate  + " as a series");
											XYSeries newCountSeries = new XYSeries(edgeName + "_count");
        										edgeCountdataset.addSeries(newCountSeries);
											newCountSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);

										}

										//edgeVehCountSeries.add((timeStampEdge-edgeTimeZeroOffset)/1000 ,readingVal);
									}
									
								}
							}


						}
						catch(Exception e) 
						{
							System.out.println("error in handling RDF.. ");
							e.printStackTrace();
						}
						}
						else
						{
							System.out.println("ignoring readings as only just started");
						} 
					}
				
				});
				try 
				{
					mySimSensorClient.subscribe(jasonSensorVehicles);
					System.out.println("connected to " + jasonSensorVehicles);
				} 
				catch (Exception e1) 
				{
					System.out.println("Exception while subscribing to sensor " + jasonSensorVehicles);
					e1.printStackTrace();
				}

				return null;
			}

		        @Override
           		protected void process(List<XYSeries> chunks) 
			{
				updateGraph(chunks);
          		}
       		};
       		worker.execute();
		
		int startStep=0;

		while(alive) 
		{
				//System.out.println("stepped " + startStep);
				//startStep++;
				//try {Thread.sleep(500);}
				//catch (Exception e) {}
		}
	}
	

	public void dumpResults(double fNum)
	{
		try 
		{
			File frame_png = new File(storeDir, "GapVidFrame-"+fNum+".png");
			ChartUtilities.saveChartAsPNG(frame_png, gapChart, 640, 480);
			//reloadedImage = ImageIO.read(frame_png);
			Image fullImage = ImageIO.read(frame_png);
			Image resizedImage = fullImage.getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), 0);
			imageLabel.setIcon(new ImageIcon(resizedImage));

				File f_gapS1 = new File(storeDir, "Gap1-"+fNum+".csv");
				FileWriter fwg1 = new FileWriter(f_gapS1,false);
				fwg1.write("RouteDistance, GapDistance \n");
				for (int i=0; i < gapSeries1.getItemCount(); i++)
				{
					fwg1.write(gapSeries1.getDataItem(i).getX() + "," + gapSeries1.getDataItem(i).getY() + " \n");
				}
				fwg1.close();
				File f_gapS2 = new File(storeDir, "Gap2-"+fNum+".csv");
				FileWriter fwg2 = new FileWriter(f_gapS2,false);
				fwg2.write("RouteDistance, GapDistance \n");
				for (int i=0; i < gapSeries2.getItemCount(); i++)
				{
					fwg2.write(gapSeries2.getDataItem(i).getX() + "," + gapSeries2.getDataItem(i).getY() + " \n");
				}
				fwg2.close();
				File f_gapS3 = new File(storeDir, "Gap3-"+fNum+".csv");
				FileWriter fwg3 = new FileWriter(f_gapS3,false);
				fwg3.write("RouteDistance, GapDistance \n");
				for (int i=0; i < gapSeries3.getItemCount(); i++)
				{
					fwg3.write(gapSeries3.getDataItem(i).getX() + "," + gapSeries3.getDataItem(i).getY() + " \n");
				}
				fwg3.close();
				File f_gapS4 = new File(storeDir, "Gap4-"+fNum+".csv");
				FileWriter fwg4 = new FileWriter(f_gapS4,false);
				fwg4.write("RouteDistance, GapDistance \n");
				for (int i=0; i < gapSeries4.getItemCount(); i++)
				{
					fwg4.write(gapSeries4.getDataItem(i).getX() + "," + gapSeries4.getDataItem(i).getY() + " \n");
				}
				fwg4.close();
				File f_speedS1 = new File(storeDir, "Speed1-"+fNum+".csv");
					FileWriter fws1 = new FileWriter(f_speedS1,false);
					fws1.write("RouteDistance, Speed \n");
					for (int i=0; i < allSpeedSeries1.getItemCount(); i++)
				{
					fws1.write(allSpeedSeries1.getDataItem(i).getX() + "," + allSpeedSeries1.getDataItem(i).getY() + " \n");
				}
				fws1.close();
				File f_speedS2 = new File(storeDir, "Speed2-"+fNum+".csv");
				FileWriter fws2 = new FileWriter(f_speedS2,false);
				fws2.write("RouteDistance, Speed \n");
				for (int i=0; i < allSpeedSeries2.getItemCount(); i++)
				{
					fws2.write(allSpeedSeries2.getDataItem(i).getX() + "," + allSpeedSeries2.getDataItem(i).getY() + " \n");
				}
				fws2.close();
				File f_speedS3 = new File(storeDir, "Speed3-"+fNum+".csv");
				FileWriter fws3 = new FileWriter(f_speedS3,false);
				fws3.write("RouteDistance, Speed \n");
				for (int i=0; i < allSpeedSeries3.getItemCount(); i++)
				{
					fws3.write(allSpeedSeries3.getDataItem(i).getX() + "," + allSpeedSeries3.getDataItem(i).getY() + " \n");
				}
				fws3.close();
				File f_speedS4 = new File(storeDir, "Speed4-"+fNum+".csv");
				FileWriter fws4 = new FileWriter(f_speedS4,false);
				fws4.write("RouteDistance, Speed \n");
				for (int i=0; i < allSpeedSeries4.getItemCount(); i++)
				{
					fws4.write(allSpeedSeries4.getDataItem(i).getX() + "," + allSpeedSeries4.getDataItem(i).getY() + " \n");
				}
				fws4.close();
		} 
		catch (IOException ex) 
		{
			throw new RuntimeException("Error saving a file",ex);
		}

	}

	public void updateGraph(List<XYSeries> newVals)
	{
		gapChart.setTitle("Vehicle gaps at " + (simTime/1000) + "s");
		XYSeries tempGapSeries1 = newVals.get(0);
		for (int i=0; i <tempGapSeries1.getItemCount(); i++ )
		{
			gapSeries1.add(tempGapSeries1.getDataItem(i));
		}

		XYSeries tempSpeedSeries1 = newVals.get(1);
		for (int j=0; j <tempSpeedSeries1.getItemCount(); j++ )
		{
			allSpeedSeries1.add(tempSpeedSeries1.getDataItem(j));
		}

		XYSeries tempGapSeries2 = newVals.get(2);
		for (int i=0; i <tempGapSeries2.getItemCount(); i++ )
		{
			gapSeries2.add(tempGapSeries2.getDataItem(i));
		}

		XYSeries tempSpeedSeries2 = newVals.get(3);
		for (int j=0; j <tempSpeedSeries2.getItemCount(); j++ )
		{
			allSpeedSeries2.add(tempSpeedSeries2.getDataItem(j));
		}

		XYSeries tempGapSeries3 = newVals.get(4);
		for (int i=0; i <tempGapSeries3.getItemCount(); i++ )
		{
			gapSeries3.add(tempGapSeries3.getDataItem(i));
		}

		XYSeries tempSpeedSeries3 = newVals.get(5);
		for (int j=0; j <tempSpeedSeries3.getItemCount(); j++ )
		{
			allSpeedSeries3.add(tempSpeedSeries3.getDataItem(j));
		}

		XYSeries tempGapSeries4 = newVals.get(6);
		for (int i=0; i <tempGapSeries4.getItemCount(); i++ )
		{
			gapSeries4.add(tempGapSeries4.getDataItem(i));
		}

		XYSeries tempSpeedSeries4 = newVals.get(7);
		for (int j=0; j <tempSpeedSeries4.getItemCount(); j++ )
		{
			allSpeedSeries4.add(tempSpeedSeries4.getDataItem(j));
		}

		double newfNum = 1000000+simTime;
		dumpResults(newfNum);
				
		gapSeries1.clear();
		gapSeries2.clear();
		gapSeries3.clear();
		gapSeries4.clear();
		allSpeedSeries1.clear();
		allSpeedSeries2.clear();
		allSpeedSeries3.clear();
		allSpeedSeries4.clear();
	}
						
	public void testWait(long delayVal)
	{
		long start = System.nanoTime();
		long end=0;
		do{
			end = System.nanoTime();
		}while(start + delayVal >= end);
	}
}

