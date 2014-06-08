package cit.workflow.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cit.workflow.Constants;
import cit.workflow.utils.WorkflowConnectionPool;

public class DatabaseManager {

	public static String WORKFLOWDATABASE=Constants.CONN_DATABASE;
	public static final String SQLFILE="database.sql";
	Connection conn;
	
	public DatabaseManager(){
		try {
			Class.forName(Constants.CONN_DRIVER);
			conn = DriverManager
					.getConnection(Constants.CONN_BASEURL, Constants.CONN_USERNAME, Constants.CONN_PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
			try {
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	public void importDatabase() {
		URL str3 = WorkflowServerImpl.class.getResource(SQLFILE);
		if (str3 == null) {
			System.out.println("Error, sql file not exsits");
			return;
		}
		File file = new File(str3.getFile());


		if (!file.exists()) {
			System.out.println("sql file not exsit");
			return;
		}
		
		InputStreamReader fileReader=null;
		try {
			fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			e1.printStackTrace();
		}

		System.out.println("Read File "+file.getAbsolutePath());
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		try {
			StringBuilder sBuilder = new StringBuilder("");
			String str = bufferedReader.readLine();
			while (str != null) {
				// 去掉一些注释，和一些没用的字符
				if (!str.startsWith("#") && !str.startsWith("/*") && !str.startsWith("–") && !str.startsWith("\n"))
					sBuilder.append(str);
				str = bufferedReader.readLine();
			}
			String[] strArr = sBuilder.toString().split(";");
			List<String> strList = new ArrayList<String>();
			for (String s : strArr) {
				strList.add(s);
//				System.out.println(s);
			}
			executeBatchByStatement(strList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
				fileReader.close();
			} catch (Exception e) {
			}
		}

	}



	public  boolean executeBatchByStatement(List<String> sqlList) {
		System.out.println("start importing database");
		Statement stmt = null;
		boolean ret = false;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for (String s : sqlList) {
				stmt.addBatch(s);
			}
			stmt.executeBatch();
			conn.commit();
			ret = true;
		} catch (BatchUpdateException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.setAutoCommit(true);
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("import database "+ (ret?"success":"fail"));
		return ret;
	}
	
	public boolean checkDatabase(){
		Statement stmt = null;
		ResultSet rs=null;
		try {
			String sql="select count(*) as numofjworkflow from schemata where schema_name='"+WORKFLOWDATABASE+"'";
			stmt=conn.createStatement();
			rs=stmt.executeQuery(sql);
			int num=0;
			while(rs.next()){
				num=rs.getInt(1);
				if(num>0) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if(rs!=null)rs.close();
				if (stmt != null)stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return false;
	}
	
	public void createDatabase() {
		Statement stmt = null;
		try {
			System.out.println("Connecting to database...");
			// STEP 4: Execute a query
			System.out.println("Creating database...");
			stmt = conn.createStatement();

			String sql = "CREATE DATABASE jworkflow";
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)stmt.close();
			} catch (SQLException se2) {
				se2.printStackTrace();
			}// nothing we can do
		}// end try
	}
}
