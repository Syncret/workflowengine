/*
 * Created on 2005-5-9
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package load;

import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventLoadAnalyzer extends DBUtility {

    private LinkedList loadActivityStack;
    
    private List eventQueue;
    
    private LoadActivity curLoadActivity;
    private LoadActivity startLoadActivity;
    
    public EventLoadAnalyzer(Connection conn) {
        super(conn);
        
        loadActivityStack = new LinkedList();
        
        eventQueue = new LinkedList();
    }
    
    public void analyzeEventLoad(int workflowID) throws Exception {
        
        //Select all task activities according to the specific workflowID and other conditions
		sql = "SELECT * from WorkflowActivityInformation WHERE WorkflowID=? AND ((ActivityType=3 AND ActivityImplementation=1) OR ActivityType=1) ORDER BY ActivityID";
		params = new Object[] {new Integer(workflowID)};
		types = new int[] {Types.INTEGER};   
		List activityList = (List) executeQuery(new MapListHandler());
		
		int len = activityList.size();
		for (int i = 0; i < len; i++) {
		    Map activity = (Map) activityList.get(i);
		    LoadActivity loadActivity = ElementFactory.createLoadActivity(workflowID, ((Integer) activity.get("ActivityID")).intValue(), ((Integer) activity.get("ActivityImplementation")).intValue());
		    loadActivity.setStart(true);
		    analyzeOpenLoad(loadActivity);
		    
		    ElementFactory.clearLoadActivityCache();
		    
		    loadActivity = ElementFactory.createLoadActivity(workflowID, ((Integer) activity.get("ActivityID")).intValue(), ((Integer) activity.get("ActivityImplementation")).intValue());
		    loadActivity.setStart(true);
		    analyzeSubmitLoad(loadActivity);
		}
    }
    
    private void analyzeOpenLoad(LoadActivity localActivity) throws Exception {
        WorkflowEvent event = ElementFactory.createWorkflowEvent(this.conn, localActivity.getWorkflowID(), localActivity.getActivityID(), Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
        event.setPossibility(1.0);
        
        //Push event to event queue
        eventQueue.add(event);
        
        analyzeActivityLoad(localActivity);
        
        saveOpenLoad(localActivity);
    }
    
    private void analyzeSubmitLoad(LoadActivity localActivity) throws Exception  { 
        WorkflowEvent event = ElementFactory.createWorkflowEvent(this.conn, localActivity.getWorkflowID(), localActivity.getActivityID(), Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
        event.setPossibility(1.0);
        
        //Push event to event queue
        eventQueue.add(event);
        
        analyzeActivityLoad(localActivity);
        
        saveSubmitLoad(localActivity);
    }
    
    private void analyzeActivityLoad(LoadActivity localActivity) throws Exception {
        
        //If localActivity is the logic end activity, procedure call will directly return.
        if (localActivity.getActivityID() == 2)
            return;
        
        //Save current load activity in order to restore this value after procedure call return.
        LoadActivity oldCurrentLoadActivity = curLoadActivity;
        
        //Make curLoadActivity point to this localActivity
        curLoadActivity = localActivity;
        
        WorkflowEvent event = null;
        
        if (!loadActivityStack.contains(localActivity)) {
            loadActivityStack.addFirst(localActivity);
            
            if (!localActivity.isStart()) {
	            event = ElementFactory.createWorkflowEvent(this.conn, localActivity.getWorkflowID(), localActivity.getActivityID(), Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
	            event.setPossibility(1.0);
	            
	            //Push Start event to event queue
	            eventQueue.add(event);
	            
	            if (localActivity.getImplementationType() != 1) {
		            //Create a event that this activity is submitted
		            event = ElementFactory.createWorkflowEvent(this.conn, localActivity.getWorkflowID(), localActivity.getActivityID(), Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
		            event.setPossibility(1.0);
		            
		            //Push End event to event queue
		            eventQueue.add(event);
		            
		            //Take the load of this activity self as the part of this activity's loadNum
		            // and add this load to the loadNum
		            localActivity.addLoadNum(1.0);
	            }
            }
	
	        searchSubsequentLoadActivityList();
	        
	        //Iterator the subsequent load activity list of this load activity.
	        List subsequentLoadActivityList = localActivity.getSubsequentActivityList();
	        while (!subsequentLoadActivityList.isEmpty()) {
	            LoadActivity activity = (LoadActivity) subsequentLoadActivityList.remove(0);
	            analyzeActivityLoad(activity);
	            
	            localActivity.addLoadNum(activity.getLoadNum() * activity.getLoadFactor());
	            
	        }
	        
	        //Remove localActivity from load activity stack
	        loadActivityStack.removeFirst();
	        
	        if (localActivity.isLoop()) {
	            localActivity.setLoadNum(localActivity.getLoadNum() / (1.0 - localActivity.getLoopPossibility()));
	        }
        } else {         
            localActivity.setLoop(true);
            
            double loopPossibility = localActivity.getLoadFactor();
            
            LinkedList tempStack = new LinkedList();
            LoadActivity activity = (LoadActivity) loadActivityStack.removeFirst();
            tempStack.addFirst(activity);
            while (!activity.equals(localActivity)) {
            	loopPossibility *= activity.getLoadFactor();
            	
            	activity = (LoadActivity) loadActivityStack.removeFirst();
            	tempStack.addFirst(activity);
            }
            
            localActivity.setLoopPossibility(loopPossibility);
            
            while(!tempStack.isEmpty()) {
            	loadActivityStack.addFirst(tempStack.removeFirst());
            }
        }
        
        //Restore curLoadActivity
        curLoadActivity = oldCurrentLoadActivity;
    }
    
    private void saveOpenLoad(LoadActivity loadActivity) throws Exception {            
        sql = "DELETE FROM WorkflowEventLoad WHERE WorkflowID=? AND EventExpression=?";
		params = new Object[] {Integer.valueOf(loadActivity.getWorkflowID()), "Started(" + loadActivity.getActivityID() + ")"};
		types = new int[] {Types.INTEGER, Types.VARCHAR};  
		executeUpdate();
        
        sql = "INSERT INTO WorkflowEventLoad(WorkflowID, EventExpression, EventLoad) VALUES(?,?,?)";
		params = new Object[] {Integer.valueOf(loadActivity.getWorkflowID()), "Started(" + loadActivity.getActivityID() + ")", Double.valueOf(loadActivity.getLoadNum() * loadActivity.getLoadFactor())};
		types = new int[] {Types.INTEGER, Types.VARCHAR, Types.FLOAT};   
		executeUpdate();
    }
    
    private void saveSubmitLoad(LoadActivity loadActivity) throws Exception {            
        sql = "DELETE FROM WorkflowEventLoad WHERE WorkflowID=? AND EventExpression=?";
		params = new Object[] {Integer.valueOf(loadActivity.getWorkflowID()), "EndOf(" + loadActivity.getActivityID() + ")"};
		types = new int[] {Types.INTEGER, Types.VARCHAR};  
		executeUpdate();
        
        sql = "INSERT INTO WorkflowEventLoad(WorkflowID, EventExpression, EventLoad) VALUES(?,?,?)";
		params = new Object[] {Integer.valueOf(loadActivity.getWorkflowID()), "EndOf(" + loadActivity.getActivityID() + ")", Double.valueOf(loadActivity.getLoadNum() * loadActivity.getLoadFactor())};
		types = new int[] {Types.INTEGER, Types.VARCHAR, Types.FLOAT};   
		executeUpdate();
    }
    
    private void searchSubsequentLoadActivityList() throws Exception {
        while (!eventQueue.isEmpty()) {
            WorkflowEvent event = (WorkflowEvent) eventQueue.remove(0);
            analyzeWorkflowEvent(event);
        }
    }
    
    private void analyzeWorkflowEvent(WorkflowEvent event) throws Exception {
		sql = "SELECT * FROM WorkflowProcessECARule WHERE WorkflowID=? AND EventID=?";
		params = new Object[] {Integer.valueOf(event.getWorkflowID()), Integer.valueOf(event.getEventID())};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List workflowProcessECARuleList = (List) executeQuery(new MapListHandler());
		
		//Save the number of condition about this event to a local variable.
		int conditionNum = workflowProcessECARuleList.size();

		boolean conditionCalculated = false;
		Iterator workflowProcessECARuleIterator = workflowProcessECARuleList.iterator();
		while (workflowProcessECARuleIterator.hasNext()) {
			
			Map workflowProcessECARuleMap = (Map) workflowProcessECARuleIterator.next();

			//Check whether the conditionID of this event is equal to zero or not. 
			int conditionID = ((Integer) workflowProcessECARuleMap.get("ConditionID")).intValue();
			if (conditionID != 0) {
				actionExpression(event, (String) workflowProcessECARuleMap.get("ActionExpression"), event.getPossibility() / (double) conditionNum);
			} else {
				actionExpression(event, (String) workflowProcessECARuleMap.get("ActionExpression"), event.getPossibility());
			}
		}
		
		parseParentEvent(event);
    }
    
	private void parseParentEvent(WorkflowEvent event) throws Exception {
		
		sql = "SELECT * FROM WorkflowEventRelation WHERE WorkflowID=? AND SonEventID=?";
		params = new Object[] {Integer.valueOf(event.getWorkflowID()), Integer.valueOf(event.getEventID())};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		List workflowEventRelationList = (List) executeQuery(new MapListHandler());

		int intFatherEventID = -1;
		Iterator workflowEventRelationIterator = workflowEventRelationList.iterator();
		while( workflowEventRelationIterator.hasNext() ) {	
			Map workflowEventRelationforParseMap = (Map) workflowEventRelationIterator.next();
				
			intFatherEventID = ((Integer)workflowEventRelationforParseMap.get("FatherEventID")).intValue();
			
			sql = "SELECT * FROM WorkflowEvent WHERE WorkflowID=? AND EventID=?";
			params = new Object[] {Integer.valueOf(event.getWorkflowID()), Integer.valueOf(intFatherEventID)};
			types = new int[] {Types.INTEGER, Types.INTEGER};
			Map workflowEventMap = (Map) executeQuery(new MapHandler());
			if( workflowEventMap != null ) {
			    String eventExpression = (String) workflowEventMap.get("EventRepresentation");
			    
			    boolean isAndExpression = eventExpression.indexOf("AND") == -1 ? false : true;
			    int index = 0;
			    int conditionNum = 1;
			    double possibility = 1.0;
			    if (isAndExpression) {
				    while (index != -1) {
				        index = eventExpression.indexOf("AND", index + 1);
				        conditionNum++;
				    }
				    
				    possibility /= (double) conditionNum;
			    }
			    
			    WorkflowEvent parentEvent = ElementFactory.createWorkflowEvent(event.getWorkflowID(), ((Integer) workflowEventMap.get("EventID")).intValue(), eventExpression);
			    parentEvent.setPossibility(possibility);
				eventQueue.add(parentEvent);
			}
		}	  
	}
    
	private void actionExpression(WorkflowEvent event, String action, double possibility) throws Exception {
		int intMarkPos = -1;
		String strAtomicAction = "";
    
		if( !action.equals("") )
		{
			//Remove the ()
			action = action.substring(1,action.length());
			action = action.substring(0,action.length() - 1);
        
			intMarkPos = action.indexOf( "," );
			//得到相应的原子动作，如(Activity.1, Event.2), 将分为Activity.1; Event.2两个原子动作
			while( intMarkPos > 0 )
			{
				strAtomicAction = action.substring(0, intMarkPos);
				//得到原子动作
				doAtomicAction(event, strAtomicAction, possibility);
				action = action.substring(intMarkPos + 1, action.length());
				intMarkPos = action.indexOf( "," );
			}              
			//剩下的字符串也是原子动作
			doAtomicAction(event, action, possibility);
		}
	}
	
	private void doAtomicAction(WorkflowEvent event, String action, double possibility) throws Exception {
		String strActionType;
		String strActionObject;
    
		int intMarkPos;
    
		//Get the action type and action object id
		intMarkPos = action.indexOf( "." );
		strActionType = action.substring(0, intMarkPos);
		strActionObject = action.substring(intMarkPos + 1, action.length() );
    
		if( strActionType.equals("Event") ) {
		    WorkflowEvent subsequentEvent = ElementFactory.createWorkflowEvent(conn, event.getWorkflowID(), Integer.parseInt(strActionObject));
		    subsequentEvent.setPossibility(possibility);
			eventQueue.add(subsequentEvent);
    
		} else if( strActionType.equals("Activity") ) {
			sql = "SELECT * FROM WorkflowActivityInformation WHERE WorkflowID=? AND ActivityID=?";
			params = new Object[]{Integer.valueOf(event.getWorkflowID()), Integer.valueOf(strActionObject)};
			types = new int[] {Types.VARCHAR, Types.INTEGER};
			Map activityMap = (Map) query.query(conn, sql, params, new MapHandler());	;
			if (activityMap != null) {
			    LoadActivity loadActivity = ElementFactory.createLoadActivity(event.getWorkflowID(), ((Integer) activityMap.get("ActivityID")).intValue(), ((Integer) activityMap.get("ActivityImplementation")).intValue());
			    loadActivity.setLoadFactor(possibility);
			    
			    curLoadActivity.getSubsequentActivityList().add(loadActivity);
			}
		}
	}
    
    public static void main(String[] args) {
        try {
        	new EventLoadAnalyzer(WorkflowConnectionPool.getInstance().getConnection()).analyzeEventLoad(22);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
