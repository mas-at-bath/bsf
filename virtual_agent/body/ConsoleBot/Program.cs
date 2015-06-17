/*
 * Program.cs
 * 
 *      08-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Program main of the ConsoleBot
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ConsoleBot
{
    public class CommandLine
    {
        // add later
    }

    class FirstBot
    {
        static void Main(string[] args)
        {
            // Init instance
            ConsoleBotInstance instance = ConsoleBotInstance.GloblaInstance;
            
            // Set login parameter from user input
            if (instance.NetManager.SetLoginParam(args) != true)
            {
                Console.WriteLine("Fail to set login info. Try again");
                return;
            }
            else
            {
                instance.InitailiseBSF(instance.NetManager.Username, instance.NetManager.Password);
            }

            // Run main program in Bot
            instance.Run();
        }
    }
}