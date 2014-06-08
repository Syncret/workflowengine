package cit.workflow.engine.droolsruleengine;


//lrj add begin 07-12-7
public class ExtendedDroolsRule 
{
	private String processID;
	private int eventID;
	private String ruleContent;
	private int ruleFileID;
	
	public ExtendedDroolsRule() 
	{
	}
	
	public void setProcessID(String processID) {
	  this.processID = processID;
	}
	
	public void setEventID(int eventID) {
	  this.eventID = eventID;
	}
	
	public void setRuleContent(String ruleContent) {
	  this.ruleContent = ruleContent;
	}
	
	public void setRuleFileID(int ruleFileID) {
	  this.ruleFileID = ruleFileID;
	}
	
	
	public String getProcessID() {
	  return this.processID;
	}
	
	public int getEventID() {
	  return this.eventID;
	}
	
	public String getRuleContent() {
	  return this.ruleContent;
	}
	
	public int getRuleFileID() {
	  return this.ruleFileID;
	}
	
}

// lrj add end 07-12-7