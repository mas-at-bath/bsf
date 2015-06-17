%
% Domain declarations for queue
%
type(player).
type(virtual_character).
agent(jason).
%
% -------------------------------PART 1-------------------------------
%
%
% Standard prelude for queue
%
% instant ordering
before(I1,I2):- next(I1,I2),instant(I1), instant(I2).
before(I1,I3):- before(I1,I2),before(I2,I3),
   instant(I1),instant(I2),instant(I3).
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
% Rules for Institution queue
%
  ifluent(live(queue)).
  inst(queue).
%
% The following types were declared:
%
% Type
% Agent
%
% Exogenous events
% Event: deadline (type: ex)
  event(deadline) :- true.
  evtype(deadline,ex) :- true.
  evinst(deadline,queue) :- true.
  ifluent(perm(deadline)) :- true.
  event(viol(deadline)) :- true.
  evtype(viol(deadline),viol) :- true.
  evinst(viol(deadline),queue) :- true.
% Event: newArrival (type: ex)
  event(newArrival(Agent0,Type1)) :- agent(Agent0),type(Type1).
  evtype(newArrival(Agent0,Type1),ex) :- agent(Agent0),type(Type1).
  evinst(newArrival(Agent0,Type1),queue) :- agent(Agent0),type(Type1).
  ifluent(perm(newArrival(Agent0,Type1))) :- agent(Agent0),type(Type1).
  event(viol(newArrival(Agent0,Type1))) :- agent(Agent0),type(Type1).
  evtype(viol(newArrival(Agent0,Type1)),viol) :- agent(Agent0),type(Type1).
  evinst(viol(newArrival(Agent0,Type1)),queue) :- agent(Agent0),type(Type1).
% Event: queueLast (type: ex)
  event(queueLast(Agent0)) :- agent(Agent0).
  evtype(queueLast(Agent0),ex) :- agent(Agent0).
  evinst(queueLast(Agent0),queue) :- agent(Agent0).
  ifluent(perm(queueLast(Agent0))) :- agent(Agent0).
  event(viol(queueLast(Agent0))) :- agent(Agent0).
  evtype(viol(queueLast(Agent0)),viol) :- agent(Agent0).
  evinst(viol(queueLast(Agent0)),queue) :- agent(Agent0).
% Event: yield (type: ex)
  event(yield(Agent0)) :- agent(Agent0).
  evtype(yield(Agent0),ex) :- agent(Agent0).
  evinst(yield(Agent0),queue) :- agent(Agent0).
  ifluent(perm(yield(Agent0))) :- agent(Agent0).
  event(viol(yield(Agent0))) :- agent(Agent0).
  evtype(viol(yield(Agent0)),viol) :- agent(Agent0).
  evinst(viol(yield(Agent0)),queue) :- agent(Agent0).
% Event: detectDisable (type: ex)
  event(detectDisable(Agent0)) :- agent(Agent0).
  evtype(detectDisable(Agent0),ex) :- agent(Agent0).
  evinst(detectDisable(Agent0),queue) :- agent(Agent0).
  ifluent(perm(detectDisable(Agent0))) :- agent(Agent0).
  event(viol(detectDisable(Agent0))) :- agent(Agent0).
  evtype(viol(detectDisable(Agent0)),viol) :- agent(Agent0).
  evinst(viol(detectDisable(Agent0)),queue) :- agent(Agent0).
% Institutional events
% Event: iniOblQueueLast (type: in)
  event(iniOblQueueLast(Agent0,Type1)) :- agent(Agent0),type(Type1).
  evtype(iniOblQueueLast(Agent0,Type1),inst) :- agent(Agent0),type(Type1).
  evinst(iniOblQueueLast(Agent0,Type1),queue) :- agent(Agent0),type(Type1).
  ifluent(pow(queue,iniOblQueueLast(Agent0,Type1))) :- agent(Agent0),type(Type1).
  ifluent(perm(iniOblQueueLast(Agent0,Type1))) :- agent(Agent0),type(Type1).
  event(viol(iniOblQueueLast(Agent0,Type1))) :- agent(Agent0),type(Type1).
  evtype(viol(iniOblQueueLast(Agent0,Type1)),viol) :- agent(Agent0),type(Type1).
  evinst(viol(iniOblQueueLast(Agent0,Type1)),queue) :- agent(Agent0),type(Type1).
