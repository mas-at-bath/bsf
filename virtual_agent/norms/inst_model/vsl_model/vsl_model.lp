%
% Domain declarations for vsl
%
agent(centralMember1).
follower(centralMember2).
follower(centralMember3).
follower(centralMember4).
position(p1).
position(p2).
position(p3).
speed(slow).
speed(mediumSlow).
speed(medium).
speed(normal).
%
% -------------------------------PART 1-------------------------------
%
%
% Standard prelude for vsl
%
% instant ordering
% removed for MDV 20130117
%%% before(I1,I2):- next(I1,I2),instant(I1), instant(I2).
%%% before(I1,I3):- before(I1,I2),before(I2,I3),
%%%    instant(I1),instant(I2),instant(I3).
% fluent rules
holdsat(P,In,J):- holdsat(P,In,I),not terminated(P,In,I),
    next(I,J),fluent(P, In),instant(I),instant(J), inst(In).
holdsat(P,In,J):- initiated(P,In,I),next(I,J),
    ifluent(P, In),instant(I),instant(J), inst(In).
holdsat(P,In,J):- initiated(P,In,I),next(I,J), 
    oblfluent(P, In),instant(I),instant(J), inst(In).
holdsat(P,In,J):- initiated(P,In,I),next(I,J), 
    nifluent(P, In),instant(I),instant(J), inst(In).
% all observed events occur
occurred(E,In,I):- evtype(E,In,ex),observed(E,In,I),instant(I), inst(In).
% produces null for unknown events 
occurred(null,In,I) :- not evtype(E,In,ex), observed(E,In,I), 
    instant(I), inst(In). 
% produces gap warning for unknown events 
unknown(E, In, I) :- not evtype(E,In,ex), observed(E,In,I), 
    instant(I), inst(In). 
warninggap(In, I) :- unknown(E,In,I), inst(In), instant(I). 
% a violation occurs for each non-permitted action 
occurred(viol(E),In,I):-
    occurred(E,In,I),
    evtype(E,In,ex),
    not holdsat(perm(E),In,I),
    holdsat(live(In),In,I),evinst(E,In),
    event(E),instant(I),event(viol(E)),inst(In).
occurred(viol(E),In,I):-
    occurred(E,In,I),
    evtype(E,In,inst),
    not holdsat(perm(E),In,I),
    event(E),instant(I),event(viol(E)), inst(In).
% needed until I tidy up some of the constraint generation 
true.
%
% Rules for Institution vsl
%
  ifluent(live(vsl), vsl).
  fluent(live(vsl), vsl).
  inst(vsl).
%
% Constraints for obserable events depending on mode option
%
%%  mode DEFAULT is chosen:
{observed(E,In,J)}:- evtype(E,In,ex),instant(J), not final(J), inst(In).
:- observed(E,In,J),observed(F,In,J),instant(J),evtype(E,In,ex),
evtype(F,In,ex), E!=F,inst(In). 
obs(In,I):- observed(E,In,I),evtype(E,In,ex),instant(I),inst(In).
         :- not obs(In,I), not final(I), instant(I), inst(In).

%
% The following types were declared:
%
% Position
% Follower
% Speed
% Agent
%
% Exogenous events
% Event: deadline (type: ex)
  event(deadline) :- true.
  evtype(deadline,vsl,ex) :- true.
  evinst(deadline,vsl) :- true.
  ifluent(perm(deadline), vsl) :- true.
  fluent(perm(deadline), vsl) :- true.
  event(viol(deadline)) :- true.
  evtype(viol(deadline),vsl,viol) :- true.
  evinst(viol(deadline),vsl) :- true.
% Event: emergencyBrake (type: ex)
  event(emergencyBrake(Agent0)) :- agent(Agent0).
  evtype(emergencyBrake(Agent0),vsl,ex) :- agent(Agent0).
  evinst(emergencyBrake(Agent0),vsl) :- agent(Agent0).
  ifluent(perm(emergencyBrake(Agent0)), vsl) :- agent(Agent0).
  fluent(perm(emergencyBrake(Agent0)), vsl) :- agent(Agent0).
  event(viol(emergencyBrake(Agent0))) :- agent(Agent0).
  evtype(viol(emergencyBrake(Agent0)),vsl,viol) :- agent(Agent0).
  evinst(viol(emergencyBrake(Agent0)),vsl) :- agent(Agent0).
