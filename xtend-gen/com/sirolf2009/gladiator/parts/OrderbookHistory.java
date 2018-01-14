package com.sirolf2009.gladiator.parts;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import com.sirolf2009.commonwealth.ITick;
import com.sirolf2009.commonwealth.timeseries.IPoint;
import com.sirolf2009.commonwealth.timeseries.Point;
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook;
import com.sirolf2009.gladiator.parts.ChartPart;
import gladiator.Activator;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.DoubleExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILegend;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.internal.series.LineSeries;

@SuppressWarnings("all")
public class OrderbookHistory extends ChartPart {
  public static class OrderbookHistoryComponent extends Chart {
    private final int bufferSize = 50000;
    
    private final CircularFifoQueue<Double> bidBuffer = new CircularFifoQueue<Double>(this.bufferSize);
    
    private final CircularFifoQueue<Date> bidAskDateBuffer = new CircularFifoQueue<Date>(this.bufferSize);
    
    private final CircularFifoQueue<Double> askBuffer = new CircularFifoQueue<Double>(this.bufferSize);
    
    private final CircularFifoQueue<IPoint> midBuffer = new CircularFifoQueue<IPoint>(this.bufferSize);
    
    private final CircularFifoQueue<Pair<Date, List<Pair<Double, Double>>>> volumeBuffer = new CircularFifoQueue<Pair<Date, List<Pair<Double, Double>>>>(this.bufferSize);
    
    private final long updateInterval = Duration.ofSeconds(0).toMillis();
    
    private Date lastUpdate = null;
    
    private final HashMap<Long, Color> savedColors = new HashMap<Long, Color>();
    
    private final List<Color> colors = Collections.<Color>unmodifiableList(CollectionLiterals.<Color>newArrayList(new Color(null, 0, 0, 255), new Color(null, 0, 255, 255), new Color(null, 0, 255, 0), new Color(null, 255, 255, 0), new Color(null, 255, 0, 0)));
    
    private final int largeVolume = OrderbookHistory.OrderbookHistoryComponent.getLargeVolume();
    
    private final int stepSize = ((this.largeVolume / this.colors.size()) - 1);
    
    private LineSeries bid;
    
    private LineSeries ask;
    
    private LineSeries volume;
    
    private int zoomY = 49;
    
    public OrderbookHistoryComponent(final Composite parent) {
      super(parent, SWT.NONE);
      this.setBackgroundInPlotArea(ChartPart.black);
      ITitle _title = this.getTitle();
      _title.setForeground(ChartPart.white);
      ITitle _title_1 = this.getTitle();
      _title_1.setText("");
      IAxisTick _tick = this.xAxis(this).getTick();
      _tick.setForeground(ChartPart.white);
      ITitle _title_2 = this.xAxis(this).getTitle();
      _title_2.setForeground(ChartPart.white);
      ITitle _title_3 = this.xAxis(this).getTitle();
      _title_3.setText("");
      IAxisTick _tick_1 = this.yAxis(this).getTick();
      _tick_1.setForeground(ChartPart.white);
      ITitle _title_4 = this.yAxis(this).getTitle();
      _title_4.setForeground(ChartPart.white);
      ITitle _title_5 = this.yAxis(this).getTitle();
      _title_5.setText("Price");
      IAxis _yAxis = this.yAxis(this);
      _yAxis.setPosition(IAxis.Position.Secondary);
      ILegend _legend = this.getLegend();
      _legend.setVisible(false);
      final MouseWheelListener _function = (MouseEvent it) -> {
        this.zoom((it.count / 3));
      };
      this.addMouseWheelListener(_function);
      this.bid = this.createLineSeries(this, "Bid");
      this.bid.setSymbolType(ILineSeries.PlotSymbolType.NONE);
      this.bid.setLineWidth(3);
      this.bid.setLineColor(ChartPart.green);
      this.bid.enableStep(true);
      this.ask = this.createLineSeries(this, "Ask");
      this.ask.setLineWidth(3);
      this.ask.setLineColor(ChartPart.red);
      this.ask.enableStep(true);
      this.volume = this.createLineSeries(this, "Volume");
      this.volume.setVisibleInLegend(false);
      this.volume.setLineStyle(LineStyle.NONE);
      this.volume.setSymbolType(ILineSeries.PlotSymbolType.SQUARE);
      this.volume.setSymbolSize(1);
      Activator.getData().register(this);
    }
    
    @Subscribe
    public void receiveTick(final ITick tick) {
      final Runnable _function = () -> {
        IOrderbook _orderbook = tick.getOrderbook();
        if (_orderbook!=null) {
          this.receiveOrderbook(_orderbook);
        }
      };
      new Thread(_function).start();
    }
    
