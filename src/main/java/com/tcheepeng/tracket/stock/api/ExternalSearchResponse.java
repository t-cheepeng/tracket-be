package com.tcheepeng.tracket.stock.api;

import java.util.Currency;
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
  ApiStrategy apiUsed;
}
