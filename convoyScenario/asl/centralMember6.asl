//any initial beliefs
terrain(desert).
convoyAgentName(convoyMember6).
driverAgentName(driver6).
collisionPredictionLoc(1,259,2000).
debugOutput(false).
//collisionMinTime(2).
collisionDistance(0, 24.99999999, brakeHard).
collisionDistance(25, 200, flashLights).
targetSpeed(38).
//NonVSL:insertTime(11000).
//for vsl:
//insertTime(57000).
//for vsl:
//insertTime(48500).

speedModifier(medium, 20).
speedModifier(slow,5).
speedModifier(mediumSlow, 12). 

//!startUsePush.
//!startUsePull.
//!startUseWayPoints.
//!startJoinConvoyBehind(convoyMember4).

/* Events */
+merge(Vehicle, Distance) : Distance > 100 <- .print("letting ", Vehicle, " which is ",Distance,"m ahead merge infront of me");
												!flashLights(Name);
												.send(Vehicle, tell, changeLane).
												
+merge(Vehicle, Distance) : Distance <= 100 <- .print("not letting ", Vehicle, " which is ",Distance,"m ahead merge, too close!").

+merge(Vehicle) : true <- .print("asked to let ", Vehicle, " merge infront of me but I don't know distance").

+slowDown(Value) : speedModifier(Value, NewSpeed) & driverAgentName(DAgentName) <- .print("asked to slowdown to ", Value, " which I translate to ", NewSpeed);
							.send(DAgentName, achieve, setSpeed(NewSpeed));
							.wait(5000);
							.send(DAgentName, achieve, setSpeed(-1)).
							
+time(CurrentSimTime) : insertTime(T) & targetSpeed(V) & driverAgentName(DAgentName) & scenario(vsl) & CurrentSimTime = T <- .print("can set agent behaviour now");
							.wait(500); 
							setLaneChange(0);					
							.wait(500);
							.print("sending speed message to ", V, " for VSL scenario");
							.send(DAgentName, achieve, setSpeed(V)).
							
+time(CurrentSimTime) : insertTime(T) & targetSpeed(V) & driverAgentName(DAgentName) & scenario(crash) & CurrentSimTime = T <- .print("can set agent behaviour now");
							.wait(500); //just give sim second to insert the vehicle..
							//setLaneChange(17);
							setLaneChange(0);
							.wait(500).//;
							//setAutonomy(0).	

+time(CurrentSimTime) : insertTime(T)  & CurrentSimTime = T <- .print("can set agent behaviour now");
							.wait(500); //just give sim second to insert the vehicle..
							//setLaneChange(17);
							setLaneChange(0);
							.wait(500);
							setAutonomy(0).

@upAOIVehBel[atomic]
+aoiVehicleDetection(Vehiclename, LocX, LocY) : true <- 
														.abolish(detectedVehicles(Vehiclename,_,_));
														+detectedVehicles(Vehiclename,LocX,LocY).
																										
+detectedVehicles(Name,X,Y) : true <- //.print("detected vehicle ", Name, " at ", X, " ", Y);
										checkCollisionVolume(Name,X,Y).
										
//distance of 45 seems to work pretty well, 35 will cause a crash
+detectionInCollisionZone(Name, Distance) : Distance <=45  <- .print("vehicle ",Name," detected too close in collision zone!! distance is ", Distance);
													.abolish(collisionVolClear);
													!brakeHard(Name).

+detectionInCollisionZone(Name, Distance) : Distance > 45 & Distance <=65 <- .print("vehicle ",Name," detected near in collision zone!! distance is ", Distance);
													.abolish(collisionVolClear);
													!flashLights(Name).		
													
+collisionVolClear : true <- .print("i think collision zone is clear of emergency situations now");
								//lets just give it a second anyway
								.wait(3000);
								!resumeSpeed.
										
+crashed : true <- .print("Oh no! Something went pretty wrong, I've been told I've crashed").

+changeLane : true <- .print("Percept added, I've been told to change lane!");
						quickLaneChange.

