/*
 * Created on 2005-5-9
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package load;

/**
 * @author weiwei
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkflowEvent {
    
    private int workflowID;
    private int eventID;
    private String eventRepresentation;
    private double possibility;

    /**
     * @return Returns the possibility.
     */
    public double getPossibility() {
        return possibility;
    }
    /**
     * @param possibility The possibility to set.
     */
    public void setPossibility(double possibility) {
        this.possibility = possibility;
    }
    /**
     * @return Returns the eventID.
     */
    public int getEventID() {
        return eventID;
    }
    /**
     * @param eventID The eventID to set.
     */
    public void setEventID(int eventID) {
        this.eventID = eventID;
    }
    /**
     * @return Returns the eventRepresentation.
     */
    public String getEventRepresentation() {
        return eventRepresentation;
    }
    /**
     * @param eventRepresentation The eventRepresentation to set.
     */
    public void setEventRepresentation(String eventRepresentation) {
        this.eventRepresentation = eventRepresentation;
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
}
