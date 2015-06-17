//no beliefs

!start.

+!start : true  <- .print("kick starting VSL scenario");
						.wait(1000);
						
						.send(centralMember1, tell, insertTime(35500));
						.send(centralMember2, tell, insertTime(40000));
						.send(centralMember3, tell, insertTime(44500));
						.send(centralMember4, tell, insertTime(49000));
						.send(centralMember5, tell, insertTime(53500));
						
						.send(centralMember1, achieve, startVSL);
						.send(centralMember2, achieve, startVSL);
						.send(centralMember3, achieve, startVSL);
						.send(centralMember4, achieve, startVSL);
						.send(centralMember5, achieve, startVSL);
						.print("done starting things").
