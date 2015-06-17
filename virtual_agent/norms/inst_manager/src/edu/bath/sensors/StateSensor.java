/*
 * StateSensor
 * 	
 * 	- publish the current state of the institution, i.e. norms 
 * 	- permission, prohibition, and obligation
 * 
 * 		@author		JeeHang
 * 		@date		29 Mar 2012
 */

package edu.bath.sensors;

import edu.bath.sensorframework.sensor.Sensor;
import edu.bath.sensorframework.DataReading;

import org.jivesoftware.smack.chat.*;
//import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.MessageListener;

import java.io.UnsupportedEncodingException;

public class StateSensor extends Sensor 
{
	private String m_handle;
	private String m_curLocation;
	private boolean m_bUpdated = false;
	
	private ChatMessageListener m_chatListener;
	
	public class InstChatManagerListener implements ChatManagerListener
	{
		@Override
		public void chatCreated(Chat chat, boolean message) 
		{
			chat.addMessageListener(m_chatListener);
		}
	}
	
	public StateSensor(String serverAddress, String username, String password, String nodename) throws XMPPException
	{
		super(serverAddress, username, password, nodename);
		
		m_handle = "http://127.0.0.1/sensors/state";
		m_curLocation = "http://127.0.0.1/sensors/state/norms";
		
		m_bUpdated = true;
	}
	
	public StateSensor(String serverAddress, String username, String password, ChatMessageListener listener) throws XMPPException
	{
		super(serverAddress, username, password);
		
		m_chatListener = listener;
		init();
	}
	
	public String getHandle()
	{
		return m_handle;
	}
	
	public String getLocation()
	{
		return m_curLocation;
	}
	
	public void run() throws UnsupportedEncodingException
	{
		String str = "This is a test message";
		
		while (true)
		{
			if (m_bUpdated)
			{
				DataReading data = new DataReading(getHandle(), getLocation(), System.currentTimeMillis());
				
				// set data
				data.setType("http://127.0.0.1/types#Requests");
				data.addDataValue(null, "http://127.0.0.1/sensors/types#RequestString", "new norms", false);				
				publish(data);
				
				try
				{
					Thread.sleep(2 * 1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void sendState(InstQuery iq)
	{
		try
		{
			iq.getChat().sendMessage(iq.getState());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void init()
	{
		XMPPTCPConnection connection = this.getConnection();
		ChatManager chatmanager = ChatManager.getInstanceFor(connection);

		chatmanager.addChatListener(new InstChatManagerListener());
	}
}
