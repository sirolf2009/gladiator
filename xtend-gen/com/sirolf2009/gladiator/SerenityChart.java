package com.sirolf2009.gladiator;

import com.google.common.eventbus.Subscribe;
import com.sirolf2009.commonwealth.ITick;
import com.sirolf2009.commonwealth.Tick;
import com.sirolf2009.commonwealth.indicator.line.ILineIndicator;
import com.sirolf2009.commonwealth.timeseries.IPoint;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook;
import gladiator.Activator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILegend;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.internal.series.BarSeries;
import org.swtchart.internal.series.LineSeries;

@SuppressWarnings("all")
public class SerenityChart extends Chart {
  @Data
  public static class Indicator {
    private final ILineIndicator formula;
    
    private final ILineSeries line;
    
    private final CircularFifoQueue<Date> xData;
    
    private final CircularFifoQueue<Double> yData;
    
    public Indicator(final ILineIndicator formula, final ILineSeries line, final CircularFifoQueue<Date> xData, final CircularFifoQueue<Double> yData) {
      super();
      this.formula = formula;
      this.line = line;
      this.xData = xData;
      this.yData = yData;
    }
    
    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.formula== null) ? 0 : this.formula.hashCode());
      result = prime * result + ((this.line== null) ? 0 : this.line.hashCode());
      result = prime * result + ((this.xData== null) ? 0 : this.xData.hashCode());
      result = prime * result + ((this.yData== null) ? 0 : this.yData.hashCode());
      return result;
    }
    
    @Override
    @Pure
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SerenityChart.Indicator other = (SerenityChart.Indicator) obj;
      if (this.formula == null) {
        if (other.formula != null)
          return false;
      } else if (!this.formula.equals(other.formula))
        return false;
      if (this.line == null) {
        if (other.line != null)
          return false;
      } else if (!this.line.equals(other.line))
        return false;
      if (this.xData == null) {
        if (other.xData != null)
          return false;
      } else if (!this.xData.equals(other.xData))
        return false;
      if (this.yData == null) {
        if (other.yData != null)
          return false;
      } else if (!this.yData.equals(other.yData))
        return false;
      return true;
    }
    
    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("formula", this.formula);
      b.add("line", this.line);
      b.add("xData", this.xData);
      b.add("yData", this.yData);
      return b.toString();
    }
    
    @Pure
    public ILineIndicator getFormula() {
      return this.formula;
    }
    
    @Pure
    public ILineSeries getLine() {
      return this.line;
    }
    
    @Pure
    public CircularFifoQueue<Date> getXData() {
      return this.xData;
    }
    
    @Pure
    public CircularFifoQueue<Double> getYData() {
      return this.yData;
    }
  }
  
  public final static Color red = new Color(Display.getDefault(), 255, 0, 0);
  
  public final static Color green = new Color(Display.getDefault(), 0, 255, 0);
  
  public final static Color blue = new Color(Display.getDefault(), 0, 0, 255);
  
  public final static Color white = new Color(Display.getDefault(), 255, 255, 255);
  
  public final static Color black = new Color(Display.getDefault(), 0, 0, 0);
  
  private final AtomicReference<IOrderbook> orderbook = new AtomicReference<IOrderbook>();
  
  private final List<ITrade> trades = Collections.<ITrade>synchronizedList(new ArrayList<ITrade>());
  
  private final ArrayList<ITick> ticks = new ArrayList<ITick>();
  
  private final ArrayList<SerenityChart.Indicator> indicators = new ArrayList<SerenityChart.Indicator>();
  
  public SerenityChart(final Composite parent) {
    super(parent, SWT.NONE);
    this.setBackgroundInPlotArea(SerenityChart.black);
    ITitle _title = this.getTitle();
    _title.setForeground(SerenityChart.white);
    ITitle _title_1 = this.getTitle();
    _title_1.setText("");
    IAxisTick _tick = this.xAxis(this).getTick();
    _tick.setForeground(SerenityChart.white);
    ITitle _title_2 = this.xAxis(this).getTitle();
    _title_2.setForeground(SerenityChart.white);
    ITitle _title_3 = this.xAxis(this).getTitle();
    _title_3.setText("");
    IAxisTick _tick_1 = this.yAxis(this).getTick();
    _tick_1.setForeground(SerenityChart.white);
    ITitle _title_4 = this.yAxis(this).getTitle();
    _title_4.setForeground(SerenityChart.white);
    ITitle _title_5 = this.yAxis(this).getTitle();
    _title_5.setText("");
    ILegend _legend = this.getLegend();
    _legend.setVisible(false);
    Activator.getData().register(this);
  }
  
  public SerenityChart.Indicator addIndicator(final ILineIndicator formula, final Color color, final String name) {
    CircularFifoQueue<Date> _circularFifoQueue = new CircularFifoQueue<Date>(5000);
    CircularFifoQueue<Double> _circularFifoQueue_1 = new CircularFifoQueue<Double>(5000);
    return this.addIndicator(formula, color, name, _circularFifoQueue, _circularFifoQueue_1);
  }
  
  public SerenityChart.Indicator addIndicator(final ILineIndicator formula, final Color color, final String name, final CircularFifoQueue<Date> xDates, final CircularFifoQueue<Double> yValues) {
    ILineIndicator _copy = formula.copy();
    LineSeries _createLineSeries = this.createLineSeries(this, color, name);
    SerenityChart.Indicator _indicator = new SerenityChart.Indicator(_copy, _createLineSeries, xDates, yValues);
    return this.addIndicator(_indicator);
  }
  
  public SerenityChart.Indicator addIndicator(final SerenityChart.Indicator indicator) {
    this.indicators.add(indicator);
    final Consumer<ITick> _function = (ITick it) -> {
      this.tick(it, indicator);
    };
    this.ticks.forEach(_function);
    this.adjustAxis();
    return indicator;
  }
  
  public IAxis xAxis(final Chart chart) {
    return chart.getAxisSet().getXAxes()[0];
  }
  
  public IAxis yAxis(final Chart chart) {
    return chart.getAxisSet().getYAxes()[0];
  }
  
  public void tick(final Date timestamp) {
    final List<ITrade> currentTrades = this.trades.stream().collect(Collectors.<ITrade>toList());
    this.trades.clear();
    IOrderbook _get = this.orderbook.get();
    Tick _tick = new Tick(timestamp, _get, currentTrades);
    this.tick(_tick);
  }
  
  @Subscribe
  public void tick(final ITick tick) {
    this.ticks.add(tick);
    final Consumer<SerenityChart.Indicator> _function = (SerenityChart.Indicator it) -> {
      try {
        this.tick(tick, it);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          InputOutput.<ITick>println(tick);
          e.printStackTrace();
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    };
    this.indicators.forEach(_function);
    this.adjustAxis();
  }
  
  public void tick(final ITick tick, final SerenityChart.Indicator indicator) {
    final IPoint point = indicator.formula.apply(tick);
    indicator.xData.add(point.getDate());
    indicator.yData.add(Double.valueOf(point.getY().doubleValue()));
    indicator.line.setXDateSeries(((Date[])Conversions.unwrapArray(indicator.xData, Date.class)));
    indicator.line.setYSeries(((double[])Conversions.unwrapArray(indicator.yData, double.class)));
  }
  
  public void adjustAxis() {
    final Runnable _function = () -> {
      boolean _isDisposed = this.isDisposed();
      if (_isDisposed) {
        return;
      }
      this.getAxisSet().adjustRange();
      this.redraw();
    };
    this.getDisplay().syncExec(_function);
  }
  
  public LineSeries createLineSeries(final Chart chart, final Color color, final String name) {
    ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.LINE, name);
    final Procedure1<LineSeries> _function = (LineSeries it) -> {
      it.setSymbolType(ILineSeries.PlotSymbolType.NONE);
      it.setLineColor(color);
    };
    return ObjectExtensions.<LineSeries>operator_doubleArrow(((LineSeries) _createSeries), _function);
  }
  
  public BarSeries createBarSeries(final Chart chart, final String name) {
    ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.BAR, name);
    return ((BarSeries) _createSeries);
  }
  
  public Date getFirstRunTime() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    int _get = cal.get(Calendar.SECOND);
    int _plus = (_get + 1);
    cal.set(Calendar.SECOND, _plus);
    return cal.getTime();
  }
  
  public long getPeriod() {
    return Duration.ofSeconds(1).toMillis();
  }
}
