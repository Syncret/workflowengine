/*
 * Created on 2008-12-3 by cxz
 */
package cit.workflow;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;

import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;
import cit.workflow.exception.WorkflowTransactionException;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;





public class Agentsubmition extends DBUtility{
	
	protected String ProcessID ;
	protected int ActivityID ;
	private static Logger logger = Logger.getLogger(Agentsubmition.class);


		
	
	public Agentsubmition(String ProcessID,int ActivityID){
		super(WorkflowConnectionPool.getInstance().getConnection());
		this.ProcessID=ProcessID;
		this.ActivityID=ActivityID;
	}
	
	/*  提供给Agent的接口
	 *  根据OutputXML更新数据库
	 *  提交Agent任务，并且将任务状态从 Running 改为 Completed
	 */
	public boolean SubmitAgentTask(String OutputXML) throws SQLException, RemoteException{
		
		//更新数据库
		//servercomment System.out.println("************");
		updateOutputObject(OutputXML);
		
		//提交Task
		//submitAutoActivity的人员分配未实现，暂时以1，1作为输入
		int ActorID=1;
		int ActorType=1;
		boolean result = WorkflowServer.Server.submitAutoActivity(ProcessID, ActivityID, ActorType, ActorID);
		
		
		return result;
	    
	}
	
	//更新TaskList表
	private void UpdateAgentTaskList(String processID,int taskID,int agentID,String template,String content) throws SQLException{
		sql = "insert into agenttasklist values(?,?,?,?,?)";
		params = new Object[] { processID, taskID, agentID,template,content};
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,Types.VARCHAR,Types.VARCHAR };
		this.executeUpdate();
	}
	
	//从AgentTaskList表中取出MessageContent
	public String GetXMLfromAgentTaskList(int agentID) throws SQLException{
		
		sql = "select MessageContent from AgentTaskList where processid = ? and TaskID = ? and AgentID=?";
		params = new Object[] { this.ProcessID, this.ActivityID, agentID};
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		
		List paimList=(List)executeQuery(new MapListHandler());
		Iterator paimIterator = paimList.iterator();
		
		if(paimIterator.hasNext()){
			Map ContentMap = (Map)paimIterator.next();
			String returnContent = (String)ContentMap.get("MessageContent");
			return returnContent;
		}else{
			logger.info("ERROR -- No Content meets !");
			return null;
		}
		
	}
	
//	public String SetResultContent(String content){
		
