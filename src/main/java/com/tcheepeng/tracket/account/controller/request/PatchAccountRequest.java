package com.tcheepeng.tracket.account.controller.request;

import com.tcheepeng.tracket.common.validation.ValidID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatchAccountRequest {
  @ValidID private Integer id;

  @Nullable
  @Size(max = 255, message = "Name cannot be more than 255 characters")
  private String name;

  @Nullable
  @Size(max = 65535, message = "Account description is too long")
  private String description;
}
