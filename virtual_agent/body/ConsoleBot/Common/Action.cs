/*
 * Action
 * 
 *      20-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Defines action categories
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ConsoleBot
{
    public enum Action
    {
        None,
        GoForward,          // step action - move forward
        GoBack,             // step action - move backward
        TurnLeft,           // step action - turn left
        TurnRight,          // step action - turn right
        Logout,             // logout from virtual environments
        GoToward,           // going to specific point without stop
        bow,                // bow
        Fly,                // fly - toggle action
        Move,               // go forward without suspension. Stop needed
        Pause,              // stop movement
        Stop,               // stop current action
        EndofAction
    };

    public class ActionMap
    {
        public string m_ActionString;
        public Action m_Action;

        public ActionMap(string cmd, Action action)
        {
            m_ActionString = cmd;
            m_Action = action;
        }
    };
}
