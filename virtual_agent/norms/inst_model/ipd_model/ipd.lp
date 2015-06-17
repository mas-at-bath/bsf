%
% Domain declarations for ipd
%
agent(jason1).
agent(jason2).
agent(jason3).
player(player1).
%
% -------------------------------PART 1-------------------------------
%
%
% Standard prelude for ipd
%
% instant ordering
% removed for MDV 20130117
%%% before(I1,I2):- next(I1,I2),instant(I1), instant(I2).
%%% before(I1,I3):- before(I1,I2),before(I2,I3),
%%%    instant(I1),instant(I2),instant(I3).
% fluent rules
holdsat(P,J):- holdsat(P,I),not terminated(P,I),
    next(I,J),ifluent(P),instant(I),instant(J).
holdsat(P,J):- initiated(P,I),next(I,J),
    ifluent(P),instant(I),instant(J).
holdsat(P,J):- initiated(P,I),next(I,J),
    oblfluent(P),instant(I),instant(J).
% all observed events occur
occurred(E,I):- evtype(E,ex),observed(E,I),instant(I).
% a violation occurs for each non-permitted action
occurred(viol(E),I):-
    occurred(E,I),
    evtype(E,ex),
    not holdsat(perm(E),I),
    holdsat(live(X),I),evinst(E,X),
    event(E),instant(I),event(viol(E)).
occurred(viol(E),I):-
    occurred(E,I),
    evtype(E,inst),
    not holdsat(perm(E),I),
    event(E),instant(I),event(viol(E)).
{observed(E,J)}:- evtype(E,ex),instant(J), not final(J).
:- observed(E,J),observed(F,J),instant(J),evtype(E,ex),
   evtype(F,ex), E!=F.
obs(I):- observed(E,I),evtype(E,ex),instant(I).
:- not obs(I), not final(I), instant(I).
% needed until I tidy up some of the constraint generation
true.
%
% Rules for Institution ipd
%
  ifluent(live(ipd)).
  inst(ipd).
%
% The following types were declared:
%
% Player
% Agent
%
% Exogenous events
% Event: deadline (type: ex)
  event(deadline) :- true.
  evtype(deadline,ex) :- true.
  evinst(deadline,ipd) :- true.
  ifluent(perm(deadline)) :- true.
  event(viol(deadline)) :- true.
  evtype(viol(deadline),viol) :- true.
  evinst(viol(deadline),ipd) :- true.
% Event: avoidCollision (type: ex)
  event(avoidCollision(Agent0,Player1)) :- agent(Agent0),player(Player1).
  evtype(avoidCollision(Agent0,Player1),ex) :- agent(Agent0),player(Player1).
  evinst(avoidCollision(Agent0,Player1),ipd) :- agent(Agent0),player(Player1).
  ifluent(perm(avoidCollision(Agent0,Player1))) :- agent(Agent0),player(Player1).
  event(viol(avoidCollision(Agent0,Player1))) :- agent(Agent0),player(Player1).
  evtype(viol(avoidCollision(Agent0,Player1)),viol) :- agent(Agent0),player(Player1).
  evinst(viol(avoidCollision(Agent0,Player1)),ipd) :- agent(Agent0),player(Player1).
% Event: detected (type: ex)
  event(detected(Player0)) :- player(Player0).
  evtype(detected(Player0),ex) :- player(Player0).
  evinst(detected(Player0),ipd) :- player(Player0).
  ifluent(perm(detected(Player0))) :- player(Player0).
  event(viol(detected(Player0))) :- player(Player0).
  evtype(viol(detected(Player0)),viol) :- player(Player0).
  evinst(viol(detected(Player0)),ipd) :- player(Player0).
% Event: greet (type: ex)
  event(greet(Agent0,Player1)) :- agent(Agent0),player(Player1).
  evtype(greet(Agent0,Player1),ex) :- agent(Agent0),player(Player1).
  evinst(greet(Agent0,Player1),ipd) :- agent(Agent0),player(Player1).
  ifluent(perm(greet(Agent0,Player1))) :- agent(Agent0),player(Player1).
  event(viol(greet(Agent0,Player1))) :- agent(Agent0),player(Player1).
  evtype(viol(greet(Agent0,Player1)),viol) :- agent(Agent0),player(Player1).
  evinst(viol(greet(Agent0,Player1)),ipd) :- agent(Agent0),player(Player1).
% Institutional events
% Event: intDetected (type: in)
  event(intDetected(Player0)) :- player(Player0).
  evtype(intDetected(Player0),inst) :- player(Player0).
  evinst(intDetected(Player0),ipd) :- player(Player0).
  ifluent(pow(ipd,intDetected(Player0))) :- player(Player0).
  ifluent(perm(intDetected(Player0))) :- player(Player0).
  event(viol(intDetected(Player0))) :- player(Player0).
  evtype(viol(intDetected(Player0)),viol) :- player(Player0).
  evinst(viol(intDetected(Player0)),ipd) :- player(Player0).
