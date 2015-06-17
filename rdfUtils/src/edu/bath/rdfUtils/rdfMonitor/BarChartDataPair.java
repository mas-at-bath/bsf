package edu.bath.rdfUtils.rdfMonitor;

public class BarChartDataPair {

	private double quantity=0;
	private String label="";
	BarChartDataPair(String name, double val)
	{
		System.out.println("creating new from class");
		quantity=val;
		label=name;
	}
	
	public String getName()
	{
		return label;
	}
	
	public double getQuantity()
	{
		return quantity;
	}
	
	public void setName(String newName)
	{
		label = newName;
	}
	
	public void setQuantity(double newQuant)
	{
		quantity=newQuant;
	}
	
	public void incrementQuantity()
	{
		quantity++;
	}
}
	