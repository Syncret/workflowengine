/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import cit.workflow.Constants;
import cit.workflow.eca.ProcessManager;
import cit.workflow.elements.Expression;
import cit.workflow.elements.Process;
import cit.workflow.elements.factories.ElementFactory;
import cit.workflow.elements.variables.ProcessObject;


/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SetValueActivity extends NodeActivity {

	public SetValueActivity(Connection conn, Process process, int activityID, ProcessManager processManager) {
		super(conn, process, activityID, processManager);
	}
	
	public void execute() throws Exception {
		changeState(Constants.ACTIVITY_STATE_READY, Constants.ACTIVITY_STATE_RUNNING);
		/* SetValue start */
		sql = "SELECT * FROM ProcessActivitySetValue WHERE ProcessID=? AND ActivityID=?";
		params = new Object[] {this.getProcess().getProcessID(), new Integer(this.activityID)};
		types = new int[] {Types.VARCHAR, Types.INTEGER};
		List pasvList = (List) executeQuery(new MapListHandler());
		
		if (logger.isDebugEnabled()) 
			logger.debug("Invoke workflow count: " + pasvList.size() + ".");
			
		Iterator iterator = pasvList.iterator();
		while (iterator.hasNext()) {
			Map pasvMap = (Map) iterator.next();
			Expression expression = new Expression(conn, this.getProcess().getProcessID());
			String value = expression.replaceVariables((String) pasvMap.get("Expression"));
			//sxh add 2007.11
			
			value = String.valueOf(expression.computeMathExpression(value));
			//sxh add 2007.11 end
			ProcessObject processObject = ElementFactory.createProcessObject(conn, this.getProcess().getProcessID(), ((Integer) pasvMap.get("ObjectID")).intValue());
			//ProcessObject workflowObject = new ProcessObject(conn, this.getProcess().getProcessID(), ((Integer) pasvMap.get("ObjectID")).intValue());
			processObject.setValue(value);
			//servercomment System.out.println(value);
		}
		
		/* SetValue end */
		changeState(Constants.ACTIVITY_STATE_RUNNING, Constants.ACTIVITY_STATE_COMPLETED);
	}
}
