/*
 * Created on 2004-11-22
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;

import cit.workflow.Constants;
import cit.workflow.elements.applications.DynamicInvoker;
import cit.workflow.utils.XMLOperation;
import cit.workflow.view.InformationPane;

/**
 * @author Administrator
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class WebserviceAdapter extends Adapter implements IAdapter {
	/*
	 * Private Variables
	 */

	// The parameter for the URL of the web service WSDL.
	private String wsdlURL;
	private String serviceName;
	private String portName;
	private String processID;
	private DynamicInvoker invoker = null;
	private XMLOperation xmlOperation = null;

	public WebserviceAdapter(String processID, int applicationId) {
		this.processID = processID;
		this.applicationId = applicationId;
		xmlOperation = new XMLOperation();
	}

	public String getOutputXML(String inputXML, String outputXML) {
		if (inputXML.equals("") || outputXML.equals("")) {
			return (null);
		} else {
			returnXMLDoc = getOutputXML(xmlOperation.toDocument(inputXML),
					xmlOperation.toDocument(outputXML));
			return xmlOperation.toString(returnXMLDoc);
		}
	}

	public Document getOutputXML(Document inputXMLDoc, Document outputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
		this.outputXMLDoc = outputXMLDoc;

		//servercomment System.out.println("inputXMLDoc Doc = "+ xmlOperation.toString(inputXMLDoc));
		//servercomment System.out.println("outputXMLDoc Doc = "+ xmlOperation.toString(outputXMLDoc));

		if (inputXMLDoc == null || inputXMLDoc == null) {
			return null;
		}

		initAdapterInput(inputXMLDoc);
		initAdapterOutput(outputXMLDoc);

		//servercomment System.out.println("cyd debuging");

		invokeWebService();
		return (returnXMLDoc);
	}

	/**
	 * 
	 */
	protected String AppId2XML() {
		String s = "";
		String connstr = "";
		int outId = 0;
		PreparedStatement pstmt = null;
		getConnection();

		try {
			connstr = "select OutputXMLDocumentId from "
					+ "ProcessWebserviceApplication where ApplicationID=? AND "
					+ "ProcessID=?";
			pstmt = connection.prepareStatement(connstr);
			pstmt.setLong(1, this.applicationId);
			pstmt.setString(2, this.processID);
			ResultSet rs = pstmt.executeQuery();
			if (rs == null) {
				logger.info("ResulSet is null");
			}
			if (rs.next()) {
				outId = rs.getInt(1);
				logger.info("OutputXMLDocumentId: " + outId);
			}
			rs.close();

			if (outId > 0) {
				connstr = "select SystemXMLDocument.XML "
						+ "from SystemXMLDocument, ProcessInformation "
						+ "where SystemXMLDocument.AgentID=ProcessInformation.AgentID "
						+ "AND SystemXMLDocument.XMLID=? "
						+ "AND ProcessInformation.ProcessID=? ";
				pstmt = connection.prepareStatement(connstr);
				pstmt.setInt(1, outId);
				pstmt.setString(2, this.processID);
				rs = pstmt.executeQuery();
				if (rs.next())
					s = rs.getString(1);
				rs.close();
			}
			pstmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		closeConnection();
		return (s);
	}

	@Override
	protected void initAdapterInput(Document inputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
		Element root = inputXMLDoc.getRootElement();
		getInfoFromNode(root, true);
	}

	@Override
	protected void initAdapterOutput(Document outputXMLDoc) {
		this.outputXMLDoc = outputXMLDoc;
		Element root = outputXMLDoc.getRootElement();
		getInfoFromNode(root, false);
	}

	/*
	 * 解析DOM文档(递归调用完所有节点)
	 * 若inFlag==true:表示是解析Input端口参数的;若inFlag==false:表示是解析Output端口参数的
	 */
	protected void getInfoFromNode(Element elementNode, boolean isInput) {
		List<Element> childNodes = null;
		String text = elementNode.getName();

		if (text.compareToIgnoreCase("WEBSERVICE") == 0) {
			childNodes = elementNode.getChildren();
			for (Element childNode : childNodes) {
				getInfoFromNode(childNode, isInput);
			}
		} else if (text.compareToIgnoreCase("WSDLURL") == 0) {

			wsdlURL = elementNode.getValue();
			//servercomment System.out.println("wsdlURL: " + wsdlURL);

		} else if (text.compareToIgnoreCase("SERVICENAME") == 0) {

			serviceName = elementNode.getValue();
			//servercomment System.out.println("serviceName: " + serviceName);

		} else if (text.compareToIgnoreCase("PORTNAME") == 0) {

			portName = elementNode.getValue();
			//servercomment System.out.println("portName: " + portName);

		} else if (text.compareToIgnoreCase("METHODNAME") == 0) {

			methodName = elementNode.getValue();
			//servercomment System.out.println("methodName: " + methodName);

		} else if (text.compareToIgnoreCase("USERNAME") == 0) {

			userName = elementNode.getValue();
			//servercomment System.out.println("userName: " + userName);

		} else if (text.compareToIgnoreCase("PASSWORD") == 0) {

			password = elementNode.getValue();
			//servercomment System.out.println("password: " + password);

		} else if (text.compareToIgnoreCase("INPUT") == 0) {

			childNodes = elementNode.getChildren();
			inNum = childNodes.size();
			//servercomment System.out.println("inNum in loop: " + inNum);
			inPorts = new AdapterIOPort[inNum];
			for (Element childnode : childNodes) {
				getInfoFromNode(childnode, isInput);
			}

		} else if (text.compareToIgnoreCase("OUTPUT") == 0) {
			childNodes = elementNode.getChildren();
			outNum = childNodes.size();
			outPorts = new AdapterIOPort[outNum];
			for (Element childnode : childNodes) {
				getInfoFromNode(childnode, isInput);
			}
		} else if (text.compareToIgnoreCase("PARAMETER") == 0) {
			if (isInput) {
				childNodes = elementNode.getChildren();
				inPorts[inPointer] = new AdapterIOPort();
				for (Element childnode : childNodes) {
					getInfoFromNode(childnode, isInput);
				}
				inPointer++;
			}
		} else if (text.compareToIgnoreCase("RETURN") == 0) {
			childNodes = elementNode.getChildren();
			if (childNodes.size() != 0) {
				outPorts[outPointer] = new AdapterIOPort();
				for (Element childnode : childNodes) {
					getInfoFromNode(childnode, isInput);
				}
				outPointer++;
			}
		} else if (text.compareToIgnoreCase("NAME") == 0) {
			if (isInput) {
				inPorts[inPointer].setName(elementNode.getValue());
			} else {
				outPorts[outPointer].setName(elementNode.getValue());
			}
		} else if (text.compareToIgnoreCase("TYPE") == 0) {
			if (isInput) {
				inPorts[inPointer].setType(elementNode.getValue());
			} else {
				outPorts[outPointer].setType(elementNode.getValue());
			}
		} else if (text.compareToIgnoreCase("VALUE") == 0) {
			String strTemp = elementNode.getValue();

			if (strTemp.equals("")) {
				if (isInput) {
					inPorts[inPointer].setObj(getObject(
							inPorts[inPointer].getType(), ""));
				} else {
					outPorts[outPointer].setObj(getObject(
							outPorts[outPointer].getType(), ""));
				}
			} else {
				if (isInput) {
					inPorts[inPointer].setObj(getObject(
							inPorts[inPointer].getType(), strTemp));
				} else {
					outPorts[outPointer].setObj(getObject(
							outPorts[outPointer].getType(), strTemp));
				}
			}
		}
	}

	// sxh modified 2008.1 end cyd modified 2011.7 
	public void invokeWebService() {
		try {
			int outputType = Constants.WS_OUTPUT_SIMPLE;//初始化认为返回简单个数据
			
			String invokeResult = "Web service OK";
			String[] outputvalue=null;//数组结果
			logger.info("<-------------------调用  Web Services  ---------------->");

			invoker = new DynamicInvoker(wsdlURL);
			// invoker = new
			// DynamicInvoker("http://58.196.146.14:8080/WebService/services/Test?wsdl");
			//servercomment System.out.println("wsURL : " + wsdlURL);

			Vector parameterValues = new Vector();

			for (int i = 0; i < inPorts.length; i++) {
				logger.info("输入参数" + i + ":" + inPorts[i].getObj().toString());
				String value = inPorts[i].getObj().toString();
				parameterValues.addElement(value);
			}

			Map result = null;
			long begin = Calendar.getInstance().getTime().getTime();

			try {
				//servercomment System.out.println("serviceName: " + serviceName+ " portName: " + portName + 
				//servercomment " methodName: "+ methodName + " prameterValues: "+ parameterValues.toString());
				result = invoker.invoke(serviceName, portName, methodName,
						parameterValues);
			} catch (Exception ex) {
				logger.error(ex);
			}

			for (Iterator i = result.keySet().iterator(); i.hasNext();) {
				String name = (String) i.next();
				Object value = result.get(name);

				if (value != null) {
					// cyd add 处理数组
					if (result.get(name).getClass().isArray()) {
						outputType=Constants.WS_OUTPUT_ARRAY;
						outputvalue = ((String[]) value);
						StringBuffer str = new StringBuffer();
						for (int j = 0; j < outputvalue.length; j++){
							String[] _o=outputvalue[j].split(" ");
							outputvalue[j]=_o[0];
							str.append(outputvalue[j]+" ");
						}
						invokeResult = str.toString();
					} else
						invokeResult = result.get(name).toString();
				} else {
					invokeResult = "";
				}

//				logger.info(invokeResult);
			}

			long end = Calendar.getInstance().getTime().getTime();
			long span = end - begin;

			logger.info("<------------------Web Services调用成功 ----------------->");
//			logger.info("The result is : " + invokeResult);
//			logger.info("Time cost: " + span);
			
			InformationPane.writeln("<------------------Web Services调用成功 ----------------->");
			InformationPane.writeln("The result is : " + invokeResult);
			InformationPane.writeln("Time cost: " + span);
			

			

			if (outputType == Constants.WS_OUTPUT_SIMPLE) {
				Element root = new Element("OUTPUT");
				returnXMLDoc = new Document(root);

				Element returnElement = new Element("RETURN");
				root.addContent(returnElement);

				Element nameElement = new Element("NAME");
				nameElement.setText(outPorts[outNum - 1].getName());
				returnElement.addContent(nameElement);

				Element typeElement = new Element("TYPE");
				typeElement.setText(outPorts[outNum - 1].getType());
				returnElement.addContent(typeElement);

				Element valueElement = new Element("VALUE");
				
				valueElement.setText(invokeResult);
				returnElement.addContent(valueElement);

				returnXML = xmlOperation.toString(returnXMLDoc);
			} else if (outputType == Constants.WS_OUTPUT_XML) {
				returnXML = invokeResult.toString();
			} else if (outputType == Constants.WS_OUTPUT_ARRAY) {
				Element root = new Element("OUTPUT");
				returnXMLDoc = new Document(root);
				
				
				Element returnElement = new Element("RETURN");
				
				Element nameElement = new Element("NAME");
				nameElement.setText(outPorts[outNum - 1].getName());
				returnElement.addContent(nameElement);

				Element typeElement = new Element("TYPE");
				typeElement.setText("array");
				returnElement.addContent(typeElement);

				Element valueElement = new Element("VALUE");
				
				
				for (int j = 0; j < outputvalue.length; j++){
					Element tempElement = new Element("STRING");
					tempElement.setText(outputvalue[j]);
					valueElement.addContent(tempElement);
				}
				
				returnElement.addContent(valueElement);
				root.addContent(returnElement);

				
				
				returnXML = xmlOperation.toString(returnXMLDoc);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} // end-catch
	}

	@Override
	protected void configureAdapter() {
		// TODO Auto-generated method stub

	}

	public String getServiceName() {
		return (serviceName);
	}

	public String getMethodName() {
		return (methodName);
	}

	public String getPortName() {
		return portName;
	}

	public String getWsdlURL() {
		return wsdlURL;
	}

	public int getApplicationId() {
		return (applicationId);
	}

} // @jve:decl-index=0:visual-constraint="10,10"

