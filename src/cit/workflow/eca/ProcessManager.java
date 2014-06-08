/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.eca;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.Exception;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import cit.workflow.Constants;
import cit.workflow.dao.ProcessTWCInformationDAO;
import cit.workflow.elements.Event;
import cit.workflow.elements.EventDiscoveryListener;
import cit.workflow.elements.EventLog;
import cit.workflow.elements.Expression;
import cit.workflow.elements.Model;
import cit.workflow.elements.Process;
import cit.workflow.elements.ProcessStateChangeEvent;
import cit.workflow.elements.ProcessStateChangeListener;
import cit.workflow.elements.Task;
import cit.workflow.elements.User;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.elements.activities.ActivityStateChangeEvent;
import cit.workflow.elements.activities.ActivityStateChangeListener;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.factories.EventFactory;
import cit.workflow.engine.droolsruleengine.WorkflowDroolsRuleEngine;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;
import cit.workflow.view.InformationPane;
import cit.workflow.exception.WorkflowSubmitException;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

import com.ibm.icu.util.Calendar;

/**
 * @author weiwei
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProcessManager extends DBUtility implements
		ActivityStateChangeListener, EventDiscoveryListener,
		ProcessStateChangeListener {

	private static Logger logger = Logger.getLogger(ProcessManager.class);

	private ObjectFlow objectFlow;

	private EventDetector detector;

	private EventQueue eventQueue;

	private String mProcessID;
	private Date currentDate;

	private User currentUser;

	private String[][] initialVariables=null;
	
	private String processLog="";
	
	
	public String getProcessLog() {
		return processLog;
	}
	
	private TimeRecoder timeRecoder=new TimeRecoder();
	public TimeRecoder getTimeRecoder(){
		return timeRecoder;
	}

	private List handbackEventList;
	
	//private static StatefulKnowledgeSession ksession;
	private static StatefulSession session;
	/**
	 * @return Returns the handbackEventList.
	 */
	public List getHandbackEventList() {
		return handbackEventList;
	}

	/**
	 * @return Returns the currentDate.
	 */
	public Date getCurrentDate() {
		return currentDate;
	}

	/**
	 * @return Returns the currentUser.
	 */
	public User getCurrentUser() {
		return currentUser;
	}

	public void setInitialVariables(String[][] variables){
		this.initialVariables=variables;
	}
	
	
	/**
	 * --------------------------------event listener
	 * implementation-----------------------------------------------
	 */
	public void afterActivityStateChanged(ActivityStateChangeEvent event)
			throws Exception {
		/** 添加事件日志记录 */
		EventLog eventLog = new EventLog();
		eventLog.processID = event.getActivity().getProcess().getProcessID();
		eventLog.activityID = String.valueOf(event.getActivity()
				.getActivityID());
		eventLog.type = "Information";
		eventLog.category = "Activity";
		eventLog.contents = event.toString();
		eventLog.fromState = event.getFromState();
		eventLog.toState = event.getToState();
		eventLog.computer = InetAddress.getLocalHost().getHostName();
		handbackEventList.add(eventLog);

		AbstractActivity activity = event.getActivity();

		// updated by ding on 2007.5.17
		if (event.getToState().equals(Constants.ACTIVITY_STATE_READY)) {
			xmlObjectTranslate(event, 2);
		}

		/* 3.计算相关对象的值，并复位相关数据流 */
		if (event.getToState().equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			// updated by ding on 2007.5.17
			xmlObjectTranslate(event, 1);
			objectFlow.resetObjectFlow(activity);
		}
		
		//sxh May 2008
		if (event.getToState().equals(Constants.ACTIVITY_STATE_OVERTIME)) {
			//servercomment System.out.println("Event OVERTIME!");
		}
		//end of sxh May 2008

		eventQueue.push(EventFactory.create(conn, event.getActivity()
				.getProcess(), activity.getActivityID(), event.getFromState(),
				event.getToState()));
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.ActivityStateChangeListener#beforeStateChanged(cit.workflow.elements.ActivityStateChangeEvent)
	 */
	public void beforeActivityStateChanged(ActivityStateChangeEvent event)
			throws Exception {
		// updated by dingo on 2007.5.17
		/*
		 * ？？在活动A由Waiting->Ready时，要确保活动A之前的活动是否已经将运行了O->M的转换。
		 * 而在此代码中，此时的xmlObjectTranslate(event,1)完成的是活动A的M->I的转换
		 */
		if (event.getToState().equals(Constants.ACTIVITY_STATE_READY)) {
			xmlObjectTranslate(event, 1);
		}
	}  

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.ProcessStateChangeListener#afterProcessStateChanged(cit.workflow.elements.ProcessStateChangeEvent)
	 */
	public void afterProcessStateChanged(ProcessStateChangeEvent event)
			throws Exception {
		/** 添加事件日志记录 */
		EventLog eventLog = new EventLog();
		eventLog.processID = event.getProcess().getProcessID();
		eventLog.type = "Information";
		eventLog.category = "Process";
		eventLog.contents = event.toString();
		eventLog.fromState = event.getFromState();
		eventLog.toState = event.getToState();
		eventLog.computer = InetAddress.getLocalHost().getHostName();
		handbackEventList.add(eventLog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.ProcessStateChangeListener#beforeProcessStateChanged(cit.workflow.elements.ProcessStateChangeEvent)
	 */
	public void beforeProcessStateChanged(ProcessStateChangeEvent event)
			throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.EventDetectorDiscoveryListener#discoveryEvent(cit.workflow.elements.Event)
	 */
	public void discoveryEvent(Event event) {
		eventQueue.push(event);
	}

	/** --------------------------------end----------------------------------------------- */

	public ProcessManager(Connection conn, User user) {
		super(conn);

		this.objectFlow = new ObjectFlow(conn, this);

		this.eventQueue = new EventQueue();

		this.detector = new EventDetector(conn);
		
		this.detector.addEventDiscoveryListener(this);

		this.currentDate = new java.sql.Date(new java.util.Date().getTime());

		this.currentUser = user;

		this.handbackEventList = new ArrayList();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#instantiateWorkflow(int, int,
	 *      int, int, int)
	 */
	public String instantiateWorkflow(Connection conn, int workflowID,
			int source, int caseType, String parentCaseID) {

		String processID = "";
		Model model = new Model(conn, workflowID, this);
		model.setInitialVariables(initialVariables);
		long starttime=System.currentTimeMillis();
		processID = model.instantiateWorkflow(source, caseType, parentCaseID);
		if(processID==null){
			logger.info("Workflow not started");
			InformationPane.writeln("Workflow not started");
			return null;
		}

		
		
		
		/** 添加事件日志记录 */
		EventLog eventLog = new EventLog();
		eventLog.workflowID = String.valueOf(workflowID);
		eventLog.processID = processID;
		eventLog.type = "Information";
		eventLog.category = "Process";
		eventLog.contents = "Instantiate Workflow " + workflowID
				+ ", ProcessID: " + processID;
		try {
			eventLog.computer = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			eventLog.computer = "unknown";
		}
		handbackEventList.add(eventLog);
		try{
			String ruleStr=getDroolsRules(processID);
			if(ruleStr!=null){
				addDroolsRuleEngine(ruleStr);
				session.setGlobal("objectFlow", this.objectFlow);			
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		
		long endtime=System.currentTimeMillis();
		String updateSQL = "INSERT INTO processlogs(processid,starttime, endtime) VALUES(?,?,?)";
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setString(1, processID);
			updatePStat.setLong(2, starttime);
			updatePStat.setLong(3, endtime);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return processID;
	}

	private String getDroolsRules(String processID) throws Exception {
		sql = "SELECT DroolsRuleID,DroolsRuleLHS FROM ProcessDroolsRule WHERE ProcessID=?";
		params = new Object[] {processID };
		types = new int[] { Types.VARCHAR };
		List processDroolsRuleList = (List) executeQuery(new MapListHandler());
		Iterator processDroolsRuleIterator = processDroolsRuleList
				.iterator();
		String ruleStr=null;
		StringBuilder sb=null;
		while (processDroolsRuleIterator.hasNext()) {
			if(sb==null){
				sb=new StringBuilder();
				sb.append("package droolsRule\nimport cit.workflow.eca.ObjectFlow;\nimport cit.workflow.elements.Event;\nglobal ObjectFlow objectFlow\n");
			}
			Map processDroolsRuleMap = (Map) processDroolsRuleIterator.next();
			sb.append("rule rule");
			int ruleID=((Integer) processDroolsRuleMap.get("DroolsRuleID")).intValue();
			sb.append(ruleID);
			sb.append(" when ");
			sb.append(processDroolsRuleMap.get("DroolsRuleLHS"));
			sb.append("; then ");
			sb.append("objectFlow.ecaForObjectFlow('");
			sb.append(processID);
			sb.append("',");
			sb.append(ruleID);
			sb.append(");");
			sb.append(" end\n");
		}
		if(sb!=null)
			ruleStr=sb.toString();
		return ruleStr;
	}
	private void addDroolsRuleEngine(String ruleStr) throws Exception{
		final Reader source=new StringReader(ruleStr);
		final PackageBuilder builder = new PackageBuilder();
		builder.addPackageFromDrl(source);
		final Package pkg = builder.getPackage();
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(pkg);
		session = ruleBase.newStatefulSession();
		
		
		
//		PackageBuilder builder = new PackageBuilder();
//        Reader resource=new StringReader(ruleStr);
//		builder.addPackageFromDrl(resource);		
//        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//		ruleBase.addPackage(builder.getPackage());
//		session = ruleBase.newStatefulSession();
	}
//	private void addDroolsRuleEngine(String ruleStr){
//		 KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//		 kbuilder.add(ResourceFactory.newByteArrayResource(ruleStr.getBytes()), ResourceType.DRL);
//		 KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//	     kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//         ksession = kbase.newStatefulKnowledgeSession();
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#startProcess(int)
	 */
	public boolean startProcess(String processID) {
		try {
			logger.info("Start Process......");
			writemsg("Process ID: "+processID);
			InformationPane.writeln("Start Process......");
			mProcessID=processID;
			Process process = new Process(conn, processID, this);
			
			getPreviousTime(processID);
			new ProcessTWCInformationDAO(conn).addProcessDate(processID);

			// 获得开始活动的ID
			// sxh modified 2007.11
			int activityID = process.getLogicActivityID(
					Constants.ACTIVITY_START, 1);
			// sxh modified 2007.11 end
			logger.info("StartActivity ID:  " + activityID);
			InformationPane.writeln("StartActivity ID:  " + activityID);
			AbstractActivity activity = ElementFactory.createActivity(conn,
					process, activityID, this);
			// activity.addStateChangeListener(this);

			// 调用改变活动状态过程，把该活动状态变为Running
			if (!activity.changeState(Constants.ACTIVITY_STATE_WAITING,
					Constants.ACTIVITY_STATE_COMPLETED)){
				writemsg("Process Failed");
				return false;
			}

//			//servercomment System.out.println("hahahah");
			// 处理当前全局事件队列
			handleEventQueue();

			writemsg("Process Complete");
			updateLog(processID);
			clearData(processID);
			logger.info("Process Complete");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.info("Process Failed");
			writemsg(e.getMessage());
			writemsg("Process Failed");
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#openActivity()
	 */
	/*
	 * public boolean openActivity(String processID, int activityID) {
	 * 
	 * try { Process process = new Process(conn, processID, this);
	 * AbstractActivity activity = ElementFactory.createActivity(conn, process,
	 * activityID, this); //activity.addStateChangeListener(this);
	 * 
	 * if (!canOpen(activity)) return false;
	 * 
	 * //改变属于该人员的活动副本状态 changeActorActivityState(activity,
	 * Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
	 * 
	 * //调用改变活动状态过程，把该活动状态变为Running if
	 * (!activity.changeState(Constants.ACTIVITY_STATE_READY,
	 * Constants.ACTIVITY_STATE_RUNNING)) {
	 *//**
		 * we should throw some exception manual to indicate that the change
		 * state has been
		 */
	/*
	 * return true; }
	 * 
	 * //处理当前全局事件队列 handleEventQueue();
	 * 
	 * return true; } catch(Exception e) { logger.error(e.getMessage()); return
	 * false; } }
	 */
	
	
	public boolean openAgentActivity(String processID, int activityID, int agentID) {
		try {
			Process process = new Process(conn, processID, this);
			AbstractActivity activity = ElementFactory.createActivity(conn, process, activityID, this);
			if (!activity.changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING)) {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean submitAgentActivity(String processID, int activityID, int agentID) {
		try {
			Process process = new Process(conn, processID, this);
			AbstractActivity activity = ElementFactory.createActivity(conn, process, activityID, this);
			if (!activity.changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED)) {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	// sxh modified 2007.10
	public int openActivity(String processID, int activityID) {
		// 1、调用GetTaskList，判断该任务是否分配给他了
		int personID = currentUser.getActorID();
		List taskList = getTaskList();
		Task task = null;

		Iterator taskIterator = taskList.iterator();
		boolean isAllocated = false;
		while (taskIterator.hasNext()) {
			task = (Task) taskIterator.next();
			if (task.getProcessID().equals(processID)
					&& task.getActivityID() == activityID
					&& task.getAllocateState().equals(
							Constants.TASK_STATE_ACCEPTED)) {
				// 如果是，进入2
				isAllocated = true;
				break;
			}
		}

		// 如果不是，返回-1：任务未分配给他
		if (isAllocated == false) {
			return -1;
		}

		// 2.判断活动的状态是否为Running
		if (task.getState().equals(Constants.ACTIVITY_STATE_RUNNING)) {
			// 如果是，返回0：打开成功
			return 0;
		}// 如果不是，进入3

		int roleID = task.getRoleID();

		// 3.读ProcessActivityRole表，判断是否对于所有的Role,AllocatedNumber>=MinimalNumber
		sql = "select * from processActivityRole where processID = ? and activityID = ? and AllocatedNumber < MinimalNumber";
		params = new Object[] { processID, new Integer(activityID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List processActivityRoleList = null;
		try {
			processActivityRoleList = (List) executeQuery(new MapListHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		if (processActivityRoleList.size() == 0) {
			// 如果成立

			// 将活动状态置为Running（注意修改活动状态，将引发一个事件处理的递归流程，而不是仅仅简单的修改某一字段）
			try {
				Process process = new Process(conn, processID, this);
				AbstractActivity activity = ElementFactory.createActivity(conn,
						process, activityID, this);
				if (!activity.changeState(Constants.ACTIVITY_STATE_READY,
						Constants.ACTIVITY_STATE_RUNNING)) {
					/**
					 * we should throw some exception manual to indicate that
					 * the change state has been
					 */
					return -1;
				}

				handleEventQueue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// 将Activity相关表中的起始时间设为当前日期
			try {
				java.sql.Date currentTime = new java.sql.Date(System
						.currentTimeMillis());
				// processActivityInvokingApplication
				sql = "update processActivityInvokingApplication set actualStartDate = ? where processID = ? and activityID = ?";
				params = new Object[] { currentTime, processID,
						new Integer(activityID) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
				executeUpdate();
				// processActivityInvokingProcess
				sql = "update processActivityInvokingProcess set actualStartDate = ? where processID = ? and activityID = ?";
				params = new Object[] { currentTime, processID,
						new Integer(activityID) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
				executeUpdate();
				// processActivityResource
				sql = "update processActivityResource set actualStartDate = ? where processID = ? and activityID = ?";
				params = new Object[] { currentTime, processID,
						new Integer(activityID) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
				executeUpdate();
				// processTWCInformation
				sql = "update processTWCInformation PTI left join ProcessActivityInformation PAI on PTI.processID = PAI.processID and PTI.twcid = PAI.twcid set actualStartDate = ? where PAI.processID = ? and PAI.activityID = ?";
				params = new Object[] { currentTime, processID,
						new Integer(activityID) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
				executeUpdate();

				// 将ProcessActivityPerson表中所有ActivityID为该活动的记录的ActualStartDate填入当前日期
				sql = "update ProcessActivityPerson set actualStartDate = ? where processID = ? and activityID = ?";
				params = new Object[] { currentTime, processID,
						new Integer(activityID) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
				executeUpdate();

				// 返回0：打开成功
				return 0;

			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		// 如果不成立：返回-2：人数不够
		return -2;

	}

	// sxh modified 2007.10 end

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#submitActivity()
	 */
	/*
	 * public boolean submitActivity(String processID, int activityID) {
	 * 
	 * try { Process process = new Process(conn, processID, this);
	 * AbstractActivity activity = ElementFactory.createActivity(conn, process,
	 * activityID, this);
	 */
	// activity.addStateChangeListener(this);*/
	/*
	 * //dingo add on 2007.7.29 sql = "SELECT * FROM ProcessActivityInformation
	 * WHERE ProcessID=? AND ActivityID=?"; params = new Object[] {processID,new
	 * Integer(activityID)}; types = new int[] {Types.VARCHAR,Types.INTEGER};
	 * List wfoList = (List) executeQuery(new MapListHandler()); Iterator
	 * wfoIterator = wfoList.iterator(); int activityImplementation=-1;
	 * if(wfoIterator.hasNext()) { Map wfoMap = (Map) wfoIterator.next();
	 * activityImplementation = ((Integer)
	 * wfoMap.get("ActivityImplementation")).intValue(); } //dingo end
	 */
	// if(activityImplementation==1){//dingo add 2007.7.29 // ==1表示人的活动
	// 改变属于该人员的活动副本状态
	// //servercomment System.out.println("调用改变属于该人员的活动副本状态模块");
	/*
	 * changeActorActivityState(activity, Constants.ACTIVITY_STATE_RUNNING,
	 * Constants.ACTIVITY_STATE_COMPLETED); // } //
	 * 调用改变活动状态过程，把该活动状态变为COMPLETED//dingo if
	 * (!activity.changeState(Constants.ACTIVITY_STATE_RUNNING,
	 * Constants.ACTIVITY_STATE_COMPLETED)) return true; // return false; //
	 * 处理当前全局事件队列 handleEventQueue();
	 * 
	 * return true; } catch (Exception e) { logger.error(e.getMessage()); return
	 * false; } }
	 */

	// sxh modified 2007
	public int submitActivity(String processID, int activityID) {
		// 1.调用GetTaskList，判断是否该任务已经分配给他、并且状态为Running；
		List taskList = getTaskList();
		if (taskList == null) {
			return -1;
		}
		Iterator taskIterator = taskList.iterator();
		boolean isAllocated = false;
		int roleID = 0;
		while (taskIterator.hasNext()) {
			Task task = (Task) taskIterator.next();
			if (task.getProcessID().equals(processID)
					&& task.getActivityID() == activityID
					&& task.getAllocateState().equals(
							Constants.TASK_STATE_ACCEPTED)
					&& task.getState().equals(Constants.ACTIVITY_STATE_RUNNING)) {
				isAllocated = true;
				roleID = task.getRoleID();
				break;
			}
		}
		// 如果不是，返回-1：任务无法提交
		if (isAllocated == false) {
			return -1;
		}

		// 2.将ProcessActivityPerson表中的State改为”Submitted”，这意味着下一次活动列表中将不再显示它,将ProcessActivityPerson表中该记录的ActualFinishDate填入当前日期
		int personID = currentUser.getActorID();
		java.sql.Date currentDate = new java.sql.Date(System
				.currentTimeMillis());
		sql = "update processActivityPerson set RepeatedTime = (RepeatedTime + 1), actualEndDate = ?, state = ? where processID = ? and activityID = ? and roleID = ? and personID = ?";
		params = new Object[] { currentDate, Constants.TASK_STATE_SUBMITTED,
				processID, new Integer(activityID), new Integer(roleID),
				new Integer(personID) };
		types = new int[] { Types.DATE, Types.VARCHAR, Types.VARCHAR,
				Types.INTEGER, Types.INTEGER, Types.INTEGER };

		try {
			executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		// 3.针对该活动，判断是否所有的Role，已经提交的人数是否大于等于MinimalSubmittedPerson
		try {
			sql = "select * from ProcessActivityRole where processid = ? and activityid = ? and AllocatedNumber < MinimalNumber";
			params = new Object[] { processID, new Integer(activityID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			List processActivityRoleList = (List) executeQuery(new MapListHandler());

			if (processActivityRoleList.size() == 0) {
				// 如果是，活动状态改为Completed（注意修改活动状态，将引发一个事件处理的递归流程，而不是仅仅简单的修改某一字段）；
				Process process = new Process(conn, processID, this);
				AbstractActivity activity = ElementFactory.createActivity(conn,
						process, activityID, this);
				// 调用改变活动状态过程，把该活动状态变为COMPLETED//dingo
				if (!activity.changeState(Constants.ACTIVITY_STATE_RUNNING,
						Constants.ACTIVITY_STATE_COMPLETED)) {
				}
				// 处理当前全局事件队列
				handleEventQueue();
			}

			// 将Activity相关表中的实际结束时间设为当前日期
			java.sql.Date currentTime = new java.sql.Date(System
					.currentTimeMillis());
			// processActivityInvokingApplication
			sql = "update processActivityInvokingApplication set actualEndDate = ? where processID = ? and activityID = ?";
			params = new Object[] { currentTime, processID,
					new Integer(activityID) };
			types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
			// processActivityInvokingProcess
			sql = "update processActivityInvokingProcess set actualEndDate = ? where processID = ? and activityID = ?";
			params = new Object[] { currentTime, processID,
					new Integer(activityID) };
			types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
			// processActivityResource
			sql = "update processActivityResource set actualEndDate = ? where processID = ? and activityID = ?";
			params = new Object[] { currentTime, processID,
					new Integer(activityID) };
			types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
			// processTWCInformation
			sql = "update processTWCInformation PTI left join ProcessActivityInformation PAI on PTI.processID = PAI.processID and PTI.twcid = PAI.twcid set actualEndDate = ? where PAI.processID = ? and PAI.activityID = ?";
			params = new Object[] { currentTime, processID,
					new Integer(activityID) };
			types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
			executeUpdate();

			// 将ProcessActivityPerson表中所有ActivityID为该活动的记录的ActualStartDate填入当前日期
			sql = "update ProcessActivityPerson set actualEndDate = ? where processID = ? and activityID = ?";
			params = new Object[] { currentTime, processID,
					new Integer(activityID) };
			types = new int[] { Types.DATE, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 返回0：提交成功
		return 0;

	}

	// sxh modified 2007 end

	// dingo add 2007
	public boolean submitAutoActivity(String processID, int activityID) {

		try {
			Process process = new Process(conn, processID, this);
			AbstractActivity activity = ElementFactory.createActivity(conn,
					process, activityID, this);
			// activity.addStateChangeListener(this);

			// 调用改变活动状态过程，把该活动状态变为COMPLETED//dingo
			if (!activity.changeState(Constants.ACTIVITY_STATE_RUNNING,
					Constants.ACTIVITY_STATE_COMPLETED)) {
				return true;

			}
			
			//servercomment System.out.println("ProcessID: "+processID+" ,ActivityID: "+activityID+"   has been submited!");

			// 处理当前全局事件队列
			handleEventQueue();

			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	// dingo add 2007 end

	// public void text()throws Exception{
	// try {
	// int i =0;
	// }catch(Exception e){
	// //servercomment System.out.println("failed");
	// throw e;
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.engine.WorkflowService#getTaskList()
	 */
	public List getTaskList() {
		TaskListHandler taskList = new TaskListHandler(conn, this);
		return taskList.getTaskList();
	}
	
	public List getHistoryTaskList() {
		TaskListHandler taskList = new TaskListHandler(conn, this);
		return taskList.getHistoryTaskList();
	}

	// sxh add 2007.10
	public boolean acceptTask(String processID, int activityID) {
		TaskListHandler taskList = new TaskListHandler(conn, this);
		return taskList.acceptTask(processID, activityID);
	}

	public int bindingActivitytoPerson(int personID, String processID,
			int activityID) {
		// 1.以ProcessID，ActivityID读ProcessActivityInformation表

		// 如果Activity不是原子活动，返回-1（不是原子活动），结束
		sql = "select * from processActivityInformation where processID = ? and parentID = ?";
		params = new Object[] { processID, activityID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List processActivityInformationList = null;
		try {
			processActivityInformationList = (List) executeQuery(new MapListHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		if (processActivityInformationList.size() > 0) {
			return -1;
		}
		// 如果Activity的状态是“Completed”，返回-2（活动已结束），结束
		sql = "select * from processActivityInformation where processID = ? and activityID = ?";
		params = new Object[] { processID, activityID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processActivityInformationMap = null;
		try {
			processActivityInformationMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		String state = "";
		if (processActivityInformationMap.get("State") != null) {
			state = (String) processActivityInformationMap.get("State");
		}
		// 如果Activity的状态是“Completed”，返回-2（活动已结束），结束
		if (state.equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			return -2;
		}
		// 如果Activity的状态是“Running”，返回-3（活动已分配），结束
		// 为了转授权功能，暂时注释掉
		/*if (state.equals(Constants.ACTIVITY_STATE_RUNNING)) {
			return -3;
		}*/
		// 2.读ProcessActivityPerson表,以ProcessID，PersonID，ActivityID查找，如果查找到State为"Accepted"的该任务,说明该任务已经为该人员所接受，返回0（活动已分配给他），结束
		sql = "select * from processActivityPerson where processID = ? and activityID = ? and personID = ? and state = ?";
		params = new Object[] { processID, activityID, personID,
				Constants.TASK_STATE_ACCEPTED };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,
				Types.VARCHAR };
		List processActivityPersonList = null;
		try {
			processActivityPersonList = (List) executeQuery(new MapListHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		if (processActivityPersonList.size() > 0) {
			return 0;
		}

		// 3.读出该人员的RoleID
		try {
			sql = "SELECT roleID FROM personrole where personid=?";
			params = new Object[] { new Integer(personID) };
			types = new int[] { Types.INTEGER };
			List roleList = (List) executeQuery(new MapListHandler());
			Iterator roleIterator = roleList.iterator();
			while (roleIterator.hasNext()) {

				// 3.1.选择下一RoleID
				Map roleMap = (Map) roleIterator.next();
				int roleID = (Integer) roleMap.get("roleID");
				// 3.2以ProcessID，ActivityID，RoleID去查ProcessActivityRole
				sql = "SELECT * from processActivityRole where processID = ? and activityID = ? and roleID = ?";
				params = new Object[] { processID, new Integer(activityID),
						new Integer(roleID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
				Map processActivityRoleMap = (Map) executeQuery(new MapHandler());

				// 3.2.1.如果没有记录，返回3.1，否则继续
				if (processActivityRoleMap == null) {
					continue;
				}

				// 3.2.2.比较该任务针对该RoleID的MaximalNumber和AllocatedNumber:
				// 如果AllocatedNumber<MaximalNumber，进入3.2.3；
				// 如果AllocatedNumber=MaximalNumber，返回3.1；
				int allocatedNumber = (Integer) processActivityRoleMap
						.get("AllocatedNumber");
				int maximalNumber = (Integer) processActivityRoleMap
						.get("MaximalNumber");
				if (allocatedNumber == maximalNumber) {
					continue;
				}

				// 3.2.3判断针对该Process是否有ProcessPerson的定义
				sql = "SELECT * from ProcessPerson where processid=?";
				params = new Object[] { processID };
				types = new int[] { Types.VARCHAR };
				List processPersonList = (List) executeQuery(new MapListHandler());

				// 如果有,判断是否定义了该人员可以以该角色参与该过程
				if (processPersonList.size() > 0) {
					sql = "SELECT * from ProcessPerson where processid=? and personid=? and roleid=?";
					params = new Object[] { processID, new Integer(personID),
							new Integer(roleID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER };
					Map processPersonMap = (Map) executeQuery(new MapHandler());
					// 如果没有,则返回3.1
					if (processPersonMap == null) {
						continue;
					}// 如果有,进入3.2.4
				}// 如果没有,进入3.2.4

				// 3.2.4判断是否针对其父活动有ProcessActivityTeamMember的定义(注意这是要逐步向上查的,直到查到顶层)
				sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
				params = new Object[] { processID, new Integer(activityID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				processActivityInformationMap = (Map) executeQuery(new MapHandler());
				int parentID = -1;

				if (processActivityInformationMap != null
						&& processActivityInformationMap.get("parentID") != null) {
					parentID = (Integer) processActivityInformationMap
							.get("parentID");
				}

				boolean isTeamFound = false;
				while (parentID != -1) {
					sql = "select * from processActivityTeamMember where processID = ? and activityID = ?";
					params = new Object[] { processID, new Integer(activityID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

					if (processActivityTeamMemberList.size() > 0) {
						isTeamFound = true;
						break;

					} else {
						sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						processActivityInformationMap = (Map) executeQuery(new MapHandler());
						if (processActivityInformationMap.get("parentID") != null) {
							parentID = (Integer) processActivityInformationMap
									.get("parentID");
						} else {
							parentID = -1;
						}
						continue;
					}
				}

				if (isTeamFound == true) {
					// 如果有,判断是否定义了该人员可以以该角色参与活动:
					sql = "select * from processActivityTeamMember where processID = ? and activityID = ? and personID = ? and roleID = ?";
					params = new Object[] { processID, new Integer(activityID),
							new Integer(personID), new Integer(roleID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER, Types.INTEGER };
					List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

					if (processActivityTeamMemberList.size() == 0) {
						// 如果没有,返回3.1
						continue;
					} else {
						// 如果有,进入3.2.5
					}
				}

				// 3.2.5.在ProcessActivityPerson表中加入记录,其中State为Accepted,其它字段的信息来自于ActivityInformation(时间信息，RepeatedTime),
				// DefinedWorkload来自活动对应的TWCInformation的DefinedWorkload*(ProcessActivityRole.WorkloadRatio/AllocatedNumber)(注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；

				int repeatedTime = 0;
				if (processActivityInformationMap.get("repeatedTime") != null) {
					repeatedTime = (Integer) processActivityInformationMap
							.get("repeatedTime");
				}

				float workloadRatio = 0;
				if (processActivityRoleMap != null
						&& processActivityRoleMap.get("workloadRatio") != null) {
					workloadRatio = (Float) processActivityRoleMap
							.get("workloadRatio");
				}

				// 从ProcessTWCInformation表提取信息
				sql = "select PTI.* from processTWCInformation PTI left join processActivityInformation PAI on PTI.twcid = PAI.twcid where PAI.processID = ? and PAI.activityID = ?";
				params = new Object[] { processID, new Integer(activityID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				Map processTWCInformationMap = (Map) executeQuery(new MapHandler());
				Timestamp definedStartDate = null;
				Timestamp actualStartDate = null;
				Timestamp definedEndDate = null;
				Timestamp actualEndDate = null;
				int definedWorkload = 0;
				int actualWorkload = 0;

				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedStartDate") != null) {
					definedStartDate = (Timestamp) processTWCInformationMap
							.get("definedStartDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualStartDate") != null) {
					actualStartDate = (Timestamp) processTWCInformationMap
							.get("actualStartDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedEndDate") != null) {
					definedEndDate = (Timestamp) processTWCInformationMap
							.get("definedEndDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualEndDate") != null) {
					actualEndDate = (Timestamp) processTWCInformationMap
							.get("actualEndDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedWorkload") != null) {
					definedWorkload = (Integer) processTWCInformationMap
							.get("definedWorkload");
				}

				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualWorkload") != null) {
					actualWorkload = (Integer) processTWCInformationMap
							.get("actualWorkload");
				}

				sql = "insert processActivityPerson set processID = ?, activityID = ?, roleID = ?, personID = ?, repeatedTime = ?, DefinedStartDate = ?, ActualStartDate = ?, DefinedEndDate = ?, ActualEndDate = ?, DefinedWorkload = ?, ActualWorkload = ?, State = ?";
				params = new Object[] {
						processID,
						new Integer(activityID),
						new Integer(roleID),
						new Integer(personID),
						new Integer(repeatedTime),
						definedStartDate,
						actualStartDate,
						definedEndDate,
						actualEndDate,
						new Integer(
								(int) (definedWorkload * (workloadRatio / allocatedNumber))),
						actualWorkload, Constants.TASK_STATE_ACCEPTED };
				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE, Types.DATE, Types.DATE,
						Types.INTEGER, Types.INTEGER, Types.VARCHAR };
				executeUpdate();

				// 将ProcessActivityRole中的AllocatedNumber加1，返回1：成功
				sql = "update processActivityRole set allocatedNumber = (allocatedNumber + 1) where processid = ? and activityID = ? and roleID = ?";
				params = new Object[] { processID, new Integer(activityID),
						new Integer(roleID) };
				executeUpdate();
				// (注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；
				sql = "update processActivityPerson set definedWorkload = ? where processID = ? and activityID = ? and roleID = ?";
				params = new Object[] {
						new Integer((int) (definedWorkload * (workloadRatio
								/ allocatedNumber + 1))), processID,
						new Integer(activityID), new Integer(roleID) };
				types = new int[] { Types.INTEGER, Types.VARCHAR,
						Types.INTEGER, Types.INTEGER };
				executeUpdate();

				return 1;
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return -4;
	}
	
	/**
	 * 对活动解除人员绑定
	 * @param personID 人员ID
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @return
	 */
	public int unbindingActivitytoPerson(int personID, String processID,
			int activityID) {
		// 1.以ProcessID，ActivityID读ProcessActivityInformation表

		// 如果Activity不是原子活动，返回-1（不是原子活动），结束
		sql = "select * from processActivityInformation where processID = ? and parentID = ?";
		params = new Object[] { processID, activityID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List processActivityInformationList = null;
		try {
			processActivityInformationList = (List) executeQuery(new MapListHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		if (processActivityInformationList.size() > 0) {
			return -1;
		}
		// 如果Activity的状态是“Completed”，返回-2（活动已结束），结束
		sql = "select * from processActivityInformation where processID = ? and activityID = ?";
		params = new Object[] { processID, activityID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processActivityInformationMap = null;
		try {
			processActivityInformationMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		String state = "";
		if (processActivityInformationMap.get("State") != null) {
			state = (String) processActivityInformationMap.get("State");
		}
		// 如果Activity的状态是“Completed”，返回-2（活动已结束），结束
		if (state.equals(Constants.ACTIVITY_STATE_COMPLETED)) {
			return -2;
		}
		// 如果Activity的状态是“Running”，返回-3（活动已分配），结束
		/*if (state.equals(Constants.ACTIVITY_STATE_RUNNING)) {
			return -3;
		}*/
		// 2.读ProcessActivityPerson表,以ProcessID，PersonID，ActivityID查找，如果查找到State为"Accepted"的该任务,说明该任务已经为该人员所接受，返回0（活动已分配给他），结束
		sql = "select * from processActivityPerson where processID = ? and activityID = ? and personID = ? and state = ?";
		params = new Object[] { processID, activityID, personID,
				Constants.TASK_STATE_ACCEPTED };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,
				Types.VARCHAR };
		List processActivityPersonList = null;
		try {
			processActivityPersonList = (List) executeQuery(new MapListHandler());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		if (processActivityPersonList.size() > 0) {
			return 0;
		}

		// 3.读出该人员的RoleID
		try {
			sql = "SELECT roleID FROM personrole where personid=?";
			params = new Object[] { new Integer(personID) };
			types = new int[] { Types.INTEGER };
			List roleList = (List) executeQuery(new MapListHandler());
			Iterator roleIterator = roleList.iterator();
			while (roleIterator.hasNext()) {

				// 3.1.选择下一RoleID
				Map roleMap = (Map) roleIterator.next();
				int roleID = (Integer) roleMap.get("roleID");
				// 3.2以ProcessID，ActivityID，RoleID去查ProcessActivityRole
				sql = "SELECT * from processActivityRole where processID = ? and activityID = ? and roleID = ?";
				params = new Object[] { processID, new Integer(activityID),
						new Integer(roleID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
				Map processActivityRoleMap = (Map) executeQuery(new MapHandler());

				// 3.2.1.如果没有记录，返回3.1，否则继续
				if (processActivityRoleMap == null) {
					continue;
				}

				// 3.2.2.比较该任务针对该RoleID的MaximalNumber和AllocatedNumber:
				// 如果AllocatedNumber<MaximalNumber，进入3.2.3；
				// 如果AllocatedNumber=MaximalNumber，返回3.1；
				int allocatedNumber = (Integer) processActivityRoleMap
						.get("AllocatedNumber");
				int maximalNumber = (Integer) processActivityRoleMap
						.get("MaximalNumber");
				if (allocatedNumber == maximalNumber) {
					continue;
				}

				// 3.2.3判断针对该Process是否有ProcessPerson的定义
				sql = "SELECT * from ProcessPerson where processid=?";
				params = new Object[] { processID };
				types = new int[] { Types.VARCHAR };
				List processPersonList = (List) executeQuery(new MapListHandler());

				// 如果有,判断是否定义了该人员可以以该角色参与该过程
				if (processPersonList.size() > 0) {
					sql = "SELECT * from ProcessPerson where processid=? and personid=? and roleid=?";
					params = new Object[] { processID, new Integer(personID),
							new Integer(roleID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER };
					Map processPersonMap = (Map) executeQuery(new MapHandler());
					// 如果没有,则返回3.1
					if (processPersonMap == null) {
						continue;
					}// 如果有,进入3.2.4
				}// 如果没有,进入3.2.4

				// 3.2.4判断是否针对其父活动有ProcessActivityTeamMember的定义(注意这是要逐步向上查的,直到查到顶层)
				sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
				params = new Object[] { processID, new Integer(activityID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				processActivityInformationMap = (Map) executeQuery(new MapHandler());
				int parentID = -1;

				if (processActivityInformationMap != null
						&& processActivityInformationMap.get("parentID") != null) {
					parentID = (Integer) processActivityInformationMap
							.get("parentID");
				}

				boolean isTeamFound = false;
				while (parentID != -1) {
					sql = "select * from processActivityTeamMember where processID = ? and activityID = ?";
					params = new Object[] { processID, new Integer(activityID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

					if (processActivityTeamMemberList.size() > 0) {
						isTeamFound = true;
						break;

					} else {
						sql = "select parentID from processActivityInformation where processID = ? and activityID = ?";
						params = new Object[] { processID,
								new Integer(activityID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						processActivityInformationMap = (Map) executeQuery(new MapHandler());
						if (processActivityInformationMap.get("parentID") != null) {
							parentID = (Integer) processActivityInformationMap
									.get("parentID");
						} else {
							parentID = -1;
						}
						continue;
					}
				}

				if (isTeamFound == true) {
					// 如果有,判断是否定义了该人员可以以该角色参与活动:
					sql = "select * from processActivityTeamMember where processID = ? and activityID = ? and personID = ? and roleID = ?";
					params = new Object[] { processID, new Integer(activityID),
							new Integer(personID), new Integer(roleID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER,
							Types.INTEGER, Types.INTEGER };
					List processActivityTeamMemberList = (List) executeQuery(new MapListHandler());

					if (processActivityTeamMemberList.size() == 0) {
						// 如果没有,返回3.1
						continue;
					} else {
						// 如果有,进入3.2.5
					}
				}

				// 3.2.5.在ProcessActivityPerson表中加入记录,其中State为Accepted,其它字段的信息来自于ActivityInformation(时间信息，RepeatedTime),
				// DefinedWorkload来自活动对应的TWCInformation的DefinedWorkload*(ProcessActivityRole.WorkloadRatio/AllocatedNumber)(注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；

				int repeatedTime = 0;
				if (processActivityInformationMap.get("repeatedTime") != null) {
					repeatedTime = (Integer) processActivityInformationMap
							.get("repeatedTime");
				}

				float workloadRatio = 0;
				if (processActivityRoleMap != null
						&& processActivityRoleMap.get("workloadRatio") != null) {
					workloadRatio = (Float) processActivityRoleMap
							.get("workloadRatio");
				}

				// 从ProcessTWCInformation表提取信息
				sql = "select PTI.* from processTWCInformation PTI left join processActivityInformation PAI on PTI.twcid = PAI.twcid where PAI.processID = ? and PAI.activityID = ?";
				params = new Object[] { processID, new Integer(activityID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				Map processTWCInformationMap = (Map) executeQuery(new MapHandler());
				Timestamp definedStartDate = null;
				Timestamp actualStartDate = null;
				Timestamp definedEndDate = null;
				Timestamp actualEndDate = null;
				int definedWorkload = 0;
				int actualWorkload = 0;

				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedStartDate") != null) {
					definedStartDate = (Timestamp) processTWCInformationMap
							.get("definedStartDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualStartDate") != null) {
					actualStartDate = (Timestamp) processTWCInformationMap
							.get("actualStartDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedEndDate") != null) {
					definedEndDate = (Timestamp) processTWCInformationMap
							.get("definedEndDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualEndDate") != null) {
					actualEndDate = (Timestamp) processTWCInformationMap
							.get("actualEndDate");
				}
				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("definedWorkload") != null) {
					definedWorkload = (Integer) processTWCInformationMap
							.get("definedWorkload");
				}

				if (processTWCInformationMap != null
						&& processTWCInformationMap.get("actualWorkload") != null) {
					actualWorkload = (Integer) processTWCInformationMap
							.get("actualWorkload");
				}

				sql = "insert processActivityPerson set processID = ?, activityID = ?, roleID = ?, personID = ?, repeatedTime = ?, DefinedStartDate = ?, ActualStartDate = ?, DefinedEndDate = ?, ActualEndDate = ?, DefinedWorkload = ?, ActualWorkload = ?, State = ?";
				params = new Object[] {
						processID,
						new Integer(activityID),
						new Integer(roleID),
						new Integer(personID),
						new Integer(repeatedTime),
						definedStartDate,
						actualStartDate,
						definedEndDate,
						actualEndDate,
						new Integer(
								(int) (definedWorkload * (workloadRatio / allocatedNumber))),
						actualWorkload, Constants.TASK_STATE_ACCEPTED };
				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE, Types.DATE, Types.DATE,
						Types.INTEGER, Types.INTEGER, Types.VARCHAR };
				executeUpdate();

				// 将ProcessActivityRole中的AllocatedNumber加1，返回1：成功
				sql = "update processActivityRole set allocatedNumber = (allocatedNumber + 1) where processid = ? and activityID = ? and roleID = ?";
				params = new Object[] { processID, new Integer(activityID),
						new Integer(roleID) };
				executeUpdate();
				// (注意,每当该角色多分配一个人,那么,原来该任务的所有作为该角色的多位承担者所定义的工作负荷都需要更新)；
				sql = "update processActivityPerson set definedWorkload = ? where processID = ? and activityID = ? and roleID = ?";
				params = new Object[] {
						new Integer((int) (definedWorkload * (workloadRatio
								/ allocatedNumber + 1))), processID,
						new Integer(activityID), new Integer(roleID) };
				types = new int[] { Types.INTEGER, Types.VARCHAR,
						Types.INTEGER, Types.INTEGER };
				executeUpdate();

				return 1;
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return -4;
	}
	
	public void unbindingActivitytoRole(int roleID, String processID, int activityID) {
		sql = "DELETE FROM processactivityperson WHERE processID = ? AND activityID = ? AND roleID = ?";
		params = new Object[] { processID, new Integer(activityID), new Integer(roleID) };
		types = new int[] { Types.VARCHAR, Types.VARCHAR,
				Types.INTEGER, Types.INTEGER };
		try {
			executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// sxh add 2007.10 end

	private void handleEventQueue() throws Exception {
		while (!eventQueue.IsEmpty()) {
			Event event = eventQueue.pop();
			processEvent(event);
		}
	}

	/**
	 * 功能：处理事件
	 * 
	 * @param event
	 * @throws WorkflowTransactionException
	 * 
	 * 步骤：
	 * 1.判断事件是否重复发生，如果是就把事件重复信息写入表ProcessRepeatedInformation，并修改ExpressionforParse表达式
	 * 2.修改事件相关信息 3.改变相关数据对象的状态 4.根据ECA规则来处理该事件 5.使用事件探测器来探测该事件是否会引发新的事件
	 */
	private void processEvent(Event event) throws Exception {

		sql = "SELECT * FROM ProcessEvent WHERE ProcessID=? AND EventID=?";
		params = new Object[] { event.getProcess().getProcessID(),
				new Integer(event.getEventID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processEventMap = (Map) executeQuery(new MapHandler());
		if (processEventMap != null) {

			if (logger.isInfoEnabled())
				logger.info("PROCESS EVENT----" + event);
			writemsg("PROCESS EVENT----" + event);
			InformationPane.writeln("PROCESS EVENT----" + event);
			
			int repeatedTime = ((Integer) processEventMap.get("RepeatedTime"))
					.intValue();

			// 判断是否重复发生
			if (repeatedTime > 0) {
				// 将重复事件信息写入ProcessRepeatedInformation表
				// RepeatedType---5代表Event
				sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
				params = new Object[] { event.getProcess().getProcessID(),
						new Integer(5), new Integer(event.getEventID()), null,
						processEventMap.get("RepeatedTime"),
						processEventMap.get("ActualStartDate"),
						processEventMap.get("ActualEndDate") };

				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE };
				executeUpdate();

				// if repeated, the expression for parse should be copied from
				// EventRepresentation field
				sql = "UPDATE ProcessEvent SET ExpressionforParse=?, ActiveTime=?, RepeatedTime=? WHERE ProcessID=? AND EventID=?";
				params = new Object[] {
						processEventMap.get("EventRepresentation"),
						currentDate, new Integer(repeatedTime + 1),
						event.getProcess().getProcessID(),
						new Integer(event.getEventID()) };
				types = new int[] { Types.VARCHAR, Types.DATE, Types.INTEGER,
						Types.VARCHAR, Types.INTEGER };
			} else {
				sql = "UPDATE ProcessEvent SET ActiveTime=?, RepeatedTime=? WHERE ProcessID=? AND EventID=?";
				params = new Object[] { currentDate,
						new Integer(repeatedTime + 1),
						event.getProcess().getProcessID(),
						new Integer(event.getEventID()) };
				types = new int[] { Types.DATE, Types.INTEGER, Types.VARCHAR,
						Types.INTEGER };
			}
			// 更新ProcessEvent相关信息
			executeUpdate();

			// sxh add 2007.11
			// 调用规则引擎
			/*String processID = event.getProcess().getProcessID();
			int activityID = (Integer) processEventMap.get("activityID");
			sql = "select * from processExtendedRules where processid = ? and eventid = ?";
			params = new Object[] { processID, event.getEventID() };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			Map processExtendedRulesMap = (Map) executeQuery(new MapHandler());
			if (processExtendedRulesMap != null) {
				WorkflowDroolsRuleEngine we = new WorkflowDroolsRuleEngine(processID, event.getEventID());
				we.RunEngine(conn);
			}*/
			// sxh add 2007.11 end

			// 改变相关数据对象的状态
//			objectFlow.ecaForObjectFlow(event);using drools instead of event driven
			
			session.insert(event);
			
			
			/**
			 *
			 * TO-DO
			 * original line 1459
			session.fireAllRules();
			
			
			*/

			// 处理该事件相关的流程ECA规则
			processECARule(event);

			// 处理该事件相关的活动ECA规则
			processActivityECARule(event);

			// 使用事件探测器来探测该事件是否会引发新的事件
			detector.parseEvent(event);
		}
	}

	private void processECARule(Event event) throws Exception {

		sql = "SELECT * FROM ProcessProcessECARule WHERE ProcessID=? AND EventID=?";
		params = new Object[] { event.getProcess().getProcessID(),
				new Integer(event.getEventID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List processProcessECARuleList = (List) executeQuery(new MapListHandler());

		boolean conditionCalculated = false;
		Iterator processProcessECARuleIterator = processProcessECARuleList
				.iterator();
		while (processProcessECARuleIterator.hasNext()) {

			if (logger.isInfoEnabled())
				logger.info("PROCESS EVENT ECA----" + event);
			
			InformationPane.writeln("PROCESS EVENT ECA----" + event);

			Map processProcessECARuleMap = (Map) processProcessECARuleIterator
					.next();

			int repeatedTime = ((Integer) processProcessECARuleMap
					.get("RepeatedTime")).intValue();

			if (repeatedTime > 0) {

				// 将重复执行的ECARule信息写入ProcessRepeatedInformation表
				// RepeatedType---6代表ProcessECARule
				sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
				params = new Object[] { 
						event.getProcess().getProcessID(),
						new Integer(6),
						processProcessECARuleMap.get("RuleID"),
						processProcessECARuleMap.get("ConditionID"),
						processProcessECARuleMap.get("RepeatedTime"),
						processProcessECARuleMap.get("ActiveTime"),
						processProcessECARuleMap.get("ActiveTime") };

				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE };
				executeUpdate();
			}

			// 记录重复次数
			sql = "UPDATE ProcessProcessECARule SET ActiveTime=?, RepeatedTime=? WHERE ProcessID=? AND EventID=?";
			params = new Object[] { currentDate, new Integer(repeatedTime + 1),
					event.getProcess().getProcessID(),
					new Integer(event.getEventID()) };
			types = new int[] { Types.DATE, Types.INTEGER, Types.VARCHAR,
					Types.INTEGER };
			executeUpdate();

			// ECA规则，是否有条件部分
			int conditionID = ((Integer) processProcessECARuleMap
					.get("ConditionID")).intValue();
			if (conditionID != 0) {

				if (logger.isInfoEnabled())
					logger.info("condition: "
							+ conditionID
							+ " , action expression: "
							+ (String) processProcessECARuleMap
									.get("ActionExpression") + ".");

				// 有条件部分，计算条件部分的值，如果为真，则执行对应的动作
				Expression condition = new Expression(conn, event.getProcess()
						.getProcessID(), conditionID);
				if (condition.analyzeBoolExpression()) {
					actionExpression(event, (String) processProcessECARuleMap
							.get("ActionExpression"));
				}
			} else {

				if (logger.isInfoEnabled())
					logger.info("condition: no, action expression: "
							+ (String) processProcessECARuleMap
									.get("ActionExpression") + ".");

				// 无条件部分，执行对应的动作
				actionExpression(event, (String) processProcessECARuleMap
						.get("ActionExpression"));
			}
		}
	}

	private void processActivityECARule(Event event) throws Exception {

		sql = "SELECT * FROM ProcessActivityECARule WHERE ProcessID=? AND EventID=?";
		params = new Object[] { event.getProcess().getProcessID(),
				new Integer(event.getEventID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List processActivityECARuleList = (List) executeQuery(new MapListHandler());

		boolean conditionCalculated = false;
		Iterator processActivityECARuleIterator = processActivityECARuleList
				.iterator();
		while (processActivityECARuleIterator.hasNext()) {
			Map processActivityECARuleMap = (Map) processActivityECARuleIterator
					.next();

			int repeatedTime = ((Integer) processActivityECARuleMap
					.get("RepeatedTime")).intValue();

			if (repeatedTime > 0) {

				// 将重复执行的ECARule信息写入ProcessRepeatedInformation表
				// RepeatedType---8代表ActivityECARule
				sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
				params = new Object[] { event.getProcess().getProcessID(),
						new Integer(8),
						processActivityECARuleMap.get("RuleID"),
						processActivityECARuleMap.get("ConditionID"),
						processActivityECARuleMap.get("RepeatedTime"),
						processActivityECARuleMap.get("ActiveTime"),
						processActivityECARuleMap.get("ActiveTime") };

				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE };
				executeUpdate();
			}

			// 记录重复次数
			sql = "UPDATE ProcessActivityECARule SET ActiveTime=?, RepeatedTime=? WHERE ProcessID=? AND EventID=?";
			params = new Object[] { currentDate, new Integer(repeatedTime + 1),
					event.getProcess().getProcessID(),
					new Integer(event.getEventID()) };
			types = new int[] { Types.DATE, Types.INTEGER, Types.VARCHAR,
					Types.INTEGER };
			executeUpdate();

			// ECA规则，是否有条件部分
			int conditionID = ((Integer) processActivityECARuleMap
					.get("ConditionID")).intValue();
			if (conditionID != 0) {

				if (logger.isInfoEnabled())
					logger.info("condition: "
							+ conditionID
							+ " , action expression: "
							+ (String) processActivityECARuleMap
									.get("ActionExpression") + ".");

				// 有条件部分，计算条件部分的值，如果为真，则执行对应的动作
				Expression condition = new Expression(conn, event.getProcess()
						.getProcessID(), conditionID);
				if (condition.analyzeBoolExpression()) {
					actionExpression(event, (String) processActivityECARuleMap
							.get("ActionExpression"));
				}
			} else {

				if (logger.isInfoEnabled())
					logger.info("condition: no , action expression: "
							+ (String) processActivityECARuleMap
									.get("ActionExpression") + ".");

				// 无条件部分，执行对应的动作
				actionExpression(event, (String) processActivityECARuleMap
						.get("ActionExpression"));
			}
		}
	}

	private void actionExpression(Event event, String action) throws Exception {
		int intMarkPos = -1;
		String strAtomicAction = "";

		if (!action.equals("")) {
			// Remove the ()
			action = action.substring(1, action.length());
			action = action.substring(0, action.length() - 1);

			intMarkPos = action.indexOf(",");
			// 得到相应的原子动作，如(Activity.1, Event.2), 将分为Activity.1; Event.2两个原子动作
			while (intMarkPos > 0) {
				strAtomicAction = action.substring(0, intMarkPos);
				// 得到原子动作
				doAtomicAction(event, strAtomicAction);
				action = action.substring(intMarkPos + 1, action.length());
				intMarkPos = action.indexOf(",");
			}
			// 剩下的字符串也是原子动作
			doAtomicAction(event, action);
		}
	}

	private void doAtomicAction(Event event, String action) throws Exception {
		String strActionType;
		String strActionObject;

		int intMarkPos;

		// Get the action type and action object id
		intMarkPos = action.indexOf(".");
		strActionType = action.substring(0, intMarkPos);
		strActionObject = action.substring(intMarkPos + 1, action.length());

		if (logger.isInfoEnabled())
			logger.info("DO ACTION----action: " + strActionType + ", object: "
					+ strActionObject + ", executed.");

		writemsg("DO ACTION----action: " + strActionType + ", object: "
				+ strActionObject + ", executed.");
		InformationPane.writeln("DO ACTION----action: " + strActionType + ", object: "
				+ strActionObject + ", executed.");
		
		if (strActionType.equals("Event")) {

			eventQueue.push(EventFactory.create(conn, event.getProcess(),
					Integer.parseInt(strActionObject)));

		} else if (strActionType.equals("Activity")) {
			sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
			params = new Object[] { event.getProcess().getProcessID(),
					new Integer(strActionObject) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			Map processProcessECARuleMap = (Map) query.query(conn, sql, params,
					new MapHandler());
			
			if (processProcessECARuleMap != null) {
				AbstractActivity activity = ElementFactory.createActivity(conn,
						event.getProcess(), Integer.parseInt(strActionObject),
						this);
				// activity.addStateChangeListener(this);
				activity.changeState(Constants.ACTIVITY_STATE_WAITING,
						Constants.ACTIVITY_STATE_READY);
			} else {
				throw new WorkflowTransactionException(
						"There is no right data for ECArule-Action!");
			}
		} else if (strActionType.equals("Workflow")) {
			// 启动一个意外工作流
			Model model = new Model(conn, Integer.parseInt(strActionObject),
					this);
			String processID = model.instantiateWorkflow(1, 2, event
					.getProcess().getProcessID());

			Process process = new Process(conn, processID, this);

			// 获得开始活动的ID
			// sxh modified 2007.11
			int activityID = process.getLogicActivityID(
					Constants.ACTIVITY_START, 1);
			// sxh modified 2007.11 end

			AbstractActivity activity = ElementFactory.createActivity(conn,
					process, activityID, this);
			// activity.addStateChangeListener(this);

			// 调用改变活动状态过程，把该活动状态变为Running
			if (!activity.changeState(Constants.ACTIVITY_STATE_WAITING,
					Constants.ACTIVITY_STATE_COMPLETED))
				throw new Exception("Start exception process failed!");

		} else if (strActionType.equals("Notify")) {
			// 通知关心该活动状态的其它活动的承担者该活动的状态
			// 参数是活动ID，还是活动ID的列表
			// 把通知消息放入消息数据库的消息列表里
			// 目前消息表还没有建立,目前此Action还没有实现
		} else if (strActionType.equals("ActivityAbort")) {
			/*
			 * 1.把活动的状态变为Aborted 2.添加Aborted事件到事件队列
			 */
			// 1.把活动的状态变为Aborted
			sql = "UPDATE ProcessActivityInformation SET State='Aborted' WHERE ProcessID=? AND ActivityID=?";
			params = new Object[] { event.getProcess().getProcessID(),
					new Integer(strActionObject) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			executeUpdate();

			// 2.添加Aborted事件到事件队列
			eventQueue.push(EventFactory.create(conn, event.getProcess(),
					"ActivityAborted(" + strActionObject + ")"));

		} else if (strActionType.equals("ProcessAbort")) {
			/*
			 * 1.把流程的状态变为Aborted 2.添加Aborted事件到事件队列
			 */
			// 1.把流程的状态变为Aborted
			sql = "UPDATE ProcessInformation SET State='Aborted' WHERE ProcessID=?";
			params = new Object[] { strActionObject };
			types = new int[] { Types.VARCHAR };
			executeUpdate();

			// 2.添加Aborted事件到事件队列
			eventQueue.push(EventFactory.create(conn, event.getProcess(),
					"ProcessAborted(" + strActionObject + ")"));
		} else if (strActionType.equals("Execute")) {
			// 把活动状态变为Running
			sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
			params = new Object[] { event.getProcess().getProcessID(),
					new Integer(strActionObject) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			Map processProcessECARuleMap = (Map) executeQuery(new MapHandler());
			if (processProcessECARuleMap != null) {
				AbstractActivity activity = ElementFactory.createActivity(conn,
						event.getProcess(), Integer.parseInt(strActionObject),
						this);
				// activity.addStateChangeListener(this);
				activity.changeState(Constants.ACTIVITY_STATE_READY,
						Constants.ACTIVITY_STATE_RUNNING);
			} else {
				throw new WorkflowTransactionException(
						"There is no right data for handle!");
			}
		} else if (strActionType.equals("Complete")) {
			// 把活动状态变为Completed
			sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
			params = new Object[] { event.getProcess().getProcessID(),
					new Integer(strActionObject) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			Map processProcessECARuleMap = (Map) executeQuery(new MapHandler());
			;
			if (processProcessECARuleMap != null) {
				AbstractActivity activity = ElementFactory.createActivity(conn,
						event.getProcess(), Integer.parseInt(strActionObject),
						this);
				// activity.addStateChangeListener(this);
				activity.changeState(Constants.ACTIVITY_STATE_RUNNING,
						Constants.ACTIVITY_STATE_COMPLETED);
			} else {
				throw new WorkflowTransactionException(
						"There is no right data for handle!");
			}
		}
	}

	/* ding ------------------------------------------------------------------ */
	/**
	 * 功能:数据流中xml对象转换，by Dingo on 2007.5.15
	 * 
	 * @param event
	 * @param flag
	 *            1: 考虑活动的输出 2: 考虑活动的输入
	 * @throws Exception
	 */
	private void xmlObjectTranslate(ActivityStateChangeEvent event, int flag)
			throws Exception {
//      这个函数似乎没有用到，dataflow中的XSLTO2M,XSLTM2I好像都是null的，因此在加context aware dataflow时，把这整个函数注释掉了		
//		// String header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
//		List objectList;
//		Iterator objectIterator;
//		String processID = event.getActivity().getProcess().getProcessID();
//		int activityID = event.getActivity().getActivityID();
//		String eventName = "EndOf(" + activityID + ")";
//		Map objectMap;
//		int eventID = 0;
//
//		logger.info("ProcessID: " + processID + "; flag: " + flag);
//
//		if (flag == 1) {
//			try {
//				sql = "select EventID from ProcessEvent where ProcessID = ? AND ActivityID=? AND EventName=?";
//				params = new Object[] { processID, new Integer(activityID),
//						eventName };
//				types = new int[] { Types.VARCHAR, Types.INTEGER, Types.VARCHAR };
//				objectList = (List) executeQuery(new MapListHandler());
//				objectIterator = objectList.iterator();
//				// 没有考虑返回多个记录的情况
//				if (objectIterator.hasNext()) {
//					objectMap = (Map) objectIterator.next();
//					eventID = ((Integer) objectMap.get("EventID")).intValue();
//				}
//			} catch (Exception e) {
//				logger.error(e.toString());
//			}
//		} else {
//			try {
//				sql = "select EventID from ProcessFlowObjects where ProcessID = ? AND ToActivityID=? AND FromActivityID <> 0";
//				params = new Object[] { processID, new Integer(activityID) };
//				types = new int[] { Types.VARCHAR, Types.INTEGER };
//				objectList = (List) executeQuery(new MapListHandler());
//				objectIterator = objectList.iterator();
//
//				if (objectIterator.hasNext()) {
//					objectMap = (Map) objectIterator.next();
//					eventID = ((Integer) objectMap.get("EventID")).intValue();
//				}
//			} catch (Exception e) {
//				logger.error(e.toString());
//			}
//		}
//
//		// 在此增加对数据流的处理
//		String stylesheeto2m, stylesheetm2i, oxml, mxml, ixml, new_mxml, new_ixml;
//		try {
//			sql = "select FromActivityID,ToActivityID,XSLTO2M,XSLTM2I,OXMLID,MXMLID,IXMLID from ProcessFlowObjects where ProcessID = ? AND EventID=? AND FromActivityID <> 0";
//			params = new Object[] { processID, new Integer(eventID) };
//			types = new int[] { Types.VARCHAR, Types.INTEGER };
//			objectList = (List) executeQuery(new MapListHandler());
//			objectIterator = objectList.iterator();
//
//			Map xmlMap;
//			List xmlList;
//			Iterator xmlIterator;
//			int fromActivityID, toActivityID;
//			int fromActivityImplementation = -1, toActivityImplementation = -1;
//
//			try {
//				TransformerFactory factory = TransformerFactory.newInstance();
//				while (objectIterator.hasNext()) {
//					objectMap = (Map) objectIterator.next();
//					fromActivityID = ((Integer) objectMap.get("FromActivityID"))
//							.intValue();
//					toActivityID = ((Integer) objectMap.get("ToActivityID"))
//							.intValue();
//					logger.info("FromActivityID: " + fromActivityID
//							+ "; ToActivityID: " + toActivityID);
//					// //////////////////////////dingo updated on 07-07-28
//					sql = "SELECT * FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
//					params = new Object[] { processID,
//							new Integer(fromActivityID) };
//					types = new int[] { Types.VARCHAR, Types.INTEGER };
//					List wfoList1 = (List) executeQuery(new MapListHandler());
//					Iterator wfoIterator1 = wfoList1.iterator();
//					if (wfoIterator1.hasNext()) {
//						Map wfoMap1 = (Map) wfoIterator1.next();
//						fromActivityImplementation = ((Integer) wfoMap1
//								.get("ActivityImplementation")).intValue();
//					}
//
//					params = new Object[] { processID,
//							new Integer(toActivityID) };
//					types = new int[] { Types.VARCHAR, Types.INTEGER };
//					wfoList1 = (List) executeQuery(new MapListHandler());
//					wfoIterator1 = wfoList1.iterator();
//					if (wfoIterator1.hasNext()) {
//						Map wfoMap1 = (Map) wfoIterator1.next();
//						toActivityImplementation = ((Integer) wfoMap1
//								.get("ActivityImplementation")).intValue();
//					}
//
//					// //////////////////////////dingo updated on 07-07-28
//
//					InputStream is;
//
//					if ((fromActivityID != 1 && (fromActivityImplementation == 2
//							|| fromActivityImplementation == 8 || fromActivityImplementation == 9))
//							&& flag == 1) { // output1 to m
//						// ActivityID = 1
//						// 为开始活动
//						stylesheeto2m = (String) objectMap.get("XSLTO2M");
//						logger.info("XSLTO2M: " + stylesheeto2m);
//						sql = "SELECT XML FROM ProcessXMLDocument WHERE ProcessID=? AND ObjectID=?";
//						params = new Object[] { processID,
//								(Integer) objectMap.get("OXMLID") };
//						types = new int[] { Types.VARCHAR, Types.INTEGER };
//						xmlList = (List) executeQuery(new MapListHandler());
//						xmlIterator = xmlList.iterator();
//
//						if (xmlIterator.hasNext()) {
//							xmlMap = (Map) xmlIterator.next();
//							oxml = (String) xmlMap.get("XML");
//							// oxml = header + oxml;
//
//							if (logger.isInfoEnabled()) {
//								logger.info("活动输出的XML ID（OXMLID）: "
//										+ objectMap.get("OXMLID"));
//								logger.info("OXML-XML: " + oxml);
//							}
//
//							// output1 to m
//							is = new ByteArrayInputStream(stylesheeto2m
//									.getBytes());
//							Transformer transformer1 = factory
//									.newTransformer(new StreamSource(is));
//							//servercomment System.out.println("Transformer1 successfully!");
//							// now lets style the given document
//							DocumentSource source1 = new DocumentSource(
//									DocumentHelper.parseText(oxml));
//							DocumentResult result1 = new DocumentResult();
//							transformer1.transform(source1, result1);
//							is.close();
//							// return the transformed document
//							Document transformedDoc1 = result1.getDocument();
//							mxml = transformedDoc1.asXML();
//							// new_mxml = mxml.substring(header.length());//
//							// 去掉xml文件头
//							//servercomment System.out.println("new_xml: " + mxml);// !!!!!!!!!
//							sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
//							params = new Object[] { mxml, processID,
//									(Integer) objectMap.get("MXMLID") };
//							types = new int[] { Types.VARCHAR, Types.VARCHAR,
//									Types.INTEGER };
//							//servercomment System.out.println("insert xml into db!");// !!!!!!!!!
//							executeUpdate();
//						}
//					}
//					if (toActivityID != 2
//							&& (toActivityImplementation == 2
//									|| toActivityImplementation == 8 || toActivityImplementation == 9)
//							&& flag == 2) {
//						// m to input2 ActivityID = 2为结束活动
//						stylesheetm2i = (String) objectMap.get("XSLTM2I");
//						logger.info("XSLTM2I: " + stylesheetm2i);
//
//						sql = "SELECT XML FROM ProcessXMLDocument WHERE ProcessID=? AND ObjectID=?";
//						params = new Object[] { processID,
//								(Integer) objectMap.get("MXMLID") };
//						types = new int[] { Types.VARCHAR, Types.INTEGER };
//						xmlList = (List) executeQuery(new MapListHandler());
//						xmlIterator = xmlList.iterator();
//
//						if (xmlIterator.hasNext()) {
//							xmlMap = (Map) xmlIterator.next();
//							mxml = (String) xmlMap.get("XML");
//							logger.info("MXML:\nID: " + objectMap.get("MXMLID")
//									+ "\nXML:" + mxml);
//
//							// mxml = header + mxml;// zy
//							// //servercomment System.out.println("mxml: " + mxml);// zy
//							is = new ByteArrayInputStream(stylesheetm2i
//									.getBytes());
//							Transformer transformer2 = factory
//									.newTransformer(new StreamSource(is));
//
//							// now lets style the given document
//							DocumentSource source2 = new DocumentSource(
//									DocumentHelper.parseText(mxml));
//							DocumentResult result2 = new DocumentResult();
//							transformer2.transform(source2, result2);
//							is.close();
//
//							//servercomment System.out.println("Transformer2 successfully!");
//
//							// return the transformed document
//							Document transformedDoc2 = result2.getDocument();
//							ixml = transformedDoc2.asXML();
//							// new_ixml = ixml.substring(header.length());//
//							// 去掉xml文件头
//							logger.info("new_ixml: " + ixml);// !!!!!!!!!
//							sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
//							params = new Object[] { ixml, processID,
//									(Integer) objectMap.get("IXMLID") };
//							types = new int[] { Types.VARCHAR, Types.VARCHAR,
//									Types.INTEGER };
//							//servercomment System.out.println("insert ixml into db!");// !!!!!!!!!
//							executeUpdate();
//						}
//					}
//				}
//			} catch (TransformerFactoryConfigurationError tfce) {
//				//servercomment System.out.println("Could not obtain factory!");
//				tfce.printStackTrace();
//			} catch (TransformerConfigurationException tce) {
//				//servercomment System.out.println("Could not create transformer!");
//				tce.printStackTrace();
//			} catch (TransformerException te) {
//				//servercomment System.out.println("Transformer exception!");
//				te.printStackTrace();
//			}
//		} catch (Exception e) {
//			//servercomment System.out.println(e.toString());
//		}
	}

	/*---------------------------------------------------------------------*/

	/**
	 * 功能:改变属于该Actor的活动副本状态
	 * 
	 * @param fromState
	 * @param toState
	 *            ----在这里只有两种状态:Running, Completed
	 * 
	 * 步骤 1.检查是否已经存在该Actor的活动副本,如果已经存在则把原来的纪录存入ProcessRepeatedInformation中,
	 * 并且更新ProcessActivityPerson表的纪录;否则在ProcessActivityPerson新增一条记录
	 */
	private void changeActorActivityState(AbstractActivity activity,
			String fromState, String toState) throws SQLException {

		if (currentUser == null)
			return;

		sql = "SELECT * FROM ProcessActivityPerson WHERE ProcessID=? AND ActivityID=? AND PersonID=?";
		params = new Object[] { activity.getProcess().getProcessID(),
				new Integer(activity.getActivityID()),
				new Integer(currentUser.getActorID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		Map processActivityPersonMap = (Map) executeQuery(new MapHandler());
		if (processActivityPersonMap != null) {

			// 如果活动重复执行,就把重复信息写入表ProcessRepeatedInformation
			// 要在信息被更新之前,把重复信息写入ProcessRepeatedInformation
			if (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) {

				// RepeatedType---2代表Person
				sql = "INSERT INTO ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1, RepeatedID2, RepeatedTime, StartTime, EndTime) VALUES(?,?,?,?,?,?,?)";
				params = new Object[] { activity.getProcess().getProcessID(),
						new Integer(2), new Integer(activity.getActivityID()),
						new Integer(currentUser.getActorID()),
						processActivityPersonMap.get("RepeatedTime"),
						processActivityPersonMap.get("ActualStartDate"),
						processActivityPersonMap.get("ActualEndDate") };

				types = new int[] { Types.VARCHAR, Types.INTEGER,
						Types.INTEGER, Types.INTEGER, Types.INTEGER,
						Types.DATE, Types.DATE };
				executeUpdate();
			}

			// 如果改变后的状态为Running,则需要修改实际开始时间,否则需要修改实际结束时间并且重复次数加1
			if (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) {
				sql = "UPDATE ProcessActivityPerson SET ActualStartDate=?, State=? WHERE ProcessID=? AND ActivityID=? AND PersonID=?";
				params = new Object[] {
						new java.sql.Date(new java.util.Date().getTime()),
						toState, activity.getProcess().getProcessID(),
						new Integer(activity.getActivityID()),
						new Integer(currentUser.getActorID()) };
				types = new int[] { Types.DATE, Types.VARCHAR, Types.VARCHAR,
						Types.INTEGER, Types.INTEGER };
			} else {
				sql = "UPDATE ProcessActivityPerson SET RepeatedTime=?, ActualEndDate=?, State=? WHERE ProcessID=? AND ActivityID=? AND PersonID=?";
				params = new Object[] {
						new Integer(((Integer) processActivityPersonMap
								.get("RepeatedTime")).intValue() + 1),
						currentDate, toState,
						activity.getProcess().getProcessID(),
						new Integer(activity.getActivityID()),
						new Integer(currentUser.getActorID()) };
				types = new int[] { Types.INTEGER, Types.DATE, Types.VARCHAR,
						Types.VARCHAR, Types.INTEGER, Types.INTEGER };
			}
			executeUpdate();

		} else {

			// 进入这里toState必然为Constants.ACTIVITY_STATE_RUNNING,即Running
			// sql = "SELECT * FROM ProcessTWCInformation PTI LEFT JOIN
			// ProcessActivityInformation PAI ON PAI.ProcessID=PTI.ProcessID AND
			// PAI.TWCID=PTI.TWCID WHERE PTI.ProcessID=? AND PAI.ActivityID=?";
			// params = new Object[] {activity.getProcess().getProcessID(), new
			// Integer(activity.getActivityID())};
			// types = new int[] {Types.VARCHAR, Types.INTEGER};
			// Map processTWCInformationMap = (Map) executeQuery(new
			// MapHandler());
			// if (processTWCInformationMap != null) {

			/**
			 * get the role information add the role infomation to the table
			 * ProcessActivityPerson 2006.4.10 Dy
			 */
			int roleID = 0;
			sql = "SELECT * FROM PersonRole  WHERE PersonID=? and RoleID IN (Select roleID from ProcessActivityRole where processID=? and ActivityID =?)";
			params = new Object[] { new Integer(currentUser.getActorID()),
					activity.getProcess().getProcessID(),
					new Integer(activity.getActivityID()) };
			types = new int[] { Types.INTEGER, Types.VARCHAR, Types.INTEGER };
			Map resultMap = null;
			resultMap = (Map) executeQuery(new MapHandler());
			if (resultMap != null) {
				roleID = ((Integer) resultMap.get("RoleID")).intValue();
			}
			// 未更新的字段有:RoleID,ActualStartDate,ActualEndDate,ActualWorkload
			sql = "INSERT INTO ProcessActivityPerson(ProcessID, ActivityID,RoleID, PersonID, RepeatedTime, DefinedStartDate, ActualStartDate, DefinedEndDate, DefinedWorkload, State) VALUES(?,?,?,?,?,?,?,?,?,?)";
			params = new Object[] { activity.getProcess().getProcessID(),
					new Integer(activity.getActivityID()), new Integer(roleID),
					new Integer(currentUser.getActorID()), new Integer(0),
					new java.sql.Date(new java.util.Date().getTime()),
					new java.sql.Date(new java.util.Date().getTime()),
					new java.sql.Date(new java.util.Date().getTime()),
					new Integer(1), toState };

			types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,
					Types.INTEGER, Types.INTEGER, Types.DATE, Types.DATE,
					Types.DATE, Types.DATE, Types.INTEGER };
			executeUpdate();

			/**
			 * 需要将allocate number增加一个 表明该任务已分配给某人
			 * 
			 */
			// sql = "SELECT * FROM ProcessActivityRole WHERE ProcessID = ? AND
			// ActivityID=? AND RoleID= ?";
			// params = new Object[] {activity.getProcess().getProcessID(),
			// new Integer(activity.getActivityID()),
			// new Integer(roleID)};
			// types = new int[] {Types.VARCHAR,Types.INTEGER,Types.INTEGER};
			// resultMap = (Map) executeQuery(new MapHandler());
			// if (resultMap != null) {
			// int Num = ((Integer)resultMap.get("AllocatedNumber")).intValue();
			// Num++;
			sql = "UPDATE ProcessActivityRole SET AllocatedNumber = AllocatedNumber+1 where  ProcessID = ? AND ActivityID=? AND RoleID= ?";
			params = new Object[] { activity.getProcess().getProcessID(),
					new Integer(activity.getActivityID()), new Integer(roleID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
			executeUpdate();
			// }

			// //未更新的字段有:RoleID,ActualStartDate,ActualEndDate,ActualWorkload
			// sql = "INSERT INTO ProcessActivityPerson(ProcessID, ActivityID,
			// PersonID, RepeatedTime, DefinedStartDate, ActualStartDate,
			// DefinedEndDate, DefinedWorkload, State)
			// VALUES(?,?,?,?,?,?,?,?,?)";
			// params = new Object[] { activity.getProcess().getProcessID(), new
			// Integer(activity.getActivityID()),
			// new Integer(currentUser.getActorID()), new Integer(0),
			// new java.sql.Date(new java.util.Date().getTime()), new
			// java.sql.Date(new java.util.Date().getTime()),
			// new java.sql.Date(new java.util.Date().getTime()), new
			// Integer(1),
			// toState};
			//				
			// types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,
			// Types.INTEGER, Types.DATE, Types.DATE, Types.DATE, Types.DATE,
			// Types.INTEGER };
			// executeUpdate();
			// } else {
			// throw new WorkflowTransactionException("There is no right data
			// for handle!");
			// }
		}

		/*
		 * if (currentUser == null) return;
		 * 
		 * sql = "SELECT * FROM ProcessActivityPerson WHERE ProcessID=? AND
		 * ActivityID=? AND PersonID=?"; params = new
		 * Object[]{activity.getProcess().getProcessID(), new
		 * Integer(activity.getActivityID()), new
		 * Integer(currentUser.getActorID())}; types = new int[] {Types.VARCHAR,
		 * Types.INTEGER, Types.INTEGER}; Map processActivityPersonMap = (Map)
		 * executeQuery(new MapHandler()); if (processActivityPersonMap != null) {
		 * 
		 * //如果活动重复执行,就把重复信息写入表ProcessRepeatedInformation
		 * //要在信息被更新之前,把重复信息写入ProcessRepeatedInformation if
		 * (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) {
		 * 
		 * //RepeatedType---2代表Person sql = "INSERT INTO
		 * ProcessRepeatedInformation(ProcessID, RepeatedType, RepeatedID1,
		 * RepeatedID2, RepeatedTime, StartTime, EndTime)
		 * VALUES(?,?,?,?,?,?,?)"; params = new Object[] {
		 * activity.getProcess().getProcessID(), new Integer(2), new
		 * Integer(activity.getActivityID()), new
		 * Integer(currentUser.getActorID()),
		 * processActivityPersonMap.get("RepeatedTime"),
		 * processActivityPersonMap.get("ActualStartDate"),
		 * processActivityPersonMap.get("ActualEndDate")};
		 * 
		 * types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER,
		 * Types.INTEGER, Types.INTEGER, Types.DATE, Types.DATE};
		 * executeUpdate(); }
		 * 
		 * //如果改变后的状态为Running,则需要修改实际开始时间,否则需要修改实际结束时间并且重复次数加1 if
		 * (toState.equals(Constants.ACTIVITY_STATE_RUNNING)) { sql = "UPDATE
		 * ProcessActivityPerson SET ActualStartDate=?, State=? WHERE
		 * ProcessID=? AND ActivityID=? AND PersonID=?"; params = new Object[]
		 * {new java.sql.Date(new java.util.Date().getTime()), toState,
		 * activity.getProcess().getProcessID(), new
		 * Integer(activity.getActivityID()), new
		 * Integer(currentUser.getActorID())}; types = new int[] {Types.DATE,
		 * Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER}; } else {
		 * sql = "UPDATE ProcessActivityPerson SET RepeatedTime=?,
		 * ActualEndDate=?, State=? WHERE ProcessID=? AND ActivityID=? AND
		 * PersonID=?"; params = new Object[] {new
		 * Integer(((Integer)processActivityPersonMap.get("RepeatedTime")).intValue() +
		 * 1), currentDate, toState, activity.getProcess().getProcessID(), new
		 * Integer(activity.getActivityID()), new
		 * Integer(currentUser.getActorID())}; types = new int[] {Types.INTEGER,
		 * Types.DATE, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
		 * Types.INTEGER}; } executeUpdate(); } else {
		 * 
		 * //进入这里toState必然为Constants.ACTIVITY_STATE_RUNNING,即Running sql =
		 * "SELECT * FROM ProcessTWCInformation PTI LEFT JOIN
		 * ProcessActivityInformation PAI ON PAI.ProcessID=PTI.ProcessID AND
		 * PAI.TWCID=PTI.TWCID WHERE PTI.ProcessID=? AND PAI.ActivityID=?";
		 * params = new Object[] {activity.getProcess().getProcessID(), new
		 * Integer(activity.getActivityID())}; types = new int[] {Types.VARCHAR,
		 * Types.INTEGER}; Map processTWCInformationMap = (Map) executeQuery(new
		 * MapHandler()); if (processTWCInformationMap != null) {
		 * 
		 * //未更新的字段有:RoleID,ActualStartDate,ActualEndDate,ActualWorkload sql =
		 * "INSERT INTO ProcessActivityPerson(ProcessID, ActivityID, PersonID,
		 * RepeatedTime, DefinedStartDate, ActualStartDate, DefinedEndDate,
		 * DefinedWorkload, State) VALUES(?,?,?,?,?,?,?,?,?)"; params = new
		 * Object[] { activity.getProcess().getProcessID(), new
		 * Integer(activity.getActivityID()), new
		 * Integer(currentUser.getActorID()), new Integer(0),
		 * processTWCInformationMap.get("DefinedStartDate"), new
		 * java.sql.Date(new java.util.Date().getTime()),
		 * processTWCInformationMap.get("DefinedEndDate"),
		 * processTWCInformationMap.get("DefinedWorkload"), toState};
		 * 
		 * types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,
		 * Types.INTEGER, Types.DATE, Types.DATE, Types.DATE, Types.DATE,
		 * Types.INTEGER }; executeUpdate(); } else { throw new
		 * WorkflowTransactionException("There is no right data for handle!"); } }
		 */
	}

	/**
	 * 功能:判断该用户是否能够打开活动
	 * 
	 * @return
	 * 
	 * 步骤: 1.查找该用户所属角色对于该活动所需的最多人数,如果已分配人数达到最多人数则不可以打开
	 */
	private boolean canOpen(AbstractActivity activity) throws SQLException {
		if (currentUser == null)
			return true;

		sql = "SELECT PAR.ActivityID FROM ProcessActivityRole PAR LEFT JOIN PersonRole PR ON PR.RoleID=PAR.RoleID WHERE ProcessID=? AND ActivityID=? AND PR.PersonID=? AND MaximalNumber > AllocatedNumber";
		params = new Object[] { activity.getProcess().getProcessID(),
				new Integer(activity.getActivityID()),
				new Integer(currentUser.getActorID()) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		Map processActivityRoleMap = (Map) executeQuery(new MapHandler());
		return processActivityRoleMap == null ? false : true;
	}
	
	public void writemsg(String string){
		processLog+=string+"\n";
	}
	
	public boolean updateLog(String processID){
		sql="UPDATE processlogs SET log=?, endtime=?, idletime=? WHERE ProcessID=?";
		timeRecoder.setEndTime(System.currentTimeMillis());
		params=new Object[]{processLog,timeRecoder.getStartTime(),timeRecoder.getWaitTime(),processID};
		types=new int[]{Types.VARCHAR,Types.INTEGER,Types.INTEGER,Types.VARCHAR};
		try {
			executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void clearData(String processID){
//		ArrayList<String> tablelist = new ArrayList();
//		PreparedStatement pstmt = null;
//		try {
//			ResultSet tableset =conn.getMetaData().getTables(null, null, "process%", null);
//			while (tableset.next()) {
//				tablelist.add(tableset.getString(3));
//			}
//			tableset.close();
//			for(String table:tablelist){
//				if(table.equals("processlogs"))continue;
//				String connstr = "delete from " + table + " where ProcessID =\"" + processID+"\"";
////				String connstr = "delete from " + table;
////				System.out.println(connstr);
//				pstmt = conn.prepareStatement(connstr);
//				pstmt.execute();
//				pstmt.close();
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
	
	public static void clearAllData(){
		Connection mconn=WorkflowConnectionPool.getInstance().getConnection();
		ArrayList<String> tablelist = new ArrayList();
		PreparedStatement pstmt = null;
		try {
			ResultSet tableset =mconn.getMetaData().getTables(null, null, "process%", null);
			while (tableset.next()) {
				tablelist.add(tableset.getString(3));
			}
			tableset.close();
			for(String table:tablelist){
				if(table.equals("processlogs"))continue;
//				String connstr = "delete from " + table + " where ProcessID =\"" + processID+"\"";
				String connstr = "delete from " + table;
				pstmt = mconn.prepareStatement(connstr);
				pstmt.execute();
				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				mconn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("all clear");
	}
	
	private void getPreviousTime(String processID){
		String sql="SELECT starttime,endtime from processlogs where processid=?";
		PreparedStatement pst;
		long starttime=0;
		long endtime=0;
		try {
			pst = conn.prepareStatement(sql);
			pst.setString(1, processID);
			ResultSet rs=pst.executeQuery();
			while(rs.next()){
				starttime=rs.getLong(1);
				endtime=rs.getLong(2);
			}
			timeRecoder.setStartTime(starttime);
//			timeRecoder.addWaitTime(System.currentTimeMillis()-endtime);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public class TimeRecoder{
		private long startTime;
		private long endTime;
		private long waitTime=0;
		
		public TimeRecoder(){
			
		}
		
		public long getStartTime() {
			return startTime;
		}
		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
		public long getEndTime() {
			return endTime;
		}
		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}
		public long getWaitTime() {
			return waitTime;
		}
		public void addWaitTime(long waitTime) {
			this.waitTime += waitTime;
		}
		public long getBusyTime(){
			return endTime-startTime-waitTime;
		}
		
	}
	
	public static void main(String[] args) {
		clearAllData();
	}
}
