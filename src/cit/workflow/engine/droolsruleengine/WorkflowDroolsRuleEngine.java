package cit.workflow.engine.droolsruleengine;

// lrj add begin 07-11-8

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader; 
import java.io.File; 
import java.io.FileReader; 
import java.io.FileWriter; 
import java.io.IOException; 
import java.io.PrintWriter; 
import java.lang.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

import cit.workflow.Constants;
import cit.workflow.dao.PersonIDDAO;
import cit.workflow.dao.ProcessDAO;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;
import cit.workflow.elements.activities.AbstractActivity;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.utils.WorkflowConnectionPool;
import cit.workflow.WorkflowServer;
import cit.workflow.engine.droolsruleengine.FactsClass;

public class WorkflowDroolsRuleEngine
{	
	public WorkflowDroolsRuleEngine(){};
	public WorkflowDroolsRuleEngine(String ProcessID ,int EventID)
	{
		this.ProcessID = ProcessID;
		this.EventID = EventID;
	}
	
	public void RunEngine(Connection conn)
	{
		try
		{
		/*
		 * 访问数据库的ProcessExtendedRules表
		 * 根据ProcessID和EventID,导入规则文件的内容，
		 * 以string类型,若是多个规则文件，形成一个string类型数组
		 */
			Files f = new Files(); 	
			String ruleFileContent = "";
//			Connection conn = WorkflowConnectionPool.getInstance().getConnection();
			
			ArrayList ruleList = new ArrayList();
			ruleList = new ProcessDAO(conn).getProcessExtendedRule(this.ProcessID, this.EventID);
			
		/*
		 * 将规则文件的string类型数据写入到bin包中
		 */		
			String curDir = System.getProperty("user.dir");
			String ruleFilePath = curDir + "\\bin\\";		
			String ruleFileName = "";
				
			Iterator iterator = ruleList.iterator();			
			while(iterator.hasNext())
			{
				ExtendedDroolsRule tempRule = (ExtendedDroolsRule) iterator.next();
				ruleFileName = tempRule.getProcessID() +"_" + tempRule.getEventID() + "_" +  tempRule.getRuleFileID() + ".drl";	    						
				ruleFileContent += tempRule.getRuleContent();
				////servercomment System.out.println(ruleFilePath);
				f.createAndDeleteFile(ruleFilePath,ruleFileName);
	    		f.writeFile(ruleFilePath,ruleFileName,ruleFileContent);
//	    		//servercomment System.out.println(ruleFileContent);
			}
 
		/*
		 * 由PackageBuilder将规则文件打包
		 * 注意对于增加到当前PackageBuilder实例中的所有规则包，必须具有同样的包名称空间
		 */    		
    		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
    		Iterator iterator1 = ruleList.iterator();			
			while(iterator1.hasNext())
			{
				ExtendedDroolsRule tempRule = (ExtendedDroolsRule) iterator1.next();
				ruleFileName = tempRule.getProcessID() +"_" + tempRule.getEventID() + "_" +  tempRule.getRuleFileID() + ".drl";
				readRule(ruleFileName,ruleBase);				
			}    		
	        
		/*
		 * 初始化一个有状态的session,规则引擎启动
		 */
	        FactsClass processID = new FactsClass(this.ProcessID);
	        FactsClass eventID   = new FactsClass(this.EventID);
	        
	        ArrayList FactsArrayList = new ArrayList();	  
	        FactsArrayList.add(conn);
	        FactsArrayList.add(processID);
	        FactsArrayList.add(eventID);
	        
	        StatefulSession session = ruleBase.newStatefulSession();
	        InsertWorkingMemory(FactsArrayList,session);
            
	        session.fireAllRules();   
            session.dispose();
		}
		
		catch (Throwable t) 
		{
            t.printStackTrace();
        }
	}

	
	public void readRule(String ruleFileName,RuleBase ruleBase)throws Exception 
	{
		String str = '/' + ruleFileName;
		Reader source = new InputStreamReader( WorkflowDroolsRuleEngine.class.getResourceAsStream(str));		
		PackageBuilder builder = new PackageBuilder();
		builder.addPackageFromDrl( source );
		Package pkg = builder.getPackage();					
		ruleBase.addPackage( pkg );
	}
	
	public void InsertWorkingMemory(ArrayList list,StatefulSession session)
	{
		Iterator iterator = list.iterator();
		while(iterator.hasNext())
		{
			session.insert(iterator.next());
		}
	}

	private int EventID;
	private String ProcessID;

}

class Files 
{ 
	public Files(){}
	
	/** 
	* 文件的写入 
	* @param filePath(文件路径) 
	* @param fileName(文件名) 
	* @param args 
	* @throws IOException 
	*/ 
	public void writeFile(String filePath,String fileName,String args) throws IOException 
	{ 
		FileWriter fw = new FileWriter(filePath+fileName); 
		fw.write(args); 
		fw.close(); 
	}
	
	/** 
	* 创建与删除文件 
	* @param filePath 
	* @param fileName 
	* @return 创建成功返回true 
	* @throws IOException 
	*/ 
	public boolean createAndDeleteFile(String filePath,String fileName) throws IOException 
	{ 
		boolean result = false; 
		File file = new File(filePath,fileName); 
		if(file.exists()) 
		{ 
			file.delete(); 
		}			
		file.createNewFile(); 
		result = true; 
//		//servercomment System.out.println("文件已经创建！"); 
		return result; 
	} 
	

	/** 
	* 检查文件中是否为一个空 
	* @param filePath 
	* @param fileName 
	* @return 为空返回true 
	* @throws IOException 
	*/ 
	public boolean fileIsNull(String filePath,String fileName) throws IOException 
	{ 
		boolean result = false; 
		FileReader fr = new FileReader(filePath+fileName); 
		if(fr.read() == -1) 
		{ 
			result = true; 
			//servercomment System.out.println(fileName+" 文件中没有数据!"); 
		} 
		else 
		{ 
			//servercomment System.out.println(fileName+" 文件中有数据!"); 
		} 
		fr.close(); 
		return result; 
	} 
	/** 
	* 读取文件中的所有内容 
	* @param filePath 
	* @param fileName 
	* @throws IOException 
	*/ 
	public void readAllFile(String filePath,String fileName) throws IOException 
	{ 
		FileReader fr = new FileReader(filePath+fileName); 
		int count = fr.read(); 
		while(count != -1) 
		{ 
			//servercomment System.out.print((char)count); 
			count = fr.read(); 
		} 
		fr.close(); 
	} 
	/** 
	* 一行一行的读取文件中的数据 
	* @param filePath 
	* @param fileName 
	* @throws IOException 
	*/ 
	public String readLineFile(String filePath,String fileName) throws IOException 
	{ 
		FileReader fr = new FileReader(filePath+fileName); 
		BufferedReader br = new BufferedReader(fr); 
		String str = "";
		String line = br.readLine(); 
		str += line;

		while(line != null)
		{
			str += '\n';
			line = br.readLine();
			if(line != null)
				str += line;
		}
		return str;
	} 
}

// lrj add end 07-11-8

