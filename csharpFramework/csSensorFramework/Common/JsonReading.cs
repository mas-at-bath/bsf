/*
 * Class DataReading
 * 
 *      28-03-2013
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Basic data type for Pub/Sub in the form of JSON. 
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;   // for trace
using System.Xml;
using System.IO;

using System.Net.Json;

namespace csSensorFramework
{
    public class JsonReading
    {
        private static string JSON = "JSON";

        public class Value
        {
            public string m_key;
            public string m_type;
            public object m_obj;

            public Value()
            {
                m_key = null;
                m_type = null;
                m_obj = null;
            }

            public Value(string key, object obj)
            {
                m_key = key;
                m_type = obj.GetType().Name;
                m_obj = obj;
            }

            public string Key
            {
                get { return m_key; }
            }

            public string Type
            {
                get { return m_type; }
            }

            public object Obj
            {
                get { return m_obj; }
            }
        }

        private JsonObject m_jsonObj;               // for subscribe
        private JsonObjectCollection m_jsonObjCol;  // for publish

        public JsonReading()
        {
            m_jsonObjCol = new JsonObjectCollection();
        }

        // 
        // for subscription
        //

        public void fromJSON(string msg)
        {
            string json;

            using (XmlReader xmlrd = XmlReader.Create(new StringReader(msg)))
            {
                xmlrd.ReadToFollowing(JSON);
                json = xmlrd.ReadElementString();
                // Debug.WriteLine("json string after parsing xml data : " + json);

                JsonTextParser parser = new JsonTextParser();
                m_jsonObj = parser.Parse(json);
            }
        }

        public Value FindValue(string key)
        {
            Value val = null;

            foreach (JsonObject field in m_jsonObj as JsonObjectCollection)
            {
                if (field.Name == key)
                {
                    val = new Value(field.Name, field.GetValue());
                    break;
                }
            }

            return val;
        }

        // 
        // for Publish
        //

        public void AddValue(string key, object obj)
        {
            string objType = obj.GetType().Name;

            switch (objType)
            {
                case "Double":
                    m_jsonObjCol.Add(new JsonNumericValue(key, (double)obj));
                    break;

                case "Boolean":
                    m_jsonObjCol.Add(new JsonBooleanValue(key, (Boolean)obj));
                    break;

                default:
                    m_jsonObjCol.Add(new JsonStringValue(key, (string)obj));
                    break;
            }
        }

        public void RemoveAll()
        {
            m_jsonObjCol.Clear();
        }

        public JsonObjectCollection JsonObjCollection
        {
            get { return m_jsonObjCol; }
        }
    }
}
