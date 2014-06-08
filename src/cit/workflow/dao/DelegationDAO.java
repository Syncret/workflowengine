package cit.workflow.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

public class DelegationDAO extends DBUtility {

	public DelegationDAO(Connection conn) 
	{
		super(conn);
	}
	
	public boolean canDelegate(String processID, int activityID) throws WorkflowTransactionException {
		sql = "SELECT * FROM DelegationInformation where processID = ? and activityID = ?";
		params = new Object[] { processID, new Integer(activityID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.getDelegatedRoleID throw exception.");
		}
		if (resultList.size() > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 得到可以被转授权的角色ID数组
	 * @param delegatingRoleID 授权者角色ID
	 * @return 可以被转授权的角色ID数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getDelegatedRoleIDSet(int delegatingRoleID) throws WorkflowTransactionException {
		sql = "SELECT DelegatedRoleID FROM DelegationRelationShip where DelegatingRoleID = ?";
		params = new Object[] { new Integer(delegatingRoleID) };
		types = new int[] { Types.INTEGER };
		
		List resultList = null;
		int[] resultSet = null;
		int size = 0;
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)map.get("DelegatedRoleID");
			}
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.getDelegatedRoleID throw exception.");
		}
		return resultSet;
	}
	
	
	/**
	 * 得到可以被转授权的人员列表
	 * @param delegatingRoleID 授权者角色ID
	 * @return 可以被转授权的人员列表
	 * @throws WorkflowTransactionException
	 */
	public List getDelegatedPersonList(int delegatingRoleID) throws WorkflowTransactionException {
		sql = "SELECT pi.* FROM PersonInformation pi LEFT JOIN PersonRole pr ON pi.personID = pr.personID LEFT JOIN DelegationRelationShip drs ON pr.roleID = drs.delegatedRoleID  where delegatingRoleID = ?";
		params = new Object[] { new Integer(delegatingRoleID) };
		types = new int[] { Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.getDelegatedRoleID throw exception.");
		}
		return resultList;
	}
	
	public List getCurrentDelegatedPersonID(int delegatingRoleID) throws SQLException {
		sql = "SELECT distinct delegatedPersonID FROM DelegationDaemon where delegatingRoleID = ? AND actualEndTime IS NULL";
		params = new Object[] { new Integer(delegatingRoleID) };
		types = new int[] { Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.getDelegatedRoleID throw exception.");
		}
		return resultList;
	}
	
	
	
	/**
	 * 转授权
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param delegatingRoleID 授权者角色ID
	 * @param delegatedRoleID 被授权者角色ID
	 * @param delegatedPersonID 被授权者人员ID
	 * @param startTime 转授权开始时间
	 * @param endTime 转授权结束时间
	 * @return 转授权情况
	 * @throws WorkflowTransactionException
	 */
	public boolean delegate(String processID, int activityID, int delegatingRoleID, int delegatedRoleID, int delegatedPersonID, Date startTime, Date endTime) throws WorkflowTransactionException {
		
		sql = "INSERT INTO DelegationInformation VALUES(?, ?, ?, ?, ?, ?, ?)";
		params = new Object[] { processID, new Integer(activityID), new Integer(delegatingRoleID), new Integer(delegatedRoleID), new Integer(delegatedPersonID), startTime, endTime };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.DATE, Types.DATE };
		try {
			executeUpdate();
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.delegation throw exception.");
		}
		
		sql = "UPDATE ProcessActivityRole SET AllocatedNumber = 1 WHERE ProcessID = ? AND ActivityID = ? AND RoleID = ?";
		params = new Object[] { processID, new Integer(activityID), new Integer(delegatingRoleID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		try {
			executeUpdate();
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.delegation throw exception.");
		}
		
		sql = "INSERT INTO ProcessActivityRole VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		params = new Object[] { processID, new Integer(activityID), new Integer(delegatedRoleID), new Integer(1), new Integer(1), new Integer(1), new Integer(0), new Float(1)};
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.FLOAT };
		try {
			executeUpdate();
		} catch (SQLException e) {
			throw new WorkflowTransactionException("DelegationDAO.delegation throw exception.");
		}
		return true;
	}
	
	
	
	
	public void addDelegationInformation(int delegatingRoleID, int delegatedPersonID, Date startTime, Date definedEndTime, Date actualEndTime) throws SQLException {
		sql = "INSERT INTO DelegationDaemon VALUES(?, ?, ?, ?, ?, ?)";
		params = new Object[] { null, 
								new Integer(delegatingRoleID), 
								new Integer(delegatedPersonID), 
								startTime, 
								definedEndTime, 
								actualEndTime };
		types = new int[] { Types.INTEGER,
							Types.INTEGER, 
							Types.INTEGER, 
							Types.DATE, 
							Types.DATE, 
							Types.DATE };
		try {
			executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.close();
		}
	}
	
	public List getDelegationInformation(int delegatingRoleID, int delegatedPersonID) throws SQLException {
		sql = "SELECT * FROM DelegationDaemon WHERE delegatingRoleID = ? AND delegatedPersonID = ? AND actualEndTime IS NULL";
		params = new Object[] { new Integer(delegatingRoleID), 
								new Integer(delegatedPersonID) };
		types = new int[] { Types.INTEGER, 
							Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return resultList;
	}
	
	public void setActualEndTime(int id, Date actualEndTime) throws SQLException {
		sql = "UPDATE DelegationDaemon SET ActualEndTime = ? WHERE id = ?";
		params = new Object[] { actualEndTime, 
								new Integer(id) };
		types = new int[] { Types.DATE, 
							Types.INTEGER };
		try {
			executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.close();
		}
	}
	

}
