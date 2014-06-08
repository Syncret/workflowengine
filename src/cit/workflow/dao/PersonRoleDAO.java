/*
 * Created on 2005-3-30
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PersonRoleDAO extends DBUtility {

	public PersonRoleDAO(Connection conn) {
		super(conn);
	}
	
	/**
	 * @function: Given user name, get this user information.
	 * @param userName			a string object representing user name
	 * @return					a map object containing user information
	 */
	public Map getUserInfomation(String userName) throws WorkflowTransactionException {
		sql = "SELECT * FROM PersonInformation pr WHERE pr.UserName=?";
		params = new Object[] {new String(userName)};
		types = new int[] {Types.VARCHAR};
		Map resultMap = null;
		try {
			resultMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("PersonRoleDAO.getUserInfomation throw exception.");
		}
		return resultMap;
	}
	
	
	/**
	 * 得到工作流活动所分配的角色ID数组
	 * @param workflowID 工作流ID
	 * @param activityID 活动ID
	 * @return 角色ID数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getWorkflowActivityRoleIDSet(int workflowID, int activityID) throws WorkflowTransactionException {
		sql = "SELECT RoleID FROM WorkflowActivityRole WHERE workflowID = ? AND activityID = ?";
		params = new Object[] { new Integer(workflowID), new Integer(activityID) };
		types = new int[] { Types.INTEGER, Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("roleID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getWorkflowActivityRoleIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	/**
	 * 得到流程活动所分配的角色ID数组
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @return 角色ID数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getProcessActivityRoleIDSet(String processID, int activityID) throws WorkflowTransactionException {
		sql = "SELECT RoleID FROM ProcessActivityRole WHERE processID = ? AND activityID = ?";
		params = new Object[] { processID, new Integer(activityID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("roleID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getProcessActivityRoleIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	/**
	 * 得到该人员被分配的角色ID数组
	 * @param personID 人员ID
	 * @return 角色ID数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getRoleIDSet(int personID) throws WorkflowTransactionException {
		sql = "SELECT RoleID FROM PersonRole WHERE personID = ?";
		params = new Object[] { new Integer(personID) };
		types = new int[] { Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("roleID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonRoleIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	/**
	 * 根据角色ID得到角色信息
	 * @param roleID 角色ID
	 * @return 角色信息，以Map形式返回
	 * @throws WorkflowTransactionException
	 */
	public Map getRoleInformation(int roleID) throws WorkflowTransactionException {
		sql = "SELECT * FROM RoleInformation WHERE RoleID = ?";
		params = new Object[] { new Integer(roleID) };
		types = new int[] { Types.INTEGER };
		Map resultMap = null;
		
		try {
			resultMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getRoleInformation throw exception.");
		}
		return resultMap;
	}
	
	/**
	 * 根据人员ID得到角色信息列表
	 * @param personID 人员ID
	 * @return 角色信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getRoleInformationList(int personID) throws WorkflowTransactionException {
		sql = "SELECT ri.* FROM RoleInformation ri LEFT JOIN PersonRole pr ON ri.RoleID = pr.RoleID WHERE pr.PersonID = ?";
		params = new Object[] { new Integer(personID) };
		types = new int[] { Types.INTEGER };
		
		List resultList = null;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonIDSet throw exception.");
		}
		return resultList;
	}
	
	/**
	 * 得到角色所分配的人员ID数组
	 * @param roleID 角色ID
	 * @return 人员ID数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getPersonIDSet(int roleID) throws WorkflowTransactionException {
		sql = "SELECT personID FROM PersonRole WHERE RoleID = ?";
		params = new Object[] { new Integer(roleID) };
		types = new int[] { Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("personID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	/**
	 * 根据人员ID得到人员信息 
	 * @param personID 人员ID
	 * @return 人员信息，以Map形式返回
	 * @throws WorkflowTransactionException
	 */
	public Map getPersonInformation(int personID) throws WorkflowTransactionException {
		sql = "SELECT * FROM PersonInformation  WHERE personID=?";
		params = new Object[] {new Integer(personID)};
		types = new int[] {Types.INTEGER};
		
		Map resultMap = null;
		try {
			resultMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonInformation throw exception.");
		}
		return resultMap;
	}
	
	public int getPersonState(int personID) throws WorkflowTransactionException {
		sql = "SELECT state FROM PersonInformation  WHERE personID=?";
		params = new Object[] {new Integer(personID)};
		types = new int[] {Types.INTEGER};
		
		Map resultMap = null;
		int state = -1;
		try {
			resultMap = (Map) executeQuery(new MapHandler());
			state = (Integer)resultMap.get("state");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonInformation throw exception.");
		}
		return state;
	}
	
	/**
	 * 得到角色所分配的人员信息列表
	 * @param roleID 角色ID
	 * @return 人员信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getPersonInformationList(int roleID) throws WorkflowTransactionException {
		sql = "SELECT pi.* FROM PersonInformation pi LEFT JOIN PersonRole pr ON pi.PersonID = pr.PersonID WHERE pr.RoleID = ?";
		params = new Object[] { new Integer(roleID) };
		types = new int[] { Types.INTEGER };
		
		List resultList = null;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonIDSet throw exception.");
		}
		
		return resultList;
	}
	
	public void setPersonState(int personID, int state) throws WorkflowTransactionException {
		sql = "UPDATE PersonInformation SET state = ? WHERE personID = ?";
		params = new Object[] { new Integer(state), new Integer(personID) };
		types = new int[] { Types.INTEGER, Types.INTEGER };
		
		try {
			executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getPersonIDSet throw exception.");
		}
	}
	
	
	/**
	 * 根据角色得到工作流活动所分配的人员ID数组
	 * @param workflowID 工作流ID
	 * @param activityID 活动ID
	 * @param roleID 角色ID
	 * @return 人员数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getWorkflowActivityPersonIDSet(int workflowID, int activityID, int roleID) throws WorkflowTransactionException {
		sql = "SELECT pi.personID FROM PersonInformation pi LEFT JOIN PersonRole pr ON pi.personID = pr.personID LEFT JOIN WorkflowActivityRole war ON pr.roleID = war.roleID WHERE war.workflowID = ? AND war.activityID = ? AND war.roleID = ?";
		params = new Object[] { new Integer(workflowID), new Integer(activityID), new Integer(roleID) };
		types = new int[] { Types.INTEGER, Types.INTEGER, Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("personID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getWorkflowActivityPersonIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	/**
	 * 根据角色得到流程活动所分配的人员ID数组
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param roleID 角色ID
	 * @return 人员数组
	 * @throws WorkflowTransactionException
	 */
	public int[] getProcessActivityPersonIDSet(String processID, int activityID, int roleID) throws WorkflowTransactionException {
		sql = "SELECT pi.personID FROM PersonInformation pi LEFT JOIN PersonRole pr ON pi.personID = pr.personID LEFT JOIN ProcessActivityRole par ON pr.roleID = par.roleID WHERE par.processID = ? AND par.activityID = ? AND par.roleID = ?";
		params = new Object[] { processID, new Integer(activityID), new Integer(roleID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		
		List resultList = null;
		int size = 0;
		int[] resultSet;
		
		try {
			resultList = (List) executeQuery(new MapListHandler());
			size = resultList.size();
			resultSet = new int[size];
			for (int i = 0; i < size; ++i) {
				Map map = (Map)resultList.get(i);
				resultSet[i] = (Integer)(map.get("personID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("PersonRoleDAO.getProcessActivityPersonIDSet throw exception.");
		}
		
		return resultSet;
	}
	
	
	
	
	
	/**
	 * 2008.3
	 * 根据角色得到工作流某活动的人员信息列表
	 * @param workflowID 工作流ID
	 * @param activityID 活动ID
	 * @param roleID 角色ID
	 * @return 人员信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getWorkflowPersonList(int workflowID, int activityID, int roleID) throws WorkflowTransactionException {
		sql = "select pi.* from PersonInformation pi left join PersonRole pr on pi.personID = pr.personID left join WorkflowActivityRole war on pr.roleID = war.roleID where war.workflowID = ? and war.activityID = ? and war.roleID = ?";
		params = new Object[] { new Integer(workflowID), new Integer(activityID), new Integer(roleID) };
		types = new int[] { Types.INTEGER, Types.INTEGER, Types.INTEGER };
		
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("PersonRoleDAO.getWorkflowPersonList throw exception.");
		}
		return resultList;
	}

	/**
	 * 2008.3
	 * 根据角色得到流程某活动的人员信息列表
	 * @param processID 流程ID
	 * @param activityID 活动ID
	 * @param roleID 角色ID
	 * @return 人员信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getProcessPersonList(String processID, int activityID, int roleID) throws WorkflowTransactionException {
		sql = "select pi.* from PersonInformation pi left join PersonRole pr on pi.personID = pr.personID left join ProcessActivityRole par on pr.roleID = par.roleID where par.processID = ? and par.activityID = ? and par.roleID = ?";
		params = new Object[] { processID, new Integer(activityID), new Integer(roleID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException("PersonRoleDAO.getProcessPersonList throw exception.");
		}
		return resultList;
	}
	
	public void addPersonRole(int personID, int roleID) throws SQLException {
		sql = "INSERT INTO PersonRole (PersonID, RoleID) VALUES(?, ?)";
		params = new Object[] { new Integer(personID), 
								new Integer(roleID) };
		types = new int[] { Types.INTEGER, 
							Types.INTEGER };
		executeUpdate();
	}
	public void removePersonRole(int personID, int roleID) throws SQLException {
		sql = "DELETE FROM PersonRole WHERE PersonID = ? AND RoleID = ?";
		params = new Object[] { new Integer(personID), 
								new Integer(roleID) };
		types = new int[] { Types.INTEGER, 
							Types.INTEGER };
		executeUpdate();
	}
}
