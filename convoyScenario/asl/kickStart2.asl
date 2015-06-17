//no beliefs

!start.

+!start : true  <- .print("kick starting scenario 2 - use data pull");
						.wait(2000);
						.send(centralMember1, achieve, startUsePull);
						.send(centralMember2, achieve, startUsePull);
						.send(centralMember3, achieve, startUsePull);
						.send(centralMember4, achieve, startUsePull);
						.print("done starting things").
