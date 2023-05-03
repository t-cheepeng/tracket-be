package com.tcheepeng.tracket.common;

import com.tcheepeng.tracket.common.response.ApiError;
import com.tcheepeng.tracket.common.response.ApiResponse;
import java.util.List;

public class Constants {

  public static final String GENERIC_ERROR_CODE = "generic";
  public static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error";

  public static final ApiResponse INTERNAL_SERVER_ERROR =
      ApiResponse.builder()
          .status(ApiResponse.Status.FAIL)
          .errors(
              List.of(
                  ApiError.builder()
                      .code(GENERIC_ERROR_CODE)
                      .message(INTERNAL_SERVER_ERROR_MSG)
                      .build()))
          .build();

  public static final ApiResponse EMPTY_SUCCESS_REPLY =
      ApiResponse.builder().status(ApiResponse.Status.SUCCESS).build();
}
