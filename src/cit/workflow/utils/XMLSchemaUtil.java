package cit.workflow.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.xml.namespace.QName;

//add by chan,2012/6/17
public class XMLSchemaUtil {
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public static Namespace XMLNS = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema");
	
	public static String TNSURI="http://www.example.org/DocumentSchema";
	
	public static String TNSPREFIX="tns";
	
	public static Namespace TNS = Namespace.getNamespace(TNSPREFIX,TNSURI);
	
	private XMLOperation xmlOpera = new XMLOperation();
	
//	private Element parseSchemaRoot=null;
	
//	private Element originalSchemaRoot=null;
	
	public static List XSD_NS_URI_LIST=Arrays.asList(new String[]{
			"http://www.w3.org/1999/XMLSchema","http://www.w3.org/2000/10/XMLSchema",
			"http://www.w3.org/2001/XMLSchema"
	});
	
	public XMLSchemaUtil(){}
	
	public Document createSchemaDoc(){
		Document schemaDoc = new Document(createSchemaElement());
		return schemaDoc;
	}
	public Element createSchemaElement(){
		Element schemaRoot = new Element("schema", XMLNS);
		schemaRoot.setAttribute("targetNamespace",TNSURI);
		schemaRoot.addNamespaceDeclaration(TNS);
		return schemaRoot;
	}
	public static boolean isXSDElement(String namespaceURI){
		if(XSD_NS_URI_LIST.contains(namespaceURI))
			return true;
		return false;
	}
	
//	public static Element parseSchema(Element originalSchemaRoot){
//		return parseSchema(originalSchemaRoot,XMLNS,TNS);
//	}
//	public static Element parseSchema(Element originalSchemaRoot,Namespace xmlns,Namespace tns){
//		parseSchemaRoot = new Element("schema", xmlns);
//		parseSchemaRoot.setAttribute("targetNamespace",tns.getURI());
//		parseSchemaRoot.addNamespaceDeclaration(tns);
//		List<Element> eleList = originalSchemaRoot.getChildren("element",originalSchemaRoot.getNamespace());
//		for(Element ele:eleList){
//			parseSchemaRoot.addContent(parseElement(ele,originalSchemaRoot));
//		}
//		return parseSchemaRoot;
//	}
	
	public Element parseSchema(Element originalSchemaRoot){
		Element parseSchemaRoot = createSchemaElement();
	//	originalSchemaRoot=oriSchemaRoot;
		List<Element> eleList = originalSchemaRoot.getChildren("element",originalSchemaRoot.getNamespace());
		for(Element ele:eleList){
			parseElement(ele,originalSchemaRoot,parseSchemaRoot);
		}
		return parseSchemaRoot;
	}
	
	public void parseSchema(Element originalSchemaRoot,Element parent){
	//	parseSchemaRoot = parent;
	//	originalSchemaRoot=oriSchemaRoot;
		List<Element> eleList = originalSchemaRoot.getChildren("element",originalSchemaRoot.getNamespace());
		for(Element ele:eleList){
			parseElement(ele,originalSchemaRoot,parent);
		}
	}
	//parse parseEle, add to schemaEle,parseEleRoot is the root element of the schema containing parseEle
	public Element parseElement(Element parseEle,Element parseEleRoot,Element schemaEle){
		String parseEleName=parseEle.getName();
		Element newEle=new Element(parseEleName,XMLNS);
		schemaEle.addContent(newEle);
		for(Object o:parseEle.getAttributes()){
			Attribute attr=(Attribute)o;
			String attrValue=attr.getValue();
			String attrName=attr.getName();
			if(!attrName.equals("type")){
				if(attrName.equals("ref")){
					QName typeQName=getQName(attrValue,parseEle);
					schemaEle.removeContent(newEle);
					newEle=parseElement(getElementforQName(typeQName,"element",parseEleRoot),parseEleRoot,schemaEle);
				}
				else if(!(parseEleName.equals("complexType")&&attrName.equals("name")))
					newEle.setAttribute(attrName, attrValue);
			}else{
				QName typeQName=getQName(attrValue,parseEle);
				if(isXSDElement(typeQName.getNamespaceURI()))
            		newEle.setAttribute(attrName, typeQName.getLocalPart());
				else
					parseElement(getElementforQName(typeQName,"complexType",parseEleRoot),parseEleRoot,newEle);
			}
		}
		for(Object ob:parseEle.getChildren()){
			parseElement((Element)ob,parseEleRoot,newEle);
		}
		return newEle;
	}
	private QName getQName(String qname,Element root){
		int colonIndex=qname.indexOf(':');
		String prefix="";
		String uri=null;
		String localPart=qname.substring(colonIndex+1);
		if(colonIndex>0){
			prefix=qname.substring(0, colonIndex);		
		}
		uri=root.getNamespace(prefix).getURI();
		return new javax.xml.namespace.QName(uri,localPart);
	}
	
