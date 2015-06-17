package edu.bath.institution;

public class Test {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
				
		///---------------------------------------------------------------------------------
		// Initialise the governor only once in the beginning of the simulation.
		//---------------------------------------------------------------------------------
		ReturnCode rc; 
		//Governor governor = new Governor("grid.ial", "time.lp", "domain.idc", "results.txt");		
		Governor governor = new Governor("grid.ial");
		//
		// EXAMPLE : Set the initial state in ial, specify the domain specification and create a new Contract.
		// 		     Add the contract in the Governor and updateState with an event.
		//
		
		String initialState = "pow(transition), perm(transition), perm(clock),"+
		          "pow(intDownload(A,B,C)),perm(intDownload(A,B,C))," +
		          "perm(download(alice,x1,C)), perm(download(alice,x3,C))," +
		          "perm(download(bob,x2,C)), perm(download(bob,x4,C))," +
		          "downloadChunk(alice,x1), downloadChunk(alice,x3)," +
			      "downloadChunk(bob,x2), downloadChunk(bob,x4)," +
                  "pow(transition), perm(transition),"+
		          "perm(clock),"+
		          "pow(intReceive(Handset,Chunk)), pow(intSend(Handset)),"+
		          "perm(send(Handset,Chunk)),pow(intSend(Handset)),"+
		          "perm(intReceive(Handset,Chunk)), perm(intSend(Handset)),"+
		          "previous(4,3), previous(3,2), previous(2,1)";
			
		String domainSpec = "Handset: alice bob\n" + 
							"Chunk: x1 x2 x3 x4\n" +
							"Channel: c1 c2\n" + 
							"Time: 1 2 3 4";		
				
		Contract contract = new Contract("AG1", initialState, domainSpec);
		governor.addContract(contract);
				
		String event = "clock";		
		System.out.println(event);
		if ((rc = governor.updateState(event, "AG1")) == ReturnCode.FAILURE){
			System.out.println("Failed to update state.");
			return;
		} //else {
			//contract = governor.getContract("AG1");
			//System.out.println(contract.queryObserved("holdsat").toString());
			//System.out.println(contract.queryFluent("perm"));
		//}
		governor.cleanUp();
	}
	
}


