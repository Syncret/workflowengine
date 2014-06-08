package cit.workflow.graph.model.element;


import java.io.*;
import java.util.*;

public class LinkFlow
    implements Serializable {
  public LinkFlow() {
    this.pointArray=new ArrayList();
    this.xyposition = new double[4][2];
    xyposition[0][0] = 10;
    xyposition[0][1] = 10;
    xyposition[1][0] = 10;
    xyposition[1][0] = 10;
    this.parentID = -1;
  }

  public int getFromObjectType() {
    return fromObjectType;
  }

  public void setFromObjectType(int fromObjectType) {
    this.fromObjectType = fromObjectType;
  }

  public int getLinkID() {
    return linkID;
  }

  public void setLinkID(int linkID) {
    this.linkID = linkID;
  }

  public int getLinkType() {
    return linkType;
  }

  public void setLinkType(int linkType) {
    this.linkType = linkType;
  }

  public int getFromObjectID() {
    return fromObjectID;
  }

  public void setFromObjectID(int fromObjectID) {
    this.fromObjectID = fromObjectID;
  }

  public int getToObjectID() {
    return toObjectID;
  }

  public void setToObjectID(int toObjectID) {
    this.toObjectID = toObjectID;
  }

  public int getToObjectType() {
    return toObjectType;
  }

  public void setToObjectType(int toObjectType) {
    this.toObjectType = toObjectType;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public double[][] getXyposition() {
    return xyposition;
  }

  public void setXyposition(double[][] xyposition) {
    this.xyposition = xyposition;
  }

  private int fromObjectType;

  private int linkID;

  private int linkType;

  private int fromObjectID;

  private int toObjectID;

  private int toObjectType;

  private int lineNumber = 1;

  private double[][] xyposition;

  private int parentID;

  private ArrayList pointArray;

  public String toString() {
    return "";
  }

  public int getParentID() {
    return parentID;
  }

  public ArrayList getPointArray() {
    return pointArray;
  }

  public void setParentID(int parentID) {
    this.parentID = parentID;
  }

  public void setPointArray(ArrayList pointArray) {
    this.pointArray = pointArray;
  }
}
