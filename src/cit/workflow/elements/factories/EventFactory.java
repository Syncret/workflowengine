/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.factories;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.Constants;
import cit.workflow.elements.Event;
import cit.workflow.elements.Process;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EventFactory {
	
	public static Event create(Connection conn, Process process, int activityID, String fromState, String toState) throws Exception {
		StringBuffer eventRepresentation = new StringBuffer();
		if (toState.equals(Constants.ACTIVITY_STATE_READY)) {
			eventRepresentation.append(Constants.EVENT_INITIALIZE);
		} else if (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) {
			eventRepresentation.append(Constants.EVENT_BEIGINOF);
		} else if (toState.equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			eventRepresentation.append(Constants.EVENT_ENDOF);
		} else if (toState.equals(Constants.ACTIVITY_STATE_OVERTIME)) {
			eventRepresentation.append(Constants.EVENT_OVERTIME);
		} else if (toState.equals(Constants.ACTIVITY_STATE_ERROR)) {
			eventRepresentation.append(Constants.EVENT_ERROR);
		} else if (toState.equals(Constants.ACTIVITY_STATE_ABORT)) {
			eventRepresentation.append(Constants.EVENT_ABORT);
		} else {
			throw new Exception("No this event type!");
		}
		eventRepresentation.append("(").append(activityID).append(")");
		
		Event event = new Event();
		event.setProcess(process);
		event.setEventID(-1);
		event.setEventRepresentation(new String(eventRepresentation));
		
		return formatEvent(conn, event);
	}
	
	public static Event create(Connection conn, Process process, String eventRepresentation) throws Exception {
		Event event = new Event();
		event.setProcess(process);
		event.setEventID(-1);
		event.setEventRepresentation(eventRepresentation);
		return formatEvent(conn, event);
	}
	
	public static Event create(Process process, int eventID, String eventRepresentation) {
		Event event = new Event();
		event.setProcess(process);
		event.setEventID(eventID);
		event.setEventRepresentation(eventRepresentation);
		return event;
	}
	
	public static Event create(Connection conn, Process process, int eventID) throws Exception {
		Event event = new Event();
		event.setProcess(process);
		event.setEventID(eventID);
		event.setEventRepresentation("");
		return formatEvent(conn, event);
	}
	
	private static Event formatEvent(Connection conn, Event event) throws Exception {
		
		QueryRunner queryRunner = new QueryRunner();
		String sql = null;
		Object[] params = null;
		Map processEventMap = null;
		
		if (event.getEventID() == -1) {

			sql = "SELECT * FROM ProcessEvent WHERE ProcessID=? AND EventRepresentation=?";
			params = new Object[]{event.getProcess().getProcessID(), event.getEventRepresentation()};
			processEventMap = (Map) queryRunner.query(conn, sql, params, new MapHandler());			
			if ( processEventMap != null ) {
				event.setEventID(((Integer) processEventMap.get("EventID")).intValue());
			}
		} 
		
		if ( event.getEventRepresentation().equals("") ) {
			
			sql = "SELECT * FROM ProcessEvent WHERE ProcessID=? AND EventID=?";
			params = new Object[] {event.getProcess().getProcessID(), new Integer(event.getEventID())};
			processEventMap = (Map) queryRunner.query(conn, sql, params, new MapHandler());			
			if ( processEventMap != null ) {
				event.setEventRepresentation((String) processEventMap.get("EventRepresentation"));
			}		
		}
		return event;
	}
}
