package com.sirolf2009.gladiator.candlestickbuilder;

import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.gladiator.candlestickbuilder.MutableCandlestick;
import com.sirolf2009.gladiator.candlestickbuilder.Timeframe;
import com.sirolf2009.util.TimeUtil;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("all")
public class Timeframe1Min implements Timeframe {
  @Override
  public boolean isPartOfCandlestick(final ICandlestick candlestick, final ITrade trade) {
    return Long.valueOf(this.roundTo1Min(candlestick.getTimestamp()).getTime()).equals(Long.valueOf(this.roundTo1Min(trade.getPoint().getDate()).getTime()));
  }
  
  @Override
  public MutableCandlestick createNewCandlestick(final ITrade trade) {
    Date _roundTo1Min = this.roundTo1Min(trade.getPoint().getDate());
    return new MutableCandlestick(_roundTo1Min, trade);
  }
  
  public Date roundTo1Min(final Date date) {
    final Calendar cal = TimeUtil.getCalendar();
    cal.setTime(date);
    cal.clear(Calendar.MILLISECOND);
    cal.clear(Calendar.SECOND);
    return cal.getTime();
  }
  
  @Override
  public Duration getTimeframe() {
    return Duration.ofMinutes(1);
  }
}
