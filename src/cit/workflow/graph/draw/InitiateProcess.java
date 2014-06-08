package cit.workflow.graph.draw;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import cit.workflow.graph.model.ProcessInfo;
import cit.workflow.graph.model.element.BaseActivity;
import cit.workflow.graph.model.element.LinkFlow;
import cit.workflow.graph.model.element.LogicNode;
import cit.workflow.utils.WorkflowConnectionPool;


public class InitiateProcess{
	private ProcessInfo processInfo;
	private Connection connection;

	public ProcessInfo getProcessInfo() {
		return processInfo;
	}

	public void setProcessInfo(ProcessInfo processInfo) {
		this.processInfo = processInfo;
	}
	public void initProcess(String processID)
	{
		processInfo = new ProcessInfo();
		connection = WorkflowConnectionPool.getInstance().getConnection();
		processInfo.setProcessID(processID);
		processInfo.setWorkflowID(initWorkflowID());
		processInfo.setPackageID(initPackageID());
		processInfo.setActivitySet(initActivityList());
		processInfo.setFlowSet(initFlowSet());
		processInfo.setLogicNodeSet(initLogicNodeSet());
		
	}

	private int initPackageID() {
		// TODO Auto-generated method stub
		int packageID= 0;
		String sql = "select PackageID from WorkflowInformation where WorkflowID = " 
			+ processInfo.getWorkflowID();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()){
				packageID = resultSet.getInt("PackageID");
			}
			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return packageID;
	}

	public void initProcess(int workflowID)
	{
		processInfo = new ProcessInfo();
		connection = WorkflowConnectionPool.getInstance().getConnection();
		processInfo.setWorkflowID(workflowID);
		processInfo.setPackageID(initPackageID());
		processInfo.setActivitySet(initWorkflowActivityList());
		processInfo.setFlowSet(initFlowSet());
		processInfo.setLogicNodeSet(initLogicNodeSet());
	}
	private List initWorkflowActivityList() {
		// TODO Auto-generated method stub
		ArrayList activityList = new ArrayList();
		String sql = "select WorkflowActivityPosition.activityID, activityName, activityType, activityImplementation,parentID, " +
				"Xposition, Yposition from workflowactivityinformation, " +
				"WorkflowActivityPosition where ModelType = 1 and " +
				"WorkflowActivityPosition.activityID = workflowactivityinformation.activityID and WorkflowActivityPosition.WorkflowID=" 
				+ processInfo.getWorkflowID() + " and " + "workflowactivityinformation.workflowID = " + processInfo.getWorkflowID();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				BaseActivity activity = new BaseActivity();
				activity.setActivityID(resultSet.getInt("activityID"));
				activity.setActivityName(resultSet.getString("activityName"));
				activity.setActivityType(resultSet.getInt("activityType"));
				activity.setActivityImp(resultSet.getInt("activityImplementation"));
				activity.setParentID(resultSet.getInt("parentID"));
				Point point = new Point();
				point.setLocation(resultSet.getInt("Xposition"), resultSet.getInt("YPosition"));
				activity.setActivityPoint(point);
				activityList.add(activity);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return activityList;
		// TODO Auto-generated method stub;
	}

	private int initWorkflowID() {
		// TODO Auto-generated method stub
		String sql = "select WorkflowID from caseinformation where processID = '" + processInfo.getProcessID() + "'";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next())
			{
				return resultSet.getInt("WorkflowID");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private List initLogicNodeSet() {
		// TODO Auto-generated method stub
		ArrayList logicNodeSet = new ArrayList();
	    String connstr = "";
	    PreparedStatement pstmt = null;
	    try {
	      connstr =
	          "select * from ProcessRuleNodePosition where ModelType=1 and ProcessID="
	          + processInfo.getWorkflowID();
	      pstmt = connection.prepareStatement(connstr);
	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next()) {
	        LogicNode node = new LogicNode();
	        node.setLogicNodeID(rs.getInt("RuleID"));
	        node.setParentID(rs.getInt("ParentID"));
	        node.setInputType(rs.getInt("InputType"));
	        node.setOutputType(rs.getInt("OutputType"));
	        Point point = new Point();
	        point.setLocation(rs.getInt("Xposition"), rs.getInt("YPosition"));
	        node.setPoint(point);
	        logicNodeSet.add(node);
	      }
	      rs.close();
	      pstmt.close();
	    }
	    catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    return logicNodeSet;
	}

	private List initFlowSet() {
		// TODO Auto-generated method stub
		ArrayList flowList = new ArrayList();
		String sql = "select * from WorkflowFlow where ModelType = 1 and (Type = 1 or Type = 2) and WorkflowID = " + processInfo.getWorkflowID();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				LinkFlow linkFlow = new LinkFlow();
				linkFlow.setFromObjectID(resultSet.getInt("ObjectID1"));
				linkFlow.setFromObjectType(resultSet.getInt("FromObjectType"));
				linkFlow.setLinkID(resultSet.getInt("FlowID"));
				linkFlow.setLinkType(resultSet.getInt("Type"));
				linkFlow.setParentID(resultSet.getInt("parentID"));
				linkFlow.setToObjectID(resultSet.getInt("ObjectID2"));
				linkFlow.setToObjectType(resultSet.getInt("ToObjectType"));
				flowList.add(linkFlow);
			}
			resultSet.close();
			statement.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return flowList;
	}

	private List initActivityList() {
		ArrayList activityList = new ArrayList();
		String sql = "select ProcessActivityInformation.activityID, activityName, activityType, activityImplementation,parentID, " +
				"State, Xposition, Yposition from ProcessActivityInformation, " +
				"WorkflowActivityPosition where ModelType = 1 and WorkflowActivityPosition.activityID = ProcessActivityInformation.activityID " +
				"and WorkflowActivityPosition.WorkflowID=" 
				+ processInfo.getWorkflowID() + " and " + "ProcessActivityInformation.processID = '" + processInfo.getProcessID() + "'";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				BaseActivity activity = new BaseActivity();
				activity.setActivityID(resultSet.getInt("activityID"));
				activity.setActivityName(resultSet.getString("activityName"));
				activity.setActivityType(resultSet.getInt("activityType"));
				activity.setActivityImp(resultSet.getInt("activityImplementation"));
				activity.setActivityState(resultSet.getString("State"));
				activity.setParentID(resultSet.getInt("parentID"));
				Point point = new Point();
				point.setLocation(resultSet.getInt("Xposition"), resultSet.getInt("YPosition"));
				activity.setActivityPoint(point);
				activityList.add(activity);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return activityList;
		// TODO Auto-generated method stub
		
	}
	

}
