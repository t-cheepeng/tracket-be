package com.tcheepeng.tracket.external.api.fetcher;

import com.tcheepeng.tracket.external.api.ExternalApi;
import com.tcheepeng.tracket.external.api.model.AlpacaMarketResult;
import com.tcheepeng.tracket.external.api.model.ExternalApiException;
import com.tcheepeng.tracket.external.api.model.ExternalSearchResponse;
import com.tcheepeng.tracket.external.api.model.ExtractedResultItem;

import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.assets.Asset;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetClass;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetStatus;
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

  public List<ExternalSearchResponse> search(String query) throws ExternalApiException {
    log.info("Starting query for alpaca markets: {}", query);
    try {
      AlpacaAPI alpacaAPI = new AlpacaAPI();
      List<Asset> cryptoAssets = alpacaAPI.assets().get(AssetStatus.ACTIVE, AssetClass.CRYPTO);
      List<Asset> usEquityAssets = alpacaAPI.assets().get(AssetStatus.ACTIVE, AssetClass.US_EQUITY);
      return Stream.of(
              FuzzySearch.extractSorted(
                      query,
                      cryptoAssets.stream().map(Asset::getName).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(3)
                  .map(
                      result ->
                          ExtractedResultItem.<Asset>builder()
                              .item(cryptoAssets.get(result.getIndex()))
                              .result(result)
                              .build()),
              FuzzySearch.extractSorted(
                      query,
                      cryptoAssets.stream().map(Asset::getSymbol).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(3)
                  .map(
                      result ->
                          ExtractedResultItem.<Asset>builder()
                              .item(cryptoAssets.get(result.getIndex()))
                              .result(result)
                              .build()),
              FuzzySearch.extractSorted(
                      query,
                      usEquityAssets.stream().map(Asset::getName).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(3)
                  .map(
                      result ->
                          ExtractedResultItem.<Asset>builder()
                              .item(usEquityAssets.get(result.getIndex()))
                              .result(result)
                              .build()),
              FuzzySearch.extractSorted(
                      query,
                      usEquityAssets.stream().map(Asset::getSymbol).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(3)
                  .map(
                      result ->
                          ExtractedResultItem.<Asset>builder()
                              .item(usEquityAssets.get(result.getIndex()))
                              .result(result)
                              .build()))
          .flatMap(stream -> stream)
          .distinct()
          .sorted(Comparator.comparingInt(result -> -result.getResult().getScore()))
          .map(
              searchResult -> {
                log.info("Mapping alpaca market search result: {}", searchResult);
                return ExternalSearchResponse.builder()
                    .stockName(searchResult.getItem().getName())
                    .currencyGuess(Currency.getInstance("USD"))
                    .exchange(searchResult.getItem().getExchange())
                    .ticker(searchResult.getItem().getSymbol())
                    .stockClass(searchResult.getItem().getAssetClass().toString())
                    .exchangeCountry(
                        searchResult.getItem().getAssetClass() == AssetClass.CRYPTO
                            ? "DECENTRALISED"
                            : "USA")
                    .searchScore(searchResult.getResult().getScore())
                    .apiUsed(ApiFetcher.ALPACA_MARKET)
                    .build();
              })
          .toList();
    } catch (AlpacaClientException e) {
      throw new ExternalApiException(e);
    }
  }
}
