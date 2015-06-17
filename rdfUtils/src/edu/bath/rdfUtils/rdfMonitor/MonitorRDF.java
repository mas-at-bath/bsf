package edu.bath.rdfUtils.rdfMonitor;

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
import java.math.*;
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
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MonitorRDF extends JFrame {
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
	private long lastTimeNano=0;
	private long ownTimer=0;
	private double incrementCounter=0;
	private int jSensVehCounter=0;
	private int jSensStateCounter=0;
	private double incrementValue=1000;
	private static boolean noSleep = false;
	private long nanoToMili=1000000;
	private static SensorClient mySimSensorClient;
	JButton saveButton = new JButton("Save Graphs");
	JButton resetButton = new JButton("Reset Graphs");
	JButton quitButton = new JButton("Quit");
	GridLayout experimentLayout = new GridLayout(2,2);
	private static MonitorRDF testAgent;
	private final static int maxGap = 20;
	private XYDataset dataset, delaydataset;
	private XYSeries vehseries, stateseries, totalseries, delayseries, pubseries;
	private  DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
	private  DefaultCategoryDataset jasonBarDataset = new DefaultCategoryDataset();
	private JFreeChart chart, chart2, chart3, chart4;
	private long timeBaseline = 0;
	
	ArrayList<BarChartDataPair> barChartDataList=new ArrayList<BarChartDataPair>();	
	ArrayList<BarChartDataPair> jasonBarChartDataList=new ArrayList<BarChartDataPair>();	
	
	public static void main(String[] args) throws Exception {
	
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
	
	public MonitorRDF(String name) {
	      super(name);
        setResizable(true);
	}
	
	 private JFreeChart createBarChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createBarChart(
		"RDF Message Types", "Type", "Quantity", dataset,
		PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}
	private JFreeChart createJasonBarChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createBarChart(
		"Jason Message Types", "Type", "Quantity", dataset,
		PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}
	
	private static void createAndShowGUI() 
	{
        testAgent = new MonitorRDF("MonitorRDF");
        testAgent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testAgent.addComponentsToPane(testAgent.getContentPane());
        testAgent.pack();
        testAgent.setVisible(true);
    }
	
	  private XYDataset createDataset() { 
	    vehseries = new XYSeries("Vehicle Msgs");
        vehseries.add(0.0, 0.0);

        stateseries = new XYSeries("Jason State Msgs");
        stateseries.add(0.0, 0.0);
		
	totalseries = new XYSeries("Total Msgs");
        totalseries.add(0.0, 0.0);
		
		pubseries = new XYSeries("Published Msgs");
        pubseries.add(0.0, 0.0);

		final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(vehseries);
        dataset.addSeries(stateseries);          
		dataset.addSeries(totalseries); 		
		dataset.addSeries(pubseries);
        return dataset;	  
  }
  
  	  private XYDataset createDelayDataset() { 
	    delayseries = new XYSeries("Delay");
        delayseries.add(0.0, 0.0);

		final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(delayseries);		
        return dataset;	  
  }
  
    private JFreeChart createChart(final XYDataset dataset) {

       final JFreeChart chart = ChartFactory.createXYLineChart(
            "RDF Message Volume",      // chart title
            "Time (seconds)",                      // x axis label
            "Number of messages",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
  return chart;
  }
  
   private JFreeChart createDelayChart(final XYDataset dataset) {

       final JFreeChart chart = ChartFactory.createXYLineChart(
            "Message transmission delays",      // chart title
            "Time (seconds)",                      // x axis label
            "Delay (milliseconds)",                      // y axis label
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
		
		dataset = createDataset();
		chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		final XYPlot plot = chart.getXYPlot();
		XYItemRenderer defaultRenderer = new XYLineAndShapeRenderer();
		plot.setRenderer( 0, defaultRenderer );
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		
		//final XYDataset dataset2 = createDataset();
		chart2 = createBarChart(barDataset);
		final ChartPanel chartPanel2 = new ChartPanel(chart2);
		chartPanel2.setPreferredSize(new java.awt.Dimension(500, 270));
		chart2.removeLegend();
		
		delaydataset = createDelayDataset();
		chart3 = createDelayChart(delaydataset);
		final XYPlot plot3 = chart3.getXYPlot();
		XYItemRenderer render2= new XYLineAndShapeRenderer(false,true);
		plot3.setRenderer( 0, render2 );
		final ChartPanel chartPanel3 = new ChartPanel(chart3);
		chartPanel3.setPreferredSize(new java.awt.Dimension(500, 270));
		chart3.removeLegend();
		
		chart4 = createJasonBarChart(jasonBarDataset);
		final ChartPanel chartPanel4 = new ChartPanel(chart4);
		chartPanel4.setPreferredSize(new java.awt.Dimension(500, 270));
		chart4.removeLegend();

        compsToExperiment.setPreferredSize(new Dimension(800, 270*2));
		compsToExperiment.add(chartPanel);
        compsToExperiment.add(chartPanel2);
		compsToExperiment.add(chartPanel3);
		compsToExperiment.add(chartPanel4);
		
		controls.add(saveButton);
		controls.add(resetButton);
		controls.add(quitButton);
		
		quitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
				System.exit(0);
            }
        });
		
		resetButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
				delayseries.clear();
				vehseries.clear();
				stateseries.clear();
				totalseries.clear();
				barDataset.clear();
				pubseries.clear();
				barChartDataList.clear();
				incrementCounter=0;
				timeBaseline = System.currentTimeMillis();
            }
        });
		
		saveButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
				File filename_png = new File("MsgVolume-"+dateFormat.format(date)+".png");
				File filename2_png = new File("MsgTypes-"+dateFormat.format(date)+".png");
				File filename3_png = new File("MsgDelays-"+dateFormat.format(date)+".png");

				//create csv files too
				File filename_msgVolVeh = new File("MsgVolVeh-"+dateFormat.format(date)+".csv");
				File filename_msgVolPub = new File("MsgVolPub-"+dateFormat.format(date)+".csv");
				File filename_msgVolJState = new File("MsgVolJState-"+dateFormat.format(date)+".csv");
				File filename_msgDelay = new File("MsgDelay-"+dateFormat.format(date)+".csv");
				   try {
					FileWriter fwVolVeh = new FileWriter(filename_msgVolVeh,false);
					FileWriter fwVolPub = new FileWriter(filename_msgVolPub,false);
					FileWriter fwDelay = new FileWriter(filename_msgDelay,false);
					FileWriter fwVolJState = new FileWriter(filename_msgVolJState,false);

					fwVolVeh.write("Time, VehMsgCount \n"); 
					for (int i=0; i < vehseries.getItemCount(); i++)
					{
						fwVolVeh.write(vehseries.getDataItem(i).getX() + "," + vehseries.getDataItem(i).getY() + " \n");
					}
					fwVolVeh.close();

					fwVolPub.write("Time, VehMsgPublished \n"); 
					for (int i=0; i < pubseries.getItemCount(); i++)
					{
						fwVolPub.write(pubseries.getDataItem(i).getX() + "," + pubseries.getDataItem(i).getY() + " \n");
					}
					fwVolPub.close();

					fwVolJState.write("Time, JState \n"); 
					for (int i=0; i < stateseries.getItemCount(); i++)
					{
						fwVolJState.write(stateseries.getDataItem(i).getX() + "," + stateseries.getDataItem(i).getY() + " \n");
					}
					fwVolJState.close();



					fwDelay.write("Time, Delay \n"); 
					for (int i=0; i < delayseries.getItemCount(); i++)
					{
						fwDelay.write(delayseries.getDataItem(i).getX() + "," + delayseries.getDataItem(i).getY() + " \n");
					}
					fwDelay.close();

				    	ChartUtilities.saveChartAsPNG(filename_png, chart, 980, 550);
					ChartUtilities.saveChartAsPNG(filename2_png, chart2, 980, 550);
					ChartUtilities.saveChartAsPNG(filename3_png, chart3, 980, 550);
					} catch (IOException ex) {
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
		//BarChartDataPair aNewBarPair = new BarChartDataPair("test",1);
		timeBaseline = System.currentTimeMillis();
		mySimSensorClient.addHandler(jasonSensorVehicles, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					if(dr.findFirstValue(null, null, null) != null) 
					{	
						
						String tempReading = (String)dr.findFirstValue(null, null, null).predicate;
						String[] splitString = tempReading.split("/");
						updateBarChartData(splitString[splitString.length-1]);
						jSensVehCounter++;
						Long deltaTime = System.currentTimeMillis() - dr.getTimestamp() - simReplayTimeAdjust;
						delayseries.add(incrementCounter,deltaTime);
					}
				}catch(Exception e) {}
			}
		});
		try {
			mySimSensorClient.subscribe(jasonSensorVehicles);
			System.out.println("connected to " + jasonSensorVehicles);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor " + jasonSensorVehicles);
			e1.printStackTrace();
		}
		
		mySimSensorClient.addHandler(jasonSensorStates, new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					if(dr.findFirstValue(null, null, null) != null) 
					{		
						DataReading.Value tempVal = dr.findFirstValue(null, null, null);
						String tempReading = tempVal.predicate;
						String[] splitString = tempReading.split("/");
						updateBarChartData(splitString[splitString.length-1]);
						jSensStateCounter++;
						Long deltaTime = System.currentTimeMillis() - dr.getTimestamp() - simReplayTimeAdjust;
						delayseries.add(incrementCounter,deltaTime);
						System.out.println("JSTATE: " + tempVal.object);
					}
				}catch(Exception e) {}
			}
		});
		try {
			mySimSensorClient.subscribe(jasonSensorStates);
			System.out.println("connected to " + jasonSensorStates);
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor " + jasonSensorStates);
			e1.printStackTrace();
		}
		
		mySimSensorClient.addHandler("simStateSensor", new ReadingHandler() 
		{
			@Override
			public void handleIncomingReading(String node, String rdf) 
			{
				//System.out.println("processing a sim state msg");
				try {
					DataReading dr = DataReading.fromRDF(rdf);
					Value reqVal = dr.findFirstValue(null, "http://127.0.0.1/simDefinitions/publishedRate", null);
					if(reqVal != null) 
					{
						String tempRate = (String)reqVal.object;
						if (tempRate != null)
						{
							int finalRate = Integer.parseInt(tempRate);
							long readingTime = dr.getTimestamp();
							double rTime = (readingTime - timeBaseline) / 1000 ;
							int finalTime = (int) Math.round(rTime);
							//System.out.println("got a published rate of " + tempRate + "relative to my time was sent at " + finalTime);
							if (finalTime >= 0)
							{
								pubseries.add(finalTime,finalRate);
							}
						}
					}
					else if(dr.findFirstValue(null, null, null) != null) 
					{		
						String tempReading = (String)dr.findFirstValue(null, "http://127.0.0.1/simDefinitions/simTime", null).object;
						//System.out.println("received a sim state time: " + tempReading + ", so need to adjust baseline time for delta time");
						if (simReplayTimeAdjust == 0)
						{
							Long receivedTime = Long.parseLong(tempReading);
							//simReplayTimeAdjust = System.currentTimeMillis() - receivedTime;
							//System.out.println("adjusting time replay time factor to " + simReplayTimeAdjust);
						}

					}
				}catch(Exception e) {}
			}
		});
		try {
			mySimSensorClient.subscribe("simStateSensor");
			System.out.println("connected to " + "simStateSensor");
		} catch (Exception e1) {
			System.out.println("Exception while subscribing to sensor " + "simStateSensor");
			e1.printStackTrace();
		}
	
		while(alive) 
		{
				long newTime = System.currentTimeMillis();
				if (lastTime+incrementValue < newTime)
				{
					lastTime=newTime;
					vehseries.add(incrementCounter,jSensVehCounter);
					stateseries.add(incrementCounter,jSensStateCounter);
					totalseries.add(incrementCounter,jSensVehCounter+jSensStateCounter);
					incrementCounter=incrementCounter+(incrementValue/1000);
					jSensVehCounter=0;
					jSensStateCounter=0;
				}		
		}
	}
	
	private void updateBarChartData(String updateItem)
	{
		boolean updatedItem = false;
		for (BarChartDataPair barPair : barChartDataList) {
			if (barPair.getName().equals(updateItem))
			{
				barPair.incrementQuantity();
				barDataset.setValue(barPair.getQuantity(), "Message Types", barPair.getName());
				updatedItem=true;
			}
		}
		if (!updatedItem)
		{
			BarChartDataPair aNewBarPair = new BarChartDataPair(updateItem,1);
			barChartDataList.add(aNewBarPair);
			barDataset.setValue(aNewBarPair.getQuantity(), "Message Types", aNewBarPair.getName());
		}
	}
	
	private void updateJasonBarChartData(String updateItem)
	{
		boolean updatedItem = false;
		for (BarChartDataPair jasonBarPair : jasonBarChartDataList) {
			if (jasonBarPair.getName().equals(updateItem))
			{
				jasonBarPair.incrementQuantity();
				jasonBarDataset.setValue(jasonBarPair.getQuantity(), "Message Types", jasonBarPair.getName());
				updatedItem=true;
			}
		}
		if (!updatedItem)
		{
			BarChartDataPair aNewBarPair = new BarChartDataPair(updateItem,1);
			jasonBarChartDataList.add(aNewBarPair);
			jasonBarDataset.setValue(aNewBarPair.getQuantity(), "Message Types", aNewBarPair.getName());
		}
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