    public void receiveOrderbook(final IOrderbook it) {
      if ((it != null)) {
        this.addOrderbookToBuffer(it);
        final Function<Pair<Date, List<Pair<Double, Double>>>, Stream<Date>> _function = (Pair<Date, List<Pair<Double, Double>>> tick) -> {
          final IntFunction<Date> _function_1 = (int it_1) -> {
            return tick.getKey();
          };
          return IntStream.range(0, tick.getValue().size()).parallel().<Date>mapToObj(_function_1);
        };
        final List<Date> volumesX = this.volumeBuffer.parallelStream().<Date>flatMap(_function).collect(Collectors.<Date>toList());
        final Function<Pair<Date, List<Pair<Double, Double>>>, Stream<Double>> _function_1 = (Pair<Date, List<Pair<Double, Double>>> tick) -> {
          final Function<Pair<Double, Double>, Double> _function_2 = (Pair<Double, Double> it_1) -> {
            return it_1.getKey();
          };
          return tick.getValue().parallelStream().<Double>map(_function_2);
        };
        final List<Double> volumesY = this.volumeBuffer.parallelStream().<Double>flatMap(_function_1).collect(Collectors.<Double>toList());
        final Function<Pair<Date, List<Pair<Double, Double>>>, Stream<Color>> _function_2 = (Pair<Date, List<Pair<Double, Double>>> tick) -> {
          final Function<Pair<Double, Double>, Double> _function_3 = (Pair<Double, Double> it_1) -> {
            return Double.valueOf(Math.abs((it_1.getValue()).doubleValue()));
          };
          final Function<Double, Color> _function_4 = (Double it_1) -> {
            return this.getGradient(Long.valueOf(it_1.longValue()));
          };
          return tick.getValue().parallelStream().<Double>map(_function_3).<Color>map(_function_4);
        };
        final List<Color> volumesColor = this.volumeBuffer.parallelStream().<Color>flatMap(_function_2).collect(Collectors.<Color>toList());
        boolean _isDisposed = this.isDisposed();
        if (_isDisposed) {
          return;
        }
        final Runnable _function_3 = () -> {
          boolean _isDisposed_1 = this.isDisposed();
          if (_isDisposed_1) {
            return;
          }
          this.bid.setYSeries(((double[])Conversions.unwrapArray(this.bidBuffer, double.class)));
          this.ask.setYSeries(((double[])Conversions.unwrapArray(this.askBuffer, double.class)));
          this.bid.setXDateSeries(((Date[])Conversions.unwrapArray(this.bidAskDateBuffer, Date.class)));
          this.ask.setXDateSeries(((Date[])Conversions.unwrapArray(this.bidAskDateBuffer, Date.class)));
          this.volume.setXDateSeries(((Date[])Conversions.unwrapArray(volumesX, Date.class)));
          this.volume.setYSeries(((double[])Conversions.unwrapArray(volumesY, double.class)));
          this.volume.setSymbolColors(((Color[])Conversions.unwrapArray(volumesColor, Color.class)));
          this.adjustRange();
        };
        this.getDisplay().syncExec(_function_3);
      } else {
        System.err.println("Orderbook is null");
      }
    }
    
    public boolean addOrderbookToBuffer(final IOrderbook it) {
      boolean _xifexpression = false;
      if (((it != null) && ((this.lastUpdate == null) || ((it.getTimestamp().getTime() - this.lastUpdate.getTime()) >= this.updateInterval)))) {
        boolean _xblockexpression = false;
        {
          this.bidBuffer.add(Double.valueOf(((ILimitOrder[])Conversions.unwrapArray(it.getBids(), ILimitOrder.class))[0].getPrice().doubleValue()));
          this.askBuffer.add(Double.valueOf(((ILimitOrder[])Conversions.unwrapArray(it.getAsks(), ILimitOrder.class))[0].getPrice().doubleValue()));
          this.bidAskDateBuffer.add(it.getTimestamp());
          final Function1<ILimitOrder, Pair<Double, Double>> _function = (ILimitOrder it_1) -> {
            double _doubleValue = it_1.getPrice().doubleValue();
            double _doubleValue_1 = it_1.getAmount().doubleValue();
            return Pair.<Double, Double>of(Double.valueOf(_doubleValue), Double.valueOf(_doubleValue_1));
          };
          Iterable<Pair<Double, Double>> _map = IterableExtensions.<ILimitOrder, Pair<Double, Double>>map(it.getBids(), _function);
          final Function1<ILimitOrder, Pair<Double, Double>> _function_1 = (ILimitOrder it_1) -> {
            double _doubleValue = it_1.getPrice().doubleValue();
            double _doubleValue_1 = it_1.getAmount().doubleValue();
            return Pair.<Double, Double>of(Double.valueOf(_doubleValue), Double.valueOf(_doubleValue_1));
          };
          Iterable<Pair<Double, Double>> _map_1 = IterableExtensions.<ILimitOrder, Pair<Double, Double>>map(it.getAsks(), _function_1);
          this.volumeBuffer.add(Pair.<Date, List<Pair<Double, Double>>>of(it.getTimestamp(), IterableExtensions.<Pair<Double, Double>>toList(Iterables.<Pair<Double, Double>>concat(_map, _map_1))));
          long _time = it.getTimestamp().getTime();
          Double _last = IterableExtensions.<Double>last(this.bidBuffer);
          Double _last_1 = IterableExtensions.<Double>last(this.askBuffer);
          double _plus = DoubleExtensions.operator_plus(_last, _last_1);
          double _divide = (_plus / 2);
          Point _point = new Point(Long.valueOf(_time), Double.valueOf(_divide));
          _xblockexpression = this.midBuffer.add(_point);
        }
        _xifexpression = _xblockexpression;
      }
      return _xifexpression;
    }
    
