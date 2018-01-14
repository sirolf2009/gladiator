package com.sirolf2009.gladiator.candlestickbuilder

import com.sirolf2009.commonwealth.timeseries.ICandlestick
import com.sirolf2009.commonwealth.trading.ITrade
import java.time.Duration

interface Timeframe {
	
	def boolean isPartOfCandlestick(ICandlestick candlestick, ITrade trade)
	def MutableCandlestick createNewCandlestick(ITrade trade)
	def Duration getTimeframe()
	
}