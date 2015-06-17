ifluent(perm(iniOblYield(agent))).
ifluent(perm(yield(agent))).
ifluent(live(queue)).
ifluent(perm(deadline)).
ifluent(pow(queue,iniOblQueueLast(agent,disable))).
ifluent(perm(queueLast(agent))).
ifluent(perm(newArrival(agent,normal))).
ifluent(perm(iniOblQueueLast(agent,normal))).
ifluent(perm(detectDisable(agent))).
ifluent(pow(queue,iniOblQueueLast(agent,normal))).
ifluent(obl(yield(agent),deadline,vioYield(agent))).
ifluent(perm(newArrival(agent,disable))).
ifluent(pow(queue,iniOblYield(agent))).
ifluent(obl(queueLast(agent),deadline,vioQueue(agent))).
ifluent(perm(iniOblQueueLast(agent,disable))).
event(iniOblYield(agent)).
event(viol(iniOblYield(agent))).
event(deadline).
event(viol(deadline)).
event(iniOblQueueLast(agent,disable)).
event(viol(iniOblQueueLast(agent,disable))).
event(yield(agent)).
event(viol(yield(agent))).
event(newArrival(agent,normal)).
event(viol(newArrival(agent,normal))).
event(createQueue).
event(viol(createQueue)).
event(queueLast(agent)).
event(viol(queueLast(agent))).
event(detectDisable(agent)).
event(viol(detectDisable(agent))).
event(iniOblQueueLast(agent,normal)).
event(viol(iniOblQueueLast(agent,normal))).
event(newArrival(agent,disable)).
event(viol(newArrival(agent,disable))).
evtype(iniOblYield(agent),inst).
evinst(iniOblYield(agent),queue).
evtype(viol(iniOblYield(agent)),viol).
evinst(viol(iniOblYield(agent)),queue).
evtype(deadline,ex).
evinst(deadline,queue).
evtype(viol(deadline),viol).
evinst(viol(deadline),queue).
evtype(iniOblQueueLast(agent,disable),inst).
evinst(iniOblQueueLast(agent,disable),queue).
evtype(viol(iniOblQueueLast(agent,disable)),viol).
evinst(viol(iniOblQueueLast(agent,disable)),queue).
evtype(yield(agent),ex).
evinst(yield(agent),queue).
evtype(viol(yield(agent)),viol).
evinst(viol(yield(agent)),queue).
evtype(newArrival(agent,normal),ex).
evinst(newArrival(agent,normal),queue).
evtype(viol(newArrival(agent,normal)),viol).
evinst(viol(newArrival(agent,normal)),queue).
evtype(createQueue,ex).
evinst(createQueue,queue).
evtype(viol(createQueue),viol).
evinst(viol(createQueue),queue).
evtype(queueLast(agent),ex).
evinst(queueLast(agent),queue).
evtype(viol(queueLast(agent)),viol).
evinst(viol(queueLast(agent)),queue).
evtype(detectDisable(agent),ex).
evinst(detectDisable(agent),queue).
evtype(viol(detectDisable(agent)),viol).
evinst(viol(detectDisable(agent)),queue).
evtype(iniOblQueueLast(agent,normal),inst).
evinst(iniOblQueueLast(agent,normal),queue).
evtype(viol(iniOblQueueLast(agent,normal)),viol).
evinst(viol(iniOblQueueLast(agent,normal)),queue).
evtype(newArrival(agent,disable),ex).
evinst(newArrival(agent,disable),queue).
evtype(viol(newArrival(agent,disable)),viol).
evinst(viol(newArrival(agent,disable)),queue).
%
%causal rules for obl(yield(agent),deadline,vioYield(agent))
occured(vioYield(agent),I) :- 
	holdsat(obl(yield(agent),deadline,vioYield(agent)),I),occured(deadline,I),instant(I).
terminated(obl(yield(agent),deadline,vioYield(agent)),I) :- 
	holdsat(obl(yield(agent),deadline,vioYield(agent)),I),occured(deadline,I),instant(I).
