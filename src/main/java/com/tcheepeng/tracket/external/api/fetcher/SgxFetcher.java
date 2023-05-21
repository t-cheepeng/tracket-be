package com.tcheepeng.tracket.external.api.fetcher;

import com.tcheepeng.tracket.external.api.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
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
}
