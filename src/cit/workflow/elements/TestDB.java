package cit.workflow.elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import cit.workflow.utils.DBUtility;



public class TestDB extends DBUtility{
	private static String header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	private int workflowID;
	public TestDB(Connection conn, int workflowID){
		super(conn);
		this.workflowID = workflowID;		
	}
	
	public void doXML(){
		String stylesheeto2m,stylesheetm2i,oxml,mxml,ixml,new_mxml,new_ixml;
		//在此增加对数据流的处理！！！！dingo
	 try{
	 	sql = "select XSLTO2M,XSLTM2I,OXMLID,MXMLID,IXMLID from ProcessFlowObjects where ProcessID = ? AND EventID=? AND FromActivityID <> 0";
	 	params = new Object[]{"dingo", new Integer(21)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List objectList = (List)executeQuery(new MapListHandler());
		Iterator objectIterator = objectList.iterator();
	    //String stylesheeto2m,stylesheetm2i,oxml,mxml,ixml,new_mxml1,new_mxml2;
	    Map objectMap,xmlMap;
	   try{
	    TransformerFactory factory = TransformerFactory.newInstance();
	    List xmlList;
	    Iterator xmlIterator;
	 	while (objectIterator.hasNext()){
	 	    objectMap = (Map)objectIterator.next();	 	     
	 	    stylesheeto2m = (String) objectMap.get("XSLTO2M");
	 	    stylesheetm2i = (String) objectMap.get("XSLTM2I");
	 	    //servercomment System.out.println(stylesheeto2m);
	 		sql = "SELECT XML FROM ProcessXMLDocument WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {"dingo",(Integer) objectMap.get("OXMLID")};
			types = new int[] {Types.VARCHAR, Types.INTEGER};
			xmlList = (List) executeQuery(new MapListHandler());
			xmlIterator = xmlList.iterator();
           if(xmlIterator.hasNext())			
           { 
        	xmlMap=(Map)xmlIterator.next();
        	oxml = (String) xmlMap.get("XML");
		    oxml = header+oxml;
		    //servercomment System.out.println(oxml);//!!!!!!!!!
		    
		    //output1 to m
		    InputStream is = new ByteArrayInputStream(stylesheeto2m.getBytes());			
		    Transformer transformer1 = factory.newTransformer(new StreamSource(is));
		    //servercomment System.out.println("Transformer1 successfully!");
		    //catch(TransformerConfigurationException tce){}
            // now lets style the given document
            DocumentSource source1 = new DocumentSource(DocumentHelper.parseText(oxml));
            DocumentResult result1 = new DocumentResult();
            transformer1.transform(source1, result1);
            is.close();
            // return the transformed document
            Document transformedDoc1 = result1.getDocument();
            mxml=transformedDoc1.asXML();
            
            new_mxml = mxml.substring(header.length());//去掉xml文件头
            //servercomment System.out.println(new_mxml);//!!!!!!!!!
			sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {new_mxml, "dingo", (Integer) objectMap.get("MXMLID")};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
			
            //m to input2
			is = new ByteArrayInputStream(stylesheetm2i.getBytes()); 			
			Transformer transformer2 = factory.newTransformer(new StreamSource(is));
			//servercomment System.out.println("Transformer2 successfully!");
            // now lets style the given document
            DocumentSource source2 = new DocumentSource(DocumentHelper.parseText(mxml));
            DocumentResult result2 = new DocumentResult();
            transformer2.transform(source2, result2);
            is.close();
            // return the transformed document
            Document transformedDoc2 = result2.getDocument();
            ixml=transformedDoc2.asXML();
            new_ixml = ixml.substring(header.length());//去掉xml文件头
            //servercomment System.out.print(new_ixml);//!!!!!!!!!
			sql = "UPDATE ProcessXMLDocument SET XML=? WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {new_ixml, "dingo", (Integer) objectMap.get("IXMLID")};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
			executeUpdate();
			
	 	   }		
           /*   
		    sql = "SELECT XML FROM ProcessXMLDocument WHERE ProcessID=? AND ObjectID=?";
			params = new Object[] {"dingo",(Integer) objectMap.get("IXMLID")};
			types = new int[] {Types.VARCHAR, Types.INTEGER};
			xmlList = (List) executeQuery(new MapListHandler());
			xmlIterator = xmlList.iterator();
			if(xmlIterator.hasNext())			
	        {
			 xmlMap=(Map)xmlIterator.next();
		     ixml = (String) xmlMap.get("XML"); 
		     //servercomment System.out.print(ixml);//!!!!!!!!!
	        }  */		   
	 	}
	   }catch(TransformerFactoryConfigurationError tfce){
		   //servercomment System.out.print("Could not obtain factory!");tfce.printStackTrace();
	   }
	    catch(TransformerConfigurationException tce){
	    	//servercomment System.out.print("Could not create transformer!");tce.printStackTrace();
	    }
	    catch(TransformerException te){
	    	//servercomment System.out.print("Transformer exception!");te.printStackTrace();
	    }
	  }catch(Exception e)
	    { 
		  //servercomment System.out.print(e.toString()+"dingo!");
	    }   	
	}
		
	
	
	public void doIO(){		
//		导入ProcessFlowObjects
		sql = "SELECT * FROM WorkflowFlowObjects WHERE WorkflowID=?";
		params = new Object[] {new Integer(this.workflowID)};
		types = new int[] {Types.INTEGER};
		
	  try{
		    List wfoList = (List) executeQuery(new MapListHandler());
		
		Iterator wfoIterator = wfoList.iterator();
        //updated by dingo on 2007-01-08
		String updateSQL1 = "INSERT INTO ProcessFlowObjects(ProcessID, FlowID, EventID, FromActivityID, ToActivityID) VALUES(?,?,?,?,?)";
		String updateSQL2 = "INSERT INTO ProcessFlowObjects(ProcessID, FlowID, EventID, FromActivityID, ToActivityID, XSLTO2M, XSLTM2I, OXMLID, MXMLID, IXMLID) VALUES(?,?,?,?,?,?,?,?,?,?)";
		       int flag;//0表示活动内部流，非0表示活动间数据流
		while(wfoIterator.hasNext()) {
		 Map wfoMap = (Map) wfoIterator.next();
		 flag = ((Integer) wfoMap.get("FromActivityID")).intValue();
		 if(flag==0)
		 {
			    PreparedStatement updatePStat = conn.prepareStatement(updateSQL1);
				updatePStat.setString(1, "dingo");
				updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
				updatePStat.setInt(3, ((Integer) wfoMap.get("EventID")).intValue());	
				updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
				updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
				updatePStat.executeUpdate();
				updatePStat.close();
				
		 }
		 else{ 
			PreparedStatement updatePStat = conn.prepareStatement(updateSQL2);
			updatePStat.setString(1, "dingo");
			updatePStat.setInt(2, ((Integer) wfoMap.get("FlowID")).intValue());
			updatePStat.setInt(3, ((Integer) wfoMap.get("EventID")).intValue());	
			updatePStat.setInt(4, ((Integer) wfoMap.get("FromActivityID")).intValue());
			updatePStat.setInt(5, ((Integer) wfoMap.get("ToActivityID")).intValue());
			updatePStat.setString(6, (String)wfoMap.get("XSLTO2M"));
			updatePStat.setString(7, (String)wfoMap.get("XSLTM2I"));
			updatePStat.setInt(8, ((Integer) wfoMap.get("OXMLID")).intValue());
			updatePStat.setInt(9, ((Integer) wfoMap.get("MXMLID")).intValue());
			updatePStat.setInt(10, ((Integer) wfoMap.get("IXMLID")).intValue());
			updatePStat.executeUpdate();
			updatePStat.close();
		     }
		 		 
		 
		}
	  }catch(Exception e)
      {
		 //servercomment System.out.println(e.toString());
         //servercomment System.out.println("insert exception!");
      }
	}
	
    private static Connection conn;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Connection conn1=getConnection();
		TestDB tdb=new TestDB(conn1,35);
		//tdb.doIO();
		tdb.doXML();
//		String test = "finish";
//		OutputStream os = new ByteArrayOutputStream();
//		os.write(test.getBytes());
//		
//		//servercomment System.out.println(os.toString());
//		os.close();
	}
	private static Connection getConnection() {

	    try {
	      Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
	      //DriverManager.registerDriver(new SQLServerDriver());
	      String dbUrl =
	          "jdbc:microsoft:sqlserver://ding;DatabaseName=JWorkflow";
	      conn = DriverManager.getConnection(dbUrl, "sa", "");

	      //     "jdbc:microsoft:sqlserver://cit_60;DatabaseName=JWorkflow";
	      //conn = DriverManager.getConnection(dbUrl, "sa", "");
	      //
	      //

	      //servercomment System.out.println("connection to database:OK!");

	    }
	    catch (SQLException ex) {
	      ex.printStackTrace();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	    return conn;

	  }
}
