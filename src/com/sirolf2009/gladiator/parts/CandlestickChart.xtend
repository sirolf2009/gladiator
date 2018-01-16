package com.sirolf2009.gladiator.parts

import com.google.common.eventbus.Subscribe
import com.sirolf2009.commonwealth.timeseries.ICandlestick
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder
import com.sirolf2009.gladiator.CandlestickBuilder
import com.sirolf2009.gladiator.candlestickbuilder.Timeframe15Min
import com.sirolf2009.gladiator.parts.candlestickchart.AddOrderButton
import com.sirolf2009.gladiator.parts.candlestickchart.Coordinates
import com.sirolf2009.gladiator.parts.candlestickchart.Crosshair
import com.sirolf2009.gladiator.parts.candlestickchart.LimitOrders
import com.sirolf2009.gladiator.parts.candlestickchart.PriceLine
import gladiator.Activator
import java.awt.Rectangle
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.List
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PostConstruct
import org.eclipse.e4.ui.di.Focus
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Composite
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.Order.OrderType
import org.swtchart.Chart
import org.swtchart.IAxis.Position
import org.swtchart.IErrorBar.ErrorBarType
import org.swtchart.ILineSeries.PlotSymbolType
import org.swtchart.ISeries.SeriesType
import org.swtchart.Range
import org.swtchart.internal.PlotArea
import org.swtchart.internal.series.LineSeries

class CandlestickChart extends ChartPart {

	var CandlestickChartComponent chart

	@PostConstruct
	def createPartControl(Composite parent) {
		chart = new CandlestickChartComponent(parent)
	}

	@Focus
	def setFocus() {
		chart.setFocus()
	}

	static class CandlestickChartComponent extends Chart {

		val builder = new CandlestickBuilder(new Timeframe15Min())
		val currentCandlestick = new AtomicReference<ICandlestick>()
		val customRange = new AtomicBoolean(false)
		val customXRange = new AtomicInteger(-1)

		var LineSeries series

