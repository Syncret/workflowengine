package cit.workflow.exception;


public class WorkflowSubmitException extends Exception {
	
	public WorkflowSubmitException() {
		super("Workflow Submit Exception!");
	}	

	public WorkflowSubmitException(Exception e) {
		super(e.toString());
	}		
	
	public WorkflowSubmitException(String msg) {
		super(msg);
	}
}
