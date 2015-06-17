// Agent Institutional Robot

/*
*	Virtual environment Bot governed by Institution 
*	- Queuing and making a space whenever the old or the disabled turns up
*	- For example, this robot goes to particular position, and if there is a queue,
*	then queuing up. If the old or disabled approaches towards them, then agents
*	start to make a space for the person. 
*		
*		@author : JeeHang lee
*		@date : 23. Apr. 2012 
*/

/* Initial beliefs and rules */

// No avatar on this position

/* Initial goals */
// !greeting.

/* Plans */

// An avatar is ready to move 
+ready : true
	<- !query_state.
	
+detect(disabled) : true
	<- !query_state.

// subgoal : move
+!query_state : ready
	<- update(new_arrival).
	
+!query_state : detect(disabled) 
	<- update(disabled).
	
// norms                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
+obl(queue_last) : true
	<-  move_origin.
	
+obl(yield) : true
	<- !make_space.
		
+viol 
	<- true.

+!make_space : obl(yield)
	<- !yield.

+!make_space : obl(yield) & viol
	<- true.

+!yield : not viol
	<- back.	
	
// Greetings
+hello : true
	<- !greeting.
	
+!greeting : hello
	<- bow;
	   !greeting.