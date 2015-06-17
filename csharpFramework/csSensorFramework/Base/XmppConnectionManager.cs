using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Matrix;
using Matrix.Xmpp;
using Matrix.Xmpp.Client;
using Matrix.Xmpp.PubSub;
using Matrix.Xml;

namespace csSensorFramework
{
    class XmppConnection
    {
        private string m_server;
        private string m_user;
        private string m_pwd;
        private XmppClient m_xc;

        public XmppConnection()
        {
            m_server = null;
            m_user = null;
            m_pwd = null;
            m_xc = null;
        }

        public XmppConnection(string server, string user, string pwd)
        {
            m_server = server;
            m_user = user;
            m_pwd = pwd;
            _CreateXmppClient();            
        }

        public string Server
        {
            get { return m_server; }
        }

        public string Username
        {
            get { return m_user; }
        }

        public string Password
        {
            get { return m_pwd; }
        }

        public XmppClient Client
        {
            get { return m_xc; }
        }

        private void _CreateXmppClient()
        {
            _License();    
        }

        private void _License()
        {
            string lic = @"eJxkkFtzgjAQhf+K01enTVTQ2tlm6igiohYvYO1bIIGiQBgS1Prra6vWXl52
                        9uy3lzMLozjgmeSVfZpk8vGGRrdShGpHC/6QnNANAacQrAyUxchclSwWgK4V
                        mJY0U7F6JzVA3zl0S6lEygsCE5pyYmxpUlIlCkBfGroizWn2fgGxyCpnK4Au
                        DIyUxgmRNOHy6YezO3ZsOrFj8/chN2dUcWOfxwXvHTNSxzUNN+o6oH8ILNnj
                        qSCqKI+7zgI+4+/5BtbqGqA/AOZxlFFVFpzMhN7oLbDEnRrdj5531XVzsXfH
                        0eC1j5npLFH4OtCxHyQrar2Z46Hpbsa05dqrruHOtrvQe2kPzfZK5vk0EO6E
                        HajOV63IqU4bzlqUTt/2vb7H7Ht9aedq3lwsa3hjekPPDltbayK0deJPssLa
                        ul580PRwvj7cp6zanhmBr3WnzqjZeRt0kg5ebh4BXX0DOr+bfAgg";

            Matrix.License.LicenseManager.SetLicense(lic);
        }
    }

    public class XmppConnectionManager
    {
        private List<XmppConnection> m_connection;

        public XmppConnectionManager()
        {
            m_connection = new List<XmppConnection>(3);
        }

        ~XmppConnectionManager()
        {
            int cnt = m_connection.Count;
            
            m_connection.RemoveRange(0, cnt);
        }

        public XmppConnection AddXmppConnection(string server, string user, string pwd)
        {
            XmppConnection conn = new XmppConnection(server, user, pwd);

            m_connection.Add(conn);

            return conn;
        }

        public XmppClient GetXmppClient(string server, string user, string pwd)
        {
            XmppConnection conn = _FindXmppConnection(server, user, pwd);

            if (conn == null)
                conn = AddXmppConnection(server, user, pwd);
            
            return conn.Client;
        }

        private XmppConnection _FindXmppConnection(string server, string user, string pwd)
        {
            IEnumerator<XmppConnection> it = m_connection.GetEnumerator();
            
            while (it.MoveNext())
            {
                XmppConnection conn = (XmppConnection) it.Current;
                if (conn.Server == server &&
                    conn.Username == user &&
                    conn.Password == pwd)
                    return conn;
            }

            return null;
        }
    }
}
