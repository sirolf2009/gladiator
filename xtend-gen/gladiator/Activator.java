package gladiator;

import akka.actor.Terminated;
import com.google.common.base.Objects;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirolf2009.bitfinex.wss.event.OnDisconnected;
import com.sirolf2009.bitfinex.wss.event.OnSubscribed;
import com.sirolf2009.commonwealth.ITick;
import com.sirolf2009.commonwealth.Tick;
import com.sirolf2009.commonwealth.timeseries.Point;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook;
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.Orderbook;
import com.sirolf2009.gladiator.DataRetriever;
import com.sirolf2009.serenity.collector.Collector;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import io.reactivex.functions.Action;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trade;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import scala.concurrent.Future;

@SuppressWarnings("all")
public class Activator implements BundleActivator {
  private final static AtomicBoolean shouldReconnect = new AtomicBoolean(true);
  
  private final static List<ITrade> tickTrades = Collections.<ITrade>synchronizedList(new ArrayList<ITrade>());
  
  private final static AtomicReference<IOrderbook> tickOrderbook = new AtomicReference<IOrderbook>();
  
  private final static AtomicBoolean isPriming = new AtomicBoolean(true);
  
  private final static LinkedList<Callable<Void>> dataQueue = new LinkedList<Callable<Void>>();
  
  private final static EventBus data = new EventBus();
  
  private static Activator instance;
  
  private static BundleContext context;
  
  private static StreamingExchange exchange;
  
