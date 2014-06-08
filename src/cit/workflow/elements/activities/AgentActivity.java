/*
 * Created on 2008-11-28
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


import org.apache.log4j.Logger;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;

import cit.workflow.Agentsubmition;
import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.XMLOperation;
/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AgentActivity extends NodeActivity {

	private static Logger logger = Logger.getLogger(AgentActivity.class);
	public XMLOperation xmlOperation = new XMLOperation();
	
	public AgentActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	public void execute() throws Exception {
		//模拟Agent的返回XML，为Engine测试之用
		String AgentReturnXML = null;
		
		String Template = null;
		int AgentID = 0;
		
		int taskID=0;
		
		//servercomment System.out.println("ACTIVITY ACTION----AgentActivity EXECUTED!");
		
		
		
//		String ValuedInputXML = null;
		
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
		
		
		
		sql="SELECT * FROM processactivityinputmapping WHERE processid=? AND activityid=?";
		params=new Object[]{this.getProcess().getProcessID(),new Integer(this.activityID)};
		types=new int[]{Types.VARCHAR,Types.INTEGER};
		List paimList=(List)executeQuery(new MapListHandler());
		Iterator paimIterator=paimList.iterator();
		
		
		if(paimIterator.hasNext()){
			while(paimIterator.hasNext()){

				Map paimMap=(Map)paimIterator.next();
				
			    //在ProcessActivityInputMapping中取出inputXML和xslt
				String InputXML = (String) paimMap.get("inputXML");
				//servercomment System.out.println(" InputXML: "+ InputXML);
				
				String XSLTstring = (String)paimMap.get("xslt");
				//servercomment System.out.println(" XSLT: "+XSLTstring);
				 
				String valuedInputXML = addValueToXML(InputXML);
				//servercomment System.out.println(" valuedInputXML: "+valuedInputXML);
				
			   //outputXML为经过XSLT解析过的XML字符串,将被写入
				String AgentInputXML=getXMLbyXSLT(valuedInputXML,XSLTstring);	
				Document agentInputDoc = xmlOperation.toDocument(AgentInputXML);
				AgentInputXML = xmlOperation.toString(agentInputDoc);
				//servercomment System.out.println(" AgentInputXML : " + AgentInputXML);
				
				Template = getTemplate((String)this.getProcess().getProcessID(), this.activityID);
				AgentID = getAgentID((String)this.getProcess().getProcessID(), this.activityID);
				
			  //*********************************************************
			  //由于暂时在数据库中得不到AGENT的type，即同步或异步，在次此默认为同步*
			  //*********************************************************
					
				UpdateAgentTaskList((String)this.getProcess().getProcessID(),this.activityID,AgentID,Template,AgentInputXML);

				
			
				//servercomment System.out.println("*****************  WorkflowEngine pause ;  Agent running  ******************");
				
				//*********************** Agent 模拟 start **************************
		
			    AgentSimulator();
				
				//***********************Agent 模拟 end **************************
		
			}
			
		}
		else{               //考虑Agent没有输入的情况
		
			
		UpdateAgentTaskList((String)this.getProcess().getProcessID(),this.activityID,
				getAgentID((String)this.getProcess().getProcessID(), this.activityID),
				getTemplate((String)this.getProcess().getProcessID(), this.activityID),"");
		
		//servercomment System.out.println("*****************  WorkflowEngine pause ;  Agent running  ******************");
		
		//*********************** Agent 模拟 start **************************
		
	    AgentSimulator();
		
		//***********************Agent 模拟 end **************************
		
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
						//servercomment System.out.println("tht root name is : "+ xmlObjectRoot.getName());
						

						List xmlObjectList = xmlObjectRoot.getChildren();
						objectEt.addContent((Element) xmlObjectRoot.clone());

					} else if (objectType == Constants.OBJECT_TYPE_DOC) {

					} else if (objectType == Constants.OBJECT_TYPE_REF) {

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
	
	
	private int getAgentID(String ProcessID, int ActivityId) throws SQLException{
		
		sql="SELECT AgentID,CapabilityID FROM processactivityagent WHERE ProcessID=? AND ActivityId=?";

		params=new Object[]{this.getProcess().getProcessID(),new Integer(this.activityID)};
		
		types=new int[]{Types.VARCHAR,Types.INTEGER};
		
		List AgentList=(List)executeQuery(new MapListHandler());
		
		Iterator agentIterator=AgentList.iterator();
		
		Map agent=(Map)agentIterator.next();
		
		int AgentID = (Integer)agent.get("AgentID");
		
		return AgentID;
		
	}
	
	private String getTemplate(String ProcessID, int ActivityId) throws SQLException{		
		
		sql="SELECT AgentID,CapabilityID FROM processactivityagent WHERE ProcessID=? AND ActivityId=?";

		params=new Object[]{this.getProcess().getProcessID(),new Integer(this.activityID)};
		
		types=new int[]{Types.VARCHAR,Types.INTEGER};
		
		List AgentList=(List)executeQuery(new MapListHandler());
		
		Iterator agentIterator=AgentList.iterator();
		
		if(!agentIterator.hasNext())
			System.out.println("ERROR: There is no AgentID,CapabilityID meeting in the ProcessactivityAgent!");
		
		String Template = null;
		
		while(agentIterator.hasNext()){
			
			Map agent=(Map)agentIterator.next();
			
			sql="SELECT * FROM agentcapability WHERE AgentID=? AND CapabilityID=?";
			
			params=new Object[]{agent.get("AgentID"),agent.get("CapabilityID")};
			
			types=new int[]{Types.INTEGER,Types.INTEGER};
			
			List TemplateList=(List)executeQuery(new MapListHandler());
			
			Map TemplateMap=(Map)TemplateList.iterator().next();
			
			Template = (String)TemplateMap.get("InputTemplate");

			//servercomment System.out.println("*************** Info of Agent ****************");
			//servercomment System.out.println(" AgentID : " + TemplateMap.get("AgentID"));
			//servercomment System.out.println(" Name : " + (String)TemplateMap.get("Name"));
		    //servercomment System.out.println(" Description : " + (String)TemplateMap.get("Description"));
			//servercomment System.out.println(" InputTemplate : " + Template);
			//servercomment System.out.println("*************** End of Agent Info **************");
		}
			
			return Template;
		
	}
	
	
	
	
	//由于Portal中负责为输入XML填写数据，所以需要更具ProcessID,ObjectID到processXMLdocument中提取XML数据
	private String getAgentTaskXML(String processID)throws Exception {
		sql="SELECT ObjectSet FROM processactivityinputmapping WHERE ProcessID=? AND ActivityID=?";
		params = new Object[]{processID,this.activityID};
		types = new int[]{Types.VARCHAR, Types.INTEGER};
		List ObjectList = (List)executeQuery(new MapListHandler());
		Map ObjectMap = (Map)ObjectList.iterator().next();
		String ObjectSet = (String)ObjectMap.get("ObjectSet");
		//servercomment System.out.println("ObjectSte : "+ ObjectSet);
		
		int Setlong = 0;
		Scanner scanner1 = new Scanner(ObjectSet);
		scanner1.useDelimiter("[,()]");
		while(scanner1.hasNextInt()){
			int data = scanner1.nextInt();
			Setlong++;
		}
		int[] Objectset = new int[Setlong];
		Scanner scanner2 = new Scanner(ObjectSet);
		scanner2.useDelimiter("[,()]");
		for(int i=0; i<Setlong&&scanner2.hasNextInt();i++){
			Objectset[i] = scanner2.nextInt();
			//servercomment System.out.println("ObjectID for Agent:"+Objectset[i]);
		}
		scanner1.close();
		scanner2.close();
		
		
		int XMLnum = ChooseXMLObjects(Objectset);   //XMLObject的真实个数
		int[] XMLObject = new int[XMLnum];
		
		for(int i=0;i<XMLnum;i++){
			for(int j=i;j<Setlong;j++){
				if(Objectset[j] != -1){
					XMLObject[i] = Objectset[j];
					break;
				}
			}
		}
		
		
		if(XMLnum==1){
			sql="SELECT XML FROM processxmldocument WHERE ProcessID=? AND ObjectID=?";
			
			params=new Object[]{processID,XMLObject[0]};
			
			types=new int[]{Types.VARCHAR, Types.INTEGER};
			
			List xmlList=(List)executeQuery(new MapListHandler());
			
			Map xmlMap=(Map)xmlList.iterator().next();
			
			String inputXML = (String)xmlMap.get("XML");
			
			//servercomment System.out.println("ProcessID: "+ processID);
			//servercomment System.out.println("ObejectID: "+ XMLObject[0]);
			//servercomment System.out.println("AgentTaskXML: "+inputXML);
			
			return inputXML;
		}
		else if(XMLnum>1){
			//servercomment System.out.println("Mor than one XMLinput, to be added!");
			
		}
		else if(XMLnum<=1){
			//servercomment System.out.println("ERROR: Not XML Object aviable for Agent!");
			
		}
		return null;
	}
	
	//在输入Object中筛选出XMLObject的类型，以供Agent处理
	private int ChooseXMLObjects(int[] ObjectSet) throws SQLException{
		int ObjectID = 0;
		int XMLnum = 0;
		for(int i=0; i<ObjectSet.length; i++){
			
			ObjectID = ObjectSet[i];
		
			ElementFactory elementFacorty=new ElementFactory();	
			
			ProcessObject processObject = elementFacorty.createProcessObject(conn, this.getProcess().getProcessID(), ObjectID);
			
			if ( processObject.getObjectType() == Constants.OBJECT_TYPE_XML){
				XMLnum++;
			}
			else
				ObjectSet[i] = -1;   //非XML类型的Object数值改为-1
		}
		
		
		return XMLnum;
	}
	
	
	
	private String getXMLbyXSLT(String xmlStr, String xsltStr) {
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
	
	private void UpdateAgentTaskList(String processID,int ActivityID,int agentID,String template,String content) throws SQLException{
		sql = "insert into agenttasklist values(?,?,?,?,?)";
		params = new Object[] { processID, ActivityID, agentID,template,content};
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR,Types.VARCHAR };
		this.executeUpdate();
	}
	

	
	private String AddValue2XML(String ValuedXML ) throws SQLException{
		sql = "SELECT InputXML FROM processactivityinputmapping WHERE ProcessID=? AND ActivityID=?";
		params=new Object[]{this.getProcess().getProcessID(),new Integer(this.activityID)};
		types=new int[]{Types.VARCHAR,Types.INTEGER};
		
		Document SourceDocument = null;
		Document FinalDocument = null;
		List AgentList=(List)executeQuery( new MapListHandler());
		Iterator AgentIterator=AgentList.iterator();
		if(AgentIterator.hasNext()){
			Map agent=(Map)AgentIterator.next();
			String InputSourceString = (String)agent.get("InputXML"); 
			
            SourceDocument = xmlOperation.toDocument(InputSourceString);
            
            //servercomment System.out.println("Before add value to InputXML, it is: \n"+ xmlOperation.toString(SourceDocument));
     
            
		}
		
		
//		Element NewMessageContent = xmlOperation.toDocument(ValuedXML).getRootElement();
		
	
		
		SourceDocument.getRootElement().getChild("DebitInfo").removeChildren("DebitInfoQueryMessageContent");
		
		SourceDocument.getRootElement().getChild("DebitInfo").addContent((Element)xmlOperation.toDocument(ValuedXML).getRootElement().clone());
		
		//给SYSTEM_INFO添加信息
		SourceDocument.getRootElement().getChild("SYSTEM_INFORMATION").getChild("ProcessID").setText(this.getProcess().getProcessID());
		SourceDocument.getRootElement().getChild("SYSTEM_INFORMATION").getChild("ActivityID").setText(((Integer)this.activityID).toString());
	
		//servercomment System.out.println("After add value to InputXML, it is: \n"+ xmlOperation.toString(SourceDocument));
		
		return xmlOperation.toString(SourceDocument);
	}
	
	
	private String GetAgentOutputXML(Map agent) throws SQLException{
		
		sql="SELECT OutputXML FROM agentcapability WHERE AgentID=? AND CapabilityID=?";
		
		params=new Object[]{agent.get("AgentID"),agent.get("CapabilityID")};
		
		types=new int[]{Types.INTEGER,Types.INTEGER};
		
		List OutputXMLList=(List)executeQuery(new MapListHandler());
		
		Map OutputXMLMap=(Map)OutputXMLList.iterator().next();
		
		return (String)OutputXMLMap.get("OutputXML");
		
	}
	
	//模拟Agent活动，仅供Engine测试之用
	private void AgentSimulator() throws RemoteException, SQLException{
		
		String AgentOutputXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DebitInfoQueryResultMessageContent>"+
		"<NAME>DebitInfoQueryResultMessageContent</NAME>"+"<TYPE>String</TYPE><VALUE>"
		+"<DebitInfoQueryReport>"+
			"<Suggestion>Approved</Suggestion>"+
			"<CanBeApproved>True</CanBeApproved>"+
		"</DebitInfoQueryReport></VALUE>"+
	 "</DebitInfoQueryResultMessageContent>";
		
		Agentsubmition agentsubmit = new Agentsubmition(this.getProcess().getProcessID(),this.activityID);
	
		
	    //servercomment System.out.println("Sitimulated UPdateObject completed!");
		agentsubmit.SubmitAgentTask(AgentOutputXML);
	}

}

