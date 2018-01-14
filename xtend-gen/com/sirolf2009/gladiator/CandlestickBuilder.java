package com.sirolf2009.gladiator;

import com.sirolf2009.commonwealth.timeseries.Candlestick;
import com.sirolf2009.commonwealth.timeseries.ICandlestick;
import com.sirolf2009.commonwealth.trading.ITrade;
import com.sirolf2009.gladiator.candlestickbuilder.MutableCandlestick;
import com.sirolf2009.gladiator.candlestickbuilder.Timeframe;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class CandlestickBuilder {
  @Accessors
  private final Timeframe timeframe;
  
  private final LinkedList<ICandlestick> candlesticks = new LinkedList<ICandlestick>();
  
  @Accessors
  private MutableCandlestick current;
  
  public void addTrade(final ITrade trade) {
    if ((this.current == null)) {
      this.current = this.timeframe.createNewCandlestick(trade);
    } else {
      boolean _isPartOfCandlestick = this.timeframe.isPartOfCandlestick(this.current, trade);
      if (_isPartOfCandlestick) {
        this.current.receiveTrade(trade);
      } else {
        Candlestick _immutable = this.current.immutable();
        this.candlesticks.add(_immutable);
        this.current = this.timeframe.createNewCandlestick(trade);
      }
    }
  }
  
  public List<ICandlestick> getCandlesticks() {
    int _size = this.candlesticks.size();
    int _plus = (_size + 1);
    ArrayList<ICandlestick> _arrayList = new ArrayList<ICandlestick>(_plus);
    final Procedure1<ArrayList<ICandlestick>> _function = (ArrayList<ICandlestick> it) -> {
      it.addAll(this.candlesticks);
      it.add(this.current.immutable());
    };
    return ObjectExtensions.<ArrayList<ICandlestick>>operator_doubleArrow(_arrayList, _function);
  }
  
  public CandlestickBuilder(final Timeframe timeframe) {
    super();
    this.timeframe = timeframe;
  }
  
  @Pure
  public Timeframe getTimeframe() {
    return this.timeframe;
  }
  
  @Pure
  public MutableCandlestick getCurrent() {
    return this.current;
  }
  
  public void setCurrent(final MutableCandlestick current) {
    this.current = current;
  }
}
