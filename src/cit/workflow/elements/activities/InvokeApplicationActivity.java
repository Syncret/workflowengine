/*
 * Created on 2004-12-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.activities;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.applications.CloudServiceInvoke;
import cit.workflow.elements.applications.ApplicationInvoke;
import cit.workflow.elements.applications.CommonApplicationInvoke;
import cit.workflow.elements.applications.WebServiceInvoke;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.DocumentVariable;
import cit.workflow.elements.variables.InherentVariable;
import cit.workflow.elements.variables.ObjectVariable;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.elements.variables.ReferenceObject;
import cit.workflow.elements.variables.XMLVariable;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.XMLMergence;
import cit.workflow.utils.XMLOperation;
import cit.workflow.utils.XMLSchemaUtil;






import cit.workflow.view.InformationPane;



// //////////////////////////
import org.apache.axis.client.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;

// //////////////////////////
/**
 * @author weiwei
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class InvokeApplicationActivity extends NodeActivity {

	public static final int APPLICATION_WEB_SERVICE = 4;

	private int curOutputXMLID;
	private static Logger logger = Logger.getLogger(InvokeApplicationActivity.class);     //cxz:计算机记录器logger
	public static XMLOperation xmlOperation = new XMLOperation();
	private XMLSchemaUtil xmlSchemaUtil=new XMLSchemaUtil();
	private DefaultTreeModel objectTreeModel;
	private DefaultTreeModel sourceTreeModel;
	private ApplicationInvoke applicationInvoke=null;

	private Namespace xsl = Namespace.getNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
	private boolean isMapping = false;
	public InvokeApplicationActivity(Connection conn, Process process,
			int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}


	public void execute() throws Exception {
		
		//servercomment System.out.println("<---------- InvokeApplicationActivity Excuted -------------->");
		InformationPane.writeln("<---------- InvokeApplicationActivity Excuted -------------->");
		changeState(Constants.ACTIVITY_STATE_READY,
				Constants.ACTIVITY_STATE_RUNNING);

		sql = "SELECT PAIA.ApplicationID, PAIA.InvocationType, PXMLD.XML, PAIA.OutputXMLID FROM ProcessActivityInvokingApplication PAIA LEFT JOIN ProcessXMLDocument PXMLD ON PXMLD.ProcessID=PAIA.ProcessID AND PXMLD.ObjectID=PAIA.InputXMLID WHERE PAIA.ProcessID=? AND PAIA.ActivityID=?";
		params = new Object[] { this.getProcess().getProcessID(),
				new Integer(this.activityID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List paiaList = (List) executeQuery(new MapListHandler());

		Iterator iterator = paiaList.iterator();
		while (iterator.hasNext()) {

			Map paiaMap = (Map) iterator.next();
			int applicationID=((Integer) paiaMap
					.get("ApplicationID")).intValue();


			applicationInvoke = ElementFactory
					.createApplication(conn, applicationID, this.getProcess().getProcessID());
			
			sql = "select * from processactivityinputmapping where processid = ? and activityid = ?";
			params = new Object[] { this.getProcess().getProcessID(),
					new Integer(this.activityID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			List paimList = (List) executeQuery(new MapListHandler());
			String inputXML=null;
			String xslt=null;
			List<String> xsltList=null;
			XMLMergence xmlMergence=new XMLMergence();
			Iterator paimIterator = paimList.iterator();
			while (paimIterator.hasNext()) {
				Map paimMap = (Map) paimIterator.next();
				int dataflowID=(Integer)paimMap.get("DataFlowID");
				if(true){
//				if(isDataFlowActive(dataflowID)||dataflowID==-1){
					if(xslt==null){
						inputXML = (String) paimMap.get("inputXML");     
						xslt = (String) paimMap.get("xslt");
					}
					else{
						inputXML=xmlMergence.mergeString(inputXML, (String) paimMap.get("inputXML"));
						if(xsltList==null){
							xsltList=new ArrayList<String>();
							xsltList.add(xslt);
						}
						xsltList.add((String) paimMap.get("xslt"));
					}
				}	
			}
			if(xsltList!=null){
				Document sourceXMLDocument = xmlOperation.toDocument(inputXML);
				DefaultMutableTreeNode topTreeNode = getSchemaTreeNode(sourceXMLDocument);
				sourceTreeModel = new DefaultTreeModel(topTreeNode);
				xslt=xmlOperation.toString(buildAllXSLT(xsltList,getInputSystemXML(applicationID)));;
			}
			
			String processInputXML = addValueToXML(inputXML);
			//servercomment System.out.println("After addvalueToXML, processInputXML is:");
			//servercomment System.out.println(processInputXML);
			String wsInputXML = getXMLByXSLT(processInputXML, xslt);		
			//servercomment System.out.println("wsInputXML:");
			//servercomment System.out.println(xmlOperation.toString(xmlOperation.toDocument(wsInputXML)));		
			
			long starttime=System.currentTimeMillis();
			
			String outputXML = applicationInvoke.invoke(wsInputXML);
			
			long endtime=System.currentTimeMillis();
			processManager.getTimeRecoder().addWaitTime(endtime-starttime);
			
			//servercomment System.out.println("The outputXML is:");
			//servercomment System.out.println(outputXML);	
			if(outputXML!=null)
				updateOutputObject(outputXML);
			
			if (((Integer) paiaMap.get("InvocationType")).intValue() == Constants.INVOKE_TYPE_SYNCHRONIZATION) {
				changeState(Constants.ACTIVITY_STATE_RUNNING,
						Constants.ACTIVITY_STATE_COMPLETED);			
			} 
		}
	}

	private String addValueToXML(String xmlStr) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(xmlStr));
			Element root = doc.getRootElement();
			List objectList = root.getChildren();
			Element objectEt;
			String etName;
			Attribute oidAttr;

			int objectID;
			int objectType;
			for (int i = 0; i < objectList.size(); i++) {
				objectEt = (Element) objectList.get(i);// 循环依次得到子元素
				etName = objectEt.getName();
				oidAttr = objectEt.getAttribute("ObjectID");
				if (oidAttr != null) {
					objectID = oidAttr.getIntValue();
					ProcessObject processObject = ElementFactory
							.createProcessObject(conn, getProcess()
									.getProcessID(), objectID);
					objectType = processObject.getObjectType();
					
					/*
					 * sql = "select * from processobject where processid = ?
					 * and objectid = ?"; params = new Object[]
					 * {this.getProcess().getProcessID(), new
					 * Integer(objectID)}; types = new int[] {Types.VARCHAR,
					 * Types.INTEGER}; List poList = (List) executeQuery(new
					 * MapListHandler()); Iterator poIterator =
					 * poList.iterator(); while (poIterator.hasNext()) {
					 * objectType = (Integer)poMap.get("ObjectType");
					 */

					if (objectType == Constants.OBJECT_TYPE_INHERENT) {
						objectEt.getChild("NAME").setText(etName);
						
						sql = "select * from processInherentVariable where processid = ? and objectid = ?";
						params = new Object[] { this.getProcess().getProcessID(),
								new Integer(objectID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map pivMap = (Map)executeQuery(new MapHandler());
						int valueType = ((Integer)pivMap.get("ValueType")).intValue();
						
						if (valueType == Constants.DATA_TYPE_INTEGER) {
							objectEt.getChild("TYPE").setText("int");
						} else if (valueType == Constants.DATA_TYPE_FLOAT) {
							objectEt.getChild("TYPE").setText("float");
						} else if (valueType == Constants.DATA_TYPE_DOUBLE) {
							objectEt.getChild("TYPE").setText("double");
						} else if (valueType == Constants.DATA_TYPE_STRING) {
							objectEt.getChild("TYPE").setText("string");
						} else if (valueType == Constants.DATA_TYPE_BOOLEAN) {
							objectEt.getChild("TYPE").setText("boolean");
						}
						
						objectEt.getChild("VALUE").setText(
								(String) processObject.getValue());
					} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
						objectEt.getChild("NAME").setText(etName);
						
						sql = "select * from processObjectVariable where processid = ? and objectid = ?";
						params = new Object[] { this.getProcess().getProcessID(),
								new Integer(objectID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map pivMap = (Map)executeQuery(new MapHandler());
						int valueType = ((Integer)pivMap.get("ValueType")).intValue();
						
						if (valueType == Constants.DATA_TYPE_INTEGER) {
							objectEt.getChild("TYPE").setText("int");
						} else if (valueType == Constants.DATA_TYPE_FLOAT) {
							objectEt.getChild("TYPE").setText("float");
						} else if (valueType == Constants.DATA_TYPE_DOUBLE) {
							objectEt.getChild("TYPE").setText("double");
						} else if (valueType == Constants.DATA_TYPE_STRING) {
							objectEt.getChild("TYPE").setText("string");
						} else if (valueType == Constants.DATA_TYPE_BOOLEAN) {
							objectEt.getChild("TYPE").setText("boolean");
						}
						
						objectEt.getChild("VALUE").setText(
								(String) processObject.getValue());
					} else if (objectType == Constants.OBJECT_TYPE_XML) {
						objectEt.removeContent();
						SAXBuilder xmlObjectBuilder = new SAXBuilder();
						Document xmlObjectDoc = xmlObjectBuilder
								.build(new StringReader((String) processObject
										.getValue()));
						Element xmlObjectRoot = xmlObjectDoc.getRootElement();
						//servercomment System.out.println("tht root name is:");
						//servercomment System.out.println(xmlObjectRoot.getName());
						// //servercomment System.out.println(xmlObjectRoot.getName());

						List xmlObjectList = xmlObjectRoot.getChildren();
						// XMLOutputter outputter = new XMLOutputter();
						// objectEt.addContent(xmlObjectList);
						// XMLOutputter outputter = new XMLOutputter();
						//Element xmlObjectEt = new Element("a");
						objectEt.addContent((Element) xmlObjectRoot.clone());
						// objectEt.addContent((String)processObject.getValue());
					} else if (objectType == Constants.OBJECT_TYPE_DOC) {

					} else if (objectType == Constants.OBJECT_TYPE_REF) {

					} else if (objectType == Constants.OBJECT_TYPE_ARRAY) {
						objectEt.getChild("NAME").setText(etName);

						sql = "select * from processArrayVariable where processid = ? and objectid = ?";
						params = new Object[] {
								this.getProcess().getProcessID(),
								new Integer(objectID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map pivMap = (Map) executeQuery(new MapHandler());
						objectEt.getChild("TYPE").setText("Array");
						String value = ((String) pivMap.get("Value"))
								.toString();

						objectEt.getChild("VALUE").setText(value);

					} 
					else {
						throw new WorkflowTransactionException(
								"Object type is out of range!");
					}

				} else if (etName.equals("SYSTEM_INFORMATION")) {
					if (objectEt.getChild("ProcessID") != null) { 
						objectEt.getChild("ProcessID").setText(
								getProcess().getProcessID());
					}
					if (objectEt.getChild("ProcessName") != null) {
						objectEt.getChild("ProcessName").setText(
								getProcess().getAttributeValue("processName")
										.toString());
					}
					if (objectEt.getChild("ActivityID") != null) {
						objectEt.getChild("ActivityID").setText(
								new Integer(activityID).toString());
					}
					if (objectEt.getChild("UserID") != null) {
						objectEt.getChild("UserID").setText(
								new Integer(processManager.getCurrentUser().getActorID()).toString());
					}
				}
			}
			XMLOutputter outputter = new XMLOutputter();
			return outputter.outputString(doc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private String getXMLByXSLT(String xmlStr, String xsltStr) {
		String targetXMLStr = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			Document xmlDoc = builder.build(new StringReader(xmlStr));
			Document xsltDoc = builder.build(new StringReader(xsltStr));
			XSLTransformer transformer = new XSLTransformer(xsltDoc);
			Document targetXMLDoc = transformer.transform(xmlDoc);
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getCompactFormat());
			targetXMLStr = outputter.outputString(targetXMLDoc);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return targetXMLStr;
	}
	
	
	//将wsInputXMl的格式由<ParameterSchema><METHOD><Para0><VALUE>10</VALUE></Para0></METHOD></ParameterSchema>
	// 改为<ParameterSchema><METHOD><PARAMETER><VALUE>10</VALUE></PARAMETER></METHOD></ParameterSchema>
    // 该方法已弃用   cxz 2009.4.28
	private String formatInputXML(String wsInputXML) {
		String targetXMLStr = null;
		try {
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document xmlDoc = xmlBuilder.build(new StringReader(wsInputXML));
			List parameterList = xmlDoc.getRootElement().getChildren();
			Iterator it = parameterList.iterator();
			while (it.hasNext()) {    		
				Element MethodET = (Element)it.next();
				List ParaNList=MethodET.getChildren();
				Iterator paraIt=ParaNList.iterator();
				while(paraIt.hasNext()){
					Element ParaNET=(Element)paraIt.next();
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

	private void updateOutputObject(String xmlStr) {
		try {
			sql = "select * from processactivityoutputmapping where processid = ? and ActivityID = ?";
			params = new Object[] { this.getProcess().getProcessID(),new Integer(this.activityID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			List paomList = (List) executeQuery(new MapListHandler());
			Iterator iterator = paomList.iterator();
			
			
			
			int objectID;
			int objectType;
			while (iterator.hasNext()) {
				Map paomMap = (Map)iterator.next();
				String outputXslt=(String)paomMap.get("XSLT");
				if(outputXslt.equals(""))continue;
				objectID = (Integer)paomMap.get("ObjectID");
				ProcessObject processObject = ElementFactory.createProcessObject(conn, getProcess().getProcessID(), objectID);
				objectType = processObject.getObjectType();
				
				if (objectType == Constants.OBJECT_TYPE_INHERENT) {
					
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder.build(new StringReader(xmlStr));
					String value = xmlDoc.getRootElement().getChild("RETURN").getChild("VALUE").getValue();
					//servercomment System.out.println(value);
					
					sql = "update processInherentVariable set value = ? where processid = ? and objectid = ?";
					params = new Object[] { value, this.getProcess().getProcessID(), new Integer(objectID)};
					types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
					
					this.executeUpdate();
					
				} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder.build(new StringReader(xmlStr));
					String value = xmlDoc.getRootElement().getChild("PARAMETER").getChild("VALUE").getValue();
					//servercomment System.out.println(value);
					
					sql = "update processObjectVariable set value = ? where processid = ?";
					params = new Object[] { value, this.getProcess().getProcessID()};
					types = new int[] { Types.VARCHAR, Types.VARCHAR };
					this.executeUpdate();
				} else if (objectType == Constants.OBJECT_TYPE_XML) {
					String xslt = (String)paomMap.get("XSLT");
					String outputXML = getXMLByXSLT(xmlStr, xslt);
					
					sql = "select * from processXMLDocument where processid = ? and objectID = ?";
					params = new Object[] { this.getProcess().getProcessID(), new Integer(objectID)};
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					Map pxdMap = (Map)executeQuery(new MapHandler());
					String sourceXML = (String)pxdMap.get("xml");
					
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document sourceXMLDoc = xmlBuilder.build(new StringReader(sourceXML));
					Document outputXMLDoc = xmlBuilder.build(new StringReader(outputXML));
					
					Element sourceXMLRoot = sourceXMLDoc.getRootElement();
					
					List outputObjectList = outputXMLDoc.getRootElement().getChildren();
					
					Iterator outputIt = outputObjectList.iterator();
					while (outputIt.hasNext()) {
						Element outputElement = (Element)outputIt.next();
						Iterator xmlIt = outputElement.getChildren().iterator();
						while (xmlIt.hasNext()) {
							Element xmlElement = (Element)xmlIt.next();
							String name = xmlElement.getName();
							if (sourceXMLRoot.getChild(name) != null) {
								sourceXMLRoot.removeChild(name);
								sourceXMLRoot.addContent((Element)xmlElement.clone());
							}
						}
					}
					
					XMLOutputter outputter = new XMLOutputter();
					outputter.setFormat(Format.getCompactFormat());
					sourceXML = outputter.outputString(sourceXMLDoc);
					
					
					sql = "update processXMLDocument set XML = ? where processid = ?";
					params = new Object[] { sourceXML, this.getProcess().getProcessID()};
					types = new int[] { Types.VARCHAR, Types.VARCHAR };
					this.executeUpdate();
				} else if (objectType == Constants.OBJECT_TYPE_DOC) {
					//servercomment System.out.println("no Doc type now!");

				} else if (objectType == Constants.OBJECT_TYPE_REF) {
					//servercomment System.out.println("no Ref type now!");

				} else if (objectType == Constants.OBJECT_TYPE_ARRAY) {
					//cyd add
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder.build(new StringReader(xmlStr));
					//Element root=xmlDoc.getRootElement();
					String value = xmlDoc.getRootElement().getChild("RETURN").getChild("VALUE").getValue();
					//servercomment System.out.println(value);
					
					sql = "update processArrayVariable set value = ? where processID = ? and objectID = ?";
					params = new Object[] { value, this.getProcess().getProcessID(),new Integer(objectID)};
					types = new int[] { Types.VARCHAR, Types.VARCHAR ,Types.INTEGER};
					this.executeUpdate(); 
					
					//
					sql = "select * from processArrayVariable where processid = ? and objectID = ?";
					params = new Object[] { this.getProcess().getProcessID(),new Integer(objectID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					//List pavList = (List) executeQuery(new MapListHandler());
					//Iterator iterator2 = pavList.iterator();
					
				}else {
					throw new WorkflowTransactionException(
							"Object type is out of range!");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateInvokeResult(String outputXML) throws Exception {
		sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
		params = new Object[] { outputXML, this.getProcess().getProcessID(),
				new Integer(curOutputXMLID) };
		types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
		executeUpdate();
	}
	
	private boolean isDataFlowActive(int dataflowID) throws SQLException{
		boolean isActive=false;
		sql = "select * from processflowobjectcontrol where processid = ? and FlowID = ?";
		params = new Object[] { this.getProcess().getProcessID(),
				new Integer(dataflowID) };
		types = new int[] { Types.VARCHAR, Types.INTEGER };
		List paimList = (List) executeQuery(new MapListHandler());
		Iterator paimIterator = paimList.iterator();
		while (paimIterator.hasNext()) {
			Map paimMap = (Map) paimIterator.next();
			String state=(String)paimMap.get("State");
			if("Active".equals(state))
				isActive=true;
		}
		return isActive;
	}
	
	private String getInputSystemXML(int applicationID) throws SQLException{
		int xmlID=0;
		String systemXML="";
		String serviceTable=null;
		if(applicationInvoke instanceof WebServiceInvoke) serviceTable="webservicemethod";
		else if (applicationInvoke instanceof CloudServiceInvoke) serviceTable="cloudservicemethod";
		else if (applicationInvoke instanceof CommonApplicationInvoke) serviceTable="commonapplicationmethod";
		else {
			logger.error("Not yet supported servicetype");
		}
		sql = "select * from "+serviceTable+" where applicationid = ?";
		params = new Object[] { applicationID};
		types = new int[] { Types.INTEGER };
		List paimList = (List) executeQuery(new MapListHandler());
		Iterator paimIterator = paimList.iterator();
		while (paimIterator.hasNext()) {
			Map paimMap = (Map) paimIterator.next();
			xmlID=(Integer)paimMap.get("InputXMLID");
		}
		sql = "select * from systemxmldocument where objectid = ?";
		params = new Object[] { xmlID};
		types = new int[] { Types.INTEGER };
		paimList = (List) executeQuery(new MapListHandler());
		paimIterator = paimList.iterator();
		while (paimIterator.hasNext()) {
			Map paimMap = (Map) paimIterator.next();
			systemXML=(String)paimMap.get("XML");
		}
		return systemXML;
	}
	
//	private String buildAllXSLT(List<String>xsltList,String baseXML){
//		XMLTreeNodewithXSLT tree=buildTree(baseXML);
//		for(String xslt:xsltList){
//			tree.addXSLTInfo(xslt);
//		}
//		return tree.toXSLT();
//	}
//	public XMLTreeNodewithXSLT buildTree(String baseXML){
//		Element root=xmlOperation.toDocument(baseXML).getRootElement();
//		return new XMLTreeNodewithXSLT(root);
//	}
	
	private Document buildAllXSLT(List<String>xsltList,String baseXML){
		Document baseDoc = xmlOperation.toDocument(baseXML);
		
		DefaultMutableTreeNode topTreeNode = getSchemaTreeNode(baseDoc);
		objectTreeModel = new DefaultTreeModel(topTreeNode);
		for(String xslt:xsltList){
			setAllNodes(xmlOperation.toDocument(xslt).getRootElement(),null);
		}
		return getTreeModelDocument(objectTreeModel);
	}
	/**
	 * 得到Schema树的root node
	 */
	private DefaultMutableTreeNode getSchemaTreeNode(Document docXML) {
		Element root = docXML.getRootElement();
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new XMLTreeNode(root.getName(), "element"));
		addNode(top, root);
		return top;
	}
	
	/**
	 * 思路：首先将element的属性作为parent的孩子树节点，然后再将element的孩子作为parent的孩子树节点。
	 * 
	 * @param node - 当前节点
	 * @param element - 与当前node对应的element
	 */
	private void addNode(DefaultMutableTreeNode parent, Element element) {
		/* add attribute name node */
		List attributeList = element.getAttributes();
		Iterator itAttr = attributeList.iterator();
		Enumeration enumChildren;
		boolean exist = false;
		DefaultMutableTreeNode defaultTreeNodeTemp = null;
		XMLTreeNode treeNode = null;
		
		while(itAttr.hasNext()) {
			Attribute attr = (Attribute)itAttr.next();
			
			/* 遍历树 */
			enumChildren = parent.children();
			exist = false;
			
			while(enumChildren.hasMoreElements()) {
				defaultTreeNodeTemp = (DefaultMutableTreeNode)enumChildren.nextElement();
				treeNode = (XMLTreeNode)defaultTreeNodeTemp.getUserObject();
				
				if(treeNode.gettype() == "attribute" && treeNode.getName().equals(attr.getName())) {
					exist = true;
					break;
				}
			}
			
			if(false == exist) {
				parent.add(new DefaultMutableTreeNode(new XMLTreeNode(attr.getName(), "attribute")));
			}
			
		}
		
		/* add element text node */
		List childList = element.getChildren();
		Iterator itEle = childList.iterator();
		
		while(itEle.hasNext()) {
			Element ele = (Element)itEle.next();
			
			/* 遍历树 */
			enumChildren = parent.children();
			exist = false;
			
			while(enumChildren.hasMoreElements()) {
				defaultTreeNodeTemp = (DefaultMutableTreeNode)enumChildren.nextElement();
				treeNode = (XMLTreeNode)defaultTreeNodeTemp.getUserObject();
				
				if(treeNode.gettype() == "element" && treeNode.getName().equals(ele.getName())) {
					exist = true;
					addNode(defaultTreeNodeTemp, ele);
				}
			}
			
			if(false == exist) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new XMLTreeNode(ele.getName(), "element"));
				parent.add(node);
				addNode(node, ele);
			}			
		}
	}
	
//	public Document getXMLFromSchema(Document schemaDoc) {
//		logger.info("run getXMLFromSchema()");
//		if (schemaDoc == null) {
//			logger.info("schemaDoc is null!");
//		}
//		Element schemaRoot  = schemaDoc.getRootElement();
//		Element schemaChild = schemaRoot.getChild("element");
//		
//		List <Element> complexTypeList = schemaRoot.getChildren("complexType");
//		if (schemaChild == null) {
//			logger.info("------------------------null");
//			logger.info(getString(schemaDoc));
//		}
//		Element xmlRoot = getComplexTypeXML(complexTypeList, schemaChild);
//		Document xmlDoc = new Document(xmlRoot);
//
//		if (logger.isInfoEnabled()) {
//			String partSchema = getString(xmlDoc);
//			logger.info("根据拼合后的Schema生成相应的XML：\n" + partSchema);
//		}
//		
//		return xmlDoc;
//	}
	
//	/**
//	 * 检查element中的type属性，如果该属性名与complexTypeList中的complexType name属性的值相同则说明该type属性值为一个complexType
//	 * 同时用该complexType来代替该type，生成XML。递归调用从而生成整个Schema的XML
//	 * 
//	 * @param complexTypeList - complexType列表
//	 * @param element - Schema根节点的第一个element孩子节点。
//	 * @return
//	 */
//	private Element getComplexTypeXML (List <Element> complexTypeList, Element element) {
//		logger.info("run getComplexTypeXML()");
//		List <Element> elementList   = new ArrayList<Element>();
//		List <Element> attributeList = new ArrayList<Element>();
//		Element root = null;
//		boolean isComplexType = false;
//		
//		if (null == element) {
//			logger.info("element 为空");
//		} else{
////			
//			if (null == element.getAttributeValue("type")) {
//				root = new Element(element.getAttributeValue("name"));
//				Element innerComplexTypeEle = element.getChild("complexType");
//				Element innerSequenceEle = innerComplexTypeEle.getChild("sequence");
//				
//				if (null == innerSequenceEle) {
//					elementList = innerComplexTypeEle.getChildren("element");
//				} else {
//					elementList = innerSequenceEle.getChildren("element");
//				}
//				
//				// 暂时只考虑一个attribute的情况
//				Element innerAttributeEle = innerComplexTypeEle.getChild("attribute");
//				if (null == innerAttributeEle) {
//					attributeList = null;
//				} else {
//					attributeList = innerComplexTypeEle.getChildren("attribute");
//				}
//				
//				if (elementList != null) {
//					for (Element childEle:elementList) {
//						root.addContent(getComplexTypeXML(complexTypeList, childEle));
//					}
//				}
//				
//				
//				if (attributeList != null) {
//					for (Element childEle:attributeList) {
//						root.setAttribute(childEle.getAttributeValue("name"), childEle.getAttributeValue("fixed"));
//					}
//				}
//				
//				isComplexType = true;
//			} else {
//				for (Element complexTypeEle:complexTypeList) {
//					if (complexTypeEle.getAttributeValue("name").equalsIgnoreCase(element.getAttributeValue("type"))){
//						isComplexType = true;
//						
//						root = new Element(element.getAttributeValue("name"));
//						Element sequenceEle = complexTypeEle.getChild("sequence");
//						
//						if (null == sequenceEle) {
//							elementList = complexTypeEle.getChildren("element");
//						} else {
//							elementList = sequenceEle.getChildren("element");
//						}
//						
//						Element attributeEle = complexTypeEle.getChild("attribute");
//						
//						if (null == attributeEle) {
//							attributeList = null;
//						} else {
//							attributeList = complexTypeEle.getChildren("attribute");
//						}
//						
//						// 将complexType格式化为XML
//						if (null != elementList) {
//							for (Element childEle:elementList) {
//								root.addContent(getComplexTypeXML(complexTypeList, childEle));
//							}
//						}
//						
//						if (null != attributeList) {
//							for (Element childEle:attributeList) {
//								root.setAttribute(childEle.getAttributeValue("name"), childEle.getAttributeValue("fixed"));
//							}
//						}
//						break;
//					} 
//				}
//				
//				if (false == isComplexType) {
//					root = new Element(element.getAttributeValue("name"));
//				}
//				
//				if (null == element.getAttribute("fixed")) {
//					
//				} else {
//					
//				}
//			}
//		}	
//		return root;
//	}
//	
//	/**
//	 * get Document from String
//	 * @param strSchema
//	 * @return
//	 */
//	private Document getDocument (String strSchema) {
//		Document schemaDoc = null;
//		
//		try {
//			Reader inputSchema = new StringReader(strSchema);
//			SAXBuilder sb = new SAXBuilder();
//			schemaDoc = sb.build(inputSchema);
//		} catch (JDOMException e) {
//			logger.error(e);
//		} catch (IOException e) {
//			logger.error(e);
//		}
//		return schemaDoc;
//	}
	
//	/**
//	 * get String from Document
//	 * @param doc
//	 * @return
//	 */
//	private String getString(Document doc) {
//		Format format = Format.getCompactFormat();
//		format.setEncoding("UTF-8");
//	    format.setIndent("   ");
//	    XMLOutputter outp = new XMLOutputter(format);
//	    
//	    if(null == doc) {
//	    	return "";
//	    } else {
//	    	String strDocument = outp.outputString(doc);
//	    	return strDocument;
//	    }
//	}
	
	/**********************************************************************************************************************/
	public class XMLTreeNode{ 
		private String name; 
		private String type; 
		private TreePath treePath;
		private String xsltInstruction;//chan 7-5
		
		public XMLTreeNode(String name, String type){ 
			this.name = name; 
			this.type = type; 
			this.treePath = null;
			this.xsltInstruction = ""; //chan 7-5
		} 
		
		public void setName(String Name){ 
			this.name=Name; 
		} 

		public String getName(){ 
			return name; 
		} 

		@Override
		public String toString(){ 
			return name; 
		} 
		
		public String gettype(){ 
			return type; 
		} 
		
		public void settype(String type){ 
			this.type = type; 
		}

		public TreePath getTreePath() {
			return treePath;
		}

		public void setTreePath(TreePath treePath) {
			this.treePath = treePath;
		}
		//chan 7-5
		public String getXsltInstruction() {
			return xsltInstruction;
		}

		public void setXsltInstruction(String xsltInstruction) {
			this.xsltInstruction = xsltInstruction;
		} 
	} 

	/*******************************************************************************************************/
	// Traverse all nodes in tree
	public Document getTreeModelDocument(TreeModel rightTreeModel) {
		DefaultMutableTreeNode rootTreeNode = (DefaultMutableTreeNode)rightTreeModel.getRoot();
		Element root = new Element(((XMLTreeNode)rootTreeNode.getUserObject()).getName());
		
		/* xsl:stylesheet */
		root = new Element("stylesheet");
		root.setNamespace(xsl);
		root.addNamespaceDeclaration(xsl);
		root.setAttribute("version", "1.0");
		Document xsltDoc = new Document(root);
		
		Element element = new Element("template", xsl);//<xsl:template match="/">
		element.setAttribute("match", "/");
		
		root.addContent(element);//<xsl:template match="/">

		Element childRoot = new Element(((XMLTreeNode)(rootTreeNode.getUserObject())).getName());//<Catalog>
		element.addContent(childRoot);//<Catalog>
		isMapping = false;
		visitAllNodes(childRoot, rootTreeNode);
		
		if (true == isMapping) {
			return xsltDoc;
		} else {
			return null;
		}		
	}

	/**
	 * 根据右边树中所保存对应关系信息生成XSLT
	 * @param eleNode
	 * @param treeNode
	 */
	public void visitAllNodes(Element eleNode, DefaultMutableTreeNode treeNode) {
		// node is visited exactly once
		if (treeNode.getChildCount() >= 0) {
			//chan 7-5
			for (Enumeration e = treeNode.children(); e.hasMoreElements(); ) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
				XMLTreeNode rightNode = (XMLTreeNode)n.getUserObject();
				Element child = new Element(rightNode.getName());//先为子节点实例化Element，然后再实例化<xsl:>
//				if(rightNode.getTreePath() != null){	
//					if(n.isLeaf()||rightNode.getTreePath().toString().contains("VALUE")==true){
//						DefaultMutableTreeNode leftTreeNode = (DefaultMutableTreeNode)rightNode.getTreePath().getLastPathComponent();
//						XMLTreeNode leftNode = (XMLTreeNode)leftTreeNode.getUserObject();
//						Element xslLeafElement;
//						if(leftNode.getXsltInstruction().equals("for-each")){
//							Element forEach=new Element("for-each",xsl);
//							forEach.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
//							xslLeafElement = new Element("value-of", xsl);
//							isMapping = true;
//							xslLeafElement.setAttribute("select", ".");
//							eleNode.addContent(forEach);
//							forEach.addContent(child);
//						}
//						else
//						{
//							eleNode.addContent(child);
//							xslLeafElement = new Element("value-of", xsl);
//							if(leftNode.gettype().equalsIgnoreCase("attribute")){
//								isMapping = true;
//								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/@" + leftNode.getName());
//							} else if(leftNode.gettype().equalsIgnoreCase("element")){
//								isMapping = true;
//								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
//							}
//						}
//						child.addContent(xslLeafElement);
//					}
//				}else{
//					eleNode.addContent(child);
//					visitAllNodes(child, n);
//				}

//				if(n.isLeaf()){
//					if(rightNode.getTreePath() != null){	
//						DefaultMutableTreeNode leftTreeNode = (DefaultMutableTreeNode)rightNode.getTreePath().getLastPathComponent();
//						XMLTreeNode leftNode = (XMLTreeNode)leftTreeNode.getUserObject();
//						Element xslLeafElement;
//						if(leftNode.getXsltInstruction().equals("for-each")){
//							Element forEach=new Element("for-each",xsl);
//							forEach.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
//							xslLeafElement = new Element("value-of", xsl);
//							isMapping = true;
//							xslLeafElement.setAttribute("select", ".");
//							eleNode.addContent(forEach);
//							forEach.addContent(child);
//						}
//						else
//						{
//							eleNode.addContent(child);
//							xslLeafElement = new Element("value-of", xsl);
//							if(leftNode.gettype().equalsIgnoreCase("attribute")){
//								isMapping = true;
//								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/@" + leftNode.getName());
//							} else if(leftNode.gettype().equalsIgnoreCase("element")){
//								isMapping = true;
//								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
//							}
//						}
//						child.addContent(xslLeafElement);
//					}
//				}else{
//					eleNode.addContent(child);
//					visitAllNodes(child, n);
//				}
				if(rightNode.getTreePath() != null){	
				if(n.isLeaf()||rightNode.getTreePath().toString().contains("VALUE")==true){
					
						DefaultMutableTreeNode leftTreeNode = (DefaultMutableTreeNode)rightNode.getTreePath().getLastPathComponent();
						XMLTreeNode leftNode = (XMLTreeNode)leftTreeNode.getUserObject();
						Element xslLeafElement;
						if(rightNode.getXsltInstruction().equals("for-each")){
							Element forEach=new Element("for-each",xsl);
							forEach.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
							xslLeafElement = new Element("value-of", xsl);
							isMapping = true;
							xslLeafElement.setAttribute("select", ".");
							eleNode.addContent(forEach);
							forEach.addContent(child);
						}
						else
						{
							eleNode.addContent(child);
							xslLeafElement = new Element("value-of", xsl);
							if(leftNode.gettype().equalsIgnoreCase("attribute")){
								isMapping = true;
								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/@" + leftNode.getName());
							} else if(leftNode.gettype().equalsIgnoreCase("element")){
								isMapping = true;
								xslLeafElement.setAttribute("select", getParentTreePathString(rightNode.getTreePath()) + "/" + leftNode.getName());
							}
						}
						child.addContent(xslLeafElement);
					}
				}else{
					eleNode.addContent(child);
					visitAllNodes(child, n);
				}
			}
		}
	}
	
	/**
	 * 将XSLT信息初始化到右边树中，即将信息写入objectTreeModel中
	 * @param eleNode
	 * @param treeNode
	 */
	public void setAllNodes(Element eleNode, String forEachPath) {
		List childList = eleNode.getChildren();
		//chan 7-5
		boolean forEach=false;
//		String forEachPath="";
		if(childList.size() > 0) {
			
			for(int i = 0 ; i < childList.size(); i++) {
				Element childElement = (Element)childList.get(i);
				if(childElement.getName().equalsIgnoreCase("for-each")) {
			//		forEach=true;
					forEachPath=childElement.getAttribute("select").getValue();
					logger.info(forEachPath);
					setAllNodes(childElement,forEachPath);
				}
				else if(childElement.getName().equalsIgnoreCase("value-of")) {
					logger.info("找到了value-of！");
					String elementPath = "";

					logger.info(forEachPath);
					TreePath treePath = new TreePath("");
					logger.info(childElement.getText());
					String tmpStr=childElement.getAttribute("select").getValue();
					if(forEachPath!=null){
						forEach=true;
						if(tmpStr!=null&&!tmpStr.equals(".")){
							tmpStr=forEachPath+"/"+tmpStr;
						}
						else
							tmpStr=forEachPath;
					}
					logger.info(tmpStr);

					treePath=getTreePath(tmpStr);
					while(childElement.getParentElement() != null) {
						if(childElement.getName().equalsIgnoreCase("stylesheet")||
								childElement.getName().equalsIgnoreCase("template")||
								childElement.getName().equalsIgnoreCase("value-of")) {
							childElement = childElement.getParentElement();
							continue;
						}
						elementPath = childElement.getName() + "," + elementPath;
						childElement = childElement.getParentElement();
					}
					
					String [] path = elementPath.trim().split(",");
					setTreeNodeAttribute((DefaultMutableTreeNode)objectTreeModel.getRoot(), path, treePath,forEach);
				} else {
					setAllNodes(childElement,forEachPath);
				}
			}
		}
	}
	
	/**
	 * 根据elementPath提供的叶子节点路径信息设置该叶子节点的treePath属性。
	 *   ((XMLTreeNode)leafTreeNode.getUserObject()).setTreePath(treePath);
	 * @param treeNode - 树的根节点
	 * @param elementPath - 要设置treePath的节点路径
	 * @param treePath - 
	 */
	private void setTreeNodeAttribute(DefaultMutableTreeNode treeNode, String [] elementPath, TreePath treePath, boolean ifForEach) {
		DefaultMutableTreeNode leafTreeNode = treeNode;
		
		for(String treeNodeName:elementPath) {
			
			for(Enumeration e = leafTreeNode.children(); e.hasMoreElements(); ) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();
				XMLTreeNode xmlTreeNode = (XMLTreeNode)childNode.getUserObject();
				
				if((xmlTreeNode.getName().equalsIgnoreCase(treeNodeName))) {
					leafTreeNode = childNode;
				}
			}
		}
		
		((XMLTreeNode)leafTreeNode.getUserObject()).setTreePath(treePath);
		if(ifForEach)
			((XMLTreeNode)leafTreeNode.getUserObject()).setXsltInstruction("for-each");
	}
	
	/**
	 * 根据treePath得到该路径最后一个节点的父节点路径信息。
	 * @param treePath
	 * @return
	 */
	private String getParentTreePathString(TreePath treePath){
		String strTreePath = treePath.getParentPath().toString();
		strTreePath = strTreePath.substring(1, strTreePath.length()-1);
		strTreePath = strTreePath.replaceAll(" ", "");
		strTreePath = strTreePath.replaceAll(",", "/");

		return strTreePath;
	}
	private TreePath getTreePath(String xpath){
		Vector <String>childVector = new Vector<String>();
		String [] childArray = xpath.split("/");
		logger.info(childArray.toString());	
		for(String childStr:childArray) {
			if(!childStr.equals("")){
				if(childStr.trim().charAt(0) == '@') {
					childStr.substring(1);
				}
				childVector.add(childStr.trim());
			}
		}
		childVector.remove(0);
		
		return getTreePath((DefaultMutableTreeNode)sourceTreeModel.getRoot(), childVector);
			
	}
	
	/**
	 * 目的：得到vnode给出的路径的节点的TreePath。
	 * 调用该方法时，currnode为树的根节点，vnode为与该树相对应（结构对应）的路径信息（其中不包括根节点）
	 * @param currnod - 当前的树节点
	 * @param vnode   -  
	 * @return - TreePath
	 */
	private TreePath getTreePath(DefaultMutableTreeNode currnode, Vector vnode){   
        int childcount = currnode.getChildCount();
        String strnode = vnode.elementAt(0).toString();   
        DefaultMutableTreeNode child = null;
        TreePath treePath = null;
        
        for(int i=0; i<childcount; i++){
            child = (DefaultMutableTreeNode)currnode.getChildAt(i);
            
            if(strnode.equals(child.toString())) {
            	logger.info(child);
                break;
            }
        }
        
        if(child != null){   
        	vnode.removeElementAt(0);
        	if(vnode.size()>0){   
        		treePath = getTreePath(child, vnode);
        	}else{
        		
        		treePath = new TreePath(child.getPath());
        	}   
        }
        logger.info("TreePath: " + new TreePath(child.getPath()));
        return treePath;
    }
	
	
//	public XMLTreeNodewithXSLT buildTree(String baseXML){
//		Element root=xmlOperation.toDocument(baseXML).getRootElement();
//		return new XMLTreeNodewithXSLT(root);
//	}
	
	/*
	 * private void invokeApplication(int applicationID, int invokeType, String
	 * inputMap) throws Exception { int applicationType =
	 * getApplicationType(applicationID); if (applicationType ==
	 * APPLICATION_WEB_SERVICE) { invokeWebService(applicationID, invokeType,
	 * inputMap); } }
	 * 
	 * private void invokeWebService(final int applicationID, int invokeType,
	 * String inputXML) throws Exception { sql = "SELECT WSDLURL, MethodName,
	 * UserName, Password FROM WebServiceMethod WHERE ApplicationID=?"; params =
	 * new Object[] {new Integer(applicationID)}; types = new int[]
	 * {Types.INTEGER}; Map saiMap = (Map) executeQuery(new MapHandler()); if
	 * (saiMap == null) throw new Exception("There is no web service for
	 * invocation!");
	 * 
	 * final Document document = DocumentHelper.createDocument(); Element
	 * webServiceElement = document.addElement("WEBSERVICE");
	 * 
	 * Element wsdlElement = webServiceElement.addElement("WSDLURL");
	 * wsdlElement.addText((String) saiMap.get("WSDLURL"));
	 * 
	 * Element methodElement = webServiceElement.addElement("METHODNAME");
	 * methodElement.addText((String) saiMap.get("MethodName"));
	 * 
	 * Document inputDocument = DocumentHelper.parseText(inputXML); Element
	 * inputElement = inputDocument.getRootElement();
	 * webServiceElement.add(inputElement);
	 * 
	 * Element usernameElement = webServiceElement.addElement("USERNAME");
	 * usernameElement.addText((String) saiMap.get("UserName"));
	 * 
	 * Element passwordElement = webServiceElement.addElement("PASSWORD");
	 * passwordElement.addText((String) saiMap.get("Password"));
	 * 
	 * if (invokeType == Constants.INVOKE_TYPE_ASYNCHRONISM) { Thread workThread =
	 * new Thread(new Runnable() {
	 * 
	 * public void run() { try { executeWebService(document.asXML(),
	 * applicationID); } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * }); workThread.start(); } else { executeWebService(document.asXML(),
	 * applicationID); } }
	 * 
	 * private void executeWebService(String inputXML, int applicationID) throws
	 * SQLException { WebserviceAdapter adapter = new WebserviceAdapter(false);
	 * String outputXML = adapter.getOutputXML(inputXML, applicationID); sql =
	 * "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
	 * params = new Object[] {outputXML, this.getProcess().getProcessID(), new
	 * Integer(this.curOutputXMLID)}; types = new int[] {Types.VARCHAR,
	 * Types.VARCHAR, Types.INTEGER}; executeUpdate(); }
	 * 
	 * private int getApplicationType(int applicationID) throws Exception { sql =
	 * "SELECT ApplicationType FROM SystemApplicationInformation WHERE
	 * ApplicationID=?"; params = new Object[] {new Integer(applicationID)};
	 * types = new int[] {Types.INTEGER}; Map saiMap = (Map) executeQuery(new
	 * MapHandler()); return ((Integer)
	 * saiMap.get("ApplicationType")).intValue(); }
	 */
}
