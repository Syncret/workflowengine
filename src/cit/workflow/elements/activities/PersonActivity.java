/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.sql.Connection;

import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PersonActivity extends NodeActivity {

	public PersonActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	/* (non-Javadoc)
	 * @see cit.workflow.elements.activities.Executable#execute()
	 */
	public void execute() throws Exception {
		//servercomment System.out.println("ACTIVITY ACTION----PersonActivity EXECUTED!");
	}
}
