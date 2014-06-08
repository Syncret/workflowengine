/*
 * Created on 2004-10-14
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import cit.workflow.Constants;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WorkflowConnectionPool {
	
	private DataSource dataSource;
	
	//工作流数据库连接池唯一实例
	private static final WorkflowConnectionPool connectionPool = new WorkflowConnectionPool();
	
	private static Logger logger = Logger.getLogger(WorkflowConnectionPool.class);
	
	/**
	 * Get instance of the connection pool
	 * singleton pattern
	 * @return handle of connection pool
	 */
	public static WorkflowConnectionPool getInstance() {
//		//servercomment System.out.println("getInstance");
		return connectionPool;
	}
	
	private WorkflowConnectionPool() {
//		//servercomment System.out.println("constructed");
		initializeConnectionPool();
	}
	
	/**
	 * Get a handle of dataSource to connection the database
	 * Notice: you can change "default auto commit" mode of the database in this function
	 * @return org.apache.commons.dbcp.BasicDataSource
	 */
	private DataSource setupDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(Constants.CONN_DRIVER);
		ds.setUrl(Constants.CONN_URL);
		ds.setUsername(Constants.CONN_USERNAME);
		ds.setPassword(Constants.CONN_PASSWORD);
		//ds.setDefaultAutoCommit(Constants.CONN_AUTOCOMMIT);//dingo updated on 07.07.31 for mysql db
		ds.setDefaultAutoCommit(false);//dingo updated on 07.07.31 for mysql db
		ds.setTestOnBorrow(true);
		return ds;
	}
	
	private void initializeConnectionPool() {
		this.dataSource = setupDataSource();
	}
	
	/**
	 * Get a connection handle of the database, this operation is automicity
	 * Notice: you can change "auto commit" mode of the connection in this function
	 * @return handle of connection
	 */
	public synchronized Connection getConnection() {
//		//servercomment System.out.println("getConnection");
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			//set auto commit
			conn.setAutoCommit(false);
		} catch(SQLException e) {
			logger.error(e.getMessage());
		}
		return conn;
	}
	
	/**
	 * Close the dataSource of the database
	 */
	public void close() {
		try {
			BasicDataSource bds = (BasicDataSource) dataSource;
			bds.close();
		}  catch(SQLException e) {
			logger.error(e.getMessage());
		}
	}
}
