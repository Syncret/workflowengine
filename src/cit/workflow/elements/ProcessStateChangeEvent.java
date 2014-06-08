/*
 * Created on 2004-11-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements;

import java.util.EventObject;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProcessStateChangeEvent extends EventObject {

	private Process process;
	private String fromState;
	private String toState; 
	
	public ProcessStateChangeEvent(Process process, String fromState, String toState) {
		super(process);
		
		this.process = process;
		this.fromState = fromState;
		this.toState = toState;
	}
	
	/**
	 * @return Returns the fromState.
	 */
	public String getFromState() {
		return fromState;
	}
	/**
	 * @return Returns the process.
	 */
	public Process getProcess() {
		return process;
	}
	/**
	 * @return Returns the toState.
	 */
	public String getToState() {
		return toState;
	}
	/* (non-Javadoc)
	 * @see java.util.EventObject#getSource()
	 */
	public Object getSource() {
		// TODO Auto-generated method stub
		return super.getSource();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ProcessID: " + process.getProcessID() + ", State changed from " + fromState + " to " + toState + ".";
	}
}
