package com.sirolf2009.gladiator

import com.sirolf2009.commonwealth.timeseries.ICandlestick
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.gladiator.candlestickbuilder.MutableCandlestick
import com.sirolf2009.gladiator.candlestickbuilder.Timeframe
import java.util.ArrayList
import java.util.LinkedList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor class CandlestickBuilder {

	@Accessors val Timeframe timeframe
	val candlesticks = new LinkedList<ICandlestick>()
	@Accessors var MutableCandlestick current

	def void addTrade(ITrade trade) {
		if(current === null) {
			current = timeframe.createNewCandlestick(trade)
		} else {
			if(timeframe.isPartOfCandlestick(current, trade)) {
				current.receiveTrade(trade)
			} else {
				candlesticks += current.immutable()
				current = timeframe.createNewCandlestick(trade)
			}
		}
	}

	def List<ICandlestick> getCandlesticks() {
		return new ArrayList(candlesticks.size() + 1) => [
			addAll(candlesticks)
			if(current !== null) {
				add(current.immutable())
			}
		]
	}

}
