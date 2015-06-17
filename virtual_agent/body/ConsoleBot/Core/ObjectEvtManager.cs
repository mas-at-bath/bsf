/*
 * Object Manager
 * 
 *      18-04-2012
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Handling events being related in objects in the simulator. 
 *      All events about objects are sent from the simulator.
 */

using System;
using System.Collections.Generic;
using System.Collections;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Threading;

using OpenMetaverse;

namespace ConsoleBot
{
    class ObjectEvtManager
    {
        private readonly static string CONDUCTOR = "nohannara Resident";
        private readonly static float NOINIT = -1000.0f;

        private ConsoleBotInstance m_botInstance = null;
        private GridClient m_GridClient = null;
                
        private Vector3 m_PosDest;
        private Quaternion m_rotation;

        // test
        private IntentionRecogniser m_ir = null;
        private Vector3 m_oldPos;
        private string m_leader = null;
        private bool m_highSpeed = false;

        // blackboard
        private Hashtable m_avatarTable = new Hashtable();

        public ObjectEvtManager(ConsoleBotInstance botInstance)
        {
            m_botInstance = botInstance;
            m_GridClient = botInstance.Client;

            m_rotation = m_GridClient.Self.SimRotation;

            if (m_GridClient != null)
            {
                RegisterEventHandler();
            }

            m_ir = new IntentionRecogniser(botInstance);
            m_PosDest = new Vector3(NOINIT, NOINIT, NOINIT);
            m_oldPos = new Vector3(NOINIT, NOINIT, NOINIT);
        }

        ~ObjectEvtManager()
        {
            if (m_GridClient != null)
                DeRegisterEventHandler();
        }
        
        // Event handler
        private void RegisterEventHandler()
        {
            m_GridClient.Objects.ObjectUpdate += new EventHandler<PrimEventArgs>(OnObjectUpdate);
            m_GridClient.Objects.TerseObjectUpdate += new EventHandler<TerseObjectUpdateEventArgs>(OnTerseObjectUpdate);
            m_GridClient.Objects.AvatarUpdate += new EventHandler<AvatarUpdateEventArgs>(OnAvatarUpdate);
        }

        private void DeRegisterEventHandler()
        {
            m_GridClient.Objects.ObjectUpdate -= new EventHandler<PrimEventArgs>(OnObjectUpdate);
            m_GridClient.Objects.TerseObjectUpdate -= new EventHandler<TerseObjectUpdateEventArgs>(OnTerseObjectUpdate);
            m_GridClient.Objects.AvatarUpdate += new EventHandler<AvatarUpdateEventArgs>(OnAvatarUpdate);
        }

        public void OnObjectUpdate(object sender, PrimEventArgs e)
        {
            // Console.WriteLine("OnObjectUpdated" + e.Prim);
        }

        public void OnTerseObjectUpdate(object sender, TerseObjectUpdateEventArgs e)
        {
            ObjectMovementUpdate omu = e.Update;
            Avatar avatar;

            m_GridClient.Network.CurrentSim.ObjectsAvatars.TryGetValue(e.Update.LocalID, out avatar);

            if (avatar != null && avatar.Name != "")
            {
                if (avatar.Name == CONDUCTOR)
                {
                    m_ir.UpdateState(m_GridClient.Self.RelativePosition, avatar.Position);
                    Debug.WriteLine("UUID = " + avatar.ID);

                    if (Vector3.Distance(m_GridClient.Self.RelativePosition, avatar.Position) < 5)
                        Debug.WriteLine("collision happened between player and " + m_GridClient.Self.Name);
                }

                // follow avatar
                FollowAvatar(avatar);
            }
        }

        public void OnAvatarUpdate(object sender, AvatarUpdateEventArgs e)
        {
            /*
             * NOP
             * 
            Avatar avatar = e.Avatar;

            if ((avatar != null) && (avatar.Name == CONDUCTOR))
                m_ir.UpdateState(m_GridClient.Self.RelativePosition, avatar.Position);
             */
            Avatar avatar = e.Avatar;
            if (avatar != null)
            {
                m_avatarTable[avatar.Name] = avatar.Position;
                Console.WriteLine("OnAvatarUpdated: " + avatar.Name);
            }
        }

        public void UpdateAction(string action)
        {
            string term;

            if (action.Contains("moveto"))
            {
                term = action.Substring(action.IndexOf("(") + 1, action.IndexOf(")") - action.IndexOf("(") - 1);
                m_leader = term + " Resident";
                MoveTo(term, m_leader);
            }
            else if (action.Contains("follow"))
            {
                // follow someone
                term = action.Substring(action.IndexOf("(") + 1, action.IndexOf(")") - action.IndexOf("(") - 1);
                m_leader = term + " Resident";
            }
            else if (action.Contains("update_ipd"))
            {
                // update cap
                m_ir.updateAction(action);
            }
            else if (action.Contains("update_speed"))
            {
                term = action.Substring(action.IndexOf("(") + 1, action.IndexOf(")") - action.IndexOf("(") - 1);
                m_highSpeed = (term == "fast") ? true : false;
            }
            else
            {
                // no-op
            }
        }

        // Get Origin
        public Vector3 Destination
        {
            get { return m_PosDest; }
            set { m_PosDest = value; }
        }

        // variable IntentionRecogniser
        public IntentionRecogniser IR
        {
            get { return m_ir; }
            set { m_ir = value; }
        }

        //
        //
        //
        private bool DoNextBehaviour(float distToDest, float distToSelf, bool bDisabled)
        {
            bool result = false;

            ActionManager am = new ActionManager(m_botInstance);

            if (bDisabled == true)
            {
                if (distToDest < distToSelf)
                {
                    am.InvokeAction(Action.GoBack);
                }
                else
                {
                    Console.WriteLine("waiting for additional movement");
                }
            }
            else
            {
                if (distToSelf > distToDest)
                {
                    // enable social force
                    // enable Inter-Personal distance behaviours
                }
            }

            return result;
        }

        // Following
        private void FollowAvatar(Avatar avatar)
        {
            if (avatar.Name == m_leader)
            {
                Vector3 pos = avatar.Position;
                if (Vector3.Distance(pos, m_oldPos) > 0)
                {
                    if (pos.Z > 1)
                    {
                        m_GridClient.Self.AutoPilotLocal(Convert.ToInt32(pos.X + 0.7), Convert.ToInt32(pos.Y + 0.7), pos.Z);

                    }
                    else
                    {
                        m_GridClient.Self.AutoPilotCancel();
                    }

                    m_oldPos = pos;
                }

                m_GridClient.Self.Movement.BodyRotation = avatar.Rotation;
                m_GridClient.Self.Movement.SendUpdate(true);
            }
        }

        private void MoveTo(string term, string name)
        {
            Vector3 pos = new Vector3(-1, -1, -1);

            if (m_avatarTable.ContainsKey(name))
            {
                pos = (Vector3)m_avatarTable[name];
            }
            else
            {
                if (isPlace(term) == true)
                    pos = new Vector3(81, 114, 37);
            }

            if (pos.Z > 1)
            {
                if (m_highSpeed == true)
                    m_botInstance.MoveManager.MoveToward(pos);
                else
                    m_botInstance.MoveManager.MoveTowardIndirect(pos);
            }
        }

        private void MoveTo(Vector3 destination)
        {
        }

        private bool isPlace(string term)
        {
            if (term == "exit")
                return true;

            return false;
        }
    }
}
