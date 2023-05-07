package com.tcheepeng.tracket.stock.controller;

import static com.tcheepeng.tracket.common.validation.BusinessValidations.*;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.BK_SELL_WRONG_STOCK;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.common.validation.BusinessValidations;
import com.tcheepeng.tracket.integration.common.TestWebSecurityConfiguration;
import com.tcheepeng.tracket.stock.api.ApiStrategy;
import com.tcheepeng.tracket.stock.api.ExternalSearchResponse;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.model.AssetClass;
import com.tcheepeng.tracket.stock.model.Stock;
import com.tcheepeng.tracket.stock.service.StockService;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestWebSecurityConfiguration.class})
@WebMvcTest(controllers = StockController.class)
public class StockControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StockService service;

  private static Stream<Arguments> getInvalidCreateStockRequests() {
    CreateStockRequest blankDisplay = TestHelper.getCreateStockRequest();
    blankDisplay.setDisplayTickerSymbol("");
    CreateStockRequest longDisplay = TestHelper.getCreateStockRequest();
    longDisplay.setDisplayTickerSymbol("longer than 20 characters");
    CreateStockRequest nullAsset = TestHelper.getCreateStockRequest();
    nullAsset.setAssetClass(null);
    CreateStockRequest nullApiTickers = TestHelper.getCreateStockRequest();
    nullApiTickers.setApiTickers(null);

    return Stream.of(
        Arguments.of(
            blankDisplay,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"displayTickerSymbol\",\"message\":\"Ticker symbol cannot be blank\"}],\"data\":null}"),
        Arguments.of(
            nullAsset,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"assetClass\",\"message\":\"Asset class cannot be empty\"}],\"data\":null}"),
        Arguments.of(
            nullApiTickers,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"apiTickers\",\"message\":\"Ticker to API mapping cannot be null\"}],\"data\":null}"),
        Arguments.of(
            longDisplay,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"displayTickerSymbol\",\"message\":\"Ticker symbol cannot be more than 20 characters\"}],\"data\":null}"));
  }

  private static Stream<Arguments> getDataIntegrityViolationsTradeStockRequests() {
    return Arrays.stream(BusinessValidations.values())
        .map(
            validations -> {
              String code = "";
              String errorMsg = "";
              switch (validations) {
                case BK_ACCOUNT_STOCK_CURRENCY_MUST_BE_SAME -> {
                  code = "accountId";
                  errorMsg = "Stock currency and account currency must be the same";
                }
                case BK_STOCK_NOT_DELETED -> {
                  code = "name";
                  errorMsg = "Stock does not exist";
                }
                case BK_ACCOUNT_NOT_DELETED -> {
                  code = "accountId";
                  errorMsg = "Account does not exist";
                }
                case BK_SELL_WHAT_IS_BOUGHT -> {
                  code = "buyId";
                  errorMsg = "Corresponding buy trade for sell trade does not exist";
                }
                case BK_SELL_MUST_HAVE_BUY_ID -> {
                  code = "buyId";
                  errorMsg = "Sell trade must have corresponding buy trade";
                }
                case BK_SELL_WRONG_STOCK -> {
                  code = "buyId";
                  errorMsg = "Sell trade does not match with the corresponding buy trade";
                }
              }
              String expectedJson =
                  String.format(
                      "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"%s\",\"message\":\"%s\"}],\"data\":null}",
                      code, errorMsg);
              return Arguments.of(
                  TestHelper.getTradeStockRequest(),
                  new DataIntegrityViolationException(validations.name()),
                  expectedJson);
            });
  }

  @Test
  void Query_external_api_is_successful() throws Exception {
    when(service.queryExternalStockApi("ABC"))
        .thenReturn(
            List.of(
                ExternalSearchResponse.builder()
                    .stockName("ABC")
                    .stockClass("ETF")
                    .apiUsed(ApiStrategy.YAHOO_FINANCE)
                    .exchange("SES")
                    .exchangeCountry("Singapore")
                    .ticker("A35.SI")
                    .currencyGuess(Currency.getInstance("SGD"))
                    .build()));

    MvcResult result =
        mockMvc
            .perform(get("/api/stock/search/ABC").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).queryExternalStockApi("ABC");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"searchResponses\":[{\"stockName\":\"ABC\",\"currencyGuess\":\"SGD\",\"exchange\":\"SES\",\"ticker\":\"A35.SI\",\"stockClass\":\"ETF\",\"exchangeCountry\":\"Singapore\",\"searchScore\":0.0,\"apiUsed\":\"YAHOO_FINANCE\"}]}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Query_external_api_no_results() throws Exception {
    when(service.queryExternalStockApi("no results")).thenReturn(List.of());

    MvcResult result =
        mockMvc
            .perform(get("/api/stock/search/no results").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).queryExternalStockApi("no results");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"searchResponses\":[]}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_stock_is_successful() throws Exception {
    when(service.getStock("ABC")).thenReturn(Optional.of(TestHelper.getTestStock()));

    MvcResult result =
        mockMvc
            .perform(get("/api/stock/ABC").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).getStock("ABC");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"name\":\"ABC\",\"currency\":\"SGD\",\"assetClass\":\"BOND\",\"displayTickerSymbol\":\"Display\"}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_all_stock_is_successful() throws Exception {
    Stock testStock1 = TestHelper.getTestStock();
    Stock testStock2 = TestHelper.getTestStock();
    testStock2.setName("BCD");
    testStock2.setAssetClass(AssetClass.CRYPTOCURRENCY);
    when(service.getAllStocks()).thenReturn(List.of(testStock1, testStock2));

    MvcResult result =
            mockMvc
                    .perform(get("/api/stock/").contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"stocks\":[{\"name\":\"ABC\",\"currency\":\"SGD\",\"assetClass\":\"BOND\",\"displayTickerSymbol\":\"Display\"},{\"name\":\"BCD\",\"currency\":\"SGD\",\"assetClass\":\"CRYPTOCURRENCY\",\"displayTickerSymbol\":\"Display\"}]}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_all_stock_no_stock() throws Exception {
    when(service.getAllStocks()).thenReturn(List.of());

    MvcResult result =
            mockMvc
                    .perform(get("/api/stock/").contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
            "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"stocks\":[]}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_stock_not_found() throws Exception {
    when(service.getStock("not found")).thenReturn(Optional.empty());

    MvcResult result =
        mockMvc
            .perform(get("/api/stock/not found").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

    verify(service).getStock("not found");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"404\",\"message\":\"Unable to find stock or stock is deleted\"}],\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_stock_is_deleted() throws Exception {
    Stock stock = TestHelper.getTestStock();
    stock.setDeleted(true);
    when(service.getStock("deleted")).thenReturn(Optional.of(stock));

    MvcResult result =
        mockMvc
            .perform(get("/api/stock/deleted").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

    verify(service).getStock("deleted");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"404\",\"message\":\"Unable to find stock or stock is deleted\"}],\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Create_stock_is_successful() throws Exception {
    CreateStockRequest request = TestHelper.getCreateStockRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/stock/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).createStock(request);
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @ParameterizedTest
  @MethodSource("getInvalidCreateStockRequests")
  void Create_stock_with_invalid_requests(CreateStockRequest request, String expectedJson)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/stock/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    verifyNoInteractions(service);
    String jsonBody = result.getResponse().getContentAsString();
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Update_stock_is_successful() throws Exception {
    PatchStockRequest request = TestHelper.getPatchStockRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                patch("/api/stock/base")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).updateStock(request);
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Update_stock_no_changes() throws Exception {
    PatchStockRequest request = TestHelper.getPatchStockRequest();
    request.setCurrency(null);
    request.setAssetClass(null);
    request.setDisplayTickerSymbol(null);
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                patch("/api/stock/base")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).updateStock(request);
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Delete_stock_is_successful() throws Exception {
    MvcResult result =
        mockMvc
            .perform(delete("/api/stock/ABC").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).deleteStock("ABC");
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Trade_stock_is_successful() throws Exception {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/stock/trade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    verify(service).tradeStock(request);
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @ParameterizedTest
  @MethodSource("getDataIntegrityViolationsTradeStockRequests")
  void Trade_stock_data_integrity_violations(
      TradeStockRequest request,
      DataIntegrityViolationException exceptionToThrow,
      String expectedJson)
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    doThrow(exceptionToThrow).when(service).tradeStock(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/stock/trade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }
}
