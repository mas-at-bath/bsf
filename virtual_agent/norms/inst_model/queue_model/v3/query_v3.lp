observed(createQueue,t0).
observed(newArrival(3),t1).
observed(agentType(3,disable),t1).
observed(agentType(3,disable),t2).


temp(obl(E,D,V)):- ifluent(obl(E,D,V)).

#hide.
%#show occured(X,I).
#show holdsat(F,I):temp(F). 


