:- not valid(I,J),instant(I),next(I,J).

%:- changed(I), final(I),instant(I).
% a state is valid if it changed something from the previous state. 


valid(I,J):- ctrans(I,J),next(I,J).

ctrans(I,J):- ifluent(F), holdsat(F,I), next(I,J), not holdsat(F,J).
ctrans(I,J):- ifluent(F), not holdsat(F,I), next(I,J), holdsat(F,J).


event(epsilon).
evtype(epsilon,obs).
% or if it comes after an epsilon transition

valid(I,J):- etrans(I,J), final(J), next(I,J).
valid(I,J):- etrans(I,J),next(I,J),etrans(J,K),next(J,K). 
etrans(I,J):-observed(epsilon,I),next(I,J),instant(I),instant(J).

%:- etrans(I1), observed(E,I2), next(I1,I2), E!=epsilon,evtype(E,obs), instant(I).

%haseps:- observed(epsilon,I),instant(I).
%:- haseps, not observed(epsilon,I), final(I).
%:- etrans(I),  etrans(J), next(I,J), not final(J).

%:- etrans(I),not etrans(J), not final(J),next(I,J), instant(I),instant(J).


% 0{final(I1)}1:- not changed(I1),final(I2), next(I1,I2), instant(I1), instant(I2).