/*
 * Created on 2004-10-14
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Process extends DBUtility {
	
	private static Logger logger = Logger.getLogger(Process.class);
	
	//流程ID
	private String processID;
	
	private ProcessManager processManager;
	
	protected List listenerList;
	
	private String processState = Constants.PROCESS_STATE_CREATED;

	public Process(Connection conn, String processID, ProcessManager processManager) {
		super(conn);
		this.processID = processID;
		this.processManager = processManager;
		this.listenerList = new ArrayList();
		this.listenerList.add(processManager);
	}
	
	/**
	 * 功能：改变流程状态，并修改相应的TWC信息.
	 * 
	 * 前提条件：
	 * 		1.开始节点的状态变为Ready,那么流程的状态就要变为Running
	 * 		2.结束节点的状态变为Completed,那么流程的状态就要变为Completed
	 * 
	 * 步骤：
	 * 		1.改变流程状态
	 * 		2.获得流程的TWCID
	 * 		3.修改流程的TWCInformation
	 */
	public void changeState(String fromState, String toState) throws Exception {
		
		beforeStateChanged(fromState, toState);
		
		//改变流程的状态
		updateState(fromState, toState);
		
		afterStateChanged(fromState, toState);
						
	}
	
	protected void beforeStateChanged(String fromState, String toState) throws Exception  {
		
		/* 调用活动状态变化事件监听者,使其在状态改变时能做出合适的相应 */
		Iterator iterator = listenerList.iterator();
		while (iterator.hasNext()) {
			ProcessStateChangeListener listener = (ProcessStateChangeListener) iterator.next();
			listener.beforeProcessStateChanged(new ProcessStateChangeEvent(this, fromState, toState));
		}
	}
	
	protected void afterStateChanged(String fromState, String toState) throws Exception  {
	
		//获得流程的TWCID
		sql = "SELECT * FROM ProcessInformation WHERE ProcessID=?";
		params = new Object[]{this.processID};
		types = new int[] {Types.VARCHAR};
		Map processInformationMap = (Map) executeQuery(new MapHandler());
		if (processInformationMap == null)
			throw new WorkflowTransactionException("There is no right data for handle!");
			
		//如果流程状态改变，须修改流程的开始或结束时间
		if (toState.equals(Constants.PROCESS_STATE_RUNNING)) {
			sql = "UPDATE ProcessTWCInformation SET ActualStartDate=? WHERE ProcessID=? AND TWCID=?";
			params = new Object[] {processManager.getCurrentDate(), this.processID, processInformationMap.get("TWCID")};
			types = new int[] {Types.DATE, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
		} else if (toState.equals(Constants.PROCESS_STATE_COMPLETED)) {
			sql = "UPDATE ProcessTWCInformation SET ActualEndDate=? WHERE ProcessID=? AND TWCID=?";
			params = new Object[] {processManager.getCurrentDate(), this.processID, processInformationMap.get("TWCID")};
			types = new int[] {Types.DATE, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
		}
		
		/* 当流程结束判断是否存在等待该流程结束的活动，如果存在则提交该活动 */
		if (toState.equals(Constants.PROCESS_STATE_COMPLETED)) {
			notifyActivityToCompleted();
		}
		
		/* 调用活动状态变化事件监听者,使其在状态改变时能做出合适的相应 */
		Iterator iterator = listenerList.iterator();
		while (iterator.hasNext()) {
			ProcessStateChangeListener listener = (ProcessStateChangeListener) iterator.next();
			listener.afterProcessStateChanged(new ProcessStateChangeEvent(this, fromState, toState));
		}
	}
	
	private void notifyActivityToCompleted() throws Exception {
		sql = "SELECT * FROM ProcessActivityInvokingProcess WHERE InvokedProcessID=?";
		params = new Object[] {this.processID};
		types = new int[] {Types.VARCHAR};
		Map paipMap = (Map) executeQuery(new MapHandler());
		if (paipMap != null) {
			int invocationType = ((Integer) paipMap.get("InvocationType")).intValue();
			
			if (invocationType == Constants.INVOKE_TYPE_SYNCHRONIZATION) {
				/* 更新流程同步执行的实际结束时间 */
				sql = "UPDATE ProcessActivityInvokingProcess SET ActualEndDate=? WHERE InvokedProcessID=?";
				params = new Object[] {processManager.getCurrentDate(), this.processID};
				types = new int[] {Types.DATE, Types.VARCHAR};
				executeUpdate();
				
				/* 唤醒同步等待的活动，继续执行 */
				String parentProcessID = (String) paipMap.get("ProcessID");
				int activityID = ((Integer) paipMap.get("ActivityID")).intValue();
				
				Process parentProcess = new Process(conn, parentProcessID, processManager);
				AbstractActivity activity = ElementFactory.createActivity(conn, parentProcess, activityID, processManager);
				
				/** Note: add state changeListener is very important! */
				//activity.addStateChangeListener(GlobalUtils.getInstance().getProcessManager());
				
				activity.changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
			}
		}
	}
	
	private void updateState(String fromState, String toState) throws Exception {
		
		sql = "UPDATE ProcessInformation SET State=? WHERE ProcessID=?";
		params = new Object[]{toState, this.processID};
		types = new int[] {Types.VARCHAR, Types.VARCHAR};
		executeUpdate();
		
		processState = toState;
	}
	
	//sxh modified 2007.11
	public int getLogicActivityID(int activityType, int layer) throws SQLException {

		sql = "SELECT ActivityID FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityType=? AND Layer=?";
		params = new Object[]{processID, new Integer(activityType), new Integer(layer)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		Map map = (Map) executeQuery(new MapHandler());
		if (map != null) {
			return ((Integer)map.get("ActivityID")).intValue();
		} else {
			throw new WorkflowTransactionException("There is no right data for handle!");
		}	
	}
	//sxh modified 2007.11 end
	
	public Object getAttributeValue(String attributeName) throws Exception {
		sql = "SELECT " + attributeName + " FROM ProcessInformation WHERE ProcessID=?";
		params = new Object[]{this.processID};
		types = new int[] {Types.VARCHAR};
		Map processInformationMap = (Map) executeQuery(new MapHandler());
		return processInformationMap.get(attributeName);
	}
	
	public boolean deleteProcess() {
		try {
			Statement stat = conn.createStatement();
			stat.addBatch("DELETE FROM ProcessInformation WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessTWCInformation WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessActivityInformation WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessActivityRole WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessObject WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessFlowObjects WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessFlowObjectControl WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessEvent WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessEventRelation WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessEventRelationforParse WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessCondition WHERE ProcessID='" + this.processID + "'");
			stat.addBatch("DELETE FROM ProcessProcessECARule WHERE ProcessID='" + this.processID + "'");
			stat.executeBatch();
			stat.close();
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public void addStateChangeListener(ProcessStateChangeListener listener) {
		listenerList.add(listener);
	}
	
	public boolean removeStateChangeListener(ProcessStateChangeListener listener) {
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
	public String getProcessID() {
		return processID;
	}
}
