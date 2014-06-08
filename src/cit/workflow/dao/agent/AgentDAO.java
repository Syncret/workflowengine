package cit.workflow.dao.agent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.model.activities.ProcessActivityAgent;
import cit.workflow.utils.DBUtility;

public class AgentDAO extends DBUtility {
	public AgentDAO(Connection conn) {
		super(conn);
	}
	public List getAgentTaskList() throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessActivityAgent WHERE state=?";
		params = new Object[] {new String(Constants.PROCESS_STATE_RUNNING)};
		types = new int[] {Types.VARCHAR};
		List resultList = null;
		List agentTaskList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
			agentTaskList = new ArrayList();
			for (int i = 0; i < resultList.size(); ++i) {
				Map resultMap = (Map)resultList.get(i);
				ProcessActivityAgent activityAgent = new ProcessActivityAgent(resultMap);
				agentTaskList.add(activityAgent);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("ProcessDAO.getProcess throw exception.");
		}
		return agentTaskList;
	}
	
	public List getAgentTaskList(int agentID) throws WorkflowTransactionException {
		sql = "SELECT * FROM ProcessActivityAgent WHERE agentID = ? AND state=?";
		params = new Object[] {new Integer(agentID), new String(Constants.PROCESS_STATE_RUNNING)};
		types = new int[] {Types.INTEGER, Types.VARCHAR};
		List resultList = null;
		List agentTaskList = null;
		try {
			resultList = (List) executeQuery(new MapListHandler());
			agentTaskList = new ArrayList();
			for (int i = 0; i < resultList.size(); ++i) {
				Map resultMap = (Map)resultList.get(i);
				ProcessActivityAgent activityAgent = new ProcessActivityAgent(resultMap);
				agentTaskList.add(activityAgent);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WorkflowTransactionException("ProcessDAO.getProcess throw exception.");
		}
		return agentTaskList;
	}
}

