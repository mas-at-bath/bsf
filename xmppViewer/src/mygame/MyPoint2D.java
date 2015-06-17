/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;



/**
 *
 * @author vin
 */
public class MyPoint2D {
    
    private double xPoint, yPoint;
    
    public MyPoint2D()
    {
    }
        
    public MyPoint2D(double newXPoint, double newYPoint)
    {
        xPoint=newXPoint;
        yPoint=newYPoint;
    }
    
    public double getX()
    {
        return xPoint;
    }
    
    public double getY()
    {
        return yPoint;
    }
    
}
