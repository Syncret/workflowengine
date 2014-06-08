/*
 * Created on 2005-3-8
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;
import java.awt.GridLayout;
import java.io.InputStreamReader;
import java.io.BufferedReader;
//import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.JFileChooser;


import org.jdom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CommandlineAdapter extends Adapter implements IAdapter {
	public CommandlineAdapter(boolean flag) {
		this.displayFlag = flag;
	}
	protected void configureAdapter() {
		
	}
	public Document getOutputXML(Document inputXML, Document outputXML){
		return null;
	}
	public String getOutputXML(String inputXML,String outputXML){
		return null;
	}
	
	public String getOutputXML(String inputXML, int applicationID){
		return null;
	}

}
