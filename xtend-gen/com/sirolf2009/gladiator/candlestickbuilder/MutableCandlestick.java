package com.sirolf2009.gladiator.candlestickbuilder;

import com.sirolf2009.commonwealth.timeseries.Candlestick;
import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import com.sirolf2009.commonwealth.trading.ITrade;
import java.util.Date;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@Accessors
@SuppressWarnings("all")
public class MutableCandlestick implements ICandlestick {
  private Date timestamp;
  
  private Number open;
  
  private Number high;
  
  private Number low;
  
  private Number close;
  
  public MutableCandlestick(final Date timestamp, final ITrade trade) {
    this.timestamp = timestamp;
    this.open = trade.getPrice();
    this.high = trade.getPrice();
    this.low = trade.getPrice();
    this.close = trade.getPrice();
  }
  
  public void receiveTrade(final ITrade trade) {
    double _doubleValue = trade.getPrice().doubleValue();
    double _doubleValue_1 = this.high.doubleValue();
    boolean _greaterThan = (_doubleValue > _doubleValue_1);
    if (_greaterThan) {
      this.high = trade.getPrice();
    }
    double _doubleValue_2 = trade.getPrice().doubleValue();
    double _doubleValue_3 = this.low.doubleValue();
    boolean _lessThan = (_doubleValue_2 < _doubleValue_3);
    if (_lessThan) {
      this.low = trade.getPrice();
    }
    this.close = trade.getPrice();
  }
  
  public Candlestick immutable() {
    return new Candlestick(this.timestamp, this.open, this.high, this.low, this.close);
  }
  
  @Pure
  public Date getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(final Date timestamp) {
    this.timestamp = timestamp;
  }
  
  @Pure
  public Number getOpen() {
    return this.open;
  }
  
  public void setOpen(final Number open) {
    this.open = open;
  }
  
  @Pure
  public Number getHigh() {
    return this.high;
  }
  
  public void setHigh(final Number high) {
    this.high = high;
  }
  
  @Pure
  public Number getLow() {
    return this.low;
  }
  
  public void setLow(final Number low) {
    this.low = low;
  }
  
  @Pure
  public Number getClose() {
    return this.close;
  }
  
  public void setClose(final Number close) {
    this.close = close;
  }
}
