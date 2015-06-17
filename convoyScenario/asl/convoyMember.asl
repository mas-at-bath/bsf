/* Events */
//ok lets assume, that on creation, every convoyMember assumes it is at the front of the convoy, until its told otherwise.
position(1).
amMoving(false).
convoyPassed(false).
isWaypointScenario(true).
distanceToCarInfront(-10000000000, -0.00000001, odd).
distanceToCarInfront(0, 10.9999999999, applyBrakes).
distanceToCarInfront(11, 34.9999999999, slowDown).
distanceToCarInfront(35, 45.9999999999, standardSpeed).
distanceToCarInfront(46, 200.999999999, speedUp).
distanceToCarInfront(201, 1000, lostMyLeader).

//+wayPoint(X,Y,T) : true <- .print("told some waypoints").

+started(Owner) : useDataPull <- .print("convoyMember started using data pull strategy");
							.my_name(N);
							.send(Owner, tell, convoyMemberName(N));
							!convoyMgmtPlan.

+started(Owner) : true <- .print("convoyMember started by ", Owner);
							.my_name(N);
							.print("convoyMember agent instance called ", N);
							.send(Owner, tell, convoyMemberName(N)).
							
+noUseWayPoints : true <- .print("using standard convoy push method").	
+useWayPoints : true <- .print("using standard convoy waypoint method").	

+vehicleBehind(VehBehind) : wayPoint(X,Y,Z) & .count(wayPoint(_,_,_),NWays) <- .print("told vehicle behind me in convoy is ", VehBehind, " and I have been told ",NWays," waypoints").

+vehicleBehind(VehBehind) : true <- .print("no waypoints, told vehicle behind me in convoy is ", VehBehind).

+vehicleAhead(VehAhead) : true <- .print("told vehicle ahead of me in convoy is ", VehAhead).
+myDriverName(DriverName) : true <- .print("have been told driver agent in this car is ", DriverName).

+amMoving(true) : true <- .print("convoy agent told moving");
									.abolish(amMoving(false)).
									
+amMoving(false) : true <- .print("convoy agent told NOT moving");
									.abolish(amMoving(true)).
									
+convoyPassed(true) : true <- .print("changing convoy passed status");
									.abolish(convoyPassed(false)).								
+convoyPassed(false) : true <- .print("changing convoy passed status");
									.abolish(convoyPassed(true)).									

//+entityUpdate(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true <- .print("I've received position update").

//dropped this requirement & started(Owner)

@upCMBel[atomic]
+convoyMemberTempInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true <-
										.abolish(convoyMemberInfo(_,_,_,_,_,_,_));
										+convoyMemberInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
										.abolish(convoyMemberTempInfo(_,_,_,_,_,_,_)).


//have commented this out as if theres no convoyFollower then it will fail - should probably replace with a generic convoy
//member with the linked list approach
+convoyMemberInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true & started(Owner) & vehicleBehind(VehBehind) <- //.print("trying to update ", VehBehind);
															.send(VehBehind, tell, vehAheadTempInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo)).
																			
@upCMVAheadBel[atomic]
+vehAheadTempInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo) : true <-
										.abolish(vehAheadInfo(_,_,_,_,_,_,_));
										+vehAheadInfo(PosX, PosY, PosZ, Health, Speed, ChassiHeading, AuxInfo);
										.abolish(vehAheadTempInfo(_,_,_,_,_,_,_)).														
															
+vehAheadInfo(DPosX, DPosY, DPosZ, Health, Speed, ChassiHeading, AuxInfo) : true & useWayPoints & amMoving(false) & myDriverName(DriverName) & convoyMemberInfo(MyX,MyY,MyZ,_,_,_,_)
										& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
										& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
										& distanceToCarInfront(MIN,MAX,GOAL) & MIN <= DTotal & MAX >= DTotal
										<- 	//.print("I've been told by driver that we've stopped, but I need to: ", GOAL);
										.send(DriverName, achieve, clearArrivedState);
										.send(DriverName, achieve, GOAL).
																										
+vehAheadInfo(DPosX, DPosY, DPosZ, Health, Speed, ChassiHeading, AuxInfo) : true & useWayPoints & amMoving(true) & myDriverName(DriverName) & convoyMemberInfo(MyX,MyY,MyZ,_,_,_,_)
										& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
										& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
										& distanceToCarInfront(MIN,MAX,GOAL) & MIN <= DTotal & MAX >= DTotal
										<- 	//.print("I need to: ", GOAL);
										.send(DriverName, achieve, GOAL).
										
+vehAheadInfo(DPosX, DPosY, DPosZ, Health, Speed, ChassiHeading, AuxInfo) : true & noUseWayPoints & amMoving(false) & myDriverName(DriverName) & convoyMemberInfo(MyX,MyY,MyZ,_,_,_,_)
										& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
										& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
										& distanceToCarInfront(MIN,MAX,GOAL) & MIN <= DTotal & MAX >= DTotal
										<- 	//.print("I've been told by driver that we've stopped, but I need to: ", GOAL);
										.send(DriverName, achieve, clearArrivedState);
										.send(DriverName, achieve, GOAL);
										.send(DriverName, tell, updatedTempMoveXZ(DPosX,DPosZ));
										.send(DriverName, achieve, moveToKnownPosition).
																										
