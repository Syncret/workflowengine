package cit.workflow.elements.applications;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;












import cit.jcloud.cloudservice.ComputeService;
import cit.jcloud.cloudservice.StorageService;
import cit.workflow.utils.DBUtility;
import cit.workflow.utils.XMLOperation;
import cit.workflow.view.InformationPane;

public class CloudServiceInvoke extends DBUtility implements ApplicationInvoke{

	private int applicationID;

	private String processID;
	
	private Document inputDoc = null;
	
	private Document outputDoc = null;
	
	private XMLOperation xmlOperation = new XMLOperation();

	
	protected final Logger logger = Logger.getLogger(this.getClass());

	public CloudServiceInvoke(Connection conn, int applicationID, String processID) {
		super(conn);
		this.applicationID = applicationID;
		this.processID = processID;
		xmlOperation = new XMLOperation();
	}
	
	public CloudServiceInvoke(Connection conn, int applicationID) {
		super(conn);
		this.applicationID = applicationID;
	}
	

	public String invoke(String inputXML) throws Exception {
//		sql = "select ApplicationName from SystemApplicationInformation where ApplicationID=?";
//		params = new Object[] { applicationID };
//		types = new int[] {Types.VARCHAR};
//		Map resultMap = (Map) executeQuery(new MapHandler());
//		String applicationName=(String)resultMap.get("ApplicationName");
//		if(!applicationName.contains(":")){
//			System.err.println("invoke Cloud Service error: Failed to get provider");
//		}
//		String[] strs=applicationName.split(":");
//		provider=strs[0];
//		String serviceTypeName=strs[1];
		logger.info("<----------------------Invoking Cloud Service--------------------->");
		InformationPane.writeln("<----------------------Invoking Cloud Service--------------------->");
		sql = "select * from cloudservicemethod where ApplicationID=?";
		params=new Object[]{applicationID};
		types=new int[]{Types.INTEGER};
		Map resultMap=(Map)executeQuery(new MapHandler());
		int serviceType=(Integer) resultMap.get("CloudServiceType");
		String outputXML=null;
		switch (serviceType) {
		case 1:
			outputXML=UploadFiletoCloud(inputXML);
			break;
		case 2:
			outputXML=DeleteFileFromCloud(inputXML);
			break;
		case 3:
			outputXML=DownloadFileFromCloud(inputXML);
			break;
		case 4:
			outputXML=CreateVirtualMachine(inputXML);
			break;
		case 5:
			outputXML=DeleteVirtualMachine(inputXML);
			break;
		default:
			logger.error("Unsupportted Cloud Service Type");
		}
		InformationPane.writeln("<----------------------Cloud Service Complete--------------------->");
		return outputXML;
	}
	
