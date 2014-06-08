package cit.workflow.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import cit.workflow.WorkflowServer;
import cit.workflow.elements.Model;

public class WorkflowEngineMainFrame extends JFrame{
	private JButton owButton=new JButton();
	private int workflowID=0;
	private String[][] initialVariables=null;
//	private ConsolePane consolePane=ConsolePane.getInstance();
	private InformationPane consolePane=InformationPane.getInstance();
	private static Logger logger = Logger.getLogger(WorkflowEngineMainFrame.class);
	
	public void setWorkflowID(int id){this.workflowID=id;}
	public void setInitialVariables(String[][] variables){this.initialVariables=variables;}
	public int getWorkflowID(){return workflowID;}
	public WorkflowEngineMainFrame(){
		this.setSize(GetFrameSize());
		this.setTitle("云互联软件平台 V1.0");
		owButton.setText("选择云工作流");
		this.add(consolePane, BorderLayout.CENTER);
		InformationPane.Running=true;
		this.add(owButton,BorderLayout.SOUTH);
		owButton.addActionListener(new OpenWorkflowAdaptee(this));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private class OpenWorkflowAdaptee implements ActionListener{
		private WorkflowEngineMainFrame frame;
		public OpenWorkflowAdaptee(WorkflowEngineMainFrame frame){
			this.frame=frame;
		}
		
		public void actionPerformed(ActionEvent event){
			workflowID=-1;
			SelectWorkflowDialog swdlg=new SelectWorkflowDialog(frame);
			swdlg.setLocationRelativeTo(frame);
			swdlg.setModal(true);
			swdlg.setVisible(true);
			if (workflowID <= 0)
				return;
			InitialVariablesDialog ivdlg=new InitialVariablesDialog(workflowID,frame);
			ivdlg.setLocationRelativeTo(frame);
			ivdlg.Init();
			ivdlg.setModal(true);
			ivdlg.setVisible(true);
			if (initialVariables==null) return;
			WorkflowServer ws = WorkflowServer.getInstance();
			
			ws.setInitialVariables(initialVariables);
			InformationPane.clearText();
			new WorkflowThread(ws).run();

		}
	}
	
	public static Dimension GetFrameSize(){
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension frameSize=new Dimension(screenSize.width/2,screenSize.height*3/4);
		Dimension frameSize=new Dimension(700,550);
		return frameSize;
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
	
	
	private class WorkflowThread extends Thread{
		private WorkflowServer ws;
		public WorkflowThread(WorkflowServer ws){
			this.ws=ws;
		}
		
		
		public void run(){
			String processID;
			try {
				processID = ws.instantiateWorkflow(workflowID, 1, 1, "",
						1, 1);
				logger.info("ProcessID : " + processID);
				Object[] processLog= ws.startProcess(processID, 1, 1);
//				System.out.println("Process Complete = " + success);
				InformationPane.writeln("Process Complete");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	public static void main(String argv[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				WorkflowEngineMainFrame mainFrame = new WorkflowEngineMainFrame();
				centerComponent(mainFrame);
				mainFrame.setVisible(true);
			}
		});
	}
}
