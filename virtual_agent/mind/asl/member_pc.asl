/*
*	member agent 
*		
*		@author : JeeHang lee
*		@date :  
*/

/* 
 * Initial beliefs and rules
 */
 territory(social).
 friend(bathuni1).
 see(guide, nohannara).
 
/* 
 * Rules
 */
 
 has_seen(ROLE) :-
 	.count(see(ROLE, NAME), CNT) & 
 	CNT > 0.
 	
my_friend(NAME) :-
	friend(M1) & NAME = M1.
 	
// Initial goal
!verify.
	
/* 
 * Plans
 */

/* Belief Addition */
+see(ROLE, NAME) 
	: not has_seen(ROLE, NAME) & ROLE = guide & not my_friend(NAME)
	<- !join_group(ROLE, NAME)[priority(100), deadline(40)].
	
+see(ROLE, NAME) 
	: not has_seen(ROLE, NAME) & not ROLE = guide & my_friend(NAME)
	<- .count(leader(GUIDE), CNT);
	!escape_group(guide, GUIDE);
	!join_group(ROLE, NAME).
	
+emergency
	: true
	<- !exit_building[priority(20), deadline(50)].
		
/* Goal Addition */
@plan_join_group_1[duration(30)]
+!join_group(ROLE, NAME)	
	: see(ROLE, NAME) & has_seen(ROLE)
	<- .print("join_group"); 
	+territory(social);
	+leader(NAME);
	update_speed(low);
	moveto(NAME); 
	follow(NAME);
	update_ipd(none).

@plan_join_group_2[duration(60)]
+!join_group(ROLE, NAME) 
	: not ROLE = guide & see(ROLE, NAME)
	<- +territory(none);
	+leader(NAME);
	update_speed(low);
	follow(NAME);
	update_ipd(none).

@plan_escape_group_3[duration(30)]
+!escape_group(ROLE, NAME)[priority(5), deadline(60)] 
	: ROLE = guide & has_seen(guide)
	<- -see(ROLE, NAME);
	-leader(NAME);
	update_speed(low);
	follow(none);
	update_ipd(social).
	
@plan_escape_building_1[duration(30)]
+!exit_building
	<- update_ipd(none);
	update_speed(fast);
	moveto(exit).
	
@plan_escape_building_2[duration(30)]
+!exit_building
	: .count(leader(G), CNT) & CNT > 0 &
	has_seen(guide) & .count(see(guide, NAME), N) & N > 0 & 
	not emergency
	<- -leader(G); 
	update_ipd(none);
	update_speed(low);
	moveto(exit).
		
+!verify : true
	<- +see(guide, nohannara);
	+emergency.