/*
 * Network Manager
 * 
 *      08-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Managing the networking and communicating between client and server
 *      - such as login/logout, update notify etc
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

using OpenMetaverse;
using OpenMetaverse.Packets;

namespace ConsoleBot
{
    class NetworkManager
    {
        private string m_FirstName;
        private string m_LastName;
        private string m_Password;

        private ConsoleBotInstance m_BotInstance;
        private GridClient m_GridClient = null;

        private static string strArgUsername = "-u";
        private static string strArgPassword = "-p";
        private static string strFixedLastName = "Resident";

        private bool m_bSetLoginParam;

        // Constructor
        public NetworkManager()
        {
            m_FirstName = null;
            m_LastName = strFixedLastName;
            m_Password = null;

            m_BotInstance = null;

            m_bSetLoginParam = false;
        }

        public NetworkManager(ConsoleBotInstance botInstance)
        {
            m_FirstName = null;
            m_LastName = strFixedLastName;
            m_Password = null;

            m_BotInstance = botInstance;
            m_GridClient = m_BotInstance.Client;

            m_bSetLoginParam = false;

            if (m_GridClient != null)
                RegisterEventHandler();
        }

        public bool SetLoginParam(string[] arg)
        {
            if (arg.Length > 0)
            {
                for (int i = 0; i < arg.Length; i++)
                {
                    if (arg[i].CompareTo(strArgUsername) == 0)
                    {
                        m_FirstName = arg[i + 1];   i++;
                    }
                    else if (arg[i].CompareTo(strArgPassword) == 0)
                    {
                        m_Password = arg[i + 1];    i++;
                    }
                }              
            }

            m_bSetLoginParam = ((m_FirstName != null) && (m_LastName != null));

            return m_bSetLoginParam;
        }

        public bool Login()
        {
            bool res = false;

            if (m_bSetLoginParam == false)
            {
                Console.WriteLine("Invalid user name or password");
                return false;
            }

            res = m_GridClient.Network.Login(m_FirstName, m_LastName, m_Password, "My First Bot", m_FirstName);
            if (res == true)
            {
                Console.WriteLine("Login Success!!");
            }
            else
            {
                Console.WriteLine("I couldn't log in, here is why: " + m_GridClient.Network.LoginMessage);
                Console.WriteLine("press enter to close...");
                Console.ReadLine();
            }

            return res;
        }

        public void Logout()
        {
            m_GridClient.Network.Logout();

            if (m_GridClient != null)
                DeRegisterEventHandler();
        }

        // 
        private void RegisterEventHandler()
        {
            m_GridClient.Network.LoginProgress += new EventHandler<LoginProgressEventArgs>(Network_LoginProgress);
            m_GridClient.Network.EventQueueRunning += new EventHandler<EventQueueRunningEventArgs>(Network_OnEventQueueRunning);
            m_GridClient.Network.SimChanged += new EventHandler<SimChangedEventArgs>(Network_SimChanged);

            // Test for collision detection
            m_GridClient.Network.RegisterCallback(PacketType.MeanCollisionAlert, new EventHandler<PacketReceivedEventArgs>(Network_MeanCollisionAlertHandler));
        }

        // 
        private void DeRegisterEventHandler()
        {
            m_GridClient.Network.LoginProgress -= new EventHandler<LoginProgressEventArgs>(Network_LoginProgress);
            m_GridClient.Network.EventQueueRunning -= new EventHandler<EventQueueRunningEventArgs>(Network_OnEventQueueRunning);
            m_GridClient.Network.SimChanged -= new EventHandler<SimChangedEventArgs>(Network_SimChanged);
        }

        //
        private void Network_LoginProgress(object sender, LoginProgressEventArgs e)
        {
            if (e.Status == LoginStatus.Success)
            {
                Console.WriteLine("I'm connected to the simulator, going to greet everyone around me");
                
                // m_GridClient.Self.Chat("ready", 0, ChatType.Normal);
                m_GridClient.Self.Movement.Camera.Far = 64.0f;
                                
                Console.ReadLine(); // Wait for user to press a key before exit
            }
        }

        private void Network_OnEventQueueRunning(object sender, EventQueueRunningEventArgs e)
        {
            if (e.Simulator == m_GridClient.Network.CurrentSim)
            {
                m_GridClient.Appearance.SetPreviousAppearance(true);
            }
        }

        private void Network_SimChanged(object sender, SimChangedEventArgs e)
        {
            if (e.PreviousSimulator != null)
            {
                // m_GridClient.Appearance.SetPreviousAppearance(false);
            }
        }

        // Test
        private void Network_MeanCollisionAlertHandler(object sender, PacketReceivedEventArgs e)
        {
            Debug.WriteLine("collision detected ");

            var packet = e.Packet;

            MeanCollisionAlertPacket collision = (MeanCollisionAlertPacket)packet;

            for (int i = 0; i < collision.MeanCollision.Length; i++)
            {
                MeanCollisionAlertPacket.MeanCollisionBlock block = collision.MeanCollision[i];

                DateTime time = Utils.UnixTimeToDateTime(block.Time);
                MeanCollisionType type = (MeanCollisionType)block.Type;

                //Self_OnMeanCollision(e.Simulator, new MeanCollisionEventArgs(type, block.Perp, block.Victim, block.Mag, time));
                MeanCollisionEventArgs mc = new MeanCollisionEventArgs(type, block.Perp, block.Victim, block.Mag, time);
                Debug.WriteLine("Attacker = " + mc.Aggressor);
            }
        }

        //////////////////////////////////////////////////////////////////////////
        // Get/Set properties
        //////////////////////////////////////////////////////////////////////////

        // Get User Name
        public string Username
        {
            get { return m_FirstName; }
        }

        // Get User Name
        public string Password
        {
            get { return m_Password; }
        }
    }
}