	private String UploadFiletoCloud(String inputXML) throws JDOMException, IOException {
		String provider;
		String identity;
		String credential;
		String folderName;
		String fileName;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(inputXML));
		Element root = doc.getRootElement();
		provider = root.getChildText("provider");
		identity = root.getChildText("identity");
		credential = root.getChildText("credential");
		folderName = root.getChildText("foldername");
		fileName = root.getChildText("filename");		
		StorageService service = new StorageService(0, provider, null, null, null);
		service.ServiceInit(identity, credential);
		File file = new File(".\\" + fileName);
		service.UploadFile(folderName, fileName, file);
		logger.info("Upload Complete");
		InformationPane.writeln("Upload Complete: "+fileName);
		return null;
	}

	private String DeleteFileFromCloud(String inputXML) throws JDOMException, IOException {
		String provider;
		String identity;
		String credential;
		String folderName;
		String fileName;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(inputXML));
		Element root = doc.getRootElement();
		provider = root.getChildText("provider");
		identity = root.getChildText("identity");
		credential = root.getChildText("credential");
		folderName = root.getChildText("foldername");
		fileName = root.getChildText("filename");		
		StorageService service = new StorageService(0, provider, null, null, null);
		service.ServiceInit(identity, credential);
		File file = new File(".\\" + fileName);
		service.DeleteBlob(folderName, fileName);
		logger.info("Delete Complete");
		InformationPane.writeln("Delete Complete: "+fileName);
		return null;
	}
	
	private String DownloadFileFromCloud(String inputXML) throws JDOMException, IOException {
		String provider;
		String identity;
		String credential;
		String folderName;
		String srcFileName;
		String dstFileName;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(inputXML));
		Element root = doc.getRootElement();
		provider = root.getChildText("provider");
		identity = root.getChildText("identity");
		credential = root.getChildText("credential");
		folderName = root.getChildText("foldername");
		srcFileName = root.getChildText("srcfilename");		
		dstFileName=root.getChildText("dstfilename");
		StorageService service = new StorageService(0, provider, null, null, null);
		service.ServiceInit(identity, credential);
		try {
			service.DownloadFile(folderName, srcFileName, dstFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Download Complete");
		InformationPane.writeln("Download : "+dstFileName);
		return null;
	}

	private String DeleteVirtualMachine(String inputXML) throws JDOMException, IOException {
		logger.info("Call delete virtual machine cloud service");
		String provider;
		String identity;
		String credential;
		String groupName;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(inputXML));
		Element root = doc.getRootElement();
		provider = root.getChildText("provider");
		identity = root.getChildText("identity");
		credential = root.getChildText("credential");
		groupName = root.getChildText("groupName");
		ComputeService service=new ComputeService(provider);
		logger.info("Deleting virtual machine on "+provider+" "+groupName);
		InformationPane.writeln("Deleting virtual machine on "+provider+" "+groupName);
		service.removeVirtualMachine(groupName, identity, credential);
		logger.info("Delete Complete");
		InformationPane.writeln("Delete Complete");
		return null;
	}

	private String CreateVirtualMachine(String inputXML) throws JDOMException, IOException {
		logger.info("Call create virtual machine cloud service");
		String provider;
		String identity;
		String credential;
		String groupName;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(new StringReader(inputXML));
		Element root = doc.getRootElement();
		provider = root.getChildText("provider");
		identity = root.getChildText("identity");
		credential = root.getChildText("credential");
		groupName = root.getChildText("groupName");
		ComputeService service=new ComputeService(provider);
		logger.info("Creating virtual machine on "+provider+" "+groupName);
		InformationPane.writeln("Creating virtual machine on "+provider+" "+groupName);
		service.createVirtualMachine(groupName, identity, credential);
		logger.info("Create completed");
		InformationPane.writeln("Create completed");
		return null;
	}

	
	/* use AWS-API
	 * 
	 * public String invoke(String inputXML) throws Exception {
		sql = "select ProcessName from ProcessInformation where ProcessID=?";
		params = new Object[] { processID };
		types = new int[] {Types.VARCHAR};
		Map resultMap = (Map) executeQuery(new MapHandler());
		String processName=(String)resultMap.get("ProcessName");
		
		sql = "select * from awss3method where appID=?";
		params = new Object[] { applicationID };
		types = new int[] {Types.VARCHAR};
		resultMap = (Map) executeQuery(new MapHandler());
		provider=(String)resultMap.get("provider");
		identity=(String)resultMap.get("identity");
		credential=(String)resultMap.get("credential");
		containerName=(String)resultMap.get("containerName");
		
		
		Date currentDate=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String strDate=df.format(currentDate);
		File file=new File(".\\"+processName+"_"+strDate+"_"+processID+".txt");
		file.createNewFile();
		file.deleteOnExit();
		FileWriter filewriter=new FileWriter(file);
		filewriter.write("ProcessName: "+processName+'\n');
		filewriter.write("ProcessID: "+processID+'\n');
		filewriter.write("Date: "+currentDate+"\n\n");

		filewriter.write(inputXML);
		filewriter.close();
		
		// add to AWS S3
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
					identity, credential);
			AmazonS3 S3 = new AmazonS3Client(awsCredentials);
			S3.putObject(containerName, file.getName(), file);
		} catch (AmazonServiceException ase) {
			System.out
					.println("Caught an AmazonServiceException, which means your request made it "
							+ "to Amazon S3, but was rejected with an error response for some reason.");
			//servercomment System.out.println("Error Message:    " + ase.getMessage());
			//servercomment System.out.println("HTTP Status Code: " + ase.getStatusCode());
			//servercomment System.out.println("AWS Error Code:   " + ase.getErrorCode());
			//servercomment System.out.println("Error Type:       " + ase.getErrorType());
			//servercomment System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
					.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with S3, "
							+ "such as not being able to access the network.");
			//servercomment System.out.println("Error Message: " + ace.getMessage());
		}
		
		logger.info("upload to S3");

		return inputXML;
	}*/

}
