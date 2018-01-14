package com.sirolf2009.gladiator.candlestickbuilder

import com.sirolf2009.commonwealth.timeseries.ICandlestick
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.util.TimeUtil
import java.util.Calendar
import java.util.Date
import java.time.Duration

class Timeframe15Min implements Timeframe {
	
	override isPartOfCandlestick(ICandlestick candlestick, ITrade trade) {
		return candlestick.timestamp.roundTo15Min.time.equals(trade.point.date.roundTo15Min.time)
	}
	
	override createNewCandlestick(ITrade trade) {
		return new MutableCandlestick(trade.point.date.roundTo15Min, trade)
	}
	
	def roundTo15Min(Date date) {
		val cal = TimeUtil.calendar
		cal.time = date
		cal.clear(Calendar.MILLISECOND)
		cal.clear(Calendar.SECOND)
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - (cal.get(Calendar.MINUTE) % 15))
		return cal.time
	}
	
	override getTimeframe() {
		return Duration.ofMinutes(15)
	}
	
}