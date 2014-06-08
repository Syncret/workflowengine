package loid.adapters;

/**
 * @(#)UnifiedQueryAdapter.java     1.0 2007/03/25
 * 
 * Copyright (c) 2006-2007 Shanghai Jiao Tong University.
 * 
 * This software is the confidential and proprietary information of SJTU.
 * ("Confidential Information").  You shall not disclose such Confidential 
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered intowith SJTU.
 */
//package cit.workflow.elements.adapters;

import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import loid.adapters.Adapter;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cit.workflow.utils.XMLMergence;
//import cn.edu.sjtu.sgdai.core.dataaccess.ClassAccessManager;
//import cn.edu.sjtu.sgdai.core.metadata.DAIJoinDataRow;
//import cn.edu.sjtu.sgdai.core.metadata.DAIPropertyInfo;

/**
 * 实现对具体方法的调用，不涉及从数据库获取调用信息的操作，仅使用调用信息执行调用。
 * @author cnelite
 * 
 * 命名上的区分<br>
 * 1.具体方法的输入参数信息（InputXML）、输出参数信息（OutputXML）
 * 2.调用方法时需要的所有参数信息不仅包括方法的输入参数同时还包括方法调用时需要的一些信息<br>
 *   调用信息(InvokeXML)、返回结果信息(ReturnXML)
 *   
 * invoke.xml
 * <UNIFIEDQUERY>
 * 	<SQL>
 * 		<PARAMETER></PARAMETER>
 * 		..................
 * 		<PARAMETER></PARAMETER>
 *  </SQL>
 *  <SQL>
 *      .......................
 *  </SQL>
 * </UNIFIEDQUERY>
 */
public class UnifiedQueryAdapter {

	/*private String sql = "";
	private String sqlTemplate = "";
	
	ClassAccessManager cam = ClassAccessManager.getInstance();
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	
	public UnifiedQueryAdapter(String strSQLTemplate){
		sqlTemplate = strSQLTemplate;
	}
	
	*//**
	 * 调用统一查询接口执行
	 * @param invokeXML
	 * @return
	 *//*
	public String invokeUnifiedQuery(String invokeXML){
		
		logger.info("调用统一查询接口执行......");
		
		XMLMergence mergeResult = new XMLMergence();
		Element root = new Element("OUTPUT");
		Document docMain = new Document(root);
		
		try{
			Reader reader = new StringReader(invokeXML);
			SAXBuilder sb = new SAXBuilder();
			Document doc  = sb.build(reader);
			List sqlList = doc.getRootElement().getChildren();
			Iterator it = sqlList.iterator();
			
			while(it.hasNext()) {
				Element sqlElement = (Element)it.next();
				sql = fillSQL(sqlTemplate, sqlElement);
				List<DAIJoinDataRow> resultRow = cam.unifiedQuery(sql);
				Document docVice = buildXML(resultRow);
				mergeResult.merge(docMain, docVice);
			}
		}catch(Exception e){
			logger.error(e);
		}
		
		XMLOutputter outp = new XMLOutputter();
		String strOutputXML = outp.outputString(docMain);
		logger.info("合并后的结果为：" + strOutputXML);
		return strOutputXML;
	}
	
	
	*//**
	 * 将unifiedQuery()的结果组装成XML !!!暂时不考虑分类，将所有SQL语句的查询结果放一块儿。
	 * @param listDAIJoinDataRow
	 * @return
	 * 
	 * 目前
	 * <OUTPUT>
	 *   <ITEM>
	 *     <FIELD></FIELD>
	 *     <FIELD></FIELD>
	 *     ......
	 *   </ITEM>
	 *   ......
	 * </OUTPUT>
	 * 原本
	 * <RESULT>
	 *   <SQL> </SQL>
	 *   <ITEM>
	 *     <FIELD></FIELD>
	 *     <FIELD></FIELD>
	 *     ......
	 *   </ITEM>
	 *   ......
	 * </RESULT>
	 *//*
	private Document buildXML(List<DAIJoinDataRow> result){
		//Element upRoot = new Element("OUTPUT");
		Element root  = new Element("OUTPUT");
		//upRoot.addContent(root);
		Document doc  = new Document(root);
		
		String[] propertyNames = null;

		String strOutputXML = "";
		
		//root.addContent(new Element ("SQL").setText(sql));
		
		if(propertyNames == null && !result.isEmpty()){
			propertyNames = result.get(0).getPropertyNames();
		}

		for (DAIJoinDataRow row:result){
			Element itemElement = new Element("ITEM");
			root.addContent(itemElement);
			
			for(String name:propertyNames){
				DAIPropertyInfo tempInfo = row.getProperty(name).getPropertyInfo();
				
				if(!tempInfo.isObjectProperty()){
					Element nodeElement = new Element(name);
					nodeElement.setText((String.valueOf(row.getPropertyValue(name))));
					logger.info(name + ":" + String.valueOf(row.getPropertyValue(name)));
					itemElement.addContent(nodeElement);
				}
			}
		}
		
		try{
			Format format = Format.getCompactFormat();
			format.setEncoding("UTF-8");
			format.setIndent("    ");
			XMLOutputter outp = new XMLOutputter(format);
			strOutputXML = outp.outputString(doc);
			logger.info("Unified Query outputs：\n" + strOutputXML);
		}catch(Exception e){
			logger.error(e);
		}
		
		logger.info("查询结果输出成功！");
		return doc;
	}
	
	*//**
	 * 根据SQL模板和给的参数来填充SQL模板
	 * @param sqlTemplate
	 * @param sqlElement
	 * @return
	 *//*
	public String fillSQL(String sqlTemplate, Element sqlElement){
		String output = null;
		List paraList = sqlElement.getChildren("PARAMETER");
		int num = paraList.size();
		
		for(int i = 0; i < num; i++){
			output = sql.replaceFirst("\\?", (String)((Element)paraList.get(i)).getText());
		}
		
		logger.info(output);
		return output;
	}*/
}
