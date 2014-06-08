package cit.workflow.view;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.border.EtchedBorder;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.ScrollPaneConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.ComponentOrientation;

public class WorkflowMainManager {

	private static final WorkflowMainManager manager=new WorkflowMainManager();
	private JFrame frmWorkflowManager;
	private JTree hostTree;
	private HostTreeNode treeNodeRoot;
	private HostTreeNode treeNodeHosts;
	private DefaultTreeModel hostTreeModel;
	private JToolBar toolBar;
	private JButton btnRun;
	private JButton btnStop;
	private JButton btnAddhost;
	private JButton btnDeletehost;
	private JSplitPane mainSplitPane;
	private JScrollPane treeScrollPane;
	private JSplitPane rightSplitPane;
	private JSplitPane hostSplitPane;
	private JScrollPane logScrollPane;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmNewWorkflow;
	private JMenuItem mntmExit;
	private JMenu mnEdit;
	private JMenuItem mntmRunHost;
	private JMenuItem mntmStopHost;
	private JMenuItem mntmAddHost;
	private JMenuItem mntmDeleteHost;
	private JMenuItem mntmViewHost;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;
	private JScrollPane serviceTableScrollPane;
	private JScrollPane hostTableScrollPane;
	private JTable serviceTable;
	private JPanel hostInformationPanel;
	private JTable logHostTable;
	private JTextPane logTextPane;
	private JPanel hostStatePanel;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WorkflowMainManager window = new WorkflowMainManager();
					window.frmWorkflowManager.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WorkflowMainManager() {
		initialize();
	}
	
