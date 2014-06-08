package cit.workflow.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class RemoteDeploy {
	private String warpath="scripts/deploy_tomcat.xml";
	public RemoteDeploy(){}
	
	public void setWarPath(String path){this.warpath=path;}
	public String getWarPath(){return this.warpath;}
	
	/**
	 * deploy the axis2 with the web service on the remote web server
	 * @param url set the url of the target server
	 */
	public boolean deploy(String url){
		setURL(url+"/manager/text","scripts/deploy_tomcat.xml");
		Runtime rt=Runtime.getRuntime();
		Process p;
		boolean success=false;
		try {
			p=rt.exec("cmd /c start scripts\\deploy.bat");
//			p=rt.exec("cmd /c scripts\\deploy.bat");
			//servercomment System.out.println("calling ant...");
			p.waitFor();
			InputStream fis=p.getInputStream();
			InputStreamReader isr=new InputStreamReader(fis);
			/*
			LineNumberReader input=new LineNumberReader(isr);
			String line;
			while((line=input.readLine())!=null) //servercomment System.out.println(line);
			*/
			BufferedReader br = new BufferedReader(isr);
			// 直到读完为止
			String msg = null;
			while((msg = br.readLine())!=null){ 
				//servercomment System.out.println(msg); 
				if(msg.contains("BUILD SUCCESSFUL")) success=true;
			}
			br.close();
			isr.close();
			fis.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * 修改build.xml文件中的url属性，使ant能够将服务部署到url指定的tomcat服务器
	 * @param url 指定部署的服务器url
	 * @param path 指定build.xml的路径
	 */
	public void setURL(String url, String path){
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(path));
			Attribute attr=(Attribute)document.selectSingleNode("/project/property[@name='url']/@value");
			attr.setValue(url);
			XMLWriter writer = new XMLWriter(new FileWriter(new File(path)));
			writer.write(document);
			writer.close();
			//servercomment System.out.println("Modify url to "+url+" in "+path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		new RemoteDeploy().deploy("http://192.168.1.52:8080");
	}
}
