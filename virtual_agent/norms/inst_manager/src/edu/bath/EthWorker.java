/*
 * EthWorker
 * - Provide functionality to publish contracts onto ethereum block chain
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
import java.io.UnsupportedEncodingException;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.message.BasicHeader;
import java.io.*;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader; 
import org.apache.http.message.BasicHttpResponse; 
import org.apache.http.protocol.HTTP; 
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;

public class EthWorker {

	private String ethRPCAddress = "http://127.0.0.1:8100";
	private String myAccount = "";
	//String environmentCurrentContract = "";
	//String environmentFirstContract = "";
	
	public EthWorker(String address) {
		ethRPCAddress = address;
		myAccount = getCoinBase();
		/*try
		{
			FileReader fr = new FileReader("startRDFContract.log"); 
			BufferedReader br = new BufferedReader(fr);
			String s;
			if ((s = br.readLine()) != null) 
			{
				System.out.println("Setting start contract address to:" + s);
				environmentFirstContract = s;
			}
			else
			{
				System.out.println("problem getting start contract address from file..");
			}
			fr.close();
		}
		catch (Exception e)
		{
			System.out.println("problem getting start contract address from file..");
			e.printStackTrace();
		}

		findLatestRDFContract();*/
	}	

	public JSONObject send(String ethMsg)
	{
		//System.out.println("Starting to send Http sender");
		HttpClient httpClient = new DefaultHttpClient();
		String myEthAddress="";
		JSONObject ethResult = new JSONObject();

		try {
			HttpResponse response;	
			JSONParser parser = new JSONParser();
		        HttpPost post = new HttpPost(ethRPCAddress);
		        StringEntity se = new StringEntity(ethMsg);

		        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		        post.setEntity(se);
		        response = httpClient.execute(post);

		        if (response != null) {
		            	InputStream in = response.getEntity().getContent(); //Get the data in the entity
				String readLine;
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuilder responseStrBuilder = new StringBuilder();
 				String inputStr;
    				while ((inputStr = br.readLine()) != null)
				{
        				responseStrBuilder.append(inputStr);
				}
				if (responseStrBuilder.toString().contains("error"))
				{
					System.out.println("WARNING! could be error");
					System.out.println(responseStrBuilder.toString());
				}
				/*else if (responseStrBuilder.toString().contains("\"result\":[]"))
				{
					System.out.println("result is an array!! NOT HANDLED YET!!");
					System.out.println(responseStrBuilder.toString());
					ethResult = "FIX";
				}*/
				else
				{
					//System.out.println(responseStrBuilder.toString());
					Object obj = parser.parse(responseStrBuilder.toString());
					JSONObject jsonObject = (JSONObject) obj;
					ethResult = jsonObject;
				}
			}

           	} 
		catch (Exception e) {
                	e.printStackTrace();
                	System.out.println("Error, Cannot Estabilish Connection");
            	}

		/*if (ethResult = null) 
		{ 
			System.out.println("WARNING: no eth result"); 
		}*/
	
		return ethResult;

	}

	public String findResult(JSONObject jObj, String findValue)
	{
		String resultFound = (String) jObj.get(findValue);
		return resultFound;
	}

	public String findArrayResult(JSONObject jObj, String findValue)
	{
		JSONObject jsonRes = (JSONObject) jObj.get("result");
		String foundres = (String) jsonRes.get(findValue);
		return foundres;
	}

	public String findResultOfArray(JSONObject jObj, String findValue)
	{
		String foundres = "none";
		JSONArray jsonArr = (JSONArray) jObj.get("result");
		if (jsonArr != null)
		{
			if (jsonArr.size() == 1)
			{
				JSONObject foundJSONObj = (JSONObject) jsonArr.get(0);
				foundres = (String) foundJSONObj.get(findValue);
			}
		}
		return foundres;
	}

	public String findContractCode(JSONObject jObj)
	{
		JSONObject jsonRes = (JSONObject) jObj.get("result");
		JSONObject cont = (JSONObject) jsonRes.get("TripleObservationContract");
		String foundres = (String) cont.get("code");
		return foundres;
	}
	
	public String getEthBalance(String account)
	{
		EthBalance ethBal = new EthBalance(account);
		return findResult(send(ethBal.toJSONString()),"result");
	}

	private String getCoinBase()
	{
		JSONObject json = new JSONObject();
		json.put("jsonrpc", "2.0");
		json.put("method", "eth_coinbase");
		json.put("params", "[]");
		json.put("id", 64);
		String account = findResult(send(json.toString()),"result");
		return account;
	}

	public String getAccountNumber()
	{
		return myAccount;
	}

	public String sendTransaction(String from, String data, int gas)
	{
		JSONObject json = new JSONObject();
		JSONObject params = new JSONObject();
		params.put("from", from);
		params.put("data", data);
		params.put("gas", "0x"+Integer.toHexString(gas));
		JSONArray paramArray = new JSONArray();
		paramArray.add(params);

		json.put("jsonrpc", "2.0");
		json.put("method", "eth_sendTransaction");
		json.put("params", paramArray);
		json.put("id", 1);
		String txResult = findResult(send(json.toString()),"result");
		return txResult;
	}

	public String sendTransaction(String from, String to, String data)
	{
		JSONObject json = new JSONObject();
		JSONObject params = new JSONObject();
		params.put("from", from);
		params.put("to", to);
		params.put("data", data);
		JSONArray paramArray = new JSONArray();
		paramArray.add(params);

		json.put("jsonrpc", "2.0");
		json.put("method", "eth_sendTransaction");
		json.put("params", paramArray);
		json.put("id", 1);
		//System.out.println(json.toString());
		String txResult = findResult(send(json.toString()),"result");
		return txResult;
	}

	public String sendCall(String to, String data)
	{
		EthCall myEthCall = new EthCall(to,data);
		JSONObject retVal = send(myEthCall.toJSONString());
		//System.out.println(retVal.toString());
		return findResult(retVal,"result");
	}


	public JSONObject getTxReceipt(String txHash)
	{
		EthTxReceipt ethTxReceipt = new EthTxReceipt(txHash);
		JSONObject recObj = send(ethTxReceipt.toJSONString());
		//System.out.println("received: " + recObj.toString());
		//findArrayResult(recObj, contractAddress);
		return recObj;
	}

	public void addPendingTxFilter() //TODO not tested yet
	{
		JSONObject json = new JSONObject();
		json.put("jsonrpc", "2.0");
		json.put("method", "eth_newPendingTransactionFilter");
		json.put("params", "[]");
		json.put("id", 73);
		send(json.toString());
	}

	public String addGeneralFilter()
	{
		JSONObject json = new JSONObject();
		json.put("jsonrpc", "2.0");
		json.put("method", "eth_newFilter");
		json.put("params", "[]");
		json.put("id", 73);

		JSONObject params = new JSONObject();
		params.put("fromBlock", "0x1");
		params.put("toBlock", "latest");
		JSONArray paramArray = new JSONArray();
		paramArray.add(params);

		json.put("params", paramArray);
		json.put("id", 1);

		JSONObject recObj = send(json.toString());
		//System.out.println("filter: " + recObj.toString());
		return findResult(recObj, "result");
	}

	public String addNewBlockFilter()
	{
		JSONObject json = new JSONObject();
		json.put("jsonrpc", "2.0");
		json.put("method", "eth_newBlockFilter");
		json.put("params", "[]");
		json.put("id", 73);
		JSONObject recObj = send(json.toString());
		return findResult(recObj, "result");
	}
	
	public String createRDFContract(String s, String p, String o)
	{
		EthRDFContract newContract = new EthRDFContract(s,p,o);
		//System.out.println(newContract.toJSONString());
		JSONObject recObj = send(newContract.toJSONString());
		//System.out.println(recObj.toString());
		return findContractCode(recObj);
	}

	public JSONObject getFilterChanges(String id)
	{
		EthGetFilterChanges ethChange = new EthGetFilterChanges(id);
		JSONObject changesObj= send(ethChange.toJSONString());
		//System.out.println("Changes: " + changesObj.toString());
		return changesObj;
	}

	public String convertHexToString(String hex)
	{
		//http://stackoverflow.com/questions/4785654/convert-a-string-of-hex-into-ascii-in-java
    		StringBuilder output = new StringBuilder();
    		for (int i = 0; i < hex.length(); i+=2) {
        		String str = hex.substring(i, i+2);
			if (!str.equals("0x"))
			{
        			output.append((char)Integer.parseInt(str, 16));
			}
   		}
    		return output.toString();
	}

	public void createNewRDFContract()
	{
		//try creating an RDF contract, these would act as environment observations
		System.out.println("trying to send contract, first generate bytecode..");

		//remember if generating contract bytecode via https://chriseth.github.io/browser-solidity/, to enable optimisation
		//to match code generated locally here
		String contractByteCode2 = createRDFContract("http://127.0.0.1/subj/testSubj2", "http://127.0.0.1/pred/testPred2", "testObj2");
		String txResultHash = sendTransaction(myAccount, contractByteCode2, 5000000);
		System.out.println("sent! got back: " + txResultHash);
		
		//wait until its been mined, then see if we can call a function on it 
		sleep(5000);
		System.out.println("now try to get the tx receipt from that...");
		JSONObject recReceipt = getTxReceipt(txResultHash);
		if (recReceipt.get("result") == null)
		{
			System.out.println("no tx receipt... too soon?");
		}
		else
		{
			//System.out.println(recReceipt.toString());
			String contractAddress = findArrayResult(recReceipt, "contractAddress");
			System.out.println("new contract address is " + contractAddress);
			//call this function to put the contracts pred,obj,subj into the event log of that contract, so change filter will detect it
			//TODO: so many better ways to do this!
			sendTransaction(myAccount, contractAddress, "c33fb877");



			/*this was trying the linkedlist idea..
			//point env contract chain to this
			String newContractLoc = new String("0x53390100"+"000000000000000000000000"+contractAddress.substring(2));
			//System.out.println(newContractLoc);
			sendTransaction(myAccount, environmentCurrentContract, newContractLoc);

			//TODO: might be neater to check the return value? but for the moment..
			//sleep to let transaction be mined, then check if previous contract now points to new one, if so update var
			sleep(5000);
			String retHexVal = sendCall(environmentCurrentContract, "0xb8568c6f");
			if (contractAddress.substring(2).equals(retHexVal.substring(26)))
			{
				System.out.println("previous last contract now points to new contract, all is good!");
			}
			else
			{
				System.out.println("last contract couldn't be updated, new contract was " + contractAddress.substring(2) + " but last known one points to " + retHexVal.substring(26));
			}*/
		}
	}

	/*public String findLatestRDFContract()
	{
		String currentPointer = environmentFirstContract;
		String retHexVal = sendCall(currentPointer, "0xb8568c6f");
		while (!retHexVal.equals("0x0000000000000000000000000000000000000000000000000000000000000000"))
		{
			String nextContract = "0x"+retHexVal.substring(26);
			currentPointer = nextContract;
			//System.out.println(nextContract);
			retHexVal = sendCall(nextContract, "0xb8568c6f");
			//System.out.println(retHexVal);
		}
		System.out.println("last RDF contract is " + currentPointer);
		environmentCurrentContract = currentPointer;
		return currentPointer;
	}

	public String getLatestRDFContract()
	{
		return environmentCurrentContract;
	}*/

	public void sleep(long mili) {
		try {
			Thread.sleep(mili);
		} catch (InterruptedException e) {
			// no-op
		}
	}
}
