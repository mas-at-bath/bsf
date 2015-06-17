%:- not final(I),not changed(I),instant(I).
:- not valid(I), instant(I).
valid(I):- changed(I), instant(I).
valid(I):- final(I), instant(I).
%valid(I):-  next(J,I), instant(I),instant(J), occured(E,I),evtype(E,viol).
%:- changed(I), final(I),instant(I).
changed(I):- initiated(F,I),ifluent(F),instant(I),not holdsat(F,I),not final(I).
changed(I):- terminated(F,I),ifluent(F),instant(I),holdsat(F,I),not final(I).

event(epsilon).
evtype(epsilon,ex).
%occured(epsilon,I):- next(I,F),final(F), not changed(I), instant(I).
valid(I):- occured(epsilon,I),instant(I).
allok:- occured(E,I),instant(I),evtype(E,ex), E!=epsilon.
:- not allok.
% an epsilon may only be placed at the end of the transition
:- occured(epsilon,I), next(I,F), not final(F),instant(I),instant(F),not occured(epsilon,F).




