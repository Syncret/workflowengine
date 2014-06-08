/*
 * Created on 2004-10-14
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow;

import org.antlr.grammar.v3.ANTLRv3Parser.finallyClause_return;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Constants {
	/*
	public static final String CONN_DIRVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver"; 
	public static final String CONN_URL = "jdbc:microsoft:sqlserver://ww:1433;DatabaseName=JWorkflow";
	public static final String CONN_USERNAME = "sa";
	public static final String CONN_PASSWORD = "";
	public static final boolean CONN_AUTOCOMMIT = true;*/
	
	public static final String CONN_DRIVER = Resources.getString("CONN_DRIVER");
	public static final String CONN_BASEURL=Resources.getString("CONN_BASEURL");
	public static final String CONN_DATABASE=Resources.getString("CONN_DATABASE");
	public static final String CONN_URL = Resources.getString("CONN_URL");
	public static final String CONN_USERNAME = Resources.getString("CONN_USERNAME");
	public static final String CONN_PASSWORD = Resources.getString("CONN_PASSWORD");
	public static final String GRAPH_PATH = Resources.getString("GRAPH_PATH");
	public static final String AGENTRULE_PATH = Resources.getString("AGENTRULE_PATH");
	public static final String WORKFLOW_ID = Resources.getString("WORKFLOW_ID");
	public static final boolean CONN_AUTOCOMMIT = true;
	
	/**
	 * Actor类型:
	 * 		1----Person
	 * 		2----Agent
	 * 		3----Agency
	 */
	 public static final int ACTOR_PERSON = 1;
	 public static final int ACTOR_AGENT = 2;
	 public static final int ACTOR_AGENCY = 3;
	
	/**
	 * 活动的类型分类：
	 * 		1----开始活动
	 * 		2----结束活动
	 * 		3----一般活动
	 */
	public static final int ACTIVITY_ROOT = -1;
	public static final int ACTIVITY_START = 1;
	public static final int ACTIVITY_END = 2;
	public static final int ACTIVITY_GENERAL = 3;
	
	/**
	 * 活动的状态分类：
	 * 		Waiting, Ready, Running, Completed, Any
	 */
	public static final String ACTIVITY_STATE_WAITING = "Waiting";
	public static final String ACTIVITY_STATE_READY = "Ready";
	public static final String ACTIVITY_STATE_RUNNING = "Running";
	public static final String ACTIVITY_STATE_COMPLETED = "Completed";
	//sxh May 2008
	public static final String ACTIVITY_STATE_HOLDING = "Holding";
	public static final String ACTIVITY_STATE_OVERTIME = "OverTime";
	public static final String ACTIVITY_STATE_ABORT = "Abort";
	public static final String ACTIVITY_STATE_ERROR = "Error";
	//end of sxh May 2008
	public static final String ACTIVITY_STATE_ANY = "Any";
	
	/**
	 * 任务分配状态分类：
	 * 		Accepted, UnAccepted, Submitted
	 */
	public static final String TASK_STATE_ACCEPTED = "Accepted";
	public static final String TASK_STATE_UNACCEPTED = "UnAccepted";
	public static final String TASK_STATE_SUBMITTED = "Submitted";
	
	/**
	 * 事件名称:
	 * 		Initialize, BeginOf, EndOf
	 */
	public static final String EVENT_INITIALIZE = "Initialized";
	public static final String EVENT_BEIGINOF = "Started";
	public static final String EVENT_ENDOF = "EndOf";
	//sxh May 2008
	public static final String EVENT_OVERTIME = "OverTime";
	public static final String EVENT_ERROR = "Error";
	public static final String EVENT_ABORT = "Aborted";
	//end of sxh May 2008
	
	/**
	 * 流程的状态分类：
	 * 		Created，Running，Completed，InError, Suspended, Aborted
	 */
	public static final String PROCESS_STATE_CREATED = "Created";
	public static final String PROCESS_STATE_RUNNING = "Running";
	public static final String PROCESS_STATE_COMPLETED = "Completed";
	
	/**
	 * 对象的状态分类：
	 * 		0----未生成
	 * 		1----生成
	 * 		2----正在使用
	 * 		3----已删除
	 */
	public static final int OBJECT_STATE_NOTCREATED = 0;
	public static final int OBJECT_STATE_CREATED = 1;
	public static final int OBJECT_STATE_INUSED = 2;
	public static final int OBJECT_STATE_DELETED = 3;
	
	/**
	 * 对象的类型：
	 * 		1----内部变量
	 * 		2----对象变量
	 * 		3----XML对象
	 * 		4----文档对象
	 * 		5----引用对象
	 */
	public static final int OBJECT_TYPE_INHERENT = 1;
	public static final int OBJECT_TYPE_OBJECT = 2;
	public static final int OBJECT_TYPE_XML = 3;
	public static final int OBJECT_TYPE_DOC = 4;
	public static final int OBJECT_TYPE_REF = 5;
	//cyd add
	public static final int OBJECT_TYPE_ARRAY =6;
	
	/**
	 * 对象的权限分类：
	 * 		1----Read
	 * 		2----Check In/Out
	 */
	public static final int OBJECT_RIGHT_READ = 1;
	public static final int OBJECT_RIGHT_CHECK = 2;
	
	/*
	 * 数据流的状态分类：
	 * 		InActive, Active
	 */
	public static final String DATAFLOW_STATE_INACTIVE = "InActive";
	public static final String DATAFLOW_STATE_ACTIVE = "Active";
	
	//事件分类
	public static final String ACTION_EVENT_TYPE = "Action";
	public static final String ACTIVITY_STATE_EVENT_TYPE = "ActivityState";

	//前缀操作符
	public static final String aryPreOperators[] = { "REP", "NOT",  "ANY"};    
	//中缀操作符
	public static final String aryMidOperators[] = { "AND", "OR",  "PRE"}; 
	
	public static final int PRE_OP_NUMBER = 3;
	public static final int MID_OP_NUMBER = 3;
	
	//提交任务模式
	public static final int SUBMITTED_ONE = 1;
	public static final int SUBMITTED_ALL = 2;
	
	/*
	 * 数据类型:
	 * 		1：integer, 2:float, 3:double，4：String，5：Booleen
	 */
	public static final int DATA_TYPE_INTEGER = 1;
	public static final int DATA_TYPE_FLOAT = 2;
	public static final int DATA_TYPE_DOUBLE = 3;
	public static final int DATA_TYPE_STRING = 4;
	public static final int DATA_TYPE_BOOLEAN = 5;
	
	/*
	 * 调用方式：
	 * 		1.同步, 2.异步
	 */
	public static final int INVOKE_TYPE_SYNCHRONIZATION = 1;
	public static final int INVOKE_TYPE_ASYNCHRONISM = 2;
	
	/*
	 * 注册事件类型：
	 */
	public static final int WORKFLOW_SERVICE_EVENT = 1;
	
	/*
	 * 监听事件类型：
	 */
	public static final int WORKFLOW_INSTANTIATE_EVENT = 1;
	public static final int WORKFLOW_STARTPROCESS_EVENT = 2;
	public static final int WORKFLOW_OPENACTIVITY_EVENT = 3;
	public static final int WORKFLOW_SUBMITACTIVITY_EVENT = 4;
	public static final int WORKFLOW_GETTASKLIST_EVENT = 5;
	
	/*
	 * 流程事件类型:
	 * 		1.create, 2.start, 3.complete, 4.abort
	 */
	public static final int EVENT_PROCESS_CREATED = 1;
	public static final int EVENT_PROCESS_STARTED = 2;
	public static final int EVENT_PROCESS_COMPLETED = 3;
	public static final int EVENT_PROCESS_ABORTED = 4;
	
	/*
	 应用类型:
		1：JavaClass
		2: COM/DCOM
		3: Script
		4：Web Service
		5: CommandLine
	 */
	public static final int APPLICATION_JAVACLASS = 1;
	public static final int APPLICATION_COMDCOM = 2;
	public static final int APPLICATION_SCRIPT = 3;
	public static final int APPLICATION_WEB_SERVICE = 4;
	public static final int APPLICATION_COMMANDLINE = 5;
	public static final int APPLICATION_DATABASE = 6;
	public static final int APPLICATION_QUERY = 7;
	public static final int APPLIATION_CLOUDSERVICE = 8;
	public static final int APPLICATION_COMMONAPPLICATION=9;
	
	/*
	 * WebService输出类型:
	 * 1:简单类型
	 * 2:XML类型
	 */
	public static final int WS_OUTPUT_SIMPLE = 1;
	public static final int WS_OUTPUT_XML = 2;
	//cyd add
	public static final int WS_OUTPUT_ARRAY=3;
	
	
	
	/**
	 * 数据流文件路径
	 */
	public static final String workflowFilePath = "C:\\";
	public static final String processFilePath = "\\\\127.0.0.1\\Process_Doc";
	public static final String processTemplatePath = "\\\\127.0.0.1\\template";
	public static final String processDocPath = "\\\\127.0.0.1\\doc";
	
	public static void main(String[] args){
		
	}
	
}