+vehAheadInfo(DPosX, DPosY, DPosZ, Health, Speed, ChassiHeading, AuxInfo) : true & noUseWayPoints & amMoving(true) & myDriverName(DriverName) & convoyMemberInfo(MyX,MyY,MyZ,_,_,_,_)
										& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
										& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
										& distanceToCarInfront(MIN,MAX,GOAL) & MIN <= DTotal & MAX >= DTotal
										<- 	//.print("I need to: ", GOAL);
										.send(DriverName, achieve, GOAL);
										.send(DriverName, tell, updatedTempMoveXZ(DPosX,DPosZ)).										
																																			
+vehAheadInfo(DPosX, DPosY, DPosZ, Health, Speed, ChassiHeading, AuxInfo) : true 
										<- .print("convoyMember vehAheadInfo not enough info").
										
+!joinBack : vehicleAhead(VehicleAhead) & myDriverName(DriverName) & convoyPassed(false) <-
								.print("waiting to join back of convoy still..");
								.send(VehicleAhead, askOne, convoyMemberInfo(X,Y,Z,_,_,_,_),convoyMemberInfo(X1,Y1,Z1,A1,B1,C1,D1));
								!checkIfConvoyPassed(X1,Y1,Z1);
								!joinBack.
								
+!joinBack :  vehicleAhead(VehicleAhead) & myDriverName(DriverName) & convoyPassed(true) <-
								.print("WOO OK to join back of convoy");
								.send(VehicleAhead, askOne, convoyMemberInfo(X,Y,Z,_,_,_,_),convoyMemberInfo(X1,Y1,Z1,A1,B1,C1,D1));
								!convoyMgmtPlan.							
										
+!joinBack : true <- .print("AGH something wrong with joinBack plan").								
								
+!convoyMgmtPlan : true & vehicleAhead(VehicleAhead) & myDriverName(DriverName) & amMoving(false) <-// .print("convoy management plan already running..");
								.send(VehicleAhead, askOne, convoyMemberInfo(X,Y,Z,_,_,_,_),convoyMemberInfo(X1,Y1,Z1,A1,B1,C1,D1));
								.send(DriverName, achieve, clearArrivedState);
								.send(DriverName, tell, updatedTempMoveXZ(X1,Z1));
								.send(DriverName, achieve, moveToKnownPosition);
								!manageDistance(X1,Y1,Z1);
								.wait(1500);
								!convoyMgmtPlan.

+!convoyMgmtPlan : true & vehicleAhead(VehicleAhead) & myDriverName(DriverName) & amMoving(true) <-// .print("convoy management plan already running..");
								.send(VehicleAhead, askOne, convoyMemberInfo(X,Y,Z,_,_,_,_),convoyMemberInfo(X1,Y1,Z1,A1,B1,C1,D1));
								.send(DriverName, tell, updatedTempMoveXZ(X1,Z1));
								!manageDistance(X1,Y1,Z1);
								.wait(1500);
								!convoyMgmtPlan.
																																								
+!convoyMgmtPlan : true <- .print("im either lead convoy vehicle, or pretty confused").

+!checkIfConvoyPassed(DPosX,DPosY, DPosZ) : convoyMemberInfo(MyX, MyY, MyZ, Health, Speed, ChassiHeading, AuxInfo)
							& myDriverName(DriverName) 
							& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
							& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
							& DTotal <= 40
							<- 												
							+convoyPassed(true).
							
+!checkIfConvoyPassed(DPosX,DPosY, DPosZ) : convoyMemberInfo(MyX, MyY, MyZ, Health, Speed, ChassiHeading, AuxInfo)
							& myDriverName(DriverName) 
							& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
							& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
							& DTotal > 40
							<- .wait(10).
							
+!checkIfConvoyPassed(DPosX,DPosY, DPosZ) : true <- .print("something wrong with checkIfConvoyPassed").

+!manageDistance(DPosX,DPosY, DPosZ) : convoyMemberInfo(MyX, MyY, MyZ, Health, Speed, ChassiHeading, AuxInfo)
							& myDriverName(DriverName) 
							& DX=(DPosX-MyX)& DY=(DPosY-MyY) & DZ=(DPosZ-MyZ)
							& DTotal = math.abs(DX)+math.abs(DY)+math.abs(DZ)
							& distanceToCarInfront(MIN,MAX,GOAL) & MIN <= DTotal & MAX >= DTotal
							<- .send(DriverName, achieve, GOAL).
							
+!manageDistance(DPosX,DPosY, DPosZ) : true <- .print("not enough info to manage distance yet").

+!passBackWaypoints : started(Owner) & vehicleBehind(VehBehind) <- 
	.print("passing waypoints back along convoy members to ", VehBehind);
	.findall(wayPoint(A,B,C), wayPoint(A,B,C), Val);
	.send(VehBehind, tell, Val);
	.wait(1000);
	.send(VehBehind, achieve, followWaypoints).
										
+!followWaypoints : myDriverName(Driver) & .count(wayPoint(_,_,_),NWays) <- 
	.print("pushing ",NWays," waypoints to ", Driver);
	.findall(wayPoint(A,B,C), wayPoint(A,B,C), Val);
	.send(Driver, tell, Val);
	.wait(1000);
	.send(Driver, achieve, followWayPoints).
