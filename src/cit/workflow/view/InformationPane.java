package cit.workflow.view;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class InformationPane extends JScrollPane {

	private JTextPane textPane=null;   
	private static InformationPane console = null;   
	public static boolean Running=false;
	  
    public static synchronized InformationPane getInstance() {   
        if (console == null) {   
            console = new InformationPane();   
        }   
        return console;   
    } 
    
    private InformationPane(){
    	textPane=  new JTextPane();
    	setViewportView(textPane);   
    }
    
    public void writeMessage(String message){
    	StyledDocument doc = (StyledDocument) textPane   
                .getDocument();   
    	try {
			doc.insertString(doc.getLength(), message, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	textPane.setCaretPosition(textPane.getDocument()   
                .getLength());   
    	
    	textPane.paintImmediately(textPane.getBounds());
    	this.paintImmediately(this.getBounds());
    }
    public void clearPane(){
    	StyledDocument doc = (StyledDocument) textPane   
                .getDocument();   
    	try {
			doc.remove(0, doc.getLength());
			textPane.setCaretPosition(0); 
			textPane.paintImmediately(textPane.getBounds());
	    	this.paintImmediately(this.getBounds());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    }
    
    
    public static void clearText(){
    	if(Running)
    		InformationPane.getInstance().clearPane();
    }
    
    public static void write(String message){
    	if(Running)
    		InformationPane.getInstance().writeMessage(message);
    }
    
    public static void writeln(String message){
    	if(Running)
    		InformationPane.getInstance().writeMessage(message+'\n');
    }
}
