//any initial beliefs
terrain(desert).
convoyAgentName(convoyMember1).
driverAgentName(driver1).
collisionPredictionLoc(0,0,0).
debugOutput(false).
collisionMinTime(1.5).

!start.

/* Events */
// when battle starts

+aboutToCrash : true & driverAgentName(DAgentName) <- .print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX WOW told about to crash");
							.send(DAgentName, achieve, emergencyStop).
	
+!start : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) <- .print("started");
//changeCollisionVolume(0,0,0,5, 1, 5);

//.print("import map said: ",RA).

						//.create_agent(CAgentName, "convoyMember.asl");
					//	monitorAgent(CAgentName);	
						.create_agent(DAgentName, "asl/driver.asl");
						.my_name(N);
						.wait(1000);
						.send(DAgentName, tell, started(N));
						//.send(DAgentName, tell, desiredXZ(50,25));
						//.send(DAgentName, achieve, moveToKnownPosition);
						//.wait(8000);
						//.send(DAgentName, tell, desiredXZ(0,55));
						//.send(DAgentName, achieve, moveToKnownPosition);
					
						//.send(DAgentName, achieve, calcBathRoute);
						.send(DAgentName, achieve, loadRoute);
						
					//	monitorAgent(DAgentName);
						.wait(2000);
						.send(DAgentName, achieve, followWayPoints);
						
					//	.send(CAgentName, tell, myDriverName(DAgentName));
					//	.send(CAgentName, tell, started(N));
						.wait(2000);
						///.send(CAgentName, tell, vehicleBehind(convoyMember2));
						.wait(100).

						
+driverName(D) : debugOutput(true)  <- .print("have been told driver agent I just started is called ",D).
+convoyMemberName(C) : debugOutput(true)  <- .print("have been told convoy member agent I just started is called ",C).	

+test(X) : true <- .print("have been told test message").	

+dummy : true <- .print("have been told dummy message").	

//+obstacle(X,Y,Z) : true <- .print("obstacle belief added");
							//.send(driver, tell, obstacle(X,Y,Z)).

//+onScanObstacle(PosX, PosY, PosZ) : true <- .print("SCANNED OBSTACLE! At: ", PosX, " ", PosY, " ",PosZ);
															//obstacle(PosX,PosY,PosZ).
						
+onScanTank(Name,Side,X,Y,Z) : driverAgentName(DAgentName) 	<- //.print("adding vehicle ", Name);
																addKnownVehicle(Name, X, Y, Z).
															
//I think we need to define any tankcoders side environment updates here, and pass on that information. Alternatively, sub agents may be able to 'ask'									

//+info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : debugOutput(false) <- .print("pos update").
																									//turnToAngle(20);
																									//setSpeed(4).

+info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true //& collisionPredictionLoc(X1,Y1,Z1) 
																						& convoyAgentName(CName) 
																						& driverAgentName(DName) 
																						//check agents have registered back their names i.e. have started 
																						& driverName(NameD)
																						& convoyMemberName(NameC)
																						<- 
																						///& collisionMinTime(CTime) <- //.print("trying to update convoyMember");
																					//	checkForCollisions(PosX,PosY,PosZ,ChassiHeading,CTime);
																					
																						//.my_name(N);
																						//.print("position updated ",PosX, " ",PosY, " ",PosZ);
																						//io.fileWrite(PosX, PosZ, ChassiHeading, 1);																						
																						.send(CName,tell,convoyMemberTempInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo));
																						.send(DName, tell,updatedTempPositionHolder(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo)).
																				//	.send(DName, tell,entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo)).
																				

+info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true //& collisionPredictionLoc(X1,Y1,Z1) 
																						& driverAgentName(DName)  
																						& driverName(NameD)
																						//& collisionMinTime(CTime) 
																						<- //.print("trying to update convoyMember");
																						//checkForCollisions(PosX,PosY,PosZ,ChassiHeading,CTime);
																						.send(DName, tell,updatedTempPositionHolder(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo)).																						
																					//	.send(DName, tell,entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo)).																						
																						
//not really doing anything with this fall back plan yet
+info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true  <- //.print(PosX, ",", PosY,",", PosZ, Health,",", Speed,",", ChassiHeading, AuxInfo);
																					//	.print("XXXXXXXXXXX flying solo XXXXXXXXXXXX").
																					.wait(1).
																						 //io.fileWrite(PosX, PosZ, ChassiHeading).																					  
/* Plans */
	
//+!arrivedAtDestination : true <- .print("arrived");
	//			  setSpeed(0);
		//		  brake;
				  //.drop_desire(moveToXZ(X,Z)).
			//	  !!arrivedAtDestination.
			

+!chosenSpeed(0) : true & driverAgentName(DAgentName) <- .print("Central Agent told to stop ");
							//.send(DAgentName, tell, intendedSpeed(0));
							setSpeed(0);
							brake;
							wait(250).						
+!chosenSpeed(V) : true & driverAgentName(DAgentName) <- //.print("Central Agent told to achieve speed ", V);
							//.send(DAgentName, tell, intendedSpeed(V));
							setSpeed(V);
							wait(250).
							
+!requestTurnToAngle(A) : true <- //.print("Central Agent told to achieve angle ", A);
							turnToAngle(A);
							wait(250).

+!updateColPred(StartX,StartY,StartZ,FinX, FinY, FinZ, Heading) : true <- //.print("Changing collision volume spec: ", CentX, " ", CentY, " ",CentZ, " ", ExX, " ", ExY, " ", ExZ);
															//.print("Position volume: ", StartX, " ", StartY, " ", StartZ, " and end ", FinX, " ", FinY, " ", FinZ);
															// prob dont need this actually: .my_name(Name);	
															changeCollisionVolume(StartX,StartY,StartZ,FinX, FinY, FinZ).
															//.wait(250);.
															
+!updateJasonVisual(JX, JY, JZ) : true <- updateJasonVisualItem(JX, JY, JZ).
+!updateWayPointVisual(WayPoint, XWaypoint, ZWaypoint) : true <- //.print("updating waypoint visual to ",WayPoint);
											updateJasonWayPoint(WayPoint, XWaypoint, ZWaypoint).

//+!chosenSpeed(10)[source(driver)] <- .print("WOAH").							
							
+!loopBrake : true <- brake.
						//!!loopBrake.

+!loop : true <- .print("looping");
				!!loop.



