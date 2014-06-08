
/*
 * Created on 2011-07
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.variables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import cit.workflow.Constants;
import cit.workflow.exception.WorkflowTransactionException;

/**
 * @author cyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ArrayVariable extends ProcessObject {
	
	public ArrayVariable() {}//dingo 8.2
	
	public ArrayVariable(Connection conn, String processID, int objectID) {
		super(conn, processID, objectID);

		this.objectType = Constants.OBJECT_TYPE_ARRAY;

	}
	
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.variables.ProcessObject#getValue()
	 */
	public Object getValue() throws Exception {
		sql = "SELECT XML FROM ProcessDocument WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map pxmldMap = (Map) executeQuery(new MapHandler());
		return pxmldMap.get("XML");
	}
	

	//	sxh add 2007
	public Object getPath() throws Exception {
		sql = "SELECT Path FROM ProcessDocument WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map pxmldMap = (Map) executeQuery(new MapHandler());
		return pxmldMap.get("PATH");
	}
	//sxh add 2007 end
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.variables.ProcessObject#setValue(java.lang.Object)
	 */
	public void changeObjectValue(Object value) throws Exception {
		sql = "UPDATE ProcessDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] {value, processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
		executeUpdate();
		
		sql = "SELECT XPATH from ProcessObjectVariable where processID = ? and fromObjectID = ?";
		params = new Object[] {processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map objectMap = (Map) executeQuery(new MapHandler());
		if (objectMap != null) {
			Document document = DocumentHelper.parseText((String)value);
			Node node = document.selectSingleNode((String) objectMap.get("XPATH"));
			//String objectValue = node.getText();
			
			sql = "UPDATE ProcessObjectVariable SET Value=? WHERE ProcessID=? AND fromObjectID=?";
			params = new Object[] { node.getText(), processID, objectID };
			types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
			executeUpdate();
		} else {
			//throw new WorkflowTransactionException();
		}
	}
}
