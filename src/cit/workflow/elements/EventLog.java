/*
 * Created on 2005-1-21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements;

import java.io.Serializable;

/**
 * @author weiwei
 */
public class EventLog implements Serializable {
	public String type;
	public String category;
	public String dateTime;
	public String contents;
	public String computer;
	public String workflowID;
	public String processID;
	public String activityID;
	public String fromState;
	public String toState;
}
