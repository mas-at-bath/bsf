observed(createQueue,t0).
observed(newArrival(agent,normal),t1).

temp(obl(E,D,V)):- ifluent(obl(E,D,V)).

#hide.
%#show occured(X,I).
#show holdsat(F,I):temp(F). 


