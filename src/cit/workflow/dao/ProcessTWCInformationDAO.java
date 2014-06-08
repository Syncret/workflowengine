package cit.workflow.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;

public class ProcessTWCInformationDAO  extends DBUtility {
	
	public ProcessTWCInformationDAO(Connection conn){
		super(conn);
	}
	
	public boolean addProcessDate(String processID) throws WorkflowTransactionException {
		/*sql = "SELECT MostPossibleDuration FROM ProcessInformation WHERE processID = ?";
		params = new Object[] {processID};
		types = new int[] {Types.VARCHAR};
		int duration = 0;
		try {
			Map resultMap = (Map)executeQuery(new MapHandler());
			duration = (Integer)resultMap.get("MostPossibleDuration");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date startDate = new Date();
		Date definedEndDate = new Date();
		definedEndDate.setTime(definedEndDate.getTime() + duration * 1000 * 60 * 60 * 24);
		sql = "INSERT INTO ProcessTWCInformation VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		params = new Object[] {processID, 0, 0, null, new Timestamp(System.currentTimeMillis()), new Timestamp(definedEndDate.getTime()), null, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.FLOAT, Types.TIMESTAMP, Types.TIMESTAMP, Types.TIMESTAMP, Types.TIMESTAMP, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.INTEGER, Types.INTEGER};
		try {
			executeUpdate();
		} catch (SQLException e) {
			throw new WorkflowTransactionException("ProcessTWCInformationDAO.addProcessDate throw exception.");
		}*/
		return true;
	}

}
