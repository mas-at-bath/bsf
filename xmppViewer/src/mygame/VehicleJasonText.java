    
package mygame;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;



  class VehicleJasonText
  {
      
    class BeliefPair
    {
        String agentName;
        String count;
    }
  
      public CopyOnWriteArrayList<BeliefPair> childAgents = new CopyOnWriteArrayList<BeliefPair>();
      public String vehicleName;
      
      public void addChildPair(String agName, String countVal)
      {
          BeliefPair newBBPair = new BeliefPair();
          newBBPair.agentName = agName;
          newBBPair.count = countVal;
          childAgents.add(newBBPair);
      }
      
      public String getChildData()
      {
          String allData = "";
          
           for(BeliefPair bPair : childAgents) 
                 {
                     allData = (allData+bPair.agentName + ":" + bPair.count + " ");
                 }
          return allData;
      }
      
  }