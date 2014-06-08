package loid.adapters;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RegisterNewServiceDlg extends JDialog {
  JLabel jLabel1 = new JLabel();
  JTextField jTextField1 = new JTextField();
  JLabel jLabel2 = new JLabel();
  JComboBox jComboBox1 = new JComboBox();
  JLabel jLabel3 = new JLabel();
  JComboBox jComboBox2 = new JComboBox();
  JLabel jLabel4 = new JLabel();
  JTextField jTextField2 = new JTextField();
  JLabel jLabel5 = new JLabel();
  JTextField jTextField3 = new JTextField();
  JButton jButton1 = new JButton();
  JButton jButton2 = new JButton();
  JButton jButton3 = new JButton();

  public RegisterNewServiceDlg (Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public RegisterNewServiceDlg() {
    this(null, "", false);
  }

  private void jbInit() throws Exception {
    jLabel1.setText("请输入要注册的WebService的WSDL的URL");
    jLabel1.setBounds(new Rectangle(12, 56, 224, 34));
    this.getContentPane().setLayout(null);
    jTextField1.setText("");
    jTextField1.setBounds(new Rectangle(227, 55, 335, 41));
    jLabel2.setText("请输入服务对CPU的以来程度");
    jLabel2.setBounds(new Rectangle(24, 128, 169, 41));
    jComboBox1.setBounds(new Rectangle(182, 140, 61, 18));
    jLabel3.setBounds(new Rectangle(284, 128, 169, 41));
    jLabel3.setText("请输入服务对Memory的以来程度");
    jComboBox2.setBounds(new Rectangle(463, 142, 61, 18));
    jLabel4.setBounds(new Rectangle(24, 185, 55, 41));
    jLabel4.setText("服务描述");
    jTextField2.setBounds(new Rectangle(87, 182, 470, 41));
    jTextField2.setText("");
    jLabel5.setText("提供商描述");
    jLabel5.setBounds(new Rectangle(21, 253, 55, 41));
    jTextField3.setText("");
    jTextField3.setBounds(new Rectangle(89, 246, 470, 41));
    jButton1.setBounds(new Rectangle(59, 336, 111, 39));
    jButton1.setText("重置");
    jButton2.setText("提交");
    jButton2.setBounds(new Rectangle(228, 336, 111, 39));
    jButton3.setText("取消");
    jButton3.addActionListener(new RegisterNewServiceDlg_jButton3_actionAdapter(this));
    jButton3.setBounds(new Rectangle(390, 335, 111, 39));
    this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(jTextField1, null);
    this.getContentPane().add(jComboBox2, null);
    this.getContentPane().add(jLabel3, null);
    this.getContentPane().add(jLabel2, null);
    this.getContentPane().add(jComboBox1, null);
    this.getContentPane().add(jLabel4, null);
    this.getContentPane().add(jTextField2, null);
    this.getContentPane().add(jLabel5, null);
    this.getContentPane().add(jTextField3, null);
    this.getContentPane().add(jButton1, null);
    this.getContentPane().add(jButton2, null);
    this.getContentPane().add(jButton3, null);

  }

  public void jButton3_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}

class RegisterNewServiceDlg_jButton3_actionAdapter
    implements ActionListener {
  private RegisterNewServiceDlg adaptee;
  RegisterNewServiceDlg_jButton3_actionAdapter(RegisterNewServiceDlg adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.jButton3_actionPerformed(e);
  }
}
