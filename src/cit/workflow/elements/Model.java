/*
 * Created on 2004-10-19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;

import net.jini.id.UuidFactory;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Model extends DBUtility {
	
	private static Logger logger = Logger.getLogger(Model.class);
	
	private int workflowID;
	
	private ProcessManager processManager;
	
	private String[][] initialVariables=null;
	
	public void setInitialVariables(String[][] variables){
		this.initialVariables=variables;
	}
	
	public Model(Connection conn, int workflowID, ProcessManager processManager) {
		super(conn);
		this.workflowID = workflowID;
		this.processManager = processManager;
	}
	/*
	public static String driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver"; 
	public static String url = "jdbc:microsoft:sqlserver://ww:1433;DatabaseName=Workflow";
	public static String username = "sa";
	public static String password = "";
	
	public static Connection oldConn;
	
	static {
		try {
			Class.forName(driver);
			oldConn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			//servercomment System.out.println("Can't find driver class: " + driver);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}
	
	public String instantiateOldWorkflow(int source, int caseType, String parentCaseID) {
		String processID = "";
		try {	
			String updateSQL = null;
			//1.从老数据库的WorkflowInformation中导入ProcessInformation
			PreparedStatement pstat = oldConn.prepareStatement("SELECT * FROM WorkflowInformation WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowInformation = pstat.executeQuery();
			if (rsWorkflowInformation.next()) {
				//导入ProcessInformation
				processID = UuidFactory.generate().toString();

				updateSQL = "INSERT INTO ProcessInformation(ProcessID, ProcessName, Source, CaseType, ParentCaseID, Description, MaximalLayer, State, TWCID) VALUES(?,?,?,?,?,?,?,?,?)";
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setString(2, rsWorkflowInformation.getString("WorkflowName"));
				updatePStat.setInt(3, source);
				updatePStat.setInt(4, caseType);
				updatePStat.setString(5, parentCaseID);
				updatePStat.setString(6, rsWorkflowInformation.getString("Description"));
				updatePStat.setInt(7, rsWorkflowInformation.getInt("MaximalLayer"));
				updatePStat.setString(8, Constants.PROCESS_STATE_CREATED);
				updatePStat.setInt(9, rsWorkflowInformation.getInt("TWCID"));
				updatePStat.executeUpdate();
				updatePStat.close();

				//updatePStat.executeUpdate();
				//updatePStat.close();
				
				//导入CaseInformation
				//processID = getMaxProccessID();	
				pstat.close();
			} else {
				pstat.close();
				logger.warn("There is no workflow whose WorkflowID is " + workflowID);
			}
			
			//2.从老数据库的WorkflowTWCInformation中导入ProcessTWCInformation，包括流程和活动TWC信息
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowTWCInformation WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowTWCInformation = pstat.executeQuery();
			updateSQL = "INSERT INTO ProcessTWCInformation(ProcessID, TWCID, DefinedWorkload, WorkloadDistributionType, Parameter1, Parameter2, Parameter3, ReduceRate, ExtraCost) VALUES(?,?,?,?,?,?,?,?,?)";
			while(rsWorkflowTWCInformation.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowTWCInformation.getInt("TWCID"));
				updatePStat.setInt(3, rsWorkflowTWCInformation.getInt("DefinedWorkload"));
				updatePStat.setInt(4, rsWorkflowTWCInformation.getInt("WorkloadDistributionType"));
				updatePStat.setFloat(5, rsWorkflowTWCInformation.getFloat("Parameter1"));
				updatePStat.setFloat(6, rsWorkflowTWCInformation.getFloat("Parameter2"));
				updatePStat.setFloat(7, rsWorkflowTWCInformation.getFloat("Parameter3"));
				updatePStat.setFloat(8, rsWorkflowTWCInformation.getFloat("ReduceRate"));
				updatePStat.setInt(9, rsWorkflowTWCInformation.getInt("DefinedCost"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//3.从老数据库的WorkflowActivityInformation中导入ProcessActivityInformation
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowActivityInformation WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowActivityInformation = pstat.executeQuery();
			updateSQL = "INSERT INTO ProcessActivityInformation(ProcessID, ActivityID, ActivityName, Layer, ParentID, Description, ChoiceManager, ActivityType, ActivityImplementation, StartTime, Duration, IconID, ActivityURL, MultiPersonMode, SubmitPersonNumber, RepeatedTime, TWCID, State) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			while(rsWorkflowActivityInformation.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowActivityInformation.getInt("ActivityID"));
				updatePStat.setString(3, rsWorkflowActivityInformation.getString("ActivityName"));
				updatePStat.setInt(4, rsWorkflowActivityInformation.getInt("Layer"));
				updatePStat.setInt(5, rsWorkflowActivityInformation.getInt("ParentID"));
				updatePStat.setString(6, rsWorkflowActivityInformation.getString("Description"));
				updatePStat.setBoolean(7, rsWorkflowActivityInformation.getBoolean("ChoiceManager"));
				updatePStat.setInt(8, rsWorkflowActivityInformation.getInt("ActivityType"));
				updatePStat.setInt(9, 1);
				updatePStat.setDate(10, processManager.getCurrentDate());
				updatePStat.setInt(11, rsWorkflowActivityInformation.getInt("Duration"));
				updatePStat.setInt(12, 1);
				updatePStat.setString(13, "");
				updatePStat.setInt(14, rsWorkflowActivityInformation.getInt("MultiPersonMode"));
				updatePStat.setInt(15, 0);						//该活动已提交人数
				updatePStat.setInt(16, 0);						//该活动重复次数
				updatePStat.setInt(17, rsWorkflowActivityInformation.getInt("TWCID"));
				updatePStat.setString(18, Constants.ACTIVITY_STATE_WAITING);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//4.从老数据库的WorkflowActivityRole中导入ProcessActivityRole
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowActivityRole WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowActivityRole = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessActivityRole(ProcessID, ActivityID, RoleID, MinimalNumber, MaximalNumber, MinimalSubmittedPerson, WorkloadRatio, AllocatedNumber) VALUES(?,?,?,?,?,?,?,?)";
			while(rsWorkflowActivityRole.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowActivityRole.getInt("ActivityID"));
				updatePStat.setInt(3, rsWorkflowActivityRole.getInt("RoleID"));
				updatePStat.setInt(4, rsWorkflowActivityRole.getInt("MinimalNumber"));
				updatePStat.setInt(5, rsWorkflowActivityRole.getInt("MaximalNumber"));
				//updatePStat.setInt(6, rsWorkflowActivityRole.getInt("MinimalSubmittedPerson"));
				updatePStat.setInt(6, 1);
				//updatePStat.setFloat(7, rsWorkflowActivityRole.getFloat("WorkloadRatio"));
				updatePStat.setFloat(7, 1.0f);
				updatePStat.setInt(8, 0);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
	
			//5.从老数据库的WorkflowObject中导入ProcessObject
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowObject WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowObject = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessObject(ProcessID, ObjectID, ISInput, ISOutput, ISVisible, ObjectType, Scope, PackageObjectID, State) VALUES(?,?,?,?,?,?,?,?,?)";
			while(rsWorkflowObject.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowObject.getInt("ObjectID"));
				
				//updatePStat.setBoolean(3, rsWorkflowObject.getBoolean("ISInput"));
				//updatePStat.setBoolean(4, rsWorkflowObject.getBoolean("ISOutput"));
				//updatePStat.setBoolean(5, rsWorkflowObject.getBoolean("ISVisible"));
				
				updatePStat.setBoolean(3, true);
				updatePStat.setBoolean(4, true);
				updatePStat.setBoolean(5, true);
				
				updatePStat.setInt(6, 1);
				//updatePStat.setInt(7, rsWorkflowObject.getInt("Scope"));
				updatePStat.setInt(7, 1);
				//updatePStat.setInt(8, rsWorkflowObject.getInt("PackageObjectID"));
				updatePStat.setInt(8, 1);
				updatePStat.setInt(9, Constants.OBJECT_STATE_NOTCREATED);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//6.从老数据库的WorkflowFlowObjects中导入ProcessFlowObjects
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowFlowObjects WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowFlowObjects = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessFlowObjects(ProcessID, FlowID, EventID, FromActivityID, ToActivityID) VALUES(?,?,?,?,?)";
			while(rsWorkflowFlowObjects.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowFlowObjects.getInt("FlowID"));
				//updatePStat.setInt(3, rsWorkflowFlowObjects.getInt("EventID"));
				updatePStat.setInt(3, 1);
				updatePStat.setInt(4, rsWorkflowFlowObjects.getInt("FromActivityID"));
				updatePStat.setInt(5, rsWorkflowFlowObjects.getInt("ToActivityID"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//7.从老数据库的WorkflowFlowObjectControl中导入ProcessFlowObjectControl
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowFlowObjectControl WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowFlowObjectControl = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessFlowObjectControl(ProcessID, FlowID, ObjectID, Privilege, State) VALUES(?,?,?,?,?)";
			while(rsWorkflowFlowObjectControl.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowFlowObjectControl.getInt("FlowID"));
				updatePStat.setInt(3, rsWorkflowFlowObjectControl.getInt("ObjectID"));
				updatePStat.setInt(4, rsWorkflowFlowObjectControl.getInt("Right"));
				updatePStat.setString(5, Constants.DATAFLOW_STATE_INACTIVE);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();*/
			
			/***************************************************************************************
				活动相关信息包括：ProcessEvent, ProcessEventRelation, ProcessEventRelationforParse, ProcessCondition, ProcessProcessECARule, ProcessActivityECARule
				
				具体步骤：
					1.把相应的数据从WorkflowEvent导入到ProcessEvent.
					2.把相应的数据从WorkflowEventRelation导入到ProcessEventRelation和ProcessEventRelationforParse.
					3.把相应的数据从WorkflowCondition导入到ProcessCondition.
					4.把相应的数据从WorkflowProcessECARule导入到ProcessProcessECARule.
					5.把相应的数据从WorkflowActivityECARule导入到ProcessActivityECARule.
			*****************************************************************************************/
			//8.从老数据库的WorkflowEvent中导入ProcessEvent
			/*
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowEvent WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowEvent = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessEvent(ProcessID, EventID, EventName, Description, EventRepresentation, ExpressionForParse, EventType, LogicType, ActivityID, RepeatedTime) VALUES(?,?,?,?,?,?,?,?,?,?)";
			while(rsWorkflowEvent.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowEvent.getInt("EventID"));
				updatePStat.setString(3, rsWorkflowEvent.getString("EventName"));
				updatePStat.setString(4, rsWorkflowEvent.getString("Description"));
				updatePStat.setString(5, rsWorkflowEvent.getString("EventRepresentation"));
				updatePStat.setString(6, rsWorkflowEvent.getString("EventRepresentation"));
				updatePStat.setInt(7, rsWorkflowEvent.getInt("EventType"));
				updatePStat.setInt(8, rsWorkflowEvent.getInt("LogicType"));
				updatePStat.setInt(9, rsWorkflowEvent.getInt("ActivityID"));
				updatePStat.setInt(10, 0);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//9.从老数据库的WorkflowEventRelation中导入ProcessEventRelation和ProcessEventRelationforParse
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowEventRelation WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowEventRelation = pstat.executeQuery();	
			while(rsWorkflowEventRelation.next()) {
				PreparedStatement updatePStat = conn.prepareStatement("INSERT INTO ProcessEventRelation(ProcessID, ParentEventID, ChildEventID) VALUES(?,?,?)");
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowEventRelation.getInt("FatherEventID"));
				updatePStat.setInt(3, rsWorkflowEventRelation.getInt("SonEventID"));
				updatePStat.executeUpdate();
				updatePStat.close();
				
				updatePStat = conn.prepareStatement("INSERT INTO ProcessEventRelationforParse(ProcessID, FatherEventID, SonEventID) VALUES(?,?,?)");
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowEventRelation.getInt("FatherEventID"));
				updatePStat.setInt(3, rsWorkflowEventRelation.getInt("SonEventID"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//10.从老数据库的WorkflowCondition中导入ProcessCondition
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowCondition WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowCondition = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessCondition(ProcessID, ConditionID, ConditionRepresentation) VALUES(?,?,?)";
			while(rsWorkflowCondition.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowCondition.getInt("ConditionID"));
				updatePStat.setString(3, rsWorkflowCondition.getString("ConditionRepresentation"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
			//11.从老数据库的WorkflowProcessECARule中导入ProcessProcessECARule
			pstat = oldConn.prepareStatement("SELECT * FROM WorkflowProcessECARule  WHERE WorkflowID=?");
			pstat.setInt(1, this.workflowID);
			ResultSet rsWorkflowProcessECARule  = pstat.executeQuery();	
			updateSQL = "INSERT INTO ProcessProcessECARule(ProcessID, RuleID, EventID, ConditionID, ActionExpression , Probability , ProbabilityCoefficient, RepeatedTime) VALUES(?,?,?,?,?,?,?,?)";
			while(rsWorkflowProcessECARule.next()) {
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, rsWorkflowProcessECARule.getInt("RuleID"));
				updatePStat.setInt(3, rsWorkflowProcessECARule.getInt("EventID"));
				updatePStat.setInt(4, rsWorkflowProcessECARule.getInt("ConditionID"));
				updatePStat.setString(5, rsWorkflowProcessECARule.getString("ActionExpression"));
				updatePStat.setFloat(6, rsWorkflowProcessECARule.getFloat("Probability"));
				updatePStat.setFloat(7, rsWorkflowProcessECARule.getFloat("ProbabilityCoefficient"));
				updatePStat.setInt(8, 0);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			pstat.close();
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			processID = "";
		}
		return processID;
	}
	*/
	
	
	/**
	 * 功能：实例化工作流模型
	 * 
	 * @param workflowID
	 * @param source
	 * @param caseType
	 * @param parentCaseID
	 * @param initiatorID
	 * @return
	 * 
	 * 步骤：
	 * 		1.导入流程信息
	 * 		2.导入流程相关的信息
	 * 		3.导入活动相关的信息
	 * 		4.导入流程对象和数据流信息
	 * 		5.导入ECA相关的信息
	 */
	public String instantiateWorkflow(int source, int caseType, String parentCaseID) {
		//servercomment System.out.println("InstantiateWorkflow started!");
		String processID = "";
		String packageName = "";
		try {
			String updateSQL = null;

			//sxh add 2007.11
			/**************************************************************************************
			 一些共享信息，包括：WorkflowInformation, PackageInformation
			 **************************************************************************************/
			//workflowInformation
			sql = "SELECT * from WorkflowInformation WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			Map workflowInformationMap = (Map) executeQuery(new MapHandler());
			//packageInformation
			Map packageInformationMap = null;
			if (workflowInformationMap != null) {
				sql = "SELECT * from PackageInformation WHERE packageID=?";
				params = new Object[] {(Integer)workflowInformationMap.get("packageID")};
				types = new int[] {Types.INTEGER};
				packageInformationMap = (Map) executeQuery(new MapHandler());
			}
			//sxh add 2007.11 end
			
			
			//1.导入流程信息
			/***************************************************************************************
				流程信息包括：ProcessInformation, CaseInformation, ProcessTWCInformation
				具体步骤：
					1.把WorkflowInformation里面相关的信息写入ProcessInformation和CaseInformation.
					2.把WorkflowTWCInformation里面相关的信息写入ProcessTWCInformation.
			*****************************************************************************************/
			sql = "SELECT WorkflowInformation.*, PI.DurationUnit, PI.CostUnit FROM WorkflowInformation LEFT JOIN PackageInformation PI ON PI.PackageID=WorkflowInformation.PackageID WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			Map wiMap = (Map) executeQuery(new MapHandler());
			if (wiMap != null) {
				//生成processID
				processID = UuidFactory.generate().toString();
				
				//导入ProcessInformation
				updateSQL = "INSERT INTO ProcessInformation(ProcessID, ProcessName, Source, CaseType, ParentCaseID, Description, MaximalLayer, Persistent, State, TWCID, DurationUnit, CostUnit) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setString(2, (String) wiMap.get("WorkflowName"));
				updatePStat.setInt(3, source);
				updatePStat.setInt(4, caseType);
				updatePStat.setString(5, parentCaseID);
				updatePStat.setString(6, (String) wiMap.get("Description"));
				updatePStat.setInt(7, ((Integer) wiMap.get("MaximalLayer")).intValue());
				updatePStat.setBoolean(8, ((Boolean) wiMap.get("Persistent").equals(1)));
				updatePStat.setString(9, Constants.PROCESS_STATE_CREATED);
				updatePStat.setInt(10, ((Integer) wiMap.get("TWCID")).intValue());
				updatePStat.setString(11, (String) wiMap.get("DurationUnit"));
				updatePStat.setFloat(12, ((Float) wiMap.get("CostUnit")).floatValue());
				updatePStat.executeUpdate();
				updatePStat.close();
				
				//导入CaseInformation
				//processID = getMaxProccessID();
				updateSQL = "INSERT INTO CaseInformation(ProcessID, CaseName, WorkflowID, InitiatorID, InitiatorDate, KeyWords) VALUES(?,?,?,?,?,?)";
				updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setString(2, (String) wiMap.get("WorkflowName"));
				updatePStat.setInt(3, this.workflowID);
				updatePStat.setInt(4, processManager.getCurrentUser().getActorID());
				updatePStat.setDate(5, processManager.getCurrentDate());
				updatePStat.setString(6, " ");
				updatePStat.executeUpdate();
				updatePStat.close();
			} else {
				logger.warn("There is no workflow whose WorkflowID is " + workflowID);
				return "";
			}
			/**
			 * get the package imformation 
			 * add by daiyi
			 * 2006.4.24
			 */
			if(wiMap != null){
				int packageID = ((Integer)wiMap.get("PackageID")).intValue();
				sql = "SELECT * FROM PackageInformation where packageID =?";
				params = new Object[]{new Integer(packageID)};
				types = new int[]{Types.INTEGER};
				Map packageMap = (Map)executeQuery(new MapHandler());
				if(packageMap != null){
					packageName = packageMap.get("PackageName").toString();
				}else {
					logger.warn("There is no such a pachage");
					return "";
				}
			}
			
			
			//导入ProcessTWCInformation
			sql = "SELECT * FROM WorkflowTWCInformation WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wtwciList = (List) executeQuery(new MapListHandler());
			
			Iterator wtwciIterator = wtwciList.iterator();
			updateSQL = "INSERT INTO ProcessTWCInformation(ProcessID, TWCID, DefinedWorkload, WorkloadDistributionType, Parameter1, Parameter2, Parameter3, ReduceRate, ExtraCost, CompletionRate, DefinedStartDate, ActualStartDate, DefinedEndDate, ActualEndDate, ActualWorkload, ActualCost) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			while(wtwciIterator.hasNext()) {
				Map wtwcMap = (Map) wtwciIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wtwcMap.get("TWCID")).intValue());
				updatePStat.setInt(3, ((Integer) wtwcMap.get("DefinedWorkload")).intValue());
