package com.tcheepeng.tracket.group.controller.request;

import com.tcheepeng.tracket.common.validation.ValidID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountAccountGroupRequest {
  @ValidID private Integer accountId;
  @ValidID private Integer accountGroupId;
}
