package com.tcheepeng.tracket.external.api.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class AlpacaMarketResult {
  private String tickerSymbol;
  private Double open;
  private Double high;
  private Double low;
  private Double close;
  private Long volume;
  private ZonedDateTime timestamp;
  private Long tradeCount;
}
