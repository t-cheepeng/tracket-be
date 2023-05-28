package com.tcheepeng.tracket.scheduler;

import com.tcheepeng.tracket.common.exceptions.TracketServiceException;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.external.api.model.AlpacaMarketResult;
import com.tcheepeng.tracket.external.model.AlpacaMarketClosePrice;
import com.tcheepeng.tracket.external.model.SgxClosePrice;
import com.tcheepeng.tracket.external.service.FetchService;
import com.tcheepeng.tracket.external.service.InformationProcessorService;
import com.tcheepeng.tracket.stock.service.StockHistoryService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Scheduler {

  private final FetchService fetchService;
  private final InformationProcessorService informationProcessorService;
  private final StockHistoryService stockHistoryService;
  private final TimeOperator timeOperator;

  Scheduler(
      final FetchService fetchService,
      final InformationProcessorService informationProcessorService,
      final StockHistoryService stockHistoryService,
      final TimeOperator timeOperator) {
    this.fetchService = fetchService;
    this.informationProcessorService = informationProcessorService;
    this.stockHistoryService = stockHistoryService;
    this.timeOperator = timeOperator;
  }

  @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Singapore")
  @Transactional
  public void fetchDailyHistoricalStockPrices() {
    try {
      List<AlpacaMarketResult> results = fetchService.fetchHistoricalStockPrices();
      List<AlpacaMarketClosePrice> processedData =
          informationProcessorService.processAlpacaMarketClosePriceData(results);
      stockHistoryService.storeAlpacaMarketClosingPrice(processedData);
    } catch (TracketServiceException e) {
      Instant now = timeOperator.getCurrentInstant();
      ZonedDateTime dateTime = now.atZone(ZoneId.of("Asia/Singapore"));
      DateTimeFormatter dateTimeFormatter =
          new DateTimeFormatterBuilder().appendPattern("u-MM-dd HH:mm:ss.SSS O").toFormatter();
      String currentTs = dateTimeFormatter.format(dateTime);
      String errorMsg = "Failed to fetch historical stock prices on: " + currentTs;
      log.error(errorMsg, e);
    }
  }

  @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Singapore")
  @Transactional
  public void fetchSgxHistoricalStockPrices() {
    try {
      Map<LocalDate, Pair<Integer, byte[]>> rawData = fetchService.fetchSgxHistoricalStockPrices();
      if (rawData.isEmpty()) {
        log.info("No data to be processed for: {}", LocalDate.now());
      }

      rawData
          .values()
          .forEach(
              rawDataPair -> {
                List<SgxClosePrice> sgxClosingPriceList =
                    informationProcessorService.processSgxClosingPriceData(rawDataPair);
                stockHistoryService.storeSgxStockClosingPrice(sgxClosingPriceList);
              });
    } catch (TracketServiceException e) {
      Instant now = timeOperator.getCurrentInstant();
      ZonedDateTime dateTime = now.atZone(ZoneId.of("Asia/Singapore"));
      DateTimeFormatter dateTimeFormatter =
          new DateTimeFormatterBuilder().appendPattern("u-MM-dd HH:mm:ss.SSS O").toFormatter();
      String currentTs = dateTimeFormatter.format(dateTime);
      String errorMsg = "Failed to fetch SGX historical stock prices on: " + currentTs;
      log.error(errorMsg, e);
    }
  }
}
