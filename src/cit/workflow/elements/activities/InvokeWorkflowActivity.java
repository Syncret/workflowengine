/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Model;
import cit.workflow.elements.Process;
import cit.workflow.elements.factories.ElementFactory;

/**
 * 功能: 实例化并启动执行其它流程
 * 
 * @author weiwei
 */
public class InvokeWorkflowActivity extends NodeActivity {
	
	public InvokeWorkflowActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	public void execute() throws Exception {
		//servercomment System.out.println("ACTIVITY ACTION----InvokeWorkflowActivity EXECUTED!");
		
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
		/* Invoke Workflow Start */ 
		sql = "SELECT InvokedWorkflowID, InvocationType FROM ProcessActivityInvokingWorkflow WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List paiwList = (List) executeQuery(new MapListHandler());
		
		if (logger.isDebugEnabled()) 
			logger.debug("Invoke workflow count: " + paiwList.size() + ".");
			
		Iterator iterator = paiwList.iterator();
		while (iterator.hasNext()) {
			Map paiwMap = (Map) iterator.next();
			invokeWorkflow(((Integer)paiwMap.get("InvokedWorkflowID")).intValue(), ((Integer)paiwMap.get("InvocationType")).intValue());
		}
		
		/* Invoke Workflow End */
	}
	
	private void invokeWorkflow(int workflowID, int invokeType) throws Exception {
		Model model = new Model(conn, workflowID, processManager);
		String newProcessID = model.instantiateWorkflow(1, 3, this.process.getProcessID());
		if (newProcessID == "")
			throw new Exception("Instantiate workflow failed!");
			
		Process newProcess = new Process(conn, newProcessID, processManager);
		//sxh modified 2007.11
		int activityID = newProcess.getLogicActivityID(Constants.ACTIVITY_START, 1);
		//sxh modified 2007.11 end
		AbstractActivity activity = ElementFactory.createActivity(conn, newProcess, activityID, processManager);
		activity.setListenerList(this.getListenerList());
		if (!activity.changeState(Constants.ACTIVITY_STATE_WAITING, Constants.ACTIVITY_STATE_COMPLETED))
			throw new Exception("Start process failed!");
			
		/* 经过该活动调用的流程需要记录调用的信息 */
		updateInvokingProcessInformation(newProcessID, invokeType);
		
		if (invokeType == Constants.INVOKE_TYPE_ASYNCHRONISM) 
			changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
	}
	
	private void updateInvokingProcessInformation(String processID, int invokeType) throws Exception {
		
		sql = "INSERT INTO ProcessActivityInvokingProcess(ProcessID, ActivityID, InvokedProcessID, InvocationType, ActualStartDate, ActualEndDate) VALUES(?,?,?,?,?,?)";
		params = new Object[] { this.getProcess().getProcessID(), new Integer(this.activityID), 
								processID, new Integer(invokeType), processManager.getCurrentDate(), processManager.getCurrentDate()};
				
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.DATE, Types.DATE};
		executeUpdate();
		
		/*
		sql = "SELECT ProcessID FROM ProcessActivityInvokingProcess WHERE ProcessID=? AND ActivityID=? AND InvokedProcessID=?";
		params = new Object[] {new Integer(this.getProcess().getProcessID()), new Integer(this.activityID), new Integer(processID)};
		types = new int[] {Types.INTEGER, Types.INTEGER, Types.INTEGER};
		Map paipMap = (Map) executeQuery(new MapHandler());
		if (paipMap == null) {
			
			//将重复事件信息写入ProcessRepeatedInformation表
			//RepeatedType---4代表Process
			sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
			params = new Object[] { new Integer(this.getProcess().getProcessID()), new Integer(4), 
									new Integer(this.getActivityID()), new Integer(processID),
									paipMap.get("RepeatedTime"), paipMap.get("ActualStartDate"), 
									paipMap.get("ActualEndDate")};
				
			types = new int[] {Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.DATE, Types.DATE};
			executeUpdate();
			
			sql = "UPDATE ProcessActivityInvokingProcess SET ActualStartDate=? WHERE ProcessID=? AND ActivityID=? AND InvokedProcessID=?";
			params = new Object[] {new Integer(this.getProcess().getProcessID()), new Integer(this.activityID), new Integer(processID)};
			types = new int[] {Types.INTEGER, Types.INTEGER, Types.INTEGER};
			executeUpdate();
		} else {
			sql = "INSERT INTO ProcessActivityInvokingProcess(ProcessID, ActivityID, InvokedProcessID, RepeatedTime, ActualStartTime) VALUES(?,?,?,?,?)";
			params = new Object[] { new Integer(this.getProcess().getProcessID()), new Integer(this.activityID), 
									new Integer(processID), new Integer(0), GlobalUtils.getInstance().getCurrentDate()};
				
			types = new int[] {Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.DATE};
			executeUpdate();
		}*/
	}
}
