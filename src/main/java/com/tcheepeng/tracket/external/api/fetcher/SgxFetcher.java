package com.tcheepeng.tracket.external.api.fetcher;

import com.tcheepeng.tracket.external.api.ExternalApi;
import com.tcheepeng.tracket.external.api.model.ExternalApiException;
import com.tcheepeng.tracket.external.api.model.ExternalSearchResponse;
import com.tcheepeng.tracket.external.api.model.ExtractedResultItem;
import com.tcheepeng.tracket.external.model.SgxClosePrice;
import com.tcheepeng.tracket.external.model.SgxFetchPriceHistory;
import com.tcheepeng.tracket.external.service.FetchService;
import com.tcheepeng.tracket.external.service.InformationProcessorService;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
public class SgxFetcher {

  private final OkHttpClient httpClient;

  private SgxFetcher() {
    this.httpClient = new OkHttpClient();
  }

  public static SgxFetcher init() {
    return new SgxFetcher();
  }

  public byte[] fetchHistoricalClosingPrices(int pathCode, String fileName) {
    String sgxApiUrl =
        "https://links.sgx.com/1.0.0/securities-historical/" + pathCode + "/" + fileName;
    Request request = new Request.Builder().get().url(sgxApiUrl).build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.info(
            "Unable to fetch historical closing prices for {}. Response was {}",
            sgxApiUrl,
            response);
        throw new ExternalApiException("SGX historical closing prices response unsuccessful");
      }

      ResponseBody body = response.body();

      if (body == null) {
        log.info("No SESprice.dat retrieved");
        throw new ExternalApiException("SGX historical closing prices body is empty");
      }

      return body.bytes();
    } catch (Exception e) {
      throw new ExternalApiException(e);
    }
  }

  public List<ExternalSearchResponse> search(
      FetchService fetchService,
      InformationProcessorService informationProcessorService,
      String query) {
    try {
      SgxFetchPriceHistory sgxClosePriceList = fetchService.retrieveLatestStockClosingPrice();
      List<SgxClosePrice> closePrices =
          informationProcessorService.processSgxClosingPriceData(sgxClosePriceList.getFileBlob());
      return Stream.of(
              FuzzySearch.extractSorted(
                      query,
                      closePrices.stream().map(SgxClosePrice::getStockName).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(4)
                  .map(
                      result ->
                          ExtractedResultItem.<SgxClosePrice>builder()
                              .item(closePrices.get(result.getIndex()))
                              .result(result)
                              .build()),
              FuzzySearch.extractSorted(
                      query,
                      closePrices.stream().map(SgxClosePrice::getStockCode).toList(),
                      ExternalApi.FUZZY_SEARCH_SCORE_THRESHOLD)
                  .stream()
                  .limit(4)
                  .map(
                      result ->
                          ExtractedResultItem.<SgxClosePrice>builder()
                              .item(closePrices.get(result.getIndex()))
                              .result(result)
                              .build()))
          .flatMap(stream -> stream)
          .distinct()
          .sorted(Comparator.comparingInt(result -> -result.getResult().getScore()))
          .map(
              searchResult -> {
                log.info("Mapping SGX search result: {}", searchResult);
                return ExternalSearchResponse.builder()
                    .stockName(searchResult.getItem().getStockName())
                    .currencyGuess(Currency.getInstance(searchResult.getItem().getCurrency()))
                    .exchange("SGX")
                    .ticker(searchResult.getItem().getStockCode())
                    .stockClass("SG EQUITY")
                    .exchangeCountry("SG")
                    .searchScore(searchResult.getResult().getScore())
                    .apiUsed(ApiFetcher.SGX)
                    .build();
              })
          .toList();
    } catch (Exception e) {
      throw new ExternalApiException(e);
    }
  }
}
