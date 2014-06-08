package cit.workflow.engine.droolsruleengine;

// lrj add begin 07-11-7
import java.rmi.RemoteException;
import java.sql.Connection;

import cit.workflow.dao.PersonIDDAO;
import cit.workflow.utils.WorkflowConnectionPool;


public class PersonID 
{
	public PersonID(int ActivityID,String ProcessID)
	{
		this.MyActivityID = ActivityID;
		this.MyProcessID = ProcessID;
	}
	public int GetPersonIDFromProcessactivityperson() throws RemoteException
	{
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		try {
			this.MyPersonID = new PersonIDDAO(conn).getPersonID(this.MyProcessID,this.MyActivityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return this.MyPersonID;
	}
	
	public int GetPersonID()
	{
		return this.MyPersonID;
	}
	
	public String GetProcessID()
	{
		return this.MyProcessID;
	}
	
	public int GetActivityID()
	{
		return this.MyActivityID;
	}
	
	private void commitAction(Connection connection) {
		try {
			connection.commit();
			connection.close();
		} catch (Exception e) {
			System.err.println("Execution thread: "
					+ Thread.currentThread().getName()
					+ ", Connection commit error, message: " + e.getMessage());
		}
	}
	private String MyProcessID;
	private int MyActivityID;
	private int MyPersonID;
}

//lrj add end 07-11-7
