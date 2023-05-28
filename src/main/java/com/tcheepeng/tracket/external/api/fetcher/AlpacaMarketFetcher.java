package com.tcheepeng.tracket.external.api.fetcher;

import com.tcheepeng.tracket.external.api.model.AlpacaMarketResult;
import com.tcheepeng.tracket.external.api.model.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.StockBar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.snapshot.Snapshot;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;

@Slf4j
public class AlpacaMarketFetcher {

  private String ticker;

  private AlpacaMarketFetcher() {}

  public static AlpacaMarketFetcher init() {
    return new AlpacaMarketFetcher();
  }

  public AlpacaMarketFetcher withTicker(String ticker) {
    this.ticker = ticker;
    return this;
  }

  public AlpacaMarketResult fetch() throws ExternalApiException {
    if (this.ticker == null) {
      throw new ExternalApiException("Stock ticker cannot be null");
    }

    AlpacaAPI alpacaAPI = new AlpacaAPI();
    try {
      Snapshot snapshot = alpacaAPI.stockMarketData().getSnapshot(this.ticker);
      StockBar dailyBar = snapshot.getDailyBar();
      log.info("Retrieved {} snapshot {}", this.ticker, snapshot);

      return AlpacaMarketResult.builder()
          .tickerSymbol(this.ticker)
          .open(dailyBar.getOpen())
          .high(dailyBar.getHigh())
          .low(dailyBar.getLow())
          .close(dailyBar.getClose())
          .volume(dailyBar.getVolume())
          .timestamp(dailyBar.getTimestamp())
          .tradeCount(dailyBar.getTradeCount())
          .build();
    } catch (AlpacaClientException e) {
      throw new ExternalApiException(e);
    }
  }
}
