/*
 * Created on 2004-10-17
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Task implements Serializable {
	
	private String processID;
	private String processName;
	private int activityID;
	//sxh add 2007.10
	private int roleID;
	//sxh add 2007.10 end
	private String activityName;
	private Date definedStartDate;
	private Date definedEndDate;
	private String state;
	private String allocateState;
	
	//sxh add 2007
	public Task() {
		
	}
	//sxh add 2007 end
	
	/**added by dy
	 * isBind is used for identicated the state of task
	 * 1---bind to person
	 * 0---not bind to person
	 */
	private boolean isBind;
	
	public boolean getIsBind(){
		return isBind;
	}
	public void setisBind(boolean isbind){
		isBind = isbind;
	}
	
	/**
	 * @return
	 */
	public int getActivityID() {
		return activityID;
	}

	/**
	 * @return
	 */
	public Date getDefinedEndDate() {
		return definedEndDate;
	}

	/**
	 * @return
	 */
	public Date getDefinedStartDate() {
		return definedStartDate;
	}

	/**
	 * @return
	 */
	public String getProcessID() {
		return processID;
	}

	/**
	 * @return
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * @return
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param i
	 */
	public void setActivityID(int i) {
		activityID = i;
	}

	/**
	 * @param date
	 */
	public void setDefinedEndDate(Date date) {
		definedEndDate = date;
	}

	/**
	 * @param date
	 */
	public void setDefinedStartDate(Date date) {
		definedStartDate = date;
	}

	/**
	 * @param i
	 */
	public void setProcessID(String i) {
		processID = i;
	}

	/**
	 * @param string
	 */
	public void setProcessName(String string) {
		processName = string;
	}

	/**
	 * @param string
	 */
	public void setState(String string) {
		state = string;
	}

	/**
	 * @return
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * @param string
	 */
	public void setActivityName(String string) {
		activityName = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Task) {
			Task task = (Task) obj;
			return this.processID.equals(task.getProcessID()) && this.activityID == task.getActivityID();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer("Task: ");
		result.append(processID)
				.append(", ActivityID: ")
				.append(activityID)
				.append(", Activity: ")
				.append(activityName)
				.append(", roleID: ")
				.append(roleID)
				.append(", definedStartDate: ")
				.append(definedStartDate)
				.append(", definedEndDate: ")
				.append(definedEndDate)
				.append(", State: ")
				.append(state)
				.append(", allocateState: ")
				.append(allocateState);
		return result.toString();
	}
	
	//sxh add 2007.10
	public String getAllocateState() {
		return allocateState;
	}
	public void setAllocateState(String allocateState) {
		this.allocateState = allocateState;
	}

	public int getRoleID() {
		return roleID;
	}
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	//sxh add 2007.10 end
}
