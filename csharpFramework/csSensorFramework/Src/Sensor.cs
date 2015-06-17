/*
 * Sensor Class
 * 
 *      04-01-2012
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Sensor framework to collect, exchange data among external/internal components.
 *      Being developed from bath-sensor-framework (BSF) by java programming language.
 *      Only one difference between BSF and this is the base library, Jabber .net and smacks 
 *      which arecorresponding to programming language, such as c# and java

 *      28-03-2013
 *      Modified by JeeHang (jeehanglee@gmail.com)
 *      
 *      Changes in XMPP library, from Jabber-net to Matrix
 *      Both Messaging and Pub/Sub are supported
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

using System.Net.Json;

namespace csSensorFramework
{
    class SensorPayload : XmppXElement
    {
        public SensorPayload() : base(Constant.XMLNS_JSON, Constant.JSON)
        {
            // no-op
        }
    }

    // Event Argument for ready to use sensor.
    // Ready Event is derived from System.EventArgs.
    public class SensorCreateEventArgs : System.EventArgs
    {
        private readonly bool m_bReady;

        public SensorCreateEventArgs(bool ready)
        {
            this.m_bReady = ready;
        }

        public bool Ready
        {
            get { return m_bReady; }
        }
    }

    // Xmpp SensorClient created event
    public delegate void SensorCreateEventHandler(object sender, SensorCreateEventArgs e);

    public class Sensor
    {
        private string m_user;
        private string m_pwd;
        private string m_node;
        private string m_server;

        private XmppClient m_xmppClient;
        private PubSubManager m_psMgr;

        private SensorPayload m_payload;
        private Item m_psItem;
        private bool m_bReady;
        private Jid m_psJid;

        //
        // Public Methods
        //
        public Sensor(string server, string user, string pwd, string node)
        {
            m_user = user;
            m_server = server;
            m_pwd = pwd;
            m_node = node;

            m_xmppClient = new XmppClient();
            m_payload = new SensorPayload();
            m_psItem = new Matrix.Xmpp.PubSub.Item();
            m_psJid = new Jid("pubsub." + m_server);
            m_bReady = false;

            _License();
            Connect();
        }

        public Sensor()
        {
        }

        public void Create(string server, string user, string pwd, string node)
        {
            m_user = user;
            m_server = server;
            m_pwd = pwd;
            m_node = node;

            m_xmppClient = new XmppClient();
            m_payload = new SensorPayload();
            m_psItem = new Matrix.Xmpp.PubSub.Item();
            m_psJid = new Jid("pubsub." + m_server);
            m_bReady = false;

            _License();
            Connect();
        }

        public void Cleanup()
        {
            m_psMgr.DeleteNode(m_psJid, m_node, new EventHandler<IqEventArgs>(OnPubSubDeleteNode));
        }

        ~Sensor()
        {
            m_psMgr.Dispose();
            // m_xmppClient.Close();
        }

        public void Publish(JsonReading jr)
        {
            if (m_bReady == true)
            {
                var item = new Matrix.Xmpp.PubSub.Item();
                item.Add(new SensorPayload { Value = jr.JsonObjCollection.ToString() });
                m_psMgr.PublishItem(m_psJid, m_node, item);
                //Debug.WriteLine("item published" + jr.JsonObjCollection.ToString());
            }
        }

        //
        // Get/Set Properies
        //

        public XmppClient Client
        {
            get { return m_xmppClient; }
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

        private void _CreateSensorCommon()
        {
            m_psMgr = new PubSubManager(m_xmppClient);
            m_psJid = new Jid("pubsub." + m_server);
            m_psMgr.CreateNode(m_psJid, m_node, new EventHandler<IqEventArgs>(OnPubSubCreateNode));
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
            Debug.WriteLine("Success in XMPP Binding in sensor: " + e.Jid);
            _CreateSensorCommon();
        }

        private void OnMessage(object sender, MessageEventArgs e)
        {
            Debug.WriteLine("Default message handler is enabled:");
        }

        private void OnPubSubCreateNode(object sender, IqEventArgs e)
        {
            m_bReady = false;
            var error = e.Iq.Element<Error>();

            if (e.Iq.Type == Matrix.Xmpp.IqType.result)
            {
                Debug.WriteLine("Node [" + m_node + "] is created!");
                m_bReady = true;
            }
            else if (e.Iq.Type == Matrix.Xmpp.IqType.error)
            {
                if (error != null)
                {
                    if (error.Condition == Matrix.Xmpp.Base.ErrorCondition.Conflict)
                    {
                        Debug.WriteLine("Node [" + m_node + "] is already exist!");
                        m_bReady = true;
                    }
                    else
                    {
                        Debug.WriteLine("creation of node [{0}] failed!\r\nError Condition: {1}\r\nError Type: {2}", m_node, error.Condition, error.Type);
                    }
                }
            }

            if ((m_bReady) && (m_node != null))
            {
                SensorCreateEventArgs sce = new SensorCreateEventArgs(m_bReady);
                OnSensorCreateEvent(sce);
            }
        }

        // The event member that is of type SensorClientEventHandler
        public event SensorCreateEventHandler SensorCreateEvent;

        // The protected OnSensorClientEvent method raises the event by invoking 
        // the delegates. The sender is always this, the current instance of the class.
        protected virtual void OnSensorCreateEvent(SensorCreateEventArgs e)
        {
            if (m_bReady == true)
            {
                SensorCreateEvent(this, e);
            }
        }

        private void OnPubSubDeleteNode(object sender, IqEventArgs e)
        {
            var pubsub = e.Iq.Element<Matrix.Xmpp.PubSub.PubSub>();
            var error = e.Iq.Element<Error>();

            if (e.Iq.Type == Matrix.Xmpp.IqType.result)
            {
                Debug.WriteLine("node [" + m_node + "] is deleted!");
            }
            else if (e.Iq.Type == Matrix.Xmpp.IqType.error)
            {
                Debug.WriteLine("creation of node [{0}] failed!\r\nError Condition: {1}\r\nError Type: {2}", m_node, error.Condition, error.Type);
            }
        }

        // for c++ users
        public bool isCreated()
        {
            return m_bReady;
        }
    }
}
