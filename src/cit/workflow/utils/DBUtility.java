/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DBUtility {
	
	/* 数据库操作对象定义 */
	protected Connection conn;
	
	protected QueryRunner query;
	
	protected Object[] params;
	
	protected int[] types;
	
	protected String sql;
	
	//dingo 8.2
	public DBUtility() {}
	
	public DBUtility(Connection conn) {
		this.conn = conn;
		this.query = new QueryRunner();
	}
	
	public Object executeQuery(ResultSetHandler rsh) throws SQLException {
		return query.query(conn, sql, rsh, params);
	}
	
	public void executeUpdate() throws SQLException {
//		query.update(conn, sql, params, types);
		query.update(conn, sql, params);
		
	}
	public int executeUpdate(int DFTest) throws SQLException {
//		return (query.update(conn, sql, params, types));
		return (query.update(conn, sql, params));
	}
}
