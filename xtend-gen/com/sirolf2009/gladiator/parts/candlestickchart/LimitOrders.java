package com.sirolf2009.gladiator.parts.candlestickchart;

import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder;
import java.util.List;
import java.util.function.Consumer;
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
public class LimitOrders implements ICustomPaintListener {
  private final Color askColor;
  
  private final Color bidColor;
  
  private final IAxis yAxis;
  
  private final PlotArea plotArea;
  
  private final List<ILimitOrder> askOrders;
  
  private final List<ILimitOrder> bidOrders;
  
  @Override
  public boolean drawBehindSeries() {
    return false;
  }
  
  @Override
  public void paintControl(final PaintEvent e) {
    final GC it = e.gc;
    it.setLineStyle(SWT.LINE_DASHDOTDOT);
    it.setForeground(this.askColor);
    final Consumer<ILimitOrder> _function = (ILimitOrder it_1) -> {
      final int height = this.yAxis.getPixelCoordinate(it_1.getPrice().doubleValue());
      e.gc.drawLine(0, height, this.plotArea.getBounds().width, height);
    };
    this.askOrders.forEach(_function);
    it.setForeground(this.bidColor);
    final Consumer<ILimitOrder> _function_1 = (ILimitOrder it_1) -> {
      final int height = this.yAxis.getPixelCoordinate(it_1.getPrice().doubleValue());
      e.gc.drawLine(0, height, this.plotArea.getBounds().width, height);
    };
    this.bidOrders.forEach(_function_1);
  }
  
  public LimitOrders(final Color askColor, final Color bidColor, final IAxis yAxis, final PlotArea plotArea, final List<ILimitOrder> askOrders, final List<ILimitOrder> bidOrders) {
    super();
    this.askColor = askColor;
    this.bidColor = bidColor;
    this.yAxis = yAxis;
    this.plotArea = plotArea;
    this.askOrders = askOrders;
    this.bidOrders = bidOrders;
  }
}
