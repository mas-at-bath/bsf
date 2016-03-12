/*
 * EthCall
 * - Provide functionality to get ethereum balance
 * 
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

//based on curl -X POST --data '{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "0xe5b7e53b86ad6ea783d637c1eb143650fcc31254","data":"0x6d4ce63c"},"latest"],"id":1}'

public class EthCall {

	private String jsonrpc="2.0";
	private String method = "eth_call";
	private int id=1;
	private List<String> params;
	
	public EthCall(String to, String data)
	{
		params = new ArrayList<String>(); 
		JSONObject mainDetails = new JSONObject();
		mainDetails.put("to", to);
		mainDetails.put("data", data);
		String detailString = mainDetails.toString();
		params.add(detailString);
		params.add("latest");
	}

	@Override
	public String toString() {
	   return "DataObject [jsonrpc="+jsonrpc+", method="+method+", params="
		+ params + ", id="+id+"]";
	}

	public String toJSONString() 
	{
		Gson gson = new Gson();
		//gson will put the entire object into json so the private defs early on need to match what is needed in the msg
		String jsonStr = gson.toJson(this);
		//EUGH hacky.. there could be problems with this later!!
		//TODO: might not even be needed
		jsonStr = jsonStr.replace("\\\"", "\"");
		jsonStr = jsonStr.replace("}\",", "},");
		jsonStr = jsonStr.replace("[\"{", "[{");
		return jsonStr;
	}	
}
