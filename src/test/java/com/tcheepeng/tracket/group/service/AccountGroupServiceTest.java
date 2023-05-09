package com.tcheepeng.tracket.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.group.repository.AccountAccountGroupRepository;
import com.tcheepeng.tracket.group.repository.AccountGroupRepository;
import com.tcheepeng.tracket.group.service.dto.GroupMapping;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountGroupServiceTest {

  @InjectMocks AccountGroupService accountGroupService;

  @Mock AccountGroupRepository repository;

  @Mock AccountAccountGroupRepository accountAccountGroupRepository;

  @Test
  public void Create_account_group_is_successful() {
    CreateAccountGroupRequest request =
        CreateAccountGroupRequest.builder().name("test-name").currency("SGD").build();
    ArgumentCaptor<AccountGroup> captor = ArgumentCaptor.forClass(AccountGroup.class);

    accountGroupService.createAccountGroup(request);

    verify(repository).save(captor.capture());
    assertThat(captor.getValue())
        .returns("test-name", from(AccountGroup::getName))
        .returns("SGD", from(AccountGroup::getCurrency));
  }

  @Test
  void Get_group_mapping_is_successful() {
    AccountAccountGroup accountNotInTestGroup = TestHelper.getTestAccountAccountGroup();
    accountNotInTestGroup.getAccountAccountGroup().setAccountGroupId(2);
    when(repository.findAll()).thenReturn(List.of(TestHelper.getTestAccountGroup()));
    when(accountAccountGroupRepository.findAll())
        .thenReturn(List.of(TestHelper.getTestAccountAccountGroup(), accountNotInTestGroup));

    List<GroupMapping> groups = accountGroupService.getGroupMappings();

    assertThat(groups)
        .containsExactly(
            GroupMapping.builder()
                .id(1)
                .name("ABC")
                .currency("SGD")
                .accountIdUnderGroup(List.of(1))
                .build());
  }
}
