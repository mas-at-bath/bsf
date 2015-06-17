/*
 * Moving Data Sensor 
 * - Extends Sensor for publishing moving data 
 * 
 * 		@author		JeeHang
 * 		@date		06 Feb 2012
 */

import org.jivesoftware.smack.XMPPException;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class MovDataSensor extends Sensor 
{
	private Chat m_chatBot;
	private Chat m_chatInst;
	
	private String jidInst = "instman@jlnetbook/smack";
	
	private MessageListener m_BotMsgListener;
	private MessageListener m_InstMsgListener;
	
	//////////////////////////////////////////////////////////////////
	// Defines, classes
	//////////////////////////////////////////////////////////////////
	
	public enum TARGET
	{
		NONE,
		BOT,
		INSTITUTION;
	}
	
	public class ChatManagerListenerEx implements ChatManagerListener
	{
		@Override
		public void chatCreated(Chat chat, boolean message) 
		{
			m_chatBot = chat;
			m_chatBot.addMessageListener(m_BotMsgListener);
		}
	}
	
	//////////////////////////////////////////////////////////////////
	// Defines, classes
	//////////////////////////////////////////////////////////////////
	
	public MovDataSensor(String serverAddress, String username, String password, MessageListener BotListener, MessageListener InstListener) throws XMPPException 
	{
		super(serverAddress, username, password);
		
		m_BotMsgListener = BotListener;
		m_InstMsgListener = InstListener;
		
		init();
	}
	
	public void createChat(TARGET target, MessageListener listener)
	{
		if (target == TARGET.INSTITUTION)
		{
			m_chatInst = this.getConnection().getChatManager().createChat(jidInst, listener);
		}
		else
		{
			System.out.println("no chat handle created!");
		}
	}
	
	public void run() throws InterruptedException
	{
		// no operation
	}
	
	public void sendAction(String action)
	{
		try
		{
			if (isBotAction(action) == true)
			{
				m_chatBot.sendMessage(action);
			}
			else
			{
				m_chatInst.sendMessage(getUpdateString(action));
			}
		}
		catch (XMPPException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void sendTo(TARGET target, String msg)
	{
		Chat chat = null;
		
		switch (target)
		{
			case BOT:
				chat = m_chatBot;
				break;
				
			case INSTITUTION:
				chat = m_chatInst;
				break;
				
			default:
				System.out.println("sendMessage filed : untargetted. Choose BOT or INSTITUTION");
				break;
		}
		
		try
		{
			chat.sendMessage(msg);
		}
		catch (XMPPException e)
		{
			e.printStackTrace();
		}
	}
	
	private void init()
	{
		createChat(MovDataSensor.TARGET.INSTITUTION, m_InstMsgListener);
		
		Connection connection = this.getConnection();
		ChatManager chatmanager = connection.getChatManager();
		chatmanager.addChatListener(new ChatManagerListenerEx());	
	}
	
	private boolean isBotAction(String action)
	{
		boolean res = false;
		
		res = (action.startsWith("update") == false);
		
		return res;
	}	
	
	private String getUpdateString(String action)
	{
		String update = null;
		
		if (action.compareTo("update(new_arrival)") == 0)
			update = "newArrival(%s, normal)";
		else if (action.compareTo("update(disabled)") == 0)
			update = "detectDisable(%s)";
			
		return update; 
	}
}
