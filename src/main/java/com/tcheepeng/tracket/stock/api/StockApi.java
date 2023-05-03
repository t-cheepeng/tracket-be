package com.tcheepeng.tracket.stock.api;

import com.tcheepeng.tracket.stock.api.strategies.BaseStrategy;
import com.tcheepeng.tracket.stock.api.strategies.YahooFinanceStrategy;
import java.util.Objects;

public class StockApi {

  public static BaseStrategy of(ApiStrategy strategy) {
    if (Objects.requireNonNull(strategy) == ApiStrategy.YAHOO_FINANCE) {
      return YahooFinanceStrategy.init();
    } else {
      return YahooFinanceStrategy.init();
    }
  }
}
