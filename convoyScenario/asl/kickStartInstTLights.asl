//no beliefs

!start.

+!start : true  <- .print("kick starting bath traffic lights scenario");
						.wait(1000);
						.send(centralMember1, tell, insertTime(30000));
						.send(centralMember1, achieve, startUseInstTrafficLightFullSUMO);
						//.send(centralMember2, achieve, startUseInstTrafficLightFullSUMO);
						.print("done starting things").
