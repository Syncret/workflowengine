/*
 * Created on 2004-11-2
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.eca;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.awt.Color;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.elements.Event;
import cit.workflow.elements.Task;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ObjectFlow extends DBUtility {
	
	private ProcessManager processManager;
	private static Logger logger = Logger.getLogger(ObjectFlow.class);
	
	public ObjectFlow(Connection conn, ProcessManager processManager) {	
		super(conn);
		
		this.processManager = processManager;
	}
	
	/**
	 * 功能：激活与此事件相关的数据流
	 * 
	 * @param event
	 * @throws SQLException
	 * 
	 * 步骤：
	 * 		1. 直接修改与由事件激活的数据流的对象状态为"Active"
	 */
//	public void ecaForObjectFlow(Event event) throws SQLException {
//		sql = "?, ActiveTime=?, RepeatedTime=RepeatedTime+1 WHERE ProcessID=? AND (FlowID IN (SELECT FlowID FROM ProcessFlowObjects WHERE ProcessID=? AND EventID=?))";
//		params = new Object[] { Constants.DATAFLOW_STATE_ACTIVE,
//				processManager.getCurrentDate(),
//				event.getProcess().getProcessID(),
//				event.getProcess().getProcessID(),
//				new Integer(event.getEventID()) };
//		types = new int[] { Types.VARCHAR, Types.DATE, Types.VARCHAR,
//				Types.VARCHAR, Types.INTEGER };
//		executeUpdate();
//		// test event log;
//		sql = "select objectID,State from ProcessFlowObjectControl where ProcessID = ? AND (FlowID IN (SELECT FlowID FROM ProcessFlowObjects WHERE ProcessID=? AND EventID=?)) ";
//		params = new Object[] { event.getProcess().getProcessID(),
//				event.getProcess().getProcessID(),
//				new Integer(event.getEventID()) };
//		types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
//		List objectList = (List) executeQuery(new MapListHandler());
//		Iterator objectIterator = objectList.iterator();
//		while (objectIterator.hasNext()) {
//			Map objectMap = (Map) objectIterator.next();
//			logger.info("FlowObject INFO: Set ObjectID = "
//					+ objectMap.get("ObjectID").toString() + " State to :"
//					+ objectMap.get("State").toString());
//		}
//	}
	
	public void ecaForObjectFlow(String processID, int ruleID){
		try {
			if (conn.isClosed())conn = WorkflowConnectionPool.getInstance().getConnection();
			sql = "UPDATE ProcessFlowObjectControl SET State=?, ActiveTime=?, RepeatedTime=RepeatedTime+1 WHERE ProcessID=? AND "
					+ "(FlowID IN (SELECT FlowID FROM ProcessFlowObjects WHERE ProcessID=? AND DroolsRuleID=?))";
			params = new Object[] { Constants.DATAFLOW_STATE_ACTIVE, processManager.getCurrentDate(), processID,
					processID, ruleID };
			types = new int[] { Types.VARCHAR, Types.DATE, Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
			// test event log;
			sql = "select objectID,State from ProcessFlowObjectControl where ProcessID = ? AND (FlowID IN (SELECT FlowID FROM "
					+ "ProcessFlowObjects WHERE ProcessID=? AND DroolsRuleID=?)) ";
			params = new Object[] { processID, processID, ruleID };
			types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
			List objectList = (List) executeQuery(new MapListHandler());
			Iterator objectIterator = objectList.iterator();
			while (objectIterator.hasNext()) {
				Map objectMap = (Map) objectIterator.next();
				logger.info("FlowObject INFO: Set ObjectID = " + objectMap.get("ObjectID").toString() + " State to :"
						+ objectMap.get("State").toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 功能：复位活动对应的所有数据流的状态为"Inactive"
	 * 
	 * @param activity
	 * @throws SQLException
	 * 
	 * 步骤：
	 * 		1. 直接修改与活动相关的数据流的对象
	 */
	public void resetObjectFlow(AbstractActivity activity){
		sql = "UPDATE ProcessFlowObjectControl SET State=?, ActiveTime=? WHERE ProcessID=? AND (FlowID IN (SELECT FlowID FROM ProcessFlowObjects WHERE ProcessID=? AND ToActivityID=?))";
		params = new Object[]{Constants.DATAFLOW_STATE_INACTIVE, processManager.getCurrentDate(), activity.getProcess().getProcessID(), activity.getProcess().getProcessID(), new Integer(activity.getActivityID())};
		types = new int[] {Types.VARCHAR, Types.DATE, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
//		executeUpdate();
		int result;
		try {
			result = executeUpdate(1);
			if (result >0) {
				//servercomment System.out.println("FlowObject INFO:");
				//servercomment System.out.println("Set activity "+(activity.getActivityID())+" 's FlowObjects State to Inactive " );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
//	/**
//	 * 
//	 * @author dy
//	 *　功能：读取相关活动的数据流信息
//	 * @param activity
//	 * @throws SQLException
//	 * 
//	 * 步骤： 
//	 * 		1.直接读取相关活动的数据流信息
//	 * 
//	 */
//	 public List getActivityObjectFlow(int activityID) throws SQLException {
//	 	List objectFlowList = new ArrayList();
//	 	sql = "select objectID from processFlowObjects as a , ProcessFlowObjectControl as b where a.ToActivityID = '?' and  a.ProcessID ='?' and b.ProcessID ='?'and b.State = '?' and a.FlowID = b.FlowID ";
////	 	params = new Object[]{new Integer(activity.getActivityID()), activity.getProcess().getProcessID(),activity.getProcess().getProcessID(),Constants.DATAFLOW_STATE_ACTIVE};
//	 	params = new Object[]{new Integer(activityID),"57501966-f124-46a4-944f-dbe47457f3b7","57501966-f124-46a4-944f-dbe47457f3b7",Constants.DATAFLOW_STATE_ACTIVE};
//	 	types = new int[]{Types.INTEGER,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR};
//	 	List objectList = (List)executeQuery(new MapListHandler());
//	 	
//	 	Iterator objectIterator = objectList.iterator();
//	 	while (objectIterator.hasNext()){
//	 		Map objectMap = (Map)objectIterator.next();
//	 		
//	 		objectFlowList.add((Integer)objectMap.get("ObjectID"));
//	 	}
//	 	
//	 	return objectFlowList;
//	 }
//		sql = "SELECT PAP.*, PAI.ActivityName, PI.ProcessName FROM ProcessActivityPerson PAP LEFT JOIN ProcessActivityInformation PAI ON PAI.ProcessID=PAP.ProcessID AND PAI.ActivityID=PAP.ActivityID LEFT JOIN ProcessInformation PI ON PI.ProcessID=PAP.ProcessID WHERE PAP.PersonID=? AND PAP.State<>?";
//		params = new Object[]{new Integer(processManager.getCurrentUser().getActorID()), Constants.ACTIVITY_STATE_COMPLETED};
//		types = new int[] {Types.INTEGER, Types.VARCHAR};
//		List processActivityPersonList = (List) executeQuery(new MapListHandler());
//		
//		Iterator processActivityPersonIterator = processActivityPersonList.iterator();
//	
//		while (processActivityPersonIterator.hasNext()) {
//			
//			Map processActivityPersonMap = (Map) processActivityPersonIterator.next();
//			
//			Task task = new Task();
//			task.setProcessID((String) processActivityPersonMap.get("ProcessID"));
//			task.setProcessName((String) processActivityPersonMap.get("ProcessName"));
//			task.setActivityID(((Integer) processActivityPersonMap.get("ActivityID")).intValue());
//			task.setActivityName((String) processActivityPersonMap.get("ActivityName"));
//			task.setDefinedStartDate(new java.sql.Date(((Date) processActivityPersonMap.get("DefinedStartDate")).getTime()));
//			task.setDefinedEndDate(new java.sql.Date(((Date) processActivityPersonMap.get("DefinedEndDate")).getTime()));
//			task.setState((String) processActivityPersonMap.get("State"));
//			
//			//增加任务到任务列表
//			taskList.add(task);
//			
//			if (logger.isInfoEnabled())
//				logger.info("ADD PERSON TASK LIST----" + task);
//		}	
}
