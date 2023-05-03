package com.tcheepeng.tracket.stock.controller.request;

import com.tcheepeng.tracket.common.validation.ValidName;
import com.tcheepeng.tracket.stock.model.AssetClass;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatchStockRequest {
  @ValidName private String name;
  @Nullable private String currency;
  @Nullable private AssetClass assetClass;
  @Nullable private String displayTickerSymbol;
}
