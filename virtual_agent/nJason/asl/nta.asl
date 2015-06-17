/* Initial beliefs and rules */

/* Initial goals */

!at(100, 100)[priority(100)].
!start(10)[priority(50)].
!free(10)[priority(10)].

/* plans */
+!start(0).
@p1[duration(100)]
+!start(X)//[priority(50)]
	<- .print(start(X));
	!start(X-1)[priority(50),deadline(500)].
	
+!free(0).
@p2[duration(800)]
+!free(X)[priority(10)]
	<- .print(free(X));
	!free(X-1)[priority(10),deadline(1000)].

+!at(0, 0).
@p3[duration(200)]
+!at(X,Y)//[priority(100)] 
	<- .print(at(X,Y));
	moveTo(X,Y);
	!at(X-1, Y-1)[priority(100),deadline(1000)].

/* Plan Library */
@p4[duration(200)]
+!greeting[priority(10), deadline(1000)] : true
	<- bow;
	!greeting.

@p5[duration(200)]
+!back(X,Y)
	<- .print(goback(X,Y));	
	goback.
	
//@p6[duration(200)]
//+obl(back(X,Y),DL,Viol)
//	<- !back(X,Y)[priority(Viol),deadline(DL)].

/*
+obl(at(X,Y),DL,violation): X > 0 & Y > 0 & DL > 0
	<- !at(X,Y)[deadline(DL),priority(violation)].
	
+obl(at(X,Y),DL,violation) : X > 0 & Y > 0 & DL > 0
	<- !greeting.
*/

+!shout : true
	<- .print("this is just simple shout").

