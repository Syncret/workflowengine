/*
 * Created on 2004-11-3
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.activities;

import java.sql.Connection;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StartActivity extends AbstractActivity {
	
	public StartActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cit.workflow.elements.AbstractActivity#afterStateChanged()
	 */
	protected void afterStateChanged(String fromState, String toState) throws Exception {
		
		super.afterStateChanged(fromState, toState);
		
		if (toState.equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			
			int parentID = ((Integer) getAttributeValue("ParentID")).intValue();
			if (parentID == Constants.ACTIVITY_ROOT) {
				this.process.changeState(Constants.PROCESS_STATE_CREATED, Constants.PROCESS_STATE_RUNNING);
			} else {
				AbstractActivity parentActivity = this.getParentActivity();
				//parentActivity.setListenerList(this.listenerList);
				parentActivity.changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
			}
		} 
		
		/* 对逻辑活动的特殊处理，逻辑活动一变为Ready，就将其置为Completed */
		if (toState.equals(Constants.ACTIVITY_STATE_READY)) {
			changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_COMPLETED);
		}
		
		/* 创建由于状态变化而引起的事件,并放入事件队列 */
		//Event event = EventFactory.create(this, fromState, toState);
		//GlobalUtils.getInstance().getEventQueue().push(event);
	}
	
}
