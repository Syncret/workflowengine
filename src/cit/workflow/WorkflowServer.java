/*
 * Created on 2004-12-22
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

import ch.qos.logback.classic.Logger;
import cit.workflow.dao.DelegationDAO;
import cit.workflow.dao.ProcessDAO;
import cit.workflow.dao.WorkflowDAO;
import cit.workflow.dao.agent.AgentDAO;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.User;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkflowServer {
	
	//cxz modified on 2008.12.3
	public static final WorkflowServer Server = new WorkflowServer();
	
	//private List eventQueue;
	
	private ThreadLocal localEventQueue = new ThreadLocal();
	
	public static DelayActivityThread delayActivityThread;
	
	private String[][] initialVariables=null;
	
	public void setInitialVariables(String[][] variables){
		this.initialVariables=variables;
	}
	
	
	/**
	 * 用于处理延迟活动的线程
	 */
	public static class DelayActivityThread extends Thread {
		private boolean keepRunning = false;
		
		private boolean sleep = false;
		
		public DelayActivityThread() {
			setDaemon(true);
		}
		
		public void terminate() {
			keepRunning = false;
		}
		
		public boolean isSleep() {
			return sleep;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			
			keepRunning = true;
			
			Connection conn = null;
			QueryRunner query = null;
			
			try {
				while (keepRunning) {
					
					boolean successed = false;
						
					long currentTime = System.currentTimeMillis();
						
					conn = WorkflowConnectionPool.getInstance().getConnection();
					query = new QueryRunner();
					String sql = "SELECT ProcessID, ActivityID, MIN(Expiration) AS Expiration FROM ProcessDelayActivity GROUP BY ProcessID, ActivityID";
					Object[] params = new Object[] {};
					int[] types = new int[] {};
					Map pda = (Map) query.query(conn, sql,  new MapHandler(),params);
					if (pda != null) {
							
						long sleepTime = Long.parseLong((String)pda.get("Expiration")) - currentTime;
						if (sleepTime > 0) {
							
							conn.close();
							/* Let current thread sleep */
							try {
								sleep = true;
								Thread.sleep(sleepTime);
							} catch(InterruptedException e) {
							} finally {
								sleep = false;
							}
							conn = WorkflowConnectionPool.getInstance().getConnection();
						}
							
						/* Delete this record */
						sql = "DELETE FROM ProcessDelayActivity WHERE ProcessID=? AND ActivityID=?";
						params = new Object[] {pda.get("ProcessID"), pda.get("ActivityID")};
						types = new int[] {Types.INTEGER, Types.INTEGER};
						query.update(conn, sql, params, types);
							
						/* Submit this delay activity */
						ProcessManager processManager = new ProcessManager(conn, null);
						//sxh modified 2007.10
						//successed = processManager.submitActivity((String)pda.get("ProcessID"), ((Integer)pda.get("ActivityID")).intValue());
						int state = processManager.submitActivity((String)pda.get("ProcessID"), ((Integer)pda.get("ActivityID")).intValue());
						if (state == -1) {
							successed = false;
						} else if (state == 0){
							successed = true;
						}
						//sxh modified 2007.10 end
					} else {
						keepRunning = false;
					}
					closeConnection(conn, successed);
				}
			} catch(Exception e) {
				e.printStackTrace();
				if (conn != null) {
					closeConnection(conn, false);
				}
			}
		}
		
		private void closeConnection(Connection conn, boolean submit) {
			try {
				if (submit)
					conn.commit();
				else
					conn.rollback();
				conn.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private WorkflowServer() {
		localEventQueue = new ThreadLocal();
		
		//eventQueue = Collections.synchronizedList(new LinkedList());
		
		delayActivityThread = new DelayActivityThread();
		delayActivityThread.start();
	}
	
	public static WorkflowServer getInstance() {
		return Server;
	}
	
	/**
	 * 实例化工作流为流程
	 * @param workflowID 工作流ID
	 * @param source 1：来自工作流模型实例化 2：来自项目的定义
	 * @param caseType 表明该过程是主过程还是意外过程、子过程（子项目）1：主过程 2：意外过程
	 * @param parentCaseID 如果为意外过程，子过程，则用此表明其父过程ID，否则填空
	 * @param actorType 人员类型
	 * @param actorID 人员ID
	 * @return 流程ID
	 * @throws RemoteException
	 */
	public String instantiateWorkflow(int workflowID, int source, int caseType, String parentCaseID, int actorType, int actorID) throws RemoteException {
		//servercomment System.out.println("CurrentThread: " + Thread.currentThread());
		
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		String processID = "";
		ProcessManager processManager = new ProcessManager(conn, new User(actorType, actorID));
		processManager.setInitialVariables(initialVariables);
		processID = processManager.instantiateWorkflow(conn, workflowID, source, caseType, parentCaseID);
		if (processID == "") 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		
		localEventQueue.set(processManager.getHandbackEventList());
				
		return processID;
	}
	
	/**
	 * 实例化工作流为流程
	 * @param workflowID 工作流ID
	 * @return 流程ID
	 * @throws RemoteException
	 */
	public String instantiateWorkflow(int workflowID) throws RemoteException {
		//servercomment System.out.println("CurrentThread: " + Thread.currentThread());
		
		return instantiateWorkflow(workflowID, 1, 1, "", 1, 1);
	}
	
	/**
	 * 启动流程
	 * @param processID 流程ID
	 * @param actorType 人员类型
	 * @param actorID 人员ID
	 * @return 是否启动成功
	 * @throws RemoteException
	 */
	public Object[] startProcess(String processID, int actorType, int actorID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(actorType, actorID));
		boolean succeed = processManager.startProcess(processID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());
		
		//processid, log, starttime, endtime, waittime
		Object[] result=new Object[]{processID,processManager.getProcessLog(),processManager.getTimeRecoder().getStartTime(),
				processManager.getTimeRecoder().getEndTime(),processManager.getTimeRecoder().getWaitTime()};
		return result;
	}
	
	/**
	 * 启动流程
	 * @param processID 流程ID
	 * @return 是否启动成功
	 * @throws RemoteException
	 */
	public Object[] startProcess(String processID) throws RemoteException {
		return startProcess(processID,1,1);
	}
	
	/**
	 * 得到每个service Item 的物理属性
	 * @return resutlMap  由5个key-value对组成
	 * @throws RemoteException
	 * @author cxz
	 */
	public java.util.Map<String, Double> getItemAttributions() throws RemoteException {
		Map<String, Double> result = new HashMap(5);
		
		//TODO
		result.put("CPUFrequency", 1.6);
		result.put("MemorySize", (double)1028);
		result.put("InvokingTime", (double)2000);
		result.put("CPUUsage", 60.0);
		result.put("MemoryUsage", 60.0);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see cit.workflow.engine.WorkflowService#openActivity()
	 */
	/*public boolean openActivity(String processID, int activityID, int actorType, int actorID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(actorType, actorID));
		boolean succeed = processManager.openActivity(processID, activityID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());
		
		return succeed;
	}*/
	//sxh modified 2007.10
	/**
	 * 打开活动
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param personID 人员ID
	 * @return 是否打开成功
	 * @throws RemoteException
	 */
	public int openActivity(String processID, int activityID, int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(personID));
		int succeed = processManager.openActivity(processID, activityID);
		if (succeed != 0) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());

		
		return succeed;
	}
	//sxh modified 2007.10 end

	/* (non-Javadoc)
	 * @see cit.workflow.engine.WorkflowService#submitActivity()
	 */
	/*public boolean submitActivity(String processID, int activityID, int actorType, int actorID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(actorType, actorID));
		boolean succeed = processManager.submitActivity(processID, activityID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());
		
		return succeed;
	}*/
	//sxh modifide 2007.10
	
	/**
	 * 提交活动 
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param personID 人员ID 
	 * @return 是否提交成功
	 * @throws RemoteException
	 */
	public int submitActivity(String processID, int activityID, int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(personID));
		int succeed = processManager.submitActivity(processID, activityID);
		if (succeed != 0) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());
		
		return succeed;
	}
	//sxh modified 2007.10 end
	
	//dingo add 2007
	public boolean submitAutoActivity(String processID, int activityID, int actorType, int actorID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, new User(actorType, actorID));
		boolean succeed = processManager.submitAutoActivity(processID, activityID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());
		
		return succeed;
	}
	//dingo add 2007 end
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#getTaskList()
	 */
	/*public List getTaskList(int actorType, int actorID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List taskList = new ProcessManager(conn, new User(actorType, actorID)).getTaskList();
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return taskList;
	}*/
	//sxh modified 2007.10
	/**
	 * 得到有关人员的任务列表，该列表中的任务能够被该人员查看到
	 * @param personID 人员ID
	 * @return 任务列表
	 * @throws RemoteException
	 */
	public List getTaskList(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List taskList = new ProcessManager(conn, new User(personID)).getTaskList();
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return taskList;
	}
	//sxh modified 2007.end
	
	
	public List getHistoryTaskList(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List taskList = new ProcessManager(conn, new User(personID)).getHistoryTaskList();
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return taskList;
	}
	/**
	 * 得到状态为Running的Agent任务列表
	 * @return Agent任务列表，列表中每一个对象是ProcessActivityAgent类型
	 */
	public List getAgentTaskList() {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List agentTaskList = null;
		try {
			agentTaskList = new AgentDAO(conn).getAgentTaskList();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return agentTaskList;
	}
	
	/**
	 * 得到分配给某agent的状态为Running的Agent任务列表
	 * @param agentID agent ID
	 * @return Agent任务列表，列表中每一个对象是ProcessActivityAgent类型
	 */
	public List getAgentTaskList(int agentID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List agentTaskList = null;
		try {
			agentTaskList = new AgentDAO(conn).getAgentTaskList(agentID);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return agentTaskList;
	}
	
	/**
	 * 打开agent活动
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param agentID agent ID
	 * @return
	 */
	public boolean openAgentActivity(String processID, int activityID, int agentID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, null);
		boolean succeed = processManager.openAgentActivity(processID, activityID, agentID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());

		
		return succeed;
	}
	
	/**
	 * 提交agent活动
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param agentID agent ID
	 * @return
	 */
	public boolean submitAgentActivity(String processID, int activityID, int agentID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		ProcessManager processManager = new ProcessManager(conn, null);
		boolean succeed = processManager.submitAgentActivity(processID, activityID, agentID);
		if (!succeed) 
			rollbackAction(conn);
		else {
			commitAction(conn);
		}
		
		localEventQueue.set(processManager.getHandbackEventList());

		
		return succeed;
	}
	
	public boolean canDelegate(String processID, int activityID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean result = false;
		try {
			result = new DelegationDAO(conn).canDelegate(processID, activityID);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return result;
	}
	
	/**
	 * 得到可以被转授权的角色ID数组
	 * @param delegatingRoleID 授权者角色ID
	 * @return 可以被转授权的角色ID数组
	 * @throws RemoteException
	 */
	public int[] getDelegatedRoleIDSet(int delegatingRoleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int[] roleSet = null;
		try {
			roleSet = new DelegationDAO(conn).getDelegatedRoleIDSet(delegatingRoleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return roleSet;
	}
	
	/**
	 * 2008.3
	 * 得到可以被转授权的人员列表
	 * @param delegatingRoleID 授权者角色ID
	 * @return 可以被转授权的人员列表
	 * @throws RemoteException
	 */
	public List getDelegatedPersonList(int delegatingRoleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List personList = null;
		try {
			personList = new DelegationDAO(conn).getDelegatedPersonList(delegatingRoleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		commitAction(conn);
		return personList;
	}
	
	
	
	/**
	 * 转授权
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param delegatingRoleID 授权者角色ID
	 * @param delegatedRoleID 被授权者角色ID
	 * @param delegatedPersonID 被授权者人员ID
	 * @param startTime 转授权开始时间
	 * @param endTime 转授权结束时间
	 * @return 转授权情况
	 * @throws RemoteException
	 */
	public boolean delegate(String processID, int activityID, int delegatingRoleID, int delegatedRoleID, int delegatedPersonID, Date startTime, Date endTime) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean success = false;
		try {
			success = new DelegationDAO(conn).delegate(processID, activityID, delegatingRoleID, delegatedRoleID, delegatedPersonID, startTime, endTime);
			if (success) {
				bindingActivitytoPerson(delegatedPersonID, processID, activityID);
				unbindingActivitytoRole(delegatingRoleID, processID, activityID);
			}
			commitAction(conn);
		} catch (Exception ex) {
			ex.printStackTrace();
			rollbackAction(conn);
		}
		return success;
	}
	/**
	 * 转授权
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param delegatingRoleID 授权者角色ID
	 * @param delegatedRoleID 被授权者角色ID
	 * @param delegatedPersonID 被授权者人员ID
	 * @return 转授权情况
	 * @throws RemoteException
	 */
	public boolean delegate(String processID, int activityID, int delegatingRoleID, int delegatedRoleID, int delegatedPersonID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean success = false;
		try {
			success = new DelegationDAO(conn).delegate(processID, activityID, delegatingRoleID, delegatedRoleID, delegatedPersonID, new Date(), new Date());
			if (success) {
				bindingActivitytoPerson(delegatedPersonID, processID, activityID);
			}
			commitAction(conn);
		} catch (Exception ex) {
			ex.printStackTrace();
			rollbackAction(conn);
		}
		return success;
	}

	/**
	 * 接受任务
	 * @param personID 人员ID
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @return 是否接受成功
	 * @throws RemoteException
	 */
	public boolean acceptTask(int personID, String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean success = new ProcessManager(conn, new User(personID)).acceptTask(processID, activityID);
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return success;
	}
	
	/**
	 * 把还没有激活的活动与某个用户直接绑定
	 * @param personID 人员ID
	 * @param processID 流程ID
	 * @param activityID 活动ID 
	 * @return 绑定结果代码，1代表绑定成功，0代表活动已分配给他，-1代表该活动不是原子活动，-2代表该活动已结束，-3代表活动已分配，-4代表无相应角色
	 * @throws RemoteException
	 */
	public int bindingActivitytoPerson(int personID, String processID, int activityID) throws RemoteException{
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int flag = new ProcessManager(conn, new User(personID)).bindingActivitytoPerson(personID, processID, activityID);
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return flag;
	}
	/**
	 * 2008.3
	 * 对活动解除人员绑定
	 * @param personID
	 * @param processID
	 * @param activityID
	 * @return
	 * @throws RemoteException
	 */
	public int unbindingActivitytoPerson(int personID, String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int flag = new ProcessManager(conn, new User(personID)).unbindingActivitytoPerson(personID, processID, activityID);
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
		return flag;
	}
	
	public void unbindingActivitytoRole(int roleID, String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		new ProcessManager(conn, new User(roleID)).unbindingActivitytoRole(roleID, processID, activityID);
		try {
			conn.close();
		} catch(Exception e) {
			//servercomment System.out.println(e.getMessage());
		}
	}
	
	public int getActivitytoPerson(String processID, int activityID, int roleID) throws RemoteException {
		return -1;
	}

	/**
	 * 给流程分配人员
	 * @param processID 流程ID
	 * @param personID 人员ID
	 * @param roleID 角色ID
	 * @return 是否分配成功
	 * @throws RemoteException
	 */
	public boolean addProcessPerson(String processID, int personID, int roleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		succeed = new ProcessDAO(conn).addProcessPersonBinding(processID, personID, roleID);
		commitAction(conn);
		return succeed;
	}
	/**
	 * 给复合活动分配人员
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param personID 人员ID
	 * @param roleID 角色ID
	 * @return 是否分配成功
	 * @throws RemoteException
	 */
	public boolean addProcessActivityTeamMember(String processID, int activityID, int personID, int roleID) throws RemoteException{
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		succeed = new ProcessDAO(conn).addProcessActivityTeamMember(processID, activityID, personID, roleID);
		commitAction(conn);
		return succeed;
	}
	//sxh add 2006.10 end
	
	public boolean deleteMaxProcess() throws RemoteException{
		boolean succeed = false;
		/*
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Model model = new Model(conn, 1, null);
		int maxProcessID = model.getMaxProccessID();
		Process process = new Process(conn, maxProcessID, null);
		succeed = process.deleteProcess();
		if (!succeed) 
			rollbackAction(conn);
		else
			commitAction(conn);*/
			
		return succeed;
	}
	
	
	
	public List getWorkflowObject(Map paramMap) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new WorkflowDAO(conn).getWorkflowObject(paramMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}
	
	private void closeAction(Connection connection) {
		try {
			connection.close();
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection close error, message: " + e.getMessage());
		}
	}
	
	private void rollbackAction(Connection connection) {
		try {
			connection.rollback();
			connection.close();
			////servercomment System.out.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback and close");
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback error, message: " + e.getMessage());
		}
	}
	
	private void commitAction(Connection connection) {
		try {
			connection.commit();
			connection.close();
			////servercomment System.out.println("Execution thread: " + Thread.currentThread().getName() + ", Connection commit and close");
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection commit error, message: " + e.getMessage());
		}
	}

	public List getEventQueue() {
		return (List) localEventQueue.get();
	}
	
	
	public static void main(String[] args){
		WorkflowServer cxz = new WorkflowServer();
		Map result;
		try{
			
			result = cxz.getItemAttributions();
			Set keySet = result.keySet();
			Iterator keyIterator = keySet.iterator();
			while(keyIterator.hasNext()){
				String tempKey = (String)keyIterator.next();
				//servercomment System.out.println(tempKey + ":  " + result.get(tempKey));
			}

			//servercomment System.out.println(cxz.getItemAttributions().hashCode());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
