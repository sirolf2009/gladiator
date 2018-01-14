package com.sirolf2009.gladiator

import com.sirolf2009.commonwealth.ITick
import com.sirolf2009.commonwealth.Tick
import com.sirolf2009.commonwealth.indicator.line.ILineIndicator
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook
import java.time.Duration
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.xtend.lib.annotations.Data
import org.swtchart.Chart
import org.swtchart.ILineSeries
import org.swtchart.ILineSeries.PlotSymbolType
import org.swtchart.ISeries.SeriesType
import org.swtchart.internal.series.BarSeries
import org.swtchart.internal.series.LineSeries
import com.google.common.eventbus.Subscribe
import gladiator.Activator

class SerenityChart extends Chart {

	public static val red = new Color(Display.^default, 255, 0, 0)
	public static val green = new Color(Display.^default, 0, 255, 0)
	public static val blue = new Color(Display.^default, 0, 0, 255)
	public static val white = new Color(Display.^default, 255, 255, 255)
	public static val black = new Color(Display.^default, 0, 0, 0)

	val orderbook = new AtomicReference<IOrderbook>
	val trades = Collections.synchronizedList(new ArrayList<ITrade>())
	val ticks = new ArrayList<ITick>()
	val indicators = new ArrayList<Indicator>()

	new(Composite parent) {
		super(parent, SWT.NONE)
		backgroundInPlotArea = black
		title.foreground = white
		title.text = ""
		xAxis.tick.foreground = white
		xAxis.title.foreground = white
		xAxis.title.text = ""
		yAxis.tick.foreground = white
		yAxis.title.foreground = white
		yAxis.title.text = ""
		legend.visible = false
		Activator.data.register(this)
	}

	def addIndicator(ILineIndicator formula, Color color, String name) {
		return addIndicator(formula, color, name, new CircularFifoQueue<Date>(5000), new CircularFifoQueue<Double>(5000))
	}

	def addIndicator(ILineIndicator formula, Color color, String name, CircularFifoQueue<Date> xDates, CircularFifoQueue<Double> yValues) {
		return addIndicator(new Indicator(formula.copy, createLineSeries(color, name), xDates, yValues))
	}

	def addIndicator(Indicator indicator) {
		indicators.add(indicator)
		ticks.forEach [
			tick(it, indicator)
		]
		adjustAxis()
		return indicator
	}

	def xAxis(Chart chart) {
		return chart.axisSet.XAxes.get(0)
	}

	def yAxis(Chart chart) {
		return chart.axisSet.YAxes.get(0)
	}

	def tick(Date timestamp) {
		val currentTrades = trades.stream.collect(Collectors.toList())
		trades.clear()
		tick(new Tick(timestamp, orderbook.get(), currentTrades))
	}

	@Subscribe
	def tick(ITick tick) {
		ticks += tick
		indicators.forEach [
			try {
				tick(tick, it)
			} catch(Exception e) {
				println(tick)
				e.printStackTrace()
			}
		]
		adjustAxis()
	}

	def tick(ITick tick, Indicator indicator) {
		val point = indicator.formula.apply(tick)
		indicator.xData.add(point.date)
		indicator.yData.add(point.y.doubleValue())
		indicator.line.XDateSeries = indicator.xData
		indicator.line.YSeries = indicator.yData
	}

	def adjustAxis() {
		display.syncExec [
			if(disposed) {
				return
			}
			axisSet.adjustRange()
			redraw()
		]
	}

	def createLineSeries(Chart chart, Color color, String name) {
		return chart.seriesSet.createSeries(SeriesType.LINE, name) as LineSeries => [
			symbolType = PlotSymbolType.NONE
			lineColor = color
		]
	}

	def createBarSeries(Chart chart, String name) {
		return chart.seriesSet.createSeries(SeriesType.BAR, name) as BarSeries
	}

	def getFirstRunTime() {
		val cal = Calendar.getInstance()
		cal.set(Calendar.MILLISECOND, 0)
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 1)
		return cal.time
	}

	def getPeriod() {
		return Duration.ofSeconds(1).toMillis()
	}

	@Data static class Indicator {
		val ILineIndicator formula
		val ILineSeries line
		val CircularFifoQueue<Date> xData
		val CircularFifoQueue<Double> yData

	}

}
