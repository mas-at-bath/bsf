/*
 * EthRDFContract
 * 		@author		V Baines
 * 		@date		March 2016
 * 
 */

package edu.bath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EthOblContract {

	private String jsonrpc="2.0";
	private String method = "eth_compileSolidity";
	private int id=1;
	private List<String> params;
	
	public EthOblContract(String subj, String pred, String obj)
	{
		String contractOutline = "contract TripleObservationContract { int8 setSuccessful = 0; string subj = \"REPLACESUBJ\"; string pred = \"REPLACEPRED\"; string obj = \"REPLACEOBJ\"; address nextContract; event Print(string s,string p,string o); event Test(bytes t); event Test2(bytes s, bytes p); event Observation(bytes s, bytes p, bytes o); function process() { Print(subj,pred,obj); } function getNextContract() constant returns (address) { return nextContract;} function getSubj() constant returns (string) { return subj;} function getPred() constant returns (string) { return pred; } function getObj() constant returns (string) { return obj; } function setNextContract(address newLoc)  returns (address) { if (setSuccessful == 0) { nextContract=newLoc; setSuccessful = 1; } return nextContract; } function testfn(bytes t) { Test(t);} function testfn2(bytes s, bytes p) { Test2(s,p);} function addObservation(bytes s, bytes p, bytes o) { Observation(s,p,o);} }";

//"contract TripleObservationContract { string subj = \"REPLACESUBJ\"; string pred = \"REPLACEPRED\"; string obj = \"REPLACEOBJ\"; function getSubj() constant returns (string) { return subj;} function getPred() constant returns (string) { return pred; } function getObj() constant returns (string) { return obj; } }";
		contractOutline = contractOutline.replace("REPLACESUBJ", subj);
		contractOutline = contractOutline.replace("REPLACEPRED", pred);
		contractOutline = contractOutline.replace("REPLACEOBJ", obj);
		params = new ArrayList<String>(); 
		params.add(contractOutline);
	}

	@Override
	public String toString() {
	   return "DataObject [jsonrpc="+jsonrpc+", method="+method+", params="
		+ params + ", id="+id+"]";
	}

	public String toJSONString() 
	{
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		//gson will put the entire object into json so the private defs early on need to match what is needed in the msg
		String jsonStr = gson.toJson(this);
		return jsonStr;
	}
	
}
