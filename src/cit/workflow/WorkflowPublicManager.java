/*
 * Created on 2005-3-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Random;

import org.apache.commons.dbutils.handlers.MapListHandler;

import java.util.Random;


import cit.workflow.dao.PersonRoleDAO;
import cit.workflow.dao.ProcessDAO;
import cit.workflow.dao.WorkflowDAO;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WorkflowPublicManager {
	private static final WorkflowPublicManager publicManager = new WorkflowPublicManager();

	private WorkflowPublicManager() {
		// TODO
	}

	public static WorkflowPublicManager getInstance() {
		return publicManager;
	}
	
	/**
	 * 根据角色得到工作流信息列表
	 * 
	 * @param roleID
	 *            角色roleID
	 * @param functionType
	 *            角色功能类型
	 * @return 工作流信息列表
	 * @throws RemoteException
	 */
	public List getWorkflow(int roleID, int functionType) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new WorkflowDAO(conn)
					.getWorkflow(roleID, functionType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}
	
	/**
	 * 根据工作流ID得到该工作流的子工作流
	 * @param workflowID 工作流ID
	 * @return 子工作流信息列表
	 * @throws RemoteException
	 */
	public List getSubWorkflow(int workflowID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new WorkflowDAO(conn).getSubWorkflow(workflowID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}

	/**
	 * 根据用户名得到用户信息
	 * @param userName 用户名
	 * @return 用户信息
	 * @throws RemoteException
	 */
	public Map getUserInfomation(String userName) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Map resultMap = null;
		try {
			resultMap = new PersonRoleDAO(conn).getUserInfomation(userName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultMap;
	}
	
	
	/**
	 * 得到工作流活动所分配的角色ID数组
	 * @param workflowID 工作流ID
	 * @param activityID 活动ID
	 * @return 角色ID数组
	 * @throws RemoteException
	 */
	public int[] getWorkflowActivityRoleIDSet(int workflowID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int[] resultSet = null;
		try {
			resultSet = new PersonRoleDAO(conn).getWorkflowActivityRoleIDSet(workflowID, activityID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultSet;
	}
	
	/**
	 * 得到流程活动所分配的角色ID数组
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @return 角色ID数组
	 * @throws RemoteException
	 */
	public int[] getProcessActivityRoleIDSet(String processID, int activityID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int[] resultSet = null;
		try {
			resultSet = new PersonRoleDAO(conn).getProcessActivityRoleIDSet(processID, activityID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultSet;
	}
	
	/**
	 * 得到该人员被分配的角色ID数组
	 * @param personID 人员ID
	 * @return 角色ID数组
	 * @throws RemoteException
	 */
	public int[] getRoleIDSet(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int[] resultSet = null;
		try {
			resultSet = new PersonRoleDAO(conn).getRoleIDSet(personID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultSet;
	}
	
	/**
	 * 根据角色ID得到角色信息
	 * @param roleID 角色ID
	 * @return 角色信息，以Map形式返回
	 * @throws RemoteException
	 */
	public Map getRoleInformation(int roleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Map resultMap = null;
		try {
			resultMap = new PersonRoleDAO(conn).getRoleInformation(roleID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return resultMap;
	}
	
	
	/**
	 * 根据人员ID得到角色信息列表
	 * @param personID 人员ID
	 * @return 角色信息列表
	 * @throws RemoteException
	 */
	public List getRoleInformationList(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new PersonRoleDAO(conn).getRoleInformationList(personID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}
		return resultList;
	}
	
	
	
	/**
	 * 得到角色所分配的人员ID数组
	 * @param roleID 角色ID
	 * @return 人员ID数组
	 * @throws RemoteException
	 */
	public int[] getPersonIDSet(int roleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		int[] resultSet = null;
		try {
			resultSet = new PersonRoleDAO(conn).getPersonIDSet(roleID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultSet;
	}
	
	/**
	 * 根据人员ID得到人员信息 
	 * @param personID 人员ID
	 * @return 人员信息，以Map形式返回
	 * @throws RemoteException
	 */
	public Map getPersonInformation(int personID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Map resultMap = null;
		try {
			resultMap = new PersonRoleDAO(conn).getPersonInformation(personID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultMap;
	}
	
	/**
	 * 得到角色所分配的人员信息列表
	 * @param roleID 角色ID
	 * @return 人员信息列表
	 * @throws RemoteException
	 */
	public List getPersonInformationList(int roleID) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new PersonRoleDAO(conn).getPersonInformationList(roleID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAction(conn);
		}

		return resultList;
	}

	public double getRequestLoad(String processID, String eventExpression)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		double result = 0.0;
		try {
			result = new WorkflowDAO(conn).getRequestLoad(processID,
					eventExpression);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return result;
	}

	/**
	 * 功能：获得某活动的输入对象列表
	 * 
	 * @param processID 该活动所属的流程ID
	 * @param activityID 该活动ID
	 * @return 输入对象列表
	 * @throws RemoteException
	 */
	public List getInputObjectList(String processID, int activityID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn).getInputObjectList(processID,
					activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}

	//sxh add 2007.9
	/**
	 * 获得某活动的内部生成对象列表
	 * 
	 * @param processID 该活动所属的流程ID
	 * @param activityID 该活动ID
	 * @return 内部生成对象列表
	 * @throws RemoteException
	 */
	public List getInternalObjectList(String processID, int activityID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn).getInternalObjectList(processID,
					activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}
	//sxh add 2007.9 end

	/**
	 * 获得某活动的输出对象列表
	 * 
	 * @param processID 该活动所属的流程ID
	 * @param activityID 该活动ID
	 * @return 输出对象列表
	 * @throws RemoteException
	 */
	public List getOutputObjectList(String processID, int activityID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn).getOutputObjectList(processID,
					activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}

	/**
	 * 获得流程输入对象列表
	 * 
	 * @param processID 该活动所属的流程ID
	 * @return 流程输入对象列表
	 * @throws RemoteException
	 */
	public List getProcessInputObjectList(String processID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn)
					.getProcessInputObjectList(processID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}

	/**
	 * 获得流程输出对象列表
	 * 
	 * @param processID 该活动所属的流程ID
	 * @return 流程输出对象列表
	 * @throws RemoteException
	 */
	public List getProcessOutputObjectList(String processID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn)
					.getProcessOutputObjectList(processID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}

	/**
	 * 根据条件参数得到对象列表
	 * 
	 * @param paramMap
	 *            条件参数，将多组条件以Map形式封装，每一组条件对应于一组key和value，key为数据库中字段名称，value为字段的值
	 * @return 符合条件的对象列表
	 * @throws RemoteException
	 */
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

	/**
	 * 得到对象的值
	 * 
	 * @param processID
	 *            对象所属流程ID
	 * @param objectID
	 *            对象ID
	 * @return 对象的值
	 * @throws RemoteException
	 */
	public Object getProcessObjectValue(String processID, int objectID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Object result = null;
		try {
			ProcessObject workflowObject = ElementFactory.createProcessObject(
					conn, processID, objectID);
			result = workflowObject.getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		return result;
	}

	/**
	 * 得到文档对象的路径
	 * 
	 * @param processID
	 *            文档对象所在流程ID
	 * @param objectID
	 *            文档对象ID
	 * @return 路径对象
	 * @throws RemoteException
	 */
	public Object getProcessDocumentPath(String processID, int objectID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Object result = null;
		try {
			ProcessObject workflowObject = ElementFactory.createProcessObject(
					conn, processID, objectID);

			DocumentVariable dv = (DocumentVariable) workflowObject;
			result = dv.getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		return result;
	}

	

	/**
	 * get the objects list of some activiy based on some process
	 */
	public List getActivityObjectFlow(String processID, int activityID)
			throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		List resultList = null;
		try {
			resultList = new ProcessDAO(conn).getActivityObjectFlow(processID,
					activityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return resultList;
	}



	/**
	 * 修改变量的值
	 * 
	 * @param processID
	 *            变量所在流程ID
	 * @param objectID
	 *            变量ID
	 * @param value
	 *            待修改的值
	 * @return 是否修改成功
	 * @throws RemoteException
	 */
	public boolean setProcessObjectValue(String processID, int objectID,
			String value) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		try {
			ProcessObject workflowObject = ElementFactory.createProcessObject(
					conn, processID, objectID);
			workflowObject.setValue(value);
			succeed = true;
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
	
	/**
	 * 2008.3
	 * 上传文档并进行更新
	 * @param path 文档路径
	 * @param content 文档内容，以byte串传输
	 * @return 是否更新成功
	 */
	public boolean uploadDocument(String path, byte[] content) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean success = false;

		try {
			FileOutputStream out = new FileOutputStream(path);
			out.write(content);
			out.close();
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		return success;
	}
	
	/**
	 * 2008.3
	 * 下载文档
	 * @param path 文档路径
	 * @return 文档内容，以byte串传输
	 * @throws RemoteException
	 */
	public byte[] downloadDocument(String path) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		File file = new File(path);
		long length = file.length();
		byte[] content = new byte[(int)length];
//		Byte[] result = new Byte[(int)length];
		boolean success = false;

		try {
			FileInputStream in = new FileInputStream(path);
			in.read(content);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*for (int i = 0; i < content.length; ++i) {
			result[i] = new Byte(content[i]);
		}*/
		commitAction(conn);
		return content;
	}

	/**
	 * 上传文档
	 * @param processID 文档所在流程ID
	 * @param activityID 文档所属活动ID
	 * @param objectID 文档对象ID
	 * @param content 文档内容，以byte串传输
	 * @return 是否更新成功
	 * @throws RemoteException
	 */
	public boolean uploadDocument(String processID, int activityID,
			int objectID, byte[] content) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		Object result = null;
		Map activityMap = null;
		String state = null;
		boolean success = false;

		try {
			activityMap = new ProcessDAO(conn).getActivityInformation(
					processID, activityID);
			if (activityMap != null) {
				state = (String) activityMap.get("state");
				if (state.equals(Constants.ACTIVITY_STATE_RUNNING)) {
					ProcessObject workflowObject = ElementFactory
							.createProcessObject(conn, processID, objectID);
					DocumentVariable docVariable = (DocumentVariable) workflowObject;
					String path = docVariable.getPath().toString();
					FileOutputStream out = new FileOutputStream(path);
					out.write(content);
					out.close();
					success = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);
		return success;
	}



	public void myTestService() {
		int SleepTime = 0;
		Random random = null;
		for(int i=1,k=1;i<5;i++){
			random = new Random();
			SleepTime += k*random.nextInt(10);
			k=k*10;
		}
		//servercomment System.out.println("This is my test service! operating Time: "+SleepTime +" milliSeconds...");
		try{
			Thread.sleep(SleepTime);
		}catch(Exception e){
			e.printStackTrace();
		}
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

	public synchronized void  myTestService(int level) throws InterruptedException {
		if(level<1||level>6){
			System.err.println("level must between 1 and 6!");
			return;
		}
		Random random = new Random(47);
		double rand = random.nextDouble();
		long SleepTime = 0;
		switch(level){
		case 1: SleepTime = (long)(rand *10); break;
		case 2: SleepTime = (long)(10 + rand *10); break;
		case 3: SleepTime = (long)(100 + rand *100); break;
		case 4: SleepTime = (long)(1000 + rand *1000); break;
		case 5: SleepTime = (long)(5000 + rand *5000); break;
		case 6: SleepTime = (long)(10000 + rand *10000); break;
		default: return;
		}
		//servercomment System.out.println(" ^_^ This is cxz's test service! It will sleep for "+SleepTime+" Milliseconds...");
		
		Thread.sleep(SleepTime);
		
		
		

	}

	
	private void closeAction(Connection connection) {
		try {
			connection.close();
		} catch (Exception e) {
			System.err
			.println("Execution thread: "
					+ Thread.currentThread().getName()
					+ ", Connection close error, message: "
					+ e.getMessage());
		}
	}
	
	private void rollbackAction(Connection connection) {
		try {
			connection.rollback();
			connection.close();
		} catch (Exception e) {
			System.err
					.println("Execution thread: "
							+ Thread.currentThread().getName()
							+ ", Connection rollback error, message: "
							+ e.getMessage());
		}
	}

	
	private void commitAction(Connection connection) {
		try {
			connection.commit();
			connection.close();
		} catch (Exception e) {
			System.err.println("Execution thread: "
					+ Thread.currentThread().getName()
					+ ", Connection commit error, message: " + e.getMessage());
		}
	}

	
	public static void main(String args[]){
		WorkflowPublicManager cxz = new WorkflowPublicManager();
		for(int i=1;i<=6;i++)
			try {
				cxz.myTestService(i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
	}

}
