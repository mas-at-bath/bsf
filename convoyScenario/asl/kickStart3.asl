//no beliefs

!start.

+!start : true  <- .print("kick starting scenario 3 - use waypoints");
						.wait(1000);
						.send(centralMember1, achieve, startUseWayPoints);
						.send(centralMember2, achieve, startUseWayPoints);
						.send(centralMember3, achieve, startUseWayPoints);
						.send(centralMember4, achieve, startUseWayPoints);
						.print("done starting things").
