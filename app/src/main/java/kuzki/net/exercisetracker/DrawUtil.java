package kuzki.net.exercisetracker;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class DrawUtil {

    public static void drawChart(List<TimedDistance> tDistance,
                                 List<TimedDistance> base,
                                 LinearLayout chartContainer,
                                 android.content.Context context) {

        if (tDistance == null
                || tDistance.size() == 0
                || base == null || base.size()==0
                || chartContainer == null
                || context == null) {
            return;
        }
        // Creating an XYSeries for Incoming series
        XYSeries baseSeries = new XYSeries("Base");
        XYSeries incomeSeries = new XYSeries("Income");

        long maxTime = 0;
        double maxDistance = 0;
        for (int i = 0; i < tDistance.size(); i++) {
            if (tDistance.get(i).getTime() > maxTime) {
                maxTime = tDistance.get(i).getTime();
            }
            if (tDistance.get(i).getDistance() > maxDistance) {
                maxDistance = tDistance.get(i).getDistance();
            }
            incomeSeries.add(tDistance.get(i).getTime(), tDistance.get(i).getDistance());
        }

        for (int i = 0; i < base.size(); i++) {
            if (base.get(i).getTime() > maxTime) {
                maxTime = base.get(i).getTime();
            }
            if (base.get(i).getDistance() > maxDistance) {
                maxDistance = base.get(i).getDistance();
            }
            baseSeries.add(base.get(i).getTime(), base.get(i).getDistance());
        }

        // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // Adding Income Series to the dataset
        dataset.addSeries(incomeSeries);
        // Adding base Series to dataset
        dataset.addSeries(baseSeries);

        // Creating XYSeriesRenderer to customize incomeSeries
        XYSeriesRenderer incomeRenderer = new XYSeriesRenderer();
        incomeRenderer.setColor(Color.CYAN); //color of the graph set to cyan
        incomeRenderer.setFillPoints(true);
        incomeRenderer.setLineWidth(2f);
        incomeRenderer.setDisplayChartValues(true);
        //setting chart value distance
        incomeRenderer.setDisplayChartValuesDistance(10);
        //setting line graph point style to circle
        incomeRenderer.setPointStyle(PointStyle.CIRCLE);
        //setting stroke of the line chart to solid
        incomeRenderer.setStroke(BasicStroke.SOLID);

        // Creating XYSeriesRenderer to customize baseSeries
        XYSeriesRenderer baseRenderer = new XYSeriesRenderer();
        baseRenderer.setColor(Color.GREEN);
        baseRenderer.setFillPoints(true);
        baseRenderer.setLineWidth(4f);
        baseRenderer.setDisplayChartValues(true);
        //setting line graph point style to circle
        baseRenderer.setPointStyle(PointStyle.SQUARE);
        //setting stroke of the line chart to solid
        baseRenderer.setStroke(BasicStroke.SOLID);

        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setXLabels(0);
        multiRenderer.setChartTitle("Current vs base Chart");
        multiRenderer.setXTitle("Time in Seconds");
        multiRenderer.setYTitle("Distance to Origin");

        /***
        * Customizing graphs
        */
        //setting text size of the title
        multiRenderer.setChartTitleTextSize(38);
        //setting text size of the axis title
        multiRenderer.setAxisTitleTextSize(34);
        //setting text size of the graph lable
        multiRenderer.setLabelsTextSize(34);
        //setting zoom buttons visiblity
        multiRenderer.setZoomButtonsVisible(false);
        //setting pan enablity which uses graph to move on both axis
        multiRenderer.setPanEnabled(false, false);
        //setting click false on graph
        multiRenderer.setClickEnabled(false);
        //setting zoom to false on both axis
        multiRenderer.setZoomEnabled(false, false);
        //setting lines to display on y axis
        multiRenderer.setShowGridY(true);
        //setting lines to display on x axis
        multiRenderer.setShowGridX(true);
        //setting legend to fit the screen size
        multiRenderer.setFitLegend(true);
        //setting displaying line on grid
        multiRenderer.setShowGrid(true);
        //setting zoom to false
        multiRenderer.setZoomEnabled(false);
        //setting external zoom functions to false
        multiRenderer.setExternalZoomEnabled(false);
        //setting displaying lines on graph to be formatted(like using graphics)
        multiRenderer.setAntialiasing(true);
        //setting to in scroll to false
        multiRenderer.setInScroll(false);
        //setting to set legend height of the graph
        multiRenderer.setLegendHeight(30);
        //setting x axis label align
        multiRenderer.setXLabelsAlign(Align.CENTER);
        //setting y axis label to align
        multiRenderer.setYLabelsAlign(Align.LEFT);
        //setting text style
        multiRenderer.setTextTypeface("sans_serif", Typeface.NORMAL);
        //setting no of values to display in y axis
        multiRenderer.setYLabels(10);

        multiRenderer.setYAxisMax(maxDistance);
        //setting used to move the graph on xaxiz to .5 to the right
        multiRenderer.setXAxisMin(0);
        //setting used to move the graph on xaxiz to .5 to the right
        multiRenderer.setXAxisMax(maxTime);
        //setting bar size or space between two bars
        //multiRenderer.setBarSpacing(0.5);
        //Setting background color of the graph to transparent
        multiRenderer.setBackgroundColor(Color.TRANSPARENT);
        //Setting margin color of the graph to transparent
        multiRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        //multiRenderer.setMarginsColor(getResources().getColor(R.color.transparent_background));
        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setScale(2f);
        //setting x axis point size
        multiRenderer.setPointSize(4f);
        //setting the margin size for the graph in the order top, left, bottom, right
        multiRenderer.setMargins(new int[]{30, 30, 30, 30});

      // for (int i = 0; i < tDistance.size(); i++) {
      //      multiRenderer.addXTextLabel(tDistance.get(i).getTime(), tDistance.get(i).getTime().toString());
      //  }

        for (Integer i = 0; i <= maxTime; i++) {
            multiRenderer.addXTextLabel(i,i.toString());
            i = i + 4;
        }

        // Adding incomeRenderer and baseRenderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        // should be same
        multiRenderer.addSeriesRenderer(incomeRenderer);
        multiRenderer.addSeriesRenderer(baseRenderer);

        //this part is used to display graph on the xml
        // LinearLayout chartContainer = (LinearLayout) findViewById(R.id.right_layout);
        //remove any views before u paint the chart
        chartContainer.removeAllViews();
        //drawing bar chart
        //  showMessageView = ChartFactory.getLineChartView(context, dataset, multiRenderer);
        //adding the view to the linearlayout
        chartContainer.addView(ChartFactory.getLineChartView(context, dataset, multiRenderer));
    }
}
