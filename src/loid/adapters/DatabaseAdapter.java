/*
 * Created on 2005-3-8
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;


import java.awt.GridLayout;
import java.sql.*;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import java.util.List;
import cit.workflow.utils.XMLOperation;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;




//import loid.adapters.JavaclassAdapter.JavaclassDlg;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DatabaseAdapter extends Adapter implements IAdapter {
    // Constants used for more efficient execution.
    private static final int _ORCL = 0;
    private static final int _DB2 = 1;
    private static final int _LACCS = 2;
    private static final int _RACCS = 3;
    private static final int _MSSQL = 4;
    private static final int _PGSQL = 5;
    private static final int _MYSQL = 6;
    private static final int _SYBASE = 7;
	private static final String[] DBFormats={"Oracle","DB2","Local MS Access","Remote MS Access","MS SQL Server",
								"PostgreSQL","MySQL","Sybase SQL Anywhere"};
	private static final String[] DBDrivers={"oracle.jdbc.driver.OracleDriver","com.ibm.db2.jcc.DB2Driver","sun.jdbc.odbc.JdbcOdbcDriver",
								"","com.microsoft.jdbc.sqlserver.SQLServerDriver","org.postgresql.Driver","com.mysql.jdbc.Driver",""};

	private int	   _dbFormat=0;
	private String databaseFormat=DBFormats[0];
	private String driverName=DBDrivers[0];
	private String databaseURL="";
	private String predefinedSQL="";
	private Connection dbCon;

	//	cxz add on 2009.03.05
	private String processID;
	
	private String outputXML="OUTPUT";
	
	private XMLOperation xmlOperation = null;
	//cxz add end
	
	public DatabaseAdapter(boolean flag){
		this.displayFlag=flag;
		xmlOperation = new XMLOperation();
	}
	
	public DatabaseAdapter(String processID, int applicationID){
		this.applicationId = applicationID;
		this.processID = processID;
		xmlOperation = new XMLOperation();
	}
	
	public DatabaseAdapter( String processID, int applicationID, boolean displayFlag){
		this.applicationId = applicationID;
		this.processID = processID;
		this.displayFlag = displayFlag;
		xmlOperation = new XMLOperation();
	}
	
	
	public void Init() {
		// TODO Auto-generated method stub
		//根据displayFlag确定目前的Workflow是处于建模阶段还是执行阶段.
		if(displayFlag){//建模:显示属性设置对话框
			DatabaseDlg dd=new DatabaseDlg(this);
			dd.setModal(true);
			dd.show();
			//servercomment System.out.print("Success!");			
		}
		else{//引擎执行
	
		}			
	}

	/* (non-Javadoc)
	 * @see loid.adapters.IAdapter#getOutputXML(java.lang.String, int)
	 */
/*	public String getOutputXML(String xml, int ApplicationId) {
		// TODO Auto-generated method stub
		setInputXML(xml,ApplicationId);
		String s=AppId2XML();
		if(xml.length()==0)
			return("");
		xml2IOPorts(s,false);		
//		outputXML="<?xml version='1.0'?>"+"<DATABASE><DATABASEURL>"+
//					databaseUrl+"</DATABASEURL><DRIVERNAME>"+driverName+
//					"</DRIVERNAME><PREDEFINEDSQL>"+predefinedSQL+"</PREDEFINEDSQL><OUTPUT>";
		outputXML="<OUTPUT>";
		execSql();
		outputXML+="</OUTPUT>";
//		outputXML+="</OUTPUT><USERNAME>"+userName+"</USERNAME><PASSWORD>"+
//					password+"</PASSWORD></DATABASE>";
		return(outputXML);
	}
*/
	public String getOutputXML( String inputXML, String outputXML) {
		logger.info("Final input XML is:\n" + xmlOperation.formatString(inputXML));
		
		if(inputXML.equals("") || outputXML.equals("")){
			return null;
		}else{
			returnXMLDoc = getOutputXML(xmlOperation.toDocument(inputXML), xmlOperation.toDocument(outputXML));
			return xmlOperation.toString(returnXMLDoc);
		}
	}
	
	public Document getOutputXML(Document inputXMLDoc, Document outputXMLDoc){
		this.inputXMLDoc = inputXMLDoc;
		this.outputXMLDoc = outputXMLDoc;
		
		if(inputXMLDoc == null || outputXMLDoc == null) {
			return null;
		}
		
		initAdapterInput(inputXMLDoc);
		initAdapterOutput(outputXMLDoc);
		
		
		return (returnXMLDoc);
		
	}
	
	protected void initAdapterInput(Document inputXMLDoc) {
		this.inputXMLDoc = inputXMLDoc;
		Element root = inputXMLDoc.getRootElement();
		getInfoFromNode( root, true);
	}
	
	protected void initAdaptrOutput(Document outputXMLDoc){
		this.outputXMLDoc = outputXMLDoc;
		Element root = outputXMLDoc.getRootElement();
		getInfoFromNode(root, false);
	}
	
	private void invokeDataBase(){
		
		execSql();
		outputXML+="</OUTPUT>";
		outputXMLDoc = xmlOperation.toDocument(outputXML);
	}
	
	
	
