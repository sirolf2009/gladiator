package com.sirolf2009.gladiator.parts.candlestickchart;

import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.internal.PlotArea;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class PriceLine implements ICustomPaintListener {
  private final Color lineColor;
  
  private final PlotArea plotArea;
  
  private final IAxis yAxis;
  
  private final AtomicReference<ICandlestick> currentCandlestick;
  
  @Override
  public boolean drawBehindSeries() {
    return false;
  }
  
  @Override
  public void paintControl(final PaintEvent e) {
    final ICandlestick current = this.currentCandlestick.get();
    if ((current != null)) {
      final GC it = e.gc;
      it.setLineStyle(SWT.LINE_DASH);
      it.setForeground(this.lineColor);
      final int height = this.yAxis.getPixelCoordinate(this.currentCandlestick.get().getClose().doubleValue());
      it.drawLine(0, height, this.plotArea.getBounds().width, height);
    }
  }
  
  public PriceLine(final Color lineColor, final PlotArea plotArea, final IAxis yAxis, final AtomicReference<ICandlestick> currentCandlestick) {
    super();
    this.lineColor = lineColor;
    this.plotArea = plotArea;
    this.yAxis = yAxis;
    this.currentCandlestick = currentCandlestick;
  }
}
