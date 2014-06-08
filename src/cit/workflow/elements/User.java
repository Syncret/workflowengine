/*
 * Created on 2004-10-14
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cit.workflow.elements;

/**
 * @author weiwei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class User {

	private int actorType;
	
	private int actorID;

	public User(int actorType, int actorID) {
		this.actorType = actorType;
		this.actorID = actorID;
	}
	
	//sxh add 2007.10
	public User(int actorID) {
		this.actorID = actorID;
	}
	//sxh add 2007.10 end

	/**
	 * @return
	 */
	public int getActorID() {
		return actorID;
	}

	/**
	 * @return
	 */
	public int getActorType() {
		return actorType;
	}

	/**
	 * @param i
	 */
	public void setActorID(int i) {
		actorID = i;
	}

	/**
	 * @param i
	 */
	public void setActorType(int i) {
		actorType = i;
	}

}
