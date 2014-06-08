package cit.workflow.view;

import javax.swing.tree.DefaultMutableTreeNode;

public class HostTreeNode extends DefaultMutableTreeNode{
	
	private int nodeType;//1.Root Node 2.Host Node 3.Workflow Node
	public static final int ROOTNODE=0;
	public static final int HOSTSNODE=1;
	public static final int HOSTNODE=2;
	public static final int WORKFLOWNODE=3;
	public HostTreeNode(){super();}
	public HostTreeNode(Object arg0, boolean arg1){super(arg0,arg1);}
	public HostTreeNode(Object arg0){super(arg0);}
	public HostTreeNode(Object arg0,int type){super(arg0);this.nodeType=type;}
	public void setNodeType(int type){this.nodeType=type;}
	public int getNodeType(){return this.nodeType;}
}
