package com.tcheepeng.tracket.external.service;

import com.tcheepeng.tracket.common.exceptions.TracketServiceException;
import com.tcheepeng.tracket.external.api.ExternalApi;
import com.tcheepeng.tracket.external.api.ExternalApiException;
import com.tcheepeng.tracket.external.api.ExternalApiResponse;
import com.tcheepeng.tracket.external.model.SgxFetchPriceHistory;
import com.tcheepeng.tracket.external.repository.SgxFetchPriceHistoryRepository;
import com.tcheepeng.tracket.stock.model.TickerApi;
import com.tcheepeng.tracket.stock.repository.TickerApiRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;
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
  public void fetchHistoricalStockPrices() throws TracketServiceException {
    try {
      List<TickerApi> tickerApis =
          tickerApiRepository.findByStockIsNotDeleted().stream()
              .filter(tickerAPi -> !tickerAPi.getStock().isDeleted())
              .toList();
      log.info("{}", tickerApis);

      List<ExternalApiResponse> externalApiResponse =
          tickerApis.stream()
              .map(
                  tickerApi ->
                      ExternalApi.alpacaMarket()
                          .daily()
                          .withTicker(tickerApi.getTickerSymbol())
                          .fetch())
              .toList();
      log.info("{}", externalApiResponse);
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
      List<Pair<LocalDate, Integer>> datesToFetch =
          Stream.iterate(1, num -> num <= numOfDays, f -> f + 1)
              .map(
                  dayAfter ->
                      Pair.of(
                          sgxFetchPriceHistory.getDateOfPathCode().toLocalDate().plusDays(dayAfter),
                          dayAfter))
              .filter(day -> !weekends.contains(day.getFirst().getDayOfWeek()))
              .toList();
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
