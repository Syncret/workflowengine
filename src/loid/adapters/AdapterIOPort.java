/*
 * Created on 2004-11-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AdapterIOPort {
	private String name;
	private String type;
	private int    objId;
	private Object obj;
	public AdapterIOPort(){}
	public String getName(){
		return(this.name);
	}
	public void setName(String name){
		this.name=name;
	}
	public String getType(){
		return(this.type);
	}
	public void setType(String type){
		this.type=type;
	}
	public int getObjId(){
		return(this.objId);
	}
	public void setObjId(int objId){
		this.objId=objId;
	}	
	public Object getObj(){
		return(this.obj);
	}
	public void setObj(Object obj){
		this.obj=obj;
	}		
}
