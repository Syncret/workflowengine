/*
 * Created on 2005-4-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow;

import java.rmi.RemoteException;
import java.sql.Connection;

import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.utils.WorkflowConnectionPool;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkflowStatisticManager {
    
	private static final WorkflowStatisticManager statisticManager = new WorkflowStatisticManager();
	
	private WorkflowStatisticManager() {
		//TODO 
	}
	
	public static WorkflowStatisticManager getInstance() {
		return statisticManager;
	}
	
	/**
	 * 修改流程对象的XML字段的值
	 * @param processID 流程ID
	 * @param objectID 对象ID，可以是XML对象和文档对象
	 * @param value XML字段的值
	 * @return 是否修改成功
	 * @throws RemoteException
	 */
	public boolean setProcessObjectValue(String processID, int objectID, String value) throws RemoteException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		boolean succeed = false;
		try {
			ProcessObject workflowObject = ElementFactory.createProcessObject(conn, processID, objectID);
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
