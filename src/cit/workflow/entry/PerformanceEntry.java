/*
 * Created on 2004-10-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.entry;

import net.jini.entry.AbstractEntry;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PerformanceEntry extends AbstractEntry {
	
	public Integer processorNum;
	
	public Long memorySum;
	
	public Integer cpuUsage;
	
	public Integer memoryUsage;
	
	/** max concurrent users */
	public Integer maxConcurrentUsers;

	public PerformanceEntry() {

	}
	
	public PerformanceEntry(int maxUsers) {
		maxConcurrentUsers = new Integer(maxUsers);
		processorNum = new Integer(1);
		memorySum = new Long(0);
		cpuUsage = new Integer(0);
		memoryUsage = new Integer(0);
	}
}
