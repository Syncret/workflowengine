/*
 * Created on 2005-3-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;
import cit.workflow.Constants;
import cit.workflow.elements.*;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.InherentVariable;
import cit.workflow.elements.variables.ObjectVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.elements.variables.XMLVariable;

/**
 * @author weiwei
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WorkflowDAO extends DBUtility {

	public WorkflowDAO(Connection conn) {
		super(conn);
	}

	

	/**
	 * 功能：根据用户ID选择该用户能够进行相应操作的模型集合
	 * 
	 * @param userID
	 *            用户的ID
	 * @param functionType
	 *            见表：WorkflowResponsibleRoles中的FunctionType
	 * @return 工作流模型集合
	 */
	/*
	 * public List getWorkflow(int userID, int functionType) throws
	 * WorkflowTransactionException { sql = "SELECT wi.*, pi.PackageID,
	 * pi.PackageName, pi.Description as PackageDescription, pi.DurationUnit,
	 * pi.CostUnit, pi. AuthorID, pi.CreateDate as PackageCreateDate FROM
	 * WorkflowInformation wi LEFT JOIN PackageInformation pi ON
	 * pi.PackageID=wi.PackageID LEFT JOIN WorkflowResponsibleRoles wrr ON
	 * wrr.WorkflowID=wi.WorkflowID LEFT JOIN PersonRole pr ON
	 * pr.RoleID=wrr.RoleID WHERE pr.PersonID=? AND wrr.FunctionType=?"; // sql =
	 * "SELECT wi.*, pi.* FROM WorkflowInformation wi LEFT JOIN //
	 * PackageInformation pi ON pi.PackageID=wi.PackageID LEFT JOIN //
	 * WorkflowResponsibleRoles wrr ON wrr.WorkflowID=wi.WorkflowID LEFT // JOIN
	 * PersonRole pr ON pr.RoleID=wrr.RoleID WHERE pr.PersonID=? AND //
	 * wrr.FunctionType=?"; params = new Object[] { new Integer(userID), new
	 * Integer(functionType) }; types = new int[] { Types.INTEGER, Types.INTEGER };
	 * List resultList = null; try { resultList = (List) executeQuery(new
	 * MapListHandler());
	 *  } catch (SQLException e) { throw new WorkflowTransactionException(
	 * "WorkflowDAO.getWorkflow throw exception."); } return resultList; }
	 */

	public List getWorkflow(int roleID, int functionType)
			throws WorkflowTransactionException {
		sql = "SELECT wi.*, pi.PackageID, pi.PackageName, pi.Description as PackageDescription, pi.DurationUnit, pi.CostUnit, pi. AuthorID, pi.CreateDate as PackageCreateDate FROM WorkflowInformation wi LEFT JOIN PackageInformation pi ON pi.PackageID=wi.PackageID LEFT JOIN WorkflowResponsibleRoles wrr ON wrr.WorkflowID=wi.WorkflowID WHERE wrr.RoleID = ? AND wrr.FunctionType = ?";
		params = new Object[] { new Integer(roleID), new Integer(functionType) };
		types = new int[] { Types.INTEGER, Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());

		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowDAO.getWorkflow throw exception.");
		}
		return resultList;
	}
	
	/**
	 * 根据工作流ID得到该工作流的子工作流
	 * @param workflowID 工作流ID
	 * @return 子工作流信息列表
	 * @throws WorkflowTransactionException
	 */
	public List getSubWorkflow(int workflowID)	throws WorkflowTransactionException {
		sql = "SELECT wi.*, pi.PackageID, pi.PackageName, pi.Description as PackageDescription, pi.DurationUnit, pi.CostUnit, pi. AuthorID, pi.CreateDate as PackageCreateDate FROM WorkflowInformation wi LEFT JOIN PackageInformation pi ON pi.PackageID=wi.PackageID WHERE wi.parentWorkflowID = ?";
		params = new Object[] { new Integer(workflowID) };
		types = new int[] { Types.INTEGER, Types.INTEGER };
		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
				"WorkflowDAO.getWorkflow throw exception.");
		}
		return resultList;
	}



	public List getWorkflowObject(Map paramMap)
			throws WorkflowTransactionException {
		StringBuffer sqlBuffer = new StringBuffer(
				"SELECT * FROM WorkflowObject WHERE 1=1 ");
		List paramList = new ArrayList();
		Set keySet = paramMap.entrySet();
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			sqlBuffer.append("AND ").append((String) entry.getKey()).append(
					"=? ");
			paramList.add(entry.getValue());
		}

		sql = sqlBuffer.toString();
		params = paramList.toArray();
		types = null;

		List resultList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getWorkflowObject throw exception.");
		}
		return resultList;
	}

	public double getRequestLoad(String processID, String eventExpression)
			throws WorkflowTransactionException {
		sql = "SELECT * FROM WorkflowEventLoad WHERE EventExpression=? AND WorkflowID IN (SELECT WorkflowID FROM CaseInformation WHERE ProcessID=?)";
		params = new Object[] { eventExpression, processID };
		types = new int[] { Types.VARCHAR, Types.VARCHAR };

		Map workflowEventLoadMap = null;
		try {
			workflowEventLoadMap = (Map) executeQuery(new MapHandler());
		} catch (SQLException e) {
			throw new WorkflowTransactionException(
					"WorkflowObjectDAO.getRequestLoad throw exception.");
		}

		if (workflowEventLoadMap != null)
			return ((Double) workflowEventLoadMap.get("EventLoad"))
					.doubleValue();
		else
			return 0.0;
	}

	
	


}
