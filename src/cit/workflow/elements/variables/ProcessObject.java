package cit.workflow.elements.variables;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.elements.applications.ApplicationInvoke;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ProcessObject extends DBUtility implements Serializable{
	
	protected String processID;
	protected int objectID;

	//sxh add 2007
	protected int objectType;
	//sxh add 2007 end
	
	//sxh add 2007.11
	protected int privilege;
	//sxh add 2007.11 end

	

	public ProcessObject() {}//dingo 8.2
	
	public ProcessObject(Connection conn, String processID, int objectID) {
		super(conn);
		
		this.processID = processID;
		this.objectID = objectID;
	}

	/**
	 * @return Returns the objectID.
	 */
	public int getObjectID() {
		return objectID;
	}
	
	//sxh add 2007
	/**
	 * @return Returns the objectType.
	 */
	public int getObjectType() {
		return objectType;
	}
	//sxh add 2007 end
	
	//sxh add 2007.11
	public int getPrivilege() {
		return privilege;
	}

	public void setPrivilege(int privilege) {
		this.privilege = privilege;
	}
	//sxh add 2007.11 end
	
	/**
	 * @return Returns the processID.
	 */
	public String getProcessID() {
		return processID;
	}
	
	public void setValue(Object value) throws Exception {
		changeObjectValue(value);
		
		synchronizedParentObjectValue(processID, objectID, value);
		
		synchronizedChildObjectValue(processID, objectID, value);
	}
	
	public abstract void changeObjectValue(Object value) throws Exception;
	public abstract Object getValue() throws Exception;
	
	protected void synchronizedParentObjectValue(String currentProcessID, int currentObjectID, Object value) throws Exception {
		//Synchronized parent's object value
		sql = "SELECT * FROM ProcessObjectMapping WHERE ProcessID=? AND ObjectID=? AND MappingRelation=?";
		params = new Object[]{currentProcessID, new Integer(currentObjectID), new Integer(0)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		List mappingObjectList = (List) executeQuery(new MapListHandler());
		Iterator iterator = mappingObjectList.iterator();
		while (iterator.hasNext()) {
			Map mappingObjectMap = (Map) iterator.next();
			ProcessObject mappingObject = ElementFactory.createProcessObject(conn, (String) mappingObjectMap.get("MappingProcessID"), ((Integer) mappingObjectMap.get("MappingObjectID")).intValue());
			mappingObject.changeObjectValue(value);
			
			synchronizedParentObjectValue((String) mappingObjectMap.get("MappingProcessID"), ((Integer) mappingObjectMap.get("MappingObjectID")).intValue(), value);
		}
	}
	
	protected void synchronizedChildObjectValue(String currentProcessID, int currentObjectID, Object value) throws Exception {
		//Synchronized child's object value
		sql = "SELECT * FROM ProcessObjectMapping WHERE ProcessID=? AND ObjectID=? AND MappingRelation=?";
		params = new Object[]{processID, new Integer(objectID), new Integer(1)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		List mappingObjectList = (List) executeQuery(new MapListHandler());
		Iterator iterator = mappingObjectList.iterator();
		while (iterator.hasNext()) {
			Map mappingObjectMap = (Map) iterator.next();
			ProcessObject mappingObject = ElementFactory.createProcessObject(conn, (String) mappingObjectMap.get("MappingProcessID"), ((Integer) mappingObjectMap.get("MappingObjectID")).intValue());
			mappingObject.changeObjectValue(value);
			
			synchronizedChildObjectValue((String) mappingObjectMap.get("MappingProcessID"), ((Integer) mappingObjectMap.get("MappingObjectID")).intValue(), value);
		}
	}
	
//	protected void changeMappingObjectListValue(List mappingObjectList) throws Exception {
//		Iterator iterator = mappingObjectList.iterator();
//		while (iterator.hasNext()) {
//			Map mappingObjectMap = (Map) iterator.next();
//			ProcessObject mappingObject = ElementFactory.createProcessObject(conn, (String) mappingObjectMap.get("MappingProcessID"), ((Integer) mappingObjectMap.get("MappingObjectID")).intValue());
//			mappingObject.setValue(getValue());
//		}
//	}
	/*
	public void setValue(String value) throws Exception {
		sql = "SELECT ObjectType FROM ProcessObject WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		Map processObjectMap = (Map) executeQuery(new MapHandler());
		if (processObjectMap != null) {
			int objectType = ((Integer) processObjectMap.get("objectType")).intValue();
			
			if (objectType == Constants.OBJECT_TYPE_INHERENT) {
				setInherentValue(value);
			} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
				setObjectValue(value);
			} else if (objectType == Constants.OBJECT_TYPE_XML) {
				setXMLValue(value);
			} else if (objectType == Constants.OBJECT_TYPE_DOC) {
				setDocumentValue(value);
			} else
				throw new WorkflowTransactionException("Object type is out of range!");
		}
		else
			throw new WorkflowTransactionException("There is no right data for object!");
	}
	
	public String getValue() throws Exception {
		sql = "SELECT ObjectType FROM ProcessObject WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		Map processObjectMap = (Map) executeQuery(new MapHandler());
		if (processObjectMap != null) {
			int objectType = ((Integer) processObjectMap.get("objectType")).intValue();
			
			if (objectType == Constants.OBJECT_TYPE_INHERENT) {
				return getInherentValue();
			} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
				return getObjectValue();
			} else if (objectType == Constants.OBJECT_TYPE_XML) {
				return null;
			} else if (objectType == Constants.OBJECT_TYPE_DOC) {
				return null;
			} else
				throw new WorkflowTransactionException("Object type is out of range!");
		}
		else
			throw new WorkflowTransactionException("There is no right data for object!");
	}
	
	private String getInherentValue() throws SQLException {
		sql = "SELECT ValueType, Value FROM ProcessInherentVariable WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		Map processInherentVariableMap = (Map) executeQuery(new MapHandler());
		if (processInherentVariableMap != null) {
			int valueType = ((Integer) processInherentVariableMap.get("ValueType")).intValue();
			
			if (valueType == Constants.DATA_TYPE_STRING)
				return "'" + (String) processInherentVariableMap.get("Value") + "'";
				
			return (String) processInherentVariableMap.get("Value");
		}
		else
			throw new WorkflowTransactionException();
	}
	
	private String getObjectValue() throws Exception {
		sql = "SELECT POV.ValueType, POV.XPath, PXD.XML FROM ProcessObjectVariable POV LEFT JOIN ProcessXMLDocument PXD ON PXD.ProcessID=POV.ProcessID AND PXD.ObjectID=POV.FromObjectID WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		Map processObjectVariableMap = (Map) executeQuery(new MapHandler());
		if (processObjectVariableMap != null) {
	        Document document = DocumentHelper.parseText((String) processObjectVariableMap.get("XML"));
	        Node node = document.selectSingleNode((String) processObjectVariableMap.get("XPath"));
	        String objectValue = node.getText();
			
			int valueType = ((Integer) processObjectVariableMap.get("ValueType")).intValue();
			
			if (valueType == Constants.DATA_TYPE_STRING)
				return "'" + objectValue + "'";
				
			return objectValue;
		}
		else
			throw new WorkflowTransactionException();
	}
	
	private void setInherentValue(String value) throws Exception {
		sql = "UPDATE ProcessInherentVariable SET Value=" + value + " WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] {new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		executeUpdate();
	}
	
	private void setObjectValue(String value) throws Exception {
		sql = "SELECT POV.FromObjectID, POV.XPath, PXD.XML FROM ProcessObjectVariable POV LEFT JOIN ProcessXMLDocument PXD ON PXD.ProcessID=POV.ProcessID AND PXD.ObjectID=POV.FromObjectID WHERE ProcessID=? AND ObjectID=?";
		params = new Object[]{new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.INTEGER, Types.INTEGER};
		Map processObjectVariableMap = (Map) executeQuery(new MapHandler());
		if (processObjectVariableMap != null) {
	        Document document = DocumentHelper.parseText((String) processObjectVariableMap.get("XML"));
	        Node node = document.selectSingleNode((String) processObjectVariableMap.get("XPath"));
	        node.setText(value);
			
			sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {document.asXML(), new Integer(processID), processObjectVariableMap.get("FromObjectID")};
			types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
			executeUpdate();
		}
		else
			throw new WorkflowTransactionException();
	}
	
	private void setXMLValue(String value) throws Exception {
		sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] {value, new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		executeUpdate();
	}
	
	private void setDocumentValue(String value) throws Exception {
		sql = "UPDATE ProcessDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] {value, new Integer(processID), new Integer(objectID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		executeUpdate();
	}*/
}
