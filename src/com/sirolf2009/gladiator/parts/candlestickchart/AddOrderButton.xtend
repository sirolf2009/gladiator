package com.sirolf2009.gladiator.parts.candlestickchart

import java.awt.Rectangle
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.swt.SWT
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Display
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.swtchart.ICustomPaintListener
import org.swtchart.internal.PlotArea

@FinalFieldsConstructor class AddOrderButton implements ICustomPaintListener {
	
	val white = new Color(Display.^default, 255, 255, 255)
	val gray = new Color(Display.^default, 120, 120, 120)
	
	val AtomicReference<Pair<Integer, Integer>> mousePos
	val AtomicReference<Rectangle> addOrderPos
	val PlotArea plotArea

	override drawBehindSeries() {
		return false
	}

	override paintControl(PaintEvent e) {
		val mouse = mousePos.get()
		val mouseY = mouse.value
		val it = e.gc
		foreground = white
		background = gray
		fillRectangle(plotArea.bounds.width - 16, mouseY - 8, 16, 16)
		addOrderPos.set(new Rectangle(plotArea.bounds.width - 16, mouseY - 8, 16, 16))
		lineStyle = SWT.LINE_SOLID
		drawLine(plotArea.bounds.width - 12, mouseY, plotArea.bounds.width - 4, mouseY)
		drawLine(plotArea.bounds.width - 8, mouseY - 4, plotArea.bounds.width - 8, mouseY + 4)
	}

}
