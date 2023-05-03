package com.tcheepeng.tracket.account.controller;

import static com.tcheepeng.tracket.common.TestHelper.assertInternalServerError;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.model.Account;
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
        "cashInCents": 0
        }
        """;
    String currencyRequest =
        """
        {
        "accountType": "INVESTMENT",
        "currency": "",
        "description": "string",
        "name": "string",
        "cashInCents": 0
        }
        """;
    String emptyNameRequest =
        """
        {
        "accountType": "INVESTMENT",
        "currency": "SGD",
        "name": "",
        "cashInCents": 0
        }
        """;
    String negativeCash =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGD",
            "name": "asd",
            "cashInCents": -2
            }
            """;
    String longCurrency =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGDDD",
            "name": "asd",
            "cashInCents": 0
            }
            """;
    String longName =
        """
            {
            "accountType": "INVESTMENT",
            "currency": "SGD",
            "name": "asdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTMENTasdINVESTMENTINVESTMENTINVESTMENTINVESTME",
            "cashInCents": 0
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
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"cashInCents\",\"message\":\"Cash cannot be negative\"}],\"data\":null}"),
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
    Account returnedAccount = TestHelper.getTestAccount();
    when(service.getAccount(1)).thenReturn(Optional.of(returnedAccount));

    MvcResult result =
        mockMvc
            .perform(get("/api/account/1").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"id\":1,\"accountType\":\"INVESTMENT\",\"currency\":\"SGD\",\"description\":\"Account description\",\"name\":\"Test Account\",\"cashInCents\":1000}}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Get_account_that_is_deleted() throws Exception {
    Account returnedAccount = TestHelper.getTestAccount();
    returnedAccount.setDeleted(true);
    when(service.getAccount(1)).thenReturn(Optional.of(returnedAccount));

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
  void Get_all_account_with_deleted_account_is_successful() throws Exception {
    Account deletedAccount = TestHelper.getTestAccount();
    Account notDeletedAccount = TestHelper.getTestAccount();
    deletedAccount.setDeleted(true);
    deletedAccount.setId(2);
    when(service.getAllAccounts()).thenReturn(List.of(notDeletedAccount, deletedAccount));

    MvcResult result =
            mockMvc
                    .perform(get("/api/account/").contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":{\"accounts\":[{\"id\":1,\"accountType\":\"INVESTMENT\",\"currency\":\"SGD\",\"description\":\"Account description\",\"name\":\"Test Account\",\"cashInCents\":1000}]}}";
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
}
