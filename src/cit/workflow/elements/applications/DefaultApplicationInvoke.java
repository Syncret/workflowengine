/*
 * Created on 2005-3-13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cit.workflow.elements.applications;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DefaultApplicationInvoke implements ApplicationInvoke {

	/* (non-Javadoc)
	 * @see cit.workflow.elements.applications.ApplicationInvoke#invoke(java.lang.String)
	 */
	public String invoke(String inputXML) throws Exception {
		
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			
		}
		
		for (int i = 0; i < 200000000; i++) {
			long multiple = i * i;
		}
		
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			
		}
		
		for (int i = 0; i < 200000000; i++) {
			long multiple = i * i;
		}
		
		return null;
	}
}
