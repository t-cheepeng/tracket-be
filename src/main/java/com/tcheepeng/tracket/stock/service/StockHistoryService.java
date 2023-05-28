package com.tcheepeng.tracket.stock.service;

import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import com.tcheepeng.tracket.external.model.AlpacaMarketClosePrice;
import com.tcheepeng.tracket.external.model.SgxClosePrice;
import com.tcheepeng.tracket.stock.model.HistoricalStockPrice;
import com.tcheepeng.tracket.stock.model.TickerApi;
import com.tcheepeng.tracket.stock.repository.HistoricalStockPriceRepository;
import com.tcheepeng.tracket.stock.repository.TickerApiRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockHistoryService {

  private final TickerApiRepository tickerApiRepository;
  private final HistoricalStockPriceRepository historicalStockPriceRepository;
  private final TimeOperator timeOperator;

  public StockHistoryService(
      final TickerApiRepository tickerApiRepository,
      final HistoricalStockPriceRepository historicalStockPriceRepository,
      final TimeOperator timeOperator) {
    this.tickerApiRepository = tickerApiRepository;
    this.historicalStockPriceRepository = historicalStockPriceRepository;
    this.timeOperator = timeOperator;
  }

  public void storeSgxStockClosingPrice(List<SgxClosePrice> sgxClosePriceList) {
    List<TickerApi> tickers =
        tickerApiRepository.findAllByApiEqualsAndStockIsDeletedFalse(ApiFetcher.SGX);
    tickers.forEach(
        ticker -> {
          String tickerSymbol = ticker.getTickerSymbol().trim();
          for (SgxClosePrice closingPrice : sgxClosePriceList) {
            if (closingPrice.getStockCode().trim().equals(tickerSymbol)) {
              log.info("Stored ticker {} matches SGX closing price list {}", ticker, closingPrice);
              BigDecimal stockClosingPrice = new BigDecimal(closingPrice.getClose().trim());
              LocalDate priceIndicativeDate = closingPrice.getDate();
              LocalDateTime priceIndicativeDateTime =
                  priceIndicativeDate.atTime(
                      LocalTime.ofInstant(
                          timeOperator.getCurrentInstant(), ZoneId.of("Asia/Singapore")));

              HistoricalStockPrice historicalStockPrice = new HistoricalStockPrice();
              historicalStockPrice.setPriceTs(Timestamp.valueOf(priceIndicativeDateTime));
              historicalStockPrice.setPrice(stockClosingPrice);
              historicalStockPrice.setName(ticker.getName());
              historicalStockPrice.setApi(ApiFetcher.SGX);
              log.info("Storing historical closing price: {}", historicalStockPrice);
              historicalStockPriceRepository.save(historicalStockPrice);
            }
          }
        });
  }

  public void storeAlpacaMarketClosingPrice(List<AlpacaMarketClosePrice> alpacaMarketResults) {
    List<TickerApi> tickers =
        tickerApiRepository.findAllByApiEqualsAndStockIsDeletedFalse(ApiFetcher.ALPACA_MARKET);
    List<HistoricalStockPrice> historicalStockPrices =
        tickers.stream()
            .map(
                ticker -> {
                  for (AlpacaMarketClosePrice alpacaMarketClosePrice : alpacaMarketResults) {
                    if (alpacaMarketClosePrice.getTickerSymbol().equals(ticker.getTickerSymbol())) {
                      HistoricalStockPrice historicalStockPrice = new HistoricalStockPrice();
                      historicalStockPrice.setPriceTs(
                          alpacaMarketClosePrice.getIndicativePriceTs());
                      historicalStockPrice.setPrice(alpacaMarketClosePrice.getPrice());
                      historicalStockPrice.setName(ticker.getStock().getName());
                      historicalStockPrice.setApi(ApiFetcher.ALPACA_MARKET);
                      return historicalStockPrice;
                    }
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .toList();
    historicalStockPriceRepository.saveAll(historicalStockPrices);
  }
}
