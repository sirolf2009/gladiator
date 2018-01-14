package com.sirolf2009.gladiator.parts.candlestickchart

import com.sirolf2009.commonwealth.timeseries.ICandlestick
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.swt.SWT
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.swtchart.ICustomPaintListener
import org.swtchart.internal.PlotArea
import org.swtchart.IAxis

@FinalFieldsConstructor class PriceLine implements ICustomPaintListener {

	val Color lineColor
	val PlotArea plotArea
	val IAxis yAxis
	val AtomicReference<ICandlestick> currentCandlestick

	override drawBehindSeries() {
		return false
	}

	override paintControl(PaintEvent e) {
		val current = currentCandlestick.get()
		if(current !== null) {
			val it = e.gc
			lineStyle = SWT.LINE_DASH
			foreground = lineColor
			val height = yAxis.getPixelCoordinate(currentCandlestick.get().close.doubleValue())
			drawLine(0, height, plotArea.bounds.width, height)
		}
	}

}
