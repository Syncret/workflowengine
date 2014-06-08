/*
 * Created on 2004-11-21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.engine;


/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface LifeCycleAdmin {
	
	/**
	 * stop service, remove from lookupservice
	 * @throws java.rmi.RemoteException
	 */
	public void stop() throws java.rmi.RemoteException;
	
	/**
	 * start service, register from lookupservice
	 * @throws java.rmi.RemoteException
	 */
	public void start() throws java.rmi.RemoteException;
}
