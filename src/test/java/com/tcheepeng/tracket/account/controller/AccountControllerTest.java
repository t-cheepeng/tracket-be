package com.tcheepeng.tracket.account.controller;

import static com.tcheepeng.tracket.common.TestHelper.assertInternalServerError;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.BK_ACCOUNT_MUST_EXIST;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.service.AccountService;
import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.integration.common.TestWebSecurityConfiguration;
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
@WebMvcTest(controllers = AccountController.class)
class AccountControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AccountService service;

  public static Stream<Arguments> getInvalidCreateAccountRequests() {
    String accountRequest =
        """
        {
        "currency": "SGD",
        "description": "string",
        "name": "string",
        "cash": 0
        }
        """;
    String currencyRequest =
        """
        {
        "accountType": "INVESTMENT",
        "currency": "",
        "description": "string",
        "name": "string",
        "cash": 0
        }
        """;
    String emptyNameRequest =
        """
        {
        "accountType": "INVESTMENT",
        "currency": "SGD",
        "name": "",
        "cash": 0
        }
        """;
    String negativeCash =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGD",
            "name": "asd",
            "cash": -2
            }
            """;
    String longCurrency =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGDDD",
            "name": "asd",
            "cash": 0
            }
            """;
    String longName =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGD",
            "name": "asdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTME",
            "cash": 0
            }
            """;
    return Stream.of(
        Arguments.of(
            accountRequest,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"accountType\",\"message\":\"Account type cannot be empty\"}],\"data\":null}"),
        Arguments.of(
            currencyRequest,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency does not exist\"}],\"data\":null}"),
        Arguments.of(
            emptyNameRequest,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"name\",\"message\":\"Account name cannot be empty\"}],\"data\":null}"),
        Arguments.of(
            negativeCash,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"cash\",\"message\":\"Initial cash cannot be negative\"}],\"data\":null}"),
        Arguments.of(
            longCurrency,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency cannot be more than 4 characters\"}],\"data\":null}"),
        Arguments.of(
            longName,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"name\",\"message\":\"Name cannot be more than 255 characters\"}],\"data\":null}"));
  }

  public static Stream<Arguments> getInvalidPatchAccountRequests() {
    String idRequest =
        """
            {
            "description": "string",
            "name": "string"
            }
            """;
    String negativeRequest =
        """
             {
             "id": -23
             }
             """;
    return Stream.of(
        Arguments.of(
            idRequest,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"id\",\"message\":\"ID must not be empty\"}],\"data\":null}"),
        Arguments.of(
            negativeRequest,
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"id\",\"message\":\"ID must be positive\"}],\"data\":null}"));
  }

  @Test
  void Get_account_normally_is_successful() throws Exception {
    AccountResponse returnedAccount = TestHelper.getTestAccountResponse();
    when(service.getAccount(1)).thenReturn(Optional.of(returnedAccount));

    MvcResult result =
        mockMvc
            .perform(get("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"id\":1,\"accountType\":\"INVESTMENT\",\"currency\":\"SGD\",\"description\":\"Account description\",\"name\":\"Test Account\",\"cash\":\"1.000000\",\"assetValue\":\"1.000000\"}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_account_that_is_deleted() throws Exception {
    when(service.getAccount(1)).thenReturn(Optional.empty());

    MvcResult result =
        mockMvc
            .perform(get("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"404\",\"message\":\"Unable to find account or account is deleted\"}],\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_account_account_not_found() throws Exception {
    when(service.getAccount(1)).thenReturn(Optional.empty());

    MvcResult result =
        mockMvc
            .perform(get("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"404\",\"message\":\"Unable to find account or account is deleted\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Get_account_service_exceptions() throws Exception {
    when(service.getAccount(1)).thenThrow(new RuntimeException());

    MvcResult result =
        mockMvc
            .perform(get("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();
    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @Test
  void Get_all_account_service_exceptions() throws Exception {
    when(service.getAllAccounts()).thenThrow(new RuntimeException());

    MvcResult result =
        mockMvc
            .perform(get("/api/account/").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();
    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @Test
  void Get_all_account_is_successful() throws Exception {
    AccountResponse account = TestHelper.getTestAccountResponse();
    when(service.getAllAccounts()).thenReturn(List.of(account));

    MvcResult result =
        mockMvc
            .perform(get("/api/account/").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"accounts\":[{\"id\":1,\"accountType\":\"INVESTMENT\",\"currency\":\"SGD\",\"description\":\"Account description\",\"name\":\"Test Account\",\"cash\":\"1.000000\",\"assetValue\":\"1.000000\"}]}}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_normally_is_successful() throws Exception {
    CreateAccountRequest request = TestHelper.getCreateAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_integrity_violation_fk_currency() throws Exception {
    CreateAccountRequest request = TestHelper.getCreateAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    DataIntegrityViolationException exception = new DataIntegrityViolationException("fk_currency");
    doThrow(exception).when(service).createAccount(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency does not exist\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_integrity_violation_others() throws Exception {
    CreateAccountRequest request = TestHelper.getCreateAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    DataIntegrityViolationException exception =
        new DataIntegrityViolationException("fk_something_else_violated");
    doThrow(exception).when(service).createAccount(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"generic\",\"message\":\"Failed to process entity\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_service_exception() throws Exception {
    CreateAccountRequest request = TestHelper.getCreateAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    doThrow(RuntimeException.class).when(service).createAccount(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();
    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @ParameterizedTest
  @MethodSource("getInvalidCreateAccountRequests")
  void Create_account_invalid_requests(String jsonRequest, String expectedJsonReply)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    verifyNoInteractions(service);
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Patch_account_is_successful() throws Exception {
    PatchAccountRequest request = TestHelper.getPatchAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                patch("/api/account/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String expectedJsonReply = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Patch_account_service_throws_exception() throws Exception {
    PatchAccountRequest request = TestHelper.getPatchAccountRequest();
    ObjectMapper mapper = new ObjectMapper();

    doThrow(RuntimeException.class).when(service).patchAccount(any(PatchAccountRequest.class));

    MvcResult result =
        mockMvc
            .perform(
                patch("/api/account/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();

    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @ParameterizedTest
  @MethodSource("getInvalidPatchAccountRequests")
  void Patch_account_invalid_requests(String jsonRequest, String expectedJsonReply)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                patch("/api/account/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    verifyNoInteractions(service);
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Delete_account_is_successful() throws Exception {
    MvcResult result =
        mockMvc
            .perform(delete("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String expectedJsonReply = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Delete_account_negative_id_is_successful() throws Exception {
    MvcResult result =
        mockMvc
            .perform(delete("/api/account/-1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String expectedJsonReply = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Delete_account_service_throws_exception() throws Exception {
    doThrow(RuntimeException.class).when(service).deleteAccount(1);

    MvcResult result =
        mockMvc
            .perform(delete("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();

    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @Test
  void Deposit_to_account() throws Exception {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String expectedJsonReply = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Transact_unknown_transaction_type_to_account_fails() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"accountIdFrom\":0,\"amountsInCents\":1000,\"transactionType\":\"UNKNOWN\"}"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String expectedJsonReply =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"transactionType\",\"message\":\"Account transactions must be one of [WITHDRAW, TRANSFER, DEPOSIT]\"}],\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Transact_negative_amount_fails() throws Exception {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    request.setAmount("-10.00");
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String expectedJsonReply =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"amountInCents\",\"message\":\"Transaction amount must be greater than 0\"}],\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Transact_non_existent_account_fails() throws Exception {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    request.setAmount("10.00");
    ObjectMapper mapper = new ObjectMapper();
    doThrow(new DataIntegrityViolationException(BK_ACCOUNT_MUST_EXIST.name()))
        .when(service)
        .transactAccount(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

    String expectedJsonReply =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"accountId\",\"message\":\"Account does not exist\"}],\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Transfer_non_existent_exchange_rate_fails() throws Exception {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    request.setTransactionType(AccountTransactionType.TRANSFER);
    request.setExchangeRate(null);
    request.setAccountIdTo(1);
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String expectedJsonReply =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"exchangeRateInMilli\",\"message\":\"Exchange rate for transfer must be specified\"}],\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }

  @Test
  void Transfer_non_existent_account_id_fails() throws Exception {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    request.setTransactionType(AccountTransactionType.TRANSFER);
    request.setExchangeRate("1.00");
    request.setAccountIdTo(null);
    ObjectMapper mapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/account/transact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String expectedJsonReply =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"accountIdTo\",\"message\":\"Account to be transferred to must be specified\"}],\"data\":null}";
    assertEquals(
        result.getResponse().getContentAsString(), expectedJsonReply, JSONCompareMode.STRICT);
  }
}
