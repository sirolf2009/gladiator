package com.sirolf2009.gladiator.parts.candlestickchart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Pair;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class Coordinates implements ICustomPaintListener {
  private final AtomicReference<Pair<Integer, Integer>> mousePos;
  
  private final IAxis xAxis;
  
  private final IAxis yAxis;
  
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
    StringConcatenation _builder = new StringConcatenation();
    SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    double _dataCoordinate = this.xAxis.getDataCoordinate((mouseX).intValue());
    Date _date = new Date(((long) _dataCoordinate));
    String _format = _simpleDateFormat.format(_date);
    _builder.append(_format);
    _builder.append(", ");
    double _dataCoordinate_1 = this.yAxis.getDataCoordinate((mouseY).intValue());
    _builder.append(((int) _dataCoordinate_1));
    it.drawText(_builder.toString(), 0, 0);
  }
  
  public Coordinates(final AtomicReference<Pair<Integer, Integer>> mousePos, final IAxis xAxis, final IAxis yAxis) {
    super();
    this.mousePos = mousePos;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
  }
}
