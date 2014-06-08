//package cit.workflow.webservice;
//
//import java.rmi.RemoteException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.jws.WebService;
//
//import org.jfree.data.time.TimeSeries;
//
//
//
//
///**
// * @author weiwei
// *
// * TODO To change the template for this generated type comment go to
// * Window - Preferences - Java - Code Style - Code Templates
// */
//@WebService(endpointInterface="cit.workflow.webservice.WorkflowServerInterface")
//public class WorkflowServerTest implements WorkflowServerInterface{
//	private Thread thread;
//	private boolean running;
//
//	public class Testthread implements Runnable{
//		
//		@Override
//		public void run() {
//			try {
//				//servercomment System.out.println("Workflow engine service start");
//				while (running) {
//					Thread.sleep(1000);
//					//servercomment System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date())+ " Workflow Service Running");
//				}
//				//servercomment System.out.println("Workflow engine service stop");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		}
//	}
//	
//	public String instantiateWorkflow(int workflowID) {
//		return "processID";
//	}
//	
//	public void start(){
//		running=true;
//		if(thread==null || !thread.isAlive()){
//			thread=new Thread(new Testthread());
//			thread.setDaemon(true);
//			thread.start();
//		}
//	}
//	
//	public void stop(){
//		running=false;
//	}
//	
//
//	public boolean startProcess(String processID) throws RemoteException {
//		return true;
//	}
//	
//	public WorkflowServerTest(){
//		thread=new Thread(new Testthread());
//		thread.setDaemon(true);
//		//servercomment System.out.println("Workflow Engine Service constructed");
//	}
//	
//	@Override
//	protected void finalize() {
//		//servercomment System.out.println("Workflow Engine Service destructed");
//	}
//	
//	public static void main(String[] args) {
//		//servercomment System.out.println("Main Function:");
//		WorkflowServerTest test=new WorkflowServerTest();
//		try {
//			test.start();
//			Thread.sleep(5000);
//			test.stop();
//			Thread.sleep(5000);
//			test.start();
//			Thread.sleep(5000);
//		}  catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//
//
//
//	@Override
//	public void setMonitorInteval(long inteval) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setRun(boolean run) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//}
