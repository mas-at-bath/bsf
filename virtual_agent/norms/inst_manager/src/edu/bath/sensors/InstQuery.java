package edu.bath.sensors;

import org.jivesoftware.smack.chat.*;

public class InstQuery 
{
	private Chat m_chat;
	private String m_query;
	private String m_state;
	
	public InstQuery()
	{
		m_chat = null;
		m_query = null;
		m_state = null;
	}
	
	public InstQuery(Chat chat, String query)
	{
		m_chat = chat;
		m_query = query;
		m_state = null;
	}
	
	public Chat getChat()
	{
		return m_chat;
	}
	
	public String getQuery()
	{
		return m_query;
	}
	
	public String getState()
	{
		return m_state;
	}
	
	public void setState(String newState)
	{
		m_state = newState;
	}

}
