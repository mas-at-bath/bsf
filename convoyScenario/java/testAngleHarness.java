import math.geom2d.*;
import java.lang.Math.*;

public class testAngleHarness  {
	
	public static void main(String[] args) throws Exception {
		double x0=-0;		
		double y0=0;
		double x1=67;
		double y1=67;

		Point2D startPoint = new Point2D(x0,y0);
		Point2D endPoint = new Point2D(x1,y1);

		System.out.println("Angle between " + startPoint.toString() + " and " + endPoint.toString() );
		System.out.println("psuedo angle: " + Angle2D.getPseudoAngle(startPoint,endPoint));
		System.out.println("hoz angle: "+ Angle2D.getHorizontalAngle(startPoint,endPoint));	
		System.out.println(Math.toDegrees(Math.atan2(y0-y1, x0-x1)));


		System.out.println("test cases: ");
System.out.println("0:");
System.out.println(getAngle(-10, 0));
System.out.println("45:");
System.out.println(getAngle(0, 0));
System.out.println("90:");
System.out.println(getAngle(0, -10));
System.out.println("135:");
System.out.println(getAngle(0, -20));
System.out.println("180:");
System.out.println(getAngle(-10, -20));
System.out.println("225:");
System.out.println(getAngle(-20, -20));
System.out.println("270:");
System.out.println(getAngle(-20, -10));
System.out.println("315:");
System.out.println(getAngle(-20, 0));
System.out.println("360:");
System.out.println(getAngle(-10,0));
	}

	public static double getAngle(double x, double y)
	{
		Double initVal = Math.toDegrees(Math.atan2(-10-x, -10-y));
		Double finVal = 0.0;
		if (initVal < 0)
		{
			finVal = initVal+180;
		}
		else
		{	
			finVal=initVal+180;
		}
		//return Math.toDegrees(Math.atan2(0-x, 0-y));
		return finVal;
	}
}
