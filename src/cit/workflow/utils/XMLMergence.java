/**
 * @(#)XMLMergence.java     1.0 2006/12/30
 * 
 * Copyright (c) 2006-2007 Shanghai Jiao Tong University.
 * 
 * This software is the confidential and proprietary information of SJTU.
 * ("Confidential Information").  You shall not disclose such Confidential 
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered intowith SJTU.
 */
package cit.workflow.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * merge two xml files into one xml file, those have same style or Schema 
 * @author vvPsyche
 * @version 1.0 2006/12/30
 */
public class XMLMergence {
	
	public XMLMergence() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * merge two xml files into one xml String
	 * @param strMainXML String : file name
	 * @param strViceXML String : file name
	 * @return
	 */
	public String merge(String strMainXMLPath, String strViceXMLPath){
		String strXML = "";
		FileInputStream fiMain = null;
		FileInputStream fiVice = null;
		
		try{
			SAXBuilder sb = new SAXBuilder();
			
			fiMain = new FileInputStream(strMainXMLPath);
			Document docMain = sb.build(fiMain);
			
			fiVice = new FileInputStream(strViceXMLPath);
			Document docVice = sb.build(fiVice);
			
			strXML = merge(docMain, docVice);
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		return strXML;
	}
	
	/**
	 * merge two xml files into one xml String
	 * @param strMainXML String : XML String
	 * @param strViceXML String : XML String
	 * @return String : merged XML String
	 */
	public String mergeString(String strMainXML, String strViceXML){
		String strXML = "";
		
		try{
			SAXBuilder sb = new SAXBuilder();
			
			Reader inMain = new StringReader(strMainXML);
			Document docMain = sb.build(inMain);
			
			Reader inVice = new StringReader(strViceXML);
			Document docVice = sb.build(inVice);
			
			strXML = merge(docMain, docVice);
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		return strXML;
	}
	
	/**
	 * XML String can be read and use SAXBuilder.build into Document
	 * @param docMain Document : XML Document
	 * @param docVice Document : XML Document
	 * @return
	 */
	public String merge(Document docMain, Document docVice){
		String strXML = "";
		String strErr = "XML files have NOT been merged";
		Boolean isOver = false;

		Element rootMain = (Element)docMain.getRootElement();
		Element rootVice = (Element)docVice.getRootElement();
		
		List listItems = rootVice.getChildren();
		int itemNumber = listItems.size();
		
		for(int i = 0; i < itemNumber; i++){
			Element addedItem = (Element)listItems.get(i);
			isOver = duplicate(docMain, rootMain, addedItem);
		}
		
		strXML = writeTo(docMain);

		if(isOver){
			//servercomment System.out.println("XML files have been merged.");
			return strXML;
		}else{
			//servercomment System.out.println ("XML files have NOT been merged.");
			return strErr;
		}
	}
	
	/**
	 * add nodes into main xml Document uses recursion
	 * @param doc_dup
	 * @param father
	 * @param son
	 * @return
	 */
	public Boolean duplicate(Document doc_dup, Element father, Element son){
		Boolean isdone = false;
		String sonName = son.getName();
		Element subItem = new Element(sonName);
		List listAttribute = (List)new ArrayList();
		
		// copy node's attribute
		listAttribute = son.getAttributes();
		int intCount = listAttribute.size();
		
		if( intCount > 0){
			for(int i = 0; i < intCount; i++){
				Attribute attribute = (Attribute)listAttribute.get(i);
				subItem.setAttribute((Attribute)attribute.clone());
			}
		}
		
		// copy node's value
		String strText = son.getText();
		
		if(strText != null && strText.length() > 0){
			subItem.setText(strText);
		}
		
		father.addContent(subItem);
		
		// copy sub nodes
		List sub_Elements = son.getChildren();
		int sub_Element_Number = sub_Elements.size();
		
		if(sub_Element_Number < 1){
			isdone = true;
		}else{
			for(int i = 0; i < sub_Element_Number; i++){
				Element sub_Element = (Element)sub_Elements.get(i);
				isdone = duplicate(doc_dup, subItem, sub_Element);
			}
		}
		
		return isdone;
	}
	
	/**
	 * write Document into file or String
	 * @param doc
	 * @return
	 */
	private String writeTo(Document doc){
		String strXML = "";
		Format format = Format.getCompactFormat();
		format.setEncoding("GBK");
		format.setIndent("    ");
		
		try{
			XMLOutputter outp = new XMLOutputter(format);
			strXML = outp.outputString(doc);
			outp.output(doc, new FileOutputStream("d:\\merge.xml"));
		}catch(IOException e){
			System.err.println(e.getMessage());
		}

		return strXML;
	}
}
