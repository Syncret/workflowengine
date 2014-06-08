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

import loid.adapters.JavaclassAdapter;

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
public class JavaClassInvoke extends DBUtility implements ApplicationInvoke {

	private int applicationID;

	private String processID;

	private boolean isSystem;

	private Document inputDoc = null;

	private Document outputDoc = null;

	private XMLOperation xmlOperation = new XMLOperation();

	protected final Logger logger = Logger.getLogger(this.getClass());

	public JavaClassInvoke(Connection conn, int applicationID, String processID) {
		super(conn);
		
		this.applicationID = applicationID;
		this.processID = processID;
	}
	
	public JavaClassInvoke(Connection conn, int applicationID, String processID, boolean isSystem) {
		super(conn);

		this.applicationID = applicationID;
		this.processID = processID;
		this.isSystem = isSystem;
		xmlOperation = new XMLOperation();
	}
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.applications.ApplicationInvoke#invoke(java.lang.String)
	 */
	public String invoke(String inputXML) throws Exception {
		inputDoc = getInputXMLDocument(inputXML);
		//servercomment System.out.println("inputDoc: ");
		//servercomment System.out.println(xmlOperation.toString(inputDoc));
		
		outputDoc = getOutputXMLDocument();
		JavaclassAdapter adapter = new JavaclassAdapter(processID, applicationID );

		logger.info("Input XML (before invoking JavaClass): \n" + xmlOperation.toString(inputDoc));
		logger.info("Output XML (before invoking JavaClass): \n" + xmlOperation.toString(outputDoc));
		return xmlOperation.toString(adapter.getOutputXML(inputDoc, outputDoc));
	}
	
	private Document getInputXMLDocument(String inputXML) throws Exception {
		
		//servercomment System.out.println("wsInputXML before formating :");
		//servercomment System.out.println(xmlOperation.toString(xmlOperation.toDocument(inputXML)));

		inputXML = formatInputXML(inputXML);
		
		//servercomment System.out.println("wsInputXML after formating :");
		//servercomment System.out.println(xmlOperation.toString(xmlOperation.toDocument(inputXML)));
		

		sql = "SELECT * FROM JavaClassMethod WHERE ApplicationID=?";

		params = new Object[] { new Integer(applicationID) };
		types = new int[] { Types.INTEGER };
		Map saiMap = (Map) executeQuery(new MapHandler());

		if (saiMap == null)
			throw new Exception("There is no java class for invocation!");

		Element JavaClassElement = new Element("JAVACLASS");
		JavaClassElement.setAttribute("ClassName", (String) saiMap.get("ClassName"));
		Document doc = new Document(JavaClassElement);

		Element classurlElement = new Element("CLASSURL");
		classurlElement.setText((String) saiMap.get("ClassURL"));
		JavaClassElement.addContent(classurlElement);

		Element methodElement = new Element("METHODNAME");
		methodElement.setText((String) saiMap.get("MethodName"));
		//servercomment System.out.println("MethodName: "+ (String) saiMap.get("MethodName"));
		JavaClassElement.addContent(methodElement);
		
		Element usernameElement = new Element("USERNAME");
		usernameElement.setText((String) saiMap.get("UserName"));
		JavaClassElement.addContent(usernameElement);
		
		Element passwordElement = new Element("PASSWORD");
		passwordElement.setText((String) saiMap.get("Password"));
		JavaClassElement.addContent(passwordElement);
		
//		logger.info("<INPUT> Section:\n" + xmlOperation.formatString(inputXML));

		Document inputDocument = xmlOperation.toDocument(inputXML);
		Element inputElement = inputDocument.getRootElement();
		inputElement = (Element) inputElement.clone();
		JavaClassElement.addContent(inputElement);

		return doc;
	}

	private Document getOutputXMLDocument() {
		Document doc = null;
		int outId = 0;

		try {
			sql = "select OutputXMLId from JavaClassMethod where ApplicationID=?" ;

			params = new Object[] { applicationID };
			types = new int[] { Types.INTEGER };
			Map saiMap = (Map) executeQuery(new MapHandler());

			if (saiMap == null)
				throw new Exception("ResulSet is null!");

			outId = (Integer) saiMap.get("OutputXMLId");
			logger.info("OutputXMLId: " + outId);

			if (outId > 0) {
				sql = "select XML from SystemXMLDocument where ObjectID=?";

				params = new Object[] { new Integer(outId) };
				types = new int[] { Types.INTEGER };
				saiMap = (Map) executeQuery(new MapHandler());

				String outputXML = (String) saiMap.get("XML");
				doc = xmlOperation.toDocument(outputXML);
				logger.info("outputXML(get from database): " + xmlOperation.toString(doc));
			}
		} catch (Exception ex) {
			logger.error(ex);
		}

		return doc;
	}

//	将wsInputXMl的格式由<ParameterSchema><METHOD><Para0><VALUE>10</VALUE></Para0></METHOD></ParameterSchema>
// 改为<ParameterSchema><METHOD><PARAMETER><VALUE>10</VALUE></PARAMETER></METHOD></ParameterSchema>
	
	private String formatInputXML(String wsInputXML) {
		String targetXMLStr = null;
		try {
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document xmlDoc = xmlBuilder.build(new StringReader(wsInputXML));
			List parameterList = xmlDoc.getRootElement().getChildren();
			Iterator it = parameterList.iterator();

			while (it.hasNext()) {
				Element MethodET = (Element) it.next();
				List ParaNList = MethodET.getChildren();
				Iterator paraIt = ParaNList.iterator();

				while (paraIt.hasNext()) {
					Element ParaNET = (Element) paraIt.next();
					ParaNET.setName("PARAMETER");
				}
			}
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getCompactFormat());
			targetXMLStr = outputter.outputString(xmlDoc);
		} catch (Exception ex) {
			
			ex.printStackTrace();
		}

		return targetXMLStr;
	}
	
	
}
