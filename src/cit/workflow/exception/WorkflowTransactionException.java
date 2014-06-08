/*
 * Created on 2004-10-20
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.exception;

import java.sql.SQLException;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WorkflowTransactionException extends SQLException {
	
	public WorkflowTransactionException() {
		super("Workflow Transaction failed!");
	}
	
	public WorkflowTransactionException(String msg) {
		super(msg);
	}
}
