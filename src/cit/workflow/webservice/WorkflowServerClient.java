package cit.workflow.webservice;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;


public class WorkflowServerClient {
	
	private WorkflowServerInterface client;
	private String urlstring;
	
	public WorkflowServerClient(String url1){
		this.urlstring=url1;
		connect();
	}
	
	public void connect() {
		try {
//			URL url = new URL(
//					"http://localhost:8080/axis2/services/WorkflowServerImpl?wsdl");
//			QName qname = new QName("http://webservice.workflow.cit/",
//					"WorkflowServerImpl");
//			Service service = Service.create(url, qname);
//			client = service.getPort(new QName(
//					"http://webservice.workflow.cit/",
//					"WorkflowServerImplServiceHttpSoap12Endpoint"),
//					WorkflowServerInterface.class);
			URL url = new URL(urlstring);
			QName qname = new QName("http://webservice.workflow.cit/",
					"WorkflowServerImplService");
			Service service = Service.create(url, qname);
			client = service.getPort(
					new QName("http://webservice.workflow.cit/",
							"WorkflowServerImplPort"),
					WorkflowServerInterface.class);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
//		//servercomment System.out.println("connect successfully");
	}

	public String instantiateWorkflow(int workflowID, 
			int source, int caseType, String parentCaseID, 
			int actorType, int actorID) throws RemoteException{
		String processID=client.instantiateWorkflow(workflowID);
		return processID;
	}

	public Object[] startProcess(String processID, int actorType, int actorID) 
			throws RemoteException{
		return client.startProcess(processID);
	}
	
	public static void main(String[] args){
		WorkflowServerClient client=new WorkflowServerClient("http://localhost:8080/workflow/Workflow?wsdl");
	}
}
