package cit.workflow.dao;

//lrj add  begin 07-12-6
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

public class ActivityInformationDAO extends DBUtility{
	
	public ActivityInformationDAO(Connection conn){
		super(conn);
	}
	
	public int getActivityID(String processID, int eventID) throws WorkflowTransactionException{
		PreparedStatement pstmt = null;
		int activityID = 0;
		sql = "SELECT * FROM ProcessEvent " +
				"WHERE ProcessID = ?"+
				"AND EventID = ?";
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, processID);
			pstmt.setInt(2, eventID);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
			{
				activityID = rs.getInt("ActivityID");
			}
			pstmt.close();
		}
		catch (SQLException e){
			throw new WorkflowTransactionException("ActivityIDDAO.getActivityID throw exception.");
		}
		return activityID;
	}
	
	public String getActivityState(String processID, int activityID) throws WorkflowTransactionException
	{
		String activityState = "";
		PreparedStatement pstmt = null;
		sql = "SELECT * FROM ProcessActivityInformation " +
				"WHERE ProcessID = ?"+
				"AND ActivityID = ?";
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, processID);
			pstmt.setInt(2, activityID);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
			{
				activityState = rs.getString("State");
			}
			pstmt.close();
		}
		catch (SQLException e){
			throw new WorkflowTransactionException("ActivityStateDAO.getActivityID throw exception.");
		}
		return activityState;
	}
	
	
	public boolean setActivityStartDate(String processID, int activityID, Date actualStartDate) throws WorkflowTransactionException {
		sql = "UPDATE ProcessTWCInformation pti LEFT JOIN ProcessActivityInformation pai ON pti.processID = pai.processID AND pti.twcID = pai.twcID SET pti.actualStartDate = ? WHERE pai.processID = ? AND pai.activityID = ?";
		params = new Object[] {actualStartDate, processID, new Integer(activityID)};
		types = new int[] {Types.DATE, Types.VARCHAR, Types.INTEGER};
		boolean success = false;
		try {
			executeUpdate();
			success = true;
		} catch (SQLException e) {
			throw new WorkflowTransactionException("PersonRoleDAO.getUserInfomation throw exception.");
		}
		return success;
	}
}
//lrj add end 07-12-6