% Event: slowDown (type: ex)
  event(slowDown(Follower0,Speed1)) :- follower(Follower0),speed(Speed1).
  evtype(slowDown(Follower0,Speed1),vsl,ex) :- follower(Follower0),speed(Speed1).
  evinst(slowDown(Follower0,Speed1),vsl) :- follower(Follower0),speed(Speed1).
  ifluent(perm(slowDown(Follower0,Speed1)), vsl) :- follower(Follower0),speed(Speed1).
  fluent(perm(slowDown(Follower0,Speed1)), vsl) :- follower(Follower0),speed(Speed1).
  event(viol(slowDown(Follower0,Speed1))) :- follower(Follower0),speed(Speed1).
  evtype(viol(slowDown(Follower0,Speed1)),vsl,viol) :- follower(Follower0),speed(Speed1).
  evinst(viol(slowDown(Follower0,Speed1)),vsl) :- follower(Follower0),speed(Speed1).
%
% null event for unknown events
% Event: null (type: ex)
  event(null).
  evtype(null,vsl,ex).
  evinst(null,vsl).
  ifluent(perm(null), vsl).
  fluent(perm(null), vsl).
  event(viol(null)).
  evtype(viol(null),vsl,viol).
  evinst(viol(null),vsl).
% Institutional events
% Event: iniOblSlowDown (type: in)
  event(iniOblSlowDown(Agent0)) :- agent(Agent0).
  evtype(iniOblSlowDown(Agent0),vsl,inst) :- agent(Agent0).
  evinst(iniOblSlowDown(Agent0),vsl) :- agent(Agent0).
  ifluent(pow(vsl,iniOblSlowDown(Agent0)),vsl) :- agent(Agent0).
  ifluent(perm(iniOblSlowDown(Agent0)),vsl) :- agent(Agent0).
  fluent(pow(vsl,iniOblSlowDown(Agent0)),vsl) :- agent(Agent0).
  fluent(perm(iniOblSlowDown(Agent0)),vsl) :- agent(Agent0).
  event(viol(iniOblSlowDown(Agent0))) :- agent(Agent0).
  evtype(viol(iniOblSlowDown(Agent0)),vsl,viol) :- agent(Agent0).
  evinst(viol(iniOblSlowDown(Agent0)),vsl) :- agent(Agent0).
%
% Violation events
%
% Event: vioSlowDown (type: in)
  event(vioSlowDown(Follower0)) :- follower(Follower0).
  evtype(vioSlowDown(Follower0),vsl,viol) :- follower(Follower0).
  evinst(vioSlowDown(Follower0),vsl) :- follower(Follower0).
%
% Creation events
%
%
% Dissolution events
%
%
% dissolve events
%
%
% inertial fluents
%
ifluent(speedMode(Follower0,Speed1),vsl) :-
  follower(Follower0),speed(Speed1).
fluent(speedMode(Follower0,Speed1),vsl) :-
  follower(Follower0),speed(Speed1).

ifluent(vehiclePosition(Follower0,Position1),vsl) :-
  follower(Follower0),position(Position1).
fluent(vehiclePosition(Follower0,Position1),vsl) :-
  follower(Follower0),position(Position1).

%
% noninertial fluents
%
%
% violation fluents (to be implemented)

%
% obligation fluents
%
oblfluent(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)), vsl) :-
   event(slowDown(Follower0,Speed1)),
   event(deadline),
   event(vioSlowDown(Follower2)), follower(Follower0),speed(Speed1),true,follower(Follower2),inst(vsl).
fluent(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)), vsl) :-
   event(slowDown(Follower0,Speed1)),
   event(deadline),
   event(vioSlowDown(Follower2)), follower(Follower0),speed(Speed1),true,follower(Follower2),inst(vsl).
terminated(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)),vsl,I) :-
   event(slowDown(Follower0,Speed1)), occurred(slowDown(Follower0,Speed1),vsl,I),
   event(deadline),
   holdsat(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)),vsl,I),
   event(vioSlowDown(Follower2)), follower(Follower0),speed(Speed1),true,follower(Follower2),inst(vsl).
terminated(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)),vsl,I) :-
   event(slowDown(Follower0,Speed1)), 
   event(deadline), occurred(deadline,vsl,I),
   holdsat(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)),vsl,I),
   event(vioSlowDown(Follower2)), follower(Follower0),speed(Speed1),true,follower(Follower2),inst(vsl).
