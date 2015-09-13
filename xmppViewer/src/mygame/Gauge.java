package mygame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

/**
 *
 * @author vin
 */
public class Gauge {
    
    private JFreeChart chart;
    private static DefaultValueDataset dataset;
    private long timeStart=0;
    private String myTitle;
   
    public Gauge(String gaugeTitle)
    {
        myTitle = gaugeTitle;
        dataset = createDataset();
        chart = createChart(dataset);
        chart.setBackgroundPaint(new Color(255,255,255,0));
        System.out.println("created chart");
    }
    
    public void setValue(Double newVal)
    {
        dataset.setValue(newVal);
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
    
    private DefaultValueDataset createDataset() 
    { 
	dataset = new DefaultValueDataset(50.0);
        return dataset;	  
    }

    private JFreeChart createChart(ValueDataset dataset) 
	{
		String title = "";	
                MeterPlot plot = new MeterPlot(dataset);
                plot.addInterval(new MeterInterval("High", new Range(30.0, 40.0)));
                plot.setDialOutlinePaint(Color.white);
                plot.setDialBackgroundPaint(Color.white);
                plot.setDialOutlinePaint(Color.gray);
                plot.setNeedlePaint(Color.darkGray);
                plot.setTickLabelFont(new Font("Dialog", Font.BOLD, 10));
                plot.setTickLabelPaint(Color.darkGray);
                plot.setBackgroundPaint(Color.white);
                plot.setTickSize(5.0);
                plot.setTickPaint(Color.lightGray);
                plot.setValuePaint(Color.black);
                Range gaugeRange = new Range(0d,40d);
                plot.setRange(gaugeRange);
                JFreeChart chart = new JFreeChart(myTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
                return chart;
  	}
}
