/*
 * Created on 2004-10-17
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Event {
	
	private Process process;
	
	private int eventID;
	
	//长度为1的字符串数组
	private String eventRepresentation;
	
	public Event() {
		eventRepresentation = "";
	}

	/**
	 * @return
	 */
	public int getEventID() {
		return eventID;
	}

	/**
	 * @return
	 */
	public String getEventRepresentation() {
		return eventRepresentation;
	}

	/**
	 * @param i
	 */
	public void setEventID(int i) {
		eventID = i;
	}

	/**
	 * @param strings
	 */
	public void setEventRepresentation(String string) {
		eventRepresentation = string;
	}
	/**
	 * @return
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @param process
	 */
	public void setProcess(Process process) {
		this.process = process;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ProcessID: " + process.getProcessID() + ", EventID: " + eventID + ", Expression: " + eventRepresentation + ".";
	}

}
