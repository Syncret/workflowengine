/*
 * Created on 2004-11-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.utils;

import cit.workflow.entry.PerformanceEntry;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SystemInformation {
	
	public static native double getCPUUsage();
	
	public static native double getMemoryUsage();
	
	public static native long getAvailableMemory();
	
	public static native PerformanceEntry formPerformanceEntry();
	
	public static PerformanceEntry getPerformanceEntry(int processorNum, long memorySum, int cpuUsage, int memoryUsage) {
		PerformanceEntry entry = new PerformanceEntry(2);
		entry.processorNum = new Integer(processorNum);
		entry.memorySum = new Long(memorySum);
		entry.cpuUsage = new Integer(cpuUsage);
		entry.memoryUsage = new Integer(memoryUsage);
		return entry;
	}

	static
	{
        try
        {
            System.loadLibrary("SystemEntry");
        } catch (UnsatisfiedLinkError e) {
        	//servercomment System.out.println ("native lib 'SystemEntry' not found in 'java.library.path': " + System.getProperty ("java.library.path"));
        	throw e;
		}
	}
}