% Event: iniOblYield (type: in)
  event(iniOblYield(Agent0)) :- agent(Agent0).
  evtype(iniOblYield(Agent0),inst) :- agent(Agent0).
  evinst(iniOblYield(Agent0),queue) :- agent(Agent0).
  ifluent(pow(queue,iniOblYield(Agent0))) :- agent(Agent0).
  ifluent(perm(iniOblYield(Agent0))) :- agent(Agent0).
  event(viol(iniOblYield(Agent0))) :- agent(Agent0).
  evtype(viol(iniOblYield(Agent0)),viol) :- agent(Agent0).
  evinst(viol(iniOblYield(Agent0)),queue) :- agent(Agent0).
%
% Violation events
%
% Event: vioYield (type: in)
  event(vioYield(Agent0)) :- agent(Agent0).
  evtype(vioYield(Agent0),viol) :- agent(Agent0).
  evinst(vioYield(Agent0),queue) :- agent(Agent0).
% Event: vioQueue (type: in)
  event(vioQueue(Agent0)) :- agent(Agent0).
  evtype(vioQueue(Agent0),viol) :- agent(Agent0).
  evinst(vioQueue(Agent0),queue) :- agent(Agent0).
%
% Creation events
%
% Event: createQueue (type: ex)
  event(createQueue) :- true.
  evtype(createQueue,ex) :- true.
  evinst(createQueue,queue) :- true.
  ifluent(perm(createQueue)) :- true.
  event(viol(createQueue)) :- true.
  evtype(viol(createQueue),viol) :- true.
  evinst(viol(createQueue),queue) :- true.
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
oblfluent(obl(queueLast(Agent0),deadline,vioQueue(Agent1))) :-
   event(queueLast(Agent0);deadline;vioQueue(Agent1)),
   agent(Agent0),true,agent(Agent1).
