package com.sirolf2009.gladiator.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.internal.series.BarSeries;
import org.swtchart.internal.series.LineSeries;

@SuppressWarnings("all")
public abstract class ChartPart {
  public final static Color red = new Color(Display.getDefault(), 255, 0, 0);
  
  public final static Color green = new Color(Display.getDefault(), 0, 255, 0);
  
  public final static Color blue = new Color(Display.getDefault(), 0, 0, 255);
  
  public final static Color white = new Color(Display.getDefault(), 255, 255, 255);
  
  public final static Color black = new Color(Display.getDefault(), 0, 0, 0);
  
  public Chart createChart(final Composite parent) {
    Chart _chart = new Chart(parent, SWT.NONE);
    final Procedure1<Chart> _function = (Chart it) -> {
      it.setBackgroundInPlotArea(ChartPart.black);
      ITitle _title = it.getTitle();
      _title.setForeground(ChartPart.white);
      ITitle _title_1 = it.getTitle();
      _title_1.setText("");
      IAxisTick _tick = this.xAxis(it).getTick();
      _tick.setForeground(ChartPart.white);
      ITitle _title_2 = this.xAxis(it).getTitle();
      _title_2.setForeground(ChartPart.white);
      ITitle _title_3 = this.xAxis(it).getTitle();
      _title_3.setText("");
      IAxisTick _tick_1 = this.yAxis(it).getTick();
      _tick_1.setForeground(ChartPart.white);
      ITitle _title_4 = this.yAxis(it).getTitle();
      _title_4.setForeground(ChartPart.white);
      ITitle _title_5 = this.yAxis(it).getTitle();
      _title_5.setText("");
    };
    return ObjectExtensions.<Chart>operator_doubleArrow(_chart, _function);
  }
  
  public LineSeries createLineSeries(final Chart chart, final String name) {
    ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.LINE, name);
    final Procedure1<LineSeries> _function = (LineSeries it) -> {
      it.setSymbolType(ILineSeries.PlotSymbolType.NONE);
    };
    return ObjectExtensions.<LineSeries>operator_doubleArrow(((LineSeries) _createSeries), _function);
  }
  
  public BarSeries createBarSeries(final Chart chart, final String name) {
    ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.BAR, name);
    return ((BarSeries) _createSeries);
  }
  
  public IAxis xAxis(final Chart chart) {
    return chart.getAxisSet().getXAxes()[0];
  }
  
  public IAxis yAxis(final Chart chart) {
    return chart.getAxisSet().getYAxes()[0];
  }
}
