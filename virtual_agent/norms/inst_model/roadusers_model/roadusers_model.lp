%
% Domain declarations for roadusers
%
agent(centralMember1).
agent(centralMember2).
agent(centralMember3).
agent(centralMember4).
%
% -------------------------------PART 1-------------------------------
%
%
% Standard prelude for roadusers
%
% suppress clingo warnings in absence of inertials, non-inertials or obligations
ifluent(0,0).
nifluent(0,0).
oblfluent(0,0). 
% fluent rules
holdsat(P,In,J):- holdsat(P,In,I),not terminated(P,In,I),
    next(I,J),ifluent(P, In),instant(I),instant(J), inst(In).
holdsat(P,In,J):- initiated(P,In,I),next(I,J),
    ifluent(P, In),instant(I),instant(J), inst(In).
holdsat(P,In,J):- initiated(P,In,I),next(I,J), 
    oblfluent(P, In),instant(I),instant(J), inst(In).
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
% Rules for Institution roadusers
%
  ifluent(live(roadusers), roadusers).
  fluent(live(roadusers), roadusers).
  inst(roadusers).
%
% Constraints for obserable events depending on mode option
%
%%  mode SINGLE is chosen:
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
% Event: changeLane (type: ex)
  event(changeLane(Agent0)) :- agent(Agent0).
  evtype(changeLane(Agent0),roadusers,ex) :- agent(Agent0).
  evinst(changeLane(Agent0),roadusers) :- agent(Agent0).
  ifluent(perm(changeLane(Agent0)), roadusers) :- agent(Agent0).
  fluent(perm(changeLane(Agent0)), roadusers) :- agent(Agent0).
  event(viol(changeLane(Agent0))) :- agent(Agent0).
  evtype(viol(changeLane(Agent0)), roadusers, viol) :- agent(Agent0).
  evinst(viol(changeLane(Agent0)),roadusers) :- agent(Agent0).
% Event: deadline (type: ex)
  event(deadline) :- true.
  evtype(deadline,roadusers,ex) :- true.
  evinst(deadline,roadusers) :- true.
  ifluent(perm(deadline), roadusers) :- true.
  fluent(perm(deadline), roadusers) :- true.
  event(viol(deadline)) :- true.
  evtype(viol(deadline), roadusers, viol) :- true.
  evinst(viol(deadline),roadusers) :- true.
% Event: flashLights (type: ex)
  event(flashLights(Agent0)) :- agent(Agent0).
  evtype(flashLights(Agent0),roadusers,ex) :- agent(Agent0).
  evinst(flashLights(Agent0),roadusers) :- agent(Agent0).
  ifluent(perm(flashLights(Agent0)), roadusers) :- agent(Agent0).
  fluent(perm(flashLights(Agent0)), roadusers) :- agent(Agent0).
  event(viol(flashLights(Agent0))) :- agent(Agent0).
  evtype(viol(flashLights(Agent0)), roadusers, viol) :- agent(Agent0).
  evinst(viol(flashLights(Agent0)),roadusers) :- agent(Agent0).
%
% null event for unknown events
% Event: null (type: ex)
  event(null).
  evtype(null,roadusers,ex).
  evinst(null,roadusers).
  ifluent(perm(null), roadusers).
  fluent(perm(null), roadusers).
  event(viol(null)).
  evtype(viol(null),roadusers,viol).
  evinst(viol(null),roadusers).
% Institutional events
% Event: iniOblChangeLane (type: in)
  event(iniOblChangeLane(Agent0)) :- agent(Agent0).
  evtype(iniOblChangeLane(Agent0),roadusers,inst) :- agent(Agent0).
  evinst(iniOblChangeLane(Agent0),roadusers) :- agent(Agent0).
  ifluent(pow(roadusers,iniOblChangeLane(Agent0)),roadusers) :- agent(Agent0).
  ifluent(perm(iniOblChangeLane(Agent0)),roadusers) :- agent(Agent0).
  fluent(pow(roadusers,iniOblChangeLane(Agent0)),roadusers) :- agent(Agent0).
  fluent(perm(iniOblChangeLane(Agent0)),roadusers) :- agent(Agent0).
  event(viol(iniOblChangeLane(Agent0))) :- agent(Agent0).
  evtype(viol(iniOblChangeLane(Agent0)),roadusers,viol) :- agent(Agent0).
  evinst(viol(iniOblChangeLane(Agent0)),roadusers) :- agent(Agent0).
