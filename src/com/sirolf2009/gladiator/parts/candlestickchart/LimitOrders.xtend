package com.sirolf2009.gladiator.parts.candlestickchart

import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder
import java.util.List
import org.eclipse.swt.SWT
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.swtchart.IAxis
import org.swtchart.ICustomPaintListener
import org.swtchart.internal.PlotArea

@FinalFieldsConstructor class LimitOrders implements ICustomPaintListener {
	
	val Color askColor
	val Color bidColor
	val IAxis yAxis
	val PlotArea plotArea
	val List<ILimitOrder> askOrders
	val List<ILimitOrder> bidOrders
	
	override drawBehindSeries() {
		return false
	}
	
	override paintControl(PaintEvent e) {
		val it = e.gc
		lineStyle = SWT.LINE_DASHDOTDOT
		foreground = askColor
		askOrders.forEach[
			val height = yAxis.getPixelCoordinate(price.doubleValue())
			e.gc.drawLine(0, height, plotArea.bounds.width, height)
		]
		foreground = bidColor
		bidOrders.forEach[
			val height = yAxis.getPixelCoordinate(price.doubleValue())
			e.gc.drawLine(0, height, plotArea.bounds.width, height)
		]
	}
	
}