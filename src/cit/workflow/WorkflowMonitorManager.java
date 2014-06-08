/*
 * Created on 2005-3-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.dao.ActivityInformationDAO;
import cit.workflow.dao.PersonRoleDAO;
import cit.workflow.dao.ProcessDAO;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.graph.ProcessPicService;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkflowMonitorManager {
	private static final WorkflowMonitorManager monitorManager = new WorkflowMonitorManager();
	
	private WorkflowMonitorManager() {
		//TODO 
	}
	
	public static WorkflowMonitorManager getInstance() {
		return monitorManager;
	}
	
	
	/**
	 * 根据条件参数得到活动信息列表
	 * @param paramMap 条件参数，将多组条件以Map形式封装，每一组条件对应于一组key和value，key为数据库中字段名称，value为字段的值
	 * @return 符合条件的活动信息列表
	 * @throws RemoteException
	 */
	public List getActivity(Map paramMap) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List result = null;
		try {
		    result = new ProcessDAO(conn).getActivity(paramMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	
	public Map getActivityInformation(String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Map result = null;
		try {
		    result = new ProcessDAO(conn).getActivityInformation(processID, activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	public List getProcessActivityList(String processID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List result = null;
		try {
		    result = new ProcessDAO(conn).getProcessActivityList(processID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	/**
	 * 2008.3
	 * 得到活动的URL
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @return 活动的URL
	 * @throws RemoteException
	 */
	public String getActivityURL(String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		String url = null;
		try {
		    url = new ProcessDAO(conn).getActivityURL(processID, activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return url;
	}
	
	public boolean setActivityStartDate(String processID, int activityID, Date actualStartDate) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean success = false;
		try {
		    success = new ActivityInformationDAO(conn).setActivityStartDate(processID, activityID, actualStartDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return success;
	}
	
	
	
	/**
	 * 功能：根据角色ID选择该用户能够进行相应操作的流程ID集合
	 * @param roleID
	 * @param functionType
	 * @return 流程ID集合
	 * @throws RemoteException
	 */
	public String[] getProcessIDSet(int roleID, int functionType) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		String[] result = null;
		try {
		    result = new ProcessDAO(conn).getProcessIDSet(roleID, functionType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	/**
	 * 得到流程信息
	 * @param processID 流程ID
	 * @return 流程信息，以Map形式返回
	 * @throws RemoteException
	 */
	public Map getProcessInformation(String processID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Map result = null;
		try {
		    result = new ProcessDAO(conn).getProcessInformation(processID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}

	
	/**
	 * 功能：获得当前正在运行的流程信息
	 * @return 流程信息列表，以Map类型封装
	 */
	public List getProcess() throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List result = null;
		try {
		    result = new ProcessDAO(conn).getProcess();
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	
	/**
	 * 功能：根据角色获得当前正在运行的流程信息
	 * @param roleID 角色的roleID
	 * @param functionType 角色的功能类型
	 * @return 流程信息列表，以Map类型封装
	 * @throws RemoteException
	 */
	public List getProcess(int roleID, int functionType) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List result = null;
		try {
		    result = new ProcessDAO(conn).getProcess(roleID, functionType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	/**
	 * 功能：根据角色获得当前正在运行的流程信息
	 * @param roleID 角色的roleID
	 * @param functionType 角色的功能类型
	 * @return 流程信息列表，以Map类型封装
	 * @throws RemoteException
	 */
	public List getProcessMonitorInformationList(int roleID, int functionType) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List result = null;
		try {
		    result = new ProcessDAO(conn).getProcessMonitorInformationList(roleID, functionType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		
		return result;
	}
	
	
	
	/**
	 * 得到工作流信息的图片的存储地址
	 * @param workflowID 工作流ID
	 * @return 图片地址
	 */
	public String getProcessPic(int workflowID) throws RemoteException {
		ProcessPicService processPicService;
		String fileName;
		processPicService = new ProcessPicService();
		fileName = processPicService.drawProcess(workflowID);
		return fileName;
	}
	
	/**
	 * 得到流程信息的图片的存储地址
	 * @param processID 流程ID
	 * @return 图片地址
	 */
	public String getProcessPic(String processID) throws RemoteException{
		ProcessPicService processPicService;
		String fileName;
		processPicService = new ProcessPicService();
		fileName = processPicService.drawProcess(processID);
		return fileName;
	}

	/**
	 * 修改流程信息
	 * 
	 * @param processID
	 *            流程ID
	 * @param processName
	 *            待修改的流程名
	 * @param processDescription
	 *            待修改的流程描述
	 * @param persistent
	 *            待修改的是否持久化属性
	 * @return 是否修改成功
	 * @throws RemoteException
	 */
	public boolean setProcessInformation(String processID, String processName,
			String processDescription, boolean persistent) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		try {
			succeed = new ProcessDAO(conn).setProcessInformation(processID,
					processName, processDescription, persistent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!succeed)
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		return succeed;
	}
	
	//sxh add 2007.11
	/**
	 * 设置流程名称
	 * @param processID 该流程的ID
	 * @param processName 将要设置的流程名称
	 * @return 是否设置成功
	 * @throws RemoteException
	 */
	public boolean setProcessName(String processID, String processName) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		try {
			succeed = new ProcessDAO(conn).setProcessName(processID, processName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!succeed)
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		return succeed;
	}
	//sxh add 2007.11 end
	
	
	public int getPersonState(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int state = -1;
		try {
			state = new PersonRoleDAO(conn).getPersonState(personID);
			commitAction(conn);
		} catch (Exception e) {
			e.printStackTrace();
			rollbackAction(conn);
		}
		return state;
	}
	
	public void setPersonState(int personID, int state) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		try {
			new PersonRoleDAO(conn).setPersonState(personID, state);
			commitAction(conn);
		} catch (Exception e) {
			e.printStackTrace();
			rollbackAction(conn);
		}
	}
	
	
	
	private void rollbackAction(Connection connection) {
		try {
			connection.rollback();
			connection.close();
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback error, message: " + e.getMessage());
		}
	}
	
	private void commitAction(Connection connection) {
		try {
			connection.commit();
			connection.close();
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection commit error, message: " + e.getMessage());
		}
	}
}
