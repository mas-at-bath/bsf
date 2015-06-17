package jsonExample;

import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;

import jsonExample.JsonSensor;

public class SimpleJsonPublisher {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws XMPPException, UnsupportedEncodingException 
	{
		// TODO Auto-generated method stub
		JsonSensor js = new JsonSensor("jl2", "user1", "bathstudent", "example");
		js.run();
	}

}
