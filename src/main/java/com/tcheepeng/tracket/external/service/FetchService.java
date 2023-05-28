package com.tcheepeng.tracket.external.service;

import com.tcheepeng.tracket.common.exceptions.TracketServiceException;
import com.tcheepeng.tracket.external.api.ExternalApi;
import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import com.tcheepeng.tracket.external.api.model.AlpacaMarketResult;
import com.tcheepeng.tracket.external.api.model.ExternalApiException;
import com.tcheepeng.tracket.external.model.SgxFetchPriceHistory;
import com.tcheepeng.tracket.external.repository.SgxFetchPriceHistoryRepository;
import com.tcheepeng.tracket.stock.model.TickerApi;
import com.tcheepeng.tracket.stock.repository.TickerApiRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FetchService {

  private final TickerApiRepository tickerApiRepository;
  private final SgxFetchPriceHistoryRepository sgxFetchPriceHistoryRepository;

  public FetchService(
      final TickerApiRepository tickerApiRepository,
      final SgxFetchPriceHistoryRepository sgxFetchPriceHistoryRepository) {
    this.tickerApiRepository = tickerApiRepository;
    this.sgxFetchPriceHistoryRepository = sgxFetchPriceHistoryRepository;
  }

  @Transactional
  public List<AlpacaMarketResult> fetchHistoricalStockPrices() throws TracketServiceException {
    try {
      List<TickerApi> tickerApis =
          tickerApiRepository.findApiStock().stream()
              .filter(
                  tickerApi ->
                      !tickerApi.getStock().isDeleted()
                          && tickerApi.getApi().equals(ApiFetcher.ALPACA_MARKET))
              .toList();
      log.info("{}", tickerApis);

      List<AlpacaMarketResult> externalApiResponse =
          tickerApis.stream()
              .map(
                  tickerApi ->
                      ExternalApi.alpacaMarket().withTicker(tickerApi.getTickerSymbol()).fetch())
              .toList();
      log.info("{}", externalApiResponse);
      return externalApiResponse;
    } catch (ExternalApiException e) {
      throw new TracketServiceException(e);
    }
  }

  @Transactional
  public Map<LocalDate, Pair<Integer, byte[]>> fetchSgxHistoricalStockPrices()
      throws TracketServiceException {
    try {
      SgxFetchPriceHistory sgxFetchPriceHistory =
          sgxFetchPriceHistoryRepository.findTopByOrderByDateOfPathCodeDesc();
      Duration durationFromLastRun =
          Duration.between(
              sgxFetchPriceHistory.getDateOfPathCode().toLocalDate().atStartOfDay(),
              LocalDate.now().atStartOfDay());
      long numOfDays = durationFromLastRun.toDays();
      Set<DayOfWeek> weekends = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

      int dayAfter = 1;
      List<Pair<LocalDate, Integer>> datesToFetch = new ArrayList<>((int) numOfDays);
      for (int i = 1; i <= numOfDays; i++) {
        LocalDate possibleFetchDate =
            sgxFetchPriceHistory.getDateOfPathCode().toLocalDate().plusDays(i);
        if (weekends.contains(possibleFetchDate.getDayOfWeek())) {
          continue;
        }

        datesToFetch.add(Pair.of(possibleFetchDate, dayAfter));
        dayAfter++;
      }
      log.info("SGX historical dates to fetch: {}", datesToFetch);
      Map<LocalDate, Pair<Integer, byte[]>> result = new HashMap<>();
      datesToFetch.forEach(
          dateAndDayAfter -> {
            int pathCode = sgxFetchPriceHistory.getPathCode() + dateAndDayAfter.getSecond();
            log.info(
                "Fetching SGX historical data for date {} at path {}",
                dateAndDayAfter.getFirst(),
                pathCode);
            byte[] data = ExternalApi.sgx().fetchHistoricalClosingPrices(pathCode, "SESprice.dat");
            result.putIfAbsent(dateAndDayAfter.getFirst(), Pair.of(pathCode, data));
          });
      return result;
    } catch (ExternalApiException e) {
      throw new TracketServiceException(e);
    }
  }
}