terminated(obl(queueLast(Agent0),deadline,vioQueue(Agent1)),I) :-
   occurred(queueLast(Agent0),I),
   holdsat(obl(queueLast(Agent0),deadline,vioQueue(Agent1)),I),
   event(queueLast(Agent0);deadline;vioQueue(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
terminated(obl(queueLast(Agent0),deadline,vioQueue(Agent1)),I) :-
   occurred(deadline,I),
   holdsat(obl(queueLast(Agent0),deadline,vioQueue(Agent1)),I),
   event(queueLast(Agent0);deadline;vioQueue(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
occurred(vioQueue(Agent1),I) :-
   occurred(deadline,I),
   holdsat(obl(queueLast(Agent0),deadline,vioQueue(Agent1)),I),
   event(queueLast(Agent0);deadline;vioQueue(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
oblfluent(obl(yield(Agent0),deadline,vioYield(Agent1))) :-
   event(yield(Agent0);deadline;vioYield(Agent1)),
   agent(Agent0),true,agent(Agent1).
terminated(obl(yield(Agent0),deadline,vioYield(Agent1)),I) :-
   occurred(yield(Agent0),I),
   holdsat(obl(yield(Agent0),deadline,vioYield(Agent1)),I),
   event(yield(Agent0);deadline;vioYield(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
terminated(obl(yield(Agent0),deadline,vioYield(Agent1)),I) :-
   occurred(deadline,I),
   holdsat(obl(yield(Agent0),deadline,vioYield(Agent1)),I),
   event(yield(Agent0);deadline;vioYield(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
occurred(vioYield(Agent1),I) :-
   occurred(deadline,I),
   holdsat(obl(yield(Agent0),deadline,vioYield(Agent1)),I),
   event(yield(Agent0);deadline;vioYield(Agent1)),
   agent(Agent0),true,agent(Agent1),instant(I).
%
% -------------------------------PART 2-------------------------------
%
%
% generate rules
%
%
% Translation of detectDisable(Agent) generates iniOblYield(Agent) if []
occurred(iniOblYield(Agent),I) :- occurred(detectDisable(Agent),I),
   holdsat(pow(queue,iniOblYield(Agent)),I),
   agent(Agent),
   instant(I).
%
% Translation of newArrival(Agent,Type) generates iniOblQueueLast(Agent,Type) if []
occurred(iniOblQueueLast(Agent,Type),I) :- occurred(newArrival(Agent,Type),I),
   holdsat(pow(queue,iniOblQueueLast(Agent,Type)),I),
   type(Type),
   agent(Agent),
   instant(I).
%
% initiate rules
%
%
% Translation of iniOblYield(Agent) initiates ['perm', ['yield', ['Agent']]] if []
%
initiated(perm(yield(Agent)),I) :-
   occurred(iniOblYield(Agent),I),
   holdsat(live(queue),I),
   agent(Agent),
   instant(I).
%
% Translation of iniOblYield(Agent) initiates ['obl', [['yield', ['Agent']], ['deadline', []], ['vioYield', ['Agent']]]] if []
%
initiated(obl(yield(Agent),deadline,vioYield(Agent)),I) :-
   occurred(iniOblYield(Agent),I),
   holdsat(live(queue),I),
   agent(Agent),
   instant(I).
%
% Translation of iniOblQueueLast(Agent,Type) initiates ['perm', ['queueLast', ['Agent']]] if []
%
initiated(perm(queueLast(Agent)),I) :-
   occurred(iniOblQueueLast(Agent,Type),I),
   holdsat(live(queue),I),
   type(Type),
   agent(Agent),
   instant(I).
%
% Translation of iniOblQueueLast(Agent,Type) initiates ['obl', [['queueLast', ['Agent']], ['deadline', []], ['vioQueue', ['Agent']]]] if []
%
initiated(obl(queueLast(Agent),deadline,vioQueue(Agent)),I) :-
   occurred(iniOblQueueLast(Agent,Type),I),
   holdsat(live(queue),I),
   type(Type),
   agent(Agent),
   instant(I).
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
% at least one create event
%
% clear state to allow for re-creation
%
terminated(F,I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   holdsat(F,I),
   instant(I).
initiated(live(queue),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   instant(I).
%
% set up initial state
%
% initially: ['perm', ['deadline', []]]
initiated(perm(deadline),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   instant(I).
% initially: ['pow', ['iniOblQueueLast', ['Agent', 'Type']]]
initiated(pow(queue,iniOblQueueLast(Agent,Type)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   type(Type),
   agent(Agent),
   instant(I).
% initially: ['perm', ['iniOblQueueLast', ['Agent', 'Type']]]
initiated(perm(iniOblQueueLast(Agent,Type)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   type(Type),
   agent(Agent),
   instant(I).
% initially: ['pow', ['iniOblYield', ['Agent']]]
initiated(pow(queue,iniOblYield(Agent)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   agent(Agent),
   instant(I).
% initially: ['perm', ['iniOblYield', ['Agent']]]
initiated(perm(iniOblYield(Agent)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   agent(Agent),
   instant(I).
% initially: ['perm', ['newArrival', ['Agent', 'Type']]]
initiated(perm(newArrival(Agent,Type)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   type(Type),
   agent(Agent),
   instant(I).
% initially: ['perm', ['detectDisable', ['Agent']]]
initiated(perm(detectDisable(Agent)),I) :-
   occurred(createQueue,I),
   not holdsat(live(queue),I),
   agent(Agent),
   instant(I).
%
% End of file
%
