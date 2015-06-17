//no beliefs

!start.

+!start : true  <- .print("kick starting scenario 1 - use data push");
						.wait(2000);
						.send(centralMember1, achieve, startUsePush);
						.send(centralMember2, achieve, startUsePush);
						.send(centralMember3, achieve, startUsePush);
						.send(centralMember4, achieve, startUsePush);
						.print("done starting things").
