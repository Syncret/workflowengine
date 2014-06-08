/*
 * Created on 2004-10-19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.utils;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TypeConvert {

	public static String CurrentTimeToString() {
		Date temp = new Date();
		return ConvertDateTimeToString(temp.getTime());
	}
	
	//datetime's style is "2001-01-02 12:34:23";
	public static Date ConvertStringToDateTime(String datetime) {
		DateFormat df = DateFormat.getDateTimeInstance();
		Date temp = null;
		try {
			temp = df.parse(datetime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	public static String ConvertDateTimeToString(long millsecond) {
		Date temp = new Date(millsecond);
		return DateFormat.getDateTimeInstance().format(temp);
	}
}
