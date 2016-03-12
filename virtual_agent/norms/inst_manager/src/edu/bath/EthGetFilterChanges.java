/*
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

//based on curl -X POST --data '{"jsonrpc":"2.0","method":"eth_getFilterChanges","params":["0x16"],"id":73}'


public class EthGetFilterChanges {

	private String jsonrpc="2.0";
	private String method = "eth_getFilterChanges";
	private int id=73;
	private List<String> params;
	
	public EthGetFilterChanges(String id)
	{
		params = new ArrayList<String>(); 
		params.add(id);
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
		return jsonStr;
	}	
}
