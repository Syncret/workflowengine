package cit.workflow.monitor;

/*************************
 * 测试类，与主程序与关，可删
 *************************/

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

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

public class TestChart extends ChartPanel{
	private static TimeSeries timeSeries;
	private long value = 0;
	private PerfMonitor monitor=null;

	public TestChart(String chartContent, String title, String yaxisName,TimeSeries ts) {
		super(createChart(chartContent, title, yaxisName,ts));
		monitor=new PerfMonitor();
	}

	private static JFreeChart createChart(String chartContent, String title,
			String yaxisName,TimeSeries ts) {
		timeSeries=ts;
		// 创建时序图对象
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
	
	
	public static TimeSeries list2ts(LinkedList<Object[]> perfList) {
		TimeSeries ts = new TimeSeries("");
		try {
			for (Object[] perf : perfList) {
				ts.add(new Second(new Date((long)perf[0])), (double) perf[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ts;
	}



	public static void main(String[] args) {
		JFrame frame = new JFrame("Test Chart");
		final PerfData pd=new PerfData();
		pd.setInteval(1000);
		pd.setRun(true);
		final TimeSeries ts=new TimeSeries("CPU Ratio");
		ts.addAndOrUpdate(list2ts(pd.getCpuPerfList()));
		TestChart rtcp = new TestChart("CPU Usage", "CPU Usage", "CPU Usage(%)",ts);
		frame.getContentPane().add(rtcp, new BorderLayout().CENTER);
		frame.pack();
		frame.setVisible(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int i=1000;
				while(i>0){
//					ts.addAndOrUpdate(list2ts(pd.getCpuPerfList()));
					ts.addAndOrUpdate(list2ts(pd.getMemoryPerfList()));
					i--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				System.exit(0);
			}
		});
	}
}
