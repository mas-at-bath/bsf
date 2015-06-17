//no beliefs

!start.

+!start : true  <- .print("kick starting institution scenario - using m25 waypoints");
						.wait(1000);
						.send(centralMember1, tell, insertTime(8000));
						.send(centralMember2, tell, insertTime(14000));
						
						.send(centralMember1, achieve, startUseInstFlash);
						.send(centralMember2, achieve, startUseInstFlash);
						.print("done starting things").
