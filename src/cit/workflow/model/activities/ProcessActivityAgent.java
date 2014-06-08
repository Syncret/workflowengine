package cit.workflow.model.activities;

import java.util.Date;
import java.util.Map;

public class ProcessActivityAgent {
	private String processID;
	private int activityID;
	private int agentID;
	private int capabilityID;
	private int repeatedTime;
	private Date definedStartDate;
	private Date actualStartDate;
	private Date definedEndDate;
	private Date actualEndDate;
	private int definedWorkload;
	private int actualWorkload;
	private String state;
	public int getActivityID() {
		return activityID;
	}
	public void setActivityID(int activityID) {
		this.activityID = activityID;
	}
	public Date getActualEndDate() {
		return actualEndDate;
	}
	public void setActualEndDate(Date actualEndDate) {
		this.actualEndDate = actualEndDate;
	}
	public Date getActualStartDate() {
		return actualStartDate;
	}
	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}
	public int getActualWorkload() {
		return actualWorkload;
	}
	public void setActualWorkload(int actualWorkload) {
		this.actualWorkload = actualWorkload;
	}
	public int getAgentID() {
		return agentID;
	}
	public void setAgentID(int agentID) {
		this.agentID = agentID;
	}
	public int getCapabilityID() {
		return capabilityID;
	}
	public void setCapabilityID(int capabilityID) {
		this.capabilityID = capabilityID;
	}
	public Date getDefinedEndDate() {
		return definedEndDate;
	}
	public void setDefinedEndDate(Date definedEndDate) {
		this.definedEndDate = definedEndDate;
	}
	public Date getDefinedStartDate() {
		return definedStartDate;
	}
	public void setDefinedStartDate(Date definedStartDate) {
		this.definedStartDate = definedStartDate;
	}
	public int getDefinedWorkload() {
		return definedWorkload;
	}
	public void setDefinedWorkload(int definedWorkload) {
		this.definedWorkload = definedWorkload;
	}
	public String getProcessID() {
		return processID;
	}
	public void setProcessID(String processID) {
		this.processID = processID;
	}
	public int getRepeatedTime() {
		return repeatedTime;
	}
	public void setRepeatedTime(int repeatedTime) {
		this.repeatedTime = repeatedTime;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public ProcessActivityAgent() {
		processID = "";
		activityID = -1;
		agentID = -1;
		capabilityID = -1;
		repeatedTime = -1;
		definedStartDate = null;
		actualStartDate = null;
		definedEndDate = null;
		actualEndDate = null;
		definedWorkload = -1;
		actualWorkload = -1;
		state = "";
	}
	public ProcessActivityAgent(Map map) {
		if (map.get("processID") != null) {
			processID = (String)map.get("processID");
		}
		if (map.get("activityID") != null) {
			activityID = (Integer)map.get("activityID");
		}
		if (map.get("agentID") != null) {
			agentID = (Integer)map.get("agentID");
		}
		if (map.get("capabilityID") != null) {
			capabilityID = (Integer)map.get("capabilityID");
		}
		if (map.get("repeatedTime") != null) {
			repeatedTime = (Integer)map.get("repeatedTime");
		}
		if (map.get("definedStartDate") != null) {
			definedStartDate = (Date)map.get("definedStartDate");
		}
		if (map.get("actualStartDate") != null) {
			actualStartDate = (Date)map.get("actualStartDate");
		}
		if (map.get("definedEndDate") != null) {
			definedEndDate = (Date)map.get("definedEndDate");
		}
		if (map.get("definedStartDate") != null) {
			definedStartDate = (Date)map.get("definedStartDate");
		}
		if (map.get("actualEndDate") != null) {
			actualEndDate = (Date)map.get("actualEndDate");
		}
		if (map.get("definedWorkload") != null) {
			definedWorkload = (Integer)map.get("definedWorkload");
		}
		if (map.get("actualWorkload") != null) {
			actualWorkload = (Integer)map.get("actualWorkload");
		}
		if (map.get("state") != null) {
			state = (String)map.get("state");
		}
	}
}
