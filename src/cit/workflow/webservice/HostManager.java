package cit.workflow.webservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.*;
import org.apache.log4j.Logger;

import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;

public class HostManager extends DBUtility {
	
	private static Logger logger = Logger.getLogger(HostManager.class);
	
	
	public HostManager(Connection conn){
		super(conn);
	}
	
	/**
	 * 改变host的状态
	 * @param address host的地址
	 * @param status 需修改为的状态，0为Available，1为Idle，2为Running
	 */
	public void changeHostStatus(String address,int status){
		String tstatus=null;
		if(status==0) tstatus="Available";
		else if(status==1) tstatus="Idle";
		else if (status==2) tstatus="Running";
		else if(status==-1) tstatus="Unavailable";
		else{
			logger.error("Illegal Status");
			return;
		}
		String updateSQL = "UPDATE availablehosts SET state = ? WHERE address = ?";
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setString(1, tstatus);
			updatePStat.setString(2, address);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("change host: {"+address+"} status to "+tstatus);
	}
	
	
	/**
	 * 改变host的状态
	 * @param address host的地址
	 * @param status 需修改为的状态，0为Available，1为Idle，2为Running
	 */
	public void changeHostPerformance(String address,int performance){
		String updateSQL = "UPDATE availablehosts SET performance = ? WHERE address = ?";
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setInt(1, performance);
			updatePStat.setString(2, address);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("change host: {"+address+"} performance to "+performance);
	}
	
	
	/**
	 * 获取一个空闲的Host地址
	 * @return
	 */
	public String getIdleHost(){
		String address=null;
		try {
			sql = "SELECT * from availablehosts WHERE state=?";
			params = new Object[] {"Idle"};
			types = new int[] {Types.VARCHAR};
			Map hostaddressMap;
			hostaddressMap = (Map) executeQuery(new MapHandler());
			if(hostaddressMap==null){
//				logger.info("No idle hosts now");
				return null;
			}
			address=(String) hostaddressMap.get("address");
			logger.info("get idle host: "+address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	/**
	 * 获取一个可用的Host地址
	 * @return
	 */
	public String getAvailableHost(){
		String address=null;
		try {
			sql = "SELECT * from availablehosts WHERE state=?";
			params = new Object[] {"Available"};
			types = new int[] {Types.VARCHAR};
			Map hostaddressMap;
			hostaddressMap = (Map) executeQuery(new MapHandler());
			if(hostaddressMap==null){
				logger.info("No available hosts now");
				return null;
			}
			address=(String) hostaddressMap.get("address");
			logger.info("get available host: "+address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	/**
	 * 获取正在运行的host
	 * @return
	 */
	public List<String> getRunningHosts(){
		
		List<String> address = new ArrayList<String>();
		try {
			sql = "SELECT address from availablehosts WHERE state=?";
			params = new Object[] {"Running"};
			types = new int[] {Types.VARCHAR};
			List<Map> hostaddressList;
			hostaddressList = (List) executeQuery(new MapListHandler());
			if(hostaddressList==null){
				logger.info("No running hosts now");
				return null;
			}
			Iterator addIterator=hostaddressList.iterator();
			while(addIterator.hasNext()){
				Map addrMap = (Map) addIterator.next();
				address.add((String)addrMap.get("address"));
			}
			logger.info("get running host: "+address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	/**
	 * 获取可用的hosts
	 * @return
	 */
	public List<String> getAvailableHosts(){
		
		List<String> address = new ArrayList<String>();
		try {
			sql = "SELECT address from availablehosts WHERE state=? order by Performance";
			params = new Object[] {"Available"};
			types = new int[] {Types.VARCHAR};
			List<Map> hostaddressList;
			hostaddressList = (List) executeQuery(new MapListHandler());
			if(hostaddressList==null){
				logger.info("No running hosts now");
				return null;
			}
			Iterator addIterator=hostaddressList.iterator();
			while(addIterator.hasNext()){
				Map addrMap = (Map) addIterator.next();
				address.add((String)addrMap.get("address"));
			}
			logger.info("get available hosts: "+address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	/**
	 * 获取host的基本性能
	 * @param address host的地址
	 */
	public int getPerformance(String address){
		int performance=0;
			try {
				sql = "SELECT performance from availablehosts WHERE address=?";
				params = new Object[] {address};
				types = new int[] {Types.VARCHAR};
				Map hostMap;
				hostMap = (Map) executeQuery(new MapHandler());
				if(hostMap==null){
					logger.error("no such host");
					return 0;
				}
				performance=(int)hostMap.get("performance");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return performance;
	}
	
	/**
	 * 获取正在运行的host
	 * @return
	 */
	public List<String> getDeployedHosts(){
		
		List<String> address = new ArrayList<String>();
		try {
			sql = "SELECT address from availablehosts WHERE state=? or state=? ORDER BY performance";
			params = new Object[] {"Idle","Running"};
			types = new int[] {Types.VARCHAR};
			List<Map> hostaddressList;
			hostaddressList = (List) executeQuery(new MapListHandler());
			if(hostaddressList==null){
				logger.info("No running hosts now");
				return null;
			}
			Iterator addIterator=hostaddressList.iterator();
			while(addIterator.hasNext()){
				Map addrMap = (Map) addIterator.next();
				address.add((String)addrMap.get("address"));
			}
			logger.info("get deployed host: "+address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	/**
	 * 添加可用的host
	 * @param address 添加的host地址
	 * @param performance 添加的host性能
	 */
	public void addAvailableHost(String address, int performance){
		try {
			sql = "SELECT * from availablehosts WHERE address=?";
			params = new Object[] {address};
			types = new int[] {Types.VARCHAR};
			Map hostaddressMap;
			hostaddressMap = (Map) executeQuery(new MapHandler());
			if(hostaddressMap!=null){
				logger.warn(address+" already in list");
				return;
			}
		} 
		 catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		String updateSQL = "INSERT INTO availablehosts VALUES(?,?,?)";
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setString(1, address);
			updatePStat.setString(2, "Available");
			updatePStat.setInt(3, performance);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("add available host:"+address);
	}
	
	/**
	 * 从数据库中删除host
	 * @param address 需删除的host
	 */
	public void deleteHost(String address){
		try {
			sql = "SELECT * from availablehosts WHERE address=?";
			params = new Object[] {address};
			types = new int[] {Types.VARCHAR};
			Map hostaddressMap;
			hostaddressMap = (Map) executeQuery(new MapHandler());
			if(hostaddressMap==null){
				logger.warn(address+" not in list");
				return;
			}
			if(hostaddressMap.get("status")=="Running"){
				logger.warn(address+" still in use");
				return;
			}
		} 
		 catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		String updateSQL = "DELETE from availablehosts WHERE address=?";
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setString(1, address);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("delete host: "+address);
	}
	
	public void commitAction(Connection connection) {
		try {
			connection.commit();
			connection.close();
			////servercomment System.out.println("Execution thread: " + Thread.currentThread().getName() + ", Connection commit and close");
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection commit error, message: " + e.getMessage());
		}
	}
	
	public void rollbackAction(Connection connection) {
		try {
			connection.rollback();
			connection.close();
			////servercomment System.out.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback and close");
		} catch(Exception e) {
			System.err.println("Execution thread: " + Thread.currentThread().getName() + ", Connection rollback error, message: " + e.getMessage());
		}
	}
	
	public static void main(String args[]){
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		HostManager hostmanager=new HostManager(conn);
		String host1="http://192.168.1.23:8080";
		String host2="http://192.168.1.32:8080";
		String host3="http://192.168.1.52:8080";
//		hostmanager.addAvailableHost(host1,6);
//		hostmanager.addAvailableHost(host2,6);
//		hostmanager.addAvailableHost(host3,6);
//		hostmanager.deleteHost(host1);
//		hostmanager.deleteHost(host2);
//		hostmanager.deleteHost(host3);
//		hostmanager.addAvailableHost(host3,12);
//		hostmanager.changeHostPerformance(host1, 6);
//		hostmanager.changeHostPerformance(host2, 8);
//		hostmanager.changeHostPerformance(host3, 4);
		hostmanager.changeHostStatus(host1,1);
		hostmanager.changeHostStatus(host2,0);
		hostmanager.changeHostStatus(host3,1);
//		//servercomment System.out.println(hostmanager.getPerformance("http://192.168.1.32:8080"));
//		hostmanager.getDepolyedHosts();
//		String a=hostmanager.getAvailableHost();
//		String b=hostmanager.getIdleHost();
//		List<String> addresses=hostmanager.getRunningHosts();
		hostmanager.commitAction(conn);
		
	}
}
