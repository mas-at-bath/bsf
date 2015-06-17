/*
 * ConsoleBotInstance
 * 
 *      08-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

using Matrix.Xmpp.Client;

using OpenMetaverse;
using csSensorFramework;

namespace ConsoleBot
{
    // Main instance of ConsoleBot 
    class ConsoleBotInstance
    {
        private GridClient m_Client;            // Open Metaverse Grid Client
        private NetworkManager m_netManager;    // Network / Connection management
        private MovementManager m_moveManager;  // Avatar movement management
        private ChatManager m_chatManager;
        private ObjectEvtManager m_objManager;
        private JasonInterface m_infJason;

        // Sensor Client - Subscribe or Receive message from Sensor via XMPP
        private SensorClient m_sensorClient;

        //////////////////////////////////////////////////////////////////////////
        // Methods
        //////////////////////////////////////////////////////////////////////////

        // Constructor
        public ConsoleBotInstance()
        {
            m_globalInstance = this;
            m_Client = new GridClient();
        }

        // Constructor
        public ConsoleBotInstance(GridClient client)
        {
            m_globalInstance = this;
            m_Client = client;
            
            m_netManager = new NetworkManager(this);
            m_moveManager = new MovementManager(this);
            m_objManager = new ObjectEvtManager(this);
        }

        ~ConsoleBotInstance()
        {
            // no-operation
        }

        public void InitailiseBSF(string user, string pwd)
        {
            m_sensorClient = new SensorClient("jl2", user, pwd);
            m_sensorClient.SensorClientCreateEvent += new SensorClientCreateEventHandler(OnSensorClientCreateEvent);
        }

        protected void OnSensorClientCreateEvent(object sender, SensorClientCreateEventArgs e)
        {
            if (e.Ready == true)
            {
                //m_sensorClient.Subscribe(Constant.NODE_ACTION, OnSubscribeHandler);
                m_sensorClient.Subscribe(m_netManager.Username + "_action", OnSubscribeHandler);
            }

            m_chatManager = new ChatManager(this);
            m_infJason = new JasonInterface(this);
        }

        protected void OnSensorCreateEvent(object sender, SensorCreateEventArgs e)
        {
            if (e.Ready == true)
            {
                Debug.WriteLine("Sensor Created in ChatManager");
            }
        }

        public void OnSubscribeHandler(object sender, MessageEventArgs e)
        {
            JsonReading jr = new JsonReading();
            jr.fromJSON(e.Message.ToString());

            JsonReading.Value val = jr.FindValue("ACTION");
            Debug.WriteLine(val.m_obj.ToString());

            m_objManager.UpdateAction(val.m_obj.ToString());
        }

        /*
        public void OnSubscribeHandler(object sender, MessageEventArgs e)
        {
            ActionManager am = new ActionManager(this);
            Action action = Action.None;

            JsonReading jr = new JsonReading();
            jr.fromJSON(e.Message.ToString());

            JsonReading.Value val = jr.FindValue("ACTION");
            Debug.WriteLine(val.m_obj.ToString());

            action = am.GetAction(val.m_obj.ToString());
            am.InvokeAction(action);
        }
        */ 

        /*
         * Legacy code - XMPP message handler supported by Jabber-net library. 
         * This is no longer needed, but left in order for the reference how message handling works.
         * Also, helps what operations should be carried out in the message handler.
         * 
        // XMPP Message handling and perfoming actions
        public void JabberReadMsg(object sender, Message msg)
        {
            ActionManager am = new ActionManager(this);
            Action action = Action.None;

            if (msg.From == "nohannara" || msg.From.ToString().Contains("mind"))
            {
                // ignore keep-alive message
                if (msg.Body != " ")
                {
                    Console.WriteLine("RECV: " + msg.Body);
                    //m_sensorClient.sendMessage("user2" + "@jlnetbook/smack", "Got it: " + msg.Body);

                    action = am.GetAction(msg.Body);
                    am.InvokeAction(action);
                }
            }
            else
            {
                string name = m_Client.Self.FirstName;
                string target = "mind" + name.ElementAt(name.Length - 1) + "@jlnetbook/smack";

                m_sensor.sendMessage(target, msg.Body);
            }
        }
        */

        public void Run()
        {
            string cmd;

            ActionManager am = new ActionManager(this);
            Action action = Action.None;

            if (m_netManager.Login() == true)
            {
                cmd = Console.ReadLine();
                action = am.GetAction(cmd);

                while (action != Action.Logout)
                {
                    am.InvokeAction(action);
                    cmd = Console.ReadLine();
                    action = am.GetAction(cmd);
                }

                if (action == Action.Logout)
                {
                    m_netManager.Logout();
                }
            }
        }


        //////////////////////////////////////////////////////////////////////////
        // Get/Set properties
        //////////////////////////////////////////////////////////////////////////

        // Singleton, there can be only one instance
        private static ConsoleBotInstance m_globalInstance = null;
        public static ConsoleBotInstance GloblaInstance
        {
            get
            {
                if (m_globalInstance == null)
                {
                    m_globalInstance = new ConsoleBotInstance(new GridClient());
                }

                return m_globalInstance;
            }
        }

        // Get GridClient
        public GridClient Client
        {
            get { return m_Client; }
        }

        // Get Network Manager
        public NetworkManager NetManager
        {
            get { return m_netManager; }
        }

        // Get Movement Manager
        public MovementManager MoveManager
        {
            get { return m_moveManager; }
        }

        public ObjectEvtManager ObjEvtManager
        {
            get { return m_objManager; }
        }
    }
}