//	}
	
	
	
	// 根据Agent的返回结果对数据库进行更新映射
	public void updateOutputObject(String xmlStr) {
		try {
			sql = "select * from processactivityoutputmapping where ProcessID = ? and ActivityID = ?";
			params = new Object[] { ProcessID,ActivityID};
			types = new int[] { Types.VARCHAR,Types.INTEGER };
			List paomList = (List) executeQuery(new MapListHandler());
			Iterator iterator = paomList.iterator();
			
			int objectID;
			int objectType;
			while (iterator.hasNext()) {
				//servercomment System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@has entered while loop!");
				Map paomMap = (Map)iterator.next();
				objectID = (Integer)paomMap.get("ObjectID");
				
				//servercomment System.out.println("ProcessID:"+ProcessID);
				//servercomment System.out.println("ObjectID:"+objectID);
				
				
				ElementFactory elementFacorty=new ElementFactory();	
	
				ProcessObject processObject = elementFacorty.createProcessObject(conn, ProcessID, objectID);
				
				//servercomment System.out.println("cxz");
				objectType = processObject.getObjectType();
				
				if (objectType == Constants.OBJECT_TYPE_INHERENT) {
					String xslt = (String)paomMap.get("XSLT");
					String outputXML = getXMLbyXSLT(xmlStr, xslt);
					
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder.build(new StringReader(outputXML));
					
					//servercomment System.out.println("************Isvalid XML: "+xmlStr);
					//servercomment System.out.println("************Isvalid xslt: "+xslt);
					//servercomment System.out.println("************Isvalid Type: "+ objectType);

					//servercomment System.out.println("************Isvalid: "+ outputXML);
					
				  //String value = xmlDoc.getRootElement().getChild("PARAMETER").getChild("VALUE").getValue();
					String value = xmlDoc.getRootElement().getChild("VALUE").getValue();
					
					//servercomment System.out.println(value);
					
					sql = "update processInherentVariable set value = ? where processid = ? and objectid = ?";
					params = new Object[] { value, ProcessID, new Integer(objectID)};
					types = new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER };
					this.executeUpdate();
				} 
				else if (objectType == Constants.OBJECT_TYPE_OBJECT) {
					SAXBuilder xmlBuilder = new SAXBuilder();
					Document xmlDoc = xmlBuilder.build(new StringReader(xmlStr));
					String value = xmlDoc.getRootElement().getChild("PARAMETER").getChild("VALUE").getValue();
					//servercomment System.out.println(value);
					
					sql = "update processObjectVariable set value = ? where processid = ?";
					params = new Object[] { value, ProcessID};
					types = new int[] { Types.VARCHAR, Types.VARCHAR };
					this.executeUpdate();
				} 
				else if (objectType == Constants.OBJECT_TYPE_XML) {
					String xslt = (String)paomMap.get("XSLT");
					String outputXML = getXMLbyXSLT(xmlStr, xslt);
					
					sql = "select * from processXMLDocument where processid = ? and objectID = ?";
					params = new Object[] {ProcessID, new Integer(objectID)};
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
					
					
					sql = "update processXMLDocument set XML = ? where processid = ? and objectID = ?";
					params = new Object[] { sourceXML, ProcessID, new Integer(objectID)};
					types = new int[] { Types.VARCHAR, Types.VARCHAR ,Types.INTEGER};
					this.executeUpdate();
				} 
				else if (objectType == Constants.OBJECT_TYPE_DOC) {

				} 
				else if (objectType == Constants.OBJECT_TYPE_REF) {

				} 
				else {
					throw new WorkflowTransactionException(
							"Object type is out of range!");
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
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
	
	//暂时性的替代接口，一弥补引擎暂时无法载入XML的BUG
	public  void SetAgentInputXML(String processID)throws SQLException{
			
			sql="UPDATE processactivityoutputmapping SET InputXML=? WHERE processID=?";
			params=new Object[]{ processID};
			types=new int[]{Types.VARCHAR,Types.INTEGER};

		
	}


	private Map getActorMap(String ProcessID) throws SQLException{
		sql="SELECT ActorType,ActorID FORM processparticipant WHERE processid=?";
		params=new Object[]{ProcessID};
		types=new int[]{Types.VARCHAR};
		List paimList=(List)executeQuery(new MapListHandler());
		Iterator paimIterator=paimList.iterator();
		if(paimIterator.hasNext()){
			Map ActorMap=(Map)paimIterator.next();
			return ActorMap;
		}else{
			//servercomment System.out.println("No Actor exist in this Process!");
			return null;
		}
	}
	
	//just for text
	private void Updateprocessactivityoutputmapping(String processID,int ActivityID,int ObjectID,String XSLT) throws SQLException{
		sql = "insert into processactivityoutputmapping values(?,?,?,?)";
		params = new Object[] { processID, ActivityID, ObjectID,XSLT};
		types = new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER,Types.VARCHAR};
		this.executeUpdate();
	}

	
	public static void main(String[] args) throws SQLException {
		
		Agentsubmition test=new Agentsubmition("eb3f9d52-9630-413a-8f70-e3a7f223b749",1);
		/*
		try {
			text.Updateprocessactivityoutputmapping("cxz",4, 2, "cxz");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		*/
		String cxz = test.GetXMLfromAgentTaskList(1);
		logger.info("MessageContent ------- "+cxz);
	}

}