	private Element getElementforQName(QName qname,String eleName,Element schemaRoot){
		for(Object obj:schemaRoot.getChildren(eleName,schemaRoot.getNamespace())){
			Element ele=(Element)obj;
			if(qname.getLocalPart().equals(ele.getAttributeValue("name"))){
				return ele;
			}
		}
		return null;
	}
	/**
	 * 从固定格式的XML得到简单的Schema
	 * 
	 * @param xmlDoc
	 * @return 符合应用输入参数格式的Schema
	 */
	@SuppressWarnings("unchecked")
	public Document getXMLFromSchema(Document schemaDoc) {
		logger.info("run getXMLFromSchema()");
		if (schemaDoc == null) {
			logger.info("schemaDoc is null!");
		}
		Element schemaRoot = schemaDoc.getRootElement();
		Element schemaChild = schemaRoot.getChild("element",XMLNS);

		if (schemaChild == null) {
			logger.info("---------------------null-----------------");
			logger.info(xmlOpera.toString(schemaDoc));
			return null;
		}

		List<Element> complexTypeList = schemaRoot.getChildren("complexType",XMLNS);
		Element xmlRoot = getComplexTypeXML(complexTypeList, schemaChild);
		Document xmlDoc = new Document(xmlRoot);
		// chan 2012-6-4
		// logger.info("根据Schema生成相应的XML：\n" + xmlOpera.toString(xmlDoc));

		return xmlDoc;
	}

	/**
	 * 检查element中的type属性，如果该属性名与complexTypeList中的complexType
	 * name属性的值相同则说明该type属性值为一个complexType
	 * 同时用该complexType来代替该type，生成XML。递归调用从而生成整个Schema的XML
	 * 
	 * @param complexTypeList
	 *            - complexType列表
	 * @param element
	 *            - Schema根节点的第一个element孩子节点。
	 * @return
	 */
	private Element getComplexTypeXML(List<Element> complexTypeList,
			Element element) {
		logger.info("run getComplexTypeXML()");
		List<Element> elementList = new ArrayList<Element>();
		List<Element> attributeList = new ArrayList<Element>();
		Element root = null;
		boolean isComplexType = false;

		if (null == element) {
			logger.info("element 为空");
		} else {
			//			
			if (null == element.getAttributeValue("type")) {
				root = new Element(element.getAttributeValue("name"));
				Element innerComplexTypeEle = element.getChild("complexType",XMLNS);
				Element innerSequenceEle = innerComplexTypeEle
						.getChild("sequence",XMLNS);

				if (null == innerSequenceEle) {
					elementList = innerComplexTypeEle.getChildren("element",XMLNS);
				} else {
					elementList = innerSequenceEle.getChildren("element",XMLNS);
				}

				// 暂时只考虑一个attribute的情况
				Element innerAttributeEle = innerComplexTypeEle
						.getChild("attribute",XMLNS);
				if (null == innerAttributeEle) {
					attributeList = null;
				} else {
					attributeList = innerComplexTypeEle
							.getChildren("attribute",XMLNS);
				}

				if (elementList != null) {
					for (Element childEle : elementList) {
						root.addContent(getComplexTypeXML(complexTypeList,
								childEle));
					}
				}

				if (attributeList != null) {
					for (Element childEle : attributeList) {
						root.setAttribute(childEle.getAttributeValue("name"),
								childEle.getAttributeValue("fixed"));
					}
				}

				isComplexType = true;
			} else {
				for (Element complexTypeEle : complexTypeList) {
					String typeValue=element.getAttributeValue("type");
					typeValue=typeValue.substring(typeValue.indexOf(':')+1);
					if (complexTypeEle
							.getAttributeValue("name")
							.equalsIgnoreCase(typeValue)) {
						isComplexType = true;

						root = new Element(element.getAttributeValue("name"));
						Element sequenceEle = complexTypeEle
								.getChild("sequence",XMLNS);

						if (null == sequenceEle) {
							elementList = complexTypeEle.getChildren("element",XMLNS);
						} else {
							elementList = sequenceEle.getChildren("element",XMLNS);
						}

						Element attributeEle = complexTypeEle
								.getChild("attribute",XMLNS);

						if (null == attributeEle) {
							attributeList = null;
						} else {
							attributeList = complexTypeEle
									.getChildren("attribute",XMLNS);
						}

						// 将complexType格式化为XML
						if (null != elementList) {
							for (Element childEle : elementList) {
								root.addContent(getComplexTypeXML(
										complexTypeList, childEle));
							}
						}

						if (null != attributeList) {
							for (Element childEle : attributeList) {
								root.setAttribute(childEle
										.getAttributeValue("name"), childEle
										.getAttributeValue("fixed"));
							}
						}
						break;
					}
				}

				if (false == isComplexType) {
					root = new Element(element.getAttributeValue("name"));
				}

				if (null == element.getAttribute("fixed",XMLNS)) {

				} else {

				}
			}
		}
		return root;
	}
	public static void main(String[] args){
		XMLSchemaUtil util=new XMLSchemaUtil();
		 SAXBuilder sb=new SAXBuilder(); 
		 try {
			Document doc=sb.build("../test/resource/testS.xsd");
	//		Document doc=sb.build("../test/resource/TestXML_Schema.xsd");
			Document parsedDoc=util.createSchemaDoc();
			parsedDoc.setRootElement(util.parseSchema(doc.getRootElement()));
			//servercomment System.out.println(util.xmlOpera.toString(parsedDoc));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