%
% Violation events
%
% Event: vioMove (type: in)
  event(vioMove(Agent0)) :- agent(Agent0).
  evtype(vioMove(Agent0),roadusers,viol) :- agent(Agent0).
  evinst(vioMove(Agent0),roadusers) :- agent(Agent0).
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
oblfluent(obl(changeLane(Agent0),deadline,vioMove(Agent1)), roadusers) :-
   event(changeLane(Agent0)),
   event(deadline),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
ifluent(obl(changeLane(Agent0),deadline,vioMove(Agent1)), roadusers) :-
   event(changeLane(Agent0)),
   event(deadline),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
fluent(obl(changeLane(Agent0),deadline,vioMove(Agent1)), roadusers) :-
   event(changeLane(Agent0)),
   event(deadline),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
terminated(obl(changeLane(Agent0),deadline,vioMove(Agent1)),roadusers,I) :-
   event(changeLane(Agent0)), occurred(changeLane(Agent0),roadusers,I),
   event(deadline),
   holdsat(obl(changeLane(Agent0),deadline,vioMove(Agent1)),roadusers,I),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
terminated(obl(changeLane(Agent0),deadline,vioMove(Agent1)),roadusers,I) :-
   event(changeLane(Agent0)), 
   event(deadline), occurred(deadline,roadusers,I),
   holdsat(obl(changeLane(Agent0),deadline,vioMove(Agent1)),roadusers,I),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
occurred(vioMove(Agent1),roadusers,I) :-
   event(changeLane(Agent0)), 
   event(deadline), occurred(deadline,roadusers,I),
   holdsat(obl(changeLane(Agent0),deadline,vioMove(Agent1)),roadusers,I),
   event(vioMove(Agent1)), agent(Agent0),true,agent(Agent1),inst(roadusers).
%
% -------------------------------PART 2-------------------------------
%
%
% generate rules
%
%
% Translation of flashLights(Agent) generates iniOblChangeLane(Agent) if [] in 
occurred(iniOblChangeLane(Agent),roadusers,I) :- occurred(flashLights(Agent),roadusers,I),
   holdsat(pow(roadusers,iniOblChangeLane(Agent)),roadusers,I),
   agent(Agent),
   inst(roadusers), instant(I).
%
% initiate rules
%
%
% Translation of iniOblChangeLane(Agent) initiates ['perm', ['changeLane', ['Agent']]] if []
%
initiated(perm(changeLane(Agent)),roadusers,I) :-
   occurred(iniOblChangeLane(Agent),roadusers,I),
   holdsat(live(roadusers),roadusers,I), inst(roadusers),
   agent(Agent),
   inst(roadusers), instant(I).
%
% Translation of iniOblChangeLane(Agent) initiates ['obl', [['changeLane', ['Agent']], ['deadline', []], ['vioMove', ['Agent']]]] if []
%
initiated(obl(changeLane(Agent),deadline,vioMove(Agent)),roadusers,I) :-
   occurred(iniOblChangeLane(Agent),roadusers,I),
   holdsat(live(roadusers),roadusers,I), inst(roadusers),
   agent(Agent),
   inst(roadusers), instant(I).
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
holdsat(live(roadusers),roadusers,I) :- start(I), inst(roadusers).
holdsat(perm(null),roadusers,I) :- start(I), inst(roadusers).
% initially: perm(deadline)
holdsat(perm(deadline),roadusers,I) :-
   inst(roadusers), start(I).
% initially: perm(iniOblChangeLane(Agent))
holdsat(perm(iniOblChangeLane(Agent)),roadusers,I) :-
   agent(Agent),
   inst(roadusers), start(I).
% initially: pow(roadusers,iniOblChangeLane(Agent))
holdsat(pow(roadusers,iniOblChangeLane(Agent)),roadusers,I) :-
   agent(Agent),
   inst(roadusers), start(I).
%
% End of file
%
