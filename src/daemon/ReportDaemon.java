package daemon;

import java.util.Calendar;
import java.util.Map;

import cit.workflow.WorkflowMonitorManager;
import cit.workflow.WorkflowPublicManager;
import cit.workflow.WorkflowServer;

public class ReportDaemon implements Runnable{
	public void run() {
		int curDay;
		boolean hasCreated = false;
		Calendar calendar;
		while (true) {
			calendar = Calendar.getInstance();
			curDay = calendar.get(calendar.DAY_OF_WEEK);
			if (curDay == calendar.MONDAY && !hasCreated) {
				runEngine();
				hasCreated = true;
				System.exit(0);
			} else {
				try {
					//1天：1000 * 60 * 60 * 24 =86400000
					hasCreated = false;
					Thread.sleep(86400000);
				} catch (Exception ex) {
					System.err.println("Sleep has been broken!");
					ex.printStackTrace();
				}
			}
		}
	}
	private void allocateProcess(int workflowID, int staffPersonID, int teacherPersonID) {
		try {
			WorkflowServer ws = WorkflowServer.getInstance();
			String processID = ws.instantiateWorkflow(workflowID, 1, 1, "", 1, 56);
			WorkflowMonitorManager wmm = WorkflowMonitorManager.getInstance();
			Map processInformationMap = wmm.getProcessInformation(processID);
			String processName = (String)processInformationMap.get("processName");
			WorkflowPublicManager wpm = WorkflowPublicManager.getInstance();
			Map personInformationMap = wpm.getPersonInformation(staffPersonID);
			String personName = (String)personInformationMap.get("personName");
			StringBuffer pnBuf = new StringBuffer();
			pnBuf.append(personName);
			pnBuf.append("的");
			pnBuf.append(processName);
			wmm.setProcessName(processID, pnBuf.toString());			
			ws.bindingActivitytoPerson(staffPersonID, processID, 3);
			ws.bindingActivitytoPerson(teacherPersonID, processID, 4);
			ws.bindingActivitytoPerson(staffPersonID, processID, 5);
			ws.startProcess(processID, 1, 56);
			ws.openActivity(processID, 3, staffPersonID);
			//ws.submitActivity(processID, 3, staffPersonID);
			
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private void runEngine() {
		int workflowID = 1;
		int teacherPersonID = 8;
		int[] staffPersonID = {2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16};
		for (int staffIndex = 0; staffIndex < staffPersonID.length; staffIndex++) {
			allocateProcess(workflowID, staffPersonID[staffIndex], teacherPersonID);
		}
		
	}
	public static void main(String[] args) {
		ReportDaemon rd = new ReportDaemon();
		Thread t = new Thread(rd);
		t.start();
	}
}