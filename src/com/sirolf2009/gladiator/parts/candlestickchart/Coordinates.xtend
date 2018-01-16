package com.sirolf2009.gladiator.parts.candlestickchart

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.swt.events.PaintEvent
import org.swtchart.ICustomPaintListener
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.swtchart.IAxis

@FinalFieldsConstructor class Coordinates implements ICustomPaintListener {

	val AtomicReference<Pair<Integer, Integer>> mousePos
	val IAxis xAxis
	val IAxis yAxis

	override drawBehindSeries() {
		return false
	}

	override paintControl(PaintEvent e) {
		val mouse = mousePos.get()
		val mouseX = mouse.key
		val mouseY = mouse.value
		val it = e.gc
		drawText('''«new SimpleDateFormat("HH:mm:ss").format(new Date(xAxis.getDataCoordinate(mouseX) as long))», «yAxis.getDataCoordinate(mouseY) as int»''', 0, 0)
	}

}
