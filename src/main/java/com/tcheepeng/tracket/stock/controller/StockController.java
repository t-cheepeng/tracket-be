package com.tcheepeng.tracket.stock.controller;

import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.common.Constants;
import com.tcheepeng.tracket.common.response.ApiError;
import com.tcheepeng.tracket.common.response.ApiResponse;
import com.tcheepeng.tracket.external.api.model.ExternalSearchResponse;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.controller.response.*;
import com.tcheepeng.tracket.stock.model.Stock;
import com.tcheepeng.tracket.stock.model.Trade;
import com.tcheepeng.tracket.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@Slf4j
public class StockController {

  private final StockService stockService;

  public StockController(final StockService stockService) {
    this.stockService = stockService;
  }

  @Operation(summary = "Query a stock from external APIs")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock found in external APIs",
            content = {
              @Content(
                  schema = @Schema(implementation = SearchResponseData.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                            {
                              "status": "SUCCESS",
                              "data": {
                                "searchResponse": [
                                    {
                                     "stockName": "Straits Times Index Fund ETF",
                                     "currencyGuess": "SGD",
                                     "exchange": "SES",
                                     "ticker": "ES3.SI",
                                     "stockClass": "ETF",
                                     "exchangeCountry": "Singapore",
                                     "searchScore": 20000,
                                     "apiUsed": "YAHOO_FINANCE"
                                    }
                                ]
                              }
                            }
                           """)
                  })
            }),
      })
  @GetMapping(value = "/search/{query}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> queryExternalStockApi(@PathVariable String query) {
    List<ExternalSearchResponse> searchResults = stockService.queryExternalStockApi(query);
    return new ResponseEntity<>(
        ApiResponse.builder()
            .status(ApiResponse.Status.SUCCESS)
            .data(SearchResponseData.builder().searchResponses(searchResults).build())
            .build(),
        HttpStatus.OK);
  }

  @Operation(summary = "Get a stock by its name")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock found",
            content = {
              @Content(
                  schema = @Schema(implementation = StockResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "name": "Straits Times Index Fund ETF",
                                    "currency": "SGD",
                                    "assetClass": "ETF",
                                    "displayTickerSymbol": "STI"
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
                                    "message": "Unable to find stock or stock is deleted"
                                  }
                                ]
                              }
                             """)
                  })
            })
      })
  @GetMapping(value = "/{stockName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getStock(
      @Parameter(description = "Name of the stock to get", example = "Straits Times Index Fund ETF")
          @PathVariable
          String stockName) {
    Optional<Stock> optionalStock = stockService.getStock(stockName);
    if (optionalStock.isEmpty() || optionalStock.get().isDeleted()) {
      return new ResponseEntity<>(
          ApiResponse.builder()
              .status(ApiResponse.Status.FAIL)
              .errors(
                  List.of(
                      ApiError.builder()
                          .message("Unable to find stock or stock is deleted")
                          .code("404")
                          .build()))
              .build(),
          HttpStatus.NOT_FOUND);
    }

    Stock stock = optionalStock.get();
    StockResponse stockResponse =
        StockResponse.builder()
            .name(stock.getName())
            .currency(stock.getCurrency())
            .assetClass(stock.getAssetClass())
            .displayTickerSymbol(stock.getDisplayTickerSymbol())
            .build();
    return new ResponseEntity<>(
        ApiResponse.builder().status(ApiResponse.Status.SUCCESS).data(stockResponse).build(),
        HttpStatus.OK);
  }

  @Operation(summary = "Get all stocks")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "Returns all stocks in database or an empty list of nothing is in database.",
            content = {
              @Content(
                  schema = @Schema(implementation = StocksResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "stocks": [
                                        {
                                        "name": "Straits Times Index Fund ETF",
                                        "currency": "SGD",
                                        "assetClass": "ETF",
                                        "displayTickerSymbol": "STI"
                                        }
                                    ]
                                  }
                              }
                             """)
                  })
            }),
      })
  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAllStocks() {
    List<Stock> stocks = stockService.getAllStocks();
    List<StockResponse> stockResponses =
        stocks.stream()
            .map(
                stock ->
                    StockResponse.builder()
                        .name(stock.getName())
                        .assetClass(stock.getAssetClass())
                        .currency(stock.getCurrency())
                        .displayTickerSymbol(stock.getDisplayTickerSymbol())
                        .build())
            .toList();
    return new ResponseEntity<>(
        ApiResponse.builder()
            .status(ApiResponse.Status.SUCCESS)
            .data(StocksResponse.builder().stocks(stockResponses).build())
            .build(),
        HttpStatus.OK);
  }

  @Operation(summary = "Create a stock and ticker to api mappings")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock and ticker to api mapping is created successfully",
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
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> createStock(@RequestBody @Valid CreateStockRequest request) {
    stockService.createStock(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Update a stock. All aspects of the stock can be updated")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock is updated successfully",
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
  @PatchMapping(value = "/base", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> patchStock(@RequestBody @Valid PatchStockRequest request) {
    stockService.updateStock(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Delete a stock by its name")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock deleted",
            content = {
              @Content(
                  schema = @Schema(implementation = AccountResponse.class),
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
  @DeleteMapping(value = "/{stockName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> deleteStock(
      @Parameter(
              description = "Name of the stock to delete",
              example = "Straits Times Index Fund ETF")
          @PathVariable
          String stockName) {
    stockService.deleteStock(stockName);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Add a stock trade for account")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock trade is added for account successfully",
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
      value = "/trade",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> tradeStock(@Valid @RequestBody TradeStockRequest request) {
    stockService.tradeStock(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Get all trades")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "Returns all trades in database or an empty list of nothing is in database.",
            content = {
              @Content(
                  schema = @Schema(implementation = TradesResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                "data": {
                                    "trades": [
                                        {

                                        }
                                    ]
                                  }
                              }
                             """)
                  })
            }),
      })
  @GetMapping(value = "/trade/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAllTrades() {
    List<Trade> trades = stockService.getAllTrades();
    List<TradeResponse> tradeResponses =
        trades.stream()
            .map(
                trade ->
                    TradeResponse.builder()
                        .tradeTs(trade.getTradeTs())
                        .tradeType(trade.getTradeType())
                        .numOfUnits(trade.getNumOfUnits())
                        .pricePerUnit(trade.getPricePerUnit().toPlainString())
                        .name(trade.getName())
                        .account(trade.getAccount())
                        .fee(trade.getFee().toPlainString())
                        .buyId(trade.getBuyId())
                        .build())
            .toList();
    return new ResponseEntity<>(
        ApiResponse.builder()
            .status(ApiResponse.Status.SUCCESS)
            .data(TradesResponse.builder().trades(tradeResponses).build())
            .build(),
        HttpStatus.OK);
  }
}
