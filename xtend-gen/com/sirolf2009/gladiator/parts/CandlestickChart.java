package com.sirolf2009.gladiator.parts;

import com.google.common.eventbus.Subscribe;
import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder;
import com.sirolf2009.gladiator.CandlestickBuilder;
import com.sirolf2009.gladiator.candlestickbuilder.Timeframe1Min;
import com.sirolf2009.gladiator.parts.ChartPart;
import com.sirolf2009.gladiator.parts.candlestickchart.AddOrderButton;
import com.sirolf2009.gladiator.parts.candlestickchart.Coordinates;
import com.sirolf2009.gladiator.parts.candlestickchart.Crosshair;
import com.sirolf2009.gladiator.parts.candlestickchart.LimitOrders;
import com.sirolf2009.gladiator.parts.candlestickchart.PriceLine;
import gladiator.Activator;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.service.trade.TradeService;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IErrorBar;
import org.swtchart.ILegend;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ITitle;
import org.swtchart.Range;
import org.swtchart.internal.PlotArea;
import org.swtchart.internal.series.LineSeries;

@SuppressWarnings("all")
public class CandlestickChart extends ChartPart {
  public static class CandlestickChartComponent extends Chart {
    private final CandlestickBuilder builder = new CandlestickBuilder(new Timeframe1Min());
    
    private final AtomicReference<ICandlestick> currentCandlestick = new AtomicReference<ICandlestick>();
    
    private final AtomicBoolean customRange = new AtomicBoolean(false);
    
    private final AtomicInteger customXRange = new AtomicInteger((-1));
    
    private LineSeries series;
    
