package cit.workflow.graph.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.Port;

import cit.workflow.graph.model.ProcessInfo;
import cit.workflow.graph.model.element.BaseActivity;
import cit.workflow.graph.model.element.LinkFlow;
import cit.workflow.graph.model.element.LogicNode;
import cit.workflow.graph.util.ImageUtil;
import cit.workflow.graph.util.GraphConst;

public class GeneratePic {
	private JGraph workflowGraph;
	private List activityCell;
	private List logicCell;
	private List flagSet;
	private List flowSet;
	private List activitySet;
	private List logicNodeSet;
	
	public GeneratePic()
	{
		workflowGraph = new WorkflowGraph(new DefaultGraphModel());
		GraphLayoutCache view = new GraphLayoutCache(workflowGraph);
	    workflowGraph.setGraphLayoutCache(view);
	    workflowGraph.setGridEnabled(true);
	    workflowGraph.setGridVisible(true);
	    workflowGraph.setMarqueeColor(Color.lightGray);
	    workflowGraph.setGridSize(15);
	    workflowGraph.setPortsVisible(false);
	    workflowGraph.setSizeable(false);
	    workflowGraph.setAntiAliased(true);
	    Border border = BorderFactory.createLineBorder(Color.black);
	    workflowGraph.setBorder(border);
		activityCell = new ArrayList();
		logicCell = new ArrayList();
		flagSet = new ArrayList();
		flowSet = new ArrayList();
	}
	public JGraph getWorkflowGraph() {
		return workflowGraph;
	}
	public void setWorkflowGraph(JGraph workflowGraph) {
		this.workflowGraph = workflowGraph;
	}
	public String drawProcess(String processID)
	{
		InitiateProcess initProcess = new InitiateProcess();
		initProcess.initProcess(processID);
		ProcessInfo process = initProcess.getProcessInfo();
		activitySet = process.getActivitySet();
		logicNodeSet = process.getLogicNodeSet();
		flowSet = process.getFlowSet();
		int maxX = getMaxX();
		int maxY = getMaxY();
		workflowGraph.setSize(maxX + 80, maxY + 80);
		drawActivitySet(activitySet);
		drawLogicNodeSet(logicNodeSet);
		drawFlowSet(flowSet,GraphConst.DRAW_PROCESSTYPE);	
		String fileName = ((WorkflowGraph) workflowGraph).generateImage(process.getPackageID(),process.getWorkflowID(),process.getProcessID());
		return fileName;
	}
	private int getMaxY() {
		// TODO Auto-generated method stub
		int max = 0;
		BaseActivity activity;
		LogicNode logicNode;
		for(int i=0; i<activitySet.size(); i++)
		{
			activity = (BaseActivity)activitySet.get(i);
			if(max<activity.getActivityPoint().getY())
			{
				max = (int)activity.getActivityPoint().getY();
			}
		}
		for(int i=0; i<logicNodeSet.size(); i++)
		{
			logicNode = (LogicNode)logicNodeSet.get(i);
			if(max < logicNode.getPoint().getY())
			{
				max = (int)logicNode.getPoint().getY();
			}
		}
		return max;
	}
	private int getMaxX() {
		// TODO Auto-generated method stub
		int max = 0;
		BaseActivity activity;
		LogicNode logicNode;
		for(int i=0; i<activitySet.size(); i++)
		{
			activity = (BaseActivity)activitySet.get(i);
			if(max<activity.getActivityPoint().getX())
			{
				max = (int)activity.getActivityPoint().getX();
			}
		}
		for(int i=0; i<logicNodeSet.size(); i++)
		{
			logicNode = (LogicNode)logicNodeSet.get(i);
			if(max < logicNode.getPoint().getX())
			{
				max = (int)logicNode.getPoint().getX();
			}
		}
		return max;
	}
	private void drawFlowSet(List flowSet, int drawType) {
		// TODO Auto-generated method stub
		 LinkFlow flow;
		 Port sourcePort = null;
		 Port targetPort = null;
		 DefaultGraphCell source = null;
		 DefaultGraphCell target = null;
		 DefaultEdge edge;
		 this.flowSet = flowSet;
		 for(int i=0; i<flowSet.size(); i++)
		 {
			 flow = (LinkFlow)flowSet.get(i);
			 if(flow.getParentID()!=-1)
			 {
				 continue;
			 }
			 source = getConnectCell(flow.getFromObjectType(),flow.getFromObjectID());
			 target = getConnectCell(flow.getToObjectType(),flow.getToObjectID());
			 sourcePort = getConnectPort(source);
			 targetPort = getConnectPort(target);
			 if(sourcePort!=null&&targetPort!=null)
			 {
				 Color color = null;
				 if(flow.getLinkType()==GraphConst.DATAFLOW_LINK_TYPE)
				 {
					 color = Color.BLACK;
				 }else
				 {
					 if(drawType == GraphConst.DRAW_PROCESSTYPE)
					{
						 color = getLinkColor(source,target);
					}else
					{
						color = Color.BLACK;
					}
					 
				 }
				 edge = this.connect(sourcePort, targetPort,
		                                flow.getLinkType(),color);
				 edge.setUserObject(flow);
			 }
			
		 }
	}
	private Color getLinkColor(DefaultGraphCell source, DefaultGraphCell target) {
		// TODO Auto-generated method stub
		Color color = Color.BLACK;
		String sourceState = null;
		String targetState = null;
		Object sourceObject = source.getUserObject();
		Object targetObject = target.getUserObject();
		if(sourceObject instanceof BaseActivity)
		{
			sourceState = ((BaseActivity)sourceObject).getActivityState();
		}else if(sourceObject instanceof LogicNode)
		{
			for(int j=0; j<logicCell.size(); j++)
			 {
				 Boolean flag = new Boolean(false);
				 this.flagSet.add(j,flag);
			 }
			sourceState = getAncesterState((LogicNode)sourceObject);
		}
		if(sourceState.equals(GraphConst.ACTIVITY_STATE_COMPLETED))// completed
		{
			color = Color.green;
		}
		return color;
	}
	private String getAncesterState(LogicNode node) {
		// TODO Auto-generated method stub
		String state = GraphConst.ACTIVITY_STATE_WAITING;
		for(int i=0; i<logicCell.size(); i++)
		{
			LogicNode tempNode = (LogicNode)((DefaultGraphCell)logicCell.get(i)).getUserObject();
			if(tempNode.getLogicNodeID()==node.getLogicNodeID())
			{
				Boolean flag = (Boolean)flagSet.get(i);
				if(flag==true)
				{
					return GraphConst.ACTIVITY_STATE_WAITING;
				}else
				{
					flag = true;
					flagSet.set(i,flag);
				}
				break;
			}
		}
		if(node.getInputType()==GraphConst.AND_LOGIC_TYPE)
		{
			for(int i=0; i<flowSet.size(); i++)
			{
				LinkFlow flow = (LinkFlow)flowSet.get(i);
				if(flow.getLinkType()==GraphConst.DATAFLOW_LINK_TYPE)
				{
					continue;
				}
				if(flow.getToObjectType()==GraphConst.LOGICNODE_OBJECT_TYPE)
				{
					if(flow.getToObjectID()==node.getLogicNodeID())
					{
						DefaultGraphCell sourceCell = getConnectCell(flow.getFromObjectType(),flow.getFromObjectID());
						Object userObject = sourceCell.getUserObject();
						
						if(userObject instanceof BaseActivity)
						{
							state = ((BaseActivity)userObject).getActivityState();
							
							
						}else if(userObject instanceof LogicNode)
						{
							state = getAncesterState((LogicNode)userObject);
							
						}
						if(!state.equals(GraphConst.ACTIVITY_STATE_COMPLETED))
						{
							return GraphConst.ACTIVITY_STATE_WAITING;
						}
					}
					
				}
			}
		}else
		{
			for(int i=0; i<flowSet.size(); i++)
			{
				LinkFlow flow = (LinkFlow)flowSet.get(i);
				if(flow.getLinkType()==GraphConst.DATAFLOW_LINK_TYPE)
				{
					continue;
				}
				if(flow.getToObjectType()==GraphConst.LOGICNODE_OBJECT_TYPE)
				{
					if(flow.getToObjectID()==node.getLogicNodeID())
					{
						DefaultGraphCell sourceCell = getConnectCell(flow.getFromObjectType(),flow.getFromObjectID());
						Object userObject = sourceCell.getUserObject();
						if(userObject instanceof BaseActivity)
						{
							state = ((BaseActivity)userObject).getActivityState();
						}else if(userObject instanceof LogicNode)
						{
							state = getAncesterState((LogicNode)userObject);
						}
						if(state.equals(GraphConst.ACTIVITY_STATE_COMPLETED))
						{
							return GraphConst.ACTIVITY_STATE_COMPLETED;
						}
					}
				}
			}
		}
		return state;
	}
	private DefaultGraphCell getConnectCell(int objectType, int objectID) {
		// TODO Auto-generated method stub
		DefaultGraphCell cell = null;
		  if (objectType ==
              GraphConst.LOGICNODE_OBJECT_TYPE) 
		  {
			  for (int j = 0; j < logicCell.size(); j++) 
			  {
				  if ( ( (LogicNode) ( (DefaultGraphCell) logicCell
                                  .get(j)).getUserObject()).getLogicNodeID() == objectID) {
                cell = (DefaultGraphCell) logicCell.get(j);
                break;
              }
            }
          }else if (objectType==GraphConst.ACTIVITY_OBJECT_TYPE) 
          {
            for (int j = 0; j < activityCell.size(); j++) {
              if ( ( (BaseActivity) ( (DefaultGraphCell) activityCell
                                         .get(j)).getUserObject())
                  .getActivityID() == objectID) {
                cell = (DefaultGraphCell) activityCell
                    .get(j);
                break;
              }
            }
          }
		return cell;
	}
	private DefaultEdge connect(Port sourcePort, Port targetPort, int linkType, Color color) {
		// TODO Auto-generated method stub
		 ConnectionSet cs = new ConnectionSet();
		 DefaultEdge edge = new DefaultEdge(this.workflowGraph);
		 edge.setSource(workflowGraph.getGraphLayoutCache().getMapping(sourcePort, true));
		 edge.setTarget(workflowGraph.getGraphLayoutCache().getMapping(targetPort, true));
		 cs.connect(edge, sourcePort, targetPort);
		 Map map = new Hashtable();
		 GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
		              GraphConstants.setLabelAlongEdge(map,false);

		    if (linkType == GraphConst.DATAFLOW_LINK_TYPE) {
		      float[] dash = {
		          5f, 5f};
		      GraphConstants.setDashPattern(map, dash);
		    }
		    // Add a label along edge attribute
		    GraphConstants.setEditable(map, false);
		    GraphConstants.setLabelAlongEdge(map, false);
		    GraphConstants.setLineColor(map, color);
		    // Construct a Map from cells to Maps (for insert)
		    Hashtable attributes = new Hashtable();
		    // Associate the Edge with its Attributes
		    attributes.put(edge, map);
		    // Insert the Edge and its Attributes
		    workflowGraph.getGraphLayoutCache().insert(new Object[] {edge}, attributes,
		                                       cs, null, null);
		    return edge;
	}
	private DefaultPort getConnectPort(DefaultGraphCell cell) {
		// TODO Auto-generated method stub
		DefaultPort port = null;
		if(cell!=null)
		{
			Object[] objectCell = new Object[2];
			objectCell[0] = cell;
			objectCell = workflowGraph.getDescendants(objectCell);
			if (objectCell[1] instanceof DefaultPort) 
			{
	            port = (DefaultPort) objectCell[1];
	        }
		}
		return port;
	}
	private void drawActivitySet(List activitySet) {
		// TODO Auto-generated method stub
		BaseActivity activity;
		String iconName = null;
		Point point;
		for(int i=0; i<activitySet.size(); i++)
		{
			activity = (BaseActivity)activitySet.get(i);
			if(activity.getParentID()!=-1)
			{
				continue;
			}
			if(activity.getActivityType()==GraphConst.START_ACTIVITY_TYPE)
			{
				iconName = "StartActivity";
			}else if(activity.getActivityType()==GraphConst.END_ACTIVITY_TYPE)
			{
				iconName = "EndActivity";
			}else if(activity.getActivityType() == GraphConst.GENERAL_ACTIVITY_TYPE)
			{
				switch(activity.getActivityImp())
				{
					case GraphConst.ACTIVITY_APPLICATION:
						iconName = "ApplicationActivity";
						break;
					case GraphConst.ACTIVITY_COMPOSITE:
						iconName = "CompositeActivity";
						break;
					case GraphConst.ACTIVITY_TRANSFORM_XML:
						iconName = "TransformXMLActivity";
						break;
					case GraphConst.ACTIVITY_SET_VALUE:
						iconName = "SetValueActivity";
						break;
					case GraphConst.ACTIVITY_SUBWORKFLOW:
						iconName = "SubworkflowActivity";
						break;
					case GraphConst.ACTIVITY_PERSON:
						iconName = "PersonActivity";
						break;
					case GraphConst.ACTIVITY_SCHEDULE:
						iconName = "ScheduleActivity";
						break;
					
				}
			}
			point = activity.getActivityPoint();
			iconName = iconName + "Object";
			DefaultGraphCell tempCell = insert(activity,iconName,point,activity.getActivityState());
			tempCell.setUserObject(activity);
			activityCell.add(tempCell);
			
		}
		
	}
	private DefaultGraphCell insert(Object userObject,String iconName, Point point,String state) {
		// TODO Auto-generated method stub
		DefaultGraphCell item = new DefaultGraphCell();
		item.add(new DefaultPort());
		Map viewMap = new Hashtable();
	    Map propertyMap = createPropertyMap(userObject,iconName, point,state); ////////////////////
	    viewMap.put(item, propertyMap);
	    workflowGraph.getModel().insert(new Object[] {item}
	                              , viewMap, null, null,
	                              null);
		return item;
	}
	private Map createPropertyMap(Object userObject,String iconName, Point point,String state) {
		// TODO Auto-generated method stub
		 boolean isEditable = false;
		 Dimension dimension = null;
		 Map map = new Hashtable();
		 ImageIcon icon = ImageUtil.getIcon(iconName);
		 int iconWidth = 0;
		 int iconHeight = 0;
		 if (icon != null) 
		 {
			 iconWidth = icon.getIconWidth();
			 iconHeight = icon.getIconHeight();
		 }
		 int txtWidth = workflowGraph.getFontMetrics(workflowGraph.getFont()).charWidth('A');
		 int txtHeight = workflowGraph.getFontMetrics(workflowGraph.getFont()).getHeight(); // 璁＄畻1涓瓧绗︾殑瀹藉害鍜岄佩搴?
		 if(userObject instanceof BaseActivity)
		 {
			 txtHeight = txtHeight * 2;
			 Border border = null;
			 if(state.equals(GraphConst.ACTIVITY_STATE_COMPLETED))
			 {
				 border = BorderFactory.createLineBorder(Color.green,3);
				 GraphConstants.setBorder(map,border);
			 }else if(state.equals(GraphConst.ACTIVITY_STATE_RUNNING))
			 {
				 border = BorderFactory.createLineBorder(Color.orange,3);
				 GraphConstants.setBorder(map,border);
			 }else if(state.equals(GraphConst.ACTIVITY_STATE_READY))
			 {
				 border = BorderFactory.createLineBorder(Color.red,3);
				 GraphConstants.setBorder(map,border);
			 }
			
		 }
		 else
		 {
			 txtHeight = txtHeight / 2;
		 }
		 dimension = new Dimension(iconWidth + txtWidth, iconHeight
		                                + txtHeight);
		 Rectangle2D bounds = new Rectangle(point, dimension);
		 GraphConstants.setBounds(map, bounds);
		 GraphConstants.setOpaque(map, true);
		 GraphConstants.setIcon(map, icon);
		 GraphConstants.setEditable(map, isEditable);
		 GraphConstants.setLabelAlongEdge(map, true);
		return map;
	}
	private void drawLogicNodeSet(List logicNodeSet) {
		// TODO Auto-generated method stub
		  LogicNode logicNode;
		  String iconName = null;
		  if(logicNodeSet != null) 
		  {
		      int size = logicNodeSet.size();
		      for (int i = 0; i < size; i++) 
		      {
		    	  logicNode = (LogicNode) logicNodeSet.get(i);
		    	  if(logicNode.getParentID() != -1) 
		    	  {
		    		  continue;
		    	  }
		        if(logicNode.getInputType() == GraphConst.AND_LOGIC_TYPE
		            && logicNode.getOutputType() == GraphConst.AND_LOGIC_TYPE) 
		        {
		        	iconName = "And-andNode";
		        }
		        else if (logicNode.getInputType() == GraphConst.AND_LOGIC_TYPE
		                 && logicNode.getOutputType() == GraphConst.OR_LOGIC_TYPE)
		        {
		        	iconName = "And-orNode";
		        }
		        else if (logicNode.getInputType() == GraphConst.OR_LOGIC_TYPE
		                 && logicNode.getOutputType() == GraphConst.AND_LOGIC_TYPE) 
		        {
		        	iconName = "Or-andNode";
		        }
		        else if (logicNode.getInputType() == GraphConst.OR_LOGIC_TYPE
		                 && logicNode.getOutputType() == GraphConst.OR_LOGIC_TYPE) 
		        {
		        	iconName = "Or-orNode";
		        }
		        iconName = iconName + "Object";
		        DefaultGraphCell tempLogicCell = this.insert(logicNode,iconName,
		            logicNode.getPoint(),"");
		        tempLogicCell.setUserObject(logicNode);
		        logicCell.add(tempLogicCell);
		      }
		    }
		
		
	}
	public String drawProcess(int workflowID)
	{
		InitiateProcess initProcess = new InitiateProcess();
		initProcess.initProcess(workflowID);
		ProcessInfo process = initProcess.getProcessInfo();
		activitySet = process.getActivitySet();
		logicNodeSet = process.getLogicNodeSet();
		flowSet = process.getFlowSet();
		int maxX = getMaxX();
		int maxY = getMaxY();
		workflowGraph.setSize(maxX + 80, maxY + 80);
		drawActivitySet(activitySet);
		drawLogicNodeSet(logicNodeSet);
		drawFlowSet(flowSet,GraphConst.DRAW_WORKFLOWTYPE);
		String fileName = ((WorkflowGraph) workflowGraph).generateImage(process.getPackageID(),process.getWorkflowID());
		return fileName;
	}

}