  @Override
  public void start(final BundleContext bundleContext) throws Exception {
    Activator.instance = this;
    Activator.context = bundleContext;
    final Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      final TimerTask _this = this;
      @Override
      public void run() {
        final Runnable _function = () -> {
          long _scheduledExecutionTime = this.scheduledExecutionTime();
          final Date timestamp = new Date(_scheduledExecutionTime);
          final List<ITrade> currentTrades = Activator.tickTrades.stream().collect(Collectors.<ITrade>toList());
          Activator.tickTrades.clear();
          final IOrderbook orderbook = Activator.tickOrderbook.get();
          if ((orderbook != null)) {
            Tick _tick = new Tick(timestamp, orderbook, currentTrades);
            Activator.data.post(_tick);
          }
        };
        new Thread(_function).start();
      }
    }, this.getFirstRunTime(), this.getPeriod());
    final Runnable _function = () -> {
      try {
        final DataRetriever retriever = new DataRetriever();
        long _currentTimeMillis = System.currentTimeMillis();
        long _millis = Duration.ofDays(1).toMillis();
        long _minus = (_currentTimeMillis - _millis);
        Date _date = new Date(_minus);
        long _currentTimeMillis_1 = System.currentTimeMillis();
        long _millis_1 = Duration.ofMinutes(30).toMillis();
        long _plus = (_currentTimeMillis_1 + _millis_1);
        Date _date_1 = new Date(_plus);
        final Consumer<ITrade> _function_1 = (ITrade it) -> {
          Activator.data.post(it);
        };
        retriever.getData(_date, _date_1).forEach(_function_1);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          e.printStackTrace();
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      Activator.isPriming.set(false);
      final Consumer<Callable<Void>> _function_2 = (Callable<Void> it) -> {
        try {
          it.call();
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      Activator.dataQueue.forEach(_function_2);
    };
    new Thread(_function).start();
    this.connect();
  }
  
  public void connect() {
    final ExchangeSpecification spec = new GDAXStreamingExchange().getDefaultExchangeSpecification();
    Activator.exchange = StreamingExchangeFactory.INSTANCE.createExchange(spec);
    final Action _function = () -> {
      final io.reactivex.functions.Consumer<Trade> _function_1 = (Trade it) -> {
        long _time = it.getTimestamp().getTime();
        double _doubleValue = it.getPrice().doubleValue();
        Point _point = new Point(Long.valueOf(_time), Double.valueOf(_doubleValue));
        double _xifexpression = (double) 0;
        Order.OrderType _type = it.getType();
        boolean _equals = Objects.equal(_type, Order.OrderType.BID);
        if (_equals) {
          _xifexpression = it.getOriginalAmount().doubleValue();
        } else {
          double _doubleValue_1 = it.getOriginalAmount().doubleValue();
          _xifexpression = (-_doubleValue_1);
        }
        final com.sirolf2009.commonwealth.trading.Trade trade = new com.sirolf2009.commonwealth.trading.Trade(_point, Double.valueOf(_xifexpression));
        this.onTrade(trade);
      };
      Activator.exchange.getStreamingMarketDataService().getTrades(CurrencyPair.BTC_EUR).subscribe(_function_1);
    };
    Activator.exchange.connect().subscribe(_function);
  }
  
  @Subscribe
  public void onSubscribed(final OnSubscribed onSubscribed) {
    onSubscribed.getEventBus().register(this);
  }
  
  @Subscribe
  public void onDisconnected(final OnDisconnected onDisconnected) {
    boolean _get = Activator.shouldReconnect.get();
    if (_get) {
      this.connect();
    } else {
    }
  }
  
  @Subscribe
  public void onTrade(final ITrade trade) {
    boolean _get = Activator.isPriming.get();
    boolean _not = (!_get);
    if (_not) {
      try {
        Activator.data.post(trade);
        Activator.tickTrades.add(trade);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          e.printStackTrace();
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    } else {
      Activator.dataQueue.add(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          try {
            Activator.data.post(trade);
            Activator.tickTrades.add(trade);
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              e.printStackTrace();
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          return null;
        }
      });
    }
  }
  
  @Subscribe
  public void onOrderbook(final IOrderbook orderbook) {
    boolean _get = Activator.isPriming.get();
    boolean _not = (!_get);
    if (_not) {
      try {
        Activator.data.post(orderbook);
        Activator.tickOrderbook.set(orderbook);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          e.printStackTrace();
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      Activator.dataQueue.add(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          try {
            Activator.data.post(orderbook);
            Activator.tickOrderbook.set(orderbook);
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              e.printStackTrace();
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          return null;
        }
      });
    }
  }
  
  @Override
  public void stop(final BundleContext bundleContext) throws Exception {
    Activator.context = null;
    Activator.shouldReconnect.set(false);
    Activator.exchange.disconnect();
  }
  
  public static String getConfiguration(final IPreferenceStore preferences, final String name, final String defaultValue) {
    final String conf = preferences.getString(name);
    boolean _isEmpty = conf.isEmpty();
    if (_isEmpty) {
      return defaultValue;
    } else {
      return conf;
    }
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
  
  public static BundleContext getContext() {
    return Activator.context;
  }
  
  public static EventBus getData() {
    return Activator.data;
  }
  
  public static StreamingExchange getExchange() {
    return Activator.exchange;
  }
  
  public static Activator getDefault() {
    return Activator.instance;
  }
  
  public static List<IOrderbook> getOrderbooksFromApi(final String host) {
    try {
      List<IOrderbook> _xblockexpression = null;
      {
        final URL url = new URL("http", host, "/orderbook");
        final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd\'T\'HH:mm:ssX").create();
        final JsonArray array = gson.<JsonArray>fromJson(IOUtils.toString(url, Charset.defaultCharset()), JsonArray.class);
        final Function1<JsonElement, IOrderbook> _function = (JsonElement it) -> {
          final JsonObject orderbook = it.getAsJsonObject();
          final Date date = gson.<Date>fromJson(orderbook.get("timestamp"), Date.class);
          final Function1<JsonElement, ILimitOrder> _function_1 = (JsonElement it_1) -> {
            final double price = it_1.getAsJsonObject().getAsJsonPrimitive("price").getAsDouble();
            final double amount = it_1.getAsJsonObject().getAsJsonPrimitive("amount").getAsDouble();
            LimitOrder _limitOrder = new LimitOrder(Double.valueOf(price), Double.valueOf(amount));
            return ((ILimitOrder) _limitOrder);
          };
          final Function1<ILimitOrder, Double> _function_2 = (ILimitOrder it_1) -> {
            return Double.valueOf(it_1.getPrice().doubleValue());
          };
          final List<ILimitOrder> asks = IterableExtensions.<ILimitOrder>toList(IterableExtensions.<ILimitOrder, Double>sortBy(IterableExtensions.<JsonElement, ILimitOrder>map(orderbook.getAsJsonArray("asks"), _function_1), _function_2));
          final Function1<JsonElement, ILimitOrder> _function_3 = (JsonElement it_1) -> {
            final double price = it_1.getAsJsonObject().getAsJsonPrimitive("price").getAsDouble();
            final double amount = it_1.getAsJsonObject().getAsJsonPrimitive("amount").getAsDouble();
            LimitOrder _limitOrder = new LimitOrder(Double.valueOf(price), Double.valueOf(amount));
            return ((ILimitOrder) _limitOrder);
          };
          final Function1<ILimitOrder, Double> _function_4 = (ILimitOrder it_1) -> {
            return Double.valueOf(it_1.getPrice().doubleValue());
          };
          final List<ILimitOrder> bids = IterableExtensions.<ILimitOrder>toList(ListExtensions.<ILimitOrder>reverse(IterableExtensions.<ILimitOrder, Double>sortBy(IterableExtensions.<JsonElement, ILimitOrder>map(orderbook.getAsJsonArray("bids"), _function_3), _function_4)));
          Orderbook _orderbook = new Orderbook(date, asks, bids);
          return ((IOrderbook) _orderbook);
        };
        final Function1<IOrderbook, Date> _function_1 = (IOrderbook it) -> {
          return it.getTimestamp();
        };
        _xblockexpression = IterableExtensions.<IOrderbook, Date>sortBy(IterableExtensions.<JsonElement, IOrderbook>map(array, _function), _function_1);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static List<IOrderbook> getOrderbookData() {
    try {
      List<IOrderbook> _xblockexpression = null;
      {
        final AtomicLong linecounter = new AtomicLong((-1));
        final Function1<String, String[]> _function = (String it) -> {
          return it.split(",");
        };
        final Function1<String[], IOrderbook> _function_1 = (String[] it) -> {
          IOrderbook _xtrycatchfinallyexpression = null;
          try {
            IOrderbook _xblockexpression_1 = null;
            {
              linecounter.incrementAndGet();
              int _size = ((List<String>)Conversions.doWrapArray(it)).size();
              final Function1<Integer, Pair<String, ILimitOrder>> _function_2 = (Integer i) -> {
                Pair<String, ILimitOrder> _xtrycatchfinallyexpression_1 = null;
                try {
                  Pair<String, ILimitOrder> _xblockexpression_2 = null;
                  {
                    final String[] data = it[(i).intValue()].split(":");
                    final double price = Double.parseDouble(data[1]);
                    final double amount = Double.parseDouble(data[2]);
                    String _get = data[0];
                    LimitOrder _limitOrder = new LimitOrder(Double.valueOf(price), Double.valueOf(amount));
                    _xblockexpression_2 = Pair.<String, ILimitOrder>of(_get, ((ILimitOrder) _limitOrder));
                  }
                  _xtrycatchfinallyexpression_1 = _xblockexpression_2;
                } catch (final Throwable _t) {
                  if (_t instanceof Exception) {
                    final Exception e = (Exception)_t;
                    String _get = it[(i).intValue()];
                    String _plus = ((("Failed to parse column " + i) + ": ") + _get);
                    throw new RuntimeException(_plus, e);
                  } else {
                    throw Exceptions.sneakyThrow(_t);
                  }
                }
                return _xtrycatchfinallyexpression_1;
              };
              final Function1<Pair<String, ILimitOrder>, String> _function_3 = (Pair<String, ILimitOrder> it_1) -> {
                return it_1.getKey();
              };
              final Map<String, List<Pair<String, ILimitOrder>>> orders = IterableExtensions.<String, Pair<String, ILimitOrder>>groupBy(IterableExtensions.<Integer, Pair<String, ILimitOrder>>map(new ExclusiveRange(1, _size, true), _function_2), _function_3);
              long _parseLong = Long.parseLong(it[0]);
              Date _date = new Date(_parseLong);
              final Function1<Pair<String, ILimitOrder>, ILimitOrder> _function_4 = (Pair<String, ILimitOrder> it_1) -> {
                return it_1.getValue();
              };
              final Function1<ILimitOrder, Double> _function_5 = (ILimitOrder it_1) -> {
                return Double.valueOf(it_1.getPrice().doubleValue());
              };
              List<ILimitOrder> _list = IterableExtensions.<ILimitOrder>toList(IterableExtensions.<ILimitOrder, Double>sortBy(ListExtensions.<Pair<String, ILimitOrder>, ILimitOrder>map(orders.get("ask"), _function_4), _function_5));
              final Function1<Pair<String, ILimitOrder>, ILimitOrder> _function_6 = (Pair<String, ILimitOrder> it_1) -> {
                return it_1.getValue();
              };
              final Function1<ILimitOrder, Double> _function_7 = (ILimitOrder it_1) -> {
                return Double.valueOf(it_1.getPrice().doubleValue());
              };
              List<ILimitOrder> _list_1 = IterableExtensions.<ILimitOrder>toList(IterableExtensions.<ILimitOrder, Double>sortBy(ListExtensions.<Pair<String, ILimitOrder>, ILimitOrder>map(orders.get("bid"), _function_6), _function_7));
              Orderbook _orderbook = new Orderbook(_date, _list, _list_1);
              _xblockexpression_1 = ((IOrderbook) _orderbook);
            }
            _xtrycatchfinallyexpression = _xblockexpression_1;
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              long _get = linecounter.get();
              String _plus = ("Failed to parse line " + Long.valueOf(_get));
              String _plus_1 = (_plus + " ");
              List<String> _list = IterableExtensions.<String>toList(((Iterable<String>)Conversions.doWrapArray(it)));
              String _plus_2 = (_plus_1 + _list);
              throw new RuntimeException(_plus_2, e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          return _xtrycatchfinallyexpression;
        };
        _xblockexpression = ListExtensions.<String[], IOrderbook>map(ListExtensions.<String, String[]>map(Files.readAllLines(Paths.get("/home/sirolf2009/git/serenity/orderbook.csv")), _function), _function_1);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static Future<Terminated> getSerenityData(final Consumer<ITick> tickConsumer) {
    long _currentTimeMillis = System.currentTimeMillis();
    long _millis = Duration.ofMinutes(15).toMillis();
    long _minus = (_currentTimeMillis - _millis);
    Date _date = new Date(_minus);
    Date _date_1 = new Date();
    return Collector.getData("serenity", _date, _date_1, tickConsumer);
  }
}