    public Color getGradient(final Long it) {
      boolean _containsKey = this.savedColors.containsKey(it);
      boolean _not = (!_containsKey);
      if (_not) {
        int _intValue = Long.valueOf(((it).longValue() / this.stepSize)).intValue();
        int _size = this.colors.size();
        int _minus = (_size - 1);
        final Color c1 = this.colors.get(Math.max(Math.min(_intValue, _minus), 0));
        int _intValue_1 = Long.valueOf((((it).longValue() / this.stepSize) + 1)).intValue();
        int _size_1 = this.colors.size();
        int _minus_1 = (_size_1 - 1);
        final Color c2 = this.colors.get(Math.max(Math.min(_intValue_1, _minus_1), 0));
        final long amt = (((it).longValue() % this.stepSize) / this.stepSize);
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        final int r2 = c2.getRed();
        final int g2 = c2.getGreen();
        final int b2 = c2.getBlue();
        Display _display = this.getDisplay();
        int _intValue_2 = Integer.valueOf(Math.round((r1 + ((r2 - r1) * amt)))).intValue();
        int _intValue_3 = Integer.valueOf(Math.round((g1 + ((g2 - g1) * amt)))).intValue();
        int _intValue_4 = Integer.valueOf(Math.round((b1 + ((b2 - b1) * amt)))).intValue();
        Color _color = new Color(_display, _intValue_2, _intValue_3, _intValue_4);
        this.savedColors.put(it, _color);
      }
      return this.savedColors.get(it);
    }
    
    public void zoom(final int amount) {
      this.zoomY = Math.max(49, (this.zoomY + amount));
      this.adjustRange();
    }
    
    public void adjustRange() {
      if ((this.zoomY < 50)) {
        this.getAxisSet().adjustRange();
        this.redraw();
      } else {
        this.xAxis(this).adjustRange();
        Double _last = IterableExtensions.<Double>last(this.bidBuffer);
        Double _last_1 = IterableExtensions.<Double>last(this.askBuffer);
        double _plus = DoubleExtensions.operator_plus(_last, _last_1);
        final double mid = (_plus / 2);
        IAxis _yAxis = this.yAxis(this);
        Range _range = new Range((mid - (mid / this.zoomY)), (mid + (mid / this.zoomY)));
        _yAxis.setRange(_range);
        this.redraw();
      }
    }
    
    public LineSeries createLineSeries(final Chart chart, final String name) {
      ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.LINE, name);
      final Procedure1<LineSeries> _function = (LineSeries it) -> {
        it.setSymbolType(ILineSeries.PlotSymbolType.NONE);
      };
      return ObjectExtensions.<LineSeries>operator_doubleArrow(((LineSeries) _createSeries), _function);
    }
    
    public IAxis xAxis(final Chart chart) {
      return chart.getAxisSet().getXAxes()[0];
    }
    
    public IAxis yAxis(final Chart chart) {
      return chart.getAxisSet().getYAxes()[0];
    }
    
    public static int getLargeVolume() {
      return 50;
    }
  }
  
  private OrderbookHistory.OrderbookHistoryComponent chart;
  
  @PostConstruct
  public OrderbookHistory.OrderbookHistoryComponent createPartControl(final Composite parent) {
    OrderbookHistory.OrderbookHistoryComponent _orderbookHistoryComponent = new OrderbookHistory.OrderbookHistoryComponent(parent);
    return this.chart = _orderbookHistoryComponent;
  }
  
  @Focus
  public boolean setFocus() {
    return this.chart.setFocus();
  }
}
