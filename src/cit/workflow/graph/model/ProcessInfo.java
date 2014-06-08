package cit.workflow.graph.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProcessInfo {

	  
	private int workflowID;
	
	private int packageID;
	
	private String processID;
	
	private List flowSet; // 锟斤拷锟斤拷锟斤拷<LinkFlow>

	private List activitySet; // 锟筋动<BaseActivityInfo>
	
	private List logicNodeSet; // 锟竭硷拷锟节碉拷<LogicNode>

	public List getActivitySet() {
		return activitySet;
	}

	public void setActivitySet(List activitySet) {
		this.activitySet = activitySet;
	}

	public List getFlowSet() {
		return flowSet;
	}

	public void setFlowSet(List flowSet) {
		this.flowSet = flowSet;
	}

	public List getLogicNodeSet() {
		return logicNodeSet;
	}

	public void setLogicNodeSet(List logicNodeSet) {
		this.logicNodeSet = logicNodeSet;
	}

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public int getWorkflowID() {
		return workflowID;
	}

	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}

	public int getPackageID() {
		return packageID;
	}

	public void setPackageID(int packageID) {
		this.packageID = packageID;
	}
	

}
