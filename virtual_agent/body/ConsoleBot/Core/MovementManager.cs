/*
 * Movement Manager
 * 
 *      08-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Managing the agent movement such as going ahead, turning left/right or back
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Timers;
using System.Threading;
using System.Diagnostics;

using OpenMetaverse;

namespace ConsoleBot
{
    class MovementManager
    {
        private ConsoleBotInstance m_BotInstance;
        private GridClient m_Client;

        private System.Timers.Timer m_Timer;
        private bool m_GoForward;
        private bool m_GoBack;
        private bool m_TurnRight;
        private bool m_TurnLeft;
        private bool m_Flying;

        public MovementManager()
        {
            // do nothing
            m_BotInstance = null;
            m_Client = null;

            m_GoForward = false;
            m_GoBack = false;
            m_TurnRight = false;
            m_TurnLeft = false;
            m_Flying = false;
        }

        public MovementManager(ConsoleBotInstance instance)
        {
            m_BotInstance = instance;
            m_Client = m_BotInstance.Client;

            m_GoForward = false;
            m_GoBack = false;
            m_TurnRight = false;
            m_TurnLeft = false;
            m_Flying = false;

            m_Timer = new System.Timers.Timer(500);
            m_Timer.Elapsed += new ElapsedEventHandler(TurnTimerElapsed);
            m_Timer.Enabled = false;
        }

        ~MovementManager()
        {
            Dispose();
        }

        public void Dispose()
        {
            m_Timer.Enabled = false;
            m_Timer.Dispose();
            m_Timer = null;
        }
        
        public bool GoForward
        {
            get
            {
                return m_GoForward;
            }
            set
            {
                m_GoForward = value;
                if (value)
                {
                    m_Client.Self.Movement.AtPos = true;
                    m_Client.Self.Movement.SendUpdate(true);
                }
                else
                {
                    m_Client.Self.Movement.AtPos = false;
                    m_Client.Self.Movement.SendUpdate(true);
                }
            }
        }

        public bool GoBack
        {
            get
            {
                return m_GoBack;
            }
            set
            {
                m_GoBack = value;
                if (value)
                {
                    m_Client.Self.Movement.AtNeg = true;
                    m_Client.Self.Movement.SendUpdate(true);
                }
                else
                {
                    m_Client.Self.Movement.AtNeg = false;
                    m_Client.Self.Movement.SendUpdate(true);

                }
            }
        }

        public bool TurnLeft
        {
            get
            {
                return m_TurnLeft;
            }
            set
            {
                m_TurnLeft = value;
                if (value)
                {
                    TurnTimerElapsed(null, null);
                    m_Timer.Enabled = true;
                }
                else
                {
                    m_Timer.Enabled = false;
                    m_Client.Self.Movement.TurnLeft = false;
                    m_Client.Self.Movement.SendUpdate(true);
                }
            }
        }

        public bool TurnRight
        {
            get
            {
                return m_TurnRight;
            }
            set
            {
                m_TurnRight = value;
                if (value)
                {
                    TurnTimerElapsed(null, null);
                    m_Timer.Enabled = true;
                }
                else
                {
                    m_Timer.Enabled = false;
                    m_Client.Self.Movement.TurnRight = false;
                    m_Client.Self.Movement.SendUpdate(true);
                }
            }
        }

        public bool Fly
        {
            get
            {
                return m_Flying;
            }
            set
            {
                m_Flying = m_Client.Self.Movement.Fly = value;
            }
        }

        private void TurnTimerElapsed(object sender, ElapsedEventArgs e)
        {
            float delta = ((float) m_Timer.Interval / 1000f) * 2;

            if (m_TurnLeft)
            {
                m_Client.Self.Movement.TurnLeft = true;
                m_Client.Self.Movement.BodyRotation = m_Client.Self.Movement.BodyRotation * Quaternion.CreateFromAxisAngle(Vector3.UnitZ, delta);
                m_Client.Self.Movement.SendUpdate(true);
            }
            else if (m_TurnRight)
            {
                m_Client.Self.Movement.TurnRight = true;
                m_Client.Self.Movement.BodyRotation = m_Client.Self.Movement.BodyRotation * Quaternion.CreateFromAxisAngle(Vector3.UnitZ, -delta);
                m_Client.Self.Movement.SendUpdate(true);
            }
        }

        public void DoGesture(UUID uuidGesture)
        {
            if (m_BotInstance != null)
            {
                m_BotInstance.Client.Self.PlayGesture(uuidGesture);
            }
        }

        public void DoAnimation(UUID uuidAnimation, bool bAnim)
        {
            if (m_BotInstance != null)
            {
                Dictionary<UUID, bool> anim = new Dictionary<UUID, bool>();
                anim.Add(uuidAnimation, bAnim);

                m_BotInstance.Client.Self.Animate(anim, false);
            }
        }

        public void MoveTowardIndirect(Vector3 pos)
        {
            Vector3 org = m_Client.Self.RelativePosition;

            int mod = Math.Max(Convert.ToInt32(Math.Abs(pos.X - org.X)) / 10, Convert.ToInt32(Math.Abs(pos.Y - org.Y)) / 10);
            int stepcnt = 10 * (mod + 1); 

            float stepX = (pos.X - org.X) / stepcnt;
            float stepY = (pos.Y - org.Y) / stepcnt;

            // Debug.WriteLine("stepX = " + stepX + ", StepY = " + stepY);

            for (int i = 0; i < stepcnt; i++)
            {
                org.X += stepX; org.Y += stepY;

                m_Client.Self.AutoPilotLocal(Convert.ToInt32(org.X), Convert.ToInt32(org.Y), pos.Z);
                
                Thread.Sleep(500);
            }
        }

        public void MoveToward(Vector3 pos)
        {
            if (m_Client != null)
            {
                if (pos.Z > 1)
                {
                    m_Client.Self.AutoPilotLocal(Convert.ToInt32(pos.X), Convert.ToInt32(pos.Y), pos.Z);
                }
                else
                {
                    m_Client.Self.AutoPilotCancel();
                }
            }
        }
    }
}
