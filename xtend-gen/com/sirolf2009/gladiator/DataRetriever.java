package com.sirolf2009.gladiator;

import com.google.common.collect.Iterables;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.sirolf2009.commonwealth.ITick;
import com.sirolf2009.commonwealth.Tick;
import com.sirolf2009.commonwealth.timeseries.Point;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.commonwealth.trading.Trade;
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook;
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder;
import com.sirolf2009.commonwealth.trading.orderbook.Orderbook;
import com.sirolf2009.util.TimeUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class DataRetriever implements AutoCloseable {
  private final JSch jsch;
  
  private final Session session;
  
  private final ChannelSftp channel;
  
  public DataRetriever() {
    try {
      JSch _jSch = new JSch();
      this.jsch = _jSch;
      this.jsch.addIdentity("~/.ssh/id_rsa");
      this.session = this.jsch.getSession("root", "serenity");
      this.session.setConfig("StrictHostKeyChecking", "no");
      this.session.connect();
      Channel _openChannel = this.session.openChannel("sftp");
      this.channel = ((ChannelSftp) _openChannel);
      this.channel.connect();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Iterable<ITrade> getData(final Date from, final Date to) {
    final Function1<Date, Optional<ArrayList<ITrade>>> _function = (Date it) -> {
      return this.getData(it);
    };
    final Function1<Optional<ArrayList<ITrade>>, Boolean> _function_1 = (Optional<ArrayList<ITrade>> it) -> {
      return Boolean.valueOf(it.isPresent());
    };
    final Function1<Optional<ArrayList<ITrade>>, ArrayList<ITrade>> _function_2 = (Optional<ArrayList<ITrade>> it) -> {
      return it.get();
    };
    return Iterables.<ITrade>concat(IterableExtensions.<Optional<ArrayList<ITrade>>, ArrayList<ITrade>>map(IterableExtensions.<Optional<ArrayList<ITrade>>>filter(ListExtensions.<Date, Optional<ArrayList<ITrade>>>map(TimeUtil.getPointsToDate(this.roundTo15Min(from), this.roundTo15Min(to), 15), _function), _function_1), _function_2));
  }
  
  public Optional<ArrayList<ITrade>> getData(final Date timestamp) {
    final ArrayList<ITick> ticks = new ArrayList<ITick>();
    final ArrayList<ITrade> trades = new ArrayList<ITrade>();
    try {
      final Consumer<String> _function = (String it) -> {
        boolean _startsWith = it.startsWith("o");
        if (_startsWith) {
          final IOrderbook orderbook = this.parseOrderbook(it);
          Date _timestamp = orderbook.getTimestamp();
          List<ITrade> _collect = trades.stream().collect(Collectors.<ITrade>toList());
          Tick _tick = new Tick(_timestamp, orderbook, _collect);
          ticks.add(_tick);
          trades.clear();
        } else {
          ITrade _parseTrade = this.parseTrade(it);
          trades.add(_parseTrade);
        }
      };
      this.getLines(this.getFile(timestamp)).forEach(_function);
      return Optional.<ArrayList<ITrade>>of(trades);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        return Optional.<ArrayList<ITrade>>empty();
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  public List<String> getLines(final File file) {
    try {
      final InputStream in = this.channel.get(file.toString());
      InputStreamReader _inputStreamReader = new InputStreamReader(in);
      final BufferedReader reader = new BufferedReader(_inputStreamReader);
      final List<String> lines = reader.lines().collect(Collectors.<String>toList());
      reader.close();
      in.close();
      return lines;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public IOrderbook parseOrderbook(final String line) {
    try {
      final String[] it = line.split(",");
      long _parseLong = Long.parseLong(it[1]);
      final Date timestamp = new Date(_parseLong);
      final Function1<String, ILimitOrder> _function = (String it_1) -> {
        try {
          final String[] it_2 = it_1.split(":");
          double _parseDouble = Double.parseDouble(it_2[0]);
          double _parseDouble_1 = Double.parseDouble(it_2[1]);
          LimitOrder _limitOrder = new LimitOrder(Double.valueOf(_parseDouble), Double.valueOf(_parseDouble_1));
          return ((ILimitOrder) _limitOrder);
        } catch (final Throwable _t) {
          if (_t instanceof Exception) {
            final Exception e = (Exception)_t;
            e.printStackTrace();
            return null;
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      };
      final Function1<ILimitOrder, Boolean> _function_1 = (ILimitOrder it_1) -> {
        return Boolean.valueOf((it_1 != null));
      };
      final List<ILimitOrder> asks = IterableExtensions.<ILimitOrder>toList(IterableExtensions.<ILimitOrder>filter(ListExtensions.<String, ILimitOrder>map(((List<String>)Conversions.doWrapArray(it[2].split(";"))), _function), _function_1));
      final Function1<String, ILimitOrder> _function_2 = (String it_1) -> {
        try {
          final String[] it_2 = it_1.split(":");
          double _parseDouble = Double.parseDouble(it_2[0]);
          double _parseDouble_1 = Double.parseDouble(it_2[1]);
          LimitOrder _limitOrder = new LimitOrder(Double.valueOf(_parseDouble), Double.valueOf(_parseDouble_1));
          return ((ILimitOrder) _limitOrder);
        } catch (final Throwable _t) {
          if (_t instanceof Exception) {
            final Exception e = (Exception)_t;
            e.printStackTrace();
            return null;
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      };
      final Function1<ILimitOrder, Boolean> _function_3 = (ILimitOrder it_1) -> {
        return Boolean.valueOf((it_1 != null));
      };
      final List<ILimitOrder> bids = IterableExtensions.<ILimitOrder>toList(IterableExtensions.<ILimitOrder>filter(ListExtensions.<String, ILimitOrder>map(((List<String>)Conversions.doWrapArray(it[3].split(";"))), _function_2), _function_3));
      Orderbook _orderbook = new Orderbook(timestamp, asks, bids);
      return ((IOrderbook) _orderbook);
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
  
  public ITrade parseTrade(final String line) {
    final String[] it = line.split(",");
    long _parseLong = Long.parseLong(it[1]);
    double _parseDouble = Double.parseDouble(it[2]);
    Point _point = new Point(Long.valueOf(_parseLong), Double.valueOf(_parseDouble));
    double _parseDouble_1 = Double.parseDouble(it[3]);
    Trade _trade = new Trade(_point, Double.valueOf(_parseDouble_1));
    return ((ITrade) _trade);
  }
  
  public File getFile(final Date timestamp) {
    return this.getFile(this.getCalendar(timestamp));
  }
  
  public File getFile(final Calendar cal) {
    File _file = new File("/BTCEUR");
    String _string = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
    final File year = new File(_file, _string);
    String _string_1 = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
    final File month = new File(year, _string_1);
    String _string_2 = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
    final File day = new File(month, _string_2);
    String _string_3 = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY)).toString();
    final File hour = new File(day, _string_3);
    String _format = TimeUtil.format(cal.getTime());
    return new File(hour, _format);
  }
  
  public Calendar getCalendar() {
    return this.roundTo15Min(Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam")));
  }
  
  public Date roundTo15Min(final Date date) {
    return this.roundTo15Min(this.getCalendar(date)).getTime();
  }
  
  public Calendar getCalendar(final Date date) {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
    cal.setTime(date);
    return cal;
  }
  
  public Calendar roundTo15Min(final Calendar cal) {
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    final int minute = this.getMinute(cal);
    cal.set(Calendar.MINUTE, (minute - (minute % 15)));
    return cal;
  }
  
  public int getMinute(final Calendar cal) {
    return cal.get(Calendar.MINUTE);
  }
  
  @Override
  public void close() throws Exception {
    this.channel.disconnect();
    this.session.disconnect();
  }
  
  public static void main(final String[] args) {
    final DataRetriever retriever = new DataRetriever();
    long _currentTimeMillis = System.currentTimeMillis();
    long _millis = Duration.ofHours(4).toMillis();
    long _minus = (_currentTimeMillis - _millis);
    Date _date = new Date(_minus);
    Date _date_1 = new Date();
    final Consumer<ITrade> _function = (ITrade it) -> {
      InputOutput.<ITrade>println(it);
    };
    retriever.getData(_date, _date_1).forEach(_function);
  }
}
