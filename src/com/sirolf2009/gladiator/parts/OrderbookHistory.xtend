package com.sirolf2009.gladiator.parts

import com.sirolf2009.commonwealth.timeseries.IPoint
import com.sirolf2009.commonwealth.timeseries.Point
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook
import java.time.Duration
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.annotation.PostConstruct
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.eclipse.e4.ui.di.Focus
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Composite
import org.swtchart.Chart
import org.swtchart.IAxis.Position
import org.swtchart.ILineSeries.PlotSymbolType
import org.swtchart.ISeries.SeriesType
import org.swtchart.LineStyle
import org.swtchart.Range
import org.swtchart.internal.series.LineSeries
import com.google.common.eventbus.Subscribe
import com.sirolf2009.commonwealth.ITick
import gladiator.Activator

class OrderbookHistory extends ChartPart {

	var OrderbookHistoryComponent chart

	@PostConstruct
	def createPartControl(Composite parent) {
		chart = new OrderbookHistoryComponent(parent)
	}

	@Focus
	def setFocus() {
		chart.setFocus()
	}

	static class OrderbookHistoryComponent extends Chart {

		val bufferSize = 50000
		val bidBuffer = new CircularFifoQueue<Double>(bufferSize)
		val bidAskDateBuffer = new CircularFifoQueue<Date>(bufferSize)
		val askBuffer = new CircularFifoQueue<Double>(bufferSize)
		val midBuffer = new CircularFifoQueue<IPoint>(bufferSize)
		val volumeBuffer = new CircularFifoQueue<Pair<Date, List<Pair<Double, Double>>>>(bufferSize)
		val updateInterval = Duration.ofSeconds(0).toMillis()
		var Date lastUpdate = null

		val savedColors = new HashMap<Long, Color>()
		val colors = #[
			new Color(null, 0, 0, 255),
			new Color(null, 0, 255, 255),
			new Color(null, 0, 255, 0),
			new Color(null, 255, 255, 0),
			new Color(null, 255, 0, 0)
		]
		val largeVolume = getLargeVolume()
		val stepSize = largeVolume / colors.size() - 1

		var LineSeries bid
		var LineSeries ask
		var LineSeries volume

		var zoomY = 49

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
			yAxis.title.text = "Price"
			yAxis.position = Position.Secondary
			legend.visible = false
			addMouseWheelListener[
				zoom(count / 3)
			]

			bid = createLineSeries("Bid")
			bid.symbolType = PlotSymbolType.NONE
			bid.lineWidth = 3
			bid.lineColor = green
			bid.enableStep(true)
			ask = createLineSeries("Ask")
			ask.lineWidth = 3
			ask.lineColor = red
			ask.enableStep(true)
			volume = createLineSeries("Volume")
			volume.visibleInLegend = false
			volume.lineStyle = LineStyle.NONE
			volume.symbolType = PlotSymbolType.SQUARE
			volume.symbolSize = 1

			Activator.data.register(this)
		}

		@Subscribe
		def receiveTick(ITick tick) {
			new Thread [
				tick.orderbook?.receiveOrderbook
			].start()
		}

		def receiveOrderbook(IOrderbook it) {
			if(it !== null) {
				addOrderbookToBuffer()
				val volumesX = volumeBuffer.parallelStream.flatMap [ tick |
					IntStream.range(0, tick.value.size()).parallel().mapToObj [
						tick.key
					]
				].collect(Collectors.toList())
				val volumesY = volumeBuffer.parallelStream.flatMap [ tick |
					tick.value.parallelStream.map[key]
				].collect(Collectors.toList())
				val volumesColor = volumeBuffer.parallelStream.flatMap [ tick |
					tick.value.parallelStream.map[Math.abs(value)].map [
						getGradient(longValue)
					]
				].collect(Collectors.toList())
				if(disposed) {
					return
				}
				display.syncExec [
					if(disposed) {
						return
					}
					bid.YSeries = bidBuffer
					ask.YSeries = askBuffer
					bid.XDateSeries = bidAskDateBuffer
					ask.XDateSeries = bidAskDateBuffer
					volume.XDateSeries = volumesX
					volume.YSeries = volumesY
					volume.symbolColors = volumesColor
					adjustRange()
				]
			} else {
				System.err.println("Orderbook is null")
			}
		}

		def addOrderbookToBuffer(IOrderbook it) {
			if(it !== null && (lastUpdate === null || timestamp.time - lastUpdate.time >= updateInterval)) {
				bidBuffer.add(bids.get(0).price.doubleValue())
				askBuffer.add(asks.get(0).price.doubleValue())
				bidAskDateBuffer.add(timestamp)

				volumeBuffer.add(Pair.of(timestamp, (bids.map[price.doubleValue() -> amount.doubleValue()] + asks.map[price.doubleValue() -> amount.doubleValue()]).toList()))
				midBuffer.add(new Point(timestamp.time, (bidBuffer.last + askBuffer.last) / 2))
			}
		}

		def getGradient(Long it) {
			if(!savedColors.containsKey(it)) {
				val c1 = colors.get(Math.max(Math.min((it / stepSize).intValue, colors.size() - 1), 0))
				val c2 = colors.get(Math.max(Math.min((it / stepSize + 1).intValue, colors.size() - 1), 0))
				val amt = it % stepSize / stepSize

				val r1 = c1.red
				val g1 = c1.green
				val b1 = c1.blue
				val r2 = c2.red
				val g2 = c2.green
				val b2 = c2.blue

				savedColors.put(it, new Color(display, Math.round(r1 + (r2 - r1) * amt).intValue, Math.round(g1 + (g2 - g1) * amt).intValue, Math.round(b1 + (b2 - b1) * amt).intValue))
			}
			return savedColors.get(it)
		}

		def zoom(int amount) {
			zoomY = Math.max(49, zoomY + amount)
			adjustRange()
		}

		def adjustRange() {
			if(zoomY < 50) {
				axisSet.adjustRange()
				redraw()
			} else {
				xAxis.adjustRange()
				val mid = (bidBuffer.last() + askBuffer.last()) / 2
				yAxis.range = new Range(mid - (mid / zoomY), mid + (mid / zoomY))
				redraw()
			}
		}

		def createLineSeries(Chart chart, String name) {
			return chart.seriesSet.createSeries(SeriesType.LINE, name) as LineSeries => [
				symbolType = PlotSymbolType.NONE
			]
		}

		def xAxis(Chart chart) {
			return chart.axisSet.XAxes.get(0)
		}

		def yAxis(Chart chart) {
			return chart.axisSet.YAxes.get(0)
		}

		def static getLargeVolume() {
			return 50
		}

	}

}
