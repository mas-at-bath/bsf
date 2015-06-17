/*
 * ChatManager
 * 
 *      08-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

using OpenMetaverse;
using csSensorFramework;

namespace ConsoleBot
{
    class ChatManager
    {
        private GridClient m_Client;
        private ObjectEvtManager m_objManager = null;
        private ActionManager m_actionManager = null;
        private Sensor m_Sensor;

        public ChatManager(ConsoleBotInstance botInstance)
        {
            m_Client = botInstance.Client;
            m_objManager = botInstance.ObjEvtManager;

            m_actionManager = new ActionManager(botInstance);
            m_Client.Self.ChatFromSimulator += new EventHandler<ChatEventArgs>(OnChatFromSimulator);

            // m_Sensor = new Sensor("jl2", "user2", botInstance.NetManager.Password, Constant.NODE_PERCEPT);
            m_Sensor = new Sensor("jl2", botInstance.NetManager.Username + "_client", botInstance.NetManager.Password, botInstance.NetManager.Username + "_percept");
            m_Sensor.SensorCreateEvent += new SensorCreateEventHandler(OnSensorCreateEvent);

        }

        public void OnSensorCreateEvent(object sender, SensorCreateEventArgs e)
        {
            Debug.WriteLine("Sensor Created in Chat Manager.");
        }

        public void OnChatFromSimulator(object sender, ChatEventArgs e)
        {
            if (e.Message != "\n" && e.Message != " " && e.Message != "")
            {
                string msg = e.Message;
                string name = m_Client.Self.FirstName;
                string target = "mind" + name.ElementAt(name.Length - 1) + "@jlnetbook/smack";

                Console.WriteLine("Incoming Message : " + msg);

                JsonReading jr = new JsonReading();
                string evt = null;

                if (e.FromName.StartsWith(Constant.CONDUCTOR))
                {
                    // Case 1
                    if (msg.Equals("all aboard") == true)
                    {
                        SetDestination(e.Position); evt = "ready";
                    }
                    else
                    {
                        evt = msg;
                    }
                }
                else if (e.FromName.StartsWith(Constant.DISABLED))
                {
                    // Perceive as if it recognises disabled people
                    if (msg.Contains("space") == true)
                    {
                        evt = "detect(disabled)";
                    }
                }

                if (evt != null)
                {
                    jr.AddValue("EVENT", evt);
                    m_Sensor.Publish(jr);
                }
                else
                {
                    // no-op
                    Debug.WriteLine("No data published in Chat Message Handler!");
                }
            }
        }

        private void SetDestination(Vector3 destination)
        {
            if (m_objManager != null)
            {
                m_objManager.Destination = destination;

                // test code
                m_actionManager.BeginAction(Action.GoToward, false);
            }
        }

        public Sensor ChatSensor
        {
            get { return m_Sensor; }
            set { m_Sensor = value; }
        }
    }
}
