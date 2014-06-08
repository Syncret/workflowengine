package cit.workflow.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import cit.workflow.utils.WorkflowConnectionPool;

public class InitialVariablesDialog extends JDialog{
	protected final Logger logger =   Logger.getLogger(this.getClass());
	private JScrollPane scrollPane=null;
	private JTable table=null;
	private JPanel buttonPanel=new JPanel();
	private JButton confirmButton=new JButton();
	private JButton cancelButton=new JButton();
	private int workflowID;
	private WorkflowEngineMainFrame mainFrame;
	Connection conn = null;
	QueryRunner query = null;
	
	public WorkflowEngineMainFrame getMainFrame(){return mainFrame;}
	
	public InitialVariablesDialog(int workflowID,WorkflowEngineMainFrame frame){
		this.workflowID=workflowID;
		this.mainFrame=frame;
		conn = WorkflowConnectionPool.getInstance().getConnection();
		query = new QueryRunner();
		this.setTitle("Initial Variables");
		
	}
	
	public void Init(){
		this.setSize(400,450);
		ViewUtil.centerComponent(this);
		confirmButton.setText("Execute");
		confirmButton.addActionListener(new ConfirmActionListener(this));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new CancelActionListener(this));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(confirmButton);
		buttonPanel.add(cancelButton);
		
		String[] headers={"ID","Name","Type","Value"};
		
		Object[][] data=getVariablesData();
		DefaultTableModel model = new DefaultTableModel(data, headers) {
			public boolean isCellEditable(int row, int column) {
				if(column<this.getColumnCount()-1) return false;
				else return true;
			}
		};
		table = new JTable(model) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer renderer = super.getCellRenderer(row, column);
				if (renderer instanceof JLabel) {
					if(column==0||column==2)
						((JLabel)renderer).setHorizontalAlignment(JLabel.CENTER);
					if(column==1||column==3)
						((JLabel)renderer).setHorizontalAlignment(JLabel.LEFT);
				}
				return renderer;
			}
		};
		TableColumn column1 = table.getColumnModel().getColumn(0);
		TableColumn column2 = table.getColumnModel().getColumn(1);
		TableColumn column3 =table.getColumnModel().getColumn(2);
		TableColumn column4=table.getColumnModel().getColumn(3);
		column1.setPreferredWidth(30);
		column1.setResizable(false);
		column2.setPreferredWidth(120);
		column3.setPreferredWidth(50);
		column3.setResizable(false);
		column4.setPreferredWidth(200);
		table.setFillsViewportHeight(true);
		scrollPane=new JScrollPane(table);
//		scrollPane.updateUI();
		
		this.getContentPane().add(scrollPane,BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
	}
	
	
	public Object[][] getVariablesData(){
		String connstr = "";
		Object[][] data = null;
		PreparedStatement pstmt = null;

		try {
			connstr="select * from workflowinherentvariable where workflowID=? Order by ObjectID";
			pstmt = conn.prepareStatement(connstr);
			pstmt.setInt(1, workflowID);
			ResultSet rs = pstmt.executeQuery();

			int i=0;
			while(rs.next())i++;
			if(i==0){ 
				logger.info("No variables need to be initialled");
				return null;
			}
			
			String[] valueType={"int","float","double","String"};
			data=new Object[i][4];

			int j = 0;
			rs.beforeFirst();
			while (rs.next()) {
				int objectID=rs.getInt("ObjectID");
				data[j][0]=objectID;
				data[j][1]=rs.getString("ObjectName");
				data[j][2]=valueType[rs.getInt("ValueType")-1];
				data[j][3]=rs.getString("InitialValue");
				j++;
			}

//			connstr="select * from workflowstartvariable where workflowID=?";
//			pstmt = conn.prepareStatement(connstr);
//			pstmt.setInt(1, workflowID);
//			ResultSet rs = pstmt.executeQuery();
//
//			int i=0;
//			while(rs.next())i++;
//			if(i==0){ 
//				logger.info("No variables need to be initialled");
//				return null;
//			}
//			String[] valueType={"int","float","double","String"};
//			data=new Object[i][4];
//
//			int j = 0;
//			rs.beforeFirst();
//			while (rs.next()) {
//				int objectID=rs.getInt(2);
//				data[j][0]=objectID;
//				data[j][1]=rs.getString(3);
//				if(rs.getInt(4)==6)data[j][2]="Array";
//				if(rs.getInt(4)==1){
//					PreparedStatement pstmt2 = null;
//					connstr="select * from workflowinherentvariable where workflowID=? and objectID=?";
//					pstmt2 = conn.prepareStatement(connstr);
//					pstmt2.setInt(1, workflowID);
//					pstmt2.setInt(2, objectID);
//					
//					ResultSet rs2=pstmt2.executeQuery();
//					rs2.next();
//					data[j][2]=valueType[rs2.getInt("ValueType")-1];
//					data[j][3]=rs2.getString("InitialValue");
//					pstmt2.close();
//					rs2.close();
//				}
//				j++;
//			}
			
			
			rs.close();
			pstmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return data;
	}

	private class ConfirmActionListener implements ActionListener{
		private InitialVariablesDialog mainDlg;
		public ConfirmActionListener(InitialVariablesDialog mainDlg){
			this.mainDlg=mainDlg;
		}

		public void actionPerformed(ActionEvent arg0) {
			String[][] data = null;
			int rowCount=table.getRowCount();
			if(rowCount==0)mainDlg.dispose();;
			data=new String[rowCount][2];
			for(int i=0;i<table.getRowCount();i++){
				data[i][0]=table.getValueAt(i, 0).toString();
				Object tempvalue=table.getValueAt(i, 3);
				if(tempvalue==null) tempvalue="";
				data[i][1]=tempvalue.toString();
			}
			mainDlg.getMainFrame().setInitialVariables(data);
			mainDlg.dispose();
		}
	}
	
	private class CancelActionListener implements ActionListener{
		private JDialog mainDlg;
		public CancelActionListener(JDialog mainDlg){
			this.mainDlg=mainDlg;
		}

		public void actionPerformed(ActionEvent arg0) {
			mainDlg.dispose();			
		}
	}
}
