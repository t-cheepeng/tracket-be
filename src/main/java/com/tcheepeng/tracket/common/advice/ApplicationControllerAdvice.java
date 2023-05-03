package com.tcheepeng.tracket.common.advice;

import static com.tcheepeng.tracket.common.Constants.GENERIC_ERROR_CODE;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.*;

import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.common.Constants;
import com.tcheepeng.tracket.common.response.ApiError;
import com.tcheepeng.tracket.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class ApplicationControllerAdvice extends ResponseEntityExceptionHandler {

  @Override
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = {
              @ExampleObject(
                  value =
                      """
                        {
                          "status": "FAIL",
                          "errors": [
                            {
                              "code": "<field_name>",
                              "message": "<validation_error_msg>"
                            }
                          ]
                        }
                       """)
            }),
      })
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    List<ApiError> errors =
        ex.getFieldErrors().stream()
            .map(
                fieldError ->
                    ApiError.builder()
                        .code(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
            .toList();
    log.error("Method argument not valid", ex);
    log.error("List of errors built: {}", errors);
    return new ResponseEntity<>(
        ApiResponse.builder().status(ApiResponse.Status.FAIL).errors(errors).build(),
        HttpStatus.BAD_REQUEST);
  }

  @Override
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "400",
      description = "Unable to convert http message to request model",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = {
              @ExampleObject(
                  value =
                      """
                        {
                          "status": "FAIL",
                          "errors": [
                            {
                              "code": "<field_name> | generic",
                              "message": "<validation_error_msg> | Failed to read request"
                            }
                          ]
                        }
                       """)
            }),
      })
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    String message = ex.getMessage();
    String code;
    String errorMsg;
    if (message.contains("Enum class") && message.contains(AccountType.class.getName())) {
      code = "accountType";
      errorMsg = "Account type must be one of [INVESTMENT, BUDGET]";
    } else {
      code = GENERIC_ERROR_CODE;
      errorMsg = "Failed to read request";
    }
    List<ApiError> errors = List.of(ApiError.builder().code(code).message(errorMsg).build());
    log.error("HTTP message not readable", ex);
    log.error("List of errors built: {}", errors);
    return new ResponseEntity<>(
        ApiResponse.builder().status(ApiResponse.Status.FAIL).errors(errors).build(),
        HttpStatus.BAD_REQUEST);
  }

  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "422",
      description = "Request is understood but entity is not created due to other errors",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = {
              @ExampleObject(
                  value =
                      """
                          {
                          "status": "FAIL",
                          "errors": [
                            {
                              "code": "<field_name> | generic",
                              "message": "<validation_error_msg> | Failed to process entity"
                            }
                          ]
                        }
                      """)
            })
      })
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  protected ResponseEntity<Object> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, WebRequest request) {
    ApiResponse.ApiResponseBuilder builder = ApiResponse.builder().status(ApiResponse.Status.FAIL);
    String code;
    String errorMsg;
    if (ex.getMessage().contains("fk_currency")) {
      code = "currency";
      errorMsg = "Currency does not exist";
    } else if (ex.getMessage().contains("uk_name")) {
      code = "name";
      errorMsg = "Name already exists";
    } else if (ex.getMessage().contains("fk_account_id")) {
      code = "accountId";
      errorMsg = "Account does not exist";
    } else if (ex.getMessage().contains("fk_account_group_id")) {
      code = "accountGroupId";
      errorMsg = "Account group does not exist";
    } else if (ex.getMessage().contains(BK_ACCOUNT_STOCK_CURRENCY_MUST_BE_SAME.name())) {
      code = "accountId";
      errorMsg = "Stock currency and account currency must be the same";
    } else if (ex.getMessage().contains(BK_STOCK_NOT_DELETED.name())) {
      code = "name";
      errorMsg = "Stock does not exist";
    } else if (ex.getMessage().contains(BK_ACCOUNT_NOT_DELETED.name())) {
      code = "accountId";
      errorMsg = "Account does not exist";
    } else if (ex.getMessage().contains(BK_SELL_WHAT_IS_BOUGHT.name())) {
      code = "buyId";
      errorMsg = "Corresponding buy trade for sell trade does not exist";
    } else if (ex.getMessage().contains(BK_SELL_MUST_HAVE_BUY_ID.name())) {
      code = "buyId";
      errorMsg = "Sell trade must have corresponding buy trade";
    } else if (ex.getMessage().contains(BK_SELL_WRONG_STOCK.name())) {
      code = "buyId";
      errorMsg = "Sell trade does not match with the corresponding buy trade";
    } else {
      code = Constants.GENERIC_ERROR_CODE;
      errorMsg = "Failed to process entity";
    }

    log.error("Data integrity violated", ex);
    log.error("Request causing data integrity violation: {}", request);
    return new ResponseEntity<>(
        builder.errors(List.of(ApiError.builder().code(code).message(errorMsg).build())).build(),
        HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = {
              @ExampleObject(
                  value =
                      """
                          {
                          "status": "FAIL",
                          "errors": [
                            {
                              "code": "generic",
                              "message": "Internal server error"
                            }
                          ]
                        }
                      """)
            })
      })
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected ResponseEntity<Object> handleAllUncaughtException(
      RuntimeException ex, WebRequest request) {
    log.error("Uncaught exception in application", ex);
    log.error("Request that was causing the uncaught exception: {}", request);
    return new ResponseEntity<>(Constants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
