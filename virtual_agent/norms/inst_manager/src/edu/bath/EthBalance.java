/*
 * EthBalance
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

//based on curl -X POST --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x407d73d8a49eeb85d32cf465507dd71d507100c1", "latest"],"id":1}'

public class EthBalance {

	private String jsonrpc="2.0";
	private String method = "eth_getBalance";
	private int id=1;
	private List<String> params;
	
	public EthBalance(String account)
	{
		params = new ArrayList<String>(); 
		params.add(account);
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
		return jsonStr;
	}	
}
