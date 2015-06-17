//no beliefs

!start.

+!start : true  <- .print("kick starting Crash scenario");
						.wait(1000);
						
						.send(centralMember1, tell, insertTime(34500));
						.send(centralMember2, tell, insertTime(37500));
						.send(centralMember3, tell, insertTime(43000));
						.send(centralMember4, tell, insertTime(43500));
						.send(centralMember5, tell, insertTime(46500));
						.send(centralMember6, tell, insertTime(48500));
						
						.send(centralMember1, achieve, startCrash);
						.send(centralMember2, achieve, startCrash);
						.send(centralMember3, achieve, startCrash);
						.send(centralMember4, achieve, startCrash);
						.send(centralMember5, achieve, startCrash);
						.send(centralMember6, achieve, startCrash);
						.print("done starting things").