    public CandlestickChartComponent(final Composite parent) {
      super(parent, SWT.NONE);
      Display _display = this.getDisplay();
      Color _color = new Color(_display, 52, 48, 52);
      this.setBackgroundInPlotArea(_color);
      ITitle _title = this.getTitle();
      _title.setForeground(ChartPart.black);
      ITitle _title_1 = this.getTitle();
      _title_1.setText("");
      IAxisTick _tick = this.xAxis().getTick();
      _tick.setForeground(ChartPart.black);
      ITitle _title_2 = this.xAxis().getTitle();
      _title_2.setForeground(ChartPart.black);
      ITitle _title_3 = this.xAxis().getTitle();
      _title_3.setText("");
      IAxisTick _tick_1 = this.yAxis().getTick();
      _tick_1.setForeground(ChartPart.black);
      ITitle _title_4 = this.yAxis().getTitle();
      _title_4.setForeground(ChartPart.black);
      ITitle _title_5 = this.yAxis().getTitle();
      _title_5.setText("Price");
      IAxis _yAxis = this.yAxis();
      _yAxis.setPosition(IAxis.Position.Secondary);
      ILegend _legend = this.getLegend();
      _legend.setVisible(false);
      Display _display_1 = this.getDisplay();
      Color _color_1 = new Color(_display_1, 159, 211, 86);
      this.series = this.createLineSeries(this, _color_1, "Close");
      IErrorBar _yErrorBar = this.series.getYErrorBar();
      _yErrorBar.setType(IErrorBar.ErrorBarType.BOTH);
      IErrorBar _yErrorBar_1 = this.series.getYErrorBar();
      Display _display_2 = this.getDisplay();
      Color _color_2 = new Color(_display_2, 114, 110, 96);
      _yErrorBar_1.setColor(_color_2);
      IErrorBar _yErrorBar_2 = this.series.getYErrorBar();
      _yErrorBar_2.setLineWidth(2);
      IErrorBar _yErrorBar_3 = this.series.getYErrorBar();
      _yErrorBar_3.setVisible(true);
      Activator.getData().register(this);
      Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(0), Integer.valueOf(0));
      final AtomicReference<Pair<Integer, Integer>> mousePos = new AtomicReference<Pair<Integer, Integer>>(_mappedTo);
      final AtomicReference<Rectangle> addOrderPos = new AtomicReference<Rectangle>();
      ArrayList<ILimitOrder> _arrayList = new ArrayList<ILimitOrder>();
      final List<ILimitOrder> askOrders = Collections.<ILimitOrder>synchronizedList(_arrayList);
      ArrayList<ILimitOrder> _arrayList_1 = new ArrayList<ILimitOrder>();
      final List<ILimitOrder> bidOrders = Collections.<ILimitOrder>synchronizedList(_arrayList_1);
      Composite _plotArea = this.getPlotArea();
      final Procedure1<PlotArea> _function = (PlotArea it) -> {
        Crosshair _crosshair = new Crosshair(mousePos, it);
        it.addCustomPaintListener(_crosshair);
        AddOrderButton _addOrderButton = new AddOrderButton(mousePos, addOrderPos, it);
        it.addCustomPaintListener(_addOrderButton);
        IAxis _xAxis = this.xAxis();
        IAxis _yAxis_1 = this.yAxis();
        Coordinates _coordinates = new Coordinates(mousePos, _xAxis, _yAxis_1);
        it.addCustomPaintListener(_coordinates);
        Display _display_3 = it.getDisplay();
        Color _color_3 = new Color(_display_3, 159, 211, 86);
        IAxis _yAxis_2 = this.yAxis();
        PriceLine _priceLine = new PriceLine(_color_3, it, _yAxis_2, this.currentCandlestick);
        it.addCustomPaintListener(_priceLine);
        Display _display_4 = it.getDisplay();
        Color _color_4 = new Color(_display_4, 255, 0, 0);
        Display _display_5 = it.getDisplay();
        Color _color_5 = new Color(_display_5, 0, 255, 0);
        IAxis _yAxis_3 = this.yAxis();
        LimitOrders _limitOrders = new LimitOrders(_color_4, _color_5, _yAxis_3, it, askOrders, bidOrders);
        it.addCustomPaintListener(_limitOrders);
      };
      ObjectExtensions.<PlotArea>operator_doubleArrow(((PlotArea) _plotArea), _function);
      final Listener _function_1 = (Event it) -> {
        Pair<Integer, Integer> _mappedTo_1 = Pair.<Integer, Integer>of(Integer.valueOf(it.x), Integer.valueOf(it.y));
        mousePos.set(_mappedTo_1);
        this.redraw();
      };
      this.getPlotArea().addListener(SWT.MouseMove, _function_1);
      final Listener _function_2 = (Event it) -> {
        try {
          final Rectangle addOrderPosition = addOrderPos.get();
          if (((addOrderPosition != null) && addOrderPosition.contains(it.x, it.y))) {
            final Double close = IterableExtensions.<Double>last(((Iterable<Double>)Conversions.doWrapArray(this.series.getYSeries())));
            final double price = this.yAxis().getDataCoordinate(it.y);
            if ((price > (close).doubleValue())) {
              LimitOrder _limitOrder = new LimitOrder(Double.valueOf(price), Double.valueOf(0.01d));
              askOrders.add(_limitOrder);
              TradeService _tradeService = Activator.getExchange().getTradeService();
              BigDecimal _valueOf = BigDecimal.valueOf(0.01);
              BigDecimal _valueOf_1 = BigDecimal.valueOf(0.01);
              Date _date = new Date();
              BigDecimal _valueOf_2 = BigDecimal.valueOf(price);
              org.knowm.xchange.dto.trade.LimitOrder _limitOrder_1 = new org.knowm.xchange.dto.trade.LimitOrder(Order.OrderType.ASK, _valueOf, _valueOf_1, CurrencyPair.BTC_EUR, "", _date, _valueOf_2);
              _tradeService.placeLimitOrder(_limitOrder_1);
            } else {
              if ((price < (close).doubleValue())) {
                LimitOrder _limitOrder_2 = new LimitOrder(Double.valueOf(price), Double.valueOf(0.01d));
                bidOrders.add(_limitOrder_2);
              }
            }
            this.redraw();
          }
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      this.getPlotArea().addListener(SWT.MouseDoubleClick, _function_2);
      final MouseWheelListener _function_3 = (MouseEvent it) -> {
        this.customRange.set(true);
        int _get = this.customXRange.get();
        boolean _tripleEquals = (_get == (-1));
        if (_tripleEquals) {
          this.customXRange.set(((List<Double>)Conversions.doWrapArray(this.series.getXSeries())).size());
        }
        this.customXRange.getAndAdd((-it.count));
        int _get_1 = this.customXRange.get();
        boolean _lessThan = (_get_1 < 2);
        if (_lessThan) {
          this.customXRange.set(2);
        }
        final Runnable _function_4 = () -> {
          boolean _isDisposed = this.isDisposed();
          if (_isDisposed) {
            return;
          }
          this.setRange();
          this.redraw();
        };
        it.display.syncExec(_function_4);
      };
      this.getPlotArea().addMouseWheelListener(_function_3);
    }
    
    @Subscribe
    public void onTrade(final ITrade trade) {
      boolean _isDisposed = this.isDisposed();
      if (_isDisposed) {
        return;
      }
      this.builder.addTrade(trade);
      final List<ICandlestick> candles = this.builder.getCandlesticks();
      this.currentCandlestick.set(this.builder.getCurrent());
      final Runnable _function = () -> {
        boolean _isDisposed_1 = this.isDisposed();
        if (_isDisposed_1) {
          return;
        }
        final Function1<ICandlestick, Date> _function_1 = (ICandlestick it) -> {
          return it.getTimestamp();
        };
        this.series.setXDateSeries(((Date[])Conversions.unwrapArray(ListExtensions.<ICandlestick, Date>map(candles, _function_1), Date.class)));
        final Function1<ICandlestick, Double> _function_2 = (ICandlestick it) -> {
          return Double.valueOf(it.getClose().doubleValue());
        };
        this.series.setYSeries(((double[])Conversions.unwrapArray(ListExtensions.<ICandlestick, Double>map(candles, _function_2), double.class)));
        IErrorBar _yErrorBar = this.series.getYErrorBar();
        final Function1<ICandlestick, Double> _function_3 = (ICandlestick it) -> {
          double _doubleValue = it.getHigh().doubleValue();
          double _doubleValue_1 = it.getClose().doubleValue();
          return Double.valueOf((_doubleValue - _doubleValue_1));
        };
        _yErrorBar.setPlusErrors(((double[])Conversions.unwrapArray(ListExtensions.<ICandlestick, Double>map(candles, _function_3), double.class)));
        IErrorBar _yErrorBar_1 = this.series.getYErrorBar();
        final Function1<ICandlestick, Double> _function_4 = (ICandlestick it) -> {
          double _doubleValue = it.getClose().doubleValue();
          double _doubleValue_1 = it.getLow().doubleValue();
          return Double.valueOf((_doubleValue - _doubleValue_1));
        };
        _yErrorBar_1.setMinusErrors(((double[])Conversions.unwrapArray(ListExtensions.<ICandlestick, Double>map(candles, _function_4), double.class)));
        this.setRange();
        this.redraw();
      };
      this.getDisplay().syncExec(_function);
    }
    
    public void setRange() {
      final List<ICandlestick> candles = this.builder.getCandlesticks();
      boolean _get = this.customRange.get();
      boolean _not = (!_get);
      if (_not) {
        final Function1<ICandlestick, Double> _function = (ICandlestick it) -> {
          return Double.valueOf(it.getLow().doubleValue());
        };
        Double _min = IterableExtensions.<Double>min(ListExtensions.<ICandlestick, Double>map(candles, _function));
        double _minus = ((_min).doubleValue() - 10);
        final Function1<ICandlestick, Double> _function_1 = (ICandlestick it) -> {
          return Double.valueOf(it.getHigh().doubleValue());
        };
        Double _max = IterableExtensions.<Double>max(ListExtensions.<ICandlestick, Double>map(candles, _function_1));
        double _plus = ((_max).doubleValue() + 10);
        final Range range = new Range(_minus, _plus);
        if ((range.lower == range.upper)) {
          this.yAxis().adjustRange();
        } else {
          IAxis _yAxis = this.yAxis();
          _yAxis.setRange(range);
        }
        this.xAxis().adjustRange();
      } else {
        int _size = ((List<Date>)Conversions.doWrapArray(this.series.getXDateSeries())).size();
        int _get_1 = this.customXRange.get();
        int _minus_1 = (_size - _get_1);
        final Date from = this.series.getXDateSeries()[Math.max(0, _minus_1)];
        IAxis _xAxis = this.xAxis();
        long _time = from.getTime();
        long _time_1 = IterableExtensions.<Date>last(((Iterable<Date>)Conversions.doWrapArray(this.series.getXDateSeries()))).getTime();
        long _millis = this.builder.getTimeframe().getTimeframe().toMillis();
        long _plus_1 = (_time_1 + _millis);
        Range _range = new Range(_time, _plus_1);
        _xAxis.setRange(_range);
        int _size_1 = candles.size();
        int _get_2 = this.customXRange.get();
        int _minus_2 = (_size_1 - _get_2);
        final List<ICandlestick> visibleCandles = candles.subList(Math.max(0, _minus_2), candles.size());
        final Function1<ICandlestick, Double> _function_2 = (ICandlestick it) -> {
          return Double.valueOf(it.getLow().doubleValue());
        };
        Double _min_1 = IterableExtensions.<Double>min(ListExtensions.<ICandlestick, Double>map(visibleCandles, _function_2));
        double _minus_3 = ((_min_1).doubleValue() - 10);
        final Function1<ICandlestick, Double> _function_3 = (ICandlestick it) -> {
          return Double.valueOf(it.getHigh().doubleValue());
        };
        Double _max_1 = IterableExtensions.<Double>max(ListExtensions.<ICandlestick, Double>map(visibleCandles, _function_3));
        double _plus_2 = ((_max_1).doubleValue() + 10);
        final Range range_1 = new Range(_minus_3, _plus_2);
        if ((range_1.lower == range_1.upper)) {
          this.yAxis().adjustRange();
        } else {
          IAxis _yAxis_1 = this.yAxis();
          _yAxis_1.setRange(range_1);
        }
      }
    }
    
    public LineSeries createLineSeries(final Chart chart, final Color color, final String name) {
      ISeries _createSeries = chart.getSeriesSet().createSeries(ISeries.SeriesType.LINE, name);
      final Procedure1<LineSeries> _function = (LineSeries it) -> {
        it.setSymbolType(ILineSeries.PlotSymbolType.NONE);
        it.setLineColor(color);
      };
      return ObjectExtensions.<LineSeries>operator_doubleArrow(((LineSeries) _createSeries), _function);
    }
    
    public IAxis xAxis(final Chart chart) {
      return chart.getAxisSet().getXAxes()[0];
    }
    
    public IAxis xAxis() {
      return this.getAxisSet().getXAxes()[0];
    }
    
    public IAxis yAxis(final Chart chart) {
      return chart.getAxisSet().getYAxes()[0];
    }
    
    public IAxis yAxis() {
      return this.getAxisSet().getYAxes()[0];
    }
  }
  
  private CandlestickChart.CandlestickChartComponent chart;
  
  @PostConstruct
  public CandlestickChart.CandlestickChartComponent createPartControl(final Composite parent) {
    CandlestickChart.CandlestickChartComponent _candlestickChartComponent = new CandlestickChart.CandlestickChartComponent(parent);
    return this.chart = _candlestickChartComponent;
  }
  
  @Focus
  public boolean setFocus() {
    return this.chart.setFocus();
  }
}
