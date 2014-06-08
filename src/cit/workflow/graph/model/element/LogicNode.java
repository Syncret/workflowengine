package cit.workflow.graph.model.element;


import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class LogicNode implements Serializable {
	 private int logicNodeID;

	 private int parentID;

	 private int inputType;

	 private int outputType;
	 
	 private Point point;
	
    public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public LogicNode() {
        this.parentID = -1;
    }

    public int getLogicNodeID() {
        return logicNodeID;
    }

    public void setLogicNodeID(int logicNodeID) {
        this.logicNodeID = logicNodeID;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public int getOutputType() {
        return outputType;
    }

    public void setOutputType(int outputType) {
        this.outputType = outputType;
    }
    public String toString()
    {
    	//return new Integer(logicNodeID).toString();
    	return "";
    }
   
}