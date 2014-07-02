/*
 * Created on 2004-12-22
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.webservice;

import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.jws.WebService;

import cit.workflow.WorkflowServer;
import cit.workflow.monitor.PerfData;


@WebService(endpointInterface="cit.workflow.webservice.WorkflowServerInterface")
public class WorkflowServerImpl implements WorkflowServerInterface{
	private PerfData perfData=null;
	public WorkflowServerImpl(){
		System.out.println("--------WS Service constructed, version 1.13-------");
		DatabaseManager dbm=new DatabaseManager();
		if(dbm.checkDatabase()){
			System.out.println("Database "+DatabaseManager.WORKFLOWDATABASE+" found");
		}else{
			System.out.println("Database "+DatabaseManager.WORKFLOWDATABASE+" not found");
			dbm.importDatabase();
		}
		dbm.close();
		perfData=new PerfData();
		perfData.setInteval(10000);
		System.out.println("------------------Service Start--------------------");
	}
	
	public String instantiateWorkflow(int workflowID) {
		String processID = null;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			processID = ws.instantiateWorkflow(workflowID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return processID;
	}
	
	public Object[] startProcess(String processID) throws RemoteException {
		Object[] result=null;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			result = ws.startProcess(processID);
			System.out.println(processID+" process complete");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public Object[] executeWorkflow(int workflowID){
		/*
		//processid, log, starttime, endtime, waittime
		Object[] result=null;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			String processID = ws.instantiateWorkflow(workflowID);
			result = ws.startProcess(processID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
		*/
		long t1=System.currentTimeMillis();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long t2=System.currentTimeMillis();
		Object[] result={"xxx","",t1,t2,0};
		return result;
		
	}
	
//	public TimeSeries getCPUSeries(){
//		return perfData.getCpuTimeSeries();
//	}
//	
//	public TimeSeries getMemorySeries(){
//		return perfData.getMemoryTimeSeries();
//	}
	
	public Object[] getServerInfo(){
		Object[] info={perfData.getTotalPhysicalMemory()};
		return info;
	}
	
	public LinkedList<Object[]> getCpuPerfList() {
		return perfData.getCpuPerfList();
	}

	public LinkedList<Object[]> getMemoryPerfList() {
		return perfData.getMemoryPerfList();
	}
	
	public void setMonitorInteval(long inteval){
		perfData.setInteval(inteval);
	}
	
	public void setRun(boolean run){
		perfData.setRun(run);
		System.out.println("set Run to "+run);
	}
	
	
	@Override
	public void finalize(){
		perfData.setRun(false);
		System.out.println("--------------Workflow Unconstructed---------------");
	}
	
	private static int count=1;
	private static int nowcount=0;
	public static void main(String[] args) {
//		System.out.println(BasicDataSource.class.getProtectionDomain().getCodeSource().getLocation());
		WorkflowServerImpl ws=new WorkflowServerImpl();
		for(int i=0;i<count;i++){
			new TestThread(ws).start();
		}
	}
	
	public static class TestThread extends Thread{
		private WorkflowServerImpl ws;
		public int workflowID=18;
		public TestThread(WorkflowServerImpl ws){
			this.ws=ws;
		}
		@Override
		public void run() {
			String processID = ws.instantiateWorkflow(workflowID);
			try {
				ws.startProcess(processID);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			nowcount++;
			if(nowcount==count)System.out.println("-----------------ALL OVER-----------------");
		}
	}
}
