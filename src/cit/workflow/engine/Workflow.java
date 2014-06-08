/*
 * Created on 2004-10-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.engine;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.List;

import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Workflow {
	
	//实例化流程
	public String instantiateWorkflow(int workflowID, int source, int caseType, String parentCaseID, int actorType, int actorID) throws RemoteException, UnknownEventException;

	//启动流程
	public boolean startProcess(String processID, int actorType, int actorID)throws RemoteException;
	
	//打开活动
	public boolean openActivity(String processID, int activityID, int actorType, int actorID)throws RemoteException;
	
	//提交活动
	public boolean submitActivity(String processID, int activityID, int actorType, int actorID)throws RemoteException;
	
	//获得任务列表
	public List getTaskList(int actorType, int actorID) throws RemoteException;
	
	//注册监听事件
	public EventRegistration registerListener(RemoteEventListener listener, long duration, MarshalledObject handback) throws RemoteException;
}
