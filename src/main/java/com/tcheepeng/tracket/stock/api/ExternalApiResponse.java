package com.tcheepeng.tracket.stock.api;

import java.time.Instant;
import java.util.Currency;
import com.tcheepeng.tracket.common.response.ResponseData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalApiResponse implements ResponseData {

  private String stockName;
  // Thousandths of a dollar. i.e. $3.019 = 3019. $1.05 = 1050
  private long priceInMill;
  private Currency currency;
  private Instant priceTs;
}
