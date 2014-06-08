/*
 * Created on 2004-10-20
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.eca;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cit.workflow.elements.Event;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EventQueue {
	
	private static Logger logger = Logger.getLogger(EventQueue.class);
	
	private List eventList;
	
	public EventQueue() {
		this.eventList = new ArrayList();
	}
	
	public void push(Event event){
		
		if (logger.isInfoEnabled())
			logger.info("PUSH EVENT----" + event);
			
		eventList.add(event);
	}
	
	public Event pop() throws IndexOutOfBoundsException {
	
		Event event = (Event) eventList.remove(0);
		
		if (logger.isInfoEnabled())
			logger.info("POP EVENT----" + event);
			
		return event;
	}
	
	public boolean IsEmpty() {
		return eventList.size() == 0 ? true : false;
	}
}
