package com.tcheepeng.tracket.stock.api.strategies;

import com.tcheepeng.tracket.stock.api.ExternalApiResponse;
import com.tcheepeng.tracket.stock.api.ExternalSearchResponse;
import com.tcheepeng.tracket.stock.api.StockApiException;
import java.io.IOException;import java.util.List;

public interface BaseStrategy {

  BaseStrategy withTicker(String ticker);

  BaseStrategy daily();

  ExternalApiResponse fetch() throws StockApiException;

  List<ExternalSearchResponse> search(String query) throws StockApiException;
}
