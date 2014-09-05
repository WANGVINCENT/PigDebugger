import java.awt.Shape;
import java.io.*;
import java.util.List;

import nanwang.pig.entity.AVGCounter;
import nanwang.pig.entity.DbHandler;
import nanwang.pig.entity.JobCounter;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartUtilities; 
import org.jfree.chart.plot.XYPlot;

public class LineChart {  
	
	private XYSeries points, line;
	private DbHandler dbHandler = DbHandler.getInstance();
	private List<JobCounter> joblist;
	private List<AVGCounter> avglist;
	private final static int width = 640;
	private final static int height = 480;
	private Shape cross = ShapeUtilities.createDiagonalCross(3, 1);

	public LineChart(String scriptName, int jobRankNum){
		joblist = dbHandler.readJobCounter(scriptName, jobRankNum);
		avglist = dbHandler.readAVGCounter(scriptName, jobRankNum);
	}
	
	/**
	 * Build MapCPUTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildMapCPU_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("MapCPUTimePerNode");
		line = new XYSeries("AVGMapCPUTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getMapCPUTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getMapCPUTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("MapCPUTimePerNode-ReduceNum","ReduceNum","MapCPUTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/MapCPUTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ReduceCPUTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildReduceCPU_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ReduceCPUTimePerNode");
		line = new XYSeries("AVGReduceCPUTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getReduceCPUTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getReduceCPUTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ReduceCPUTimePerNode-ReduceNum","ReduceNum","ReduceCPUTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ReduceCPUTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ReduceCPUTimePernode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildTotalCPU_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("TotalCPUTimePerNode");
		line = new XYSeries("AVGTotalCPUTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getTotalCPUTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getTotalCPUTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("EstimatedTotalCPUTimePerNode-ReduceNum","ReduceNum","EstimatedTotalCPUTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/TotalCPUTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ReduceCPUTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildTotalCPU_ShuffleBytes() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("TotalCPUTimePerNode");
		line = new XYSeries("AVGTotalCPUTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getShuffleBytes(), counter.getTotalCPUTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getShuffleBytes(), counter.getTotalCPUTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("EstimatedTotalCPUTimePerNode-ShuffleBytesPerNode","ShuffleBytesPerNode","EstimatedTotalCPUTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/TotalCPUTime-ShuffleBytes.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build MapElapsedTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildMapElapsed_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("MapElapsedTimePerNode");
		line = new XYSeries("AVGMapElapsedTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getMapElapsedTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getMapElapsedTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("MapElapsedTimePerNode-ReduceNum","ReduceNum","MapElapsedTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/MapElapsedTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ReduceElapsedTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildReduceElapsed_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ReduceElapsedTimePerNode");
		line = new XYSeries("AVGReduceElapsedTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getReduceElapsedTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getReduceElapsedTime());
		}

		my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ReduceElapsedTimePerNode-ReduceNum","ReduceNum","ReduceElapsedTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ReduceElapsedTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ShuffleBytesPerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildShuffleBytes_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ShuffleBytesPerNode");
		line = new XYSeries("AVGShuffleBytesPerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getShuffleBytes());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getShuffleBytes());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ShuffleBytesPerNode-ReduceNum","ReduceNum","ShuffleBytesPerNode(MB)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ShuffleBytes-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height); 
	}
	
	/**
	 * Build ShufflePhaseTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildShufflePhaseTime_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ShufflePhaseTimePerNode");
		line = new XYSeries("AVGShufflePhaseTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getShufflePhaseTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getShufflePhaseTime());
		}
		
        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ShufflePhaseTimePerNode-ReduceNum","ReduceNum","ShufflePhaseTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ShufflePhaseTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height);
	}
	
	/**
	 * Build SortPhaseTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildSortPhaseTime_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("SortPhaseTimePerNode");
		line = new XYSeries("AVGSortPhaseTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getSortPhaseTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getSortPhaseTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("SortPhaseTimePerNode-ReduceNum","ReduceNum","SortPhaseTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/SortPhaseTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height);
	}
	
	/**
	 * Build ReducePhaseTimePerNode - ReduceNum line chart
	 * @return
	 * @throws IOException
	 */
	public void buildReducePhaseTime_ReduceNum() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ReducePhaseTimePerNode");
		line = new XYSeries("AVGReducePhaseTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getReduceNum(), counter.getReducePhaseTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getReduceNum(), counter.getReducePhaseTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ReducePhaseTimePerNode-ReduceNum","ReduceNum","ReducePhaseTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ReducePhaseTime-ReduceNum.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height);
	}
	
	/**
	 * Build ReduceCPUTimePerNode - ShuffleBytesPerNode line chart
	 * @return
	 * @throws IOException
	 */
	public void buildReduceCPUTime_ShuffleBytes() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ReduceCPUTimePerNode");
		line = new XYSeries("AVGReduceCPUTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getShuffleBytes(), counter.getReduceCPUTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getShuffleBytes(), counter.getReduceCPUTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ReduceCPUTimePerNode-ShuffleBytesPerNode","ShuffleBytesPerNode(MB)","ReduceCPUTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ReduceCPUTime-ShuffleBytes.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height);
	}
	
	/**
	 * Build ReduceElapsedTimePerNode - ShuffleBytesPerNode line chart
	 * @return
	 * @throws IOException
	 */
	public void buildReduceElapsedTime_ShuffleBytes() throws IOException{
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		points = new XYSeries("ReduceElapsedTimePerNode");
		line = new XYSeries("AVGReduceElapsedTimePerNode");
		
		for(JobCounter counter : joblist){
			points.add(counter.getShuffleBytes(), counter.getReduceElapsedTime());
		}
		
		for(AVGCounter counter : avglist){
			line.add(counter.getShuffleBytes(), counter.getReduceElapsedTime());
		}

        my_data_series.addSeries(points);
        my_data_series.addSeries(line);
        
        JFreeChart XYLineChart = ChartFactory.createXYLineChart("ReduceElapsedTimePerNode-ShuffleBytesPerNode","ShuffleBytesPerNode(MB)","ReduceElapsedTimePerNode(s)", my_data_series, PlotOrientation.VERTICAL, true, true, false);
        
        XYPlot plot = XYLineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShape(0, cross);
        plot.setRenderer(renderer);
        
        File XYlineChart = new File("charts/ReduceElapsedTime-ShuffleBytes.png");              
        ChartUtilities.saveChartAsPNG(XYlineChart, XYLineChart, width, height);
	}
     
	public static void main(String[] args) throws IOException{
        
		String scriptName = args[0];
        int jobRankNum = Integer.valueOf(args[1]);
        
        LineChart lineChart = new LineChart(scriptName, jobRankNum);
        
    	lineChart.buildMapCPU_ReduceNum();

    	lineChart.buildReduceCPU_ReduceNum();

    	lineChart.buildMapElapsed_ReduceNum();

    	lineChart.buildReduceElapsed_ReduceNum();

    	lineChart.buildShuffleBytes_ReduceNum();

    	lineChart.buildShufflePhaseTime_ReduceNum();

    	lineChart.buildSortPhaseTime_ReduceNum();

    	lineChart.buildReducePhaseTime_ReduceNum();
    	
    	lineChart.buildReduceCPUTime_ShuffleBytes();
    	
    	lineChart.buildReduceElapsedTime_ShuffleBytes();
    	
    	lineChart.buildTotalCPU_ReduceNum();
    	
    	lineChart.buildTotalCPU_ShuffleBytes();
	}
}