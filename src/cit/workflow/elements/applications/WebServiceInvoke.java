/*
 * Created on 2005-3-13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.applications;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import loid.adapters.WebserviceAdapter;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLOperation;

/**
 * @author weiwei
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WebServiceInvoke extends DBUtility implements ApplicationInvoke {

	private int applicationID;

	private String processID;
	
	private Document inputDoc = null;
	
	private Document outputDoc = null;
	
	private XMLOperation xmlOperation = new XMLOperation();
	
	protected final Logger logger = Logger.getLogger(this.getClass());

	public WebServiceInvoke(Connection conn, int applicationID, String processID) {
		super(conn);
		this.applicationID = applicationID;
		this.processID = processID;
		xmlOperation = new XMLOperation();
	}
//Desert ?	
	public WebServiceInvoke(Connection conn, int applicationID) {
		super(conn);
		
		this.applicationID = applicationID;
	}
//Desert end
	
	/**
	 * 
	 */
	public String invoke(String inputXML) throws Exception {
		
		inputDoc = getInputXMLDocument(inputXML);
		
		//servercomment System.out.println("inputDoc: "+xmlOperation.toString(inputDoc));
		
		outputDoc = getOutputXMLDocument();
	
		//servercomment System.out.println("processID = "+processID +"     applicationID = "+applicationID);
		WebserviceAdapter adapter = new WebserviceAdapter(processID, applicationID);
		
//		logger.info("Input XML (before invoking WebService): \n " + xmlOperation.toString(inputDoc));
//		logger.info("Output XML (before invoking WebService): \n " + xmlOperation.toString(outputDoc));
		
		return xmlOperation.toString(adapter.getOutputXML(inputDoc, outputDoc));
	}
	
	private Document getInputXMLDocument(String inputXML) throws Exception {
		
		//servercomment System.out.println("inputXML - Before:  " +xmlOperation.toString(xmlOperation.toDocument(inputXML)));
		
//		logger.info("inputXML - After: \n" + xmlOperation.toString(formatInputXML(inputXML)));
		
		sql = "SELECT WebServiceName, WSDLURL, MethodName, PortName, "
				+ "UserName, Password FROM WebServiceMethod "
				+ "WHERE ApplicationID=?";
		
		params = new Object[] { applicationID };
		types = new int[] { Types.INTEGER };
		Map saiMap = (Map) executeQuery(new MapHandler());
		
		if (saiMap == null)
			throw new Exception("There is no web service for invocation!");
		
		Element webServiceElement = new Element("WEBSERVICE");
		Document doc = new Document(webServiceElement);
		
		Element wsdlElement = new Element("WSDLURL");
		wsdlElement.setText((String) saiMap.get("WSDLURL"));
		webServiceElement.addContent(wsdlElement);

		Element serviceNameElement = new Element("SERVICENAME");
		serviceNameElement.setText((String) saiMap.get("WEBSERVICENAME"));
		webServiceElement.addContent(serviceNameElement);
		
		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText((String) saiMap.get("PORTNAME"));
		webServiceElement.addContent(portNameElement);
		
		Element methodElement = new Element("METHODNAME");
		methodElement.setText((String) saiMap.get("MethodName"));
		webServiceElement.addContent(methodElement);
		
		// formate input XML
		Document inputDocument = formatInputXML(inputXML);
		Element inputElement = inputDocument.getRootElement();
		inputElement = (Element)inputElement.clone();
		webServiceElement.addContent(inputElement);

		Element usernameElement = new Element("USERNAME");
		usernameElement.setText((String) saiMap.get("UserName"));
		webServiceElement.addContent(usernameElement);

		Element passwordElement = new Element("PASSWORD");
		passwordElement.setText((String) saiMap.get("Password"));
		webServiceElement.addContent(passwordElement);
		
		return doc;
	}
	
	private Document getOutputXMLDocument() {
		Document doc = null;
		int outId = 0;

		try {
			sql = "select OutputXMLID from "
					+ "WebServiceMethod where ApplicationID=?";
			
			params = new Object[] { applicationID };
			types = new int[] {Types.INTEGER };
			Map saiMap = (Map) executeQuery(new MapHandler());
			
			if (saiMap == null)
				throw new Exception("ResulSet is null!");
			
				outId = (Integer)saiMap.get("OutputXMLID");
				logger.info("OutputXMLDocumentId: " + outId);
			
			if (outId > 0) {
				sql = "SELECT XML FROM SystemXMLDocument WHERE ObjectID = ?";
				
				params = new Object[] {new Integer(outId) };
				types = new int[] {Types.INTEGER, Types.VARCHAR};
				saiMap = (Map) executeQuery(new MapHandler());
				
				String outputXML = (String)saiMap.get("XML");
				doc = xmlOperation.toDocument(outputXML);
			}
		
		} catch (Exception ex) {
			logger.error(ex);
		}

		return doc;
	}

	/**
	 * 处理以参数名作为Element Name代替Parameter Element的情况，即用<PARAMETER>代替<Parameter Name>
	 * @param wsInputXML
	 * @return
	 */
	private Document formatInputXML(String wsInputXML) {
		Document xmlDoc = null;
		
		try {
			SAXBuilder xmlBuilder = new SAXBuilder();
			xmlDoc = xmlBuilder.build(new StringReader(wsInputXML));
			xmlDoc.getRootElement().setName("INPUT");
			
			List parameterList = xmlDoc.getRootElement().getChildren();
			Iterator it = parameterList.iterator();
			
			while (it.hasNext()) {
				Element parameterEt = (Element) it.next();
				parameterEt.setName("PARAMETER");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return xmlDoc;
	}
}
