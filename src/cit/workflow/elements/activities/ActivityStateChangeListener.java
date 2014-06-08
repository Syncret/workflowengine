/*
 * Created on 2004-11-3
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements.activities;

import java.util.EventListener;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ActivityStateChangeListener extends EventListener {
	
	public void beforeActivityStateChanged(ActivityStateChangeEvent event) throws Exception;
	
	public void afterActivityStateChanged(ActivityStateChangeEvent event) throws Exception;
	
}