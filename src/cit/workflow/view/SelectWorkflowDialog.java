package cit.workflow.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
//import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import test.SxhTest;
import cit.workflow.utils.WorkflowConnectionPool;

public class SelectWorkflowDialog extends JDialog {

	WorkflowEngineMainFrame mainFrame;
	JList<String> packageList;
	JScrollPane jPacketScrollPane;
	JScrollPane jWorkflowScrollPane;
	JTable WFmodelTable = new JTable();
	JSplitPane jSplitPane1 = new JSplitPane();
	JLabel textleft = new JLabel("包");
	JLabel textright = new JLabel("云工作流模型");
	JButton okButton = new JButton();
	int packageIndex;
	int workflowID = -1; 
	Vector<String> packageNames=new Vector<String>();
	protected final Logger logger = Logger.getLogger(this.getClass());
	Connection conn = null;
	QueryRunner query = null;
	
	
	
	
	public SelectWorkflowDialog(WorkflowEngineMainFrame mainframe){
		this.mainFrame=mainframe;
		conn = WorkflowConnectionPool.getInstance().getConnection();
		query = new QueryRunner();
		this.setTitle("选择云工作流");
		this.setSize(500, 320);
		try {
			Init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void Init() throws Exception {
		
		this.getContentPane().setLayout(null);
		this.setFont(new java.awt.Font("Dialog", 0, 12));
		
		String statement=null;
		statement="select PackageName from PackageInformation order by PackageID asc";

		Object[] params = new Object[] {};
		int[] types = new int[] {};
//		Object[] name=(Object[]) query.query(conn,	statement, params, new ArrayListHandler(), types);
		List<Object[]> names=(List<Object[]>) query.query(conn,	statement, new ArrayListHandler(),params);
		for(Object[] name:names){
			packageNames.add((String)name[0]);
		}
 
		packageList = new JList<String>(packageNames);

		packageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPacketScrollPane = new JScrollPane(packageList);
		jWorkflowScrollPane = new JScrollPane(WFmodelTable);
		// jWorkflowScrollPane = new JScrollPane(jList2);
		jSplitPane1.setBounds(new Rectangle(10, 10, 472, 220));
		jSplitPane1.setBorder(new TitledBorder(null, "云工作流模型",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));
		jSplitPane1.setDividerLocation(140);
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new MatteBorder(0, 0, 0, 0, Color.black));
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(new MatteBorder(0, 0, 0, 0, Color.black));
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(textleft, java.awt.BorderLayout.NORTH);
		leftPanel.add(jPacketScrollPane, java.awt.BorderLayout.CENTER);
		jSplitPane1.setLeftComponent(leftPanel);
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(textright, java.awt.BorderLayout.NORTH);
		rightPanel.add(jWorkflowScrollPane, java.awt.BorderLayout.CENTER);
		jSplitPane1.setRightComponent(rightPanel);
		okButton.setBounds(new Rectangle(316, 236, 80, 28));
		okButton.setText("确定");
		okButton.setEnabled(false);

		//
		WFmodelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.setTitle("选择云工作流模型");
		// jList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.getContentPane().add(okButton);
		this.getContentPane().add(jSplitPane1);
		jSplitPane1.add(leftPanel, JSplitPane.LEFT);
		jPacketScrollPane.getViewport().add(packageList, null);
		jSplitPane1.add(rightPanel, JSplitPane.RIGHT);
		// jWorkflowScrollPane.getViewport().add(jList2, null);
		jWorkflowScrollPane.getViewport().add(WFmodelTable, null);
		packageList.addListSelectionListener(new PackageListListener());
		okButton.addActionListener(new OKButtonListener());
	}
	
	private class PackageListListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent event) {
			int packageIndex = packageList.getSelectedIndex();
			Object[][] data;

			// xss 2009.02.27 modify
			try {
				
				String[] columns = { "WorkflowID", "WorkflowName", "Description" };
				String selectedPackageName = (String) packageNames.get(packageIndex);
				data = getWorkflowColumnsbyPackageID(selectedPackageName,
						columns);
//				logger.info("选中包 - " + selectedPackageName);
				WorkflowListTableModel wfModel = new WorkflowListTableModel(data);
				WFmodelTable = new JTable(wfModel);
				WFmodelTable.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						int SelectedRow = WFmodelTable.getSelectedRow();
						String selectedWorkflowName = "";

						// zy 2008.05.16 add
						try {
							workflowID = Integer.parseInt(WFmodelTable.getValueAt(
									SelectedRow, 0).toString());
							selectedWorkflowName = WFmodelTable.getValueAt(
									SelectedRow, 1).toString();
//							logger.info("选中数据流 - " + selectedWorkflowName);
							okButton.setEnabled(true);
						} catch (Exception ex) {
							okButton.setEnabled(false);
							logger.info("选择越界!");
						}
						// zy end
			// xss modify end
					}

					public void mouseEntered(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					public void mouseExited(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					public void mousePressed(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				});

				jWorkflowScrollPane.getViewport().add(WFmodelTable, null);
				// this.repaint();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// jList2.setListData(workflowName);
			
		}
		
		public Object[][] getWorkflowColumnsbyPackageID(String packageName, String[] columns){
			String connstr = "";
			Object[][] data = new Object[0][0];
			PreparedStatement pstmt = null;

			try {
				connstr = "select ";
				for (int i = 0; i < columns.length; i++) {
					connstr += "WorkflowInformation." + columns[i];
					if (i != columns.length - 1) {
						connstr += ",";
					}
				}
				connstr += " from WorkflowInformation,PackageInformation where PackageInformation.PackageID=WorkflowInformation.PackageID and PackageName='"
						+ packageName + "' order by WorkflowInformation.WorkflowID asc";
				pstmt = conn.prepareStatement(connstr);
				ResultSet rs = pstmt.executeQuery();

				// 指定了数据的第一维度
				data = new Object[30][columns.length];

				int j = 0;
				while (rs.next()) {
					for (int k = 0; k < columns.length; k++) {
						// 本函数始于为打开云工作流时读表方便而设，最初目的为只读取云工作流名称和描述，
						// 故只考虑了（rs.getString），有变化时当修改
						data[j][k] = rs.getString(columns[k]);
					}
					j++;
				}
				rs.close();
				pstmt.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return data;
		}
	}
	
	
	private class OKButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (workflowID < 0) {
				JOptionPane.showMessageDialog(null, "请选择要打开的数据流！", "提示信息",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			setVisible(false);
			dispose();
			mainFrame.setWorkflowID(workflowID);
		}
	}

	
	
	class WorkflowListTableModel extends AbstractTableModel {

		protected final Logger logger = Logger.getLogger(this.getClass());

		Object[][] data;

		String[] columnNames = { "ID", "名称", "描述" };

		public WorkflowListTableModel(Object[][] data) {
			this.data = data;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public Object getValueAt(int row, int column) {
			return data[row][column];
		}

	}
	
	 public static void centerComponent(Component comp) {
		    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		    Dimension compSize = comp.getSize();
		    if (compSize.height > screenSize.height) {
		      compSize.height = screenSize.height;
		    }
		    if (compSize.width > screenSize.width) {
		      compSize.width = screenSize.width;
		    }
		    comp.setLocation( (screenSize.width - compSize.width) / 2,
		                     (screenSize.height - compSize.height) / 2);
		  }


}