%
% Violation events
%
% Event: violpoliteness (type: in)
  event(violpoliteness(Agent0)) :- agent(Agent0).
  evtype(violpoliteness(Agent0),viol) :- agent(Agent0).
  evinst(violpoliteness(Agent0),ipd) :- agent(Agent0).
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
ifluent(friends(Agent,Player)) :-
   agent(Agent),
   player(Player),
   true.
%
% noninertial fluents
%
nifluent(lowIPD(Agent,Player)) :-
   agent(Agent),
   player(Player),
   true.
nifluent(highIPD(Agent,Player)) :-
   agent(Agent),
   player(Player),
   true.
%
% violation fluents (to be implemented)

%
% obligation fluents
%
oblfluent(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2))) :-
   event(greet(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2).
terminated(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2)),I) :-
   occurred(greet(Agent0,Player1),I),
   holdsat(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(greet(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
terminated(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2)),I) :-
   occurred(deadline,I),
   holdsat(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(greet(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
occurred(violpoliteness(Agent2),I) :-
   occurred(deadline,I),
   holdsat(obl(greet(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(greet(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
oblfluent(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2))) :-
   event(avoidCollision(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2).
terminated(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2)),I) :-
   occurred(avoidCollision(Agent0,Player1),I),
   holdsat(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(avoidCollision(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
terminated(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2)),I) :-
   occurred(deadline,I),
   holdsat(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(avoidCollision(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
occurred(violpoliteness(Agent2),I) :-
   occurred(deadline,I),
   holdsat(obl(avoidCollision(Agent0,Player1),deadline,violpoliteness(Agent2)),I),
   event(avoidCollision(Agent0,Player1);deadline;violpoliteness(Agent2)),
   agent(Agent0),player(Player1),true,agent(Agent2),instant(I).
%
% -------------------------------PART 2-------------------------------
%
%
% generate rules
%
%
% Translation of detected(Player) generates intDetected(Player) if []
occurred(intDetected(Player),I) :- occurred(detected(Player),I),
   holdsat(pow(ipd,intDetected(Player)),I),
   player(Player),
   instant(I).
%
% initiate rules
%
%
% Translation of intDetected(Player) initiates ['perm', ['greet', ['Agent', 'Player']]] if ['lowIPD', ['Agent', 'Player']]
%
initiated(perm(greet(Agent,Player)),I) :-
   occurred(intDetected(Player),I),
   holdsat(live(ipd),I),
   holdsat(lowIPD(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% Translation of intDetected(Player) initiates ['obl', [['greet', ['Agent', 'Player']], ['deadline', []], ['violpoliteness', ['Agent']]]] if ['lowIPD', ['Agent', 'Player']]
%
initiated(obl(greet(Agent,Player),deadline,violpoliteness(Agent)),I) :-
   occurred(intDetected(Player),I),
   holdsat(live(ipd),I),
   holdsat(lowIPD(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% Translation of intDetected(Player) initiates ['perm', ['avoidCollision', ['Agent', 'Player']]] if ['highIPD', ['Agent', 'Player']]
%
initiated(perm(avoidCollision(Agent,Player)),I) :-
   occurred(intDetected(Player),I),
   holdsat(live(ipd),I),
   holdsat(highIPD(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% Translation of intDetected(Player) initiates ['obl', [['avoidCollision', ['Agent', 'Player']], ['deadline', []], ['violpoliteness', ['Agent']]]] if ['highIPD', ['Agent', 'Player']]
%
initiated(obl(avoidCollision(Agent,Player),deadline,violpoliteness(Agent)),I) :-
   occurred(intDetected(Player),I),
   holdsat(live(ipd),I),
   holdsat(highIPD(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% terminate rules
%
%
% noninertial rules
%
%
% Translation of highIPD(Agent,Player) when ['not', ['lowIPD', ['Agent', 'Player']]]
holdsat(highIPD(Agent,Player),I) :-
   not
   holdsat(lowIPD(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% Translation of lowIPD(Agent,Player) when ['friends', ['Agent', 'Player']]
holdsat(lowIPD(Agent,Player),I) :-
   holdsat(friends(Agent,Player),I),
   player(Player),
   agent(Agent),
   instant(I).
%
% -------------------------------PART 3-------------------------------
%
%
% initially
%
% no creation event
holdsat(live(ipd),I) :- start(I).
% initially: perm(detected(Player))
holdsat(perm(detected(Player)),I) :-
   player(Player),
   start(I).
% initially: perm(intDetected(Player))
holdsat(perm(intDetected(Player)),I) :-
   player(Player),
   start(I).
% initially: pow(ipd,intDetected(Player))
holdsat(pow(ipd,intDetected(Player)),I) :-
   player(Player),
   start(I).

%
% End of file
%
