package cit.workflow.monitor;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.util.StringTokenizer;


/**
 * 
 * 获取系统信息的业务逻辑实现类.
 * 
 * @author GuoHuang
 */
@SuppressWarnings("restriction")
public class PerfMonitor {

	private static final int CPUTIME = 30;

	private static final int PERCENT = 100;

	private static final int FAULTLENGTH = 10;

	private static final long KB=1024;
	private static final long MB=1048576;
	private static String linuxVersion = null;
	
	private String osName;
	private long totalPhysicalMemory;
	/** 可使用内存. */
	private long totalMemory;
	/** 剩余内存. */
	private long freeMemory;
	/** 最大可使用内存. */
	private long maxMemory;

	private OperatingSystemMXBean osmxb;
	
	public PerfMonitor(){
		osName = System.getProperty("os.name");
		osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		totalPhysicalMemory=osmxb.getTotalPhysicalMemorySize();
		totalMemory = Runtime.getRuntime().totalMemory() / MB;
		maxMemory = Runtime.getRuntime().maxMemory() /MB;
	}

	/**
	 * 获得当前的监控对象.
	 * 
	 * @return 返回构造好的监控对象
	 * @throws Exception
	 * @author GuoHuang
	 */
	public void getFreeMemory() throws Exception {
		freeMemory = Runtime.getRuntime().freeMemory() /MB;

	}
	
	public long getTotalPhysicalMemory(){
		return totalPhysicalMemory;
	}
	
	public double getCpuRatio() {
		double cpuRatio = 0;
		if (osName.toLowerCase().startsWith("windows")) {
			cpuRatio = this.getCpuRatioForWindows();
		} else {
			cpuRatio = PerfMonitor.getCpuRateForLinux();
		}
		return cpuRatio;
	}
	
	public long getUsedMemory(){
		long freeMemSize=osmxb.getFreePhysicalMemorySize();
		return (totalPhysicalMemory - freeMemSize)/MB;
	}

	public double getMemoryRatio(){
		long usedMemSize=totalPhysicalMemory-osmxb.getFreePhysicalMemorySize();
		return ((double)usedMemSize)/totalPhysicalMemory*100;
	}
	
	private static double getCpuRateForLinux() {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader brStat = null;
		StringTokenizer tokenStat = null;
		try {
			System.out.println("Get usage rate of CUP , linux version: " + linuxVersion);

			Process process = Runtime.getRuntime().exec("top -b -n 1");
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			brStat = new BufferedReader(isr);

			brStat.readLine();
			brStat.readLine();

			tokenStat = new StringTokenizer(brStat.readLine());
			String cpuUsage="0";
			while(tokenStat.hasMoreTokens()){
				String s=tokenStat.nextToken();
				int index=s.indexOf("%id");
				if(index<0)continue;
				else{
					cpuUsage=s.substring(0,index);
					break;
				}
			}
			Float usage = new Float(cpuUsage);
			return (1.00 - usage/100);

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			freeResource(is, isr, brStat);
			return 1;
		} finally {
			freeResource(is, isr, brStat);
		}

	}

	private static void freeResource(InputStream is, InputStreamReader isr, BufferedReader br) {
		try {
			if (is != null)
				is.close();
			if (isr != null)
				isr.close();
			if (br != null)
				br.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	/**
	 * 获得CPU使用率.
	 * 
	 * @return 返回cpu使用率
	 * @author GuoHuang
	 */
	private double getCpuRatioForWindows() {
		try {
			String procCmd = System.getenv("windir") + "\\system32\\wbem\\wmic.exe process get "
					+ "Caption,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
			// 取进程信息
			long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
			Thread.sleep(CPUTIME);
			long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
			if (c0 != null && c1 != null) {
				long idletime = c1[0] - c0[0];
				long busytime = c1[1] - c0[1];
				return Double.valueOf(PERCENT * (busytime) / (busytime + idletime)).doubleValue();
			} else {
				return 0.0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0.0;
		}
	}

	/**
	 * 
	 * 读取CPU信息.
	 * 
	 * @param proc
	 * @return
	 * @author GuoHuang
	 */
	private long[] readCpu(final Process proc) {
		long[] retn = new long[2];
		try {
			proc.getOutputStream().close();
			InputStreamReader ir = new InputStreamReader(proc.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line = input.readLine();
			if (line == null || line.length() < FAULTLENGTH) {
				return null;
			}
			int capidx = line.indexOf("Caption");
//			int cmdidx = line.indexOf("CommandLine");
			int kmtidx = line.indexOf("KernelModeTime");
			int rocidx = line.indexOf("ReadOperationCount");
			int umtidx = line.indexOf("UserModeTime");
			int wocidx = line.indexOf("WriteOperationCount");
//			File file=new File("E:/a.txt");
//			file.createNewFile();
//			FileWriter fw=new FileWriter(file);
//			fw.write(line+"\n");
			
			long idletime = 0;
			long kneltime = 0;
			long usertime = 0;
			while ((line = input.readLine()) != null) {
//				fw.write(line+"\n");
				if (line.length() < wocidx) {
					continue;
				}
				// 字段出现顺序：Caption,(CommandLine,)KernelModeTime,ReadOperationCount,
				// ThreadCount,UserModeTime,WriteOperation
				String caption = line.substring(capidx, kmtidx - 1).trim();
				if (caption.toLowerCase().contains("wmic.exe")) {
					continue;
				}
				// log.info("line="+line);
				String s1 = line.substring(kmtidx, rocidx - 1).trim();
				String s2 = line.substring(umtidx, wocidx - 1).trim();
				if (caption.equals("System Idle Process") || caption.equals("System")) {
					idletime += Long.valueOf(s1).longValue();
					idletime += Long.valueOf(s2).longValue();
					continue;
				}
				try {
					kneltime += Long.valueOf(s1).longValue();
					usertime += Long.valueOf(s2).longValue();
				} catch (Exception e) {
//					fw.write("++++++++++++++++++++");
//					System.out.println(line);
//					System.out.println("Caption:"+line.substring(capidx , kmtidx- 1)+"}");
//					System.out.println("KernelModeTime:"+line.substring(kmtidx , rocidx- 1)+"}");
//					System.out.println("ReadOperationCount+ThreadCount:"+line.substring(rocidx , umtidx- 1)+"}");
//					System.out.println("UserModeTime:"+line.substring(umtidx , wocidx- 1)+"}");
//					System.out.println("WriteOperationCount:"+line.substring(wocidx)+"}");
					e.printStackTrace();
				}
			}
//			fw.close();
			retn[0] = idletime;
			retn[1] = kneltime + usertime;
			return retn;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				proc.getInputStream().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String substring(String src, int start_idx, int end_idx) {
		byte[] b = src.getBytes();
		String tgt = "";
		for (int i = start_idx; i <= end_idx; i++) {
			tgt += (char) b[i];
		}
		return tgt;
	}
	
	/**
	 * 测试方法.
	 * 
	 * @param args
	 * @throws Exception
	 * @author GuoHuang
	 */
	public static void main(String[] args) throws Exception {
		PerfMonitor service = new PerfMonitor();
		System.out.println(service.getCpuRatio());
//		System.out.println(service.getUsedMemory());
	}


}
