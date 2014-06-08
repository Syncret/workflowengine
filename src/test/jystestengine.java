package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import cit.workflow.utils.DBUtility;
import cit.workflow.utils.WorkflowConnectionPool;

import com.ibm.icu.util.Calendar;

public class jystestengine extends DBUtility{
	public static void main(String argv[]){
		test1();
//		System.out.println(org.apache.commons.dbutils.QueryRunner.class.getProtectionDomain().getCodeSource().getLocation());
	}
	
	private static void test1(){
//		servercomment System.out.println(MoreExecutors.class.getProtectionDomain().getCodeSource().getLocation());
//		StorageService service=new StorageService(0,"aws-s3",null,null,null);
		Connection conn = WorkflowConnectionPool.getInstance().getConnection();
		String updateSQL = "INSERT INTO processlogs(processid,log,starttime, endtime) VALUES(?,?,?,?)";
		long starttime=System.currentTimeMillis();
		long endtime=System.currentTimeMillis();
		PreparedStatement updatePStat;
		try {
			updatePStat = conn.prepareStatement(updateSQL);
			updatePStat.setString(1, "123");
			updatePStat.setString(2, "");
			updatePStat.setLong(3, 3);
			updatePStat.setLong(4, 4);
			updatePStat.executeUpdate();
			updatePStat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
