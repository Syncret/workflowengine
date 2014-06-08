/*
 * Created on 2004-11-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements;

import java.util.EventListener;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ProcessStateChangeListener extends EventListener {
	
	public void beforeProcessStateChanged(ProcessStateChangeEvent event) throws Exception;
	
	public void afterProcessStateChanged(ProcessStateChangeEvent event) throws Exception;
}