		new(Composite parent) {
			super(parent, SWT.NONE)
			backgroundInPlotArea = new Color(display, 52, 48, 52)
			title.foreground = black
			title.text = ""
			xAxis.tick.foreground = black
			xAxis.title.foreground = black
			xAxis.title.text = ""
			yAxis.tick.foreground = black
			yAxis.title.foreground = black
			yAxis.title.text = "Price"
			yAxis.position = Position.Secondary
			legend.visible = false

			series = createLineSeries(new Color(display, 159, 211, 86), "Close")
			series.YErrorBar.type = ErrorBarType.BOTH
			series.YErrorBar.color = new Color(display, 114, 110, 96)
			series.YErrorBar.lineWidth = 2
			series.YErrorBar.visible = true

			Activator.data.register(this)

			val mousePos = new AtomicReference<Pair<Integer, Integer>>(0 -> 0)
			val addOrderPos = new AtomicReference<Rectangle>()
			val askOrders = new AtomicReference<List<ILimitOrder>>()
			val bidOrders = new AtomicReference<List<ILimitOrder>>()
			(plotArea as PlotArea) => [
				addCustomPaintListener(new Crosshair(mousePos, it))
				addCustomPaintListener(new AddOrderButton(mousePos, addOrderPos, it))
				addCustomPaintListener(new Coordinates(mousePos, xAxis, yAxis))
				addCustomPaintListener(new PriceLine(new Color(display, 159, 211, 86), it, yAxis, currentCandlestick))
				addCustomPaintListener(new LimitOrders(new Color(display, 255, 0, 0), new Color(display, 0, 255, 0), yAxis, it, askOrders, bidOrders))
			]
			plotArea.addListener(SWT.MouseMove) [
				mousePos.set(x -> y)
				redraw()
			]
			plotArea.addListener(SWT.MouseDown) [
				try {
					val addOrderPosition = addOrderPos.get()
					if(addOrderPosition !== null && addOrderPosition.contains(x, y)) {
						val close = series.YSeries.last
						val price = yAxis.getDataCoordinate(y)
						val limitPrice = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP)
						if(price > close) {
							Activator.exchange.tradeService.placeLimitOrder(new org.knowm.xchange.dto.trade.LimitOrder(OrderType.ASK, BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.01), CurrencyPair.BTC_EUR, "", new Date(), limitPrice))
						} else if(price < close) {
							Activator.exchange.tradeService.placeLimitOrder(new org.knowm.xchange.dto.trade.LimitOrder(OrderType.BID, BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.01), CurrencyPair.BTC_EUR, "", new Date(), limitPrice))
						}
					}
				} catch(Exception e) {
					MessageDialog.openError(display.activeShell, "Error", e.message)
				}
			]
			plotArea.addMouseWheelListener [
				customRange.set(true)
				if(customXRange.get() === -1) {
					customXRange.set(series.XSeries.size())
				}
				customXRange.getAndAdd(-count)
				if(customXRange.get() < 2) {
					customXRange.set(2)
				}
				updateChart()
			]

			new Thread [
				while(true) {
					try {
						val orders = Activator.exchange.tradeService.getOpenOrders(Activator.exchange.tradeService.createOpenOrdersParams()).openOrders
						val ask = orders.filter[type == OrderType.ASK].map [
							new LimitOrder(limitPrice.doubleValue(), originalAmount.doubleValue()) as ILimitOrder
						].toList()
						val bid = orders.filter[type == OrderType.BID].map [
							new LimitOrder(limitPrice.doubleValue(), originalAmount.doubleValue()) as ILimitOrder
						].toList()
						askOrders.set(ask)
						bidOrders.set(bid)
						updateChart()
					} catch(Exception e) {
						e.printStackTrace()
					}
					try {
						Thread.sleep(1000)
					} catch(Exception e) {
						e.printStackTrace()
					}
				}
			].start()
		}

		@Subscribe
		def void onTrade(ITrade trade) {
			if(disposed) {
				return
			}
			builder.addTrade(trade)
			val candles = builder.getCandlesticks()
			currentCandlestick.set(builder.current)

			display.syncExec [
				if(disposed) {
					return
				}
				series.XDateSeries = candles.map[timestamp]
				series.YSeries = candles.map[close.doubleValue()]
				series.YErrorBar.plusErrors = candles.map[high.doubleValue - close.doubleValue]
				series.YErrorBar.minusErrors = candles.map[close.doubleValue - low.doubleValue]
				setRange()
				redraw()
			]
		}

		def updateChart() {
			display.syncExec [
				if(disposed) {
					return
				}
				setRange()
				redraw()
			]
		}

		def setRange() {
			val candles = builder.getCandlesticks()
			if(!customRange.get()) {
				val range = new Range(candles.map[low.doubleValue].min - 10, candles.map[high.doubleValue].max + 10)
				if(range.lower == range.upper) {
					yAxis.adjustRange()
				} else {
					yAxis.range = range
				}
				xAxis.adjustRange()
			} else {
				val from = series.XDateSeries.get(Math.max(0, series.XDateSeries.size() - customXRange.get()))
				xAxis.range = new Range(from.time, series.XDateSeries.last.time + builder.timeframe.timeframe.toMillis())

				val visibleCandles = candles.subList(Math.max(0, candles.size() - customXRange.get()), candles.size())
				val range = new Range(visibleCandles.map[low.doubleValue].min - 10, visibleCandles.map[high.doubleValue].max + 10)
				if(range.lower == range.upper) {
					yAxis.adjustRange()
				} else {
					yAxis.range = range
				}
			}
		}

		def createLineSeries(Chart chart, Color color, String name) {
			return chart.seriesSet.createSeries(SeriesType.LINE, name) as LineSeries => [
				symbolType = PlotSymbolType.NONE
				lineColor = color
			]
		}

		def xAxis(Chart chart) {
			return chart.axisSet.XAxes.get(0)
		}

		def xAxis() {
			return axisSet.XAxes.get(0)
		}

		def yAxis(Chart chart) {
			return chart.axisSet.YAxes.get(0)
		}

		def yAxis() {
			return axisSet.YAxes.get(0)
		}

	}

}
