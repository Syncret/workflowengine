/*
 * Created on 2005-5-9
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package load;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadActivity {
    
    private int workflowID;
    private int activityID;
    private int implementationType;
    private boolean isLoop;
    private double loopPossibility;
    private double loadNum;
    private double loadFactor;
    private List subsequentActivityList;
    private boolean isStart;
    
    /**
     * @param workflowID
     * @param activityID
     */
    public LoadActivity(int workflowID, int activityID, int implementationType) {
        this.workflowID = workflowID;
        this.activityID = activityID;
        this.implementationType = implementationType;
        
        //loadFactor's default value is 1.0
        this.loadFactor = 1.0;
        this.subsequentActivityList = new ArrayList();
        this.isStart = false;
        this.loopPossibility = 0.0;
    }
    
    public void reset() {
    	this.loadNum = 0.0;
    	this.loadFactor = 1.0;
        this.isStart = false;
        this.loopPossibility = 0.0;
    }
    
    /**
     * @return Returns the loopPossibility.
     */
    public double getLoopPossibility() {
        return loopPossibility;
    }
    /**
     * @param loopPossibility The loopPossibility to set.
     */
    public void setLoopPossibility(double loopPossibility) {
        this.loopPossibility = loopPossibility;
    }
    public void addLoadNum(double loadNum) {
        this.loadNum += loadNum;
    }
    
    /**
     * @return Returns the isStart.
     */
    public boolean isStart() {
        return isStart;
    }
    /**
     * @param isStart The isStart to set.
     */
    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }
    /**
     * @return Returns the implementationType.
     */
    public int getImplementationType() {
        return implementationType;
    }
    /**
     * @param implementationType The implementationType to set.
     */
    public void setImplementationType(int implementationType) {
        this.implementationType = implementationType;
    }
    /**
     * @return Returns the activityID.
     */
    public int getActivityID() {
        return activityID;
    }
    /**
     * @param activityID The activityID to set.
     */
    public void setActivityID(int activityID) {
        this.activityID = activityID;
    }
    /**
     * @return Returns the isLoop.
     */
    public boolean isLoop() {
        return isLoop;
    }
    /**
     * @param isLoop The isLoop to set.
     */
    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }
    /**
     * @return Returns the loadFactor.
     */
    public double getLoadFactor() {
        return loadFactor;
    }
    /**
     * @param loadFactor The loadFactor to set.
     */
    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }
    /**
     * @return Returns the loadNum.
     */
    public double getLoadNum() {
        return loadNum;
    }
    /**
     * @param loadNum The loadNum to set.
     */
    public void setLoadNum(double loadNum) {
        this.loadNum = loadNum;
    }
    /**
     * @return Returns the workflowID.
     */
    public int getWorkflowID() {
        return workflowID;
    }
    /**
     * @param workflowID The workflowID to set.
     */
    public void setWorkflowID(int workflowID) {
        this.workflowID = workflowID;
    }
    
    /**
     * @return Returns the subsequentActivityList.
     */
    public List getSubsequentActivityList() {
        return subsequentActivityList;
    }
    /**
     * @param subsequentActivityList The subsequentActivityList to set.
     */
    public void setSubsequentActivityList(List subsequentActivityList) {
        this.subsequentActivityList = subsequentActivityList;
    }
    public double computeLoad() {
        return 0.0;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof LoadActivity) {
            LoadActivity temp = (LoadActivity) obj;
            if (temp.getWorkflowID() == this.getWorkflowID() && temp.getActivityID() == this.getActivityID())
                return true;
        }
        
        return false;
    }
}
