/*
 * Created on 2004-10-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.engine;

import java.rmi.Remote;

import net.jini.admin.JoinAdmin;

import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.admin.StorageLocationAdmin;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WorkflowServiceAdmin extends Remote, DestroyAdmin, StorageLocationAdmin, JoinAdmin, LifeCycleAdmin {
	
}
