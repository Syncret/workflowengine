package cit.workflow.graph.draw;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JScrollPane;

import org.jgraph.JGraph;

public class ProcessApplet extends Applet{
	public void init()
	{
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setSize(600,400);
		GeneratePic generatePic = new GeneratePic();
		String parameterType = getParameter("parameterType");
		String parameterValue = getParameter("parameterValue");
		/*if(parameterType.equals("Integer"))
		{
			generatePic.drawProcess(new Integer(parameterValue).intValue()); 
		}else if(parameterType.equals("String"))
		{
			generatePic.drawProcess(parameterValue);
		}*/
		generatePic.drawProcess("84f6fbcb-2623-46ef-901f-2821669b972e");
		JGraph graph = generatePic.getWorkflowGraph();
		scrollPane.add(graph);
		this.add(scrollPane);
	}
}
