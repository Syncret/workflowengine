package cit.workflow.graph.util;

public class GraphConst {
	/*********************************ACTIVITYTYPE*************************************************/
	  public final static int START_ACTIVITY_TYPE = 1;
	  public final static int END_ACTIVITY_TYPE = 2;
	  public final static int GENERAL_ACTIVITY_TYPE = 3;
	/********************************LOGICNODETYPE************************************************/
	  public final static int AND_LOGIC_TYPE = 1;
	  public final static int OR_LOGIC_TYPE = 2;
	/********************************LINKTYPE****************************************************/
	  public final static int CONTROLFLOW_LINK_TYPE = 1;
	  public final static int DATAFLOW_LINK_TYPE = 2;
	/*******************************ACTIVITYIMPLEMENTATION***************************************/
	  //person Activity
	  public final static int ACTIVITY_PERSON = 1;
	  public final static int ACTIVITY_APPLICATION = 2;
	  public final static int ACTIVITY_AGENT = 3;
	  public final static int ACTIVITY_SCHEDULE = 5;
	  public final static int ACTIVITY_SET_VALUE = 6;
	  public final static int ACTIVITY_TRANSFORM_XML = 7;
	  public final static int ACTIVITY_SUBWORKFLOW = 8;
	  public final static int ACTIVITY_COMPOSITE = 9;
	  public final static int ACTIVITY_COMMANDLINE = 10;
	  /*******************************************************************************************/
	  public final static String IMAGE_RESOURCE_PATH = "cit/workflow/graph/util/images/";
	  /******************************************NODETYPE*****************************************/
	  public final static int ACTIVITY_OBJECT_TYPE = 1;
	  public final static int LOGICNODE_OBJECT_TYPE = 2;
	  /**************************************************ACTIVITYSTATE****************************/
	  public final static String ACTIVITY_STATE_RUNNING = "Running";
	  public final static String ACTIVITY_STATE_COMPLETED = "Completed";
	  public final static String ACTIVITY_STATE_READY = "Ready";
	  public final static String ACTIVITY_STATE_WAITING = "Waiting";
	  /**************************************************Draw TYPE********************************/
	  public final static int DRAW_WORKFLOWTYPE = 1;
	  public final static int DRAW_PROCESSTYPE = 2;
}
