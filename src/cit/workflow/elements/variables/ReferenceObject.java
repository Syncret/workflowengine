package cit.workflow.elements.variables;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;

import cit.workflow.Constants;
import cit.workflow.elements.factories.ElementFactory;

public class ReferenceObject extends ProcessObject {

	public ReferenceObject() {}//dingo 8.2
	public ReferenceObject(Connection conn, String processID, int objectID) {
		super(conn, processID, objectID);

		//sxh add 2007
		this.objectType = Constants.OBJECT_TYPE_REF;
		//sxh add 2007 end
	}

	public void changeObjectValue(Object value) throws Exception {
		sql = "SELECT * FROM ProcessReferenceVariable WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map prvMap = (Map) executeQuery(new MapHandler());
		
		String parentProcessID = (String) prvMap.get("ParentProcessID");
		int parentObjectID = ((Integer) prvMap.get("ParentObjectID")).intValue();
		
		ProcessObject parentObject = ElementFactory.createProcessObject(conn, parentProcessID, parentObjectID);
		parentObject.setValue(value);
	}

	public Object getValue() throws Exception {
		sql = "SELECT * FROM ProcessReferenceVariable WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{processID, new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		Map prvMap = (Map) executeQuery(new MapHandler());
		
		String parentProcessID = (String) prvMap.get("ParentProcessID");
		int parentObjectID = ((Integer) prvMap.get("ParentObjectID")).intValue();
		
		ProcessObject parentObject = ElementFactory.createProcessObject(conn, parentProcessID, parentObjectID);
		return parentObject.getValue();
	}

}
