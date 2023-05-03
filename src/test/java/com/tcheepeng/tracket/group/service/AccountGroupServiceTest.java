package com.tcheepeng.tracket.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.verify;import static org.mockito.Mockito.when;

import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.group.repository.AccountGroupRepository;
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
}
