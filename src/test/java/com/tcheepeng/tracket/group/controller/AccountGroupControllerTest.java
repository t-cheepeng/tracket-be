package com.tcheepeng.tracket.group.controller;

import static com.tcheepeng.tracket.common.TestHelper.assertInternalServerError;
import static org.mockito.Mockito.doThrow;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcheepeng.tracket.group.controller.request.CreateAccountAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.service.AccountGroupService;
import com.tcheepeng.tracket.integration.common.TestWebSecurityConfiguration;
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
@WebMvcTest(controllers = AccountGroupController.class)
public class AccountGroupControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AccountGroupService service;

  public static Stream<Arguments> getBadGroupAccountRequests() {
    return Stream.of(
        Arguments.of(
            CreateAccountAccountGroupRequest.builder().accountGroupId(0).build(),
            """
            {
              "status": "FAIL",
              "errors": [
                {
                  "code": "accountId",
                  "message": "ID must not be empty"
                }
              ],
              "data": null
            }
            """),
        Arguments.of(
            CreateAccountAccountGroupRequest.builder().accountId(0).build(),
            """
            {
              "status": "FAIL",
              "errors": [
                {
                  "code": "accountGroupId",
                  "message": "ID must not be empty"
                }
              ],
              "data": null
            }
          """),
        Arguments.of(
            CreateAccountAccountGroupRequest.builder().accountId(0).accountGroupId(-1).build(),
            """
            {
              "status": "FAIL",
              "errors": [
                {
                  "code": "accountGroupId",
                  "message": "ID must be positive"
                }
              ],
              "data": null
            }
            """),
        Arguments.of(
            CreateAccountAccountGroupRequest.builder().accountId(-2).accountGroupId(0).build(),
            """
            {
              "status": "FAIL",
              "errors": [
                {
                  "code": "accountId",
                  "message": "ID must be positive"
                }
              ],
              "data": null
            }
            """));
  }

  @Test
  void Create_account_group_is_successful() throws Exception {
    CreateAccountGroupRequest request =
        CreateAccountGroupRequest.builder().name("test-name").currency("SGD").build();
    ObjectMapper objectMapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_group_service_throws_exception() throws Exception {
    CreateAccountGroupRequest request =
        CreateAccountGroupRequest.builder().name("test-name").currency("SGD").build();
    ObjectMapper objectMapper = new ObjectMapper();

    doThrow(RuntimeException.class).when(service).createAccountGroup(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();

    assertInternalServerError(result.getResponse().getContentAsString());
  }

  @Test
  void Create_account_group_data_integrity_violation_currency() throws Exception {
    CreateAccountGroupRequest request =
        CreateAccountGroupRequest.builder().name("test-name").currency("ABC").build();
    ObjectMapper objectMapper = new ObjectMapper();

    DataIntegrityViolationException exception = new DataIntegrityViolationException("fk_currency");
    doThrow(exception).when(service).createAccountGroup(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency does not exist\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_group_data_integrity_violation_name() throws Exception {
    CreateAccountGroupRequest request =
        CreateAccountGroupRequest.builder().name("test-name").currency("SGD").build();
    ObjectMapper objectMapper = new ObjectMapper();

    DataIntegrityViolationException exception = new DataIntegrityViolationException("uk_name");
    doThrow(exception).when(service).createAccountGroup(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"name\",\"message\":\"Name already exists\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }

  @Test
  void Create_account_group_bad_request() throws Exception {
    CreateAccountGroupRequest[] requests =
        new CreateAccountGroupRequest[] {
          CreateAccountGroupRequest.builder().currency("SGD").build(),
          CreateAccountGroupRequest.builder().name("test-name").build(),
          CreateAccountGroupRequest.builder().name("test-name").currency("SGDDD").build(),
          CreateAccountGroupRequest.builder()
              .name(
                  "this name is going to be very loooooonggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggloooooonggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg.......................")
              .currency("SGD")
              .build(),
        };
    String[] expectedErrorReplies =
        new String[] {
          "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"name\",\"message\":\"Account name cannot be empty\"}],\"data\":null}",
          "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency does not exist\"}],\"data\":null}",
          "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"currency\",\"message\":\"Currency cannot be more than 4 characters\"}],\"data\":null}",
          "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"name\",\"message\":\"Name cannot be more than 255 characters\"}],\"data\":null}"
        };
    ObjectMapper objectMapper = new ObjectMapper();

    for (int i = 0; i < requests.length; i++) {
      CreateAccountGroupRequest request = requests[i];
      MvcResult result =
          mockMvc
              .perform(
                  post("/api/group/create")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isBadRequest())
              .andReturn();

      String jsonBody = result.getResponse().getContentAsString();
      String expectedJson = expectedErrorReplies[i];
      assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
    }
  }

  @Test
  void Group_account_is_successful() throws Exception {
    CreateAccountAccountGroupRequest request =
        CreateAccountAccountGroupRequest.builder().accountId(0).accountGroupId(0).build();
    ObjectMapper objectMapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/group")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson = "{\"status\":\"SUCCESS\",\"errors\":null,\"data\":null}";
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @ParameterizedTest
  @MethodSource("getBadGroupAccountRequests")
  void Group_account_bad_request(CreateAccountAccountGroupRequest request, String expectedJson)
      throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/group/group")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String jsonBody = result.getResponse().getContentAsString();
    assertEquals(expectedJson, jsonBody, JSONCompareMode.STRICT);
  }

  @Test
  void Group_account_data_integrity_violation_account_id() throws Exception {
    CreateAccountAccountGroupRequest request =
            CreateAccountAccountGroupRequest.builder().accountId(0).accountGroupId(0).build();
    ObjectMapper objectMapper = new ObjectMapper();

    DataIntegrityViolationException exception = new DataIntegrityViolationException("fk_account_id");
    doThrow(exception).when(service).groupAccount(request);

    MvcResult result =
            mockMvc
                    .perform(
                            post("/api/group/group")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"accountId\",\"message\":\"Account does not exist\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }


  @Test
  void Group_account_data_integrity_violation_account_group_id() throws Exception {
    CreateAccountAccountGroupRequest request =
            CreateAccountAccountGroupRequest.builder().accountId(0).accountGroupId(0).build();
    ObjectMapper objectMapper = new ObjectMapper();

    DataIntegrityViolationException exception = new DataIntegrityViolationException("fk_account_group_id");
    doThrow(exception).when(service).groupAccount(request);

    MvcResult result =
            mockMvc
                    .perform(
                            post("/api/group/group")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();
    String jsonBody = result.getResponse().getContentAsString();
    String expectedJson =
            "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"accountGroupId\",\"message\":\"Account group does not exist\"}],\"data\":null}";
    assertEquals(jsonBody, expectedJson, JSONCompareMode.STRICT);
  }
}
