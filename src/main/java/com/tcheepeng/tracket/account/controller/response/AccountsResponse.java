package com.tcheepeng.tracket.account.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountsResponse implements ResponseData {
  @NotNull private List<AccountResponse> accounts;
}
