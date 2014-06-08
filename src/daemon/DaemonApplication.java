package daemon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cit.workflow.dao.DelegationDAO;
import cit.workflow.utils.WorkflowConnectionPool;

public class DaemonApplication {
	public boolean delegation(int delegatingRoleID, int delegatedPersonID, Date startTime, Date definedEndTime) {
		if (!checkParameter(delegatingRoleID, delegatedPersonID, startTime, definedEndTime)) {
			//parameter error
			return false;
		}
		try {
			addDelegationInformation(delegatingRoleID, delegatedPersonID, startTime, definedEndTime);
		} catch (SQLException e) {
			//failed to delegate when adding delegation information to DB
			e.printStackTrace();
			return false;
		}
		Thread delegationThread = new Thread(new DelegationDaemon(delegatingRoleID, delegatedPersonID, startTime, definedEndTime));
		delegationThread.start();
		return true;
	}
	
	public boolean endDelegation(int delegatingRoleID, int delegatedPersonID) {
		List diList = getDelegationInformation(delegatingRoleID, delegatedPersonID);
		if (null == diList || diList.size()!= 1) {
			//Information error
			return false;
		}
		int id = (Integer)(((Map)diList.get(0)).get("Id"));
		setActualEndTime(id, new Date());
		return true;
		
	}
	
	public int getCurrentDelegatedPersonID(int delegatingRoleID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		List resultList = null;
		int personID = -1;
		try {
			resultList = delegationDAO.getCurrentDelegatedPersonID(delegatingRoleID);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null == resultList && resultList.size() != 1) {
			return -1;
		}
		personID = (Integer)((Map)resultList.get(0)).get("delegatedPersonID");
		return personID;
	}
	
	private boolean checkParameter(int delegatingRoleID, int delegatedPersonID, Date startTime, Date endTime) {
		// TODO Add parameter checking logic
		return true;
	}
	
	private void addDelegationInformation(int delegatingRoleID, int delegatedPersonID, Date startTime, Date definedEndTime) throws SQLException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		try {
			delegationDAO.addDelegationInformation(delegatingRoleID, delegatedPersonID, startTime, definedEndTime, null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List getDelegationInformation(int delegatingRoleID, int delegatdPersonID) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		List resultList = null;
		try {
			resultList = delegationDAO.getDelegationInformation(delegatingRoleID, delegatdPersonID);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultList;
	}
	
	private void setActualEndTime(int id, Date actualEndTime) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		try {
			delegationDAO.setActualEndTime(id, actualEndTime);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
