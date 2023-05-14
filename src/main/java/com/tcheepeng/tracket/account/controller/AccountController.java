package com.tcheepeng.tracket.account.controller;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.controller.response.AccountsResponse;
import com.tcheepeng.tracket.account.model.Account;
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
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
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
                                    "cashInCents": 1000
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
    Optional<Account> optionalAccount = accountService.getAccount(accountId);
    if (optionalAccount.isEmpty() || optionalAccount.get().isDeleted()) {
      return new ResponseEntity<>(
          ApiResponse.builder()
              .status(ApiResponse.Status.FAIL)
              .errors(
                  List.of(
                      ApiError.builder()
                          .message("Unable to find account or account is deleted")
                          .code("404")
                          .build()))
              .build(),
          HttpStatus.NOT_FOUND);
    }

    Account account = optionalAccount.get();
    AccountResponse accountData =
        AccountResponse.builder()
            .id(account.getId())
            .accountType(account.getAccountType())
            .currency(account.getCurrency())
            .description(account.getDescription())
            .name(account.getName())
            .cashInCents(account.getCashInCents())
            .build();
    return new ResponseEntity<>(
        ApiResponse.builder().status(ApiResponse.Status.SUCCESS).data(accountData).build(),
        HttpStatus.OK);
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
                                    "cashInCents": 1000
                                  }
                              }
                             """)
                  })
            }),
      })
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAllAccounts() {
    List<AccountResponse> accounts =
        accountService.getAllAccounts().stream()
            .filter(account -> !account.isDeleted())
            .map(
                account ->
                    AccountResponse.builder()
                        .id(account.getId())
                        .accountType(account.getAccountType())
                        .currency(account.getCurrency())
                        .description(account.getDescription())
                        .name(account.getName())
                        .cashInCents(account.getCashInCents())
                        .build())
            .toList();
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
    if (request.getAmountsInCents() < 0) {
      return new ResponseEntity<>(
          ApiResponse.builder()
              .status(ApiResponse.Status.FAIL)
              .errors(
                  List.of(
                      ApiError.builder()
                          .code("amountInCents")
                          .message("Transaction amount must be greater than 0")
                          .build()))
              .build(),
          HttpStatus.BAD_REQUEST);
    }
    accountService.transactAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }
}
