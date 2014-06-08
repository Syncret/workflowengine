package cit.workflow.elements.applications;

import java.sql.Connection;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

//import cn.edu.sjtu.sgdai.core.metadata.MetaDataManager;

import loid.adapters.UnifiedQueryAdapter;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLMergence;
import loid.adapters.UnifiedQueryAdapter;
/**
 * 负责执行调用并且将结果进行保存等操作，还未实现对结果的保存
 * @author cnelite
 *
 */

public class UnifiedQueryInvoke /*extends DBUtility implements ApplicationInvoke*/ {

	/*private int applicationID;
	private String sqlTemplate = "";
	private MetaDataManager metadataManager = MetaDataManager.getInstance();
	private XMLMergence mergeResult = new XMLMergence();
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	//private UnifiedQueryAdapter adapter = new UnifiedQueryAdapter();

	public UnifiedQueryInvoke(Connection conn, int applicationID) {
		super(conn);
		this.applicationID = applicationID;
		try {
			metadataManager.flushCache();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	*//**
	 * @param inputXML, String - 为调用服务所需要的输入参数信息。
	 * 可以改进的地方：根据下面所示的Input Schema，它对每个参数都提供了他的名字和属性显得数据量比较大。
	 * 如果前提是在写SQL语句模板的时候已经对参数的类型进行的表示如字符类型带有‘’（‘hello’）等，那么将
	 * 可以省去部分信息只提供其值即可。如：
	 * 目前使用的是以下方式
	 * inputXML：
	 * <INPUT>
	 * 	 <SQL>
	 * 	   <PARAMETER></PARAMETER>
	 *     <PARAMETER></PARAMETER>
	 *     ......
	 *   </SQL>
	 *   <SQL>
	 *     ......
	 *   </SQL>
	 *   ......
	 * </INPUT>
	 *********************
	 * 
	 * <INPUT>
	 * 	 <SQL>
	 * 	   <PARAMETER>
	 * 	     <NAME></NAME>
	 *       <TYPE></TYPE>
	 *       <VALUE></VALUE>
	 * 	   </PARAMETER>
	 *     ......
	 *   </SQL>
	 *   <SQL>
	 *     ......
	 *   </SQL>
	 *   ......
	 * </INPUT>
	 * 
	 * <OUTPUT>
	 *   <RESULT>
	 *     <SQL></SQL>
	 *     <ITEM>
	 *       <FIELD></FIELD>
	 *       <FIELD></FIELD>
	 *       ......
	 *     </ITEM>
	 *     ......
	 *   </RESULT>
	 *   ......
	 * </OUTPUT>
	 *//*
	public String invoke(String inputXML) throws Exception {
		sql = "SELECT PredefinedSQL FROM UnifiedQueryApplication WHERE ApplicationID=?";
		params = new Object[] {new Integer(applicationID)};
		types = new int[] {Types.INTEGER};
		Map saiMap = (Map) executeQuery(new MapHandler());
		
		if (saiMap == null)
			throw new Exception("There is no Unified Query Application for invocation!");
		
		sqlTemplate = (String)saiMap.get("PredefinedSQL");
		UnifiedQueryAdapter adapter = new UnifiedQueryAdapter(sqlTemplate);
		
		return adapter.invokeUnifiedQuery(inputXML);
	}
	
	*//** 
	 *  <SQL>
	 * 	   <PARAMETER>
	 * 	     <NAME></NAME>
	 *       <TYPE></TYPE>
	 *       <VALUE></VALUE>
	 * 	   </PARAMETER>
	 *     ......
	 *  </SQL>
	 *//*
	private Document getSQL(Element sqlElement) {
		String temp = sqlTemplate;
		String sql  = "";
		int index   = 0;
		List parameterList = sqlElement.getChildren();
		
		if (getParameterNum() != parameterList.size()) {
			//servercomment System.out.println("参数个数不匹配！");
			return null;
		}
		
		Iterator it= parameterList.iterator();
		
		while (it.hasNext()) {
			Element parameterElement = (Element)it.next();
			index = temp.indexOf("?");
			sql += temp.substring(0, index) + parameterElement.getChild("VALUE").getText();
			temp = temp.substring(index + 1);
		}
		
		Element root = new Element("UNIFIEDQUERY");
		root.addContent(new Element("SQL").setText(sql + temp));
		
		Document doc = new Document(root);
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
		format.setIndent("    ");
		XMLOutputter outp = new XMLOutputter(format);
		String strOutputXML = outp.outputString(doc);
		logger.info("Unified Query inputs:\n" + strOutputXML);
		return doc;
	}
	
	private int getParameterNum () {
		int count = 0;
		String templateTemp = sqlTemplate;
		
		if (-1 == templateTemp.indexOf("?")) {
			return 0;
		}
		
		while (-1 !=templateTemp.indexOf("?")) {
			templateTemp = templateTemp.substring(templateTemp.indexOf("?") + 1);
			count += 1;
		}
		return count;
	}
	
	public int getApplicationID() {
		return applicationID;
	}*/
}
