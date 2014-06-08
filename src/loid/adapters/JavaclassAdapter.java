/*
 * Created on 2004-12-27
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import cit.workflow.utils.XMLOperation;

/*
 * import org.w3c.dom.NamedNodeMap; import org.w3c.dom.Node; import
 * org.w3c.dom.NodeList;
 */

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JavaclassAdapter extends Adapter implements IAdapter {
	
	//Java Class 的位置和名称
	private String classUrl;
	private String myclassName;
	
	// The parameter for the constructor name.
	private int constructorNum = 0;
	private int constructorNo = 0;
	
	//Java Class 构造函数的输入端口
	private AdapterIOPort[] inCPorts;
	
	//Java Class 构造函数的输入端口数目
	private int inCNum = 0;
	
	//Java Class 构造函数的输入端口指针
	private int inCPointer = 0;
	
	//Java Class 构造函数选择标志
	private boolean constructorFlag = false;
	
//	在原有的Url上添加类名ClassName           cxz add on 08.11.13
	private String ClassName = ".";
	
	
	private String processID;
//	private boolean isSystem;
	private XMLOperation xmlOperation = null;

	public JavaclassAdapter(String processID, int applicationId) {
		this.processID = processID;
		this.applicationId = applicationId;
		xmlOperation = new XMLOperation();
	}

	/**
	 * 
	 */
	public String getOutputXML(String inputXML, String outputXML) {
		logger.info("Final input XML is:\n " + xmlOperation.formatString(inputXML));
		
		if (inputXML.equals("") || outputXML.equals("")) {
			return (null);
		} else {
			returnXMLDoc = getOutputXML(xmlOperation.toDocument(inputXML), xmlOperation.toDocument(outputXML));
			return xmlOperation.toString(returnXMLDoc);
		}
	}
	
	public Document getOutputXML(Document inputXMLDoc, Document outputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
		this.outputXMLDoc = outputXMLDoc;
		
		if (inputXMLDoc == null || outputXMLDoc == null) {
			return null;
		}
		
		
		initAdapterInput(inputXMLDoc);
		
		initAdapterOutput(outputXMLDoc);	
		
		invokeJavaClass();
		
		return (returnXMLDoc);
	}

	/*
	 * 
	 */

	protected void initAdapterInput(Document inputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
		Element root = inputXMLDoc.getRootElement();
		getInfoFromNode(root, true);
	}

	@Override
	protected void initAdapterOutput(Document outputXMLDoc) {
		this.outputXMLDoc = outputXMLDoc;
		//servercomment System.out.println("++++  outpuXMLDoc:  "+xmlOperation.toString(outputXMLDoc));
		Element root = outputXMLDoc.getRootElement();
		
		getInfoFromNode(root, false);

	}
	
	/*
	 * 解析DOM文档(递归调用完所有节点)
	 * 若inFlag==true:表示是解析Input端口参数的;若inFlag==false:表示是解析Output端口参数的
	 */
	protected void getInfoFromNode(Element elementNode,	boolean isInput) {
		// cxz add 08.11.13
		List<Element> childNodes;
		String text = elementNode.getName();
		List<Attribute> attributes = null;

		if (text.compareToIgnoreCase("JAVACLASS") == 0) {
			attributes = elementNode.getAttributes();
			for (Attribute attribute : attributes) {
				if (0 == attribute.getName().compareToIgnoreCase("ClassName")) {
					ClassName += attribute.getValue();
				}
			}

			childNodes = elementNode.getChildren();
			for (Element childNode : childNodes) {
				getInfoFromNode(childNode, isInput);
			}
		} else if (text.compareToIgnoreCase("CLASSURL") == 0) {
			
			classUrl = elementNode.getValue() + ClassName;
			
		} else if (text.compareToIgnoreCase("METHODNAME") == 0) {
			
			methodName = elementNode.getValue();
			
		} else if (text.compareToIgnoreCase("USERNAME") == 0) {
			
			userName = elementNode.getValue();
			
		} else if (text.compareToIgnoreCase("PASSWORD") == 0) {
			
			password = elementNode.getValue();
			
		} else if (text.compareToIgnoreCase("RUNTIMEINPUT") == 0) {
			
			childNodes = elementNode.getChildren();
			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);
			
		} else if (text.compareToIgnoreCase("CONSTRUCTOR") == 0) {
			
			constructorFlag = true;
			inCPointer = 0;
			childNodes = elementNode.getChildren();
			inCNum = childNodes.size();
			inCPorts = new AdapterIOPort[inCNum];
			
			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);
		} else if (text.compareToIgnoreCase("ParameterSchema") == 0) {
			childNodes = elementNode.getChildren();
			getInfoFromNode(childNodes.get(0), isInput);
		} else if (text.compareToIgnoreCase("METHOD") == 0) {
			constructorFlag = false;
			inPointer = 0;
			childNodes = elementNode.getChildren();
			inNum = childNodes.size();
			inPorts = new AdapterIOPort[inNum];

			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);
		} else if (text.compareToIgnoreCase("INPUT") == 0) {
			constructorFlag = false;
			childNodes = elementNode.getChildren();
			for (Element childnode : childNodes) {
				getInfoFromNode(childnode, isInput);
			}
		} else if (text.compareToIgnoreCase("OUTPUT") == 0) {
			constructorFlag = false;
			outPointer = 0;
			childNodes = elementNode.getChildren();
			outNum = childNodes.size();
			outPorts = new AdapterIOPort[outNum];

			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);
		} else if (text.compareToIgnoreCase("RETURN") == 0) {
			logger.info("Got return !");
			childNodes = elementNode.getChildren();
			if (childNodes.size() != 0) {
				outPorts[outPointer] = new AdapterIOPort();
				for (Element childnode : childNodes) {
					getInfoFromNode(childnode, isInput);
				}
				outPointer++;
			}
		} else if (text.compareToIgnoreCase("PARAMETER") == 0) {
			childNodes = elementNode.getChildren();
			if (isInput) {
				if (constructorFlag) {
					inCPorts[inCPointer] = new AdapterIOPort();
					for (Element childnode : childNodes)
						getInfoFromNode(childnode, isInput);
					inCPointer++;
				} else {
					inPorts[inPointer] = new AdapterIOPort();
					for (Element childnode : childNodes)
						getInfoFromNode(childnode, isInput);
					inPointer++;
				}
			} else {
				outPorts[outPointer] = new AdapterIOPort();
				for (Element childnode : childNodes)
					getInfoFromNode(childnode, isInput);
				outPointer++;
			}
		} else if (text.compareToIgnoreCase("NAME") == 0) {
			if (isInput) {
				if (constructorFlag)
					inCPorts[inCPointer].setName(elementNode.getValue());
				else
					inPorts[inPointer].setName(elementNode.getValue());
			} else
				outPorts[outPointer].setName(elementNode.getValue());
		} else if (text.compareToIgnoreCase("TYPE") == 0) {
			if (isInput) {
				if (constructorFlag)
					inCPorts[inCPointer].setType(elementNode.getValue());
				else
					inPorts[inPointer].setType(elementNode.getValue());
			} else
				outPorts[outPointer].setType(elementNode.getValue());
		} else if (text.compareToIgnoreCase("VALUE") == 0) {
			if (isInput) {
				if (constructorFlag) {
					inCPorts[inCPointer].setObj(getObject(inCPorts[inCPointer]
							.getType(), elementNode.getValue()));
				} else {
					// cxz modifyed on 2008.12.6
					// if Type is null,then set it to "String"
					if (inPorts[inPointer].getType() == null)
						inPorts[inPointer].setType("String");
					inPorts[inPointer].setObj(getObject(inPorts[inPointer].getType(), elementNode.getValue()));
				}
			} else{
				//servercomment System.out.println("outPointer : "+outPointer);
				//servercomment System.out.println("outPorts[0] Type: "+ outPorts[outPointer].getType());
				//servercomment System.out.println("elementNode value: "+elementNode.getValue());
				
				outPorts[outPointer].setObj(getObject(outPorts[outPointer].getType(), elementNode.getValue()));
	
			}
		}
		
		if (text.length() > 0) {
			// //servercomment System.out.println(indent + getNodeTypeName(node) + ": " + text);
		}
		
		if (text.compareToIgnoreCase("#document") == 0) {
			childNodes = elementNode.getChildren();
			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);
		}
	}

	/*
	 * 
	 */
	@Override
	protected void configureAdapter() {
		try {
			String name = "";
			String type = "";
			Class className = null;
			Method[] classMethods = null;
			className = Class.forName(classUrl);
			Class[] argsType = null;
			Class[] argsTypeC = null;
			//
			Constructor[] constructors = null;
			try {
				className = Class.forName(classUrl);
				constructors = className.getConstructors();
			} catch (Exception x) {
				x.printStackTrace();
			}
			if (constructorNo >= 0) {
				argsTypeC = constructors[constructorNo].getParameterTypes();
				inCNum = argsTypeC.length;
				inCPorts = new AdapterIOPort[inCNum];
				inputSchema = "<RUNTIMEINPUT><CONSTRUCTOR>";// dingo updated
				for (int i = 0; i < inCNum; i++) {
					AdapterIOPort pCin = new AdapterIOPort();
					name = "argC" + i;
					pCin.setName(name);
					type = argsTypeC[i].getName();
					pCin.setType(type);
					inCPorts[i] = pCin;
					inputSchema += "<PARAMETER><NAME>" + name + "</NAME>"
							+ "<TYPE>" + type + "</TYPE>"
							+ "<VALUE></VALUE></PARAMETER>";
				}
				inputSchema += "</CONSTRUCTOR><INPUT>";
			} else {
				inputSchema = "<RUNTIMEINPUT><CONSTRUCTOR></CONSTRUCTOR><INPUT>";// dingo
				inCNum = 0;
			}
			//
			classMethods = className.getMethods();
			inNum = 0;
			outNum = 0;
			if (classMethods[methodNo].getName().compareTo(methodName) != 0)
				return;
			argsType = classMethods[methodNo].getParameterTypes();
			inNum = argsType.length;
			inPorts = new AdapterIOPort[inNum];
			for (int i = 0; i < inNum; i++) {
				AdapterIOPort pin = new AdapterIOPort();
				name = "arg" + i;
				pin.setName(name);
				type = argsType[i].getName();
				pin.setType(type);
				inPorts[i] = pin;
				inputSchema += "<PARAMETER><NAME>" + name + "</NAME>"
						+ "<TYPE>" + type + "</TYPE>"
						+ "<VALUE></VALUE></PARAMETER>";
			}
			inputSchema += "</INPUT></RUNTIMEINPUT>";// dingo
			type = classMethods[methodNo].getReturnType().getName();
			if (type.compareToIgnoreCase("void") == 0)
				return;
			outNum = 1;
			outPorts = new AdapterIOPort[outNum];
			name = "arg" + inNum;
			outPorts[0] = new AdapterIOPort();
			outPorts[0].setName(name);
			outPorts[0].setType(type);
			outputSchema = "<OUTPUT>";
			outputSchema += "<RETURN><NAME>" + name + "</NAME>" + "<TYPE>"
					+ type + "</TYPE>" + "<VALUE></VALUE></RETURN></OUTPUT>";
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	/*
	 * 
	 */
	private void invokeJavaClass() {
		logger.info("<------------------- 调用Java Class ---------------->");
		
		try {
			String type = "";
			Object obj = null;
			Object x = null;
			if (inCNum > 0) {
				Object[] _objArrC = new Object[inCNum];
				Class[] _classArrC = new Class[inCNum];
				for (int i = 0; i < inCNum; i++) {
					_objArrC[i] = inCPorts[i].getObj();
					type = inCPorts[i].getType();
					if (type.compareTo("boolean") == 0)
						_classArrC[i] = boolean.class;
					else if (type.compareTo("int") == 0)
						_classArrC[i] = int.class;
					else if (type.compareTo("long") == 0)
						_classArrC[i] = long.class;
					else if (type.compareTo("double") == 0)
						_classArrC[i] = double.class;
					else if (type.compareTo("float") == 0)
						_classArrC[i] = float.class;
					else
						_classArrC[i] = _objArrC[i].getClass();
				}
				obj = loadClass(classUrl, _classArrC, _objArrC);
			} else {
				obj = loadClass(classUrl);
			}
			
			logger.info("inCNum: " + inCNum);
			
			if (inNum > 0) {
				Object[] _objArr = new Object[inNum];
				Class[] _classArr = new Class[inNum];
				
				for (int j = 0; j < inNum; j++) {
					_objArr[j] = inPorts[j].getObj();
					type = inPorts[j].getType();
					
					if (type.compareTo("boolean") == 0)
						_classArr[j] = boolean.class;
					else if (type.compareTo("int") == 0)
						_classArr[j] = int.class;
					else if (type.compareTo("long") == 0)
						_classArr[j] = long.class;
					else if (type.compareTo("double") == 0)
						_classArr[j] = double.class;
					else if (type.compareTo("float") == 0)
						_classArr[j] = float.class;
					else if (type.compareTo("Timestamp") == 0)
						_classArr[j] = Timestamp.class;
					else
						_classArr[j] = _objArr[j].getClass();
				}
				x = invokeMothod(obj, methodName, _classArr, _objArr);
				//servercomment System.out.println("x: "+ x.toString());
			} else {
				x = invokeMothod(obj, methodName);
			}
			logger.info("JavaclassAdapter - Input Parameter Number: " + outNum);
			logger.info("JavaclassAdapter - Output Parameter Number: " + inNum);
	
			if (outNum == 0) {
				
				logger.info(" The method is type of void, there's no return.");
				
			} else {		

				Element root = outputXMLDoc.getRootElement();
				if ( x == null) {
					root.getChild("RETURN").setText("");
					logger.info("JavaclassAdapter - return is null!");
				} else {
					root.getChild("RETURN").getChild("VALUE").setText(x.toString());
					logger.info("JavaclassAdapter - return is :" + x.toString());
				}
				returnXMLDoc = outputXMLDoc;
				logger.info("JavaclassAdapter - returnXMLDoc: " + xmlOperation.toString(returnXMLDoc));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		logger.info("<---------------------- Java Class 调用成功 --------------------->");			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see loid.adapters.IAdapter#getApplicationId()
	 */
	public int getApplicationId() {
		// TODO Auto-generated method stub
		return (applicationId);
	}

	/**
	 * @param strClassName
	 * @param argsType
	 * @param args
	 * @return Object
	 * @throws java.lang.NoSuchMethodException
	 * @throws java.lang.SecurityException
	 * @throws java.lang.ClassNotFoundException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.IllegalArgumentException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private Object loadClass(String strClassName, Class[] argsType,
			Object[] args) throws NoSuchMethodException, SecurityException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Object returnObj = null;
		Class className = null;
		Constructor constructor = null;

		className = Class.forName(strClassName);
		constructor = className.getConstructor(argsType);
		// constructors = className.getConstructors();
		returnObj = constructor.newInstance(args);
		return returnObj;
	}

	/**
	 * @param strClassName
	 * @return Object
	 * @throws java.lang.NoSuchMethodException
	 * @throws java.lang.SecurityException
	 * @throws java.lang.ClassNotFoundException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.IllegalArgumentException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private Object loadClass(String strClassName) throws NoSuchMethodException,
			SecurityException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		return loadClass(strClassName, null, null);
	}

	/**
	 * @param classObject
	 * @param strMethodName
	 * @param argsType
	 * @param args
	 * @return Object
	 * @throws java.lang.NoSuchMethodException
	 * @throws java.lang.SecurityException
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.IllegalArgumentException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private Object invokeMothod(Object classObject, String strMethodName,
			Class[] argsType, Object[] args) throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		logger.info("method name:   " + strMethodName);
		Method concatMethod = classObject.getClass().getMethod(strMethodName, argsType);
		return concatMethod.invoke(classObject, args);
	}

	/**
	 * @param classObject
	 * @param strMethodName
	 * @return Object
	 * @throws java.lang.NoSuchMethodException
	 * @throws java.lang.SecurityException
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.IllegalArgumentException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private Object invokeMothod(Object classObject, String strMethodName)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		return invokeMothod(classObject, strMethodName, null, null);
	}
}
