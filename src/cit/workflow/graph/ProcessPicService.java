package cit.workflow.graph;

import cit.workflow.graph.draw.GeneratePic;

public class ProcessPicService {
	public String drawProcess(int workflowID)
	{
		GeneratePic generatePic = new GeneratePic();
		String fileName = generatePic.drawProcess(workflowID);
		return fileName;
	}
	public String drawProcess(String processID)
	{
		GeneratePic generatePic = new GeneratePic();
		String fileName = generatePic.drawProcess(processID);
		return fileName;
	}

}
