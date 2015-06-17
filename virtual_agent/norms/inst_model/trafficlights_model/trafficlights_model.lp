%
% Domain declarations for trafficlights
%
agent(centralMember1).
agent(centralMember2).
agent(centralMember3).
agent(centralMember4).
%
% -------------------------------PART 1-------------------------------
%
%
% Standard prelude for trafficlights
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
% Rules for Institution trafficlights
%
  ifluent(live(trafficlights), trafficlights).
  fluent(live(trafficlights), trafficlights).
  inst(trafficlights).
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
% Agent
%
% Exogenous events
% Event: reduceSpeed (type: ex)
  event(reduceSpeed(Agent0)) :- agent(Agent0).
  evtype(reduceSpeed(Agent0),trafficlights,ex) :- agent(Agent0).
  evinst(reduceSpeed(Agent0),trafficlights) :- agent(Agent0).
  ifluent(perm(reduceSpeed(Agent0)), trafficlights) :- agent(Agent0).
  fluent(perm(reduceSpeed(Agent0)), trafficlights) :- agent(Agent0).
  event(viol(reduceSpeed(Agent0))) :- agent(Agent0).
  evtype(viol(reduceSpeed(Agent0)),trafficlights,viol) :- agent(Agent0).
  evinst(viol(reduceSpeed(Agent0)),trafficlights) :- agent(Agent0).
% Event: upcomingRedLight (type: ex)
  event(upcomingRedLight(Agent0)) :- agent(Agent0).
  evtype(upcomingRedLight(Agent0),trafficlights,ex) :- agent(Agent0).
  evinst(upcomingRedLight(Agent0),trafficlights) :- agent(Agent0).
  ifluent(perm(upcomingRedLight(Agent0)), trafficlights) :- agent(Agent0).
  fluent(perm(upcomingRedLight(Agent0)), trafficlights) :- agent(Agent0).
  event(viol(upcomingRedLight(Agent0))) :- agent(Agent0).
  evtype(viol(upcomingRedLight(Agent0)),trafficlights,viol) :- agent(Agent0).
  evinst(viol(upcomingRedLight(Agent0)),trafficlights) :- agent(Agent0).
% Event: deadline (type: ex)
  event(deadline) :- true.
  evtype(deadline,trafficlights,ex) :- true.
  evinst(deadline,trafficlights) :- true.
  ifluent(perm(deadline), trafficlights) :- true.
  fluent(perm(deadline), trafficlights) :- true.
  event(viol(deadline)) :- true.
  evtype(viol(deadline),trafficlights,viol) :- true.
  evinst(viol(deadline),trafficlights) :- true.
%
% null event for unknown events
% Event: null (type: ex)
  event(null).
  evtype(null,trafficlights,ex).
  evinst(null,trafficlights).
  ifluent(perm(null), trafficlights).
  fluent(perm(null), trafficlights).
  event(viol(null)).
  evtype(viol(null),trafficlights,viol).
  evinst(viol(null),trafficlights).
% Institutional events
% Event: iniOblSlowDown (type: in)
  event(iniOblSlowDown(Agent0)) :- agent(Agent0).
  evtype(iniOblSlowDown(Agent0),trafficlights,inst) :- agent(Agent0).
  evinst(iniOblSlowDown(Agent0),trafficlights) :- agent(Agent0).
  ifluent(pow(trafficlights,iniOblSlowDown(Agent0)),trafficlights) :- agent(Agent0).
  ifluent(perm(iniOblSlowDown(Agent0)),trafficlights) :- agent(Agent0).
  fluent(pow(trafficlights,iniOblSlowDown(Agent0)),trafficlights) :- agent(Agent0).
  fluent(perm(iniOblSlowDown(Agent0)),trafficlights) :- agent(Agent0).
  event(viol(iniOblSlowDown(Agent0))) :- agent(Agent0).
  evtype(viol(iniOblSlowDown(Agent0)),trafficlights,viol) :- agent(Agent0).
  evinst(viol(iniOblSlowDown(Agent0)),trafficlights) :- agent(Agent0).
