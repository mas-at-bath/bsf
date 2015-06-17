/*
 * SensorClient Class
 * 
 *      04-09-2012
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Sensor framework to collect, exchange data among external/internal components.
 *      Being developed from bath-sensor-framework (BSF) by java programming language.
 *      Only one difference between BSF and this is the base library, Jabber .net and smacks 
 *      which arecorresponding to programming language, such as c# and java

 *      28-03-2013
 *      Modified by JeeHang (jeehanglee@gmail.com)
 *      
 *      - Changes in XMPP library, from Jabber-net to Matrix
 *      - Both Messaging and Pub/Sub are supported
 *      - Event triggered when sensorClient is ready to use
 *      
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;   // for trace

using Matrix;
using Matrix.Xmpp;
using Matrix.Xmpp.Client;
using Matrix.Xmpp.PubSub;
using Matrix.Xml;

// JSON support
using System.Net.Json;

namespace csSensorFramework
{
    // Event Argument for ready to use sensorClient
    // Ready Event is derived from System.EventArgs.
    public class SensorClientCreateEventArgs : System.EventArgs
    {
        private readonly bool m_scReady;

        public SensorClientCreateEventArgs(bool ready)
        {
            this.m_scReady = ready;
        }

        public bool Ready
        {
            get { return m_scReady; }
        }
    }

    // Xmpp SensorClient created event
    public delegate void SensorClientCreateEventHandler(object sender, SensorClientCreateEventArgs e);

    // Xmpp Iq Event Handler
    public delegate void OnXmppMsgHandler(object sender, MessageEventArgs e);

    public class SensorClient
    {
        public class SubNodeHandler
        {
            private string m_node;
            private OnXmppMsgHandler m_handler;

            public SubNodeHandler(string node, OnXmppMsgHandler handler)
            {
                m_node = node;
                m_handler = handler;
            }

            public string Node
            {
                get { return m_node; }
                set { m_node = value; }
            }

            public OnXmppMsgHandler Handler
            {
                get { return m_handler; }
                set { m_handler = value; }
            }
        }

        // for XMPP connection and its operations
        private XmppClient m_xmppClient;
        private PubSubManager m_psMgr;
        private Item m_psItem;
        private bool m_bCreated;
        private Jid m_psJid;

        // for data manipulation
        private SensorPayload m_payload;

        // supplementary for connection & sub
        private string m_user;
        private string m_pwd;
        private string m_server;
        private List<SubNodeHandler> m_subNH;

        //
        // Public Methods
        //

        public SensorClient(string server, string user, string pwd)
        {
            m_user = user;
            m_server = server;
            m_pwd = pwd;
            m_subNH = new List<SubNodeHandler>(3);
            m_bCreated = false;

            m_xmppClient = new XmppClient();
            m_payload = new SensorPayload();
            m_psItem = new Matrix.Xmpp.PubSub.Item();
            m_psJid = new Jid("pubsub." + m_server);
            m_psMgr = new PubSubManager(m_xmppClient);

            _License();
            Connect();
        }

        public void Subscribe(string node, OnXmppMsgHandler handler)
        {
            m_psMgr.OnEvent += new EventHandler<MessageEventArgs>(handler);
            m_psMgr.Subscribe(m_psJid, node, m_user + "@" + m_server, new EventHandler<IqEventArgs>(OnSubscribe));
            m_subNH.Add(new SubNodeHandler(node, handler));
        }

        protected void OnSubscribe(object sender, IqEventArgs e)
        {
            var error = e.Iq.Element<Error>();
        }

        public void Unsubscribe(string node)
        {
            IEnumerator<SubNodeHandler> iter = m_subNH.GetEnumerator();
            SubNodeHandler snh = null;

            while (iter.MoveNext())
            {
                if (iter.Current.Node == node)
                {
                    snh = iter.Current;
                    break;
                }
            }

            if (snh != null)
            {
                m_psMgr.OnEvent -= new EventHandler<MessageEventArgs>(snh.Handler);
                m_psMgr.Unsubscribe(m_psJid, snh.Node, m_user + "@" + m_server);
                m_subNH.Remove(snh);
            }
            else
            {
                m_psMgr.Unsubscribe(m_psJid, node, m_user + "@" + m_server);
            }
        }

        //
        // Get/Set Properies
        //

        public XmppClient Client
        {
            get { return m_xmppClient; }
        }

        // represent the connection status to the XMPP server
        public bool Connected
        {
            get { return m_bCreated; }
        }

        //
        // Private Methods
        //

        private void _License()
        {
            string lic = @"eJxkkFtTwjAQhf8K46uj6QUpOGvG2pbCFOqliJe3QEKNtElNk1L+vah4f9nZ
                        s99ezixM+JKJmnXashD12QHJj2q50hui2GnxgQ4wXClJzVKPKc60oVwC+q7A
                        tSFCc73FNqCvHAJTa1kyhSElJcNRQwpDtFSA3jUEsqyI2H4CLkVnbwXQJ4Oo
                        JLzANSlYff7D2THdNX2wXfPXoduKEs2ituKKhbsMO5bdtTyrC+gfgnEdslJi
                        rcxu117AW/w971p9ywX0B0DGc0G0UQzP02CQrWWQot7oMmd2M3gJKuHqNGCT
                        kB7S7Hb1xAJBrd78ZvWsB55pEk4H8cJUiyRUj97QnznDKxXaW/GQ3uhN7LdN
                        JtZjlN/R7MXEE2f4eDLzG52sddfpX0RuxRt33IvrvN0+L8tRtHHmnp8V1+oi
                        aoVnVv1yM/VTezabLkIUJeTeRkkbT60zQN++Ae3fjV8FEA==";

            Matrix.License.LicenseManager.SetLicense(lic);
        }

        private void Connect()
        {
            m_xmppClient.OnBind += new System.EventHandler<JidEventArgs>(OnBind);

            m_xmppClient.SetUsername(m_user);
            m_xmppClient.SetXmppDomain(m_server);
            m_xmppClient.Password = m_pwd;

            m_xmppClient.Status = "data mode";
            m_xmppClient.Show = Matrix.Xmpp.Show.chat;

            m_xmppClient.Open();
        }

        // 
        // Event Handlers
        //

        private void OnLogin(object sender, Matrix.EventArgs e)
        {
            Debug.WriteLine("Success in XMPP Login: " + e.State);
        }

        private void OnBind(object sender, JidEventArgs e)
        {
            Debug.WriteLine("Success in XMPP Binding in sensor client: " + e.Jid);

            m_bCreated = true;
            SensorClientCreateEventArgs sce = new SensorClientCreateEventArgs(true);
            OnSensorClientCreateEvent(sce);
        }

        // The event member that is of type SensorClientEventHandler
        public event SensorClientCreateEventHandler SensorClientCreateEvent;

        // The protected OnSensorClientEvent method raises the event by invoking 
        // the delegates. The sender is always this, the current instance of the class.
        protected virtual void OnSensorClientCreateEvent(SensorClientCreateEventArgs e)
        {
            if (m_bCreated == true)
            {
                SensorClientCreateEvent(this, e);
            }
        }

        private void OnMessage(object sender, MessageEventArgs e)
        {
            Debug.WriteLine("Default message handler is enabled:");
        }

        // Invoked just after the creation (or retrieving) the node.
        // If newly created or already exists, then subscribe (or other operations) can be carried out.
        // otherwise, no-op.
        private void OnPubSubCreateNode(object sender, IqEventArgs e)
        {
            m_bCreated = false;
            var error = e.Iq.Element<Error>();

            if (e.Iq.Type == Matrix.Xmpp.IqType.result)
            {
                Debug.WriteLine("node is created!");
                m_bCreated = true;
            }
            else if (e.Iq.Type == Matrix.Xmpp.IqType.error)
            {
                if (error != null)
                {
                    if (error.Condition == Matrix.Xmpp.Base.ErrorCondition.Conflict)
                    {
                        Debug.WriteLine("Node is already exist!");
                        m_bCreated = true;
                    }
                    else
                    {
                        Debug.WriteLine("creation of node failed!\r\nError Condition: {0}\r\nError Type: {1}", error.Condition, error.Type);
                    }
                }
            }
        }
    }
}
