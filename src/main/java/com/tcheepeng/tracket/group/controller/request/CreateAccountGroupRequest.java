package com.tcheepeng.tracket.group.controller.request;

import com.tcheepeng.tracket.common.validation.ValidCurrency;
import com.tcheepeng.tracket.common.validation.ValidName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAccountGroupRequest {

  @ValidName private String name;
  @ValidCurrency private String currency;
}