+aboutToCrash : true & driverAgentName(DAgentName) <- .print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX WOW told about to crash");
							.send(DAgentName, achieve, emergencyStop).
						
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
																					//	.print("position updated ",PosX, " ",PosY, " ",PosZ);
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
+info(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true  <- .print("flying solo from: ", PosX, ",", PosY,",", PosZ, Health,",", Speed,",", ChassiHeading, AuxInfo);
																						.print("XXXXXXXXXXX flying solo XXXXXXXXXXXX").
																						 //io.fileWrite(PosX, PosZ, ChassiHeading).
																						 
+accelerating : true <- .print("told accelerating, should probably clear this belief now?").

+braking : true <- .print("told braking, should probably clear this belief now?").
																						 
/* Plans */
	
//+!arrivedAtDestination : true <- .print("arrived");
	//			  setSpeed(0);
		//		  brake;
				  //.drop_desire(moveToXZ(X,Z)).
			//	  !!arrivedAtDestination.
+!resumeSpeed : driverAgentName(DAgentName) & targetSpeed(V) <- .print("resuming speed of ", V);
								.send(DAgentName, achieve, setSpeed(V)).		
			
+!brakeHard(Target)  : driverAgentName(DAgentName) <- .print("Called plan to brake hard!!");
								hadToBrakeBecause(Target);
								.send(DAgentName, achieve, setSpeed(10)).

+!flashLights(Target) : true <- .print("Called plan to flash lights!!");
								setLights(on);
								testInstUpdate(flashLights(centralMember1));
								.wait(2000);
								setLights(off).

+!chosenSpeed(0) : true & driverAgentName(DAgentName) & convoyAgentName(CAgentName) <- .print("Central Agent told to stop ");
							//.send(DAgentName, tell, intendedSpeed(0));
							.send(CAgentName, tell, amMoving(false));
							setSpeed(0);
							brake;
							wait(250).						
+!chosenSpeed(V) : true & driverAgentName(DAgentName) & convoyAgentName(CAgentName) <- //.print("Central Agent told to achieve speed ", V);
							//.send(DAgentName, tell, intendedSpeed(V));
							.send(CAgentName, tell, amMoving(true));
							setSpeed(V);
							wait(250).
							
+!requestTurnToAngle(A) : true <- //.print("Central Agent told to achieve angle ", A);
							turnToAngle(A).

+!requestMoveTo(X, Z) : true <- //.print("requesting a moveTo ", X, " ", Z);
							moveTo(X,Z).
							
+!updateColPred(StartX,StartY,StartZ,FinX, FinY, FinZ, Heading) : true <- //.print("Changing collision volume spec: ", CentX, " ", CentY, " ",CentZ, " ", ExX, " ", ExY, " ", ExZ);
															//.print("Position volume: ", StartX, " ", StartY, " ", StartZ, " and end ", FinX, " ", FinY, " ", FinZ);
															// prob dont need this actually: .my_name(Name);	
															changeCollisionVolume(StartX,StartY,StartZ,FinX, FinY, FinZ).
															//.wait(250);.
															
+!updateJasonVisual(JX, JY, JZ) : true <- updateJasonVisualItem(JX, JY, JZ).
+!updateWayPointVisual(WayPoint, XWaypoint, ZWaypoint) : true <- //.print("updating waypoint visual to ",WayPoint);
											updateJasonWayPoint(WayPoint, XWaypoint, ZWaypoint).
											
+!performBrake : true <- //.print("agent called brake method, but maybe not implemented");
						 brake.
						 
+!loopBrake : true <- brake.
						//!!loopBrake.

+!performBrake : true <- //.print("agent called brake method, but maybe not implemented");
						 brake.
						
+!loop : true <- .print("looping");
				!!loop.

+!sendMessage(Message) : true <- //.print("sending message to BSF ", Message);
									.my_name(N);
								  sendToBSF(N,Message).

+!startUsePush : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) <- .print("started");
						.create_agent(CAgentName, "asl/convoyMember.asl");
						.create_agent(DAgentName, "asl/driver.asl");
						.my_name(N);
						.wait(3000);
						.send(DAgentName, tell, started(N));
						.send(CAgentName, tell, myDriverName(DAgentName));
						.send(CAgentName, tell, noUseWayPoints);
						.send(CAgentName, tell, started(N));
						.wait(8000).						
							
