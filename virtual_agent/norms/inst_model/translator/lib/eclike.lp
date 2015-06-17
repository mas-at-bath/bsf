


terminated_in(F,I,J):-
	instant(K),
	terminated(F,K),
	before(K,J),
	before(I,K).
	


holdsat(F,J):- initiated(F,I),next(I,J).

holdsat(F,J):- holdsat(F,I),
	       not terminated_in(F,I,J),
	       instant(I), instant(J).

% All of the action rules are the same  as in basic. 
occured(E,I):- evtype(E,obs),observed(E,I),instant(I).

occured(V,I):- holdsat(obl(E,D,V),I),occured(D,I),
    event(E;D;V),instant(I).

terminated(obl(E,D,V),I):- occured(E,I),
    holdsat(obl(E,D,V),I),
    event(E;D;V),instant(I).

terminated(obl(E,D,V),I):- occured(D,I),
    holdsat(obl(E,D,V),I),
    event(E;D;V),instant(I).

occured(viol(E),I):- 
    occured(E,I),
    evtype(E,obs),
    not holdsat(perm(E),I),
    event(E),instant(I).

occured(viol(E),I):- 
    occured(E,I),
    evtype(E,inst),
    not holdsat(perm(E),I),
    event(E),instant(I).

before(I1,I2):- next(I1,I2),instant(I1), instant(I2).
% 1.12.2
before(I1,I3):- before(I1,I2),before(I2,I3),instant(I1),instant(I2),instant(I3).

% we can pick any ordering for insants the only condition is that we
% cannot have I before itself

{next(I1,I2)}:- I1!=I2,instant(I1,I2), not before(I2,I1).
		