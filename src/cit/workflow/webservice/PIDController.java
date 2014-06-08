package cit.workflow.webservice;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cit.workflow.utils.WorkflowConnectionPool;


public class PIDController {
	
	private static Logger logger = Logger.getLogger(HostManager.class);
	/* 用于存储各个host执行工作流所消耗的时间比率 */
	private Map<String,Double> hosttimeMap=new HashMap<String,Double>();
	
//	/* 用于存储各个host执行工作流所消耗的平均时间 */
//	private Map<String,Double> averageTimeMap=new HashMap<String,Double>();
	
	/*用于存储最后几次执行工作流所消耗的时间比率*/
	private List<Double> recentTimeList=new ArrayList<Double>();
	
	/*用于存储host与其性能*/
	private Map<String,Integer> hostPerformanceMap=new HashMap<String,Integer>();
	
	private int recentTimeNum=10;
	public void setRecentTimeListNum(int recentTimeListNum){this.recentTimeNum=recentTimeListNum;}
	public int getRecentTimeListNum(){return this.recentTimeNum;}
	
	/* 用于记录每个host上正在运行的workflow数目*/
	private Map<String,Integer> flowMap=new HashMap<String,Integer>();
	
	/*指定用于比较的平均时间*/
	private double refertime=9000;
	public void setRefertime(double refertime){this.refertime=refertime;}
	public double getRefertime(){return refertime;}
	
	
	//互斥锁用于对map的读取
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();  
    private final Lock readLock = rwl.readLock();  
    private final Lock writeLock = rwl.writeLock(); 
    
    /*自配置线程*/
    private SelfConfigThread selfConfigThread;
    public void setConfigTime(long time){selfConfigThread.setConfigTime(time);}
	public long getConfigTime(){return selfConfigThread.getConfigTime();}
	//PID控制参数，比例系数与积分时间常数
	private double cp,ci;
	public void setPIDparameters(double cp,double ci){this.cp=cp;this.ci=ci;}
	
	
    public PIDController(){
    	selfConfigThread=new SelfConfigThread();
    	selfConfigThread.start();
    }
    
	/**
	 * 用于将一个工作流分配到指定的host,并记录执行时间
	 */
	public class assignedThread extends Thread{
		private int workflowID;
		private int source;
		private int caseType;
		private String parentCaseID;
		private int actorType;
		private int actorID;
		private String processID;
		
		private String host;
		
		public assignedThread(String host){
			this.host=host;
		}
		
		public void setParameters(int workflowID, int source,
			int caseType, String parentCaseID, int actorType, int actorID){
			this.workflowID=workflowID;
			this.source=source;
			this.caseType=caseType;
			this.parentCaseID=parentCaseID;
			this.actorType=actorType;
			this.actorID=actorID;
		}
		