%
% Violation events
%
% Event: vioSlowDown (type: in)
  event(vioSlowDown(Agent0)) :- agent(Agent0).
  evtype(vioSlowDown(Agent0),trafficlights,viol) :- agent(Agent0).
  evinst(vioSlowDown(Agent0),trafficlights) :- agent(Agent0).
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
%
% noninertial fluents
%
%
% violation fluents (to be implemented)

%
% obligation fluents
%
oblfluent(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)), trafficlights) :-
   event(reduceSpeed(Agent0)),
   event(deadline),
   event(vioSlowDown(Agent1)), agent(Agent0),true,agent(Agent1),inst(trafficlights).
fluent(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)), trafficlights) :-
   event(reduceSpeed(Agent0)),
   event(deadline),
   event(vioSlowDown(Agent1)), agent(Agent0),true,agent(Agent1),inst(trafficlights).
terminated(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)),trafficlights,I) :-
   event(reduceSpeed(Agent0)), occurred(reduceSpeed(Agent0),trafficlights,I),
   event(deadline),
   holdsat(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)),trafficlights,I),
   event(vioSlowDown(Agent1)), agent(Agent0),true,agent(Agent1),inst(trafficlights).
terminated(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)),trafficlights,I) :-
   event(reduceSpeed(Agent0)), 
   event(deadline), occurred(deadline,trafficlights,I),
   holdsat(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)),trafficlights,I),
   event(vioSlowDown(Agent1)), agent(Agent0),true,agent(Agent1),inst(trafficlights).
occurred(vioSlowDown(Agent1),trafficlights,I) :-
   event(reduceSpeed(Agent0)), 
   event(deadline), occurred(deadline,trafficlights,I),
   holdsat(obl(reduceSpeed(Agent0),deadline,vioSlowDown(Agent1)),trafficlights,I),
   event(vioSlowDown(Agent1)), agent(Agent0),true,agent(Agent1),inst(trafficlights).
%
% -------------------------------PART 2-------------------------------
%
% generates: [[['upcomingRedLight', ['Agent']], [['iniOblSlowDown', ['Agent']]], [], []]]
%
% generate rules
%
%
% Translation of upcomingRedLight(Agent) generates iniOblSlowDown(Agent) if [] in  
occurred(iniOblSlowDown(Agent),trafficlights,I) :- occurred(upcomingRedLight(Agent),trafficlights,I),
   holdsat(pow(trafficlights,iniOblSlowDown(Agent)),trafficlights,I),
   agent(Agent),
   inst(trafficlights), instant(I).
%
% initiate rules
%
%
% Translation of iniOblSlowDown(Agent) initiates ['perm', ['reduceSpeed', ['Agent']]] if []
%
initiated(perm(reduceSpeed(Agent)),trafficlights,I) :-
   occurred(iniOblSlowDown(Agent),trafficlights,I),
   holdsat(live(trafficlights),trafficlights,I), inst(trafficlights),
   agent(Agent),
   inst(trafficlights), instant(I).
%
% Translation of iniOblSlowDown(Agent) initiates ['obl', [['reduceSpeed', ['Agent']], ['deadline', []], ['vioSlowDown', ['Agent']]]] if []
%
initiated(obl(reduceSpeed(Agent),deadline,vioSlowDown(Agent)),trafficlights,I) :-
   occurred(iniOblSlowDown(Agent),trafficlights,I),
   holdsat(live(trafficlights),trafficlights,I), inst(trafficlights),
   agent(Agent),
   inst(trafficlights), instant(I).
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
holdsat(live(trafficlights),trafficlights,I) :- start(I), inst(trafficlights).
holdsat(perm(null),trafficlights,I) :- start(I), inst(trafficlights).
% initially: perm(deadline)
holdsat(perm(deadline),trafficlights,I) :-
   inst(trafficlights), start(I).
% initially: pow(trafficlights,iniOblSlowDown(Agent))
holdsat(pow(trafficlights,iniOblSlowDown(Agent)),trafficlights,I) :-
   agent(Agent),
   inst(trafficlights), start(I).
% initially: perm(iniOblSlowDown(Agent))
holdsat(perm(iniOblSlowDown(Agent)),trafficlights,I) :-
   agent(Agent),
   inst(trafficlights), start(I).
%
% End of file
%
