distanceRemaining(0, 27.99999999, arrivedAtDestination).
//distanceRemaining(2, 5.99999999, slowcruise).
distanceRemaining(28, 10000, cruise).
vehicleWidth(3).
vehicleHeight(1).
inEmergencyStop(false).
inStopped(true).
debugOutput(false).
debugWayPoints(true). 
//specify upper, lower and standard speed values here, currently these are suitable for m25 - i.e. 70mph 
upperSpeed(35).
standardSpeed(31).
lowerSpeed(27).
speedInterval(0.2).
collisionMinTime(3).
currentWayPoint(-1).
//driver agent is now updating its own belief value as what speed is, based on what speed its asked for.
//ideally, this would be measured, and could maybe have a java function to do that. but, it seems odd to ask the central agent
//what the speed is, as well as adding additional messages. if this is used for prediction, then i see no harm in running from the value
//which has been used to ask the central agent to achieve - i.e. human drivers just use the value indicated by the speedo, we do 
//no physical measuring..
currentRequestedSpeed(0).

//+wayPoint(X,Y,T) : .count(wayPoint(_,_,_),NWays)<- .print("told ", NWays, " waypoints").

+usingVehicleSim(sumo) : true <- .print("driver agent has been told using SUMO traffic sim").
					
+desiredXZ(CurrentX,CurrentZ) : started(Owner) <- //.print("Told desired XZ of ", CurrentX, " ",CurrentZ);
											.send(Owner,achieve, updateJasonVisual(CurrentX, 0, CurrentZ)).
											///		.count(desiredXZ(_,_),N);
												///	.print("XXXXX total beliefs on desired XZ: ",N).

+startedTime(T) : debugOutput(true) <- .print("been told started at ",T).

//+intendedSpeed(ValSpeed) : debugOutput(true) <- .print("told by central agent that am intending to travel at ", ValSpeed).

+currentWayPoint(C) : started(Owner) & debugWayPoints(true) & desiredXZ(Xpos,Zpos) <- //.print("sending waypoint ",C, " at ",Xpos," ",Zpos);
									.send(Owner,achieve, updateWayPointVisual(C,Xpos,Zpos)).					

+obstacleSeen(ObstX,ObstY,ObstZ) : debugOutput(true)  <- .print("TOLD ABOUT OBSTACLE").

+arrived(Val) : true <- .print("Added arrived belief,  to ",Val).

///+obstacleSeen(ObsX,ObsY,ObsZ) : true & started(Owner) 
									//& intendedSpeed(SpeedVal) 
									//& entityUpdate(PosX, PosY, PosZ,Health,MainGulBullets, ChassiHeading,AuxInfo)
									///& geom.evaluate(6,2,5, 0,257,2000,ObsX,ObsY,ObsZ,CollisionCheck) <- .print("I've been told about an obstacle, and value for will i collide is ", CollisionCheck).

//using 2 as seconds to look ahead at the moment
/*+updatedTempPositionHolder(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : entityUpdate(PX, PY, PZ, H, M, Heading, MGHeading) 
																										& PX = PosX
																										& PY = PosY
																										& PZ = PosZ
																									<- //.print("not moved");
																										+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
																									//	.abolish(entityUpdate(PX, PY, PZ, H, M, Heading, MGHeading));
																										//+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
																										.abolish(updatedTempPositionHolder(_,_,_,_,_,_,_)).*/

