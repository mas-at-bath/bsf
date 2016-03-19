contract Obligation {
    
    bytes private subject;
    bytes private content;
    bytes private deadline;
    bytes private institutionName;
    bytes private violation;
    
    function Obligation(bytes subj, bytes cont, bytes dline, bytes inst, bytes vio)
    {
        subject=subj;
        content=cont;
        deadline=dline;
        institutionName=inst;
        violation=vio;
    }
    
	function getSubject() constant returns (bytes) {
		return subject;
	}
	
	function getContent() constant returns (bytes) {
		return content;
	}
	
	function getDeadline() constant returns (bytes) {
		return deadline;
	}
	
	function getInstName() constant returns (bytes) {
		return institutionName;
	}
	
	function getViolation() constant returns (bytes) {
		return violation;
	}
}