terminated(obl(yield(agent),deadline,vioYield(agent)),I) :- 
	holdsat(obl(yield(agent),deadline,vioYield(agent)),I),occured(yield(agent),I),instant(I).
%
%causal rules for obl(queueLast(agent),deadline,vioQueue(agent))
occured(vioQueue(agent),I) :- 
	holdsat(obl(queueLast(agent),deadline,vioQueue(agent)),I),occured(deadline,I),instant(I).
terminated(obl(queueLast(agent),deadline,vioQueue(agent)),I) :- 
	holdsat(obl(queueLast(agent),deadline,vioQueue(agent)),I),occured(deadline,I),instant(I).
terminated(obl(queueLast(agent),deadline,vioQueue(agent)),I) :- 
	holdsat(obl(queueLast(agent),deadline,vioQueue(agent)),I),occured(queueLast(agent),I),instant(I).
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
% iniOblQueueLast(Agent,Type) initiates perm(queueLast(Agent)),obl(queueLast(Agent),deadline,vioQueue(Agent)) if ;
% 
initiated(perm(queueLast(agent)),I) :- 
	occured(iniOblQueueLast(agent,disable),I),holdsat(live(queue),I),instant(I).
initiated(obl(queueLast(agent),deadline,vioQueue(agent)),I) :- 
	occured(iniOblQueueLast(agent,disable),I),holdsat(live(queue),I),instant(I).
initiated(perm(queueLast(agent)),I) :- 
	occured(iniOblQueueLast(agent,normal),I),holdsat(live(queue),I),instant(I).
initiated(obl(queueLast(agent),deadline,vioQueue(agent)),I) :- 
	occured(iniOblQueueLast(agent,normal),I),holdsat(live(queue),I),instant(I).
%
%Translation of: 
% iniOblYield(Agent) initiates perm(yield(Agent)),obl(yield(Agent),deadline,vioYield(Agent)) if ;
% 
initiated(perm(yield(agent)),I) :- 
	occured(iniOblYield(agent),I),holdsat(live(queue),I),instant(I).
initiated(obl(yield(agent),deadline,vioYield(agent)),I) :- 
	occured(iniOblYield(agent),I),holdsat(live(queue),I),instant(I).
%
%Translation of: 
% newArrival(Agent,Type) generates iniOblQueueLast(Agent,Type) if ;
% 
occured(iniOblQueueLast(agent,disable),I) :- 
	occured(newArrival(agent,disable),I),holdsat(pow(queue,iniOblQueueLast(agent,disable)),I),instant(I).
occured(iniOblQueueLast(agent,normal),I) :- 
	occured(newArrival(agent,normal),I),holdsat(pow(queue,iniOblQueueLast(agent,normal)),I),instant(I).
%
%Translation of: 
% detectDisable(Agent) generates iniOblYield(Agent) if ;
% 
occured(iniOblYield(agent),I) :- 
	occured(detectDisable(agent),I),holdsat(pow(queue,iniOblYield(agent)),I),instant(I).
%
%Translation of: 
% initially perm(deadline) if ;
% 
initiated(perm(deadline),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(iniOblQueueLast(Agent,Type)),pow(queue:iniOblQueueLast(Agent,Type)) if ;
% 
initiated(perm(iniOblQueueLast(agent,disable)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniOblQueueLast(agent,disable)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(iniOblQueueLast(agent,normal)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniOblQueueLast(agent,normal)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(iniOblYield(Agent)),pow(queue:iniOblYield(Agent)) if ;
% 
initiated(perm(iniOblYield(agent)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(pow(queue,iniOblYield(agent)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(newArrival(Agent,Type)) if ;
% 
initiated(perm(newArrival(agent,disable)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
initiated(perm(newArrival(agent,normal)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
%
%Translation of: 
% initially perm(detectDisable(Agent)) if ;
% 
initiated(perm(detectDisable(agent)),I) :- 
	occured(createQueue,I),not holdsat(live(queue),I),instant(I).
