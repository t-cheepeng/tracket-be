package com.tcheepeng.tracket.common.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
  @NotBlank private String code;
  @NotBlank private String message;
}
