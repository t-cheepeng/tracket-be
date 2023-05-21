package com.tcheepeng.tracket.account.controller.response;

import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.common.response.ResponseData;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse implements ResponseData {
  @NotBlank private int id;
  @NotNull private AccountType accountType;
  @NotBlank private String currency;
  @Nullable private String description;
  @NotBlank private String name;
  @PositiveOrZero private int cashInCents;
  @PositiveOrZero private int assetValueInCents;
}
