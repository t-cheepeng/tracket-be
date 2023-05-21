package com.tcheepeng.tracket.external.api;

import com.tcheepeng.tracket.external.api.fetcher.SgxFetcher;
import com.tcheepeng.tracket.external.api.fetcher.YahooFinanceFetcher;
import com.tcheepeng.tracket.external.api.fetcher.AlpacaMarketFetcher;

import java.util.Objects;

public class ExternalApi {

  public static AlpacaMarketFetcher alpacaMarket() {
    return AlpacaMarketFetcher.init();
  }

  public static SgxFetcher sgx() {
    return SgxFetcher.init();
  }

  public static YahooFinanceFetcher yahooFinance() {
    return YahooFinanceFetcher.init();
  }
}
