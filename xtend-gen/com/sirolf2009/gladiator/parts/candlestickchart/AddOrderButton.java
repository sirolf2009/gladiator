package com.sirolf2009.gladiator.parts.candlestickchart;

import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.xbase.lib.Pair;
import org.swtchart.ICustomPaintListener;
import org.swtchart.internal.PlotArea;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class AddOrderButton implements ICustomPaintListener {
  private final Color white = new Color(Display.getDefault(), 255, 255, 255);
  
  private final Color gray = new Color(Display.getDefault(), 120, 120, 120);
  
  private final AtomicReference<Pair<Integer, Integer>> mousePos;
  
  private final AtomicReference<Rectangle> addOrderPos;
  
  private final PlotArea plotArea;
  
  @Override
  public boolean drawBehindSeries() {
    return false;
  }
  
  @Override
  public void paintControl(final PaintEvent e) {
    final Pair<Integer, Integer> mouse = this.mousePos.get();
    final Integer mouseY = mouse.getValue();
    final GC it = e.gc;
    it.setForeground(this.white);
    it.setBackground(this.gray);
    it.fillRectangle((this.plotArea.getBounds().width - 16), ((mouseY).intValue() - 8), 16, 16);
    Rectangle _rectangle = new Rectangle((this.plotArea.getBounds().width - 16), ((mouseY).intValue() - 8), 16, 16);
    this.addOrderPos.set(_rectangle);
    it.setLineStyle(SWT.LINE_SOLID);
    it.drawLine((this.plotArea.getBounds().width - 12), (mouseY).intValue(), (this.plotArea.getBounds().width - 4), (mouseY).intValue());
    it.drawLine((this.plotArea.getBounds().width - 8), ((mouseY).intValue() - 4), (this.plotArea.getBounds().width - 8), ((mouseY).intValue() + 4));
  }
  
  public AddOrderButton(final AtomicReference<Pair<Integer, Integer>> mousePos, final AtomicReference<Rectangle> addOrderPos, final PlotArea plotArea) {
    super();
    this.mousePos = mousePos;
    this.addOrderPos = addOrderPos;
    this.plotArea = plotArea;
  }
}
