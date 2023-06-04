package com.tcheepeng.tracket.account.controller.response;

import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.common.response.ResponseData;
import com.tcheepeng.tracket.stock.controller.response.TradeResponse;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountActivityPageResponse implements ResponseData {
  private boolean hasNextPageForTransaction;
  private int nextPageNumForTransaction;
  private boolean hasNextPageForTrade;
  private int nextPageNumForTrade;
  private List<AccountTransactions> accountTransactionsInCurrentPage;
  private List<TradeResponse> accountTradesInCurrentPage;
}
