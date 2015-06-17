/*
 * Action Manager
 * 
 *      17-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Doing some actions in virtual environment corresponding to the user command
 *      Firstly, analysing the user command that what user would like to do,
 *      Secondly, run the action such as movement, animations or social activities
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Timers;

using OpenMetaverse;

namespace ConsoleBot
{
    class ActionManager
    {
        // have to move into common action scope later
        // 03/01/2012, jeehang.lee
        private static ActionMap[] m_ActionMap = 
        {
            new ActionMap("go", Action.GoForward),
            new ActionMap("back", Action.GoBack),
            new ActionMap("left", Action.TurnLeft),
            new ActionMap("right", Action.TurnRight),
            new ActionMap("logout", Action.Logout),
            new ActionMap("quit", Action.Logout),
            new ActionMap("bow", Action.bow),
            new ActionMap("move_origin", Action.GoToward),
            new ActionMap("fly", Action.Fly),
            new ActionMap("move", Action.Move),
            new ActionMap("pause", Action.Pause),
            new ActionMap("stop", Action.Stop),
            new ActionMap("", Action.Logout)
        };

        private static UUID m_GestureHello = DefGesture.BOW;

        private ConsoleBotInstance m_BotInstance;
        private MovementManager m_MoveManager;
        
        private Timer m_Timer;
        private Action m_CurrentAction;
        private bool m_bEableAction;

        public ActionManager(ConsoleBotInstance instance)
        {
            m_BotInstance = instance;
            m_MoveManager = m_BotInstance.MoveManager;

            m_Timer = new System.Timers.Timer(500);
            m_Timer.Elapsed += new ElapsedEventHandler(ActionTimerElapsed);
            m_Timer.Enabled = false;

            m_CurrentAction = Action.None;
            m_bEableAction = false;
        }

        ~ActionManager()
        {
            m_Timer.Enabled = false;
            m_Timer.Dispose();
            m_Timer = null;
        }

        public Action GetAction(string cmd)
        {
            Action action = Action.None;

            // for measuring
            Console.WriteLine("Time to GetAction: " + DateTime.Now.Millisecond.ToString());

            foreach (ActionMap amap in m_ActionMap)
            {
                if (cmd.CompareTo(amap.m_ActionString) == 0)
                {
                    action = amap.m_Action;
                    break;
                }
            }

            return action;
        }

        public void InvokeAction(Action action)
        {
            if (action < Action.Pause)
            {
                // timer needed actions - go/back/turn left/right
                // otherwise, timer is not mandatory to run something
                BeginAction(action, (action <= Action.TurnRight));
            }
            else
            {
                if (action == Action.Pause)
                {
                    EndAction(Action.Move);
                }
                else
                {
                    // stop current action
                    EndAction(m_CurrentAction);
                }
            }
        }

        public void BeginAction(Action action, bool bUseTimer)
        {
            m_CurrentAction = action;
            m_bEableAction = true;
            EnableAction(m_MoveManager, action, m_bEableAction, bUseTimer);
        }

        public void EndAction(Action action)
        {
            EnableAction(m_MoveManager, action, false, false);
            m_CurrentAction = Action.None;
            m_bEableAction = false;
        }

        private void EnableAction(MovementManager mm, Action action, bool bStart, bool bTimerEnable)
        {
            switch (action)
            {
                case Action.GoForward:
                    mm.GoForward = bStart;
                    break;

                case Action.GoBack:
                    mm.GoBack = bStart;
                    break;

                case Action.TurnLeft:
                    mm.TurnLeft = bStart;
                    break;

                case Action.TurnRight:
                    mm.TurnRight = bStart;
                    break;

                case Action.bow:
                    mm.DoGesture(m_GestureHello);
                    break;

                case Action.Fly:
                    mm.Fly = mm.Fly ? false : true;
                    break;

                case Action.GoToward:
                    //mm.MoveToward(m_BotInstance.ObjEvtManager.Destination);
                    mm.MoveTowardIndirect(m_BotInstance.ObjEvtManager.Destination);
                    break;

                // continuous walking - require stop action
                case Action.Move:
                    mm.GoForward = bStart;
                    break;

                default:
                    break;
            }

            m_Timer.Enabled = bTimerEnable;
        }

        // This function is called in order to disable movement actions 
        // otherwise, the movement is not stop unless the user orders the
        // stop message
        private void ActionTimerElapsed(object sender, ElapsedEventArgs e)
        {
            if ((m_CurrentAction <= Action.TurnRight) && (m_Timer.Enabled == true))
            {
                if (m_bEableAction == true)
                {
                    EndAction(m_CurrentAction);
                    // EnableAction(m_MoveManager, m_CurrentAction, false, false);
                }
            }
        }
    }
}
