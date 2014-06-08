package cit.workflow.dao;

//lrj add  begin 07-12-6
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

public class ActivityIDDAO extends DBUtility{
	
	public ActivityIDDAO(Connection conn){
		super(conn);
	}
	
	public int getActivityID(String processID, int eventID) throws WorkflowTransactionException{
		PreparedStatement pstmt = null;
		int activityID = 0;
		sql = "SELECT ActivityID FROM ProcessEvent " +
				"WHERE ProcessID = ?"+
				" AND EventID = ?";
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, processID);
			pstmt.setInt(2, activityID);
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
}
//lrj add end 07-12-6
