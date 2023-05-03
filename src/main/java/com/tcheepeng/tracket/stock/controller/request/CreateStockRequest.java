package com.tcheepeng.tracket.stock.controller.request;

import com.tcheepeng.tracket.common.validation.ValidCurrency;
import com.tcheepeng.tracket.common.validation.ValidName;
import com.tcheepeng.tracket.stock.api.ApiStrategy;
import com.tcheepeng.tracket.stock.model.AssetClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateStockRequest {

  @ValidName String name;
  @ValidCurrency String currency;
  @NotNull(message = "Asset class cannot be empty") AssetClass assetClass;
  @NotNull(message = "Ticker to API mapping cannot be null") Map<ApiStrategy, String> apiTickers;

  @NotBlank(message = "Ticker symbol cannot be blank")
  @Size(max = 20, message = "Ticker symbol cannot be more than 20 characters")
  String displayTickerSymbol;
}
