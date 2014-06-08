package test;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import cit.workflow.elements.applications.DynamicInvoker;

public class cxzTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String invokeResult = "initial";
		String serviceName = "TestService";
		String portName = "Test";
		String operationName = "test";
		Vector<String> parameterValues = new Vector<String>();
		parameterValues.add("fg");
		Map outputs = null;
		String location = "http://58.196.146.14:8080/WebService/services/Test?wsdl";
		try {
			DynamicInvoker invoker = new DynamicInvoker(location);
			outputs = invoker.invoke(serviceName, portName,
					 operationName, parameterValues);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (Iterator i = outputs.keySet().iterator(); i.hasNext(); ) {
			String name = (String) i.next();
			Object value = outputs.get(name);
			
			if (value != null) {
				invokeResult = outputs.get(name).toString();
			} else {
				invokeResult = "";					
			}
			//servercomment System.out.println(invokeResult);
		}

		
	}
		
}