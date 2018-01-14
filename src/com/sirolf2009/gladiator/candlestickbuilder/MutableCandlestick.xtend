package com.sirolf2009.gladiator.candlestickbuilder

import com.sirolf2009.commonwealth.timeseries.ICandlestick
import com.sirolf2009.commonwealth.trading.ITrade
import java.util.Date
import org.eclipse.xtend.lib.annotations.Accessors
import com.sirolf2009.commonwealth.timeseries.Candlestick

@Accessors class MutableCandlestick implements ICandlestick {

	var Date timestamp
	var Number open
	var Number high
	var Number low
	var Number close

	new(Date timestamp, ITrade trade) {
		this.timestamp = timestamp
		this.open = trade.price
		this.high = trade.price
		this.low = trade.price
		this.close = trade.price
	}
	
	def void receiveTrade(ITrade trade) {
		if(trade.price.doubleValue() > high.doubleValue()) {
			high = trade.price
		}
		if(trade.price.doubleValue() < low.doubleValue()) {
			low = trade.price
		}
		close = trade.price
	}
	
	def immutable() {
		return new Candlestick(timestamp, open, high, low, close)
	}
}
