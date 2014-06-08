package cit.workflow.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;



public class PerfData{
	private PerfMonitor monitor;
	
	private LinkedList<Object[]> cpuPerfList;
	private LinkedList<Object[]> memoryPerfList;
//	private TimeSeries cpuTimeSeries;
//	private TimeSeries memoryTimeSeries;
	private boolean run=true;
	private int size=100;
	private long inteval=10000;
	private InfoColleThread thread;
	
	
	
	private void add2list(Object[] data,LinkedList<Object[]> list){
		if(list.size()>=size)list.removeFirst();
		list.add(data);
		return;
	}
	
	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
		if(run&&(thread==null||!thread.isAlive())){
			thread=new InfoColleThread();
			thread.setDaemon(true);
			thread.start();
		}
	}

	public long getInteval() {
		return inteval;
	}

	public void setInteval(long inteval) {
		this.inteval = inteval;
	}

	public PerfMonitor getMonitor() {
		return monitor;
	}
	
//	public TimeSeries getCpuTimeSeries() {
//		return cpuTimeSeries;
//	}
//
//	public TimeSeries getMemoryTimeSeries() {
//		return memoryTimeSeries;
//	}
	
	public LinkedList<Object[]> getCpuPerfList() {
		return cpuPerfList;
	}

	public LinkedList<Object[]> getMemoryPerfList() {
		return memoryPerfList;
	}
	
	public long getTotalPhysicalMemory(){
		return monitor.getTotalPhysicalMemory();
	}
	

	public  PerfData(){
		monitor=new PerfMonitor();
//		cpuTimeSeries=new TimeSeries("CPU Ratio");
//		memoryTimeSeries=new TimeSeries("Memory Usage");
		cpuPerfList=new LinkedList<Object[]>();
		memoryPerfList=new LinkedList<Object[]>();
	}
	
	
	private class InfoColleThread extends Thread{
		@Override
		public void run() {
			while (run) {
				// Second ns = new Second();
				// cpuTimeSeries.add(ns, monitor.getCpuRatio());
				// memoryTimeSeries.add(ns, monitor.getMemoryUsage());
				long now = System.currentTimeMillis();
				Object[] memPair = { now, monitor.getMemoryRatio() };
				Object[] cpuPair = { now, monitor.getCpuRatio() };
				add2list(memPair, memoryPerfList);
				add2list(cpuPair, cpuPerfList);

				try {
					Thread.sleep(now + inteval - System.currentTimeMillis());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
