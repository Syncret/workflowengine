package daemon;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cit.workflow.WorkflowPublicManager;
import cit.workflow.WorkflowServer;
import cit.workflow.dao.DelegationDAO;
import cit.workflow.dao.PersonRoleDAO;
import cit.workflow.utils.WorkflowConnectionPool;

public class DelegationDaemon implements Runnable {
	
	private int delegatingRoleID;
	private int delegatedPersonID;
	private Date startTime;
	private Date definedEndTime;
	
	private int id;
	private int delegatingPersonID;
	
	public DelegationDaemon(int delegatingRoleID, int delegatedPersonID, Date startTime, Date definedEndTime) {
		this.delegatingRoleID = delegatingRoleID;
		this.delegatedPersonID = delegatedPersonID;
		this.startTime = startTime;
		this.definedEndTime = definedEndTime;
	}

	public void run() {
		// TODO Auto-generated method stub
		/*try {
			initDelegation();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		try {
			//servercomment System.out.println("Before Delegating Daemon");
			while (beforeStart()) {
				//Wait until delegation starts
				Thread.sleep(1000);
			}
			//servercomment System.out.println("Start Delegating Daemon");
			addPersonRole();
			while (delegating()) {
				Thread.sleep(100);
			}
			removePersonRole();
			//servercomment System.out.println("End Delegating Daemon");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			//Never expected
			e.printStackTrace();
		} 
	}
	
	private boolean beforeStart() {
		Date curDate = new Date();
		if (curDate.before(startTime)) {		
			return true;
		}
		return false;
	}
	
	/*private void initDelegation() throws SQLException {
		
		//Delegating person doesn't exist
		if (null == pIDSet || pIDSet.length == 0) {
			throw new RemoteException("Exception in DelegationDaemon, delegating person doesn't exist");
		}
		delegatingPersonID = pIDSet[0];
	}*/
	
	/*private int getDelegatingPersonID() {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		PersonRoleDAO prDAO = new PersonRoleDAO(conn);
		int[] pIDSet = null;
		pIDSet = prDAO.getPersonIDSet(delegatingRoleID);
	}*/
	
	
	private boolean delegating() {
		int id = -1;
		try {
			id = getDelegateInformationID();
			if (-1 == id) {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Date curDate = new Date();
		
		if (curDate.before(startTime)) {
			//Never expected
			return false;
		}
		if (curDate.after(definedEndTime)) {
			//Out of valid period
			setActualEndTime(id);
			return false;
		}
		return true;
	}
	
	private int getDelegateInformationID() throws SQLException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		List diList = delegationDAO.getDelegationInformation(delegatingRoleID, delegatedPersonID);
		if (null == diList || diList.size()!= 1) {
			//Information error
			return -1;
		}
		int id = (Integer)(((Map)diList.get(0)).get("Id"));
		return id;
	}
	
	private void setActualEndTime(int id) {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		DelegationDAO delegationDAO = new DelegationDAO(conn);
		try {
			delegationDAO.setActualEndTime(id, new Date());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addPersonRole() throws SQLException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		PersonRoleDAO prDAO = new PersonRoleDAO(conn);
		try {
			prDAO.addPersonRole(delegatedPersonID, delegatingRoleID);
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.close();
		}
	}
	
	private void removePersonRole() throws SQLException {
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		PersonRoleDAO prDAO = new PersonRoleDAO(conn);
		try {
			prDAO.removePersonRole(delegatedPersonID, delegatingRoleID);
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.close();
		}
	}
}
