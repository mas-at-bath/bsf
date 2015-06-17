
{eobserved(E,I)}:- evtype(E,env),instant(I), next(I,J) .

ev(I):-eobserved(E,I),evtype(E,env),instant(I).
:-not ev(I),next(I,J).
:- eobserved(E1,I),eobserved(E2,I),E1!=E2, 
   instant(I),evtype(E1,env),evtype(E2,env).
observed(E,I):- eobserved(E,I),event(E),instant(I).