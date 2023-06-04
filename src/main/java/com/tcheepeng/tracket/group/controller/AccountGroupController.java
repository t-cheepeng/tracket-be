package com.tcheepeng.tracket.group.controller;

import com.tcheepeng.tracket.common.Constants;
import com.tcheepeng.tracket.common.response.ApiResponse;
import com.tcheepeng.tracket.group.controller.request.AccountAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.response.GroupMappingResponse;
import com.tcheepeng.tracket.group.controller.response.GroupMappingsResponse;
import com.tcheepeng.tracket.group.service.AccountGroupService;
import com.tcheepeng.tracket.group.service.dto.GroupMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group")
public class AccountGroupController {

  private final AccountGroupService accountGroupService;

  public AccountGroupController(final AccountGroupService accountGroupService) {
    this.accountGroupService = accountGroupService;
  }

  @Operation(summary = "Create an account group")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account group is created successfully",
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
  public ResponseEntity<ApiResponse> createAccountGroup(
      @Valid @RequestBody CreateAccountGroupRequest request) {
    accountGroupService.createAccountGroup(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Group an account under account group")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account is grouped under account group successfully",
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
      value = "/group",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> groupAccount(
      @Valid @RequestBody AccountAccountGroupRequest request) {
    accountGroupService.groupAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Ungroup an account from a group")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account is ungrouped successfully",
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
  @DeleteMapping(
      value = "/group",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> ungroupAccount(
      @Valid @RequestBody AccountAccountGroupRequest request) {
    accountGroupService.ungroupAccount(request);
    return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
  }

  @Operation(summary = "Get all groups and account to group mappings")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "All groups and account to group mappings retrieved. Will return empty "
                    + "lists as well",
            content = {
              @Content(
                  schema = @Schema(implementation = GroupMappingsResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            """
                              {
                                "status": "SUCCESS",
                                 "data": {
                                 "accountGroups": [
                                    {
                                        "name": "SG Group",
                                        "currency": "SGD",
                                        "accounts": [1,2,3]
                                    }
                                 ],
                                  }
                              }
                            """)
                  })
            }),
      })
  @GetMapping(value = "/map/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse> getAllGroupsAndMapping() {
    List<GroupMapping> groupMappings = accountGroupService.getGroupMappings();
    List<GroupMappingResponse> responseData =
        groupMappings.stream()
            .map(
                groupMapping ->
                    GroupMappingResponse.builder()
                        .id(groupMapping.getId())
                        .name(groupMapping.getName())
                        .currency(groupMapping.getCurrency())
                        .accountIdUnderGroup(groupMapping.getAccountIdUnderGroup())
                        .build())
            .toList();

    return new ResponseEntity<>(
        ApiResponse.builder()
            .status(ApiResponse.Status.SUCCESS)
            .data(GroupMappingsResponse.builder().groupMappings(responseData).build())
            .build(),
        HttpStatus.OK);
  }
}
