package com.tcheepeng.tracket.external.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SgxClosePrice {
  private LocalDate date;
  private String stockName;
  private String remarks;
  private String currency;
  private String high;
  private String low;
  private String last;
  private String change;
  private String volume;
  private String bid;
  private String offer;
  private String market;
  private String open;
  private String value;
  private String stockCode;
  private String close;
}
