package test;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cit.workflow.Constants;
import cit.workflow.Agentsubmition;
import cit.workflow.WorkflowMonitorManager;
import cit.workflow.WorkflowPublicManager;
import cit.workflow.WorkflowServer;
import cit.workflow.dao.ActivityInformationDAO;
import cit.workflow.elements.Role;
import cit.workflow.elements.Task;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.ObjectVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.elements.variables.XMLVariable;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.graph.ProcessPicService;
import cit.workflow.utils.WorkflowConnectionPool;
import daemon.DaemonApplication;

public class SxhTest {

	String ProcessID = "";

	public static List getWorkflow(int roleID, int functionType) {
		WorkflowPublicManager wp = WorkflowPublicManager.getInstance();
		List workflowList = null;
		try {

			workflowList = wp.getWorkflow(roleID, functionType);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (int i = 0; i < workflowList.size(); ++i) {
			//servercomment System.out.println(workflowList.get(i));
		}
		return workflowList;
	}

	public static String instantiateWorkflow(int workflowID, int source,
			int caseType, String parentCaseID, int actorType, int actorID) {
		String processID = null;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			processID = ws.instantiateWorkflow(workflowID, source, caseType,
					parentCaseID, actorType, actorID);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return processID;
	}

	public static Object[] startProcess(String processID, int actorType,
			int actorID) {
		Object[] log = null;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			log = ws.startProcess(processID, actorType, actorID);
			//servercomment System.out.println("Start Process = " + success);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return log;
	}

	public static int openActivity(String processID, int activityID,
			int personID) {
		int flag = -1;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			flag = ws.openActivity(processID, activityID, personID);
			//servercomment System.out.println("Open Activity = " + flag);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	public static int submitActivity(String processID, int activityID,
			int personID) {
		int flag = -1;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			flag = ws.submitActivity(processID, activityID, personID);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	public static List getTaskList(int personID) {
		WorkflowServer ws = WorkflowServer.getInstance();
		List taskList = null;
		try {

			taskList = ws.getTaskList(personID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (int i = 0; i < taskList.size(); ++i) {
			//servercomment System.out.println(taskList.get(i));
		}
		return taskList;
	}

	public static List getHistoryTaskList(int personID) {
		WorkflowServer ws = WorkflowServer.getInstance();
		List taskList = null;
		try {

			taskList = ws.getHistoryTaskList(personID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return taskList;
	}

	public static int[] getDelegatedRoleIDSet(int delegatingRoleID) {
		WorkflowServer ws = WorkflowServer.getInstance();
		int[] resultSet = null;
		try {
			resultSet = ws.getDelegatedRoleIDSet(delegatingRoleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	public static List getDelegatedPersonList(int delegatingRoleID) {
		WorkflowServer ws = WorkflowServer.getInstance();
		List resultList = null;
		try {
			resultList = ws.getDelegatedPersonList(delegatingRoleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultList;
	}

	public static boolean delegate(String processID, int activityID,
			int delegatingRoleID, int delegatedRoleID, int delegatedPersonID,
			Date startTime, Date endTime) {
		WorkflowServer ws = WorkflowServer.getInstance();
		boolean success = false;
		try {
			success = ws.delegate(processID, activityID, delegatingRoleID,
					delegatedRoleID, delegatedPersonID, startTime, endTime);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return success;
	}

	public static boolean acceptTask(int personID, String processID,
			int activityID) {
		WorkflowServer ws = WorkflowServer.getInstance();
		boolean success = false;
		try {
			success = ws.acceptTask(personID, processID, activityID);
			//servercomment System.out.println("Accept Task = " + success);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return success;
	}

	public static int bindingActivitytoPerson(int personID, String processID,
			int activityID) {
		int flag = -1;
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			flag = ws.bindingActivitytoPerson(personID, processID, activityID);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	public static List getInputObjectList(String processID, int activityID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		List l = null;
		try {
			l = workflowPublicManager.getInputObjectList(processID, activityID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return l;
	}

	public static List getInternalObjectList(String processID, int activityID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		List l = null;
		try {
			l = workflowPublicManager.getInternalObjectList(processID,
					activityID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return l;
	}

	public static boolean setProcessObjectValue(String processID, int objectID,
			String value) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		boolean success = false;
		try {
			success = workflowPublicManager.setProcessObjectValue(processID,
					objectID, value);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return success;
	}

	public static List getProcessMonitorInformationList(int roleID,
			int functionType) {
		WorkflowMonitorManager workflowMonitorManager;
		workflowMonitorManager = WorkflowMonitorManager.getInstance();
		List l = null;
		try {
			l = workflowMonitorManager.getProcessMonitorInformationList(roleID,
					functionType);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return l;
	}

	public static String getProcessDocumentPath(String processID, int objectID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		String path = null;
		try {
			path = (String) workflowPublicManager.getProcessDocumentPath(
					processID, objectID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return path;
	}

	public static boolean uploadDocument(String path, byte[] content) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		boolean success = false;
		try {
			success = workflowPublicManager.uploadDocument(path, content);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return success;
	}

	public static byte[] downloadDocument(String path) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		byte[] content = null;
		try {
			content = workflowPublicManager.downloadDocument(path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return content;
	}

	// /////////////////////////////////////
	// PersonRoleDAO //
	// /////////////////////////////////////

	public static int[] getWorkflowActivityRoleIDSet(int workflowID,
			int activityID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		int[] resultSet = null;
		try {
			resultSet = workflowPublicManager.getWorkflowActivityRoleIDSet(
					workflowID, activityID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	public static int[] getProcessActivityRoleIDSet(String processID,
			int activityID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		int[] resultSet = null;
		try {
			resultSet = workflowPublicManager.getProcessActivityRoleIDSet(
					processID, activityID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	public static int[] getRoleIDSet(int personID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		int[] resultSet = null;
		try {
			resultSet = workflowPublicManager.getRoleIDSet(personID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	public static Map getRoleInformation(int roleID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		Map resultMap = null;
		try {
			resultMap = workflowPublicManager.getRoleInformation(roleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultMap;
	}

	public static List getRoleInformationList(int personID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		List resultList = null;
		try {
			resultList = workflowPublicManager.getRoleInformationList(personID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultList;
	}

	public static int[] getPersonIDSet(int roleID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		int[] resultSet = null;
		try {
			resultSet = workflowPublicManager.getPersonIDSet(roleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	public static List getPersonInformationList(int roleID) {
		WorkflowPublicManager workflowPublicManager;
		workflowPublicManager = WorkflowPublicManager.getInstance();
		List personList = null;
		try {
			personList = workflowPublicManager.getPersonInformationList(roleID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return personList;
	}

	public static List getProcessActivityList(String processID) {
		WorkflowMonitorManager workflowMonitorManager;
		workflowMonitorManager = WorkflowMonitorManager.getInstance();
		List list = null;
		try {
			list = workflowMonitorManager.getProcessActivityList(processID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public static boolean setActivityStartDate(String processID,
			int activityID, Date actualStartDate) {
		WorkflowMonitorManager workflowMonitorManager;
		workflowMonitorManager = WorkflowMonitorManager.getInstance();
		boolean success = false;
		try {
			success = workflowMonitorManager.setActivityStartDate(processID,
					activityID, actualStartDate);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return success;
	}

	public static void testReportSubmit() {
		//servercomment System.out.println(ClassLoader.getSystemResource(""));
		// initiateWorkflow, startProcess
		// personID = 139, roleID = 56
		String processID = null;
		Object[] log= null;
		int flag = -1;
		processID = instantiateWorkflow(1, 1, 1, "", 1, 1);
		// //servercomment System.out.println("The processID is " + processID);

		flag = bindingActivitytoPerson(4, processID, 3);
		// //servercomment System.out.println("The situation of the binding activity to Person 139 is "
		// + flag);

		flag = bindingActivitytoPerson(8, processID, 4);
		// //servercomment System.out.println("The situation of the binding activity to Person 4 is "
		// + flag);

		// flag = bindingActivitytoPerson(4, processID, 5);
		// //servercomment System.out.println(flag);
		log = startProcess(processID, 1, 56);
		// //servercomment System.out.println("The situation of the startProcess is " +
		// success);
		// success = acceptTask(4, processID, 4);
		// //servercomment System.out.println("The situation of the acceptTask is " + success);
		// processID = "8d841e49-4e69-4dad-bbe9-ba552fd8393e";
		flag = openActivity(processID, 3, 4);
		// //servercomment System.out.println(flag);
		flag = submitActivity(processID, 3, 4);
		// //servercomment System.out.println(flag);
		flag = openActivity(processID, 4, 8);
		// //servercomment System.out.println(flag);
		flag = submitActivity(processID, 4, 8);
		// //servercomment System.out.println(flag);
		flag = openActivity(processID, 5, 4);
		//servercomment System.out.println(flag);
		// List objectList = getInternalObjectList(processID, 6);
		// //servercomment System.out.println(((ProcessObject)objectList.get(0)).getObjectID());
	}

	public static void testDocument() {
		String processID;
		boolean success;
		List objectList;
		int flag;

		processID = instantiateWorkflow(2, 1, 1, "", 1, 12);
		bindingActivitytoPerson(8, processID, 4);
		bindingActivitytoPerson(9, processID, 5);
		startProcess(processID, 1, 12);
		acceptTask(12, processID, 3);
		openActivity(processID, 3, 12);
		objectList = getInputObjectList(processID, 3);
		submitActivity(processID, 3, 12);
		openActivity(processID, 4, 8);
		objectList = getInputObjectList(processID, 4);
		objectList = getInternalObjectList(processID, 4);

		String value;
		value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <ModificationReview xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"C:\\DOCUME~1\\Administrator\\锟斤拷锟斤拷\\WorkflowModel\\ModificationReview.xsd\"> 	<Title/> 	<Statement/> 	<IsPass>通锟斤拷</IsPass> </ModificationReview> ";
		setProcessObjectValue(processID, 1, value);

		submitActivity(processID, 4, 8);

		openActivity(processID, 5, 9);
		submitActivity(processID, 5, 9);
		bindingActivitytoPerson(12, processID, 6);
		openActivity(processID, 6, 12);
		submitActivity(processID, 6, 12);

		bindingActivitytoPerson(12, processID, 6);
		openActivity(processID, 6, 12);
		submitActivity(processID, 6, 12);

		bindingActivitytoPerson(12, processID, 7);
		openActivity(processID, 6, 12);
		submitActivity(processID, 6, 12);

	}

	public static void testApplication() {
		String processID;
		processID = instantiateWorkflow(1, 1, 1, "", 1, 12);
		String value = "<ExhibitionInfo><ITEM><ExhibitionCenterID>3</ExhibitionCenterID><Name>lala</Name><Company>haha</Company></ITEM></ExhibitionInfo>";

		setProcessObjectValue(processID, 4, value);

		// //servercomment System.out.println(processID);
		startProcess(processID, 1, 12);
	}

	public static void testWorkflow() {
		String processID;
		processID = instantiateWorkflow(12, 1, 1, "", 1, 12);
		startProcess(processID, 1, 12);
	}

	public static void testKOSTAL() {

		String processID;
		processID = instantiateWorkflow(24, 1, 1, "", 1, 1);
		startProcess(processID, 1, 1);
		acceptTask(21, processID, 3);
		openActivity(processID, 3, 21);

		setProcessObjectValue(processID, 1, "IBM"); // company
		setProcessObjectValue(processID, 2, "No.9527"); // sheet_No.
		setProcessObjectValue(processID, 3, "2008-03-24 10:00:00"); // date

		String path = getProcessDocumentPath(processID, 5);
		File f = new File(path);
		path = f.getParent();

		String newFilePath = path + "\\debit.txt";
		byte[] b = { 1, 2 };
		uploadDocument(newFilePath, b);

		byte[] c = downloadDocument(newFilePath);

		String subProcessID;
		subProcessID = instantiateWorkflow(18, 1, 1, processID, 1, 1);
		startProcess(subProcessID, 1, 1);

		int activityID = 3;
		int[] roleIDSet = getProcessActivityRoleIDSet(subProcessID, activityID);

		if (0 != roleIDSet.length) {
			int roleID = roleIDSet[0];
			List pList = getPersonInformationList(roleID);
			if (0 != pList.size()) {
				Map pMap = (Map) pList.get(0);
				int state = (Integer) pMap.get("state");
				if (0 != state) {
					int[] delegatedRoleIDSet = getDelegatedRoleIDSet(roleID);
					if (0 != delegatedRoleIDSet.length) {
						int delegatedRoleID = delegatedRoleIDSet[0];
						List delegatedPList = getPersonInformationList(delegatedRoleID);
						if (0 < delegatedPList.size()) {
							Map delegatedPMap = (Map) delegatedPList.get(0);
							int delegatedPState = (Integer) delegatedPMap
									.get("state");
							if (0 == delegatedPState) {
								int delegatedPersonID = (Integer) delegatedPMap
										.get("PersonID");
								delegate(subProcessID, activityID, roleID,
										delegatedRoleID, delegatedPersonID,
										new Date(), new Date());
							}
						}
					}
				}
			}
		}

		openActivity(subProcessID, 3, 27);
		submitActivity(subProcessID, 3, 27);

		List l = getHistoryTaskList(27);
		//servercomment System.out.println(l.size());
		for (Object m : l) {
			//servercomment System.out.println(m);
		}

		setActivityStartDate("097b513e-c85b-44d3-9f67-76395dc882d2", 3,
				new Date());

		System.exit(0);
	}

	public static void testSubKOSTAL() {
		String subProcessID;
		subProcessID = instantiateWorkflow(1, 1, 1, "", 1, 1);
		startProcess(subProcessID, 1, 1);/*
										 * //servercomment System.out.println(acceptTask(15,
										 * subProcessID, 3));
										 * //servercomment System.out.println(
										 * openActivity(subProcessID, 3, 15));
										 * System
										 * .out.println(delegate(subProcessID,
										 * 3, 29, 30, 27, new Date(), new
										 * Date()));
										 * //servercomment System.out.println(openActivity
										 * (subProcessID, 3, 15));
										 * //servercomment System.out.println
										 * (openActivity(subProcessID, 3, 27));
										 * System
										 * .out.println(submitActivity(subProcessID
										 * , 3, 27));
										 */
	}

	public static void testActECARule() {
		String processID;
		processID = instantiateWorkflow(2, 1, 1, "", 1, 1);
		startProcess(processID, 1, 1);
	}

	public static void testModel() {
		String workflowID = Constants.WORKFLOW_ID;
		if (workflowID == null || Integer.parseInt(workflowID) == 0)
			System.err.println("The workflowID in illegal!");
		else {
			String processID = instantiateWorkflow(
					Integer.parseInt(workflowID), 1, 1, "", 1, 1);
			//servercomment System.out.println("ProcessID : " + processID);
			startProcess(processID, 1, 1);
		}
	}

	public static void testModel(int workflowID) {
			String processID = instantiateWorkflow(workflowID, 1, 1, "", 1, 1);
			//servercomment System.out.println("ProcessID : " + processID);
			startProcess(processID, 1, 1);
	}

	
	public static void test_ModelProcess() {
		startProcess("5d28115c-eb0f-485c-9801-b76db1772fdf", 1, 1);
	}

	public static void main(String[] args) throws RemoteException, SQLException {

		testModel(18);

		// test_ModelProcess();

		// //////////////////////////////////////////////////////////////////////////////////////
		/*
		 * Agentsubmition output=new
		 * Agentsubmition("4e5c24de-bc4e-42e3-b23b-7df885f45e93",3);
		 * output.SubmitAgentTask("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		 * "<DebitInfoQueryResultMessageContent>"+ "<DebitInfoQueryReport>"+
		 * "<Suggestion>Approved</Suggestion>"+
		 * "<CanBeApproved>true</CanBeApproved>"+ "</DebitInfoQueryReport>"+
		 * "</DebitInfoQueryResultMessageContent>");
		 */

		// WorkflowServer.Server.bindingActivitytoPerson(1,
		// "4bbe0407-42a9-4659-b9e3-4fc6d00b3be5", 3);
		// WorkflowServer.Server.submitActivity("4bbe0407-42a9-4659-b9e3-4fc6d00b3be5",
		// 3, 1);
		// Date myDate = new Date();
		// //servercomment System.out.println(myDate.toLocaleString());

	}
}
