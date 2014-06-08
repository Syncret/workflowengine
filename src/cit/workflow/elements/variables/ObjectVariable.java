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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import cit.workflow.Constants;
import cit.workflow.exception.WorkflowTransactionException;

/**
 * @author weiwei
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ObjectVariable extends ProcessObject {

	public ObjectVariable() {
	}// dingo 8.2

	public ObjectVariable(Connection conn, String processID, int objectID) {
		super(conn, processID, objectID);

		// sxh add 2007
		this.objectType = Constants.OBJECT_TYPE_OBJECT;
		// sxh add 2007 end
	}

	// sxh add 2007.9
	private int getFromObjectID() throws Exception {
		sql = "select fromObjectID from processObjectVariable where processID = ? and objectID = ?";
		params = new Object[] { processID, objectID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processObjectVariableMap = (Map) executeQuery(new MapHandler());
		int fromObjectID = (Integer) processObjectVariableMap
				.get("fromObjectID");
		return fromObjectID;
	}

	// sxh add 2007.9 end

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.variables.ProcessObject#getValue()
	 */
	// sxh modified 2007.9
	public Object getValue() throws Exception {
		sql = "SELECT value from processObjectVariable where processID = ? and objectID = ?";
		params = new Object[] { processID, new Integer(objectID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		Map processObjectVariableMap = (Map) executeQuery(new MapHandler());
		if (processObjectVariableMap != null) {
			return processObjectVariableMap.get("value");
		} else
			throw new WorkflowTransactionException();

		/*
		 * sql = "SELECT POV.ValueType, POV.XPath, PXD.XML FROM
		 * ProcessObjectVariable POV LEFT JOIN ProcessXMLDocument PXD ON
		 * PXD.ProcessID=POV.ProcessID AND PXD.ObjectID=POV.FromObjectID WHERE
		 * POV.ProcessID=? AND POV.ObjectID=?"; params = new Object[]{processID,
		 * new Integer(objectID)}; types = new int[] {Types.VARCHAR,
		 * Types.INTEGER};
		 * 
		 * 
		 * Map processObjectVariableMap = (Map) executeQuery(new MapHandler());
		 * if (processObjectVariableMap != null) { Document document =
		 * DocumentHelper.parseText((String)
		 * processObjectVariableMap.get("XML")); Node node =
		 * document.selectSingleNode((String)
		 * processObjectVariableMap.get("XPath")); String objectValue =
		 * node.getText();
		 *  // int valueType = ((Integer)
		 * processObjectVariableMap.get("ValueType")).intValue(); // //// if
		 * (valueType == Constants.DATA_TYPE_STRING) //// return "'" +
		 * objectValue + "'";
		 * 
		 * return objectValue; } else throw new WorkflowTransactionException();
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cit.workflow.elements.variables.ProcessObject#setValue(java.lang.Object)
	 */
	public void changeObjectValue(Object value) throws Exception {

		Map processObjectMap;
		int fromObjectID;
		int fromObjectType;

		// update objectVariable
		sql = "update processObjectVariable set value = ? where processid = ? and objectid = ?";
		params = new Object[] { value.toString(), processID, objectID };
		types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
		executeUpdate();

		// get fromObject Type
		fromObjectID = getFromObjectID();

		sql = "SELECT * from processObject where processID = ? and objectID = ?";
		params = new Object[] { processID, fromObjectID };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		processObjectMap = (Map) executeQuery(new MapHandler());

		if (processObjectMap != null) {
			fromObjectType = (Integer) processObjectMap.get("ObjectType");

			if (fromObjectType == Constants.OBJECT_TYPE_XML) { // XML Document

				sql = "SELECT POV.FromObjectID, POV.XPath, PXD.XML FROM ProcessObjectVariable POV LEFT JOIN ProcessXMLDocument PXD ON PXD.ProcessID=POV.ProcessID AND PXD.ObjectID=POV.FromObjectID WHERE POV.ProcessID=? AND POV.ObjectID=?";
				params = new Object[] { processID, new Integer(objectID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				Map processXMLDocumentMap = (Map) executeQuery(new MapHandler());
				if (processXMLDocumentMap != null) {
					Document document = DocumentHelper
							.parseText((String) processXMLDocumentMap
									.get("XML"));
					Node node = document
							.selectSingleNode((String) processXMLDocumentMap
									.get("XPath"));
					node.setText((String) value);
					

					sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
					params = new Object[] { document.asXML(), processID,
							fromObjectID };
					types = new int[] { Types.VARCHAR, Types.VARCHAR,
							Types.INTEGER };
					executeUpdate();
				} else {
					throw new WorkflowTransactionException();
				}

			} else if (fromObjectID == Constants.OBJECT_TYPE_DOC) { // Document
				sql = "SELECT POV.FromObjectID, POV.XPath, PXD.XML FROM ProcessObjectVariable POV LEFT JOIN ProcessDocument PD ON PD.ProcessID=POV.ProcessID AND PD.ObjectID=POV.FromObjectID WHERE POV.ProcessID=? AND POV.ObjectID=?";
				params = new Object[] { processID, new Integer(objectID) };
				types = new int[] { Types.VARCHAR, Types.INTEGER };
				Map processXMLDocumentMap = (Map) executeQuery(new MapHandler());
				if (processXMLDocumentMap != null) {
					Document document = DocumentHelper
							.parseText((String) processXMLDocumentMap
									.get("XML"));
					Node node = document
							.selectSingleNode((String) processXMLDocumentMap
									.get("XPath"));
					node.setText((String) value);

					sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
					params = new Object[] { document.asXML(), processID,
							fromObjectID };
					types = new int[] { Types.VARCHAR, Types.VARCHAR,
							Types.INTEGER };
					executeUpdate();
				} else {
					throw new WorkflowTransactionException();
				}
			}
		}

		/*
		 * sql = "SELECT POV.FromObjectID, POV.XPath, PXD.XML FROM
		 * ProcessObjectVariable POV LEFT JOIN ProcessXMLDocument PXD ON
		 * PXD.ProcessID=POV.ProcessID AND PXD.ObjectID=POV.FromObjectID WHERE
		 * POV.ProcessID=? AND POV.ObjectID=?"; params = new Object[]{processID,
		 * new Integer(objectID)}; types = new int[] {Types.VARCHAR,
		 * Types.INTEGER}; processObjectVariableMap = (Map) executeQuery(new
		 * MapHandler()); if (processObjectVariableMap != null) { Document
		 * document = DocumentHelper.parseText((String)
		 * processObjectVariableMap.get("XML")); Node node =
		 * document.selectSingleNode((String)
		 * processObjectVariableMap.get("XPath")); node.setText((String) value);
		 * 
		 * sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND
		 * ObjectID=?"; params = new Object[] {document.asXML(), processID,
		 * processObjectVariableMap.get("FromObjectID")}; types = new int[]
		 * {Types.VARCHAR, Types.VARCHAR, Types.INTEGER}; executeUpdate(); }
		 * else throw new WorkflowTransactionException();
		 */
	}
	// sxh modified 2007.9 end
}
