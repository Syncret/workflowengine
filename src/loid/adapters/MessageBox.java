/*
 * Created on 2004-12-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package loid.adapters;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageBox extends JDialog implements ActionListener {

	private static int emptySpace = 5;
	   private static int buttonHeight = 25;
	   private static int buttonWidth = 100;
	   private static int mainPaneHeight = 300;
	   private static int mainPaneWidth = 400;
	   private static int secondaryPaneHeight =
	                           mainPaneHeight -
	                           buttonHeight -
	                           (emptySpace * 3);
	   private static int secondaryPaneWidth =
	                           mainPaneWidth -
	                           (emptySpace * 2);
	   

	   boolean frameSizeAdjusted = false;  // Used by addNotify

	   // Declare Controls
	   JScrollPane scrollPane = new JScrollPane ();
	   JTextArea textArea = new JTextArea ();
	   JButton okButton = new JButton ("OK");

	   public MessageBox (
	         Frame parent)
	   {
	      super (parent);
	      
	      // Initialize the GUI portion of the app
	      setModal (true);
	      getContentPane ().setLayout (null);
	      setSize (mainPaneWidth, mainPaneHeight);
	      setVisible (false);
	      getContentPane ().add (scrollPane);
	      scrollPane.setBounds (
	                     emptySpace,
	                     emptySpace,
	                     secondaryPaneWidth,
	                     secondaryPaneHeight);
	      scrollPane.getViewport ().add (textArea);
	      textArea.setBounds (
	                     0,
	                     0,
	                     secondaryPaneWidth - 3,
	                     secondaryPaneHeight - 3);
	      getContentPane ().add (okButton);
	      okButton.setBounds (
	                     (mainPaneWidth - buttonWidth) / 2,
	                     mainPaneHeight - buttonHeight - emptySpace,
	                     buttonWidth,
	                     buttonHeight);
	      okButton.addActionListener (this);
	   }

	   public MessageBox ()
	   {
	      this ((Frame) null);
	   }

	   public MessageBox (
	         String sTitle)
	   {
	      this ();
	      setTitle (sTitle);
	   }

	   public MessageBox (
	         String sTitle,
	         String sMessage)
	   {
	      this ();
	      setTitle (sTitle);
	      textArea.setText (sMessage);
	   }

	   public void setVisible (
	         boolean b)
	   {
	      if (b)
	         setLocation (50, 50);
	      super.setVisible (b);
	   }

	   public void addNotify ()
	   {
	      // Record the size of the window prior to calling parents addNotify.
	      Dimension size = getSize ();

	      super.addNotify ();

	      if (frameSizeAdjusted)
	         return;
	      frameSizeAdjusted = true;

	      // Adjust size of frame according to the insets
	      Insets insets = getInsets ();
	      setSize (insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	   }

	   /**
	    * ActionListener event handler method.
	    */
	   public void actionPerformed (
	         ActionEvent event)
	   {
	      Object object = event.getSource ();
	      if (object == okButton)
	      {
	         dispose ();
	      }
	   } // ActionListener.actionPerformed ()

	   public void setText (
	         String sMessage)
	   {
	      textArea.setText (sMessage);
	   }


}
