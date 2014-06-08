package test.cit.workflow.droolsengine;


//add lrj 11-8

import java.rmi.RemoteException;
import java.sql.Connection;

import cit.workflow.dao.PersonIDDAO;
import cit.workflow.utils.WorkflowConnectionPool;

public class PersonID 
{
	private String MyProcessID;
	private int MyActivityID;
	private int MyPersonID;

	public PersonID(int ActivityID,String ProcessID)
	{
		MyActivityID = ActivityID;
		MyProcessID = ProcessID;
	}
	public int GetPersonIDFromProcessactivityperson() throws RemoteException
	{
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		try {
			MyPersonID = new PersonIDDAO(conn).getPersonID(MyProcessID,MyActivityID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commitAction(conn);

		return MyPersonID;
	}
	
	public int GetPersonID()
	{
		return MyPersonID;
	}
	
	public String GetProcessID()
	{
		return MyProcessID;
	}
	
	public int GetActivityID()
	{
		return MyActivityID;
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

}
