package cit.workflow.elements.applications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import cit.jcloud.cloudservice.StorageService;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLOperation;
import cit.workflow.view.InformationPane;

public class CommonApplicationInvoke extends DBUtility implements ApplicationInvoke {
	
	private int applicationID;

	private String processID;
	
	private Document inputDoc = null;
	
	private Document outputDoc = null;
	
	private XMLOperation xmlOperation = new XMLOperation();

	
	protected final Logger logger = Logger.getLogger(this.getClass());

	public CommonApplicationInvoke(Connection conn, int applicationID,
			String processID) {
		super(conn);
		this.applicationID = applicationID;
		this.processID = processID;
		xmlOperation = new XMLOperation();
	}

	public String invoke(String inputXML) throws Exception {
		sql = "select * from commonapplicationmethod where ApplicationID=?";
		params=new Object[]{applicationID};
		types=new int[]{Types.INTEGER};
		Map resultMap=(Map)executeQuery(new MapHandler());
		int applicationType=(Integer) resultMap.get("ApplicationType");
		String outputXML=null;
		switch (applicationType) {
		case 1:
			outputXML=SaveDataToFile(inputXML);
			break;
		case 2:
			outputXML=ReadDataFromFile(inputXML);
			break;
		case 3:
			outputXML=DeleteFile(inputXML);
			break;
		case 4:
			outputXML=Wait(inputXML);
			break;
		default:
			logger.error("Unsupportted Application Type");
		}
		InformationPane.writeln("<---------- Common Application Excuted -------------->");
		return outputXML;
	}

	private String ReadDataFromFile(String inputXML) {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		String fileName;
		try {
			doc = builder.build(new StringReader(inputXML));
			Element root = doc.getRootElement();
			fileName=root.getChildText("filename");
//			Document dataDoc=new Document();
//			dataDoc.setRootElement(root.getChild("data"));
//			dataXML=new XMLOperation().toString(dataDoc);
			File file=new File(".\\"+fileName);
			if(!file.exists()){
				logger.error("File not exsit");
				InformationPane.writeln("File not exsit");
				return null;
			}
			Document dstdoc=builder.build(file);
			logger.info("File Readed");
			InformationPane.writeln("File Readed");
			Element dstroot=dstdoc.getRootElement();
			dstroot.setName("OUTPUT");
			dstroot.getChild("data").setName("RETURN");
			return new XMLOutputter().outputString(dstdoc);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String DeleteFile(String inputXML) {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		String fileName;
		String dataXML;
		try {
			doc = builder.build(new StringReader(inputXML));
			Element root = doc.getRootElement();
			fileName=root.getChildText("filename");
			// Document dataDoc=new Document();
			// dataDoc.setRootElement(root.getChild("data"));
			// dataXML=new XMLOperation().toString(dataDoc);
			File file = new File(".\\" + fileName);
			if (!file.exists())
				logger.warn("File not exsit");
			if (file.delete()) {
				logger.info("File deleted: " + fileName);
				InformationPane.writeln("File deleted: " + fileName);
			} else{
				logger.warn("File delete Failed");
				InformationPane.writeln("File delete Failed");
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String Wait(String inputXML) {
		//simulate redundent computation
//		double sum=0;
//		for(int i=1;i<100000;i++){
//			sum+=1/(2*i)-1/(2*i+1);
//		}
		//redundent compute over
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		String second;
		try {
			doc = builder.build(new StringReader(inputXML));
			Element root = doc.getRootElement();
			second=root.getChildText("second");
			// Document dataDoc=new Document();
			// dataDoc.setRootElement(root.getChild("data"));
			// dataXML=new XMLOperation().toString(dataDoc);
			int waittime=0;
			try{
				waittime=Integer.parseInt(second);
			}
			catch(NumberFormatException e){
				logger.error("APP:WAIT: cannot convert "+second+" to int");
			}
			logger.info("Wait "+waittime+" second");
			InformationPane.writeln("Wait "+waittime+" second");
			Thread.sleep(waittime*1000);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String SaveDataToFile(String inputXML) {

		SAXBuilder builder = new SAXBuilder();
		Document doc;
		String fileName;
		String dataXML;
		try {
			doc = builder.build(new StringReader(inputXML));
			Element root = doc.getRootElement();
			fileName=root.getChildText("filename");
//			Document dataDoc=new Document();
//			dataDoc.setRootElement(root.getChild("data"));
//			dataXML=new XMLOperation().toString(dataDoc);
//			dataXML=root.getChild("data").getChildText("VALUE");
			root.removeContent(root.getChild("filename"));
			File file=new File(".\\"+fileName);
			file.createNewFile();
//			FileWriter filewriter=new FileWriter(file);
//			filewriter.write(new XMLOutputter().outputString(doc));
//			filewriter.close();
			
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
			out.write(new XMLOutputter().outputString(doc));
			out.close();
			logger.info("File written");
			InformationPane.writeln("File written: "+fileName);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
