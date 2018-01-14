package com.sirolf2009.gladiator.candlestickbuilder;

import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.gladiator.candlestickbuilder.MutableCandlestick;
import java.time.Duration;

@SuppressWarnings("all")
public interface Timeframe {
  public abstract boolean isPartOfCandlestick(final ICandlestick candlestick, final ITrade trade);
  
  public abstract MutableCandlestick createNewCandlestick(final ITrade trade);
  
  public abstract Duration getTimeframe();
}
