package cit.workflow.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class WorkflowInstanceDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		try {
//			WorkflowInstanceDialog dialog = new WorkflowInstanceDialog("",{{"","","",""}});
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */
	public WorkflowInstanceDialog(String hostname) {
		this.setTitle("Running Instance on "+hostname);
		setBounds(100, 100, 641, 427);
		ViewUtil.centerComponent(this);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBorder(new TitledBorder(null, "Running Instance on "+hostname, TitledBorder.LEADING, TitledBorder.TOP, null, null));
			String[] column={"Workflow ID","Process ID","Workflow Name","State","Start Time","End Time"};
			Object[][] data={{"1","1111111","getWeather","Running",new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new java.util.Date()),""}};
			JTable table=new JTable(data,column);
			scrollPane.setViewportView(table);
			contentPanel.add(scrollPane,BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ExitActionAdapter(this));
			}
		}
	}
	
	private class ExitActionAdapter implements ActionListener{
		private JDialog dialog;

		public ExitActionAdapter(JDialog dialog){this.dialog=dialog;}
		public void actionPerformed(ActionEvent arg0) {
			dialog.dispose();			
		}
		
	}

}
