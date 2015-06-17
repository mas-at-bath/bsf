using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;   // for trace
using System.Threading;

using Matrix.Xmpp.Client;
using csSensorFramework;

namespace SimplePublisher
{
    class Program
    {
        public static bool m_bReady;
        private static DateTime dt = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        static void Main(string[] args)
        {
            Sensor ss = new Sensor("jl2", "user2", "bathstudent", "example");
            ss.SensorCreateEvent += new SensorCreateEventHandler(OnSensorCreated);

            JsonReading jr = new JsonReading();
            int cnt = 0;

            while (cnt < 152140)
            {
                TimeSpan ts = DateTime.UtcNow - dt;
                long millis = (long)ts.TotalMilliseconds;

                jr.AddValue("takenAt", (double)millis);
                jr.AddValue("ACTION", "convoyMember4,amMoving(true)");
                jr.AddValue("position", "153.04442797954152,0.0,262.3134876039802,149.5465600849747,0.0,262.43563584243896");

                ss.Publish(jr);
                jr.RemoveAll();
                cnt++;
                Thread.Sleep(100);

            }
        }

        public static void OnSensorCreated(object sender, SensorCreateEventArgs e)
        {
            m_bReady = true;
            Debug.WriteLine("Sensor is created successfully!");
        }
    }
}
