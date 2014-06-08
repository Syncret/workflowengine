//多胎活动

package cit.workflow.elements.activities;

/*
 * Created on 2011-07-05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.activities.InvokeApplicationActivity.XMLTreeNode;
import cit.workflow.elements.applications.ApplicationInvoke;
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
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;

// //////////////////////////
/**
 * @author cyd
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class DoutaiActivity extends NodeActivity {

	public static final int APPLICATION_WEB_SERVICE = 4;

	private int curOutputXMLID;
	private static Logger logger = Logger
			.getLogger(InvokeApplicationActivity.class); // cxz:计算机记录器logger
	public XMLOperation xmlOperation = new XMLOperation();

	public DoutaiActivity(Connection conn, Process process, int activityID,
			ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}

	public void execute() throws Exception {

		InformationPane.writeln("<---------- DoutaiActivity Excuted -------------->");
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

			final ApplicationInvoke applicaitonInvoke = ElementFactory
					.createApplication(
							conn,
							((Integer) paiaMap.get("ApplicationID")).intValue(),
							this.getProcess().getProcessID());

			sql = "select * from processactivityinputmapping where processid = ? and activityid = ?";
			params = new Object[] { this.getProcess().getProcessID(),
					new Integer(this.activityID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			List paimList = (List) executeQuery(new MapListHandler());
			Iterator paimIterator = paimList.iterator();
			while (paimIterator.hasNext()) {
				Map paimMap = (Map) paimIterator.next();
				String inputXML = (String) paimMap.get("inputXML");
				logger.debug("inputXML:");
				logger.debug(inputXML);
				String xsltStr = (String) paimMap.get("xslt");
				logger.debug("XSLT:");
				logger.debug(xsltStr);

				if (((Integer) paiaMap.get("InvocationType")).intValue() == Constants.INVOKE_TYPE_ASYNCHRONISM) {
					// 异步调用 待修改
					String processInputXML = addValueToXML(inputXML);
					String wsInputXML = getXMLByXSLT(processInputXML, xsltStr);
					// wsInputXML = formatInputXML(wsInputXML);

					String outputXML = applicaitonInvoke.invoke(wsInputXML); // 异步调用
					updateOutputObject(outputXML);
				} else {

					String processInputXML = addValueToXML(inputXML);
					logger.debug("After addvalueToXML, processInputXML is:");
					logger.debug(processInputXML);
					logger.info("DuotaiActivity: Start Split the array and call the applicaiton");

//					int arraysize = getsizeofArray((processInputXML);
					

					String swsInputXML = null;
					String wsInputXML = null;
					String outputvalue = null;
					String outputXML=null;
					
					wsInputXML = getXMLByXSLT(processInputXML, xsltStr);
					int arraysize = getsizeofArray(wsInputXML);
					
					StringBuffer str = new StringBuffer();
					for (int i = 0; i < arraysize; i++) {
//						sprocessInputXML = splitArrayXML(processInputXML, i);
						swsInputXML = splitArrayXML(wsInputXML, i);
//						wsInputXML = getXMLByXSLT(sprocessInputXML, xsltStr);
						//servercomment System.out.println("wsInputXML:");
						//servercomment System.out.println(xmlOperation.toString(xmlOperation.toDocument(swsInputXML)));
						long starttime=System.currentTimeMillis();
						
						Thread.sleep(500);
						outputXML = applicaitonInvoke.invoke(swsInputXML);// 同步调用
						
						long endtime=System.currentTimeMillis();
						processManager.getTimeRecoder().addWaitTime(endtime-starttime);
						
						//servercomment System.out.println("The outputXML is:");
						//servercomment System.out.println(outputXML);

						// 合成输出
						outputvalue = getvalue(outputXML);
						str.append(outputvalue + " ");
					}
					outputXML=getoutputXML(outputXML,str.toString());
					updateOutputObject(outputXML);
					// wsInputXML = formatInputXML(wsInputXML);

					// //servercomment System.out.println("wsInputXML after formating :");
					// //servercomment System.out.println(xmlOperation.toString(xmlOperation.toDocument(wsInputXML)));

					changeState(Constants.ACTIVITY_STATE_RUNNING,
							Constants.ACTIVITY_STATE_COMPLETED);

					/*
					 * changeState(Constants.ACTIVITY_STATE_RUNNING,
					 * Constants.ACTIVITY_STATE_OVERTIME);
					 */
				}
			}
		}
	}
