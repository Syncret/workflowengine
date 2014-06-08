/*
 * Created on 2004-11-2
 */
package cit.workflow.elements.activities;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.utils.DBUtility;


/**
 * @author weiwei
 */
public abstract class AbstractActivity extends DBUtility {
	
	protected static Logger logger = Logger.getLogger(AbstractActivity.class);
	
	/* 流程ID */
	protected Process process;
	
	/* 活动ID */
	protected int activityID;
	
	//sxh add 2007.12
	protected boolean isCallBack;
	//sxh add 2007.12 end

	/* 活动当前的状态 */
	protected String activityState;
	
	protected List listenerList;
	
	protected NodeActivity parentActivity;
	
	protected ProcessManager processManager;
	
	public AbstractActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {	
		super(conn);
		
		this.processManager = processManager;
		
		this.process = process;
		this.activityID = activityID;
		this.listenerList = new ArrayList();
	}
	
	protected boolean canChangeState(String fromState, String toState) throws Exception {
		return true;
	}
	
	protected void beforeStateChanged(String fromState, String toState) throws Exception {
		/* 调用活动状态变化事件监听者,使其在状态改变时能做出合适的相应 */
		Iterator iterator = listenerList.iterator();
		while (iterator.hasNext()) {
			ActivityStateChangeListener listener = (ActivityStateChangeListener) iterator.next();
			listener.beforeActivityStateChanged(new ActivityStateChangeEvent(this, fromState, toState));
		}
	}
	
	protected void afterStateChanged(String fromState, String toState) throws Exception {
		/* 调用活动状态变化事件监听者,使其在状态改变时能做出合适的相应 */
		Iterator iterator = listenerList.iterator();
		while (iterator.hasNext()) {
			ActivityStateChangeListener listener = (ActivityStateChangeListener) iterator.next();
			listener.afterActivityStateChanged(new ActivityStateChangeEvent(this, fromState, toState));
		}
	}
	
	protected void updateState(String fromState, String toState) throws SQLException {
		if (logger.isInfoEnabled()) 
			logger.info("CHANGE STATE----Activity " + this.activityID + ", state: from " + fromState + " to " + toState + ".");
		
		sql = "UPDATE ProcessActivityInformation SET State=? WHERE ProcessID=? AND ActivityID=?";
		params =  new Object[] {toState, this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
		executeUpdate();
		
		activityState = toState;
	}
	
	public boolean changeState(String fromState, String toState) throws Exception {
		
		if (!canChangeState(fromState, toState)) 
			return false;
		
		/* 为改变做些准备工作 */
		beforeStateChanged(fromState, toState);
		
		/* 更新活动状态 */
		updateState(fromState, toState);
		
		/* 改变后做些收尾工作 */
		afterStateChanged(fromState, toState);
		
		return true;
	}
	
	/**
	 * 功能: 重置活动状态
	 * 
	 * 步骤: 
	 *		1.重置活动所有子活动
	 */
	protected void resetActivity() throws SQLException {

		sql = "UPDATE ProcessActivityInformation SET SubmitPersonNumber=?, State=? WHERE ProcessID=? AND ActivityID=?";
		params = new Object[]{new Integer(0), Constants.ACTIVITY_STATE_WAITING, this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
		executeUpdate();
		
		//复位所有子活动的状态为"Waiting"
		List childList = getChildList();
		for (int i = 0; i < childList.size(); i++) {
			AbstractActivity activity = (AbstractActivity) childList.get(i);
			activity.resetActivity();	
		}
	}
	
	protected List getChildList() throws SQLException {

		sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ParentID=?";
		params = new Object[]{this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List processActivityRoleList = (List) executeQuery(new MapListHandler());
		List childList = new ArrayList();
		for (int i = 0; i < processActivityRoleList.size(); i++) {
			Map tempMap = (Map) processActivityRoleList.get(i);
			
			AbstractActivity activity = ElementFactory.createActivity(conn, this.process, ((Integer)tempMap.get("ActivityID")).intValue(), processManager);
			//activity.setListenerList(this.getListenerList());
			activity.setParentActivity((NodeActivity) this);
			childList.add(activity);
		}
		return childList;
	}
	
	public Object getAttributeValue(String attributeName) throws Exception {
		sql = "SELECT " + attributeName + " FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
		params = new Object[]{this.process.getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processActivityInformationMap = (Map) executeQuery(new MapHandler());
		return processActivityInformationMap.get(attributeName);
	}
	
	/**
	 * @return Returns the activityID.
	 */
	public int getActivityID() {
		return activityID;
	}
	/**
	 * @return Returns the activityState.
	 */
	public String getActivityState() {
		return activityState;
	}
	/**
	 * @return Returns the process.
	 */
	public Process getProcess() {
		return process;
	}
	
	public void addStateChangeListener(ActivityStateChangeListener listener) {
		listenerList.add(listener);
	}
	
	public boolean removeStateChangeListener(ActivityStateChangeListener listener) {
		return listenerList.remove(listener);
	}
	
	public void clearStateChangeListener() {
		listenerList.clear();
	}
	/**
	 * @return
	 */
	public List getListenerList() {
		return listenerList;
	}

	/**
	 * @param list
	 */
	public void setListenerList(List list) {
		listenerList = list;
	}

	/**
	 * @return
	 */
	public NodeActivity getParentActivity() {
		return parentActivity;
	}

	/**
	 * @param activity
	 */
	public void setParentActivity(NodeActivity activity) {
		parentActivity = activity;
	}

	//sxh add 2007.12
	public boolean isCallBack() {
		return isCallBack;
	}

	public void setCallBack(boolean isCallBack) {
		this.isCallBack = isCallBack;
	}
	//sxh add 2007.12 end

}