	public WorkflowMainManager getInstance(){
		return manager;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWorkflowManager = new JFrame();
		frmWorkflowManager.setTitle("Workflow Manager");
		frmWorkflowManager.setSize(800, 600);
		ViewUtil.centerComponent(frmWorkflowManager);
		frmWorkflowManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWorkflowManager.getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolBar = new JToolBar();
		frmWorkflowManager.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		btnRun = new JButton("");
		btnRun.setBorder(new EmptyBorder(4, 4, 4, 2));
		btnRun.setIcon(new ImageIcon(new ImageIcon(WorkflowMainManager.class.getResource("/cit/workflow/graph/util/images/HostRun.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
		toolBar.add(btnRun);
		
		btnStop = new JButton("");
		btnStop.setBorder(new EmptyBorder(4, 2, 4, 2));
		btnStop.setIcon(new ImageIcon(new ImageIcon(WorkflowMainManager.class.getResource("/cit/workflow/graph/util/images/HostStop.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
		toolBar.add(btnStop);
		
		btnAddhost = new JButton("");
		btnAddhost.addActionListener(new ActionAddHost());
		btnAddhost.setBorder(new EmptyBorder(4, 2, 4, 2));
		btnAddhost.setIcon(new ImageIcon(new ImageIcon(WorkflowMainManager.class.getResource("/cit/workflow/graph/util/images/HostAdd.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
		toolBar.add(btnAddhost);
		
		btnDeletehost = new JButton("");
		btnDeletehost.addActionListener(new ActionDeleteHost());
		btnDeletehost.setBorder(new EmptyBorder(4, 2, 4, 2));
		btnDeletehost.setIcon(new ImageIcon(new ImageIcon(WorkflowMainManager.class.getResource("/cit/workflow/graph/util/images/HostDelete.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
		toolBar.add(btnDeletehost);
		
		mainSplitPane = new JSplitPane();
		mainSplitPane.setDividerSize(3);
		mainSplitPane.setResizeWeight(0.2);
		mainSplitPane.setBorder(null);
		mainSplitPane.setContinuousLayout(true);
		frmWorkflowManager.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
		
		treeScrollPane = new JScrollPane();
		mainSplitPane.setLeftComponent(treeScrollPane);
		treeScrollPane.setBorder(new TitledBorder(null, "Hosts", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		//JTree
		treeNodeRoot = new HostTreeNode("Root",HostTreeNode.ROOTNODE);
		treeNodeHosts = new HostTreeNode("Hosts",HostTreeNode.HOSTSNODE);
		HostTreeNode host1=new HostTreeNode("192.168.0.1",HostTreeNode.HOSTNODE);
		HostTreeNode workflow1=new HostTreeNode("getWeather",HostTreeNode.WORKFLOWNODE);
		host1.add(workflow1);
		treeNodeHosts.add(host1);
		treeNodeRoot.add(treeNodeHosts);
		hostTreeModel=new DefaultTreeModel(treeNodeRoot);
		hostTreeModel.addTreeModelListener(new HostTreeModelListener());
		hostTree = new JTree(hostTreeModel);
		hostTree.addMouseListener(new HostTreeMouseAdapter());
		hostTree.setRowHeight(20);
		hostTree.setRootVisible(false);
		hostTree.setShowsRootHandles(true);
		hostTree.setCellRenderer(new HostTreeCellRenderer());
		hostTree.setEditable(false);
		hostTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		hostTree.expandRow(0);
		
		treeScrollPane.setViewportView(hostTree);
		
		//right part		
		rightSplitPane = new JSplitPane();
		rightSplitPane.setDividerSize(3);
		rightSplitPane.setContinuousLayout(true);
		rightSplitPane.setResizeWeight(0.6);
		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setRightComponent(rightSplitPane);
		
		//host information panel
		hostSplitPane=new JSplitPane();
		hostSplitPane.setDividerSize(3);
		
		hostInformationPanel=new JPanel();
		hostInformationPanel.setLayout(new BorderLayout());
		hostTableScrollPane=new JScrollPane();
		hostTableScrollPane.setBorder(new TitledBorder(null, "Host Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		hostTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//		hostPanel.add(hostTableScrollPane);
		serviceTableScrollPane = new JScrollPane();
		serviceTableScrollPane.setBorder(new TitledBorder(null, "Running Service", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		hostInformationPanel.add(serviceTableScrollPane,BorderLayout.CENTER);
		hostSplitPane.setRightComponent(hostInformationPanel);
		
		
		
		rightSplitPane.setLeftComponent(hostSplitPane);
		
		hostStatePanel = new JPanel();
		hostSplitPane.setLeftComponent(hostStatePanel);
		hostSplitPane.setDividerLocation(300);
		
		
		//log panel
		logScrollPane = new JScrollPane();
		logScrollPane.setBorder(new TitledBorder(null, "Host Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logTextPane = new JTextPane();
		logScrollPane.setViewportView(logTextPane);
		rightSplitPane.setRightComponent(logScrollPane);
		mainSplitPane.setDividerLocation(200);


		
		
		
		
//		serviceTable = new JTable();
//		tableScrollPane.setViewportView(serviceTable);
		
		//Menu
		menuBar = new JMenuBar();
		frmWorkflowManager.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmNewWorkflow = new JMenuItem("New Workflow");
		mnFile.add(mntmNewWorkflow);
		mnFile.addSeparator();
		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmRunHost = new JMenuItem("Run Host");
		mnEdit.add(mntmRunHost);
		
		mntmStopHost = new JMenuItem("Stop Host");
		mnEdit.add(mntmStopHost);
		
		mntmAddHost = new JMenuItem("Add Host");
		mntmAddHost.addActionListener(new ActionAddHost());
		mnEdit.add(mntmAddHost);
		
		mntmDeleteHost = new JMenuItem("Delete Host");
		mntmDeleteHost.addActionListener(new ActionDeleteHost());
		mnEdit.add(mntmDeleteHost);
		
		mntmViewHost=new JMenuItem("View Workflow Instance");
		mntmViewHost.addActionListener(new ActionViewHost());
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
	}
	
	
	
	//hostTree mouse event listener
	private class HostTreeMouseAdapter extends MouseAdapter{
		@Override
		public void mousePressed(MouseEvent e){
			int selrow=hostTree.getRowForLocation(e.getX(),e.getY());
			hostTree.setSelectionRow(selrow);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				HostTreeNode node=(HostTreeNode) hostTree.getLastSelectedPathComponent();
				if(node==null)return;
				if(node.getNodeType()==HostTreeNode.HOSTNODE){
					//service table
					String[] columns={"","Service Name","Class","State"};
					Object[][] data={
							{"","engine service","cit.workflow.workflow","Running"},
							{"","modelling service","edu.sjtu.grid.workflow.modeling.view,ModelingMainApplet","Stopped"}};
					serviceTable=new JTable(data,columns);
					
					TableColumn column=null;
					column=serviceTable.getColumnModel().getColumn(0);
					column.setPreferredWidth(20);
					column.setMaxWidth(20);
					column.setResizable(false);
					column=serviceTable.getColumnModel().getColumn(1);
					column.setPreferredWidth(100);
					column=serviceTable.getColumnModel().getColumn(2);
					column.setPreferredWidth(200);
					column=serviceTable.getColumnModel().getColumn(3);
					column.setPreferredWidth(50);
					serviceTableScrollPane.setViewportView(serviceTable);
					//host table
					columns=new String[]{"","Host","Port","State","CPU Usage","Memory Usage"};
					data=new Object[][]{{"",node.toString(),"3333","Running","80%","50%"}};
					logHostTable = new JTable(data,columns);
					column=logHostTable.getColumnModel().getColumn(0);
					column.setPreferredWidth(20);
					column.setMaxWidth(20);
					column.setResizable(false);
					hostTableScrollPane.setViewportView(logHostTable);
					
					logTextPane.setText("Here is the log");
				}
				else return;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e){
			if(e.isPopupTrigger()){
				HostTreeNode node=(HostTreeNode) hostTree.getLastSelectedPathComponent();
				if(node==null)return;
				if(node.getNodeType()==HostTreeNode.HOSTNODE){
					JPopupMenu mouseMenu=new JPopupMenu();
					mouseMenu.add(mntmAddHost);
					mouseMenu.add(mntmDeleteHost);
					mouseMenu.add(mntmViewHost);
					mouseMenu.show(e.getComponent(), e.getX(), e.getY());
				}
				if(node.getNodeType()==HostTreeNode.HOSTSNODE){
					JPopupMenu mouseMenu=new JPopupMenu();
					mouseMenu.add(mntmAddHost);
					mouseMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}


	//add a host
	private class ActionAddHost implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String host=(String) JOptionPane.showInputDialog((Component)e.getSource(),"Please enter the url：\n","Add Host",JOptionPane.PLAIN_MESSAGE,null,null,"http://");
			try {
				URL url=new URL(host);
				if(!url.getProtocol().equals("http")){
					JOptionPane.showMessageDialog(null, "Unsupported protocal:\n"+url.getProtocol(),"Illegal Host" , JOptionPane.ERROR_MESSAGE);
					return;
				};
				if(url.getHost()==null||url.getHost().equals("")){
					return;
				}
				HostTreeNode newHostNode=new HostTreeNode(url.getHost(),HostTreeNode.HOSTNODE);
				hostTreeModel.insertNodeInto(newHostNode, treeNodeHosts, treeNodeHosts.getChildCount());
				hostTree.scrollPathToVisible(new TreePath(newHostNode.getPath()));
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Illegal URL\n"+e1.getMessage(),"Illegal Host",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	//delete a host
	private class ActionDeleteHost implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			HostTreeNode selectedNode=(HostTreeNode)hostTree.getLastSelectedPathComponent();
			if(selectedNode==null || selectedNode.getNodeType()!=HostTreeNode.HOSTNODE){			
				JOptionPane.showMessageDialog(null, "No host selected", "Info", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			else {
				int confirm=JOptionPane.showConfirmDialog(null, "Are you sure to delete the host "+selectedNode.toString(), "Info", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if(confirm==0)
					hostTreeModel.removeNodeFromParent(selectedNode);
			}
		}
	}
	
	//view a host
		private class ActionViewHost implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				HostTreeNode selectedNode=(HostTreeNode)hostTree.getLastSelectedPathComponent();
				if(selectedNode==null || selectedNode.getNodeType()!=HostTreeNode.HOSTNODE){			
					JOptionPane.showMessageDialog(null, "No host selected", "Info", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else {
					WorkflowInstanceDialog dialog=new WorkflowInstanceDialog(selectedNode.toString());
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		}
		
		
	public class HostTreeCellRenderer extends DefaultTreeCellRenderer {
		 private static final long serialVersionUID = 1L;
		 public HostTreeCellRenderer() {
		  super();
		 }
		 @Override
		 public Component getTreeCellRendererComponent(JTree tree, Object value,
		   boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		  // TODO Auto-generated method stub
		  super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		  
		  HostTreeNode treeNode = (HostTreeNode)value;
		  
		  int type = treeNode.getNodeType();
		  switch (type) {
		   case 1:
		    this.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/cit/workflow/graph/util/images/HostsTreeNode.gif")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT)));  
		    break;
		   case 2:
		    this.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/cit/workflow/graph/util/images/HostTreeNode.gif")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT)));  
		    break;
		   case 3:
		    this.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/cit/workflow/graph/util/images/WorkflowTreeNode.gif")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT)));  
		    break;
		   default:
		    break;
		  }
		  return this;
		}
	}
	
	public class HostTreeModelListener implements TreeModelListener{

		public void treeNodesChanged(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void treeNodesInserted(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void treeNodesRemoved(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void treeStructureChanged(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
