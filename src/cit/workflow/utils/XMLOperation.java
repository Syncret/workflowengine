package cit.workflow.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLOperation {

protected final Logger logger = Logger.getLogger(this.getClass());
	
	public XMLOperation() {
		
	}
	
	public String toString(Document doc) {
		
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
	    format.setIndent("   ");
	    XMLOutputter outp = new XMLOutputter(format);
	    //cyd modified
	   // ByteArrayOutputStream bo=new ByteArrayOutputStream();
	    if(doc == null) {
	    	return null;
	    } else {
	    	/*try {
				outp.output(doc, bo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    	//return bo.toString();
	    	return outp.outputString(doc);
	    }
	}
	
	public String formatString(String strXML) {
		Document schemaDoc = null;
		
		try {
			Reader inputSchema = new StringReader(strXML);
			SAXBuilder sb = new SAXBuilder();
			schemaDoc = sb.build(inputSchema);
		} catch (JDOMException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		return toString(schemaDoc);
	}
	
	public Document toDocument(String strXML) {
		Document schemaDoc = null;		
		try {
			Reader inputSchema = new StringReader(strXML);
			SAXBuilder sb = new SAXBuilder();
			schemaDoc = sb.build(inputSchema);
		} catch (JDOMException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		return schemaDoc;
	}
	
	
	@SuppressWarnings("unchecked")
	public Document getXMLFromSchema(Document schemaDoc) {
		logger.info("run getXMLFromSchema()");
		if (schemaDoc == null) {
			logger.info("schemaDoc is null!");
		}
		Element schemaRoot = schemaDoc.getRootElement();
		Element schemaChild = schemaRoot.getChild("element");
		
		if (schemaChild == null) {
			logger.info("---------------------null-----------------");
			logger.info(toString(schemaDoc));
			return null;
		}
		
		List<Element> complexTypeList = schemaRoot.getChildren("complexType");
		Element xmlRoot = getComplexTypeXML(complexTypeList, schemaChild);
		Document xmlDoc = new Document(xmlRoot);

		logger.info("****Schema**********\n" + toString(xmlDoc));

		return xmlDoc;
	}

	/**
	 * 阌熸枻鎷烽敓绲渓ement阌熷彨纰夋嫹type阌熸枻鎷烽敓镄嗭綇鎷烽敓鏂ゆ嫹阌熸枻鎷烽敓鏂ゆ嫹阌熸枻鎷烽敓鏂ゆ嫹阌熸枻鎷穋omplexTypeList阌熷彨纰夋嫹complexType
	 * name阌熸枻鎷烽敓镄嗙鎷峰€奸敓鏂ゆ嫹鍚岄敓鏂ゆ嫹璇撮敓鏂ゆ嫹阌熸枻鎷穞ype阌熸枻鎷烽敓鏂ゆ嫹链间负涓€阌熸枻鎷穋omplexType
	 * 鍚屾椂阌熺煫闱╂嫹complexType阌熸枻鎷烽敓鏂ゆ嫹阌熸枻鎷烽敓绲珁pe阌熸枻鎷烽敓鏂ゆ嫹阌熺祻ML阌熸枻鎷烽敓鎹风櫢鎷烽敓鏂ゆ嫹涔堜剑阌熸枻鎷烽敓鏂ゆ嫹阌熸枻鎷烽敓绲奵hema阌熸枻鎷稾ML
	 * 
	 * @param complexTypeList -
	 *            complexType阌熷彨鎲嬫嫹
	 * @param element -
	 *            Schema阌熸枻鎷疯瘶阌熶茎纰夋嫹涓€阌熸枻鎷积lement阌熸枻鎷烽敓鎺ヨ妭镣广€?
	 * @return
	 */
	private Element getComplexTypeXML(List<Element> complexTypeList, Element element) {
		logger.info("run getComplexTypeXML()");
		List<Element> elementList = new ArrayList<Element>();
		List<Element> attributeList = new ArrayList<Element>();
		Element root = null;
		boolean isComplexType = false;

		if (null == element) {
			logger.info("element ***");
		} else {
			//			
			if (null == element.getAttributeValue("type")) {
				root = new Element(element.getAttributeValue("name"));
				Element innerComplexTypeEle = element.getChild("complexType");
				Element innerSequenceEle = innerComplexTypeEle.getChild("sequence");

				if (null == innerSequenceEle) {
					elementList = innerComplexTypeEle.getChildren("element");
				} else {
					elementList = innerSequenceEle.getChildren("element");
				}

				// 阌熸枻鎷锋椂鍙敓鏂ゆ嫹阌熸枻鎷蜂竴阌熸枻鎷穉ttribute阌熸枻鎷烽敓鏂ゆ嫹阌燂拷
				Element innerAttributeEle = innerComplexTypeEle.getChild("attribute");
				if (null == innerAttributeEle) {
					attributeList = null;
				} else {
					attributeList = innerComplexTypeEle.getChildren("attribute");
				}

				if (elementList != null) {
					for (Element childEle : elementList) {
						root.addContent(getComplexTypeXML(complexTypeList, childEle));
					}
				}

				if (attributeList != null) {
					for (Element childEle : attributeList) {
						root.setAttribute(childEle.getAttributeValue("name"), childEle.getAttributeValue("fixed"));
					}
				}

				isComplexType = true;
			} else {
				for (Element complexTypeEle : complexTypeList) {
					if (complexTypeEle.getAttributeValue("name").equalsIgnoreCase(element.getAttributeValue("type"))) {
						isComplexType = true;

						root = new Element(element.getAttributeValue("name"));
						Element sequenceEle = complexTypeEle.getChild("sequence");

						if (null == sequenceEle) {
							elementList = complexTypeEle.getChildren("element");
						} else {
							elementList = sequenceEle.getChildren("element");
						}

						Element attributeEle = complexTypeEle.getChild("attribute");

						if (null == attributeEle) {
							attributeList = null;
						} else {
							attributeList = complexTypeEle.getChildren("attribute");
						}

						// 阌熸枻鎷穋omplexType阌熸枻鎷峰纺阌熸枻鎷蜂负XML
						if (null != elementList) {
							for (Element childEle : elementList) {
								root.addContent(getComplexTypeXML(complexTypeList, childEle));
							}
						}

						if (null != attributeList) {
							for (Element childEle : attributeList) {
								root.setAttribute(childEle.getAttributeValue("name"), childEle
										.getAttributeValue("fixed"));
							}
						}
						break;
					}
				}

				if (false == isComplexType) {
					root = new Element(element.getAttributeValue("name"));
				}

				if (null == element.getAttribute("fixed")) {

				} else {

				}
			}
		}
		return root;
	}
	
}
