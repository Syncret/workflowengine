package cit.workflow.webservice;

import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.DOCUMENT)
public interface WorkflowServerInterface {
	@WebMethod
	public String instantiateWorkflow(int workflowID)
			throws RemoteException;

	@WebMethod
	public Object[] startProcess(String processID)
			throws RemoteException;
	
	@WebMethod
	public Object[] executeWorkflow(int workflowID)
			throws RemoteException;
	
	@WebMethod
	public Object[] getServerInfo();
	
	@WebMethod
	public LinkedList<Object[]> getCpuPerfList();

	@WebMethod
	public LinkedList<Object[]> getMemoryPerfList();
	
	
	@WebMethod
	public void setMonitorInteval(long inteval);
	
	@WebMethod
	public void setRun(boolean run);
}
