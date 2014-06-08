package cit.workflow.monitor;

//RealTimeChart .java  
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class RealTimeChart extends ChartPanel implements Runnable {
	private static TimeSeries timeSeries;
	private long value = 0;
	private PerfMonitor monitor=null;

	public RealTimeChart(String chartContent, String title, String yaxisName) {
		super(createChart(chartContent, title, yaxisName));
		monitor=new PerfMonitor();
	}

	private static JFreeChart createChart(String chartContent, String title,
			String yaxisName) {
		// 创建时序图对象
		timeSeries = new TimeSeries(chartContent);
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(
				timeSeries);
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title,
				"Time(s)", yaxisName, timeseriescollection, true, true, false);
		XYPlot xyplot = jfreechart.getXYPlot();
		// x坐标设定
		ValueAxis domainAxis = xyplot.getDomainAxis();
		// 自动设置数据轴数据范围
		domainAxis.setAutoRange(true);
		// 数据轴固定数据范围 30s
		domainAxis.setFixedAutoRange(60000D);
		domainAxis.setAutoTickUnitSelection(true);

		ValueAxis rangeAxis=xyplot.getRangeAxis();
		rangeAxis.setRange(0, 100);
//		rangeAxis.setRange(0, 4048);

		return jfreechart;
	}

	public void run() {
		while (true) {
			try {
//				timeSeries.add(new Second(), getRealTimeDate());
				timeSeries.add(new Second(),monitor.getCpuRatio());
//				timeSeries.add(new Second(),monitor.getUsedMemory());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Test Chart");
		RealTimeChart rtcp = new RealTimeChart("CPU Usage", "CPU Usage", "CPU Usage(%)");
		frame.getContentPane().add(rtcp, new BorderLayout().CENTER);
		frame.pack();
		frame.setVisible(true);
		(new Thread(rtcp)).start();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				System.exit(0);
			}

		});
	}
}
