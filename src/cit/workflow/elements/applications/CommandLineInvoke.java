/*
 * Created on 2005-3-16
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.applications;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import loid.adapters.CommandlineAdapter;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cit.workflow.utils.DBUtility;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CommandLineInvoke extends DBUtility implements ApplicationInvoke {

	private int applicationID;

	public CommandLineInvoke(Connection conn, int applicationID) {
		super(conn);
		
		this.applicationID = applicationID;
	}
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.applications.ApplicationInvoke#invoke(java.lang.String)
	 */
	public String invoke(String inputXML) throws Exception {
		//sql = "SELECT Command, OutputFile, UserName, Password FROM CommandLine WHERE ApplicationID=?";//dingo
		sql = "SELECT Command, UserName, Password FROM CommandLine WHERE ApplicationID=?";
		params = new Object[] {new Integer(applicationID)};
		types = new int[] {Types.INTEGER};
		Map saiMap = (Map) executeQuery(new MapHandler());
		if (saiMap == null)
			throw new Exception("There is no command line for invocation!");
		
		final Document document = DocumentHelper.createDocument();
		Element webServiceElement = document.addElement("COMMANDLINE");
		
		Element commandElement = webServiceElement.addElement("COMMAND");
		commandElement.addText((String) saiMap.get("Command"));
		
		//Element outputfileElement = webServiceElement.addElement("OUTPUTFILE");//dingo
		//outputfileElement.addText((String) saiMap.get("OutputFile"));//dingo
		
		Document inputDocument = DocumentHelper.parseText(inputXML);
		Element inputElement = inputDocument.getRootElement();
		webServiceElement.add(inputElement);
		
		Element usernameElement = webServiceElement.addElement("USERNAME");
		usernameElement.addText((String) saiMap.get("UserName"));
		
		Element passwordElement = webServiceElement.addElement("PASSWORD");
		passwordElement.addText((String) saiMap.get("Password"));
		
		CommandlineAdapter adapter = new CommandlineAdapter(false);
		return adapter.getOutputXML(document.asXML(), applicationID);
	}
}