+!startUseWayPoints : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) <- .print("started");
						.create_agent(CAgentName, "asl/convoyMember.asl");
						.create_agent(DAgentName, "asl/driver.asl");
						.my_name(N);
						.wait(3000);
						.send(DAgentName, tell, started(N));
						.send(DAgentName, tell, useWayPoints);
						.wait(7000);
						.send(CAgentName, tell, myDriverName(DAgentName));
						.send(CAgentName, tell, started(N));
						.wait(500).
						
+!startUsePull : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) <- .print("started data pull scenario");
						.create_agent(CAgentName, "asl/convoyMember.asl");
						monitorAgent(CAgentName);	
						.create_agent(DAgentName, "asl/driver.asl");
						.my_name(N);
						.wait(1000);
						.send(DAgentName, tell, started(N));
						monitorAgent(DAgentName);
						.wait(9000);
						.send(CAgentName, tell, useDataPull);
						.send(CAgentName, tell, vehicleAhead(convoyMember3));
						.send(CAgentName, tell, myDriverName(DAgentName));
						.send(CAgentName, tell, started(N));
						.wait(4000);
						.wait(500).
						
+!startUseWayPointsM25 : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) <- .print("started");
						.my_name(N);
						.wait(7000);
						.print("adding SUMO vehicle ", N);
						addSUMOVehicle(N);
							
						.create_agent(CAgentName, "asl/convoyMember.asl");
						.create_agent(DAgentName, "asl/driver.asl");
						.my_name(N);
						.wait(1000);
						.send(DAgentName, tell, started(N));
						.wait(2000);
						//.send(CAgentName, tell, useDataPull);
						.send(CAgentName, tell, vehicleAhead(VehicleToFollow));
						.send(CAgentName, tell, myDriverName(DAgentName));
						.send(CAgentName, tell, started(N));
						.wait(5000);
					///	testInstUpdate(join_at_back(centralMember2));		
					///	.send(CAgentName, tell, vehicleBehind(convoyMember3));
						.wait(3000);
						.send(CAgentName, achieve, passBackWaypoints);
						.wait(500).
						
+!startUseInstFlash : convoyAgentName(CAgentName)  & driverAgentName(DAgentName) & targetSpeed(V) & insertTime(T)
						<- .print("started");
						.my_name(N);
						
						.print("adding SUMO vehicle ", N);
						addSUMOVehicle(N, m25, 2, 30, T);	
						.wait(5000);
						.create_agent(CAgentName, "asl/convoyMember.asl");
						.create_agent(DAgentName, "asl/driver.asl");
						
						.send(DAgentName, tell, usingVehicleSim(sumo));
						.send(DAgentName, tell, started(N));
						
						//need delay so that it reachs parking state from dummy route, until SUMO allows deletion of stops from route
						.wait(2000);
						.print("sending speed message to ", V);
						.send(DAgentName, achieve, setSpeed(V));

						.wait(500).
						
+!startVSL : true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) & insertTime(VT) <- .print("Started agent for VSL scenario!");
						+scenario(vsl);
						.my_name(N);
						.print("adding SUMO vehicle ", N);
						addSUMOVehicle(N, m25, 1, 31, VT);	
						
						.create_agent(CAgentName, "asl/convoyMember.asl");	
						.create_agent(DAgentName, "asl/driver.asl");
						.send(DAgentName, tell, usingVehicleSim(sumo));
						.send(DAgentName, tell, started(N)).
						
+!startCrash: true & convoyAgentName(CAgentName)  & driverAgentName(DAgentName) & insertTime(VT) <- .print("Started agent for Crash scenario!");
						+scenario(crash);											
						.my_name(N);
						.print("adding SUMO vehicle ", N);
						addSUMOVehicle(N, m25, 2, 31, VT);						
						.create_agent(CAgentName, "asl/convoyMember.asl");	
						.create_agent(DAgentName, "asl/driver.asl");
						.send(DAgentName, tell, usingVehicleSim(sumo));
						.send(DAgentName, tell, started(N)).
