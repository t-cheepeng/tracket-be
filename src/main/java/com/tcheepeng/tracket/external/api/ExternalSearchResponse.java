package com.tcheepeng.tracket.external.api;

import java.util.Currency;

import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalSearchResponse {
  String stockName;
  Currency currencyGuess;
  String exchange;
  String ticker;
  String stockClass;
  String exchangeCountry;
  double searchScore;
  ApiFetcher apiUsed;
}
