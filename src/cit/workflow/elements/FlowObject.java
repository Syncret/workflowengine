package cit.workflow.elements;

import java.io.Serializable;

public class FlowObject  implements Serializable{
	/**
	 * objectType: 3---XML对象; 4---文档对象
	 */
	private int objectType;
	private int objectID;
	private String objectName;
	private String objectLocation;
	private String description;
	private String objectXML;
	
	public void setObjectType(int objectType){
		this.objectType = objectType;
	}
	public void setObjectID(int objectID){
		this.objectID = objectID;
	}
	public void setObjectName(String objectName){
		this.objectName = objectName;
	}
	public void setObjectLocation(String objectLocation){
		this.objectLocation = objectLocation;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public int getObjectID(){
		return this.objectID;
	}
	public String getObjectName(){
		return this.objectName;
	}
	public String getObjectLocation(){
		return this.objectLocation;
	}
	public String getDescription(){
		return this.description;
	}
	public int getObjectType(){
		return this.objectType;
	}
	public String getObjectXML(){
		return this.objectXML;
	}
	public void setObjectXML(String objectXML){
		this.objectXML = objectXML;
	}
}
