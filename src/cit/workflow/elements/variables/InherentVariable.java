/*
 * Created on 2004-12-22
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.variables;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.Constants;
import cit.workflow.exception.WorkflowTransactionException;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InherentVariable extends ProcessObject {
	
	public InherentVariable(){}//dingo 8.2
	public InherentVariable(Connection conn, String processID, int objectID) {
		super(conn, processID, objectID);

		//sxh add 2007
		this.objectType = Constants.OBJECT_TYPE_INHERENT;
		//sxh add 2007 end
	}
	
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.variables.ProcessObject#getValue()
	 */
	public Object getValue() throws Exception {
		sql = "SELECT ValueType, Value FROM ProcessInherentVariable WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map processInherentVariableMap = (Map) executeQuery(new MapHandler());
		if (processInherentVariableMap != null) {
			int valueType = ((Integer) processInherentVariableMap.get("ValueType")).intValue();
			
//			if (valueType == Constants.DATA_TYPE_STRING)
//				return "'" + (String) processInherentVariableMap.get("Value") + "'";
				
			return (String) processInherentVariableMap.get("Value");
		}
		else
			throw new WorkflowTransactionException();
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.variables.ProcessObject#setValue(java.lang.Object)
	 */
	public void changeObjectValue(Object value) throws Exception {
		sql = "UPDATE ProcessInherentVariable SET Value=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] {value, processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
		executeUpdate();
	}
}
