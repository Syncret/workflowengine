package cit.workflow.elements.activities;

import java.sql.Connection;

import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Process;

public class CouldServiceActivity extends NodeActivity {
	public CouldServiceActivity(Connection conn, Process process, int activityID,
			ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
}
