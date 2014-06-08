/*
 * Created on 2004-11-26
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

import org.jdom.Document;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface IAdapter {
	public String getOutputXML(String inputXML, String outputXML);
	public Document getOutputXML(Document inputXMLDoc, Document outputXMLDoc);
}
