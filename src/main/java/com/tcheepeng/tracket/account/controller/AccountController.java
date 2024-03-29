package com.tcheepeng.tracket.account.controller;

import static com.tcheepeng.tracket.common.Utils.toStandardRepresentation;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountActivityPageResponse;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.controller.response.AccountsResponse;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.service.AccountService;
import com.tcheepeng.tracket.common.Constants;
import com.tcheepeng.tracket.common.response.ApiError;
import com.tcheepeng.tracket.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@Slf4j
public class AccountController {

  private final AccountService accountService;

  public AccountController(final AccountService accountService) {
    this.accountService = accountService;
  }

  @Operation(summary = "Get an account by its ID")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account found",
            content = {
              @Content(
                  schema = @Schema(implementation = AccountResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "accountType": "INVESTMENT",
                                    "currency": "SGD ",
                                    "description": "lorem ipsum",
                                    "name": "Tiger",
                                    "cashInCents": 1000,
                                    "assetValueInCents": 1000
                                  }
                              }
                             """)
                  })
            }),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Unable to find account or account is deleted",
            content = {
              @Content(
                  schema = @Schema(implementation = ApiResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "FAIL",
                                "errors": [
                                  {
                                    "code": "404",
                                    "message": "Unable to find account or account is deleted"
                                  }
                                ]
                              }
                             """)
                  })
            })
      })
  @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAccount(
      @Parameter(description = "ID of the account to get", example = "1") @PathVariable
          Integer accountId) {
    Optional<AccountResponse> optionalAccount = accountService.getAccount(accountId);
    return optionalAccount
        .map(
            accountResponse ->
                new ResponseEntity<>(
                    ApiResponse.builder()
                        .status(ApiResponse.Status.SUCCESS)
                        .data(accountResponse)
                        .build(),
                    HttpStatus.OK))
        .orElseGet(
            () ->
                new ResponseEntity<>(
                    ApiResponse.builder()
                        .status(ApiResponse.Status.FAIL)
                        .errors(
                            List.of(
                                ApiError.builder()
                                    .message("Unable to find account or account is deleted")
                                    .code("404")
                                    .build()))
                        .build(),
                    HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "Get all accounts")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "Accounts found. List can be empty and still return 200. Deleted accounts are not returned",
            content = {
              @Content(
                  schema = @Schema(implementation = AccountsResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "accountType": "INVESTMENT",
                                    "currency": "SGD ",
                                    "description": "lorem ipsum",
                                    "name": "Tiger",
                                    "cashInCents": 1000,
                                    "assetValueInCents": 1000
                                  }
                              }
                             """)
                  })
            }),
      })
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAllAccounts() {
    List<AccountResponse> accounts = accountService.getAllAccounts();
    return new ResponseEntity<>(
        ApiResponse.builder()
            .status(ApiResponse.Status.SUCCESS)
            .data(AccountsResponse.builder().accounts(accounts).build())
            .build(),
        HttpStatus.OK);
  }

  @Operation(summary = "Create an account")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account is created successfully",
            content = {
              @Content(
                  schema = @Schema(implementation = ApiResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS"
                              }
                            """)
                  })
            }),
      })
  @PostMapping(
      value = "/create",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> createAccount(
      @Valid @RequestBody CreateAccountRequest request) {
    if (request.getCash() != null && toStandardRepresentation(request.getCash()).signum() < 0) {
      return new ResponseEntity<>(
          ApiResponse.builder()
              .errors(
                  List.of(
                      ApiError.builder()
                          .code("cash")
                          .message("Initial cash cannot be negative")
                          .build()))
              .status(ApiResponse.Status.FAIL)
              .build(),
          HttpStatus.BAD_REQUEST);
    }

    accountService.createAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Update an account. Only the name and description can be updated")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account is updated successfully",
            content = {
              @Content(
                  schema = @Schema(implementation = ApiResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                            {
                              "status": "SUCCESS"
                            }
                          """)
                  })
            }),
      })
  @PatchMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> patchAccount(@Valid @RequestBody PatchAccountRequest request) {
    accountService.patchAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Delete an account by its ID")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account deleted",
            content = {
              @Content(
                  schema = @Schema(implementation = ApiResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS"
                              }
                             """)
                  })
            }),
      })
  @DeleteMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> deleteAccount(
      @Parameter(description = "ID of the account to delete", example = "1") @PathVariable
          Integer accountId) {
    accountService.deleteAccount(accountId);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Deposit, withdraw, or transfer money from an account")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction is created successfully",
            content = {
              @Content(
                  schema = @Schema(implementation = ApiResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS"
                              }
                            """)
                  })
            }),
      })
  @PostMapping(
      value = "/transact",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> transactAccount(
      @Valid @RequestBody AccountTransactionRequest request) {
    List<ApiError> apiErrors = new ArrayList<>(2);
    BigDecimal amount = toStandardRepresentation(request.getAmount());
    if (amount.signum() < 0) {
      apiErrors.add(
          ApiError.builder()
              .code("amountInCents")
              .message("Transaction amount must be greater than 0")
              .build());
    }

    if (request.getTransactionType() == AccountTransactionType.TRANSFER
        && (request.getExchangeRate() == null
            || toStandardRepresentation(request.getExchangeRate()).signum() == 0)) {
      apiErrors.add(
          ApiError.builder()
              .code("exchangeRateInMilli")
              .message("Exchange rate for transfer must be specified")
              .build());
    }

    if (request.getTransactionType() == AccountTransactionType.TRANSFER
        && request.getAccountIdTo() == null) {
      apiErrors.add(
          ApiError.builder()
              .code("accountIdTo")
              .message("Account to be transferred to must be specified")
              .build());
    }

    if (!apiErrors.isEmpty()) {
      return new ResponseEntity<>(
          ApiResponse.builder().status(ApiResponse.Status.FAIL).errors(apiErrors).build(),
          HttpStatus.BAD_REQUEST);
    }

    accountService.transactAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(
      summary =
          "Get account transactions in descending order. Each page will fetch 10 transactions")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "Get account transactions in descending order. Each page will fetch 10 transactions",
            content = {
              @Content(
                  schema = @Schema(implementation = AccountActivityPageResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "hasNextPage": true,
                                    "nextPageNum": 1,
                                    "accountTransactionsInCurrentPage": [
                                        {
                                          "id": 26,
                                          "transactionTs": "2023-05-18T12:52:17.289+00:00",
                                          "accountIdFrom": 10,
                                          "accountIdTo": 19,
                                          "amount": 10000,
                                          "transactionType": "TRANSFER",
                                          "exchangeRate": 100000
                                        }
                                    ]
                                }
                              }
                             """)
                  })
            }),
      })
  @GetMapping(value = "/history/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAccountHistory(
      @Parameter(description = "ID of the account to delete", example = "1") @PathVariable
          Integer accountId,
      @RequestParam("tradePage") int tradePageNum,
      @RequestParam("transactionPage") int transactionPageNum) {
    if (accountId == null) {
      return new ResponseEntity<>(
          ApiResponse.builder()
              .status(ApiResponse.Status.FAIL)
              .errors(
                  List.of(
                      ApiError.builder()
                          .code("accountId")
                          .message("Account ID cannot be null")
                          .build()))
              .build(),
          HttpStatus.BAD_REQUEST);
    }
    log.info("{}, {}", tradePageNum, transactionPageNum);
    AccountActivityPageResponse response =
        accountService.getAccountHistory(accountId, transactionPageNum, tradePageNum);
    return new ResponseEntity<>(
        ApiResponse.builder().status(ApiResponse.Status.SUCCESS).data(response).build(),
        HttpStatus.OK);
  }
}
