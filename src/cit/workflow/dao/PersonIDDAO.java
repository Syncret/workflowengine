package cit.workflow.dao;

//lrj add begin 07-11-8

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;


public class PersonIDDAO extends DBUtility 
{
	public PersonIDDAO(Connection conn) 
	{
		super(conn);
	}
	
	/**
	 * @function: Given ProcessID and ActivityID, get this user PersonID from Processactivityperson
	 * @param ProcessID and ActivityID	
	 * @return	PersonID
	 */
	public int getPersonID(String ProcessID,int ActivityID) throws WorkflowTransactionException
	{
		PreparedStatement pstmt = null;
		int MyPersonID = 0;
		sql = "SELECT * FROM ProcessActivityPerson " +
				"WHERE ProcessID = ?"+
				" AND ActivityID = ?" +
				" AND State = ?";
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, ProcessID);
			pstmt.setInt(2, ActivityID);
			pstmt.setString(3, "Submitted");
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
			{
				MyPersonID = rs.getInt("PersonID");
			}
			pstmt.close();
		}
		catch (SQLException e)
		{
			throw new WorkflowTransactionException("PersonIDDAO.getPersonID throw exception.");
		}
		return MyPersonID;
	}
	

}

// lrj add end 07-11-8
