package com.sirolf2009.gladiator.parts.candlestickchart;

import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.xbase.lib.Pair;
import org.swtchart.ICustomPaintListener;
import org.swtchart.internal.PlotArea;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class Crosshair implements ICustomPaintListener {
  private final AtomicReference<Pair<Integer, Integer>> mousePos;
  
  private final PlotArea plotArea;
  
  @Override
  public boolean drawBehindSeries() {
    return false;
  }
  
  @Override
  public void paintControl(final PaintEvent e) {
    final Pair<Integer, Integer> mouse = this.mousePos.get();
    final Integer mouseX = mouse.getKey();
    final Integer mouseY = mouse.getValue();
    final GC it = e.gc;
    it.setLineStyle(SWT.LINE_DASH);
    it.drawLine((mouseX).intValue(), 0, (mouseX).intValue(), this.plotArea.getBounds().height);
    it.drawLine(0, (mouseY).intValue(), this.plotArea.getBounds().width, (mouseY).intValue());
  }
  
  public Crosshair(final AtomicReference<Pair<Integer, Integer>> mousePos, final PlotArea plotArea) {
    super();
    this.mousePos = mousePos;
    this.plotArea = plotArea;
  }
}
