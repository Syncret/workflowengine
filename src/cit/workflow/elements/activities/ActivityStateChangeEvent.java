/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.util.EventObject;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ActivityStateChangeEvent extends EventObject {
	
	private AbstractActivity activity;
	private String fromState;
	private String toState; 
	
	public ActivityStateChangeEvent(AbstractActivity activity, String fromState, String toState) {
		super(activity);
		
		this.activity = activity;
		this.fromState = fromState;
		this.toState = toState;
	}
	
	/* (non-Javadoc)
	 * @see java.util.EventObject#getSource()
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return
	 */
	public AbstractActivity getActivity() {
		return activity;
	}

	/**
	 * @return
	 */
	public String getFromState() {
		return fromState;
	}

	/**
	 * @return
	 */
	public String getToState() {
		return toState;
	}
	
	public String toString() {
		return "ProcessID: " + activity.getProcess().getProcessID() + ", ActivityID: " + activity.getActivityID() + ", State changed from " + fromState + " to " + toState + ".";
	}
}
