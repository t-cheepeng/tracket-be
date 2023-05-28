package com.tcheepeng.tracket.stock.controller.request;

import com.tcheepeng.tracket.common.validation.ValidName;
import com.tcheepeng.tracket.stock.model.TradeType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeStockRequest {

  @NotNull(message = "Trade time cannot be empty")
  @Min(value = 1, message = "Trade time must be after 01 January 1970")
  private Long timestamp;

  @NotNull(message = "Trade type cannot be empty")
  private TradeType tradeType;

  @NotNull(message = "Number of stocks traded cannot be empty")
  private Integer numOfUnits;

  @NotNull(message = "Price of stock cannot be empty")
  private String price;

  @ValidName private String name;

  @NotNull(message = "Trade must be belong to an account")
  private Integer accountId;

  @Nullable private String fee;

  @Nullable private Integer buyId;
}
