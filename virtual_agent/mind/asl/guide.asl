/*
*	Guide agent 
*		
*		@author : JeeHang lee
*		@date :  
*/

/* 
 * Initial beliefs and rules
 */
member(bathuni1, bathuni2, bathuni3).
guide_limit(member, 3).
territory(social).

/* 
 * Rules
 */

too_much(M) :-
	.count(add(YY, MM, DD, HH, NN, SS, M, NAME), CNT) &
	guide_limit(M, LIMIT) &
	CNT >= Limit.
	
my_member(NAME) :-
	member(M1, M2, M3) &
	((NAME = M1) | (NAME = M2) | (NAME = M3)).
	
has_member(NAME) :-
	.count(add(YY, MM, DD, HH, NN, SS, member, NAME), CNT) & 
	.print(NAME, ": found ", CNT, " item(s)") &
	CNT > 0.
	
/*
 * Initial goal
 */

// unit test code
!verify_join(bathuni1, bathuni2, bathuni3).

/* 
 * Plans
 */

/* Belief Addition */
+join(NAME) : not too_much(member) & my_member(NAME) & not has_member(NAME)
	<- .date(YY, MM, DD); .time(HH, NN, SS); 
	+add(YY, MM, DD, HH, NN, SS, member, NAME);
//	.print("Avatar", NAME, "is added.");
//	+left(NAME);	// test
	!update_territory_mode.

+left(NAME) : my_member(NAME) & has_member(NAME)
	<- -add(YY, MM, DD, HH, NN, SS, member, NAME);
//	.print("Avatar", NAME, "is left.");
	!update_territory_mode.
	
+fire_alarm : true
	<- escape(exit).
		
/* Goal Addition */
+!update_territory_mode	: territory(T) & .count(add(YY, MM, DD, HH, NN, SS, member, NAME), CNT) & 
						guide_limit(member, LIMIT) & CNT <= LIMIT &	CNT > 0
	<- +territory(public);
	update_ipd(public).

+!update_territory_mode	: territory(T) & //T = public &
						.count(add(YY, MM, DD, HH, NN, SS, member, NAME), CNT) & CNT < 1
	<- +territory(social); 
	update_ipd(social).
		
// unit test code : verify
+!verify_join(N1, N2, N3) : true
	<- +join(N1);+join(N2);+join(N3).
