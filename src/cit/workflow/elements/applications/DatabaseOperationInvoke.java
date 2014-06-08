/*
 * Created on 2009-3-7 by cxz
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.applications;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import loid.adapters.DatabaseAdapter;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLOperation;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DatabaseOperationInvoke extends DBUtility implements ApplicationInvoke {

	private String processID;
	
	private int applicationID;
	
	private Document inputDoc = null;

	private Document outputDoc = null;
	
	private XMLOperation xmlOperation = null;

	protected final Logger logger = Logger.getLogger(this.getClass());
	
	
	
	public DatabaseOperationInvoke(Connection conn, int applicationID) {
		super(conn);
		
		this.applicationID = applicationID;
	}

	/* (non-Javadoc)
	 * @see cit.workflow.elements.applications.ApplicationInvoke#invoke(java.lang.String)
	 */
/*	public String invoke(String inputXML) throws Exception {
		sql = "SELECT DriverName, DatabaseURL, PredefinedSQL, UserName, Password FROM DatabaseOperation WHERE ApplicationID=?";
		params = new Object[] {new Integer(applicationID)};
		types = new int[] {Types.INTEGER};
		Map saiMap = (Map) executeQuery(new MapHandler());
		if (saiMap == null)
			throw new Exception("There is no web service for invocation!");
		
		final Document document = DocumentHelper.createDocument();
		Element webServiceElement = document.addElement("DATABASE");
		
		Element drivernameElement = webServiceElement.addElement("DRIVERNAME");
		drivernameElement.addText((String) saiMap.get("DriverName"));
		
		Element databaseurlElement = webServiceElement.addElement("DATABASEURL");
		databaseurlElement.addText((String) saiMap.get("DatabaseURL"));
		
		Element sqlElement = webServiceElement.addElement("PREDEFINEDSQL");
		sqlElement.addText((String) saiMap.get("PredefinedSQL"));
		
		Document inputDocument = DocumentHelper.parseText(inputXML);
		Element inputElement = inputDocument.getRootElement();
		webServiceElement.add(inputElement);
		
		Element usernameElement = webServiceElement.addElement("USERNAME");
		usernameElement.addText((String) saiMap.get("UserName"));
		
		Element passwordElement = webServiceElement.addElement("PASSWORD");
		passwordElement.addText((String) saiMap.get("Password"));
		
		DatabaseAdapter adapter = new DatabaseAdapter(false);
		return adapter.getOutputXML(document.asXML(), applicationID);
	}
*/	
	
	public String invoke(String inputXML) throws Exception{
		inputDoc = getInputXMLDocument(inputXML);
		outputDoc = getOutputXMLDocument();
		DatabaseAdapter adapter = new DatabaseAdapter(processID, applicationID);
		
		logger.info("Input XML (before DatabaseAdapter): \n" + xmlOperation.toString(inputDoc));
		logger.info("Output XML (before DatabaseAdapter): \n" + xmlOperation.toString(outputDoc));
		
		return xmlOperation.toString(adapter.getOutputXML(inputDoc, outputDoc));
		
	}
	
	private Document getInputXMLDocument(String inputXML) throws Exception{
		sql = "SELECT DriverName, DatabaseURL, PredefinedSQL, UserName, Password " +
				"FROM DatabaseOperation WHERE ApplicationID=?";
		
		params = new Object[] { new Long(applicationID), };
		types = new int[] { Types.BIGINT, Types.VARCHAR };
		Map saiMap = (Map) executeQuery(new MapHandler());
		
		if(saiMap == null)
			throw new Exception("There is no Database class for invocation!");
		
		Element DatabaseElement = new Element("DATABASE");
		Document doc = new Document(DatabaseElement);
		
		Element DrivernameElement = new Element("DRIVERNAME");
		DrivernameElement.setText((String) saiMap.get("DriverName"));
		DatabaseElement.addContent(DrivernameElement);
		
		Element DatabaseURLElement = new Element("DATABASEURL");
		DatabaseURLElement.setText((String) saiMap.get("DatabaseURL"));
		DatabaseElement.addContent(DatabaseURLElement);
		
		Element SqlElement = new Element("PREDEFINEDSQL");
		SqlElement.setText((String) saiMap.get("PredefinedSQL"));
		DatabaseElement.addContent(SqlElement);
		
		Document inputDocument = xmlOperation.toDocument(inputXML);
		Element inputElement = inputDocument.getRootElement();
		inputElement = (Element) inputElement.clone();
		
		DatabaseElement.addContent(inputElement);
		
		return doc;
	}
	
	private Document getOutputXMLDocument() {
		Document doc = null;
		int outID = 0;
		
		try{
			sql = "select OutputXMLId from DatabaseOperation where ApplicationID=?";
			params = new Object[] { new Long(applicationID)};
			types = new int[] { Types.BIGINT };
			Map saiMap =(Map) executeQuery(new MapHandler());
			
			if(saiMap == null)
				throw new Exception("ResutSet is null!");
			
			outID = (Integer) saiMap.get("OutputXMLID");
			logger.info("OutputXMLID: " + outID);
			
			if(outID > 0){
				sql = "select XML from SystemXMLDocument where ObjectID=? ";
				params = new Object[] { new Integer(outID) };
				types = new int[] { Types.INTEGER};
				saiMap = (Map) executeQuery( new MapHandler());
				
				String outputXML = (String) saiMap.get("XML");
				doc = xmlOperation.toDocument(outputXML);
				
				logger.info("outputXML(get from DB, DatabaseOperation): "+ xmlOperation.toString(doc));
			}
			
		} catch (Exception ex){
			logger.error(ex);
		}
		
		return doc;
	}
	
	
}
