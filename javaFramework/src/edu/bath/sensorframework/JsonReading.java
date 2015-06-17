package edu.bath.sensorframework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.ParseException;

/**
 * How data readings are stored and passed around the system 
 * in the form of JSON format.
 * 
 * @author JeeHang
 *
 */
public class JsonReading 
{
	public class Value 
	{
		public final String m_key;
		public final Serializable m_object;
		
		public Value(String key, Serializable object)
		{
			m_key = key;
			m_object = object;
		}
	}

	private Map m_data; 
	private JSONObject m_jo;
	
	public JsonReading() 
	{
		m_jo = new JSONObject();
	}
	
	public void fromJSON(String rdf) throws Exception 
	{
		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory() 
		{
		    public List creatArrayContainer() 
		    {
		    	return new LinkedList();
		    }

		    public Map createObjectContainer() 
		    {
		    	return new LinkedHashMap();
		    }
		};
		
		try 
		{
			String jsonstring = rdf.substring(rdf.indexOf("{"), rdf.lastIndexOf("}") + 1);
			m_data = (Map) parser.parse(jsonstring, containerFactory);
		} 
		catch (ParseException pe) 
		{
			System.out.println(pe);
		}
	}
	
	public Value findValue(String key)
	{
		Value val = null;
		String entrykey;

		Iterator iter = m_data.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();
			entrykey = (String) entry.getKey().toString();
			
			if (key.compareTo(entrykey) == 0)
			{
				val = new Value(entrykey, (Serializable) entry.getValue());
				break;
			}
		}
		return val;
	}
	
	public void addValue(Value val)
	{
		m_jo.put(val.m_key, val.m_object);
	}
	
	public void addValue(String key, Serializable object)
	{
		m_jo.put(key, object);
	}
	
	public JSONObject getJsonObject()
	{
		return m_jo;
	}
}
