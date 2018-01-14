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
public class Timeframe15Min implements Timeframe {
  @Override
  public boolean isPartOfCandlestick(final ICandlestick candlestick, final ITrade trade) {
    return Long.valueOf(this.roundTo15Min(candlestick.getTimestamp()).getTime()).equals(Long.valueOf(this.roundTo15Min(trade.getPoint().getDate()).getTime()));
  }
  
  @Override
  public MutableCandlestick createNewCandlestick(final ITrade trade) {
    Date _roundTo15Min = this.roundTo15Min(trade.getPoint().getDate());
    return new MutableCandlestick(_roundTo15Min, trade);
  }
  
  public Date roundTo15Min(final Date date) {
    final Calendar cal = TimeUtil.getCalendar();
    cal.setTime(date);
    cal.clear(Calendar.MILLISECOND);
    cal.clear(Calendar.SECOND);
    int _get = cal.get(Calendar.MINUTE);
    int _get_1 = cal.get(Calendar.MINUTE);
    int _modulo = (_get_1 % 15);
    int _minus = (_get - _modulo);
    cal.set(Calendar.MINUTE, _minus);
    return cal.getTime();
  }
  
  @Override
  public Duration getTimeframe() {
    return Duration.ofMinutes(15);
  }
}