occurred(vioSlowDown(Follower2),vsl,I) :-
   event(slowDown(Follower0,Speed1)), 
   event(deadline), occurred(deadline,vsl,I),
   holdsat(obl(slowDown(Follower0,Speed1),deadline,vioSlowDown(Follower2)),vsl,I),
   event(vioSlowDown(Follower2)), follower(Follower0),speed(Speed1),true,follower(Follower2),inst(vsl).
%
% -------------------------------PART 2-------------------------------
%
% generates: [[['emergencyBrake', ['Agent']], [['iniOblSlowDown', ['Agent']]], [], []]]
%
% generate rules
%
%
% Translation of emergencyBrake(Agent) generates iniOblSlowDown(Agent) if [] in  
occurred(iniOblSlowDown(Agent),vsl,I) :- occurred(emergencyBrake(Agent),vsl,I),
   holdsat(pow(vsl,iniOblSlowDown(Agent)),vsl,I),
   agent(Agent),
   inst(vsl), instant(I).
%
% initiate rules
%
%
% Translation of iniOblSlowDown(Agent) initiates ['perm', ['slowDown', ['Follower', 'Speed']]] if ['vehiclePosition', ['Follower', 'p3']]
%
initiated(perm(slowDown(Follower,Speed)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p3),vsl,I),
   follower(Follower),
   agent(Agent),
   speed(Speed),
   inst(vsl), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['obl', [['slowDown', ['Follower', 'medium']], ['deadline', []], ['vioSlowDown', ['Follower']]]] if ['vehiclePosition', ['Follower', 'p3']]
%
initiated(obl(slowDown(Follower,medium),deadline,vioSlowDown(Follower)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p3),vsl,I),
   follower(Follower),
   agent(Agent),
   inst(vsl), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['perm', ['slowDown', ['Follower', 'Speed']]] if ['vehiclePosition', ['Follower', 'p2']]
%
initiated(perm(slowDown(Follower,Speed)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p2),vsl,I),
   follower(Follower),
   agent(Agent),
   speed(Speed),
   inst(vsl), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['obl', [['slowDown', ['Follower', 'mediumSlow']], ['deadline', []], ['vioSlowDown', ['Follower']]]] if ['vehiclePosition', ['Follower', 'p2']]
%
initiated(obl(slowDown(Follower,mediumSlow),deadline,vioSlowDown(Follower)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p2),vsl,I),
   follower(Follower),
   agent(Agent),
   inst(vsl), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['perm', ['slowDown', ['Follower', 'Speed']]] if ['vehiclePosition', ['Follower', 'p1']]
%
initiated(perm(slowDown(Follower,Speed)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p1),vsl,I),
   follower(Follower),
   agent(Agent),
   speed(Speed),
   inst(vsl), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['obl', [['slowDown', ['Follower', 'slow']], ['deadline', []], ['vioSlowDown', ['Follower']]]] if ['vehiclePosition', ['Follower', 'p1']]
%
initiated(obl(slowDown(Follower,slow),deadline,vioSlowDown(Follower)),vsl,I) :-
   occurred(iniOblSlowDown(Agent),vsl,I),
   holdsat(live(vsl),vsl,I), inst(vsl),
   holdsat(vehiclePosition(Follower,p1),vsl,I),
   follower(Follower),
   agent(Agent),
   inst(vsl), instant(I).
%
% terminate rules
%
%
% noninertial rules
%
%
% -------------------------------PART 3-------------------------------
%
%
% initially
%
% no creation event
holdsat(live(vsl),vsl,I) :- start(I), inst(vsl).
holdsat(perm(null),vsl,I) :- start(I), inst(vsl).
% initially: perm(deadline)
holdsat(perm(deadline),vsl,I) :-
   inst(vsl), start(I).
% initially: pow(vsl,iniOblSlowDown(Agent))
holdsat(pow(vsl,iniOblSlowDown(Agent)),vsl,I) :-
   agent(Agent),
   inst(vsl), start(I).
% initially: perm(iniOblSlowDown(Agent))
holdsat(perm(iniOblSlowDown(Agent)),vsl,I) :-
   agent(Agent),
   inst(vsl), start(I).
% initially: speedMode(centralMember2,normal)
holdsat(speedMode(centralMember2,normal),vsl,I) :-
   inst(vsl), start(I).
% initially: speedMode(centralMember3,normal)
holdsat(speedMode(centralMember3,normal),vsl,I) :-
   inst(vsl), start(I).
% initially: speedMode(centralMember4,normal)
holdsat(speedMode(centralMember4,normal),vsl,I) :-
   inst(vsl), start(I).
%
% End of file
%
