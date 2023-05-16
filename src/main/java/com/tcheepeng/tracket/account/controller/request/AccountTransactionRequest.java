package com.tcheepeng.tracket.account.controller.request;

import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.common.validation.ValidID;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountTransactionRequest {
  @ValidID private int accountIdFrom;
  @Nullable private Integer accountIdTo;
  private int amountsInCents;
  private AccountTransactionType transactionType;
  @Nullable private Integer exchangeRateInMilli;
}
