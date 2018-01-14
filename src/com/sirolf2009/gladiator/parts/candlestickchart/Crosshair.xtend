package com.sirolf2009.gladiator.parts.candlestickchart

import java.util.concurrent.atomic.AtomicReference
import org.eclipse.swt.SWT
import org.eclipse.swt.events.PaintEvent
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.swtchart.ICustomPaintListener
import org.swtchart.internal.PlotArea

@FinalFieldsConstructor class Crosshair implements ICustomPaintListener {

	val AtomicReference<Pair<Integer, Integer>> mousePos
	val PlotArea plotArea

	override drawBehindSeries() {
		return false
	}

	override paintControl(PaintEvent e) {
		val mouse = mousePos.get()
		val mouseX = mouse.key
		val mouseY = mouse.value
		val it = e.gc
		lineStyle = SWT.LINE_DASH
		drawLine(mouseX, 0, mouseX, plotArea.bounds.height)
		drawLine(0, mouseY, plotArea.bounds.width, mouseY)
	}

}
