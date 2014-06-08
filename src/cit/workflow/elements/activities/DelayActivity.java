/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.Constants;
import cit.workflow.WorkflowServer;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.exception.WorkflowTransactionException;

/**
 * 功能：在工作流执行过程中延时一段时间
 * @author weiwei
 * 
 */
public class DelayActivity extends NodeActivity {

	public DelayActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	//sxh modified 2007.11
	/*public void execute() throws Exception {
		//servercomment System.out.println("ACTIVITY ACTION----DelayActivity EXECUTED!");
		
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
		 Delay start 
		sql = "SELECT Duration FROM ProcessActivitySchedule WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map pasMap = (Map) executeQuery(new MapHandler());
		if (pasMap != null) {
			 时间单位暂时以秒为单位 
			int duration = ((Integer) pasMap.get("Duration")).intValue();
			
			if (logger.isInfoEnabled())
				logger.info("DelayActivity start, delay " + duration + "s.");
			
			sql = "INSERT INTO ProcessDelayActivity(ProcessID, ActivityID, Expiration) VALUES(?,?,?)";
			
			long expiration = System.currentTimeMillis() + duration * 1000;
			
			params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID), String.valueOf(expiration)};
			types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
			executeUpdate();
			
			 唤醒执行Delay Activity线程 
			if (WorkflowServer.delayActivityThread.isAlive()) {
				if (WorkflowServer.delayActivityThread.isSleep())
					WorkflowServer.delayActivityThread.interrupt();
			} else {
				WorkflowServer.delayActivityThread = new WorkflowServer.DelayActivityThread();
				WorkflowServer.delayActivityThread.start();
			}
		} else {
			throw new WorkflowTransactionException("There is no right data for handle!");
		}
		
		 Delay end 
		//changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
	}*/
	
	public void execute() throws Exception {
		//servercomment System.out.println("ACTIVITY ACTION----DelayActivity EXECUTED!");
		
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
		/* Delay start */
		sql = "SELECT Duration, DurationUnit FROM ProcessActivitySchedule WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map pasMap = (Map) executeQuery(new MapHandler());
		if (pasMap != null) {
			/* 时间单位暂时以秒为单位 */
			int duration = ((Integer) pasMap.get("Duration")).intValue();
			String durationUnit = (String)pasMap.get("DurationUnit");
			
			if (logger.isInfoEnabled())
				logger.info("DelayActivity start, delay " + duration + durationUnit + ". ");
			
			sql = "INSERT INTO ProcessDelayActivity(ProcessID, ActivityID, Expiration) VALUES(?,?,?)";
			
			long expiration = System.currentTimeMillis();
			if (durationUnit.equals("s")) {
				//秒
				expiration += duration * 1000;
			} else if (durationUnit.equals("m")) {
				//分钟
				expiration += duration * 1000 * 60;
			} else if (durationUnit.equals("h")) {
				//小时
				expiration += duration * 1000 * 60 * 60;
			} else if (durationUnit.equals("D")) {
				//天
				expiration += duration * 1000 * 60 * 60 * 24;
			} else if (durationUnit.equals("M")) {
				//月
				//未考虑大月小月
				expiration += duration * 1000 * 60 * 60 * 24 * 30;
			} else if (durationUnit.equals("Y")) {
				//年
				//未考虑平年润年
				expiration += duration * 1000 * 60 * 60 * 24 * 365;
			}
			
			params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID), String.valueOf(expiration)};
			types = new int[] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
			executeUpdate();
			
			/* 唤醒执行Delay Activity线程 */
			if (WorkflowServer.delayActivityThread.isAlive()) {
				if (WorkflowServer.delayActivityThread.isSleep())
					WorkflowServer.delayActivityThread.interrupt();
			} else {
				WorkflowServer.delayActivityThread = new WorkflowServer.DelayActivityThread();
				WorkflowServer.delayActivityThread.start();
			}
		} else {
			throw new WorkflowTransactionException("There is no right data for handle!");
		}
		
		/* Delay end */
		//changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
	}
	//sxh modified 2007.11 end
}
