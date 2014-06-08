/*
 * Created on 2004-12-22
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.factories;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.activities.CouldServiceActivity;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.elements.activities.AgencyActivity;
import cit.workflow.elements.activities.AgentActivity;
import cit.workflow.elements.activities.DelayActivity;
import cit.workflow.elements.activities.EndActivity;
import cit.workflow.elements.activities.InvokeApplicationActivity;
import cit.workflow.elements.activities.InvokeWorkflowActivity;
import cit.workflow.elements.activities.DoutaiActivity;
import cit.workflow.elements.activities.PersonActivity;
import cit.workflow.elements.activities.SetValueActivity;
import cit.workflow.elements.activities.StartActivity;
import cit.workflow.elements.activities.XMLTransformActivity;
import cit.workflow.elements.applications.CloudServiceInvoke;
import cit.workflow.elements.applications.ApplicationInvoke;
import cit.workflow.elements.applications.CommonApplicationInvoke;
import cit.workflow.elements.applications.DefaultApplicationInvoke;
import cit.workflow.elements.applications.JavaClassInvoke;
import cit.workflow.elements.applications.WebServiceInvoke;
import cit.workflow.elements.variables.ArrayVariable;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.InherentVariable;
import cit.workflow.elements.variables.ObjectVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.elements.variables.ReferenceObject;
import cit.workflow.elements.variables.XMLVariable;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.elements.applications.CommandLineInvoke;
import cit.workflow.elements.applications.DatabaseOperationInvoke;
import cit.workflow.elements.applications.UnifiedQueryInvoke;;
/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ElementFactory {
	
	public static AbstractActivity createActivity(Connection conn, Process process, int activityID, ProcessManager processManager) throws SQLException {
		//Connection conn = GlobalUtils.getInstance().getConnection();
		QueryRunner queryRunner = new QueryRunner();
		
		String sql = "SELECT ActivityType, ActivityImplementation FROM ProcessActivityInformation WHERE ProcessID=? AND ActivityID=?";
		Object[] params = new Object[]{process.getProcessID(), new Integer(activityID)};
		int[] types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processActivityInformationMap = (Map) queryRunner.query(conn, sql,new MapHandler(),  params);
		if (processActivityInformationMap != null) {
			
			int activityType = ((Integer) processActivityInformationMap.get("ActivityType")).intValue();
			int activityImplementation = ((Integer) processActivityInformationMap.get("ActivityImplementation")).intValue();
			
			
			AbstractActivity activity = null;
			switch (activityType) {
				case 1:
					activity = new StartActivity(conn, process, activityID, processManager);
					break;
				case 2:
					activity = new EndActivity(conn, process, activityID, processManager);
					break;
				case 3: {
					
					switch (activityImplementation) {
						case 1:
							activity = new PersonActivity(conn, process, activityID, processManager);
							break;
						case 2:
							activity = new InvokeApplicationActivity(conn, process, activityID, processManager);
							break;
						case 3:
							activity = new AgentActivity(conn, process, activityID, processManager);
							break;
						case 4:
							activity = new AgencyActivity(conn, process, activityID, processManager);
							break;
						case 5:
							activity = new DelayActivity(conn, process, activityID, processManager);
							break;
						case 6:
							activity = new SetValueActivity(conn, process, activityID, processManager);
							break;
						case 7:
							activity = new XMLTransformActivity(conn, process, activityID, processManager);
							break;
						case 8:
							activity = new InvokeWorkflowActivity(conn, process, activityID, processManager);
							break;
						case 11:
							activity = new DoutaiActivity(conn, process, activityID, processManager);
							break;
						default:
							throw new WorkflowTransactionException("Create activity failed!");
						
					}
					//sxh add 2007.12
					if (processActivityInformationMap.get("isCallBack") != null) {
						activity.setCallBack((Boolean)processActivityInformationMap.get("isCallBack"));
					} else {
						activity.setCallBack(false);
					}
					//sxh add 2007.12 end
					break;	
				}
				default:
					throw new WorkflowTransactionException("Create activity failed!");
			}
			activity.addStateChangeListener(processManager);
			return activity;
		} else {
			throw new WorkflowTransactionException("There is no right data for handle!");
		}
	}
	
	//sxh modified 2007
	public static ProcessObject createProcessObject(Connection conn, String processID, int objectID) throws SQLException {
		QueryRunner queryRunner = new QueryRunner();
		
		String sql = "SELECT ObjectType FROM ProcessObject WHERE ProcessID=? AND ObjectID=?";
		Object[] params = new Object[]{processID, new Integer(objectID)};
		int[] types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processObjectMap = (Map) queryRunner.query(conn, sql, new MapHandler(),params);
		if (processObjectMap != null) {
			int objectType = ((Integer) processObjectMap.get("objectType")).intValue();
			
			if (objectType == Constants.OBJECT_TYPE_INHERENT) {			
				return new InherentVariable(conn, processID, objectID);
			} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
				return new ObjectVariable(conn, processID, objectID);
			} else if (objectType == Constants.OBJECT_TYPE_XML) {
				return new XMLVariable(conn, processID, objectID);
			} else if (objectType == Constants.OBJECT_TYPE_DOC) {
				return new DocumentVariable(conn, processID, objectID);
			} else if(objectType == Constants.OBJECT_TYPE_REF) {
				return new ReferenceObject(conn, processID, objectID);
			} else if(objectType ==Constants.OBJECT_TYPE_ARRAY){
				//cyd add 
				return new ArrayVariable(conn,processID,objectID);
			}else
				throw new WorkflowTransactionException("Object type is out of range!");
		} else
			throw new WorkflowTransactionException("There is no right data for object!");
	}
	//sxh modified 2007 end

	public static ApplicationInvoke createApplication(Connection conn, int applicationID, String processID) throws SQLException {
		QueryRunner queryRunner = new QueryRunner();

		String sql = "SELECT ApplicationType FROM SystemApplicationInformation WHERE ApplicationID=?";
		Object[] params = new Object[] {new Integer(applicationID)};
		int[] types = new int[] {Types.INTEGER};
		Map saiMap = (Map) queryRunner.query(conn, sql,new MapHandler(),params);
		if (saiMap != null) {
			int applicationType = ((Integer) saiMap.get("ApplicationType")).intValue();
			if(applicationType == Constants.APPLICATION_JAVACLASS)  {
				//servercomment System.out.println("it is a JavaClass Application!");
			}
			else if(applicationType == Constants.APPLICATION_WEB_SERVICE) {
				//servercomment System.out.println("it is a WebService Application");
			}
	
			if (applicationType == Constants.APPLICATION_JAVACLASS) {//dingo
				return new JavaClassInvoke(conn, applicationID, processID);
			//} else if (applicationType == Constants.APPLICATION_COMDCOM) {
			//	return new DefaultApplicationInvoke();
			//} else if (applicationType == Constants.APPLICATION_SCRIPT) {
			//	return new DefaultApplicationInvoke();
			} else if (applicationType == Constants.APPLICATION_WEB_SERVICE) {
				// TODO uncomment next sentence
//				return new DefaultApplicationInvoke();
				return new WebServiceInvoke(conn, applicationID, processID);
			} else if (applicationType == Constants.APPLICATION_COMMANDLINE) {//dingo
				return new CommandLineInvoke(conn, applicationID);
			} else if (applicationType == Constants.APPLICATION_DATABASE) {//dingo
				return new DatabaseOperationInvoke(conn, applicationID);
			} else if (applicationType == Constants.APPLICATION_QUERY) {//dingo
//				return new UnifiedQueryInvoke(conn, applicationID);
				return null;
			} else if(applicationType==Constants.APPLIATION_CLOUDSERVICE){
				return new CloudServiceInvoke(conn,applicationID,processID);
			} else if (applicationType==Constants.APPLICATION_COMMONAPPLICATION){
				return new CommonApplicationInvoke(conn,applicationID,processID);
			}
			else
				throw new WorkflowTransactionException("Application type is out of range!");
		} else
			throw new WorkflowTransactionException("There is no right data for application!");
	}
}