//////////////////////////////////////////////////////////////////////////////	
	
	public int getApplicationId() {
		// TODO Auto-generated method stub
		return(applicationId);
	}

	
	/*
	 * 根据该Database操作活动对应的applicationId读取数据库表,获得对应输出端口的XML格式字符串
	 */
	protected String AppId2XML(){
		String s="";
		String connstr = "";
		int outId=0;
	    PreparedStatement pstmt = null;
	    getConnection();
	    try{
		    connstr = "select OutputXMLId from DatabaseOperation where ApplicationID= " + this.applicationId;
		    pstmt = connection.prepareStatement(connstr);
		    ResultSet rs = pstmt.executeQuery();	    
		    if (rs.next()) 
		    	outId = rs.getInt(1);
		    rs.close();
		    if(outId>0){
			    connstr = "select XML from SystemXMLDocument where ObjectID= " + outId;
			    pstmt = connection.prepareStatement(connstr);
			    rs = pstmt.executeQuery();	    
			    if (rs.next()) 
			    	s = rs.getString(1);
			    rs.close();
		    }  
		    pstmt.close();
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }	
	    closeConnection();
		return(s);
	}
		/*
		 * 数据库保存函数
		 */			
		protected void save2DB(){
		    String connstr = "";
		    int appId=0;
		    PreparedStatement pstmt = null;
		    getConnection();
		    try {		      
		      connstr = "select ApplicationID from DatabaseOperation where DATABASEURL LIKE '"+ databaseURL +"' and PredefinedSQL LIKE '"+ predefinedSQL+"'";
		      pstmt = connection.prepareStatement(connstr);
		      ResultSet rs = pstmt.executeQuery();		      
		      if (rs.next()) 
		      	appId = rs.getInt(1);
		      rs.close();
		      //保证数据库中已有的Java Class相关记录将不再插入,避免重复
		      if(appId>0){		      	
		      	pstmt.close();
		      	return;
		      }		      
		      
		      connstr = "insert into SystemXMLDocument values(?,?,?,?,?)";
		      pstmt = connection.prepareStatement(connstr);
		      int id0=getID("SystemXMLDocument","ObjectID",32767);
		      pstmt.setInt(1,id0);
		      pstmt.setString(2, "InputXML"+id0);
		      pstmt.setString(3, "");
		      pstmt.setInt(4, 11);
		      pstmt.setString(5, inputSchema);
		      pstmt.execute();
		      
		      connstr = "insert into SystemXMLDocument values(?,?,?,?,?)";
		      pstmt = connection.prepareStatement(connstr);
		      int id1=id0+1;
		      pstmt.setInt(1,id1);
		      pstmt.setString(2, "OutputXML"+id1);
		      pstmt.setString(3, "");
		     pstmt.setInt(4, 12);
		      pstmt.setString(5, outputSchema);
		      pstmt.execute();
		      
		      connstr = "insert into SystemApplicationInformation values(?,?,?,?)";
		      pstmt = connection.prepareStatement(connstr);
		      int id2=getID("SystemApplicationInformation","ApplicationID",32767);
		      applicationId=id2;
		      pstmt.setInt(1,id2);
		      pstmt.setString(2, "");		      
		      String s=databaseURL;
		      pstmt.setString(3, s);
		      pstmt.setInt(4, 6);
		      pstmt.execute();
		      
		      connstr = "insert into DatabaseOperation values(?,?,?,?,?,?,?,?,?,?)";
		      pstmt = connection.prepareStatement(connstr);
		      int id3=id2;
		      pstmt.setInt(1,id3);
		      pstmt.setString(2, driverName);
		      pstmt.setString(3, databaseURL);
		      pstmt.setString(4, predefinedSQL);
		      pstmt.setString(5, userName);
		      pstmt.setString(6, password);
		      pstmt.setInt(7, 11);
		      pstmt.setInt(8, 12); 			      
		      pstmt.setInt(9, id0);
		      pstmt.setInt(10, id1);
		      pstmt.execute();		      		     		     
		      pstmt.close();
		      
		      //connection.close();
		    }
		    catch (Exception ex) {
		      ex.printStackTrace();
		    }
			closeConnection();
		}
	
	/*
	 * 	 解析DOM文档(递归调用完所有节点)
	 * inFlag=true:表示是解析Input端口参数的;inFlag=false:表示是解析Output端口参数的
	 */
	protected void getInfoFromNode(Element ElementNode, boolean isInput){
		String text = null;
		String sTemp="";
		List<Element> childNodes;;
		Element nTemp;
		
		//add by cxz on 2009.03.07
	    text = ElementNode.getName();
		
			if(text.compareToIgnoreCase("DATABASE")==0){
				childNodes = ElementNode.getChildren();
				for( Element childNode : childNodes ) 
					getInfoFromNode( childNode, isInput ); 
			}
			else if(text.compareToIgnoreCase("DATABASEURL")==0){
				
				databaseURL=ElementNode.getValue();
				_dbFormat=_getDbFormat(databaseURL);
			}
			else if(text.compareToIgnoreCase("DRIVERNAME")==0){
				
				driverName=ElementNode.getValue();
				
			}
			else if(text.compareToIgnoreCase("PREDINEDSQL")==0){
				
				predefinedSQL=ElementNode.getValue();
				
			}			
			else if(text.compareToIgnoreCase("USERNAME")==0){
				
				nTemp = ElementNode;
				if(nTemp == null)
					userName="";
				else
					userName=nTemp.getValue();
			}
			else if(text.compareToIgnoreCase("PASSWORD")==0){
				
				nTemp=ElementNode;
				if(nTemp==null)
					password="";
				else
					password=nTemp.getValue();
			}
			else if(text.compareToIgnoreCase("INPUT")==0)
			{
				inPointer=0;
				childNodes = ElementNode.getChildren();
				inNum=childNodes.size();
				inPorts=new AdapterIOPort[inNum];
				
				for( Element childnode : childNodes ) 
					getInfoFromNode(childnode, isInput);
			}
			else if(text.compareToIgnoreCase("OUTPUT")==0)
			{				
				outPointer=0;
				childNodes = ElementNode.getChildren();
				outNum=childNodes.size();
				outPorts=new AdapterIOPort[outNum];
				for( Element childnode : childNodes ) 
					getInfoFromNode(childnode, isInput);
			}
			else if(text.compareToIgnoreCase("PARAMETER")==0)
			{
				childNodes = ElementNode.getChildren();
				if(isInput){
					inPorts[inPointer]=new AdapterIOPort();
					for( Element childnode : childNodes ) 
						getInfoFromNode(childnode, isInput);
					inPointer++;
				}
				else{
					outPorts[outPointer]=new AdapterIOPort();
					for( Element childnode : childNodes ) 
						getInfoFromNode(childnode, isInput);
					outPointer++;
				}
			}	
			else if(text.compareToIgnoreCase("NAME")==0){
				
					if(isInput){
						inPorts[inPointer].setName(ElementNode.getValue());
					}					
					else
						outPorts[outPointer].setName(ElementNode.getValue());
			}
			else if(text.compareToIgnoreCase("TYPE")==0){
				if(isInput){
					inPorts[inPointer].setType(ElementNode.getValue());
				}
				else
					outPorts[outPointer].setType(ElementNode.getValue());
			}
			else if(text.compareToIgnoreCase("VALUE")==0){

					if(isInput){
						inPorts[inPointer].setObj(getObject(inPorts[inPointer].getType(),ElementNode.getValue()));
					}
					else
						outPorts[outPointer].setObj(getObject(outPorts[outPointer].getType(),ElementNode.getValue()));
				
			}
		
		
		if( text.length() > 0 ) 
	//		//servercomment System.out.println( indent + getNodeTypeName( node ) + ": " + text ); 
		if(text.compareToIgnoreCase("#document")==0){
			childNodes = ElementNode.getChildren();
			for (Element childnode : childNodes)
				getInfoFromNode(childnode, isInput);		
		}			
	}
	private int _getDbFormat(String x){
			int format=0;
        	if (databaseURL.trim().startsWith("jdbc:oracle:thin:@")) {
        		format=_ORCL;
        	}
        	else if (databaseURL.trim().startsWith("jdbc:db2:")) {
        		format=_DB2;
        	}
      	  	else if (databaseURL.trim().startsWith("jdbc:odbc:")) {
      	  		format=_LACCS;
      	  	}
        	else if (databaseURL.trim().startsWith("jdbc:microsoft:sqlserver:")) {
        		format=_MSSQL;
        	}
        	else if (databaseURL.trim().startsWith("jdbc:postgresql:")) {
        		format=_PGSQL;
        	}
        	else if (databaseURL.trim().startsWith("jdbc:mysql:")) {
        		format=_MYSQL;
        	}
        	else{
        		format=100;
        	}
		return format;
	}
	  /** Creates ports for the Database operation */
	  protected void configureAdapter() {
		inNum=1;
		outNum=2;		
		inPorts=new AdapterIOPort[inNum];
		outPorts=new AdapterIOPort[outNum];
        AdapterIOPort pin = new AdapterIOPort();
        pin.setName("InSQL");
        pin.setType("String");
        pin.setObj(new String(""));
        inPorts[0]=pin;
        inputSchema="<INPUT><PARAMETER><NAME>InSQL</NAME><TYPE>String</TYPE>" +
        			"<VALUE></VALUE></PARAMETER></INPUT>";
        AdapterIOPort pout = new AdapterIOPort();
        pout.setName("Result");
        pout.setType("String");
        outPorts[0]=pout;
        pout = new AdapterIOPort();
        pout.setName("ExitCode");
        pout.setType("String");
        outPorts[1]=pout;        
        outputSchema="<OUTPUT><PARAMETER><NAME>ResultSTR</NAME><TYPE>String</TYPE>" +
					"<VALUE></VALUE></PARAMETER><PARAMETER><NAME>ExitCode</NAME><TYPE>String</TYPE>" +
					"<VALUE></VALUE></PARAMETER></OUTPUT>";
	  }
	
	private boolean getDbConnection(){
		try {
			_setDBURL();
			Class.forName(driverName).newInstance();
			dbCon = DriverManager.getConnection(databaseURL,userName,password);
		}
		catch (Exception ex) {
			//servercomment System.out.println(ex.getMessage());
		}		
		return true;
	}
	
    private void _setDBURL(){
        switch(_dbFormat) { 
	        case _ORCL: 
	        	if (!databaseURL.trim().startsWith("jdbc:oracle:thin:@")) {
	        		if (databaseURL.trim().startsWith("jdbc:oracle:")) {// a different driver type is spcified.
	        			int ind = databaseURL.indexOf("@");
	        			if (ind > -1) {
	        				databaseURL = "jdbc:oracle:thin:@" + databaseURL.substring(ind);
	        	  	    } else {
	        	  	    	//servercomment System.out.println("Illegal database URL: "+ databaseURL);
	        	  	    }
	        		} else {
	        			databaseURL = "jdbc:oracle:thin:@" + databaseURL;
	        		}            	  	  
	        	}
	            break;
	        case _DB2: 
	        	if (!databaseURL.trim().startsWith("jdbc:db2:")) {
	        	  	  databaseURL = "jdbc:db2:" + databaseURL;
	        	}
	            break;
	        case _LACCS: 
	      	  	if (!databaseURL.trim().startsWith("jdbc:odbc:")) {
	      	  		databaseURL = "jdbc:odbc:" + databaseURL;
	      	  	}
	      	  	break;
	        case _RACCS: 
                //TODO: TAKE CARE OF HOW TO SPECIFY DRIVER FOR REMOTE ACCESS DB.
	        	break;
	        case _MSSQL: 
	        	if (!databaseURL.trim().startsWith("jdbc:microsoft:sqlserver:")) {
	        		databaseURL = "jdbc:microsoft:sqlserver:" + databaseURL;
	        	}
	            break; 
	        case _PGSQL: 
	        	if (!databaseURL.trim().startsWith("jdbc:postgresql:")) {
	        	  	  databaseURL = "jdbc:postgresql:" + databaseURL;
	        	}
	            break; 
	        case _MYSQL: 
	        	if (!databaseURL.trim().startsWith("jdbc:mysql:")) {
	        		databaseURL = "jdbc:mysql:" + databaseURL;
	        	}
	            break; 
	        case _SYBASE: 
	        	// TODO: To be added..
	            break; 
	        default: 
	            //servercomment System.out.println(databaseFormat+ " is not supported"); 
        } 
    }

    private boolean execSql(){
    	
    	
    	int exitCode=1;
    	getDbConnection();
    	if(dbCon!=null){
	    	try{
	              Statement st = dbCon.createStatement();
	              ResultSet rs=null;
	              try{
	              	if(predefinedSQL.length()>0)
	              		rs = st.executeQuery(predefinedSQL);
	              	else
	              		rs = st.executeQuery((String)inPorts[0].getObj());
	              } catch (Exception e1) {
	              	exitCode=0;
	                //servercomment System.out.println("SQL executeQuery exception"+e1.getMessage());
	              }
	              _createString(rs);
	              outputXML+="<PARAMETER><NAME>"+outPorts[0].getName()+"</NAME><TYPE>"+outPorts[0].getType()+
					"</TYPE><VALUE>"+outPorts[0].getObj().toString()+"</VALUE></PARAMETER>";
	              st.close();
	              dbCon.close();
	  	    	
	    	}catch (Exception ex) {
	    		exitCode=0;
	    		//servercomment System.out.println(ex.getMessage());
	    	}
    	}
    	else
    		exitCode=0;		
    	if(exitCode==1){
    		outPorts[1].setObj("TRUE");
    		outputXML+="<PARAMETER><NAME>"+outPorts[1].getName()+"</NAME><TYPE>"+outPorts[1].getType()+
			"</TYPE><VALUE>TRUE</VALUE></PARAMETER>";
    		return true;
    	}
    	else{
    		outPorts[1].setObj("FALSE");
    		outputXML+="<PARAMETER><NAME>"+outPorts[1].getName()+"</NAME><TYPE>"+outPorts[1].getType()+
			"</TYPE><VALUE>FALSE</VALUE></PARAMETER>";
    		return false;
    	}
    }                     
      /**Create a string result.
       */
      private void _createString(ResultSet rs){
          try {
            ResultSetMetaData md = rs.getMetaData();
            String res = "";
            while (rs.next()) {
              for (int i = 1; i <= md.getColumnCount(); i++) {
                res += md.getColumnName(i) + ": ";
                String val = rs.getString(i);
                if (val == null)
                  res += "";
                else
                  res += val;
                res += " ;  ";
              }
                res += "\n";
            }
            outPorts[0].setObj(res);
            rs.close();
          } catch (Exception ex) {
            //servercomment System.out.println(ex.getMessage()+" exception in create XML stream");
          }
      }              
      /** Create an XML stream result.
       */
      private void _createXML(ResultSet rs){
          try {
              String tab = "    ";
              String finalResult = "<?xml version=\"1.0\"?> \n";
              finalResult += "<result> \n";
              ResultSetMetaData md = rs.getMetaData();

              int colNum = md.getColumnCount();
              String tag[] = new String[colNum]; // holds all the result tags.
              for (int i = 0; i < colNum; i++) {
                  tag[i] = md.getColumnName(i+1);
                  tag[i] = tag[i].replace(' ','_');
                  if (tag[i].startsWith("#")) {
                      tag[i] = tag[i].substring(1);
                  }

                  //when joining two or more tables that have the same columns we'd like to distinguish between them.
                  int count = 1;
                  int j;
                  while (true) { //if the same tag appears more then once add an incremental index to it.
                      for (j=0; j<i; j++) {
                          if (tag[i].equals(tag[j])) { //the new tag already exist
                              if (count == 1) { // first duplicate
                                  tag[i] = tag[i] + count;
                              }
                              else {
                                  int tmp = count-1;
                                  String strCnt = "" + tmp;
                                  int index = tag[i].lastIndexOf(strCnt);
                                  tag[i] = tag[i].substring(0,index);  //remove the prev index.
                                  tag[i] = tag[i] + count;
                              }
                              count++;
                              break;
                          }
                      }
                      if (j==i) {//the tag was not found in existing tags.
                          count = 1;
                          break;
                      }
                  }
              }

              while (rs.next()) {
                  String res = tab + "<row> \n";

                  for (int i = 0; i < colNum; i++) {
                      String val = rs.getString(i+1);
                      res += tab + tab;
                      if (val == null) {
                        res += "<" + tag[i] + "/>\n";
                      }
                      else {
                        res += "<" + tag[i] + ">" + val + "</" + tag[i] + ">\n";
                      }
                  }
                  res += tab + "</row> \n";

                    finalResult += res;
            }
            finalResult += "</result>";
            outPorts[0].setObj(finalResult);
            rs.close();
          } catch (Exception ex) {
              //servercomment System.out.println(ex.getMessage()+"exception in create XML stream");
          }
      }
 	 /**
 	  * 将显示和控制功能分开,DatabaseDlg是单独的DatabaseAdapter显示类
 	  * TODO 基于JDialog类的对话框
 	  */
 		private class DatabaseDlg extends JDialog {
 			private JPanel jPanelAll = null;
 			private JPanel jPanelOut = null;
 			private JPanel jPanelIn = null;
 			private JPanel jPanelWs = null;
 			private JScrollPane jScrollPane = null;
 			private JTable jTableOut = null;
 			private JTable jTableIn = null;
 			private JLabel jLabel = null;
 			private JComboBox jComboBoxDBformat = null;
 			private JLabel jLabel1 = null;
 			private JLabel jLabel2 = null;
 			private JLabel jLabel3 = null;
 			private JLabel jLabel6 = null;
 			private JLabel jLabel7 = null;
 			private JTextField jTextFieldUser = null;
 			private JTextField jTextFieldPassword = null;
 			private JTextField jTextFieldDBDriver = null;
 			private JTextField jTextFieldDBUrl = null; 			
 			private JTextField jTextFieldPreSQL = null;
 			private JButton jButtonGetIo = null;
 			private JButton jButtonSave = null;
 			private JButton jButtonQuit = null;
 			private JTextField jTextFieldValue = null;
 			private JButton jButtonConfirm = null;
 			private JButton jButtonInvoke = null;
 			private JLabel jLabel4 = null;
 			private JLabel jLabel5 = null;
 			private JScrollPane jScrollPane1 = null;
 			
 			//该显示对话框对应的Model数据类
 			private DatabaseAdapter master;
 			//声明类AbstractTableModel扩展类MyTablemodel的两个对象
 			private MyTablemodel tmIn;
 			private MyTablemodel tmOut;
 		 
 			/**
 			 * This is the default constructor
 			 */
 			public DatabaseDlg() {
 				super();
 				initialize();
 			}
 			
 			/*
 			 * 定制的对话框构造函数 
 			 */
 			public DatabaseDlg(DatabaseAdapter x) {		
 				super();
 				this.master=x;
 				initialize();
 			}

 			/*
 			 * 配置JTable显示相应的IO端口
 			 */			
 			private void configIOTable(){
 				tmIn.vect.removeAllElements();
 				tmIn.fireTableStructureChanged();
 				tmOut.vect.removeAllElements();
 				tmOut.fireTableStructureChanged();
 				jTextFieldValue.setText("");
 				//输入端口表
 				for(int i=0;i<master.inNum;i++){
 					Vector inV=new Vector();
 					inV.add(0,""+i);
 					inV.add(1,master.inPorts[i].getName());
 					inV.add(2,master.inPorts[i].getType());
 					inV.add(3,"");
 					tmIn.vect.addElement(inV);
 				}				
 				tmIn.fireTableStructureChanged();
 				//输出端口表
 				for(int i=0;i<master.outNum;i++){
 					Vector outV=new Vector();
 					outV.add(0,""+i);
 					outV.add(1,master.outPorts[i].getName());
 					outV.add(2,master.outPorts[i].getType());
 					outV.add(3,"");
 					tmOut.vect.addElement(outV);
 				}		
 				tmOut.fireTableStructureChanged();
 			}
 			
 			/*
 			 * 根据输入Database操作方法的输入端口数据类型,生成Java Class方法调用的输入对象数组 
 			 */			
 			private void getInvokeObjectArray(){
 				Object[] obj=new Object[master.inNum];
 				String type="";
 				String value="";
 				
 				for(int i=0;i<master.inNum;i++){
					type=master.inPorts[i].getType();
 					value=(String)tmIn.getValueAt(i,3);
 					if(type.equalsIgnoreCase("int")||(type.equalsIgnoreCase("java.lang.Integer"))){
 						obj[i]=new Integer(value);
						master.inPorts[i].setObj(obj[i]);
 					}
 					else if(type.equalsIgnoreCase("double")||(type.equalsIgnoreCase("java.lang.Double"))){
 						obj[i]=new Double(value);
						master.inPorts[i].setObj(obj[i]);
 					}
 					else if(type.equalsIgnoreCase("float")||(type.equalsIgnoreCase("java.lang.Float"))){
 						obj[i]=new Double(value);
						master.inPorts[i].setObj(obj[i]);
 					}					
 					else if((type.equalsIgnoreCase("string"))||(type.equalsIgnoreCase("java.lang.String"))){
 						obj[i]=new String(value);
						master.inPorts[i].setObj(obj[i]);
 					}
 					else if(type.equalsIgnoreCase("long")||(type.equalsIgnoreCase("java.lang.Long"))){
 						obj[i]=new Long(value);
						master.inPorts[i].setObj(obj[i]);
 					}
 					else if((type.equalsIgnoreCase("boolean"))||(type.equalsIgnoreCase("java.lang.Boolean"))){
 						obj[i]=new Boolean(value);
						master.inPorts[i].setObj(obj[i]);
 					}
 					else if(type.equalsIgnoreCase("{int}")){
 						
 					}
 					else if(type.equalsIgnoreCase("{double}")){
 						
 					}
 					else if(type.equalsIgnoreCase("{float}")){
 						
 					}					
 					else if(type.equalsIgnoreCase("{string}")){
 						
 					}
 					else if(type.equalsIgnoreCase("{long}")){
 						
 					}
 					else if(type.equalsIgnoreCase("{boolean}")){
 						
 					}
 				}//end-for
 			}
 			
 			/*
 			 * 在JTable上显示该Database操作调用的输出端口
 			 */			
 			private void displayResults(){
 				for(int i=0;i<master.outNum;i++){
 					((Vector)tmOut.vect.elementAt(i)).setElementAt(master.outPorts[i].getObj(),3);				
 				}
 				tmOut.fireTableDataChanged();
 			}
 			
 			/**
 			 * This method initializes this
 			 * 
 			 * @return void
 			 */
 			private void initialize() {
 				this.setContentPane(getJPanelAll());
 				this.setTitle("Database Adapter");
 				this.setSize(511, 465);
 			}
 			/**
 			 * This method initializes jPanelOut	
 			 * 	
 			 * @return javax.swing.JPanel	
 			 */    
 			private JPanel getJPanelAll() {
 				if (jPanelAll == null) {
 					GridLayout gridLayoutAll = new GridLayout();
 					jPanelAll = new JPanel();
 					jPanelAll.setLayout(gridLayoutAll);
 					gridLayoutAll.setRows(3);			
 					jPanelAll.add(getJPanelWs(), null);
 					jPanelAll.add(getJPanelIn(), null);
 					jPanelAll.add(getJPanelOut(), null);

 				}
 				return jPanelAll;
 			}
 			/**
 			 * This method initializes jPanelOut	
 			 * 	
 			 * @return javax.swing.JPanel	
 			 */    
 			private JPanel getJPanelOut() {
 				if (jPanelOut == null) {
 					jLabel4 = new JLabel();
 					jPanelOut = new JPanel();
 					jPanelOut.setLayout(null);
 					jPanelOut.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
 					jLabel4.setBounds(1, -1, 93, 22);
 					jLabel4.setText("Output Ports:");
 					jPanelOut.add(getJScrollPane(), null);
 					jPanelOut.add(jLabel4, null);
 				}
 				return jPanelOut;
 			}
 			/**
 			 * This method initializes jPanelIn	
 			 			gridLayout2.setRows(2);
 		* 	
 			 * @return javax.swing.JPanel	
 			 */    
 			private JPanel getJPanelIn() {
 				if (jPanelIn == null) {
 					jLabel5 = new JLabel();
 					jPanelIn = new JPanel();
 					jPanelIn.setLayout(null);
 					jPanelIn.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
 					jLabel5.setBounds(5, 5, 82, 22);
 					jLabel5.setText("Input Ports:");
 					jPanelIn.add(getJTextFieldValue(), null);
 					jPanelIn.add(getJButtonConfirm(), null);
 					jPanelIn.add(getJButtonInvoke(), null);
 					jPanelIn.add(jLabel5, null);
 					jPanelIn.add(getJScrollPane1(), null);
 				}
 				return jPanelIn;
 			}
 			/**
 			 * This method initializes jPanel2	
 			 * 	
 			 * @return javax.swing.JPanel	
 			 */    
 			private JPanel getJPanelWs() {
 				if (jPanelWs == null) {
 					jLabel6 = new JLabel();
 					jLabel7 = new JLabel();
 					jLabel3 = new JLabel();
 					jLabel2 = new JLabel();
 					jLabel1 = new JLabel();
 					jLabel = new JLabel();
 					jPanelWs = new JPanel();
 					jPanelWs.setLayout(null);
 					jLabel.setBounds(8, 9, 90, 30);
 					jLabel.setText("DB format:");
 					jLabel7.setBounds(250, 9, 90, 30);
 					jLabel7.setText("Driver name:"); 					
 					jLabel1.setBounds(250, 40, 90, 30);
 					jLabel1.setText("Pre SQL:");
 					jLabel6.setBounds(8, 40, 120, 30);
 					jLabel6.setText("DB URL:");					
 					jLabel2.setBounds(8, 71, 90, 30);
 					jLabel2.setText("User name:");
 					jLabel3.setBounds(8, 102, 90, 30);
 					jLabel3.setText("Password:");
 					
 					jPanelWs.add(jLabel, null);
 					jPanelWs.add(getJComboBoxDBformat(), null);
 					jPanelWs.add(jLabel2, null);
 					jPanelWs.add(jLabel3, null);
 					jPanelWs.add(getJTextFieldUser(), null);
 					jPanelWs.add(getJTextFieldPassword(), null);
 					jPanelWs.add(getJTextFieldDBDriver(), null);
 					jPanelWs.add(getJTextFieldDBUrl(), null); 					
 					jPanelWs.add(getJTextFieldPreSQL(), null);
 					jPanelWs.add(getJButtonGetIo(), null);
 					jPanelWs.add(getJButtonSave(), null);
 					jPanelWs.add(getJButtonQuit(), null);
 					jPanelWs.add(jLabel1, null);
 					jPanelWs.add(jLabel6, null);
 					jPanelWs.add(jLabel7, null);
 				}
 				return jPanelWs;
 			}
 			/**
 			 * This method initializes jScrollPane	
 			 * 	
 			 * @return javax.swing.JScrollPane	
 			 */    
 			private JScrollPane getJScrollPane() {
 				if (jScrollPane == null) {
 					jScrollPane = new JScrollPane();
 					jScrollPane.setViewportView(getJTableOut());
 					jScrollPane.setBounds(14, 20, 466, 117);
 				}
 				return jScrollPane;
 			}
 			/**
 			 * This method initializes jTable	
 			 * 	
 			 * @return javax.swing.JTable	
 			 */    
 			private JTable getJTableOut() {
 				if (jTableOut == null) {
 					tmOut=new MyTablemodel();
 					tmOut.vect=new Vector();//实例化向量 
 					jTableOut = new JTable(tmOut);
 					jTableIn.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
 				}
 				return jTableOut;
 			}
 			/**
 			 * This method initializes jTable1	
 			 * 	
 			 * @return javax.swing.JTable	
 			 */    
 			private JTable getJTableIn() {
 				if (jTableIn == null) {
 					tmIn=new MyTablemodel();
 					tmIn.vect=new Vector();//实例化向量 
 					jTableIn = new JTable(tmIn);
 					jTableIn.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
 					jTableIn.addFocusListener(new java.awt.event.FocusAdapter() { 
 						public void focusGained(java.awt.event.FocusEvent e) {    
 							//servercomment System.out.println("focusGained()"); // TODO Auto-generated Event stub focusGained()
 							int iSel=jTableIn.getSelectedRow();
 							//jPanelgroupInValue.setEnabled(true);
 							String sValue=(String)tmIn.getValueAt(iSel,3);
 							jTextFieldValue.setText(sValue);
 							
 						}
 					});
 				}
 				return jTableIn;
 			}
 			/**
 			 * This method initializes jComboBox	
 			 * 	
 			 * @return javax.swing.JComboBox	
 			 */    
 			private JComboBox getJComboBoxDBformat() {
 				int formatsNum=DatabaseAdapter.DBFormats.length;
 				if (jComboBoxDBformat == null) {
 					jComboBoxDBformat = new JComboBox();
 					for(int i=0;i<formatsNum;i++)
 						jComboBoxDBformat.addItem(DatabaseAdapter.DBFormats[i]);
 					jComboBoxDBformat.setSelectedIndex(-1);
 					jComboBoxDBformat.setEditable(true);
 					jComboBoxDBformat.setBounds(80, 9, 150, 30);
 					jComboBoxDBformat.addItemListener(new java.awt.event.ItemListener() { 
 						public void itemStateChanged(java.awt.event.ItemEvent e) {    
 							//servercomment System.out.println("jComboBoxDBformat itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
 							int iSel=jComboBoxDBformat.getSelectedIndex();
							if(iSel<0)
								return;
 							master._dbFormat=iSel; 							
 							master.driverName=DatabaseAdapter.DBDrivers[master._dbFormat];
 							master.databaseURL="";
 							master._setDBURL();
 							jTextFieldDBDriver.setText(null);
 							jTextFieldDBUrl.setText(null);
 							jTextFieldDBDriver.setText(master.driverName);
 							jTextFieldDBUrl.setText(master.databaseURL);
 							tmIn.vect.removeAllElements();
 							tmOut.vect.removeAllElements();
 							tmIn.fireTableStructureChanged();
 							tmOut.fireTableStructureChanged();
 							jTextFieldValue.setText("");
 						}
 					});

 				}
 				return jComboBoxDBformat;
 			}
 			
 			/**
 			 * This method initializes jTextField	
 			 * 	
 			 * @return javax.swing.JTextField	
 			 */    
 			private JTextField getJTextFieldUser() {
 				if (jTextFieldUser == null) {
 					jTextFieldUser = new JTextField();
 					jTextFieldUser.setBounds(80, 71, 150, 30);
 				}
 				return jTextFieldUser;
 			}
 			/**
 			 * This method initializes jTextField	
 			 * 	
 			 * @return javax.swing.JTextField	
 			 */    
 			private JTextField getJTextFieldPassword() {
 				if (jTextFieldPassword == null) {
 					jTextFieldPassword = new JTextField();
 					jTextFieldPassword.setBounds(80, 102, 150, 30);
 				}
 				return jTextFieldPassword;
 			}
 			/**
 			 * This method initializes jTextField	
 			 * 	
 			 * @return javax.swing.JTextField	
 			 */    
 			private JTextField getJTextFieldDBDriver() {
 				if (jTextFieldDBDriver == null) {
 					jTextFieldDBDriver = new JTextField();
 					jTextFieldDBDriver.setBounds(330, 9, 150, 30);
 				}
 				return jTextFieldDBDriver;
 			}
 			/**
 			 * This method initializes jTextField	
 			 * 	
 			 * @return javax.swing.JTextField	
 			 */    
 			private JTextField getJTextFieldDBUrl() {
 				if (jTextFieldDBUrl == null) {
 					jTextFieldDBUrl = new JTextField();
 					jTextFieldDBUrl.setBounds(80, 40, 150, 30);
 				}
 				return jTextFieldDBUrl;
 			}
 			private JTextField getJTextFieldPreSQL() {
 				if (jTextFieldPreSQL == null) {
 					jTextFieldPreSQL = new JTextField();
 					jTextFieldPreSQL.setBounds(330, 40, 150, 30);
 				}
 				return jTextFieldPreSQL;
 			}
 			
 			/**
 			 * This method initializes jButton	
 			 * 	
 			 * @return javax.swing.JButton	
 			 */    
 			private JButton getJButtonGetIo() {
 				if (jButtonGetIo == null) {
 					jButtonGetIo = new JButton();
 					jButtonGetIo.setBounds(257, 102, 91, 30);
 					jButtonGetIo.setText("Get Ports");
 					jButtonGetIo.addActionListener(new java.awt.event.ActionListener() { 
 						public void actionPerformed(java.awt.event.ActionEvent e) {    
 							//servercomment System.out.println("jButtonGetIo actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 							if((jTextFieldDBDriver.getText().length() ==0)||(jTextFieldDBUrl.getText().length() ==0)){
 								MessageBox msg=new MessageBox("Null Warning!","Database Driver name or URL can not be Null!");
 								msg.setVisible(true);																					
 								return;
 							}
 							master.databaseURL=jTextFieldDBUrl.getText();
 							master.driverName=jTextFieldDBDriver.getText();
 							master.predefinedSQL=jTextFieldPreSQL.getText();
 							master.userName=jTextFieldUser.getText();
 							master.password=jTextFieldPassword.getText();		
 							master.configureAdapter();
 							configIOTable();					
 						}
 					});
 				}
 				return jButtonGetIo;
 			}
 			/**
 			 * This method initializes jButton	
 			 * 	
 			 * @return javax.swing.JButton	
 			 */    
 			private JButton getJButtonSave() {
 				if (jButtonSave == null) {
 					jButtonSave = new JButton();
 					jButtonSave.setBounds(390, 71, 90, 30);
 					jButtonSave.setText("Save");
 					jButtonSave.setMnemonic(java.awt.event.KeyEvent.VK_S);
 					jButtonSave.addActionListener(new java.awt.event.ActionListener() { 
 						public void actionPerformed(java.awt.event.ActionEvent e) {    
 							//servercomment System.out.println("jButtonSave actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 							if((master.inNum==0)||(tmIn.getRowCount()==0)){
 								MessageBox msg=new MessageBox("Null Warning!","Please press 'Get IOPorts' button first!");
 								msg.setVisible(true);																												
 								return;						
 							}
 							master.save2DB();	
 							closeDialog();
 						}
 					});
 				}
 				return jButtonSave;
 			}
 			/**
 			 * This method initializes jButton	
 			 * 	
 			 * @return javax.swing.JButton	
 			 */    
 			private JButton getJButtonQuit() {
 				if (jButtonQuit == null) {
 					jButtonQuit = new JButton();
 					jButtonQuit.setBounds(390, 102, 90, 30);
 					jButtonQuit.setText("Cancel");
 					jButtonQuit.addActionListener(new java.awt.event.ActionListener() { 
 						public void actionPerformed(java.awt.event.ActionEvent e) {    
 							//servercomment System.out.println("jButtonQuit actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 							closeDialog();
 						}
 					});
 				}
 				return jButtonQuit;
 			}
 			/**
 			 * This method initializes jTextField	
 			 * 	
 			 * @return javax.swing.JTextField	
 			 */    
 			private JTextField getJTextFieldValue() {
 				if (jTextFieldValue == null) {
 					jTextFieldValue = new JTextField();
 					jTextFieldValue.setName("jTextFieldValue");
 					jTextFieldValue.setBounds(94, 7, 88, 21);
 				}
 				return jTextFieldValue;
 			}
 			/**
 			 * This method initializes jButton	
 			 * 	
 			 * @return javax.swing.JButton	
 			 */    
 			private JButton getJButtonConfirm() {
 				if (jButtonConfirm == null) {
 					jButtonConfirm = new JButton();
 					jButtonConfirm.setText("Confirm");
 					jButtonConfirm.setName("jButtonConfirm");
 					jButtonConfirm.setBounds(185, 8, 90, 21);
 					jButtonConfirm.addActionListener(new java.awt.event.ActionListener() { 
 						public void actionPerformed(java.awt.event.ActionEvent e) {    
 							//servercomment System.out.println("jButtonConfirm actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 							String sValue=jTextFieldValue.getText();
 							if(sValue.length()==0){
 								MessageBox msg=new MessageBox("Null Warning!","Value of Input param can not be Null!");
 								msg.setVisible(true);																																			
 								return;
 							}
 							int iSel=jTableIn.getSelectedRow();
 							((Vector)tmIn.vect.elementAt(iSel)).setElementAt(sValue,3);
 							tmIn.fireTableCellUpdated(iSel,3);
 						}
 					});
 				}
 				return jButtonConfirm;
 			}
 			/**
 			 * This method initializes jButton	
 			 * 	
 			 * @return javax.swing.JButton	
 			 */    
 			private JButton getJButtonInvoke() {
 				if (jButtonInvoke == null) {
 					jButtonInvoke = new JButton();
 					jButtonInvoke.setBounds(386, 6, 94, 23);
 					jButtonInvoke.setText("Invoke");
 					jButtonInvoke.addActionListener(new java.awt.event.ActionListener() { 
 						public void actionPerformed(java.awt.event.ActionEvent e) {    
 							//servercomment System.out.println("jButtonInvoke actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 							if((master.inNum==0)||(tmIn.getRowCount()==0)){
 								MessageBox msg=new MessageBox("Null Warning!","Please press 'Get Ports' button first!");
 								msg.setVisible(true);																																										
 								return;						
 							}
 							for(int i=0;i<(master.inNum);i++){
 								String s=(String)tmIn.getValueAt(i,3).toString();
 								if((s.length()==0)&(master.predefinedSQL.length()==0)){
 									MessageBox msg=new MessageBox("Null Warning!","Value of Input Parameters can not be Null!");
 									msg.setVisible(true);																																																		
 									return;
 								}				
 							}
 							getInvokeObjectArray();
 							master.execSql();
 							MessageBox msg=new MessageBox("SQL Result!",master.outPorts[0].getObj().toString());
							msg.setVisible(true);
 							displayResults();					
 						}
 					});
 				}
 				return jButtonInvoke;
 			}
 			/**
 			 * This method initializes jScrollPane1	
 			 * 	
 			 * @return javax.swing.JScrollPane	
 			 */    
 			private JScrollPane getJScrollPane1() {
 				if (jScrollPane1 == null) {
 					jScrollPane1 = new JScrollPane();
 					jScrollPane1.setBounds(11, 29, 469, 102);
 					jScrollPane1.setViewportView(getJTableIn());
 				}
 				return jScrollPane1;
 			}
 			private void closeDialog(){
 				this.dispose();
 			}
 			private class MyTablemodel extends AbstractTableModel{
 				//声明一个向量对象
 				public Vector vect;
 				//二维表列名
 				private String title[]={"No","Name","Type","Value"}; 
 				public int getColumnCount(){ 
 					return title.length;}//取得表格列数 
 				public int getRowCount(){ 
 					return vect.size();}//取得表格行数 
 				public Object getValueAt(int row,int column){ 
 					if(!vect.isEmpty()) 
 						return((Vector)vect.elementAt(row)).elementAt(column); 
 					else 
 						return null;}//取得单元格中的属性值 
 				public String getColumnName(int column){ 
 					return title[column];}//设置表格列名 			
 				public void setValueAt 
 					(Object value,int row,int column){}//数据模型不可编辑，该方法设置为空 
 				public Class getColumnClass(int c){ 
 					return getValueAt(0,c).getClass();}//取得列所属对象类 
 				public boolean isCellEditable(int row,int column){ 
 					return false;}//设置单元格不可编辑，为缺省实现 
 			}; 			
 		}  //  @jve:decl-index=0:visual-constraint="10,10"
 		
 		
 		
 	
 		
             
}
