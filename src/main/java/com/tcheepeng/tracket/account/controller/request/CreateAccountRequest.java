package com.tcheepeng.tracket.account.controller.request;

import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.common.validation.ValidCurrency;
import com.tcheepeng.tracket.common.validation.ValidName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAccountRequest {
  @NotNull(message = "Account type cannot be empty")
  private AccountType accountType;

  @ValidCurrency private String currency;

  @Nullable
  @Size(max = 65535, message = "Account description is too long")
  private String description;

  @ValidName private String name;

  @PositiveOrZero(message = "Cash cannot be negative")
  private int cashInCents;
}
