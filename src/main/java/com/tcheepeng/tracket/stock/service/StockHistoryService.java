package com.tcheepeng.tracket.stock.service;

import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import com.tcheepeng.tracket.external.model.SgxClosingPrice;
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

  public void storeSgxStockClosingPrice(List<SgxClosingPrice> sgxClosingPriceList) {
    List<TickerApi> tickers =
        tickerApiRepository.findAllByApiEqualsAndStockIsDeletedFalse(ApiFetcher.SGX);
    tickers.forEach(
        ticker -> {
          String tickerSymbol = ticker.getTickerSymbol().trim();
          for (SgxClosingPrice closingPrice : sgxClosingPriceList) {
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
              historicalStockPrice.setPriceInCents(
                  stockClosingPrice.scaleByPowerOfTen(2).toBigInteger().intValueExact());
              historicalStockPrice.setName(ticker.getName());
              historicalStockPrice.setApi(ApiFetcher.SGX);
              log.info("Storing historical closing price: {}", historicalStockPrice);
              historicalStockPriceRepository.save(historicalStockPrice);
            }
          }
        });
  }
}
