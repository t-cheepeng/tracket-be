package com.tcheepeng.tracket.external.api.fetcher;

import com.tcheepeng.tracket.external.api.ExternalApiResponse;
import com.tcheepeng.tracket.external.api.ExternalSearchResponse;
import com.tcheepeng.tracket.external.api.ExternalApiException;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetClass;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetStatus;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.snapshot.Snapshot;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;

@Slf4j
public class AlpacaMarketFetcher {

  private String ticker;
  private boolean isDaily = false;

  private AlpacaMarketFetcher() {}

  public static AlpacaMarketFetcher init() {
    return new AlpacaMarketFetcher();
  }

  public AlpacaMarketFetcher withTicker(String ticker) {
    this.ticker = ticker;
    return this;
  }

  public AlpacaMarketFetcher daily() {
    isDaily = true;
    return this;
  }

  public ExternalApiResponse fetch() throws ExternalApiException {
    if (this.ticker == null) {
      throw new ExternalApiException("Stock ticker cannot be null");
    }

    AlpacaAPI alpacaAPI = new AlpacaAPI();
    try {
      var assets = alpacaAPI.assets().get(AssetStatus.ACTIVE, AssetClass.fromValue("sg_equity"));
      log.info("Assets available: {}", assets);
      Snapshot snapshot = alpacaAPI.stockMarketData().getSnapshot(this.ticker);



      log.info("Retrieved {} snapshot {}", this.ticker, snapshot);
      return ExternalApiResponse.builder().build();
    } catch (AlpacaClientException e) {
      throw new ExternalApiException(e);
    }
  }
}
