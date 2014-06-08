/*
 * Created on 2005-5-9
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package load;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ElementFactory {
    
    private static Map loadActivityCache = new HashMap();
    
	public static WorkflowEvent createWorkflowEvent(Connection conn, int workflowID, int activityID, String fromState, String toState) throws Exception {
		StringBuffer eventRepresentation = new StringBuffer();
		if (toState.equals(Constants.ACTIVITY_STATE_READY)) {
			eventRepresentation.append(Constants.EVENT_INITIALIZE);
		} else if (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) {
			eventRepresentation.append(Constants.EVENT_BEIGINOF);
		} else if (toState.equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			eventRepresentation.append(Constants.EVENT_ENDOF);
		} else {
			throw new Exception("No this event type!");
		}
		eventRepresentation.append("(").append(activityID).append(")");
		
		WorkflowEvent event = new WorkflowEvent();
		event.setWorkflowID(workflowID);
		event.setEventID(-1);
		event.setEventRepresentation(eventRepresentation.toString());
		
		return formatEvent(conn, event);
	}
	
	public static WorkflowEvent createWorkflowEvent(Connection conn, int workflowID, String eventRepresentation) throws Exception {
	    WorkflowEvent event = new WorkflowEvent();
	    event.setWorkflowID(workflowID);
		event.setEventID(-1);
		event.setEventRepresentation(eventRepresentation);
		return formatEvent(conn, event);
	}
	
	public static WorkflowEvent createWorkflowEvent(int workflowID, int eventID, String eventRepresentation) {
	    WorkflowEvent event = new WorkflowEvent();
	    event.setWorkflowID(workflowID);
		event.setEventID(eventID);
		event.setEventRepresentation(eventRepresentation);
		return event;
	}
	
	public static WorkflowEvent createWorkflowEvent(Connection conn, int workflowID, int eventID) throws Exception {
	    WorkflowEvent event = new WorkflowEvent();
	    event.setWorkflowID(workflowID);
		event.setEventID(eventID);
		event.setEventRepresentation("");
		return formatEvent(conn, event);
	}
	
	private static WorkflowEvent formatEvent(Connection conn, WorkflowEvent event) throws Exception {
		
		QueryRunner queryRunner = new QueryRunner();
		String sql = null;
		Object[] params = null;
		Map workflowEventMap = null;
		
		if (event.getEventID() == -1) {

			sql = "SELECT * FROM WorkflowEvent WHERE WorkflowID=? AND EventRepresentation=?";
			params = new Object[]{Integer.valueOf(event.getWorkflowID()), event.getEventRepresentation()};
			workflowEventMap = (Map) queryRunner.query(conn, sql, params, new MapHandler());			
			if ( workflowEventMap != null ) {
				event.setEventID(((Integer) workflowEventMap.get("EventID")).intValue());
			}
		} 
		
		if ( event.getEventRepresentation().equals("") ) {
			
			sql = "SELECT * FROM WorkflowEvent WHERE WorkflowID=? AND EventID=?";
			params = new Object[] {Integer.valueOf(event.getWorkflowID()), new Integer(event.getEventID())};
			workflowEventMap = (Map) queryRunner.query(conn, sql, params, new MapHandler());			
			if ( workflowEventMap != null ) {
				event.setEventRepresentation((String) workflowEventMap.get("EventRepresentation"));
			}		
		}
		return event;
	}
    
    public static LoadActivity createLoadActivity(int workflowID, int activityID, int implementationType) {
        String activityKey = workflowID + "-" + activityID;
        if (!loadActivityCache.containsKey(activityKey))
            loadActivityCache.put(activityKey, new LoadActivity(workflowID, activityID, implementationType));
        LoadActivity activity = (LoadActivity) loadActivityCache.get(activityKey);
        activity.reset();
        return activity;
    }
    
    public static void clearLoadActivityCache() {
        loadActivityCache.clear();
    }
}
