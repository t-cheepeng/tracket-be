package com.tcheepeng.tracket.external.service;

import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.external.model.SgxClosingPrice;
import com.tcheepeng.tracket.external.model.SgxFetchPriceHistory;
import com.tcheepeng.tracket.external.repository.SgxFetchPriceHistoryRepository;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class InformationProcessorService {

  private final SgxFetchPriceHistoryRepository sgxFetchPriceHistoryRepository;
  private final TimeOperator timeOperator;

  public InformationProcessorService(
      final SgxFetchPriceHistoryRepository sgxFetchPriceHistoryRepository,
      final TimeOperator timeOperator) {
    this.sgxFetchPriceHistoryRepository = sgxFetchPriceHistoryRepository;
    this.timeOperator = timeOperator;
  }

  public List<SgxClosingPrice> processSgxClosingPriceData(
      Pair<Integer, byte[]> sgxClosingPriceRawData) {
    Integer pathCode = sgxClosingPriceRawData.getFirst();
    byte[] rawData = sgxClosingPriceRawData.getSecond();
    SgxFetchPriceHistory sgxFetchPriceHistory = new SgxFetchPriceHistory();
    String semicolonDelimitedData = new String(rawData, StandardCharsets.UTF_8);
    String[] stockRow = semicolonDelimitedData.split("\\r?\\n");
    sgxFetchPriceHistory.setFetchTs(timeOperator.getCurrentTimestamp());
    sgxFetchPriceHistory.setFileBlob(rawData);
    sgxFetchPriceHistory.setPathCode(pathCode);
    List<SgxClosingPrice> closingPrices =
        Arrays.stream(stockRow)
            .map(
                stock -> {
                  String[] stockData = stock.split(";");
                  LocalDate date =
                      LocalDate.parse(stockData[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                  sgxFetchPriceHistory.setDateOfPathCode(Date.valueOf(date));
                  return SgxClosingPrice.builder()
                      .date(date)
                      .stockName(stockData[1].trim())
                      .remarks(stockData[2].trim())
                      .currency(stockData[3].trim())
                      .high(stockData[4].trim())
                      .low(stockData[5].trim())
                      .last(stockData[6].trim())
                      .change(stockData[7].trim())
                      .volume(stockData[8].trim())
                      .bid(stockData[9].trim())
                      .offer(stockData[10].trim())
                      .market(stockData[11].trim())
                      .open(stockData[12].trim())
                      .value(stockData[13].trim())
                      .stockCode(stockData[14].trim())
                      .close(stockData[15].trim())
                      .build();
                })
            .toList();
    sgxFetchPriceHistoryRepository.save(sgxFetchPriceHistory);
    return closingPrices;
  }
}
