package cit.workflow.elements.applications;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import cit.jcloud.cloudservice.StorageService;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLOperation;

public class SaveToCloudInvoke extends DBUtility implements ApplicationInvoke{

	private String provider;
	private StorageService service=null;
	private String identity=null;
	private String credential=null;
	private String folderName=null;
	private String fileName=null;
	private String dataXML=null;
	
	public SaveToCloudInvoke(String provider){
		this.provider=provider;
	}
	public SaveToCloudInvoke(){
		
	}
	public String invoke(String inputXML) throws Exception {
		
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(new StringReader(inputXML));
			Element root = doc.getRootElement();
			provider=root.getChildText("provider");
			identity=root.getChildText("identity");
			credential=root.getChildText("credential");
			folderName=root.getChildText("foldername");
			fileName=root.getChildText("filename");
//			Document dataDoc=new Document();
//			dataDoc.setRootElement(root.getChild("data"));
//			dataXML=new XMLOperation().toString(dataDoc);
			dataXML=root.getChild("data").getChildText("VALUE");
			//servercomment System.out.println("data:");
			//servercomment System.out.println(dataXML);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		service=new StorageService(0,provider,null,null,null);
		service.ServiceInit(identity, credential);
		File file=new File(".\\"+fileName+".txt");
		file.createNewFile();
		file.deleteOnExit();
		FileWriter filewriter=new FileWriter(file);
		filewriter.write(dataXML);
		filewriter.close();
		
		service.UploadFile(folderName, fileName, file);

		return null;
	}
}


