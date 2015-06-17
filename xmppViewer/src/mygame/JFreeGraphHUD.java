package mygame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
//import org.jfree.util.*;

/**
 *
 * @author vin
 */
public class JFreeGraphHUD {
    
    private JFreeChart chart;
    private XYSeries myseries1,myseries2;
    private XYDataset mydataset;
    private long timeStart=0;
   
    public JFreeGraphHUD()
    {
        mydataset = createDataset();
        chart = createChart(mydataset);
        chart.setBackgroundPaint(new Color(255,255,255,0));
        System.out.println("created chart");
    }
    
    public void update()
    {

    }
    
    public void addNewPoint(float pointValue, float pointValueAlt)
    {
        //System.out.println("adding " + pointValue + " , " + pointValueAlt);
        if(timeStart == 0)
        {
            timeStart=System.currentTimeMillis();
        }
        long updateTime = (System.currentTimeMillis() - timeStart)/1000;
        myseries1.add(updateTime,pointValue);
        myseries2.add(updateTime,pointValueAlt);

    }
    
    public void addNewPoint(float pointValue, float pointValueAlt, long timeVal)
    {
        long updateTime = timeVal/1000;
        myseries1.add(updateTime,pointValue);
        myseries2.add(updateTime,pointValueAlt);

    }
    
    public BufferedImage getBI()
    {
        BufferedImage img = draw( chart, 320, 240 ); 
        
        return img;
    }
    
    protected BufferedImage draw(JFreeChart chart, int width, int height)
    {
        BufferedImage img =
        new BufferedImage(width , height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();
        return img;
    }
    
    private XYDataset createDataset() 
    { 
	myseries1 = new XYSeries("1");
        myseries1.add(0.0, 0.0);
        myseries2 = new XYSeries("2");
        myseries2.add(0.0, 0.0);


	final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(myseries1);
        dataset.addSeries(myseries2);
        return dataset;	  
    }

    private JFreeChart createChart(final XYDataset dataset) 
	{
		//for m25, we're interested in what vehicle 2 does, for Bath more interested in vehicle 1
		String title = "";	

       		final JFreeChart chart = ChartFactory.createXYLineChart(
            		title,      // chart title
            		"Time (seconds)",                      // x axis label
            		"",                      // y axis label
            		dataset,                  // data
            		PlotOrientation.VERTICAL,
            		false,                     // include legend
            		true,                     // tooltips
            		false                     // urls
        	);
  	return chart;
  	}
}
