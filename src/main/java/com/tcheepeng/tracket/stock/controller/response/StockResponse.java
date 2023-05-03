package com.tcheepeng.tracket.stock.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import com.tcheepeng.tracket.stock.model.AssetClass;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockResponse implements ResponseData {
  private String name;
  private String currency;
  private AssetClass assetClass;
  private String displayTickerSymbol;
}
