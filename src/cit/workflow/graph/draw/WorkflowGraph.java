package cit.workflow.graph.draw;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import cit.workflow.Constants;

public class WorkflowGraph extends JGraph {

	public WorkflowGraph() {
		// TODO Auto-generated constructor stub
		super();
	}

	public WorkflowGraph(GraphModel arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public WorkflowGraph(GraphModel arg0, GraphLayoutCache arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public WorkflowGraph(GraphModel arg0, BasicMarqueeHandler arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public WorkflowGraph(GraphModel arg0, GraphLayoutCache arg1,
			BasicMarqueeHandler arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}
	public String toString()
	{
		return "";
	}
	
	//sxh modified 2007
	public String generateImage(int packageID,int workflowID)
	{
		 BufferedImage img = new BufferedImage(this.getWidth(),
                 this.getHeight(),
                 BufferedImage.TYPE_INT_RGB);
		 Graphics2D graphics = img.createGraphics();
		 String filename = null;
		 this.paint(graphics);
		 try {
			 File fd = new File(Constants.GRAPH_PATH + "/images");
			 
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }
			 filename = Constants.GRAPH_PATH + "/images/"+packageID;
			 fd = new File(filename);
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }
			 filename = filename + "/" + workflowID;
			 fd = new File(filename);
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }
			filename = filename + "/" + workflowID + ".jpg";

			ImageIO.write(img, "jpg", new File(filename));
			
			filename = filename.substring(Constants.GRAPH_PATH.length() + 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filename;
	}
	public String generateImage(int packageID,int workflowID,String processID)
	{
		 BufferedImage img = new BufferedImage(this.getWidth(),
                 this.getHeight(),
                 BufferedImage.TYPE_INT_RGB);
		 Graphics2D graphics = img.createGraphics();
		 String filename = null;
		 this.paint(graphics);
		 try {
			 File fd = new File(Constants.GRAPH_PATH + "/images");
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }
			 filename = Constants.GRAPH_PATH + "/images/"+packageID;
			 fd = new File(filename);
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }
			 filename = filename + "/" + workflowID;
			 fd = new File(filename);
			 if(!fd.exists())
			 {
				 fd.mkdir();
			 }

			filename = filename + "/" + processID + ".jpg";
			ImageIO.write(img, "jpg", new File(filename));
			
			filename = filename.substring(Constants.GRAPH_PATH.length() + 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filename;

	}
	//sxh modified 2007 end

}