private String getoutputXML(String xmlStr,String value){
	String returnXML=null;
	try {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(xmlStr));
		Element root = doc.getRootElement();
		Element returntag = (Element) root.getChildren().get(0);
		Element typetag = (Element) returntag.getChild("TYPE");
		typetag.setText("array");
		Element valuetag = (Element) returntag.getChild("VALUE");
		valuetag.removeContent();
		String[] outputvalue=value.split(" ");
		for (int j = 0; j < outputvalue.length; j++){
			Element tempElement = new Element("STRING");
			tempElement.setText(outputvalue[j]);
			valuetag.addContent(tempElement);
		}
		
		returnXML = xmlOperation.toString(doc);
		return returnXML;
	} catch (JDOMException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
}
	private String getvalue(String xmlStr) {
		String soutputvalue=null;
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			doc = builder.build(new StringReader(xmlStr));
			Element root = doc.getRootElement();
			Element returntag = (Element) root.getChildren().get(0);
			Element typetag = (Element) returntag.getChild("TYPE");
			String type=typetag.getText();
			Element valuetag = (Element) returntag.getChild("VALUE");
			if(type.equalsIgnoreCase("array")){
				List values=valuetag.getChildren();
				Element tvalue;
				StringBuffer str = new StringBuffer();
				for(int i=0;i<values.size();i++){
					tvalue=(Element)values.get(i);
					str.append(tvalue.getText()+"<string>");
				}
				soutputvalue=str.toString();	
				
			}
			else{
				soutputvalue=valuetag.getText();
			}
			return soutputvalue;
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private int getsizeofArray(String xmlStr) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			doc = builder.build(new StringReader(xmlStr));
			Element root = doc.getRootElement();
			Element valuetag = (Element) root.getChildren().get(0);
			Element evalues = (Element) valuetag.getChild("VALUE");
			// String values = valuetag.getChild("value")..getText();
			String values = evalues.getText();
			int size = values.split("\\s+").length;

			return size;
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	

	private String splitArrayXML(String xmlStr, int elementid) {

		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			doc = builder.build(new StringReader(xmlStr));
			Element root = doc.getRootElement();
			Element valuetag = (Element) root.getChildren().get(0);
			String values = valuetag.getChild("VALUE").getText();
			// String[] temp=values.split("\\s+");
			String newvalue = values.split("\\s+")[elementid];
			valuetag.getChild("VALUE").setText(newvalue);
			valuetag.getChild("TYPE").setText("string");
			XMLOutputter outputter = new XMLOutputter();
			return outputter.outputString(doc);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

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
						params = new Object[] {
								this.getProcess().getProcessID(),
								new Integer(objectID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map pivMap = (Map) executeQuery(new MapHandler());
						int valueType = ((Integer) pivMap.get("ValueType"))
								.intValue();

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
						params = new Object[] {
								this.getProcess().getProcessID(),
								new Integer(objectID) };
						types = new int[] { Types.VARCHAR, Types.INTEGER };
						Map pivMap = (Map) executeQuery(new MapHandler());
						int valueType = ((Integer) pivMap.get("ValueType"))
								.intValue();

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
						// Element xmlObjectEt = new Element("a");
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

					} else {
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
								new Integer(processManager.getCurrentUser()
										.getActorID()).toString());
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

	private void updateOutputObject(String xmlStr) {
		try {
			sql = "select * from processactivityoutputmapping where processid = ? and ActivityID = ?";
			params = new Object[] { this.getProcess().getProcessID(),
					new Integer(this.activityID) };
			types = new int[] { Types.VARCHAR, Types.INTEGER };
			List paomList = (List) executeQuery(new MapListHandler());
			Iterator iterator = paomList.iterator();

			int objectID;
			int objectType;
			while (iterator.hasNext()) {
				Map paomMap = (Map) iterator.next();
				String outputXslt=(String)paomMap.get("XSLT");
				if(outputXslt==null||outputXslt.equals(""))continue;
				objectID = (Integer) paomMap.get("ObjectID");
				ProcessObject processObject = ElementFactory
						.createProcessObject(conn, getProcess().getProcessID(),
								objectID);
				objectType = processObject.getObjectType();

				if (objectType == Constants.OBJECT_TYPE_INHERENT) {

					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder
							.build(new StringReader(xmlStr));
					String value = xmlDoc.getRootElement().getChild("RETURN")
							.getChild("VALUE").getValue();
					//servercomment System.out.println(value);

					sql = "update processInherentVariable set value = ? where processid = ? and objectid = ?";
					params = new Object[] { value,
							this.getProcess().getProcessID(),
							new Integer(objectID) };
					types = new int[] { Types.VARCHAR, Types.VARCHAR,
							Types.INTEGER };

					this.executeUpdate();

				} else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder
							.build(new StringReader(xmlStr));
					String value = xmlDoc.getRootElement()
							.getChild("PARAMETER").getChild("VALUE").getValue();
					//servercomment System.out.println(value);

					sql = "update processObjectVariable set value = ? where processid = ?";
					params = new Object[] { value,
							this.getProcess().getProcessID() };
					types = new int[] { Types.VARCHAR, Types.VARCHAR };
					this.executeUpdate();
				} else if (objectType == Constants.OBJECT_TYPE_XML) {
					String xslt = (String) paomMap.get("XSLT");
					String outputXML = getXMLByXSLT(xmlStr, xslt);

					sql = "select * from processXMLDocument where processid = ? and objectID = ?";
					params = new Object[] { this.getProcess().getProcessID(),
							new Integer(objectID) };
					types = new int[] { Types.VARCHAR, Types.INTEGER };
					Map pxdMap = (Map) executeQuery(new MapHandler());
					String sourceXML = (String) pxdMap.get("xml");

					SAXBuilder xmlBuilder = new SAXBuilder();
					Document sourceXMLDoc = xmlBuilder.build(new StringReader(
							sourceXML));
					Document outputXMLDoc = xmlBuilder.build(new StringReader(
							outputXML));

					Element sourceXMLRoot = sourceXMLDoc.getRootElement();

					List outputObjectList = outputXMLDoc.getRootElement()
							.getChildren();

					Iterator outputIt = outputObjectList.iterator();
					while (outputIt.hasNext()) {
						Element outputElement = (Element) outputIt.next();
						Iterator xmlIt = outputElement.getChildren().iterator();
						while (xmlIt.hasNext()) {
							Element xmlElement = (Element) xmlIt.next();
							String name = xmlElement.getName();
							if (sourceXMLRoot.getChild(name) != null) {
								sourceXMLRoot.removeChild(name);
								sourceXMLRoot.addContent((Element) xmlElement
										.clone());
							}
						}
					}

					XMLOutputter outputter = new XMLOutputter();
					outputter.setFormat(Format.getCompactFormat());
					sourceXML = outputter.outputString(sourceXMLDoc);

					sql = "update processXMLDocument set XML = ? where processid = ?";
					params = new Object[] { sourceXML,
							this.getProcess().getProcessID() };
					types = new int[] { Types.VARCHAR, Types.VARCHAR };
					this.executeUpdate();
				} else if (objectType == Constants.OBJECT_TYPE_DOC) {
					//servercomment System.out.println("no Doc type now!");

				} else if (objectType == Constants.OBJECT_TYPE_REF) {
					//servercomment System.out.println("no Ref type now!");

				}else if (objectType == Constants.OBJECT_TYPE_ARRAY) {
					
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
					List pavList = (List) executeQuery(new MapListHandler());
					Iterator iterator2 = pavList.iterator();
					
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
	 * if (invokeType == Constants.INVOKE_TYPE_ASYNCHRONISM) { Thread workThread
	 * = new Thread(new Runnable() {
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
	 * private int getApplicationType(int applicationID) throws Exception { sql
	 * = "SELECT ApplicationType FROM SystemApplicationInformation WHERE
	 * ApplicationID=?"; params = new Object[] {new Integer(applicationID)};
	 * types = new int[] {Types.INTEGER}; Map saiMap = (Map) executeQuery(new
	 * MapHandler()); return ((Integer)
	 * saiMap.get("ApplicationType")).intValue(); }
	 */
	
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

}
