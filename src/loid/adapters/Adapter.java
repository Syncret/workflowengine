/*
 * Created on 2005-1-5
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger; // import org.dom4j.io.DOMReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import cit.workflow.utils.*;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class Adapter {
	protected int applicationId = 0; // 活动对应的ApplicationId

	protected String methodName; // 方法名

	protected String methods[]; // 可选的所有方法

	protected int methodNum = 0; // 可选的方法数目

	protected int methodNo = 0; //所选方法的序号

	protected String userName; //用户名

	protected String password; // 密码

	//Adapter的输入输出端口
	protected AdapterIOPort[] inPorts;

	protected AdapterIOPort[] outPorts;

	//Adapter的输入输出端口数目
	protected int inNum = 0;

	protected int outNum = 0;

	//方便DOM解析的输入输出端口指针
	protected int inPointer = 0;

	protected int outPointer = 0;

	//输入输出端口的XML字符串格式(不含值)
	protected String inputSchema = "";

	protected String outputSchema = "";

	// 输入输出端口的XML字符串格式(含值)
	/*
	 * protected String inputXML = ""; protected String outputXML = "";
	 */

	protected Document inputXMLDoc = null;

	protected Document outputXMLDoc = null;

	protected String returnXML = "";

	protected Document returnXMLDoc = null;


	protected Connection connection = null;

	private XMLOperation xmlOperation = null;
	
//	是否显示属性设置对话框的标志(以区别建模调用还是引擎调用)
	protected boolean displayFlag=false;


	protected final Logger logger = Logger.getLogger(this.getClass());

	public Adapter() {
		xmlOperation = new XMLOperation();
	}

	/***************************************************************************
	 * Access DataBase
	 **************************************************************************/
	protected void getConnection() {
		try {
			// Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			// connection = DriverManager.getConnection(dbURL, "sa", "");
			// Class.forName("com.mysql.jdbc.Driver");
			// connection = DriverManager.getConnection(dbURL, "dingo",
			// "dingo");

			connection = WorkflowConnectionPool.getInstance().getConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}// end-of getConnection

	protected void closeConnection() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end-of closeConnection

	/**
	 * 
	 * @param tableName
	 * @param columnName
	 * @param maxID
	 * @return
	 */
	protected int getID(String tableName, String columnName, int maxID) {
		int ID = 0, currentID = 0;
		String connstr = "";
		PreparedStatement pstmt = null;
		try {
			connstr = "select MAX(" + columnName + ") from " + tableName;
			pstmt = connection.prepareStatement(connstr);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				currentID = rs.getInt(1);
				rs.close();
				if (currentID < maxID) {
					ID = currentID + 1;
				} else {
					int i = 0;
					connstr = "select " + columnName + " from " + tableName;
					pstmt = connection.prepareStatement(connstr);
					rs = pstmt.executeQuery();
					while (rs.next()) {
						i++;
						currentID = rs.getInt(1);
						if (i < currentID) {
							ID = i;
							break;
						}
					}
					rs.close();
				}
			}
			pstmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ID;
	}// end-of getID

	/**
	 * 
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	protected Object getObject(String type, String value) {
		Object obj = null;
		if ((type.equalsIgnoreCase("int")) || (type.equalsIgnoreCase("java.lang.Integer"))) {
			
			if (value.equals("")){
				obj = new Integer(0);
			}
			else
				obj = new Integer(value);
		} else if ((type.equalsIgnoreCase("double")) || (type.equalsIgnoreCase("java.lang.Double"))) {
			if (value.equals(""))
				obj = new Double(0);
			else
				obj = new Double(value);
		} else if ((type.equalsIgnoreCase("float")) || (type.equalsIgnoreCase("java.lang.Float"))) {
			if (value.equals(""))
				obj = new Float(0);
			else
				obj = new Float(value);
		} else if ((type.equalsIgnoreCase("string")) || (type.equalsIgnoreCase("java.lang.String"))) {
			obj = new String(value);
		} else if ((type.equalsIgnoreCase("long")) || (type.equalsIgnoreCase("java.lang.Long"))) {
			if (value.equals(""))
				obj = new Long(0);
			else
				obj = new Long(value);
		} else if ((type.equalsIgnoreCase("boolean")) || (type.equalsIgnoreCase("java.lang.Boolean"))) {
			obj = new Boolean(value);
		} else if (type.equalsIgnoreCase("{int}")) {

		} else if (type.equalsIgnoreCase("{double}")) {

		} else if (type.equalsIgnoreCase("{float}")) {

		} else if (type.equalsIgnoreCase("{string}")) {

		} else if (type.equalsIgnoreCase("{long}")) {

		} else if (type.equalsIgnoreCase("{boolean}")) {

		}
		return (obj);
	}// end-of getObject

	/***************************************************************************
	 * At this point, all data in the XML String has been parsed and loaded into
	 * memory in the form of a DOM Document object. The Document is a tree of
	 * Node objects. This getInfoFromNode() method simply recurses through a
	 * Node tree and gets info from each node.
	 **************************************************************************/
	protected void initAdapterInput(Document inputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
	}

	protected void initAdapterOutput(Document outputXMLDoc) {
		this.outputXMLDoc = outputXMLDoc;
	}

	protected abstract void configureAdapter();
}
