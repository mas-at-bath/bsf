using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;   // for trace
using System.Threading;

using csSensorFramework;
using Matrix.Xmpp.Client;

namespace SimpleSubscriber
{
    class Program
    {
        public static SensorClient sc;
        private TimeSpan ts;
        private static DateTime dt = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        private static JsonReading jr = new JsonReading();

        static void Main(string[] args)
        {
            // Creating SensorClient performs automatically 'subscribe' action
            sc = new SensorClient("jl2", "user3", "bathstudent");
            sc.SensorClientCreateEvent += new SensorClientCreateEventHandler(sc_SensorClientCreateEvent);

            while (true)
            {
                // no-op
            }
        }

        static void sc_SensorClientCreateEvent(object sender, SensorClientCreateEventArgs e)
        {
            sc.Subscribe("example", Subscribehandler);
        }

        public static void Subscribehandler(object sender, MessageEventArgs e)
        {
            TimeSpan ts = DateTime.UtcNow - dt;
            long millis = (long)ts.TotalMilliseconds;

            if (e.Message.ToString().Contains("delete node") != false)
            {
                sc.Unsubscribe("example");
            }
            else
            {
                jr.fromJSON(e.Message.ToString());
                JsonReading.Value val = jr.FindValue("takenAt");

                Double begin = (Double)val.Obj;
                long elapsed = millis - (long)begin;
                Debug.WriteLine(elapsed);
            }
        }
    }
}
