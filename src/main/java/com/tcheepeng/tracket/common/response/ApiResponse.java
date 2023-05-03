package com.tcheepeng.tracket.common.response;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;import java.util.List;

@Data
@Builder
public class ApiResponse {

  @NotBlank private Status status;
  @Nullable private List<ApiError> errors;
  @Nullable private ResponseData data;

  public enum Status {
    SUCCESS,
    FAIL,
    PENDING
  }
}