		public void run(){
			long startTime=System.currentTimeMillis();
			try {
				WorkflowServerClient ws =new WorkflowServerClient(host+"/workflow/Workflow?wsdl");
				processID = ws.instantiateWorkflow(workflowID, source, caseType,
						parentCaseID, actorType, actorID);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Object[] result=null;
			try {
				WorkflowServerClient ws = new WorkflowServerClient(host+"/workflow/Workflow?wsdl");
				result = ws.startProcess(processID, actorType, actorID);
//				//servercomment System.out.println("Start Process = " + success);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			long endTime=System.currentTimeMillis();
			int runTime=(int)(endTime-startTime);
//			//servercomment System.out.println(runTime);
			//servercomment System.out.println("workflow finished on\t"+host+"\t"+runTime);
			writeLock.lock();
			double tempRatio=runTime/refertime-1;
			Double d=hosttimeMap.get(host);
			if(d!=null)hosttimeMap.put(host, tempRatio);
			int num=flowMap.get(host)-1;
			flowMap.put(host, num);
			if(recentTimeList.size()==recentTimeNum){
				recentTimeList.remove(0);
			}
			recentTimeList.add(tempRatio);
			writeLock.unlock();
			if(num==0){
				Connection conn = WorkflowConnectionPool.getInstance().getConnection();
				HostManager hostmanager=new HostManager(conn);
				hostmanager.changeHostStatus(host, 1);
				commitAction(conn);
				writeLock.lock();
//				hosttimeMap.put(host, (double) -1);//注意考虑此host被reduce以后不应该再加入此项
				writeLock.unlock();
			}
		}
		
		
	}
	
	
	/*指定一个host运行workflow*/
	public void runWorkflow(int workflowID, int source,
			int caseType, String parentCaseID, int actorType, int actorID){
		String host;
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		HostManager hostmanager=new HostManager(conn);
		
		//先判断是否有空闲的host
		host=hostmanager.getIdleHost();
		
		
		if(host!=null){//如果有，修改其对应状态
			hostmanager.changeHostStatus(host, 2);
			int performance=hostmanager.getPerformance(host);
			hostPerformanceMap.put(host, performance);
			writeLock.lock();
			hosttimeMap.put(host, (double) 0);
			flowMap.put(host, 1);
			writeLock.unlock();
		}
		else {//如果没有，找出负载最低的host
			Double mintime=Double.MAX_VALUE;
			readLock.lock();
			Set<String> keyset = hosttimeMap.keySet();
	        for (String keyhost:keyset) {
	        	double a=(double)flowMap.get(keyhost)/hostPerformanceMap.get(keyhost);
	            if(a<mintime){
	            	host=keyhost;
	            	mintime=a;
	            }
	        }
	        readLock.unlock();
	        writeLock.lock();
	        flowMap.put(host, flowMap.get(host)+1);
	        writeLock.unlock();
		}
		assignedThread a=new assignedThread(host);
		a.setParameters(workflowID, source, caseType, parentCaseID, actorType, actorID);
		logger.info("start workflow "+workflowID+" on "+host);
		commitAction(conn);
		a.start();
	}
	
	/*每隔一段时间进行自配置的线程*/
	public class SelfConfigThread extends Thread {
		
		/*设定多久时间一次进行自配置*/
		private long configTime=10000;
		public void setConfigTime(long time){this.configTime=time;}
		public long getConfigTime(){return configTime;}
		
		private boolean keeprunning=true;
		
		public SelfConfigThread(){
			setDaemon(true);
		}
		
		public void terminate(){keeprunning=false;}
		
		public void run(){
			while(keeprunning){
				
				try {//每隔一定时间执行一定自配置算法
					Thread.sleep(configTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//计算PID控制中的比例一项
				double proportion=0;
				readLock.lock();
				Set<String> keyset = hosttimeMap.keySet();
				if(keyset.size()==0){
					readLock.unlock();
					continue;
				}
		        for (String keyhost:keyset) {
		        	double tempRatio=hosttimeMap.get(keyhost);
		            if(tempRatio!=0){
		            	proportion+=tempRatio;
		            }
		        }
		        proportion/=keyset.size();
		        readLock.unlock();
		        //计算PID控制中的积分一项
		        double integral=0;
		        readLock.lock();
		        for(double tempRatio:recentTimeList){
		        	integral+=tempRatio;
		        }
		        readLock.unlock();
		        //使用PID算法计算控制量
		        double ct=cp*proportion+ci*integral;
//---------------------------------
	        	//servercomment System.out.println(hosttimeMap);
//	        	//servercomment System.out.println(recentTimeList);
	        	//servercomment System.out.println(proportion);
	        	//servercomment System.out.println(integral);
	        	//servercomment System.out.println(ct);
//---------------------------------
	        	int totalPerformance=0;
	        	Connection conn = WorkflowConnectionPool.getInstance().getConnection();
	    		HostManager hostmanager=new HostManager(conn);
	        	List<String> hosts=hostmanager.getDeployedHosts();
	    		for(String keyhost:hosts){
	    			Integer hostPerformance=hostPerformanceMap.get(keyhost);
	    			if(hostPerformance==null){
	    				hostPerformance=hostmanager.getPerformance(keyhost);
	    				hostPerformanceMap.put(keyhost, hostPerformance);
	    			}
	    			totalPerformance+=hostPerformance;
	    		}
	    		ct*=totalPerformance;
	    		//servercomment System.out.println("*"+totalPerformance+"="+ct);
		        //根据计算出的控制量进行自配置
		        if(ct>=1){//需要增加host
		        	hosts=hostmanager.getAvailableHosts();
//		        	String host0=hosts.get(0);
//		        	int hostsMinPerformance=hostmanager.getPerformance(host0);
//		        	if(ct-hostsMinPerformance<0) {
//		        		logger.info("PID:close Performance: "+host0+" "+hostsMinPerformance);
//		        		continue;
//		        	}
		        	if(hosts.size()==0){
		        		logger.info("No more host available");
		        	}
		    		else{
		    			String host=getClosePerformanceHost(ct,hosts);
			    		if(host==null) logger.info("Maintain the status quo");
			    		else{
			    			logger.info("PID:plan to add host");
//			    			boolean deployResult=new RemoteDeploy().deploy(host);
			    			boolean deployResult=true;
			    			if(deployResult){
			    				hostmanager.changeHostStatus(host, 1);
			    				logger.info("Deploy successfully on "+host);
			    			}
			    			else logger.info("Deploy failed");
			    		}
		    		}
		    		commitAction(conn);
		        }
		        if(ct<=-1){//需要减少host
//		        	String host0=hosts.get(0);
//		        	int hostsMinPerformance=hostmanager.getPerformance(host0);
//		        	if(ct+hostsMinPerformance>0) {
//		        		logger.info("PID:close Performance: "+host0+" "+hostsMinPerformance);
//		        		continue;
//		        	}
					if (hosts.size() == 1) {
						logger.info("Only one host is running");
						String nowHost=hosts.get(0);
						hosts=hostmanager.getAvailableHosts();
						String tempHost=getClosePerformanceHost(totalPerformance+ct,hosts);
						if(tempHost!=null && nowHost!=tempHost){
							hostmanager.changeHostStatus(nowHost, 0);
							hosttimeMap.remove(nowHost);
							logger.info("Stop service on " + nowHost);
//			    			boolean deployResult=new RemoteDeploy().deploy(tempHost);
			    			boolean deployResult=true;
			    			if(deployResult){
			    				hostmanager.changeHostStatus(tempHost, 1);
			    				logger.info("Deploy successfully on "+tempHost);
			    			}
						}
						continue;
					}
					hosts=hostmanager.getDeployedHosts();
					String host=getClosePerformanceHost(ct,hosts);
					if(host==null)logger.info("Maintain the status quo");
					else{
						hostmanager.changeHostStatus(host, 0);
						hosttimeMap.remove(host);
						logger.info("Stop service on " + host);
					}
					commitAction(conn);
		        }
			}
		}
	}
	
	private String getClosePerformanceHost(double performance,List<String> hosts){
		performance=Math.abs(performance);
		String host=null;
		double min=Double.MAX_VALUE;
		for(String keyhost:hosts){
			Integer hostPerformance=hostPerformanceMap.get(keyhost);
			if(hostPerformance==null){
				Connection conn = WorkflowConnectionPool.getInstance().getConnection();
	    		HostManager hostmanager=new HostManager(conn);
				hostPerformance=hostmanager.getPerformance(keyhost);
			}
			if(hostPerformance>performance) continue;
			double temp=Math.abs(hostPerformance-performance);
			if(temp<min){
				min=temp;
				host=keyhost;
			}
		}
		return host;
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
	
	private void rollbackAction(Connection connection) {
		try {
			connection.rollback();
			connection.close();
			////servercomment System.out.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback and close");
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback error, message: " + e.getMessage());
		}
	}
	
	public void showTimeMap(){
		//servercomment System.out.println("timemap: "+hosttimeMap);
	}
	
	public static void main(String args[]){
		Map<String,Double> a=new HashMap<String,Double>();
		Double b=a.get("0");
		//servercomment System.out.println(b);
		
	}
}