//				if(wtwcMap.get("WorkloadDistributionType") != null)
				updatePStat.setInt(4, ((Integer) wtwcMap.get("WorkloadDistributionType")).intValue());
				updatePStat.setFloat(5, ((Float) wtwcMap.get("Parameter1")).floatValue());
				updatePStat.setFloat(6, ((Float) wtwcMap.get("Parameter2")).floatValue());
				updatePStat.setFloat(7, ((Float) wtwcMap.get("Parameter3")).floatValue());
				updatePStat.setFloat(8, ((Float) wtwcMap.get("ReduceRate")).floatValue());
				updatePStat.setInt(9, ((Integer) wtwcMap.get("ExtraCost")).intValue());
				
				updatePStat.setFloat(10, 0.0f);
				//sxh modified 2007.11
				updatePStat.setDate(11, null);
				updatePStat.setDate(12, null);
				updatePStat.setDate(13, null);
				updatePStat.setDate(14, null);
				//sxh modifed 2007.11 end
				updatePStat.setInt(15, 0);
				updatePStat.setInt(16, 0);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//2.导入流程相关的信息
			/***************************************************************************************
				流程相关信息包括：ProcessResponsibleRoles, ProcessResource, ProcessParticipant, ProcessApplication
				具体步骤：
					1.把相应的数据从WorkflowResponsibleRoles导入到ProcessResponsibleRoles.
					2.把相应的数据从WorkflowResource导入到ProcessResource.
					3.把相应的数据从WorkflowParticipant导入到ProcessParticipant.
					4.把相应的数据从WorkflowApplication导入到ProcessApplication.
			*****************************************************************************************/
			
			//导入ProcessResponsibleRoles
			sql = "SELECT * FROM WorkflowResponsibleRoles WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wrrList = (List) executeQuery(new MapListHandler());
			
			Iterator wrrIterator = wrrList.iterator();
			updateSQL = "INSERT INTO ProcessResponsibleRoles(ProcessID, FunctionType, RoleID) VALUES(?,?,?)";
			while(wrrIterator.hasNext()) {
				Map wrrMap = (Map) wrrIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wrrMap.get("FunctionType")).intValue());
				updatePStat.setInt(3, ((Integer) wrrMap.get("RoleID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessResource
			sql = "SELECT * FROM WorkflowResource WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wrList = (List) executeQuery(new MapListHandler());
			Iterator wrIterator = wrList.iterator();

			updateSQL = "INSERT INTO ProcessResource(ProcessID, ResourceID) VALUES(?,?)";
			while(wrIterator.hasNext()) {
				Map wrMap = (Map) wrIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wrMap.get("ResourceID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessParticipant
			sql = "SELECT * FROM WorkflowParticipant WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wpList = (List) executeQuery(new MapListHandler());
			Iterator wpIterator = wpList.iterator();

			updateSQL = "INSERT INTO ProcessParticipant(ProcessID, ActorType, ActorID) VALUES(?,?,?)";
			while(wpIterator.hasNext()) {
				Map wpMap = (Map) wpIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wpMap.get("ActorType")).intValue());
				updatePStat.setInt(3, ((Integer) wpMap.get("ActorID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessApplication
			sql = "SELECT * FROM WorkflowApplication WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waList = (List) executeQuery(new MapListHandler());
			Iterator waIterator = waList.iterator();

			updateSQL = "INSERT INTO ProcessApplication(ProcessID, ApplicationID,Description) VALUES(?,?,?)";
			while(waIterator.hasNext()) {
				Map waMap = (Map) waIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waMap.get("ApplicationID")).intValue());
				updatePStat.setString(3, (String)waMap.get("Description"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//3.导入活动相关的信息
			/***************************************************************************************
				活动相关信息包括：ProcessActivityInformation,ProcessActivityTeam, ProcessActivityManager,
					ProcessActivityRole,ProcessActivityPerson,ProcessActivityResource,ProcessActivityAgent,
					ProcessActivityAgency,ProcessActivityInvokingApplication,ProcessActivityInvokingWorkflow,
					ProcessActivityInvokingProcess,ProcessActivitySchedule,ProcessActivitySetValue,
					ProcessActivityTransformXML
				具体步骤：
					1.把相应的数据从WorkflowActivityInformation导入到ProcessActivityInformation.
					2.把相应的数据从WorkflowActivityRole导入到ProcessActivityRole.
					3.把相应的数据从WorkflowActivityTeam导入到ProcessActivityTeam.
					4.把相应的数据从WorkflowActivityResource导入到ProcessActivityResource.
					
					5.把相应的数据从WorkflowActivityAgent导入到ProcessActivityAgent.
					6.把相应的数据从WorkflowActivityAgency导入到ProcessActivityAgency.
					7.把相应的数据从WorkflowActivityInvokingApplication导入到ProcessActivityInvokingApplication.
					8.把相应的数据从WorkflowActivityInvokingWorkflow导入到ProcessActivityInvokingWorkflow.
					
					9.把相应的数据从WorkflowActivitySchedule导入到ProcessActivitySchedule.
					10.把相应的数据从WorkflowActivitySetValue导入到ProcessActivitySetValue.
					11.把相应的数据从WorkflowActivityTransformXML导入到ProcessActivityTransformXML.
					12.把相应的数据从WorkflowActivityInputMapping导入到ProcessActivityInputMapping
					13.把相应的数据从WorkflowActivityOutputMapping导入到ProcessActivityOutMapping
					
					14.把相应的数据从WorkflowActivityPosition导入到ProcessActivityPosition

				问题：
					1.ProcessActivityManager还没有导入
					2.ProcessActivityPerson还没有导入
					3.ProcessActivityInvokingProcess还没有导入
			*****************************************************************************************/
			
			//sxh modified 2007.11
			//导入ProcessActivityInformation，同时导入活动TWC到ProcessTWCInformation
			sql = "SELECT * FROM WorkflowActivityInformation WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waiList = (List) executeQuery(new MapListHandler());
			Iterator waiIterator = waiList.iterator();

			String durationUnit = (String)packageInformationMap.get("durationUnit");
			
			while(waiIterator.hasNext()) {
				Map waiMap = (Map) waiIterator.next();
				updateSQL = "INSERT INTO ProcessActivityInformation(ProcessID, ActivityID, ActivityName, Layer, ParentID, Description, IsCallBack, ChoiceManager, ActivityType, ActivityImplementation, StartTime, Duration, IconID, ActivityURL, MultiPersonMode, SubmitPersonNumber, TWCID, State, RepeatedTime) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waiMap.get("ActivityID")).intValue());
				updatePStat.setString(3, (String) waiMap.get("ActivityName"));
				updatePStat.setInt(4, ((Integer) waiMap.get("Layer")).intValue());
				updatePStat.setInt(5, ((Integer) waiMap.get("ParentID")).intValue());
				updatePStat.setString(6, (String) waiMap.get("Description"));
				//sxh add 2007.12
				if (waiMap.get("IsCallBack") != null) {
					updatePStat.setBoolean(7, (Boolean) waiMap.get("IsCallBack").equals(1));
				}
				else {
					updatePStat.setBoolean(7, false);
				}
				//sxh add 2007.12 end
				updatePStat.setBoolean(8, ((Boolean) waiMap.get("ChoiceManager").equals(1)));
				updatePStat.setInt(9, ((Integer) waiMap.get("ActivityType")).intValue());
				updatePStat.setInt(10, ((Integer) waiMap.get("ActivityImplementation")).intValue());
				updatePStat.setFloat(11, ((Float) waiMap.get("StartTime")));
				updatePStat.setFloat(12, ((Float) waiMap.get("Duration")));
				updatePStat.setInt(13, ((Integer) waiMap.get("IconID")).intValue());
				updatePStat.setString(14, (String) waiMap.get("ActivityURL"));
				updatePStat.setInt(15, ((Integer) waiMap.get("MultiPersonMode")).intValue());
				updatePStat.setInt(16, ((Integer) waiMap.get("SubmitPersonNumber")).intValue());
				updatePStat.setInt(17, ((Integer) waiMap.get("TWCID")).intValue());
				updatePStat.setString(18, Constants.ACTIVITY_STATE_WAITING);
				updatePStat.setInt(19, 0);
				updatePStat.executeUpdate();
				
				
				long definedStartTimeMillis = System.currentTimeMillis();
				long definedEndTimeMillis = System.currentTimeMillis();
				if (durationUnit.equalsIgnoreCase("d")) {
					definedStartTimeMillis += (int)(((Float) waiMap.get("StartTime")) * 24 * 60 * 60 * 1000);
					definedEndTimeMillis += (int)(((Float) waiMap.get("StartTime") + ((Float)waiMap.get("Duration"))) * 24 * 60 * 60 * 1000);
				} else {
					//To be added;
				}
				updateSQL = "update processTWCInformation set DefinedStartDate = ?, DefinedEndDate = ? where processid = ? and twcid = ?";
				updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setDate(1, new java.sql.Date(definedStartTimeMillis));
				updatePStat.setDate(2, new java.sql.Date(definedEndTimeMillis));
				updatePStat.setTimestamp(1, null);
				updatePStat.setTimestamp(2, null);
				updatePStat.setString(3, processID);
				updatePStat.setInt(4, ((Integer) waiMap.get("TWCID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			//sxh modified 2007.11 end
			
			
			
			//导入ProcessActivityRole
			sql = "SELECT * FROM WorkflowActivityRole WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List warList = (List) executeQuery(new MapListHandler());
			Iterator warIterator = warList.iterator();
			
			updateSQL = "INSERT INTO ProcessActivityRole(ProcessID, ActivityID, RoleID, MinimalNumber, MaximalNumber, MinimalSubmittedPerson, WorkloadRatio, AllocatedNumber) VALUES(?,?,?,?,?,?,?,?)";
			while(warIterator.hasNext()) {
				Map warMap = (Map) warIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) warMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) warMap.get("RoleID")).intValue());
				updatePStat.setInt(4, ((Integer) warMap.get("MinimalNumber")).intValue());
				updatePStat.setInt(5, ((Integer) warMap.get("MaximalNumber")).intValue());
				updatePStat.setInt(6, ((Integer) warMap.get("MinimalSubmittedPerson")).intValue());
				updatePStat.setFloat(7, ((Float) warMap.get("WorkloadRatio")).floatValue());
				updatePStat.setInt(8, 0);
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivityTeam
			sql = "SELECT * FROM WorkflowActivityTeam WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List watList = (List) executeQuery(new MapListHandler());
			Iterator watIterator = watList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityTeam(ProcessID, ActivityID, ActorType, ActorID, IsLeader) VALUES(?,?,?,?,?)";
			while(watIterator.hasNext()) {
				Map watMap = (Map) watIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) watMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) watMap.get("ActorType")).intValue());
				updatePStat.setInt(4, ((Integer) watMap.get("ActorID")).intValue());
				updatePStat.setBoolean(5, ((Boolean) watMap.get("IsLeader")).booleanValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivityResource
			// TODO ADD IMPORT ProcessActivityResource Code
			
			//cxz add on 2009.03.10
			//导入ProcessActivityAgent
			sql = "SELECT * FROM WorkflowActivityAgent WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List AgentList = (List) executeQuery(new MapListHandler());
			Iterator AgentIterator = AgentList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityAgent(ProcessID, ActivityID, AgentID, CapabilityID) VALUES(?,?,?,?)";
			
		
			while(AgentIterator.hasNext()) {
				Map AgentMap = (Map) AgentIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) AgentMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) AgentMap.get("AgentID")).intValue());
				updatePStat.setInt(4, ((Integer) AgentMap.get("CapabilityID")).intValue());
				
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			// cxz add end
			
			
			
			//导入ProcessActivityAgency
			// TODO ADD IMPORT ProcessActivityResource Code
			
			
			//导入ProcessActivityInvokingApplication
			sql = "SELECT * FROM WorkflowActivityInvokingApplication WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waiaList = (List) executeQuery(new MapListHandler());
			Iterator waiaIterator = waiaList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityInvokingApplication(ProcessID, ActivityID, ApplicationID, InvocationType, InputXMLID, OutputXMLID, ActualStartDate, ActualEndDate, Result) VALUES(?,?,?,?,?,?,?,?,?)";
			while(waiaIterator.hasNext()) {
				Map waiaMap = (Map) waiaIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waiaMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waiaMap.get("ApplicationID")).intValue());
				updatePStat.setInt(4, ((Integer) waiaMap.get("InvocationType")).intValue());
				updatePStat.setInt(5, ((Integer) waiaMap.get("InputSchemaID")).intValue());
				updatePStat.setInt(6, ((Integer) waiaMap.get("OutputSchemaID")).intValue());
				updatePStat.setDate(7, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.setDate(8, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.setString(9, " ");
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivityInvokingWorkflow
			sql = "SELECT * FROM WorkflowActivityInvokingWorkflow WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waiwList = (List) executeQuery(new MapListHandler());
			Iterator waiwIterator = waiwList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityInvokingWorkflow(ProcessID, ActivityID, InvokedWorkflowID, InvocationType) VALUES(?,?,?,?)";
			while(waiwIterator.hasNext()) {
				Map waiwMap = (Map) waiwIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waiwMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waiwMap.get("InvokedWorkflowID")).intValue());
				updatePStat.setInt(4, ((Integer) waiwMap.get("InvocationType")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivitySchedule
			sql = "SELECT * FROM WorkflowActivitySchedule WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wasList = (List) executeQuery(new MapListHandler());
			Iterator wasIterator = wasList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivitySchedule(ProcessID, ActivityID, Duration, DurationUnit) VALUES(?,?,?,?)";
			while(wasIterator.hasNext()) {
				Map wasMap = (Map) wasIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wasMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) wasMap.get("Duration")).intValue());
				updatePStat.setString(4, (String) wasMap.get("DurationUnit"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivitySetValue
			sql = "SELECT * FROM WorkflowActivitySetValue WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wasvList = (List) executeQuery(new MapListHandler());
			Iterator wasvIterator = wasvList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivitySetValue(ProcessID, ActivityID, ObjectID, Expression, ExpressionName) VALUES(?,?,?,?,?)";
			while(wasvIterator.hasNext()) {
				Map wasvMap = (Map) wasvIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wasvMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) wasvMap.get("ObjectID")).intValue());
				updatePStat.setString(4, (String) wasvMap.get("Expression"));
				updatePStat.setString(5, (String) wasvMap.get("ExpressionName"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivityTransformXML
			sql = "SELECT * FROM WorkflowActivityTransformXML WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List watxmlList = (List) executeQuery(new MapListHandler());
			Iterator watxmlIterator = watxmlList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityTransformXML(ProcessID, ActivityID, SourceObjectID1, TargetObjectID2, XSLT) VALUES(?,?,?,?,?)";
			while(watxmlIterator.hasNext()) {
				Map watxmlMap = (Map) watxmlIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) watxmlMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) watxmlMap.get("SourceObjectID1")).intValue());
				updatePStat.setInt(4, ((Integer) watxmlMap.get("TargetObjectID2")).intValue());
				updatePStat.setString(5, (String) watxmlMap.get("XSLT"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			
			//导入ProcessActivityInputMapping
			sql = "SELECT * FROM WorkflowActivityInputMapping WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waimList = (List) executeQuery(new MapListHandler());
			Iterator waimIterator = waimList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityInputMapping(ProcessID, ActivityID, DataflowID, InputSchema, InputXML, XSLT) VALUES(?,?,?,?,?,?)";
			while(waimIterator.hasNext()) {
				Map waimMap = (Map) waimIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waimMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waimMap.get("DataflowID")));
				updatePStat.setString(4, ((String) waimMap.get("InputSchema")));
				updatePStat.setString(5, ((String) waimMap.get("InputXML")));
				updatePStat.setString(6, (String) waimMap.get("XSLT"));
				updatePStat.executeUpdate();
				updatePStat.close();
				
			}
			
			//导入ProcessActivityOutputMapping
			sql = "SELECT * FROM WorkflowActivityOutputMapping WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waomList = (List) executeQuery(new MapListHandler());
			Iterator waomIterator = waomList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityOutputMapping(ProcessID, ActivityID, ObjectID, XSLT) VALUES(?,?,?,?)";
			while(waomIterator.hasNext()) {
				Map waomMap = (Map) waomIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waomMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waomMap.get("ObjectID")).intValue());
				updatePStat.setString(4, (String) waomMap.get("XSLT"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			
			//导入ProcessActivityPosition
			sql = "SELECT * FROM WorkflowActivityPosition WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wapList = (List) executeQuery(new MapListHandler());
			Iterator wapIterator = wapList.iterator();
	
			updateSQL = "INSERT INTO ProcessActivityPosition VALUES(?,?,?,?,?,?)";
			while(wapIterator.hasNext()) {
				Map wapMap = (Map) wapIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setInt(1, ((Integer) wapMap.get("ModelType")).intValue());
				updatePStat.setString(2, processID);
				updatePStat.setInt(3, ((Integer) wapMap.get("ActivityID")).intValue());
				updatePStat.setInt(4, ((Integer) wapMap.get("XPosition")).intValue());
				updatePStat.setInt(5, (Integer) wapMap.get("YPosition"));
				updatePStat.setInt(6, (Integer) wapMap.get("IconID"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			
			
			//4.导入流程对象和数据流信息
			/***************************************************************************************
				活动相关信息包括：ProcessObject, ProcessInherentVariable, ProcessObjectVariable,
					ProcessXMLDocument, ProcessDocument, ProcessFlowObjects, ProcessFlowObjectControl
				具体步骤：
					1.把相应的数据从WorkflowObject导入到ProcessObject.
					2.把相应的数据从WorkflowInherentVariable导入到ProcessInherentVariable.
					3.把相应的数据从WorkflowObjectVariable导入到ProcessObjectVariable.
					4.把相应的数据从WorkflowXMLDocument导入到ProcessXMLDocument.把相应的数据从WorkflowXmlObjTemplate导入到ProcessXmlObjTemplate
					
					5.把相应的数据从WorkflowReferenceVariable导入到ProcessReferenceVariable.
					
					6.把相应的数据从WorkflowDocument导入到ProcessDocument.
					7.把相应的数据从WorkflowFlowObjects导入到 ProcessFlowObjects.
					8.把相应的数据从WorkflowFlowObjectControl导入到ProcessFlowObjectControl.
			*****************************************************************************************/
			//导入ProcessObject
			sql = "SELECT * FROM WorkflowObject WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List woList = (List) executeQuery(new MapListHandler());
			Iterator woIterator = woList.iterator();

			updateSQL = "INSERT INTO ProcessObject(ProcessID, ObjectID, ISInput, ISOutput, ISVisible, ObjectType, Scope, PackageObjectID, State, ObjectName) VALUES(?,?,?,?,?,?,?,?,?,?)";
			while(woIterator.hasNext()) {
				Map woMap = (Map) woIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) woMap.get("ObjectID")).intValue());
				updatePStat.setBoolean(3, ((Boolean) woMap.get("ISInput").equals(1)));
				updatePStat.setBoolean(4, ((Boolean) woMap.get("ISOutput").equals(1)));
				updatePStat.setBoolean(5, ((Boolean) woMap.get("ISVisible").equals(1)));
				updatePStat.setInt(6, ((Integer) woMap.get("ObjectType")).intValue());
				updatePStat.setInt(7, ((Integer) woMap.get("Scope")).intValue());
				updatePStat.setInt(8, ((Integer) woMap.get("PackageObjectID")).intValue());
				updatePStat.setInt(9, Constants.OBJECT_STATE_NOTCREATED);
				updatePStat.setString(10, (String) woMap.get("ObjectName"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessInherentVariable
			sql = "SELECT * FROM WorkflowInherentVariable WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wivList = (List) executeQuery(new MapListHandler());
			Iterator wivIterator = wivList.iterator();
	
			updateSQL = "INSERT INTO ProcessInherentVariable(ProcessID, ObjectID, ObjectName, Description, ValueType, InitialValue, Value) VALUES(?,?,?,?,?,?,?)";
			while(wivIterator.hasNext()) {
				Map wivMap = (Map) wivIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wivMap.get("ObjectID")).intValue());
				updatePStat.setString(3, (String) wivMap.get("ObjectName"));
				updatePStat.setString(4, (String) wivMap.get("Description"));
				updatePStat.setInt(5, ((Integer) wivMap.get("ValueType")).intValue());
				logger.debug((String) wivMap.get("InitialValue"));
				updatePStat.setString(6, (String) wivMap.get("InitialValue"));
				//sxh add 2007.11
				updatePStat.setString(7, (String) wivMap.get("InitialValue"));
				//sxh add 2007.11 end
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			if (initialVariables != null) {
				updateSQL = "UPDATE ProcessInherentVariable SET Value = ? WHERE ProcessID = ? AND ObjectID = ? ";
				for (String[] rowData : initialVariables) {
					PreparedStatement updatePStat = conn
							.prepareStatement(updateSQL);
					updatePStat.setString(1, rowData[1]);
					updatePStat.setString(2, processID);
					updatePStat.setInt(3, Integer.parseInt(rowData[0]));
					updatePStat.executeUpdate();
					updatePStat.close();

				}
			}
			//导入ProcessObjectVariable
			sql = "SELECT * FROM WorkflowObjectVariable WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wovList = (List) executeQuery(new MapListHandler());
			Iterator wovIterator = wovList.iterator();
	
			updateSQL = "INSERT INTO ProcessObjectVariable(ProcessID, ObjectID, ObjectName, Description, ValueType, FromObjectID, XPath) VALUES(?,?,?,?,?,?,?)";
			while(wovIterator.hasNext()) {
				Map wovMap = (Map) wovIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wovMap.get("ObjectID")).intValue());
				updatePStat.setString(3, (String) wovMap.get("ObjectName"));
				updatePStat.setString(4, (String) wovMap.get("Description"));
				updatePStat.setInt(5, ((Integer) wovMap.get("ValueType")).intValue());
				updatePStat.setInt(6, ((Integer) wovMap.get("FromObjectID")).intValue());
				updatePStat.setString(7, (String) wovMap.get("XPath"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//cyd add
			//导入ProcessArrayVariable
			sql = "SELECT * FROM WorkflowArrayVariable WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wavList = (List) executeQuery(new MapListHandler());
			Iterator wavIterator = wavList.iterator();
	
			updateSQL = "INSERT INTO ProcessArrayVariable(ProcessID, ObjectID, ObjectName) VALUES(?,?,?)";
			while(wavIterator.hasNext()) {
				Map wavMap = (Map) wavIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wavMap.get("ObjectID")).intValue());
				updatePStat.setString(3, (String) wavMap.get("ObjectName"));
				//updatePStat.setString(4, (String) wavMap.get("Description"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessXMLDocument
			sql = "SELECT * FROM WorkflowXMLDocument WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wxmldList = (List) executeQuery(new MapListHandler());
			Iterator wxmldIterator = wxmldList.iterator();
	
			updateSQL = "INSERT INTO ProcessXMLDocument(ProcessID, ObjectID, ObjectName, Description, Source, XMLSchema, XML) VALUES(?,?,?,?,?,?,?)";
			while(wxmldIterator.hasNext()) {
				Map wxmldMap = (Map) wxmldIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wxmldMap.get("ObjectID")).intValue());
				updatePStat.setString(3, (String) wxmldMap.get("ObjectName"));
				updatePStat.setString(4, (String) wxmldMap.get("Description"));
				updatePStat.setInt(5, ((Integer) wxmldMap.get("Source")).intValue());
				updatePStat.setString(6, (String) wxmldMap.get("XMLSchema"));
				updatePStat.setString(7, (String) wxmldMap.get("XML"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			//导入ProcessXmlObjTemplate
			sql = "SELECT * FROM WorkflowXmlObjTemplate WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wxmlotList = (List) executeQuery(new MapListHandler());
			Iterator wxmlotIterator = wxmlotList.iterator();
	
			updateSQL = "INSERT INTO ProcessXmlObjTemplate(ProcessID, ActivityID, ObjectID, FCTXslt) VALUES(?,?,?,?)";
			while(wxmlotIterator.hasNext()) {
				Map wxmlotMap = (Map) wxmlotIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wxmlotMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) wxmlotMap.get("ObjectID")).intValue());
				updatePStat.setString(4, (String) wxmlotMap.get("FCTXslt"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessDocument
			sql = "SELECT * FROM WorkflowDocument WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wdList = (List) executeQuery(new MapListHandler());
			Iterator wdIterator = wdList.iterator();
			
			updateSQL = "INSERT INTO ProcessDocument(ProcessID, ObjectID, ObjectName, Description, Source, FromSchemaID, XML,Path) VALUES(?,?,?,?,?,?,?,?)";
			while(wdIterator.hasNext()) {
				Map wdMap = (Map) wdIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wdMap.get("ObjectID")).intValue());
				updatePStat.setString(3, (String) wdMap.get("ObjectName"));
				updatePStat.setString(4, (String) wdMap.get("Description"));
				if(wdMap.get("Source")!= null)
					updatePStat.setInt(5, ((Integer) wdMap.get("Source")).intValue());
				else
					updatePStat.setInt(5,0);
				if(wdMap.get("FromSchemaID")!=null)
					updatePStat.setInt(6, ((Integer) wdMap.get("FromSchemaID")).intValue());
				else
					updatePStat.setInt(6,0);
				updatePStat.setString(7, "");

				
				//导入实际的数据文件到文件系统中
				//更改表ProcessDocument中字段Path
				//add by dy
				//2006 4.25
				//sxh modified 2007.9
				String workflowName = (String) wiMap.get("WorkflowName");
				String processPath = instantiateFlowObject(packageName,workflowName, processID,(String) wdMap.get("Path"));
				//servercomment System.out.println(processPath);
				updatePStat.setString(8,processPath);
				
				updatePStat.executeUpdate();
				updatePStat.close();	
				
			}
			
			//导入ProcessReferenceVariable
//			sql = "SELECT * FROM WorkflowReferenceVariable WHERE (WorkflowID=?) AND (ParentWorkflowID IN (SELECT WorkflowID FROM CaseInformation WHERE ProcessID=?))";
//			params = new Object[] {new Integer(this.workflowID), parentCaseID};
//			types = new int[] {Types.INTEGER, Types.VARCHAR};
//			List wrvList = (List) executeQuery(new MapListHandler());
//			Iterator wrvIterator = wrvList.iterator();
//	
//			updateSQL = "INSERT INTO ProcessReferenceVariable(ProcessID, ObjectID, ObjectName, Description, ParentProcessID, ParentObjectID) VALUES(?,?,?,?,?,?)";
//			while(wrvIterator.hasNext()) {
//				Map wrvMap = (Map) wrvIterator.next();
//				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
//				updatePStat.setString(1, processID);
//				updatePStat.setInt(2, ((Integer) wrvMap.get("ObjectID")).intValue());
//				updatePStat.setString(3, (String) wrvMap.get("ObjectName"));
//				updatePStat.setString(4, (String) wrvMap.get("Description"));
//				updatePStat.setString(5, parentCaseID);
//				updatePStat.setInt(6, ((Integer) wrvMap.get("ParentObjectID")).intValue());
//				updatePStat.executeUpdate();
//				updatePStat.close();
//			}
			
			//导入ProcessObjectMapping
			sql = "SELECT * FROM WorkflowObjectMapping WHERE (WorkflowID=?) AND (MappingWorkflowID IN (SELECT WorkflowID FROM CaseInformation WHERE ProcessID=?))";
			params = new Object[] {new Integer(this.workflowID), parentCaseID};
			types = new int[] {Types.INTEGER, Types.VARCHAR};
			List womList = (List) executeQuery(new MapListHandler());
			Iterator womIterator = womList.iterator();
	
			updateSQL = "INSERT INTO ProcessObjectMapping(ProcessID, ObjectID, MappingRelation, MappingProcessID, MappingObjectID) VALUES(?,?,?,?,?)";
			while(womIterator.hasNext()) {
				Map womMap = (Map) womIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				//Add a relation to its self
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) womMap.get("ObjectID")).intValue());
				updatePStat.setInt(3, 0);
				updatePStat.setString(4, parentCaseID);
				updatePStat.setInt(5, ((Integer) womMap.get("MappingObjectID")).intValue());
				updatePStat.executeUpdate();
				
				//Add a relation to its parent
				updatePStat.setString(1, parentCaseID);
				updatePStat.setInt(2, ((Integer) womMap.get("MappingObjectID")).intValue());
				updatePStat.setInt(3, 1);
				updatePStat.setString(4, processID);
				updatePStat.setInt(5, ((Integer) womMap.get("ObjectID")).intValue());
				updatePStat.executeUpdate();
				
				updatePStat.close();
				
				//Synchronized current process object value with parent proces object value
				ProcessObject poDes = ElementFactory.createProcessObject(conn, processID, ((Integer) womMap.get("ObjectID")).intValue());
				ProcessObject poSrc = ElementFactory.createProcessObject(conn, parentCaseID, ((Integer) womMap.get("MappingObjectID")).intValue());
				poDes.changeObjectValue(poSrc.getValue());
			}

			
			
			
//			导入ProcessFlowObjects
			sql = "SELECT * FROM WorkflowFlowObjects WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			
		  try{
			  
			List wfoList = (List) executeQuery(new MapListHandler());		
			Iterator wfoIterator = wfoList.iterator();
	        //updated by dingo on 2007-01-08
			
			
			int fromActivityID,toActivityID;
			int fromActivityImplementation=-1,toActivityImplementation=-1;
			while(wfoIterator.hasNext()) {
			 Map wfoMap = (Map) wfoIterator.next();
			 fromActivityID = ((Integer) wfoMap.get("FromActivityID")).intValue();
			 toActivityID = ((Integer) wfoMap.get("ToActivityID")).intValue();
			 
			 ////////////////////////////dingo updated on 07-07-28
			    sql = "SELECT * FROM WorkflowActivityInformation WHERE WorkflowID=? AND ActivityID=?";
				params = new Object[] {new Integer(this.workflowID),new Integer(fromActivityID)};
				types = new int[] {Types.INTEGER,Types.INTEGER};
				List wfoList1 = (List) executeQuery(new MapListHandler());		
				Iterator wfoIterator1 = wfoList1.iterator();
				if(wfoIterator1.hasNext()) {
					 Map wfoMap1 = (Map) wfoIterator1.next();
					 fromActivityImplementation = ((Integer) wfoMap1.get("ActivityImplementation")).intValue();
				}
				
				params = new Object[] {new Integer(this.workflowID),new Integer(toActivityID)};
				types = new int[] {Types.INTEGER,Types.INTEGER};
			    wfoList1 = (List) executeQuery(new MapListHandler());		
				wfoIterator1 = wfoList1.iterator();
				if(wfoIterator1.hasNext()) {
					 Map wfoMap1 = (Map) wfoIterator1.next();
					 toActivityImplementation = ((Integer) wfoMap1.get("ActivityImplementation")).intValue();
				}
					
				
			 ////////////////////////////dingo updated on 07-07-28
			 
			 if(fromActivityID==0||(fromActivityID == 1||fromActivityImplementation==1||fromActivityImplementation==3||
					 
					 fromActivityImplementation==4||fromActivityImplementation==5||fromActivityImplementation==6||fromActivityImplementation==7) &&
					 
					 (toActivityID == 2||toActivityImplementation==1||toActivityImplementation==3||toActivityImplementation==4||toActivityImplementation==5||
					
							 toActivityImplementation==6||toActivityImplementation==7))
			 {
				 String updateSQL0 = "INSERT INTO ProcessFlowObjects(ProcessID, FlowID, DroolsRuleID, FromActivityID, ToActivityID) VALUES(?,?,?,?,?)";
				    PreparedStatement updatePStat = conn.prepareStatement(updateSQL0);
					updatePStat.setString(1, processID);
					updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
					updatePStat.setInt(3, ((Integer) wfoMap.get("DroolsRuleID")).intValue());	
					updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
					updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
					updatePStat.executeUpdate();
					updatePStat.close();
					//servercomment System.out.println("addDataFlow " + wfoMap.get("FlowID"));
			 }
			 else{ 
				if(fromActivityID == 1||fromActivityImplementation==1||fromActivityImplementation==3||fromActivityImplementation==4||fromActivityImplementation==5||fromActivityImplementation==6||fromActivityImplementation==7){
					String updateSQL1 =
			              "insert into ProcessFlowObjects(ProcessID, FlowID, DroolsRuleID, FromActivityID, ToActivityID, XSLTM2I, MXMLID, IXMLID) values(?,?,?,?,?,?,?,?)";
					
					PreparedStatement updatePStat = conn.prepareStatement(updateSQL1);
					updatePStat.setString(1, processID);
					updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
					updatePStat.setInt(3, ((Integer) wfoMap.get("DroolsRuleID")).intValue());	
					updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
					updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
					updatePStat.setString(6, (String)wfoMap.get("XSLTM2I"));
					updatePStat.setInt(7, ((Integer) wfoMap.get("MXMLID")).intValue());
					updatePStat.setInt(8, ((Integer) wfoMap.get("IXMLID")).intValue());
					updatePStat.executeUpdate();
					updatePStat.close();					
					//servercomment System.out.println("addDataFlow 1");
				}else if(toActivityID == 2||toActivityImplementation==1||toActivityImplementation==3||toActivityImplementation==4||toActivityImplementation==5||toActivityImplementation==6||toActivityImplementation==7){
					String updateSQL2 =
			              "insert into ProcessFlowObjects(ProcessID, FlowID, DroolsRuleID, FromActivityID, ToActivityID, XSLTO2M, OXMLID, MXMLID) values(?,?,?,?,?,?,?,?)";
					PreparedStatement updatePStat = conn.prepareStatement(updateSQL2);
					updatePStat.setString(1, processID);
					updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
					updatePStat.setInt(3, ((Integer) wfoMap.get("DroolsRuleID")).intValue());	
					updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
					updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
					updatePStat.setString(6, (String)wfoMap.get("XSLTO2M"));
					updatePStat.setInt(7, ((Integer) wfoMap.get("OXMLID")).intValue());
					updatePStat.setInt(8, ((Integer) wfoMap.get("MXMLID")).intValue());
					updatePStat.executeUpdate();
					updatePStat.close();				
					//servercomment System.out.println("addDataFlow 2");
				}else{
					
				String updateSQL3 = "INSERT INTO ProcessFlowObjects(ProcessID, FlowID, DroolsRuleID, FromActivityID, ToActivityID, XSLTO2M, XSLTM2I, OXMLID, MXMLID, IXMLID) VALUES(?,?,?,?,?,?,?,?,?,?)";					
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL3);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
				updatePStat.setInt(3, ((Integer) wfoMap.get("DroolsRuleID")).intValue());	
				updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
				updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
				updatePStat.setString(6, (String)wfoMap.get("XSLTO2M"));
				updatePStat.setString(7, (String)wfoMap.get("XSLTM2I"));
				updatePStat.setInt(8, ((Integer) wfoMap.get("OXMLID")).intValue()); 
				updatePStat.setInt(9, ((Integer) wfoMap.get("MXMLID")).intValue());
				updatePStat.setInt(10, ((Integer) wfoMap.get("IXMLID")).intValue());
				
				updatePStat.executeUpdate();
				updatePStat.close();
				//servercomment System.out.println("addDataFlow 3");
				}
				
			 }			 
			}
		  }catch(Exception e)
	      {
			 //servercomment System.out.println(e.toString());
	         //servercomment System.out.println("insert into ProcessFlowObjects :exception!");
	      }
		  
		  
			//导入ProcessFlowObjectControl
			sql = "SELECT * FROM WorkflowFlowObjectControl WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wfocList = (List) executeQuery(new MapListHandler());
			Iterator wfocIterator = wfocList.iterator();
		
			updateSQL = "INSERT INTO ProcessFlowObjectControl(ProcessID, FlowID, ObjectID, RepeatedTime, Privilege, State, ActiveTime) VALUES(?,?,?,?,?,?,?)";
			while(wfocIterator.hasNext()) {
				Map wfocMap = (Map) wfocIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wfocMap.get("FlowID")).intValue());
				updatePStat.setInt(3, ((Integer) wfocMap.get("ObjectID")).intValue());
				updatePStat.setInt(4, 0);
				updatePStat.setInt(5, ((Integer) wfocMap.get("Privilege")).intValue());
				updatePStat.setString(6, Constants.DATAFLOW_STATE_INACTIVE);
				updatePStat.setDate(7, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//5.导入ECA相关的信息
			/***************************************************************************************
				活动相关信息包括：ProcessEvent, ProcessEventRelation, ProcessEventRelationforParse, ProcessCondition, ProcessProcessECARule, ProcessActivityECARule
				
				具体步骤：
					1.把相应的数据从WorkflowEvent导入到ProcessEvent.
					2.把相应的数据从WorkflowEventRelation导入到ProcessEventRelation和ProcessEventRelationforParse.
					3.把相应的数据从WorkflowCondition导入到ProcessCondition.
					4.把相应的数据从WorkflowProcessECARule导入到ProcessProcessECARule.
					5.把相应的数据从WorkflowActivityECARule导入到ProcessActivityECARule.
					6.把相应的数据从WorkflowAgentRules导入到ProcessAgentRules.
					7.把相应的数据从WorkflowExtendedRules导入到ProcessExtendedRules.
					8.把相应的数据从WorkflowFlow导入到ProcessFlow
					9.把相应的数据从WorkflowRuleNodePosition导入到ProcessRuleNodePosition
					10.把相应的数据从WorkflowPointOnEdge导入到ProcessPointOnEdge
					11.把相应的数据从WorkflowControlFlowCondition导入到ProcessControlFlowCondition
			*****************************************************************************************/
			//导入ProcessEvent
			sql = "SELECT * FROM WorkflowEvent WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List weList = (List) executeQuery(new MapListHandler());
			Iterator weIterator = weList.iterator();
			
			updateSQL = "INSERT INTO ProcessEvent(ProcessID, EventID, EventName, Description, EventRepresentation, ExpressionForParse, EventType, LogicType, ActivityID, RepeatedTime, ActiveTime) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			while(weIterator.hasNext()) {
				Map weMap = (Map) weIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) weMap.get("EventID")).intValue());
				updatePStat.setString(3, (String) weMap.get("EventName"));
				updatePStat.setString(4, (String) weMap.get("Description"));
				updatePStat.setString(5, (String) weMap.get("EventRepresentation"));
				updatePStat.setString(6, (String) weMap.get("EventRepresentation"));
				updatePStat.setInt(7, ((Integer) weMap.get("EventType")).intValue());
				updatePStat.setInt(8, ((Integer) weMap.get("LogicType")).intValue());
				updatePStat.setInt(9, ((Integer) weMap.get("ActivityID")).intValue());
				updatePStat.setInt(10, 0);
				updatePStat.setDate(11, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessEventRelation和ProcessEventRelationforParse
			sql = "SELECT * FROM WorkflowEventRelation WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List werList = (List) executeQuery(new MapListHandler());
			Iterator werIterator = werList.iterator();
	
			while(werIterator.hasNext()) {
				Map werMap = (Map) werIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement("INSERT INTO ProcessEventRelation(ProcessID, ParentEventID, ChildEventID) VALUES(?,?,?)");
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) werMap.get("FatherEventID")).intValue());
				updatePStat.setInt(3, ((Integer) werMap.get("SonEventID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
				
				updatePStat = conn.prepareStatement("INSERT INTO ProcessEventRelationforParse(ProcessID, FatherEventID, SonEventID) VALUES(?,?,?)");
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) werMap.get("FatherEventID")).intValue());
				updatePStat.setInt(3, ((Integer) werMap.get("SonEventID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessCondition
			sql = "SELECT * FROM WorkflowCondition WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wcList = (List) executeQuery(new MapListHandler());
			Iterator wcIterator = wcList.iterator();
			
			updateSQL = "INSERT INTO ProcessCondition(ProcessID, ConditionID, ConditionRepresentation, ConditionName) VALUES(?,?,?,?)";
			while(wcIterator.hasNext()) {
				Map wcMap = (Map) wcIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wcMap.get("ConditionID")).intValue());
				updatePStat.setString(3, (String) wcMap.get("ConditionRepresentation"));
				updatePStat.setString(4, (String) wcMap.get("ConditionName"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessProcessECARule
			sql = "SELECT * FROM WorkflowProcessECARule WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wpecaList = (List) executeQuery(new MapListHandler());
			Iterator wpecaIterator = wpecaList.iterator();

			updateSQL = "INSERT INTO ProcessProcessECARule(ProcessID, RuleID, EventID, ConditionID, ActionExpression , Probability , ProbabilityCoefficient, RepeatedTime, ActiveTime) VALUES(?,?,?,?,?,?,?,?,?)";
			while(wpecaIterator.hasNext()) {
				Map wpecaMap = (Map) wpecaIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wpecaMap.get("RuleID")).intValue());
				updatePStat.setInt(3, ((Integer) wpecaMap.get("EventID")).intValue());
				updatePStat.setInt(4, ((Integer) wpecaMap.get("ConditionID")).intValue());
				updatePStat.setString(5, (String) wpecaMap.get("ActionExpression"));
				updatePStat.setFloat(6, ((Float) wpecaMap.get("Probability")).floatValue());
				updatePStat.setFloat(7, ((Float) wpecaMap.get("ProbabilityCoefficient")).floatValue());
				updatePStat.setInt(8, 0);
				updatePStat.setDate(9, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessActivityECARule
			sql = "SELECT * FROM WorkflowActivityECARule WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waecaList = (List) executeQuery(new MapListHandler());
			Iterator waecaIterator = waecaList.iterator();
			
			updateSQL = "INSERT INTO ProcessActivityECARule(ProcessID, ActivityID, RuleID, EventID, ConditionID, FromState, ToState, ActionExpression , Probability , ProbabilityCoefficient, RepeatedTime, ActiveTime) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
			while(waecaIterator.hasNext()) {
				Map waecaMap = (Map) waecaIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waecaMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waecaMap.get("RuleID")).intValue());
				updatePStat.setInt(4, ((Integer) waecaMap.get("EventID")).intValue());
				updatePStat.setInt(5, ((Integer) waecaMap.get("ConditionID")).intValue());
				updatePStat.setString(6, (String) waecaMap.get("FromState"));
				updatePStat.setString(7, (String) waecaMap.get("ToState"));
				updatePStat.setString(8, (String) waecaMap.get("ActionExpression"));
				updatePStat.setFloat(9, ((Integer) waecaMap.get("Probability")).floatValue());
				updatePStat.setFloat(10, ((Integer) waecaMap.get("ProbabilityCoefficient")).floatValue());
				updatePStat.setInt(11, 0);
				updatePStat.setDate(12, new java.sql.Date(System.currentTimeMillis()));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessAgentRules
			sql = "SELECT * FROM WorkflowAgentRules WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List waRuleList = (List) executeQuery(new MapListHandler());
			Iterator waRuleIterator = waRuleList.iterator();
			
			updateSQL = "INSERT INTO ProcessAgentRules(ProcessID, ActivityID, EventID, RuleFileID, RuleContent) VALUES(?,?,?,?,?)";
			while(waRuleIterator.hasNext()) {
				Map waRuleMap = (Map) waRuleIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) waRuleMap.get("ActivityID")).intValue());
				updatePStat.setInt(3, ((Integer) waRuleMap.get("EventID")).intValue());
				updatePStat.setInt(4, ((Integer) waRuleMap.get("RuleFileID")).intValue());
				updatePStat.setString(5, (String) waRuleMap.get("RuleContent"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessExtendedRules
			sql = "SELECT * FROM WorkflowExtendedRules WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List weRuleList = (List) executeQuery(new MapListHandler());
			Iterator weRuleIterator = weRuleList.iterator();
			
			updateSQL = "INSERT INTO ProcessExtendedRules(ProcessID, EventID, RuleFileID, RuleContent) VALUES(?,?,?,?)";
			while(weRuleIterator.hasNext()) {
				Map weRuleMap = (Map) weRuleIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				//updatePStat.setInt(2, ((Integer) weRuleMap.get("ActivityID")).intValue());
				updatePStat.setInt(2, ((Integer) weRuleMap.get("EventID")).intValue());
				updatePStat.setInt(3, ((Integer) weRuleMap.get("RuleFileID")).intValue());
				updatePStat.setString(4, (String) weRuleMap.get("RuleContent"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入processdroolsrule,add by chan
			sql = "SELECT * FROM WorkflowDroolsRule WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wdRuleList = (List) executeQuery(new MapListHandler());
			Iterator wdRuleIterator = wdRuleList.iterator();
			
			updateSQL = "INSERT INTO ProcessDroolsRule(ProcessID, DroolsRuleID, DroolsRuleLHS) VALUES(?,?,?)";
			while(wdRuleIterator.hasNext()) {
				Map wdRuleMap = (Map) wdRuleIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wdRuleMap.get("DroolsRuleID")).intValue());
				updatePStat.setString(3, (String) wdRuleMap.get("DroolsRuleLHS"));
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessFlow
			sql = "SELECT * FROM WorkflowFlow WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wffList = (List) executeQuery(new MapListHandler());
			Iterator wffIterator = wffList.iterator();
			
			updateSQL = "INSERT INTO ProcessFlow VALUES(?,?,?,?,?,?,?,?,?)";
			while(wffIterator.hasNext()) {
				Map wffMap = (Map) wffIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setInt(1, ((Integer)wffMap.get("ModelType")).intValue());
				updatePStat.setString(2, processID);
				updatePStat.setInt(3, ((Integer) wffMap.get("FlowID")).intValue());
				updatePStat.setInt(4, ((Integer) wffMap.get("ParentID")).intValue());
				updatePStat.setInt(5, ((Integer) wffMap.get("Type")).intValue());
				updatePStat.setInt(6, ((Integer) wffMap.get("FromObjectType")).intValue());
				updatePStat.setInt(7, ((Integer) wffMap.get("ObjectId1")).intValue());
				updatePStat.setInt(8, ((Integer) wffMap.get("ToObjectType")).intValue());
				updatePStat.setInt(9, ((Integer) wffMap.get("ObjectID2")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessRuleNodePosition
			sql = "SELECT * FROM WorkflowRuleNodePosition WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wrnpList = (List) executeQuery(new MapListHandler());
			Iterator wrnpIterator = wrnpList.iterator();
			
			updateSQL = "INSERT INTO ProcessRuleNodePosition VALUES(?,?,?,?,?,?,?,?,?)";
			while(wrnpIterator.hasNext()) {
				Map wrnpMap = (Map) wrnpIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setInt(1, ((Integer)wrnpMap.get("ModelType")).intValue());
				updatePStat.setString(2, processID);
				updatePStat.setInt(3, ((Integer) wrnpMap.get("RuleID")).intValue());
				updatePStat.setInt(4, ((Integer) wrnpMap.get("ParentID")).intValue());
				updatePStat.setInt(5, ((Integer) wrnpMap.get("InputType")).intValue());
				updatePStat.setInt(6, ((Integer) wrnpMap.get("OutputType")).intValue());
				updatePStat.setInt(7, ((Integer) wrnpMap.get("Xposition")).intValue());
				updatePStat.setInt(8, ((Integer) wrnpMap.get("Yposition")).intValue());
				updatePStat.setInt(9, ((Integer) wrnpMap.get("IconID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessPointOnEdge
			sql = "SELECT * FROM WorkflowPointOnEdge WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wpeList = (List) executeQuery(new MapListHandler());
			Iterator wpeIterator = wpeList.iterator();
			
			updateSQL = "INSERT INTO ProcessPointOnEdge VALUES(?,?,?,?,?,?,?,?,?)";
			while(wpeIterator.hasNext()) {
				Map wpepMap = (Map) wpeIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setInt(1, ((Integer)wpepMap.get("PackageID")).intValue());
				updatePStat.setString(2, processID);
				updatePStat.setInt(3, ((Integer) wpepMap.get("FlowType")).intValue());
				updatePStat.setInt(4, ((Integer) wpepMap.get("FlowID")).intValue());
				updatePStat.setInt(5, ((Integer) wpepMap.get("PointID")).intValue());
				updatePStat.setInt(6, ((Integer) wpepMap.get("Xposition")).intValue());
				updatePStat.setInt(7, ((Integer) wpepMap.get("Yposition")).intValue());
				updatePStat.setInt(8, ((Integer) wpepMap.get("LastPointID")).intValue());
				updatePStat.setInt(9, ((Integer) wpepMap.get("NextPointID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			//导入ProcessControlFlowCondition
			sql = "SELECT * FROM WorkflowControlFlowCondition WHERE WorkflowID=?";
			params = new Object[] {new Integer(this.workflowID)};
			types = new int[] {Types.INTEGER};
			List wcfcList = (List) executeQuery(new MapListHandler());
			Iterator wcfcIterator = wcfcList.iterator();
			
			updateSQL = "INSERT INTO ProcessControlFlowCondition VALUES(?,?,?)";
			while(wcfcIterator.hasNext()) {
				Map wcfcMap = (Map) wcfcIterator.next();
				PreparedStatement updatePStat = conn.prepareStatement(updateSQL);
				updatePStat.setString(1, processID);
				updatePStat.setInt(2, ((Integer) wcfcMap.get("ControlFlowID")).intValue());
				updatePStat.setInt(3, ((Integer) wcfcMap.get("ConditionID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
		return processID;
	}
	
	//sxh modified 2007.9
	/**
	 * 实例化工作流模型中涉及的数据流文件
	 * 算法：根据workflowDocument中的记录，将object所对应的文档从模型的模版库中拷贝到
	 *      该模型实际运行的某个package的process目录下。
	 *      文件的路径存储在path中。
	 * @param processID
	 * @param objectName
	 */
	public String instantiateFlowObject(String packageName, String workflowName, String processID,String path){
		//找到路径，将path中的文件拷贝至路径即可
		File workflowFile = new File(path);
		String strProcessFileDirectory = Constants.processDocPath+"\\"+packageName+"\\" + workflowName + "\\" +processID;
		File processFileDirectory = new File(strProcessFileDirectory);
		if(!processFileDirectory.exists()){
			processFileDirectory.mkdirs();
		}
		if(workflowFile.exists()){
			//copy the file to the process folder
			SaveFileToFile(path, strProcessFileDirectory+"\\"+workflowFile.getName());
		}
		return strProcessFileDirectory+"\\"+workflowFile.getName();
	}
	
    public void SaveFileToFile(String F1,String F2){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try{
            fis = new FileInputStream(new File(F1));    //建立文件输入流
            
            File file = new File(F2);
            fos = new FileOutputStream(F2);
            
            int r;
            while((r=fis.read())!=-1){
                fos.write((byte)r);
            }
        }
        catch(FileNotFoundException ex){
            //servercomment System.out.println("Source File not found:"+F1);
        }
        catch(IOException ex){
            //servercomment System.out.println(ex.getMessage());
        }
        finally{
            try{
                if(fis!=null) fis.close();
                if(fos!=null) fos.close();
            }
            catch(IOException ex){
                //servercomment System.out.println(ex);
            }
        }
    }
	
	
	
	
}
