package edu.bath;

public class SimpleRDF {

	private String subj="";
	private String pred="";
	private String obj="";
	
	public SimpleRDF(String s, String p, String o)
	{
		subj=s;
		pred=p;
		obj=o;
	}

	public void setSubj(String s) { subj = s; } 
	public void setPred(String p) { pred = p; } 
	public void setObj(String o) { obj = o; } 
	public String getSubj() { return subj; }
	public String getPred() { return pred; }
	public String getObj() { return obj; }

}

