/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XMLTransformActivity extends NodeActivity {

	public XMLTransformActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	public void execute() throws Exception {
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);

		/** XMLTransformActivity start */
		sql = "SELECT * FROM ProcessActivityTransformXML WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List patxList = (List) executeQuery(new MapListHandler());
		
		if (logger.isDebugEnabled()) 
			logger.debug("Invoke workflow count: " + patxList.size() + ".");
			
		Iterator iterator = patxList.iterator();
		while (iterator.hasNext()) {
			Map patxMap = (Map) iterator.next();
			String stylesheet = (String) patxMap.get("XSLT");
			
			sql = "SELECT XML FROM ProcessXMLDocument WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {this.getProcess().getProcessID(), patxMap.get("SourceObjectID")};
			types = new int[] {Types.VARCHAR, Types.INTEGER};
			Map pxdMap = (Map) executeQuery(new MapListHandler());
			String sourceXML = (String) pxdMap.get("XML");

            // load the transformer using JAXP
			//updated by Dingo on 2007-01-10
            TransformerFactory factory = TransformerFactory.newInstance();
            InputStream is = new ByteArrayInputStream(stylesheet.getBytes());			
		    Transformer transformer = factory.newTransformer(new StreamSource(is));
		    //servercomment System.out.println("Transformer successfully!");
            //Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));

            // now lets style the given document
            DocumentSource source = new DocumentSource(DocumentHelper.parseText(sourceXML));
            DocumentResult result = new DocumentResult();
            transformer.transform(source, result);

            // return the transformed document
            Document transformedDoc = result.getDocument();
			sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {transformedDoc.asXML(), this.getProcess().getProcessID(), patxMap.get("TargetObjectID")};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
		}
		
		/** XMLTransformActivity end */
		changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
	}
	/*
    public Document styleDocument(
            Document document, 
            String stylesheet
        ) throws Exception {

            // load the transformer using JAXP
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer( 
                new StreamSource( stylesheet ) 
            );

            // now lets style the given document
            DocumentSource source = new DocumentSource( document );
            DocumentResult result = new DocumentResult();
            transformer.transform( source, result );

            // return the transformed document
            Document transformedDoc = result.getDocument();
            return transformedDoc;
        }*/

}
