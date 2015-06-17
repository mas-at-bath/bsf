package edu.bath.example;

import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;

import edu.bath.example.SimpleSensor;

public class SimplePublisher 
{
	/**
	 * @param args
	 * @throws XMPPException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws XMPPException, UnsupportedEncodingException 
	{
		// TODO Auto-generated method stub
		SimpleSensor ss = new SimpleSensor("138.38.141.141", "user3", "bathstudent", "example");
		ss.run();
	}

}
