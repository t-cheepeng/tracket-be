package com.tcheepeng.tracket.group.controller;

import com.tcheepeng.tracket.common.Constants;
import com.tcheepeng.tracket.common.response.ApiResponse;
import com.tcheepeng.tracket.group.controller.request.CreateAccountAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.response.GroupMappingsResponse;
import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.group.service.AccountGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
            @Valid @RequestBody CreateAccountAccountGroupRequest request) {
        accountGroupService.groupAccount(request);
        return new ResponseEntity<>(Constants.EMPTY_SUCCESS_REPLY, HttpStatus.OK);
    }

    @Operation(summary = "Get all groups and account to group mappings")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "All groups and account to group mappings retrieved. Will return empty " +
                                    "lists as well",
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
        List<AccountGroup> groups = accountGroupService.getAllGroups();
        List<AccountAccountGroup> accountGroupMappings = accountGroupService.getAllAccountGroupMappings();

        return new ResponseEntity<>(ApiResponse.builder().status(ApiResponse.Status.SUCCESS).data(response).build(),
                                    HttpStatus.OK);
    }
}
