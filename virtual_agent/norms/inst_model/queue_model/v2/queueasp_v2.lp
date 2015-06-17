ifluent(inQueue(2,p2)).
ifluent(perm(newArrival(2))).
ifluent(perm(iniMove(1,p2))).
ifluent(perm(move(1,p2))).
ifluent(inQueue(1,p1)).
ifluent(live(queue)).
ifluent(pow(queue,iniMoved(1,p2))).
ifluent(perm(iniMoved(1,p1))).
ifluent(pow(queue,iniMove(1,p2))).
ifluent(agentType(1,normal)).
ifluent(perm(iniMoved(2,p2))).
ifluent(inQueue(1,p2)).
ifluent(inQueue(2,p1)).
ifluent(perm(move(2,p2))).
ifluent(perm(iniMove(2,p1))).
ifluent(perm(move(2,p1))).
ifluent(perm(iniMove(1,p1))).
ifluent(pow(queue,iniMove(1,p1))).
ifluent(agentType(2,normal)).
ifluent(perm(iniMoved(1,p2))).
ifluent(pow(queue,iniMoved(1,p1))).
ifluent(perm(move(1,p1))).
ifluent(pow(queue,iniMoved(2,p2))).
ifluent(perm(iniMoved(2,p1))).
ifluent(pow(queue,iniMove(2,p1))).
ifluent(pow(queue,iniMoved(2,p1))).
ifluent(perm(iniMove(2,p2))).
ifluent(endOfQueue(p2)).
ifluent(pow(queue,iniMove(2,p2))).
event(newArrival(1)).
event(viol(newArrival(1))).
event(iniMove(1,p2)).
event(viol(iniMove(1,p2))).
event(newArrival(2)).
event(viol(newArrival(2))).
event(iniMove(1,p1)).
event(viol(iniMove(1,p1))).
event(iniMoved(1,p2)).
event(viol(iniMoved(1,p2))).
event(iniMoved(2,p1)).
event(viol(iniMoved(2,p1))).
event(iniMoved(1,p1)).
event(viol(iniMoved(1,p1))).
event(move(1,p2)).
event(viol(move(1,p2))).
event(createQueue).
event(viol(createQueue)).
event(move(2,p1)).
event(viol(move(2,p1))).
event(move(1,p1)).
event(viol(move(1,p1))).
event(move(2,p2)).
event(viol(move(2,p2))).
event(iniMove(2,p1)).
event(viol(iniMove(2,p1))).
event(iniMoved(2,p2)).
event(viol(iniMoved(2,p2))).
event(iniMove(2,p2)).
event(viol(iniMove(2,p2))).
evtype(newArrival(1),ex).
evinst(newArrival(1),queue).
evtype(viol(newArrival(1)),viol).
evinst(viol(newArrival(1)),queue).
evtype(iniMove(1,p2),inst).
evinst(iniMove(1,p2),queue).
evtype(viol(iniMove(1,p2)),viol).
evinst(viol(iniMove(1,p2)),queue).
evtype(newArrival(2),ex).
evinst(newArrival(2),queue).
evtype(viol(newArrival(2)),viol).
evinst(viol(newArrival(2)),queue).
evtype(iniMove(1,p1),inst).
evinst(iniMove(1,p1),queue).
evtype(viol(iniMove(1,p1)),viol).
evinst(viol(iniMove(1,p1)),queue).
evtype(iniMoved(1,p2),inst).
evinst(iniMoved(1,p2),queue).
evtype(viol(iniMoved(1,p2)),viol).
evinst(viol(iniMoved(1,p2)),queue).
evtype(iniMoved(2,p1),inst).
evinst(iniMoved(2,p1),queue).
evtype(viol(iniMoved(2,p1)),viol).
evinst(viol(iniMoved(2,p1)),queue).
evtype(iniMoved(1,p1),inst).
evinst(iniMoved(1,p1),queue).
evtype(viol(iniMoved(1,p1)),viol).
evinst(viol(iniMoved(1,p1)),queue).
evtype(move(1,p2),ex).
evinst(move(1,p2),queue).
evtype(viol(move(1,p2)),viol).
evinst(viol(move(1,p2)),queue).
evtype(createQueue,ex).
evinst(createQueue,queue).
evtype(viol(createQueue),viol).
evinst(viol(createQueue),queue).
evtype(move(2,p1),ex).
evinst(move(2,p1),queue).
evtype(viol(move(2,p1)),viol).
evinst(viol(move(2,p1)),queue).
evtype(move(1,p1),ex).
evinst(move(1,p1),queue).
evtype(viol(move(1,p1)),viol).
evinst(viol(move(1,p1)),queue).
evtype(move(2,p2),ex).
evinst(move(2,p2),queue).
evtype(viol(move(2,p2)),viol).
evinst(viol(move(2,p2)),queue).
evtype(iniMove(2,p1),inst).
evinst(iniMove(2,p1),queue).
evtype(viol(iniMove(2,p1)),viol).
evinst(viol(iniMove(2,p1)),queue).
evtype(iniMoved(2,p2),inst).
evinst(iniMoved(2,p2),queue).
evtype(viol(iniMoved(2,p2)),viol).
evinst(viol(iniMoved(2,p2)),queue).
evtype(iniMove(2,p2),inst).
evinst(iniMove(2,p2),queue).
evtype(viol(iniMove(2,p2)),viol).
evinst(viol(iniMove(2,p2)),queue).
%
%Creation effects for event createQueue
initiated(live(queue),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
% 
% Rules for Institution queue
% 
% 
inst(queue).
%
%Translation of: 
% iniMove(Agent,P) initiates perm(move(Agent,P)) if ;
% 
initiated(perm(move(1,p1)),I) :- 
	occured(iniMove(1,p1),I),holdsat(live(queue),I),instant(I).
initiated(perm(move(1,p2)),I) :- 
	occured(iniMove(1,p2),I),holdsat(live(queue),I),instant(I).
initiated(perm(move(2,p1)),I) :- 
	occured(iniMove(2,p1),I),holdsat(live(queue),I),instant(I).
initiated(perm(move(2,p2)),I) :- 
	occured(iniMove(2,p2),I),holdsat(live(queue),I),instant(I).
%
%Translation of: 
% iniMoved(Agent,Position2) initiates inQueue(Agent,Position2) if ;
% 
initiated(inQueue(1,p1),I) :- 
	occured(iniMoved(1,p1),I),holdsat(live(queue),I),instant(I).
initiated(inQueue(1,p2),I) :- 
	occured(iniMoved(1,p2),I),holdsat(live(queue),I),instant(I).
initiated(inQueue(2,p1),I) :- 
	occured(iniMoved(2,p1),I),holdsat(live(queue),I),instant(I).
initiated(inQueue(2,p2),I) :- 
	occured(iniMoved(2,p2),I),holdsat(live(queue),I),instant(I).
%
%Translation of: 
% iniMoved(Agent,Position2) terminates inQueue(Agent,Position1) if inQueue(Agent,Position1);
% 
terminated(inQueue(1,p1),I) :- 
	occured(iniMoved(1,p1),I),holdsat(inQueue(1,p1),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(1,p1),I) :- 
	occured(iniMoved(1,p2),I),holdsat(inQueue(1,p1),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(1,p2),I) :- 
	occured(iniMoved(1,p1),I),holdsat(inQueue(1,p2),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(1,p2),I) :- 
	occured(iniMoved(1,p2),I),holdsat(inQueue(1,p2),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(2,p1),I) :- 
	occured(iniMoved(2,p1),I),holdsat(inQueue(2,p1),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(2,p1),I) :- 
	occured(iniMoved(2,p2),I),holdsat(inQueue(2,p1),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(2,p2),I) :- 
	occured(iniMoved(2,p1),I),holdsat(inQueue(2,p2),I),holdsat(live(queue),I),instant(I).
terminated(inQueue(2,p2),I) :- 
	occured(iniMoved(2,p2),I),holdsat(inQueue(2,p2),I),holdsat(live(queue),I),instant(I).
%
%Translation of: 
% newArrival(Agent) generates iniMove(Agent,Position) if agentType(Agent,normal),endOfQueue(Position);
% 
occured(iniMove(1,p1),I) :- 
	occured(newArrival(1),I),holdsat(agentType(1,normal),I),holdsat(endOfQueue(p1),I),holdsat(pow(queue,iniMove(1,p1)),I),instant(I).
occured(iniMove(1,p2),I) :- 
	occured(newArrival(1),I),holdsat(agentType(1,normal),I),holdsat(endOfQueue(p2),I),holdsat(pow(queue,iniMove(1,p2)),I),instant(I).
occured(iniMove(2,p1),I) :- 
	occured(newArrival(2),I),holdsat(agentType(2,normal),I),holdsat(endOfQueue(p1),I),holdsat(pow(queue,iniMove(2,p1)),I),instant(I).
occured(iniMove(2,p2),I) :- 
	occured(newArrival(2),I),holdsat(agentType(2,normal),I),holdsat(endOfQueue(p2),I),holdsat(pow(queue,iniMove(2,p2)),I),instant(I).
%
%Translation of: 
% move(Agent,Position2) generates iniMoved(Agent,Position2) if ;
% 
occured(iniMoved(1,p1),I) :- 
	occured(move(1,p1),I),holdsat(pow(queue,iniMoved(1,p1)),I),instant(I).
occured(iniMoved(1,p2),I) :- 
	occured(move(1,p2),I),holdsat(pow(queue,iniMoved(1,p2)),I),instant(I).
occured(iniMoved(2,p1),I) :- 
	occured(move(2,p1),I),holdsat(pow(queue,iniMoved(2,p1)),I),instant(I).
occured(iniMoved(2,p2),I) :- 
	occured(move(2,p2),I),holdsat(pow(queue,iniMoved(2,p2)),I),instant(I).
%
%Translation of: 
% initially inQueue(1,p1) if ;
% 
initiated(inQueue(1,p1),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially endOfQueue(p2),agentType(1,normal),agentType(2,normal) if ;
% 
initiated(endOfQueue(p2),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(agentType(1,normal),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(agentType(2,normal),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially pow(queue:iniMove(Agent,Position)),pow(queue:iniMoved(Agent,Position)) if ;
% 
initiated(pow(queue,iniMove(1,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMoved(1,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMove(1,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMoved(1,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMove(2,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMoved(2,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMove(2,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniMoved(2,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(iniMove(Agent,Position)),perm(iniMoved(Agent,Position)) if ;
% 
initiated(perm(iniMove(1,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMoved(1,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMove(1,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMoved(1,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMove(2,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMoved(2,p1)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMove(2,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniMoved(2,p2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(newArrival(2)),agentType(2,normal) if ;
% 
initiated(perm(newArrival(2)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
