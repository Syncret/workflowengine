package cit.workflow.graph.model.element;

import java.awt.Point;



public class BaseActivity {
	private int activityID;
	private String activityName;
	private int activityType;
	private int activityImp;
	private String activityState;
	private Point activityPoint;
	private int parentID;
	public BaseActivity()
	{
		activityState = "";
	}
	public int getParentID() {
		return parentID;
	}
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}
	public int getActivityID() {
		return activityID;
	}
	public void setActivityID(int activityID) {
		this.activityID = activityID;
	}
	public int getActivityImp() {
		return activityImp;
	}
	public void setActivityImp(int activityImp) {
		this.activityImp = activityImp;
	}
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public Point getActivityPoint() {
		return activityPoint;
	}
	public void setActivityPoint(Point activityPoint) {
		this.activityPoint = activityPoint;
	}
	public String getActivityState() {
		return activityState;
	}
	public void setActivityState(String activityState) {
		this.activityState = activityState;
	}
	public int getActivityType() {
		return activityType;
	}
	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}
	public String toString()
	{
		return this.activityName;
		//return new Integer(activityID).toString();
		
	}

}