@upBel[atomic]																										
+updatedTempPositionHolder(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true 
		<- .abolish(entityUpdate(_,_,_,_,_,_,_));
			+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
			.abolish(currentRequestedSpeed(_));
			+currentRequestedSpeed(Speed);
			.abolish(updatedTempPositionHolder(_,_,_,_,_,_,_)).
																										
@upXZBel[atomic]																										
+updatedTempMoveXZ(X, Z) : true <- //.print("trying to update belief of desiredXZ via updatedTempMoveXZ");	
										.abolish(desiredXZ(_,_));
										+desiredXZ(X, Z);
										//+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
										.abolish(updatedTempMoveXZ(_,_)).

+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : currentRequestedSpeed(SpeedVal) 
		& started(Owner) 
		& collisionMinTime(CTime)
		& geom.convert(SpeedVal,CTime,ChassiHeading, PosX,PosY,PosZ,XFIN,YFIN,ZFIN) 	
		<- .send(Owner,achieve, updateColPred(PosX,PosY,PosZ,XFIN,YFIN,ZFIN,ChassiHeading)).
									
									
// agent startup
+started(Owner) : true <- .print("driver agent has started by ", Owner);
							.my_name(N);
							.send(Owner, tell, driverName(N)).
																											  
/* Plans */
+!emergencyStop : true & started(Owner) <- .print("E-STOP");
					.send(Owner, achieve, chosenSpeed(0));
					.abolish(currentRequestedSpeed(_));
					+currentRequestedSpeed(0);
					.send(Owner, unachieve, requestTurnToAngle(_));
					+arrived(yes);
					+inEmergencyStop(true);
					+isStopped(true);
					.abolish(desiredXZ(_,_));
				    .drop_desire(moveToKnownPosition);
					.drop_event(moveToKnownPosition).
					//.send(Owner, achieve, loopBrake);

					
+!arrivedAtDestination : true & started(Owner) & .count(wayPoint(_,_,_),NWaysT) & NWaysT > 0
					<- //.print("XXXXXX arrived at this waypoint NEXT PLEASE XXXXXXXX");
	
					//COULD TIDY THIS UP I GUESS AND ONLY ABOLISH THE CURRENT WAYPOINT OF XY, 
					//BUT THEN I THINK SHOULD ONLY PURSUE ONE MOVETOXZ() AT ANY POINT ANYWAY
					.abolish(desiredXZ(_,_));
					!followWayPoints.
					
+!arrivedAtDestination : true & started(Owner) <- 
					.send(Owner, achieve, chosenSpeed(0));
					.abolish(currentRequestedSpeed(_));
					+currentRequestedSpeed(0);
					+arrived(yes);
					+isStopped(true);
					.send(Owner, unachieve, requestTurnToAngle(_));
					.drop_desire(moveToKnownPosition);
					.abolish(desiredXZ(_,_));
					.drop_event(moveToKnownPosition).

//+!swerve : true <- .print("swerving"). //recalculate route if were on one?

+!applyBrakes : true  & started(Owner) <- //.print("applying brakes");
							.send(Owner, achieve, sendMessage(braking));
							.send(Owner, achieve, performBrake).

+!standardSpeed : true <- //.print("maintain current speed").
							.wait(1).

								
+!setSpeed(NewValue) : currentRequestedSpeed(VOld) & started(Owner)  & standardSpeed(V) <- .print("setting new target speed to ", NewValue);
							+currentRequestedSpeed(NewValue);
							.abolish(currentRequestedSpeed(VOld));
							+standardSpeed(NewValue);
							.abolish(standardSpeed(V));
							.send(Owner, achieve, chosenSpeed(NewValue)).
							
+!setSpeed(NewValue) : started(Owner)  & standardSpeed(V) <- .print("setting new target speed to ", NewValue, " but no currentRequestedSpeed..");
							+currentRequestedSpeed(NewValue);
							+standardSpeed(NewValue);
							.abolish(standardSpeed(V));
							.send(Owner, achieve, chosenSpeed(NewValue)).
						
+!setSpeed(NewValue) : started(Owner) <- .print("main set speed request didnt work for some reason, fudging things here now");
								.abolish(standardSpeed(_));
								.abolish(currentRequestedSpeed(_));
								+currentRequestedSpeed(NewValue);
								+standardSpeed(NewValue);
								.send(Owner, achieve, chosenSpeed(NewValue)).
								
+!setSpeed(NewValue) : true <- .print("main set speed request didnt work for some reason, no fudge possible").
							
+!speedUp : true & standardSpeed(V) & lowerSpeed(L) & upperSpeed(U) & speedInterval(I) & V2=(V+I) & (V2 <= U) & started(Owner)  <- //.print("trying to speed up from ", V, " to ",V2);
						+standardSpeed(V2);
						.send(Owner, achieve, chosenSpeed(V2));
						.send(Owner, achieve, sendMessage(accelerating));
						.abolish(currentRequestedSpeed(_));
						+currentRequestedSpeed(V2);
						.abolish(standardSpeed(V)).
+!speedUp : true & standardSpeed(V)  & lowerSpeed(L) & upperSpeed(U) & speedInterval(I) & V2=(V+I) & (V2 > U) & started(Owner)  <- //.print("trying to speed up from ", V, " but already had max speed");
						.wait(1).
						
+!slowDown : true & standardSpeed(V)  & lowerSpeed(L) & upperSpeed(U) & speedInterval(I) & V2=(V-I) & (V2 >= L)  & started(Owner)  <- //.print("trying to speed up from ", V);
						+standardSpeed(V2);
						.send(Owner, achieve, chosenSpeed(V2));
						.send(Owner, achieve, sendMessage(braking));
						.abolish(currentRequestedSpeed(_));
						+currentRequestedSpeed(V2);
						.abolish(standardSpeed(V)).
+!slowDown : true & started(Owner) & standardSpeed(V)  & lowerSpeed(L) & upperSpeed(U) & speedInterval(I) & V2=(V-I) & (V2 < L) & started(Owner)  <- //.print("trying to speed up from ", V);
						//NOT SURE PERFORM BRAKE DOES ANYTHING AT MOMENT
						//.send(Owner, achieve, performBrake);
						.send(Owner, achieve, sendMessage(braking));
						.send(Owner, achieve, chosenSpeed(1));
						.abolish(currentRequestedSpeed(_));
						+currentRequestedSpeed(1);
						.wait(1).

+!lostMyLeader : true & started(Owner) <- 
					//at the moment this is a duplicate of the stop behaviour, but should be more exotic really..
					.print("lostMyLeader, so I'm stopping!");
					.send(Owner, achieve, chosenSpeed(0));
					.abolish(currentRequestedSpeed(_));
					+currentRequestedSpeed(0);
					+arrived(yes);
					+isStopped(true);
					.send(Owner, unachieve, requestTurnToAngle(_));
					.drop_desire(moveToKnownPosition);
					.abolish(desiredXZ(_,_));
					.drop_event(moveToKnownPosition).

//+!updateVolume : true <- .print("called update volume.. strange");
//							.send(Owner, achieve, chosenSpeed(2)).
					
+!cruise : true & started(Owner) & standardSpeed(V) <- //.print("cruising default at ", V);     
					//intendedSpeed(6);
					.send(Owner, achieve, chosenSpeed(V));
					.abolish(currentRequestedSpeed(_));
					+currentRequestedSpeed(V);.
					
+!followWayPoints : .count(wayPoint(_,_,_),NWays) & NWays > 0 & currentWayPoint(-1)
							& wayPoint(1,NewX,NewY) <- .print("asked to follow waypoints for first time");
							//.count(wayPoint(_,_),NWays);
							.abolish(currentWayPoint(-1));
							//.print("and I think there are this many: ", NWays);
							//.print("should move to:", NewX, " ", NewY);
							+desiredXZ(NewX,NewY);
							+currentWayPoint(2);
							!moveToKnownPosition.
							//!moveToXZ(NewX,NewY).
							//!!followWayPoints.
							
+!followWayPoints : .count(wayPoint(_,_,_),NWays) & NWays > 0 & currentWayPoint(CurrentWay)
							& wayPoint(CurrentWay,NewX,NewY) <- ///.print("asked to follow waypoints from second condition iterator");
							//.count(wayPoint(_,_),NWays);
							.abolish(currentWayPoint(CurrentWay));
							//.print("and I think there are this many: ", NWays);
							//.print("should move to:", NewX, " ", NewY);
							+desiredXZ(NewX,NewY);
							+currentWayPoint(CurrentWay+1).
							//!!followWayPoints.
							
+!followWayPoints : .count(wayPoint(_,_,_),NWays) & NWays > 0 & currentWayPoint(CurrentWay) & CurrentWay >= NWays
							& started(Owner)
							<- .print("think have done all the way points, clear everything up");
							.print("abolishing waypoint beliefs");
							.abolish(wayPoint(_,_,_));
							.wait(1000);
							.count(wayPoint(_,_,_),LeftWays);
							.print(LeftWays," waypoints remaining");
							!arrivedAtDestination;
							+currentWayPoint(-1);
							//.succeed_goal(moveToKnownPosition);
							.drop_desire(moveToKnownPosition);
							+arrivedAtDestination;
							.drop_desire(followWayPoints);
						//	.send(Owner, achieve, chosenSpeed(0));
							.print("should have told owner to hit zero speed").
							
+!followWayPoints : .count(wayPoint(_,_,_),NWays) & NWays = 0 <- .print("asked to follow waypoints BUT");
							//.count(wayPoint(_,_),NWays);
							.print("I think there are no waypoints");
							.succeed_goal(followWayPoints);
							.drop_desire(followWayPoints).
							

+!clearXZbelief : started(Owner) <- .print("would clear belief base of XZ positions");
									.wait(10).
									//.abolish(desiredXZ(_,_)).
			
			//i wonder if here we could take the agent name request, and tell it to drop that desire?
+!moveToKnownPosition : arrived(yes) <- .print("called the alternative moveToKnownPosition to drop desire, so something is still asking me to moveToKnownPosition");
								.drop_desire(moveToKnownPosition);
								.drop_event(moveToKnownPosition);
								.abolish(desiredXZ(_,_));
								///?.abolish(arrived(_));
								//possibly the first time have been told that arrived at destination, so take a breather, 1000ms seems reasonable..
								.wait(250).

//move based on SUMO integration								
+!moveToKnownPosition : entityUpdate(X,_,Z,_,_,Heading,_) 
					& usingVehicleSim(sumo)
					& desiredXZ(X1,Z1)
					& started(Owner)
					& DX=(X-X1) & DZ=(Z-Z1)
				    &  test.test1(X,X1,Z,Z1,Heading,R2)
					& D1 = math.abs(DX)+math.abs(DZ)
					& distanceRemaining(MIN,MAX,GOAL) & MIN <= D1 & MAX >= D1 
								<- 
							.wait(250);
							.send(Owner,achieve,requestMoveTo(X1,Z1));
							!GOAL;
							!!moveToKnownPosition. 

								
+!moveToKnownPosition : entityUpdate(X,_,Z,_,_,Heading,_) 
					& desiredXZ(X1,Z1)
					& started(Owner)
					& DX=(X-X1) & DZ=(Z-Z1)
				    &  test.test1(X,X1,Z,Z1,Heading,R2)
					& D1 = math.abs(DX)+math.abs(DZ)
					//AND CHECK NOT EXIST BELIEF WE'VE ARRIVED?
					& distanceRemaining(MIN,MAX,GOAL) & MIN <= D1 & MAX >= D1 
								<- 
						//	.print("total distance of move ",D1," angle ", R2," plan would be ",GOAL, " min ", MIN, " max ", MAX);
						//	.print("sending message to: ", Owner);
							.wait(250);
							.send(Owner,achieve,requestTurnToAngle(R2));
							!GOAL;
							!!moveToKnownPosition. 

+!moveToKnownPosition :  desiredXZ(X1,Z1) & entityUpdate(X,_,Z,_,_,Heading,_) 
					& started(Owner) 					& DX=(X-X1) & DZ=(Z-Z1)
									    &  test.test1(X,X1,Z,Z1,Heading,R2)
					& D1 = math.abs(DX)+math.abs(DZ) & distanceRemaining(MIN,MAX,GOAL)
								<- .print("move to known position, know my own position at the moment, but still have issue, maybe with distance outside of range");
									.print("d1 is ", D1);
							.wait(250);
							!!moveToKnownPosition. 
							
+!moveToKnownPosition :  desiredXZ(X1,Z1)
					& started(Owner)
								<- .print("move to known position, but dont know my own position at the moment, but still have desired XZ");
							.wait(250);
							!!moveToKnownPosition. 
							
//some conditions weren't met.. i wonder which..							
+!moveToKnownPosition : true <-
							.print("XXX Very Weird, still tried to move to known position but some dependency missing..!");
							.wait(250).
	
+!calcStandardRoute : true <- router.calculateRoute(51.377831,-2.360473,51.381086,-2.366266,ReturnValue).

+!calcBathRoute : true <- router.calculateBathRoute(ReturnedValue).

+!loadBathRoute : true <- router.loadBathRoute(ReturnedValue).

+!loadM25Route : true <- router.loadM25Route(ReturnedValue).

+!clearArrivedState : true <- //.print("clearing arrived beliefs");
							.abolish(arrived(_)).								
							

							
	



